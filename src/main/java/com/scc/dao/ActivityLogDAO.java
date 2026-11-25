package com.scc.dao;

import java.sql.*;

public class ActivityLogDAO {

    public ActivityLogDAO() {
        initializeTables();
    }

    public static void createLoginLogsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS loginlogs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    role TEXT,
                    status TEXT NOT NULL,
                    LoginDate TEXT NOT NULL
                )
                """;

        try (Connection initConn = DatabaseConnection.getRouteConnection();
             Statement stmt = initConn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating loginlogs table: " + e.getMessage());
        }
    }

    public static void createRegisterLogsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS registerlogs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    role TEXT NOT NULL,
                    operator TEXT,
                    status TEXT NOT NULL,
                    date TEXT NOT NULL
                )
                """;

        try (Connection userConn = DatabaseConnection.getUserConnection();
             Statement stmt = userConn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating registerlogs table: " + e.getMessage());
        }
    }

    public static void initializeTables() {
        createLoginLogsTable();
        createRegisterLogsTable();
    }

    public void LogLogin(String username, String status) {
        UserDAO.createTableIfNotExists();
        createLoginLogsTable();

        String role = null;
        String roleSql = "SELECT role FROM users WHERE username = ?";

        try (Connection userConn = DatabaseConnection.getUserConnection();
             PreparedStatement roleStmt = userConn.prepareStatement(roleSql)) {

            roleStmt.setString(1, username);
            try (ResultSet rs = roleStmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user role: " + e.getMessage());
        }

        String insertSql = "INSERT INTO loginlogs (username, role, status, LoginDate) VALUES (?, ?, ?, datetime('now', 'localtime'))";

        try (Connection initConn = DatabaseConnection.getRouteConnection();
             PreparedStatement insertStmt = initConn.prepareStatement(insertSql)) {

            insertStmt.setString(1, username);
            insertStmt.setString(2, role);
            insertStmt.setString(3, status);
            insertStmt.executeUpdate();

            System.out.println("Activity Log successfully recorded.");
        } catch (SQLException e) {
            System.out.println("Error saving activity log: " + e.getMessage());
        }
    }

    public void loglogout(String username, String status) {
        UserDAO.createTableIfNotExists();
        createLoginLogsTable();

        String role = null;
        String roleSql = "SELECT role FROM users WHERE username = ?";

        try (Connection userConn = DatabaseConnection.getUserConnection();
             PreparedStatement roleStmt = userConn.prepareStatement(roleSql)) {

            roleStmt.setString(1, username);
            try (ResultSet rs = roleStmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user role: " + e.getMessage());
        }

        String insertSql = "INSERT INTO loginlogs (username, role, status, LoginDate) VALUES (?, ?, ?, datetime('now', 'localtime'))";

        try (Connection initConn = DatabaseConnection.getRouteConnection();
             PreparedStatement insertStmt = initConn.prepareStatement(insertSql)) {

            insertStmt.setString(1, username);
            insertStmt.setString(2, role);
            insertStmt.setString(3, status);
            insertStmt.executeUpdate();

            System.out.println("Activity Log successfully recorded.");
        } catch (SQLException e) {
            System.out.println("Error saving activity log: " + e.getMessage());
        }
    }

    public void registerlog(String username, String role, String operator, String status) {
        createRegisterLogsTable();

        String insertSql = "INSERT INTO registerlogs (username, role, operator, status, date) VALUES (?, ?, ?, ?, datetime('now', 'localtime'))";

        try (Connection userConn = DatabaseConnection.getUserConnection();
             PreparedStatement insertStmt = userConn.prepareStatement(insertSql)) {

            insertStmt.setString(1, username);
            insertStmt.setString(2, role);
            insertStmt.setString(3, operator);
            insertStmt.setString(4, status);

            insertStmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving register log: " + e.getMessage());
        }
    }
    public void displayRegisterLogs() {
        // Replace table/column names below if yours differ in ActivityLogDAO.java
        String sql = "SELECT * FROM registerlogs";

        try (Connection conn = DatabaseConnection.getActivityLogConnection(); // or whichever connection registerlog() uses
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================");
            System.out.println("                              REGISTER LOGS LIST                                 ");
            System.out.println("=================================================================================");

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Print column headers dynamically so it works even if column names vary
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("%-18s%s", meta.getColumnName(i), (i < colCount ? " | " : ""));
            }
            System.out.println("\n---------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    System.out.printf("%-18s%s", (val != null ? val : ""), (i < colCount ? " | " : ""));
                }
                System.out.println();
            }

            if (!found) {
                System.out.println("No register logs found.");
            }
            System.out.println("=================================================================================");

        } catch (SQLException e) {
            System.out.println("Error displaying register logs: " + e.getMessage());
        }
    }
    public void displayLoginLogs() {
        // Match the table name and WHERE condition used in your LogLogin(...) method
        String sql = "SELECT * FROM loginlogs WHERE UPPER(status) IN ('LOGIN', 'LOGOUT')";

        try (Connection conn = DatabaseConnection.getActivityLogConnection(); // match LogLogin's connection
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================");
            System.out.println("                                LOGIN LOGS LIST                                  ");
            System.out.println("=================================================================================");

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            for (int i = 1; i <= colCount; i++) {
                System.out.printf("%-18s%s", meta.getColumnName(i), (i < colCount ? " | " : ""));
            }
            System.out.println("\n---------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    System.out.printf("%-18s%s", (val != null ? val : ""), (i < colCount ? " | " : ""));
                }
                System.out.println();
            }

            if (!found) {
                System.out.println("No login logs found.");
            }
            System.out.println("=================================================================================");

        } catch (SQLException e) {
            System.out.println("Error displaying login logs: " + e.getMessage());
        }
    }
}