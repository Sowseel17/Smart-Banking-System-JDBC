import java.sql.*;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n====== SMART BANKING SYSTEM ======");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer Money");
            System.out.println("5. View Balance");
            System.out.println("6. Transaction History");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    transferMoney();
                    break;
                case 5:
                    viewBalance();
                    break;
                case 6:
                    transactionHistory();
                    break;
                case 7:
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ---------------- CREATE ACCOUNT WITH PIN ----------------
    public static void createAccount() {
        try (Connection con = DBConnection.getConnection()) {

            sc.nextLine();
            System.out.print("Enter Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Email: ");
            String email = sc.nextLine();

            System.out.print("Enter Initial Balance: ");
            double balance = sc.nextDouble();

            System.out.print("Set 4-digit PIN: ");
            int pin = sc.nextInt();

            String sql = "INSERT INTO accounts(name,email,balance,pin) VALUES(?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setDouble(3, balance);
            ps.setInt(4, pin);

            ps.executeUpdate();

            System.out.println("Account Created Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- VERIFY PIN ----------------
    public static boolean verifyPin(Connection con, int accountId, int enteredPin) throws Exception {
        String sql = "SELECT pin FROM accounts WHERE account_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, accountId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("pin") == enteredPin;
        }
        return false;
    }

    // ---------------- DEPOSIT ----------------
    public static void depositMoney() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Account ID: ");
            int accountId = sc.nextInt();

            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            if (!verifyPin(con, accountId, pin)) {
                System.out.println("Invalid PIN!");
                return;
            }

            System.out.print("Enter Amount: ");
            double amount = sc.nextDouble();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            ps.executeUpdate();

            PreparedStatement txn = con.prepareStatement(
                    "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
            txn.setInt(1, accountId);
            txn.setString(2, "DEPOSIT");
            txn.setDouble(3, amount);
            txn.executeUpdate();

            System.out.println("Deposit Successful!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- WITHDRAW ----------------
    public static void withdrawMoney() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Account ID: ");
            int accountId = sc.nextInt();

            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            if (!verifyPin(con, accountId, pin)) {
                System.out.println("Invalid PIN!");
                return;
            }

            System.out.print("Enter Amount: ");
            double amount = sc.nextDouble();

            PreparedStatement check = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?");
            check.setInt(1, accountId);
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amount) {

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                ps.setDouble(1, amount);
                ps.setInt(2, accountId);
                ps.executeUpdate();

                PreparedStatement txn = con.prepareStatement(
                        "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
                txn.setInt(1, accountId);
                txn.setString(2, "WITHDRAW");
                txn.setDouble(3, amount);
                txn.executeUpdate();

                System.out.println("Withdrawal Successful!");
            } else {
                System.out.println("Insufficient Balance!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- TRANSFER WITH ROLLBACK ----------------
    public static void transferMoney() {
        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false); // start transaction

            System.out.print("From Account ID: ");
            int fromId = sc.nextInt();

            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            if (!verifyPin(con, fromId, pin)) {
                System.out.println("Invalid PIN!");
                return;
            }

            System.out.print("To Account ID: ");
            int toId = sc.nextInt();

            System.out.print("Amount: ");
            double amount = sc.nextDouble();

            // Check balance
            PreparedStatement check = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?");
            check.setInt(1, fromId);
            ResultSet rs = check.executeQuery();

            if (!rs.next() || rs.getDouble("balance") < amount) {
                System.out.println("Insufficient Balance!");
                return;
            }

            // Deduct
            PreparedStatement deduct = con.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
            deduct.setDouble(1, amount);
            deduct.setInt(2, fromId);
            deduct.executeUpdate();

            // Add
            PreparedStatement add = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
            add.setDouble(1, amount);
            add.setInt(2, toId);
            add.executeUpdate();

            // Record transactions
            PreparedStatement txn = con.prepareStatement(
                    "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");

            txn.setInt(1, fromId);
            txn.setString(2, "TRANSFER SENT");
            txn.setDouble(3, amount);
            txn.executeUpdate();

            txn.setInt(1, toId);
            txn.setString(2, "TRANSFER RECEIVED");
            txn.setDouble(3, amount);
            txn.executeUpdate();

            con.commit(); // success

            System.out.println("Transfer Successful!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- VIEW BALANCE ----------------
    public static void viewBalance() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Account ID: ");
            int id = sc.nextInt();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?");
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
                System.out.println("Current Balance: " + rs.getDouble("balance"));
            else
                System.out.println("Account not found!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- TRANSACTION HISTORY ----------------
    public static void transactionHistory() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Account ID: ");
            int id = sc.nextInt();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM transactions WHERE account_id = ?");
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt("txn_id") + " | "
                                + rs.getString("type") + " | "
                                + rs.getDouble("amount") + " | "
                                + rs.getTimestamp("date"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
