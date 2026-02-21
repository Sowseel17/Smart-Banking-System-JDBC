# 🏦 Smart Banking System (Core Java + JDBC)

A console-based banking system built using Core Java and MySQL (JDBC).

## 🚀 Features
- Create Account
- Deposit Money
- Withdraw Money
- Transfer Money
- View Balance
- Transaction History
- Transaction Logging

## 🛠 Tech Stack
- Java
- JDBC
- MySQL
- VS Code

## 🗂 Project Structure

SmartBankingSystem │
 ├── DBConnection.java
 ├── Main.java 
 ├── lib/ (MySQL Connector - not included) 
 └── .vscode/
## ⚙ Setup Instructions

1. Install MySQL
2. Create database `smartbank`
3. Download MySQL Connector J
4. Add connector jar to `lib/` folder
5. Compile:
        javac -cp ".;lib/mysql-connector-j-9.6.0.jar" *.java 
        java -cp ".;lib/mysql-connector-j-9.6.0.jar" Main
---

Developed by Kotla Kameswara Sowseel