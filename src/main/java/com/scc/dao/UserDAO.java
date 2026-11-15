package com.scc.dao;

import java.sql.*;

public class UserDAO {

    // Constructor: Automatically verifies and creates the 'users' table on startup
    public UserDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "email TEXT, " +
                "securityCode TEXT, " +
                "role TEXT NOT NULL, " +
                "status TEXT NOT NULL)";

        try (Connection conn = DatabaseConnection.getUserConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    // Generates a random 6-digit security code for email verification
    private String generateSecurityCode() {
        int code = 100000 + new java.util.Random().nextInt(900000);
        return String.valueOf(code);
    }

    private static final String ADMIN_EMAIL = "Kennethcanon99@gmail.com";

    public boolean registerUser(String username, String password, String role) {


        if (isUserExists(username)) {
            System.out.println("Error: Username '" + username + "' is already taken.");
            return false;
        }

        String countSql = "SELECT COUNT(*) FROM users";
        String insertSql = "INSERT INTO users(username, password, securityCode, role, status) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getUserConnection()) {

            // 2. Check table count to determine if this is the very first user
            int userCount = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) {
                    userCount = rs.getInt(1);
                }
            }

            boolean isFirstUser = (userCount == 0);

            // Reset the auto-increment sequence back if the table is empty
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

            // 3. Insert the new user into the 'users' table
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, securityCode);
                pstmt.setString(4, role.toUpperCase());
                pstmt.setString(5, status);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    if (isFirstUser) {
                        System.out.println("\n--------------------------------------------------");
                        System.out.println(" [SUCCESS] First account created!");
                        System.out.println(" Status: ACTIVE (Directly logged in, no code required)");
                        System.out.println("--------------------------------------------------\n");
                    } else {
                        // Send security code to the fixed ADMIN email
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
        String userSql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(userSql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        String updatesql = "UPDATE users SET role = ?, status = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(updatesql)) {

            pstmt.setString(1, newRole);
            pstmt.setString(2, newStatus);
            pstmt.setString(3, username);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public void displayAllUsers() {
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
        String sql = "SELECT securityCode FROM users WHERE username = ? AND status = 'INACTIVE'";
        String updateSql = "UPDATE users SET status = 'ACTIVE' WHERE username = ?";

        try (Connection conn = DatabaseConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedCode = rs.getString("securityCode");

                    // Check if the entered code matches the one stored in the database
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
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:initialize.db");
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
}


