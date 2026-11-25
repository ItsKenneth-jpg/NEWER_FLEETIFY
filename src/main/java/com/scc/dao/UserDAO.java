package com.scc.dao;

import java.sql.*;
import java.util.Random;
import java.util.Set;

public class UserDAO {

    private static final String ADMIN_EMAIL = "Kennethcanon99@gmail.com";

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "FLEET DISPATCHER",
            "TRANSIT SCHEDULER",
            "ADMIN"
    );

    // Constructor: Automatically verifies and creates the 'users' table on startup
    public UserDAO() {
        createTableIfNotExists();
    }

    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "email TEXT, " +
                "securityCode TEXT, " +
                "role TEXT NOT NULL CHECK (role IN ('FLEET DISPATCHER', 'TRANSIT SCHEDULER', 'ADMIN')), " +
                "status TEXT NOT NULL)";

        try (Connection conn = DatabaseConnection.getUserConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    // Generates a random 6-digit security code for email verification
    private static String generateSecurityCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    private static String normalizeAndValidateRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String cleaned = role.trim().toUpperCase();
        return ALLOWED_ROLES.contains(cleaned) ? cleaned : null;
    }

    public boolean registerUser(String username, String password, String role) {
        createTableIfNotExists();

        String normalizedRole = normalizeAndValidateRole(role);
        if (normalizedRole == null) {
            System.out.println("Error: Invalid role '" + role + "'. Allowed roles are: FLEET DISPATCHER, TRANSIT SCHEDULER.");
            return false;
        }

        if (isUserExists(username)) {
            System.out.println("Error: Username '" + username + "' is already taken.");
            return false;
        }

        String countSql = "SELECT COUNT(*) FROM users";
        String insertSql = "INSERT INTO users(username, password, securityCode, role, status) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getUserConnection()) {

            int userCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) {
                    userCount = rs.getInt(1);
                }
            }

            boolean isFirstUser = (userCount == 0);

            if (isFirstUser) {
                String resetSeqSql = "DELETE FROM sqlite_sequence WHERE name='users'";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(resetSeqSql);
                } catch (SQLException e) {
                    // Ignored if sqlite_sequence table doesn't exist yet
                }
            }

            String status = isFirstUser ? "ACTIVE" : "INACTIVE";
            String securityCode = isFirstUser ? null : generateSecurityCode();

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, securityCode);
                pstmt.setString(4, normalizedRole);
                pstmt.setString(5, status);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    if (isFirstUser) {
                        System.out.println("\n--------------------------------------------------");
                        System.out.println(" [SUCCESS] First account created!");
                        System.out.println(" Status: ACTIVE (Directly logged in, no code required)");
                        System.out.println("--------------------------------------------------\n");
                    } else {
                        EmailService.sendSecurityCode(ADMIN_EMAIL, securityCode);

                        System.out.println("\n--------------------------------------------------");
                        System.out.println(" [SUCCESS] Account created! Status: INACTIVE");
                        System.out.println(" [SECURITY CODE SENT TO ADMIN EMAIL]: " + ADMIN_EMAIL);
                        System.out.println("--------------------------------------------------\n");
                    }
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database Registration Failed: " + e.getMessage());
        }

        return false;
    }

    public static boolean login(String username, String password) {
        createTableIfNotExists();
        String userSql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(userSql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Username not found!");
                    return false;
                } else if (!password.equals(rs.getString("password"))) {
                    System.out.println("Wrong password!");
                    return false;
                } else {
                    System.out.println("Login Success!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error checking User: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUser(String username, String newRole, String newStatus) {
        createTableIfNotExists();

        String normalizedRole = normalizeAndValidateRole(newRole);
        if (normalizedRole == null) {
            System.out.println("Error: Invalid role '" + newRole + "'. Allowed roles are: FLEET DISPATCHER, TRANSIT SCHEDULER.");
            return false;
        }

        String updatesql = "UPDATE users SET role = ?, status = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(updatesql)) {

            pstmt.setString(1, normalizedRole);
            pstmt.setString(2, newStatus);
            pstmt.setString(3, username);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public void displayAllUsers() {
        createTableIfNotExists();
        String sql = "SELECT id, username, securityCode, role, status FROM users";

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

    public boolean isUserExists(String username) {
        createTableIfNotExists();
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking user: " + e.getMessage());
        }
        return false;
    }

    public String role(String username, String password) {
        createTableIfNotExists();
        String role = null;
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error! " + e.getMessage());
        }
        return role;
    }

    public boolean verifyAccount(String username, String enteredCode) {
        createTableIfNotExists();
        String sql = "SELECT securityCode FROM users WHERE username = ? AND status = 'INACTIVE'";
        String updateSql = "UPDATE users SET status = 'ACTIVE' WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedCode = rs.getString("securityCode");

                    if (storedCode != null && storedCode.equals(enteredCode.trim())) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, username);
                            updateStmt.executeUpdate();
                            System.out.println("\n[SUCCESS] Account successfully verified and activated!");
                            return true;
                        }
                    } else {
                        System.out.println("\n[ERROR] Invalid security code.");
                    }
                } else {
                    System.out.println("\n[ERROR] User not found or already active.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Verification Error: " + e.getMessage());
        }
        return false;
    }

    public boolean isUserActive(String username) {
        createTableIfNotExists();
        String sql = "SELECT status FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    return "ACTIVE".equalsIgnoreCase(status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking user status: " + e.getMessage());
        }
        return false;
    }

    public boolean adminExists() {
        createTableIfNotExists();
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

        try (Connection conn = DatabaseConnection.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateUserStatus(String username, String newStatus) {
        // Check your users table/column names in initialize.db if they differ (e.g., user_status or account_status)
        String sql = "UPDATE users SET status = ? WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }
    public boolean sendActivationCode(String username) {
        createTableIfNotExists();

        if (!isUserExists(username)) {
            System.out.println("[ERROR] User '" + username + "' not found.");
            return false;
        }

        String newCode = generateSecurityCode();
        String updateSql = "UPDATE users SET securityCode = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, newCode);
            pstmt.setString(2, username);

            if (pstmt.executeUpdate() > 0) {
                EmailService.sendSecurityCode(ADMIN_EMAIL, newCode);
                System.out.println("[SECURITY CODE SENT TO ADMIN EMAIL]: " + ADMIN_EMAIL);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error generating/saving security code: " + e.getMessage());
        }
        return false;
    }
}