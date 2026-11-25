package com.scc.dao;

import java.sql.*;

public class adminDAO {

    private static final String ADMIN_CODE = "FLEETIFY-ADMIN";

    public adminDAO() {
        initializeTables();
    }

    public static void createAdminTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS admin (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    email TEXT,
                    securityCode TEXT,
                    role TEXT NOT NULL DEFAULT 'ADMIN',
                    status TEXT NOT NULL DEFAULT 'ACTIVE'
                )
                """;

        try (Connection conn = DatabaseConnection.getUserConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database Error creating admin table: " + e.getMessage());
        }
    }



    public static void initializeTables() {
        createAdminTable();

    }

    // Removed 'id' from parameters since the database auto-increments it
    public boolean login(String username, String password) {
        initializeTables();

        // --- STEP 1: Check the ADMIN table first ---
        String adminSql = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(adminSql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Found in the admin table!
                    System.out.println("Login Success! Welcome admin.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error checking admin: " + e.getMessage());
        }

        // --- STEP 2: Check the USER table ---
        String userSql = "SELECT * FROM user WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(userSql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");

                    // Check if regular user account is pending approval
                    if ("INACTIVE".equalsIgnoreCase(status)) {
                        System.out.println("Error: Your account is pending admin approval.");
                        return false;
                    }

                    // Regular user is active/approved
                    System.out.println("Login successful! Welcome, " + username + ".");
                    return true;

                } else {
                    // Not found in admin OR user table
                    System.out.println("Error: Invalid username or password.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error checking user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAdmin(String username, String newStatus) {
        createAdminTable();
        String updatesql = "UPDATE admin SET status = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(updatesql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating admin status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserStatusSafely(String username, String newStatus, String currentAdmin) {


        // Block self-deactivation at the database logic level
        if (username != null && currentAdmin != null
                && username.equalsIgnoreCase(currentAdmin)
                && "INACTIVE".equalsIgnoreCase(newStatus)) {
            System.out.println("Security Error: Admins are not allowed to deactivate their own account.");
            return false;
        }

        String updatesql = "UPDATE user SET status = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(updatesql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public void displayAllUsers() {
        createAdminTable();
        String sql = "SELECT id, username, securityCode, role, status FROM admin";

        try (Connection conn = DatabaseConnection.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=====================================================================================");
            System.out.println("                               REGISTERED USERS LIST                                 ");
            System.out.println("=====================================================================================");
            System.out.printf("%-5s | %-18s | %-16s | %-20s | %-10s\n", "ID", "Username", "Security Code", "Role", "Status");
            System.out.println("-------------------------------------------------------------------------------------");

            boolean hasUsers = false;
            while (rs.next()) {
                hasUsers = true;
                System.out.printf("%-5s | %-18s | %-16s | %-20s | %-10s\n",
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("securityCode"),
                        rs.getString("role"),
                        rs.getString("status")
                );
            }

            if (!hasUsers) {
                System.out.println("No users found in the database.");
            }
            System.out.println("=====================================================================================");

        } catch (SQLException e) {
            System.out.println("Error displaying users: " + e.getMessage());
        }
    }
}