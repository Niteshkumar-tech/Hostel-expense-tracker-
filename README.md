# **Hostel Expense Tracker System**

A **Java-based desktop application** designed to help hostel students manage **personal and shared expenses** efficiently. The system allows users to record expenses, divide shared costs among participants, track balances, and manage payments.

## **Project Overview**

Managing expenses among hostel roommates can be difficult, especially when multiple students share the cost of food, electricity bills, groceries, and other daily expenses.

The **Hostel Expense Tracker System** provides a simple solution for recording and managing these expenses. It uses **Java** for application development and **MySQL** for database management.

## **Features**

* **User Management** — Add and manage users.
* **Personal Expenses** — Record individual expenses.
* **Shared Expenses** — Record expenses shared among multiple users.
* **Expense Splitting** — Divide shared costs among selected participants.
* **Balance Tracking** — Track balances between users.
* **Payments and Settlements** — Record payments and settled balances.
* **Expense Records** — View and manage expense history.
* **Database Management** — Store and manage data using MySQL.
* **User Interface** — Simple Java Swing-based graphical interface.

## **Technologies Used**

* **Java** — Core application development.
* **Java Swing** — Graphical User Interface (GUI).
* **MySQL** — Database management system.
* **JDBC** — Java Database Connectivity.
* **MySQL Connector/J** — Connection between the Java application and MySQL database.

## **Project Structure**

```text
Hostel-Expense-Tracker-System/
│
├── src/
│   └── hostel/
│       ├── db/
│       │   └── DBConnection.java
│       │
│       ├── model/
│       │   ├── Expense.java
│       │   ├── Payment.java
│       │   └── User.java
│       │
│       └── ui/
│           └── MainUI.java
│
├── database/
│   └── schema.sql
│
├── lib/
│   └── mysql-connector-j-9.3.0.jar
│
├── README.md

```

## **Database Setup**

1. Install **MySQL Server**.
2. Open **MySQL Workbench** or the MySQL command-line interface.
3. Create the required database.
4. Import and execute the SQL file available in the `database` folder.
5. Configure the **database URL, username, and password** in `DBConnection.java`.

## **How to Run the Project**

1. Clone or download the repository.
2. Make sure **Java** and **MySQL** are installed on your computer.
3. Set up the MySQL database using the provided SQL file.
4. Add **MySQL Connector/J** to the project classpath.
5. Configure the database connection in `DBConnection.java`.
6. Compile the Java source files.
7. Run the main application.

## **Requirements**

* **Java Development Kit (JDK)**
* **MySQL Server**
* **MySQL Connector/J**
* **Java IDE** such as IntelliJ IDEA, Eclipse, or NetBeans *(optional)*

## **Project Purpose**

This project demonstrates the practical implementation of:

* **Database Design**
* **Relational Database Concepts**
* **SQL Queries**
* **JDBC Connectivity**
* **Java Programming**
* **CRUD Operations**
* **Expense Management**
* **Shared Expense Calculations**

## **Author**

**Nitesh Kumar**

## **Disclaimer**

This project was developed for **educational purposes**.

## **License**

This project is intended for **educational use**.
