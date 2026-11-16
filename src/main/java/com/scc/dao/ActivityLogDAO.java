package com.scc.dao;

import java.sql.*;

public class
ActivityLogDAO {

    public void LogLogin(String username, String status) {
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
        String role = null;

        String rolesql = "SELECT role FROM users WHERE username = ?";
        try (Connection userConn = DatabaseConnection.getUserConnection();
             PreparedStatement roleStmt = userConn.prepareStatement(rolesql)) {
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
        String insertSql = "INSERT INTO registerlogs (username, role, operator, status, date) VALUES (?, ?, ?, ?, datetime('now', 'localtime'))";


        try (Connection userConn = DatabaseConnection.getUserConnection();
             PreparedStatement roleStmt = userConn.prepareStatement(insertSql)) {
            roleStmt.setString(1, username);
            roleStmt.setString(2, role);
            roleStmt.setString(3, operator);
            roleStmt.setString(4, status);

            roleStmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error fetching user role: " + e.getMessage());
        }
    }


}