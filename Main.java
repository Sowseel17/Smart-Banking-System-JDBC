import java.sql.*;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n====== SMART BANKING SYSTEM ======");
            System.out.println("1. User Login");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    userMenu();
                    break;
                case 2:
                    adminMenu();
                    break;
                case 3:
                    System.out.println("Thank you for using Smart Banking System.");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ================= USER MENU =================

    private static void userMenu() {

        System.out.print("Enter Account ID: ");
        int accId = sc.nextInt();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM accounts WHERE account_id=?");
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Account not found!");
                return;
            }

            while (true) {
                System.out.println("\n---- USER MENU ----");
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. View Balance");
                System.out.println("4. Transaction History");
                System.out.println("5. Logout");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        deposit(con, accId);
                        break;
                    case 2:
                        withdraw(con, accId);
                        break;
                    case 3:
                        viewBalance(con, accId);
                        break;
                    case 4:
                        transactionHistory(con, accId);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= ADMIN MENU =================

    private static void adminMenu() {

        System.out.print("Enter Admin Password: ");
        String password = sc.next();

        if (!password.equals("admin123")) {
            System.out.println("Wrong password!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            while (true) {
                System.out.println("\n---- ADMIN MENU ----");
                System.out.println("1. Create Account");
                System.out.println("2. View All Accounts");
                System.out.println("3. Logout");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        createAccount(con);
                        break;
                    case 2:
                        viewAllAccounts(con);
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= FUNCTIONS =================

    private static void createAccount(Connection con) throws Exception {
        sc.nextLine();
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO accounts(name,email,balance) VALUES(?,?,0)");
        ps.setString(1, name);
        ps.setString(2, email);

        ps.executeUpdate();
        System.out.println("Account created successfully!");
    }

    private static void deposit(Connection con, int accId) throws Exception {
        System.out.print("Enter amount: ");
        double amount = sc.nextDouble();

        PreparedStatement ps = con.prepareStatement(
                "UPDATE accounts SET balance=balance+? WHERE account_id=?");
        ps.setDouble(1, amount);
        ps.setInt(2, accId);
        ps.executeUpdate();

        recordTransaction(con, accId, "DEPOSIT", amount);

        System.out.println("Deposit successful!");
    }

    private static void withdraw(Connection con, int accId) throws Exception {
        System.out.print("Enter amount: ");
        double amount = sc.nextDouble();

        PreparedStatement check = con.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id=?");
        check.setInt(1, accId);
        ResultSet rs = check.executeQuery();
        rs.next();

        if (rs.getDouble("balance") < amount) {
            System.out.println("Insufficient balance!");
            return;
        }

        PreparedStatement ps = con.prepareStatement(
                "UPDATE accounts SET balance=balance-? WHERE account_id=?");
        ps.setDouble(1, amount);
        ps.setInt(2, accId);
        ps.executeUpdate();

        recordTransaction(con, accId, "WITHDRAW", amount);

        System.out.println("Withdrawal successful!");
    }

    private static void viewBalance(Connection con, int accId) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id=?");
        ps.setInt(1, accId);
        ResultSet rs = ps.executeQuery();
        rs.next();

        System.out.println("Current Balance: " + rs.getDouble("balance"));
    }

    private static void transactionHistory(Connection con, int accId) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM transactions WHERE account_id=?");
        ps.setInt(1, accId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt("txn_id") + " | "
                    + rs.getString("type") + " | "
                    + rs.getDouble("amount") + " | "
                    + rs.getTimestamp("date"));
        }
    }

    private static void viewAllAccounts(Connection con) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM accounts");

        while (rs.next()) {
            System.out.println(rs.getInt("account_id") + " | "
                    + rs.getString("name") + " | "
                    + rs.getString("email") + " | "
                    + rs.getDouble("balance"));
        }
    }

    private static void recordTransaction(Connection con, int accId,
                                          String type, double amount) throws Exception {

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
        ps.setInt(1, accId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.executeUpdate();
    }
}
