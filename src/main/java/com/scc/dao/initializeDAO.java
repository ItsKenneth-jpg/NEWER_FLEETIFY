package com.scc.dao;

import java.sql.*;

public class initializeDAO {

    public static boolean createVehicleTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS vehicle (
                    id TEXT PRIMARY KEY,
                    plate_number TEXT NOT NULL UNIQUE,
                    capacity INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error creating vehicle table: " + e.getMessage());
            return false;
        }
    }

    public static boolean createDriverTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS driver (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    license TEXT NOT NULL UNIQUE,
                    contact_number TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getDriverConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error creating driver table: " + e.getMessage());
            return false;
        }
    }

    public static boolean createRouteTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS route (
                    id TEXT PRIMARY KEY,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'IDLE'
                )
                """;

        try (Connection conn = DatabaseConnection.getRouteConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error creating route table: " + e.getMessage());
            return false;
        }
    }

    public static boolean createOngoingRouteTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Ongoingroute (
                    id TEXT,
                    route_id TEXT NOT NULL,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    vehicle_id TEXT NOT NULL,
                    vehicle_type TEXT NOT NULL,
                    driver_id TEXT NOT NULL,
                    driver TEXT NOT NULL,
                    assigned_by TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            ensureColumnExists(conn, "Ongoingroute", "route_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "origin", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "destination", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "vehicle_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "vehicle_type", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "driver_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "driver", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "assigned_by", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "Ongoingroute", "status", "TEXT NOT NULL DEFAULT 'ONGOING'");
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error creating ongoing route table: " + e.getMessage());
            return false;
        }
    }


    private static void ensureColumnExists(Connection conn, String tableName, String columnName, String columnDef) throws SQLException {
        String pragmaSql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pragmaSql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement alterStmt = conn.createStatement()) {
            alterStmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef);
        }
    }
    public static boolean createCompletedTripTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS completed_trip (
                    id TEXT PRIMARY KEY,
                    route_id TEXT NOT NULL,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    vehicle_id TEXT NOT NULL,
                    vehicle_type TEXT NOT NULL,
                    driver_id TEXT NOT NULL,
                    driver TEXT NOT NULL,
                    assigned_by TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'COMPLETED'
                )
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            ensureColumnExists(conn, "completed_trip", "route_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "origin", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "destination", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "vehicle_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "vehicle_type", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "driver_id", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "driver", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "assigned_by", "TEXT NOT NULL DEFAULT ''");
            ensureColumnExists(conn, "completed_trip", "status", "TEXT NOT NULL DEFAULT 'COMPLETED'");
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error creating completed_trip table: " + e.getMessage());
            return false;
        }
    }



    /**
     * Ensures all tables exist at application startup.
     */
    public static void initializeTables() {
        createVehicleTable();
        createDriverTable();
        createRouteTable();
        createOngoingRouteTable();
        createCompletedTripTable();
    }

    public static boolean registerVehicle(String id, String plateNumber, int capacity, String type, String status) {
        createVehicleTable();
        String sql = "INSERT INTO vehicle (id, plate_number, capacity, type, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, plateNumber);
            pstmt.setInt(3, capacity);
            pstmt.setString(4, type);
            pstmt.setString(5, status);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error registering vehicle: " + e.getMessage());
            return false;
        }
    }

    public static boolean registerDriver(String id, String name, String license, String contact, String role, String status) {
        createDriverTable();
        String sql = "INSERT INTO driver (id, name, license, contact_number, role, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getDriverConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, license);
            pstmt.setString(4, contact);
            pstmt.setString(5, role);
            pstmt.setString(6, status);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error registering driver: " + e.getMessage());
            return false;
        }
    }

    public static boolean registerRoute(String id, String origin, String destination, String status) {
        createRouteTable();
        String sql = "INSERT INTO route (id, origin, destination, status) VALUES (?, ?, ?, ?)";
        String finalStatus = (status == null || status.isBlank()) ? "IDLE" : status;

        try (Connection conn = DatabaseConnection.getRouteConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, finalStatus);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error registering route: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayIdleDrivers() {
        createDriverTable();
        String sql = "SELECT id, name, license, contact_number, status FROM driver WHERE UPPER(status) = 'IDLE'";

        try (Connection conn = DatabaseConnection.getDriverConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- AVAILABLE (IDLE) DRIVERS ---");
            System.out.printf("%-10s | %-20s | %-15s | %-15s | %-10s%n", "ID", "Name", "License", "Contact", "Status");
            System.out.println("--------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s | %-20s | %-15s | %-15s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("license"),
                        rs.getString("contact_number"),
                        rs.getString("status"));
            }
            if (!found) {
                System.out.println("No IDLE drivers available.");
            }
            return found;
        } catch (SQLException e) {
            System.out.println("Error loading idle drivers: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayIdleVehicles() {
        createVehicleTable();
        String sql = "SELECT id, plate_number, type, capacity, status FROM vehicle WHERE UPPER(status) = 'IDLE'";

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- AVAILABLE (IDLE) VEHICLES ---");
            System.out.printf("%-10s | %-15s | %-20s | %-10s | %-10s%n", "ID", "Plate No.", "Type", "Capacity", "Status");
            System.out.println("--------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s | %-15s | %-20s | %-10d | %-10s%n",
                        rs.getString("id"),
                        rs.getString("plate_number"),
                        rs.getString("type"),
                        rs.getInt("capacity"),
                        rs.getString("status"));
            }
            if (!found) {
                System.out.println("No IDLE vehicles available.");
            }
            return found;
        } catch (SQLException e) {
            System.out.println("Error loading idle vehicles: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayIdleRoutes() {
        createRouteTable();
        String sql = "SELECT id, origin, destination, status FROM route WHERE UPPER(status) = 'IDLE'";

        try (Connection conn = DatabaseConnection.getRouteConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- AVAILABLE (IDLE) ROUTES ---");
            System.out.printf("%-10s | %-20s | %-20s | %-10s%n", "ID", "Origin", "Destination", "Status");
            System.out.println("--------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s | %-20s | %-20s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("status"));
            }
            if (!found) {
                System.out.println("No IDLE routes available.");
            }
            return found;
        } catch (SQLException e) {
            System.out.println("Error loading idle routes: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayvehicle() {
        createVehicleTable();
        String sql = "SELECT id, plate_number, capacity, type, status FROM vehicle";

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=====================================================================================");
            System.out.println("                               REGISTERED VEHICLES LIST                              ");
            System.out.println("=====================================================================================");
            System.out.printf("%-10s | %-18s | %-12s | %-20s | %-10s%n", "ID", "Plate Number", "Capacity", "Type", "Status");
            System.out.println("-------------------------------------------------------------------------------------");

            boolean hasVehicles = false;
            while (rs.next()) {
                hasVehicles = true;
                System.out.printf("%-10s | %-18s | %-12d | %-20s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("plate_number"),
                        rs.getInt("capacity"),
                        rs.getString("type"),
                        rs.getString("status"));
            }

            if (!hasVehicles) {
                System.out.println("No registered vehicle found in database.");
            }
            System.out.println("=====================================================================================");
            return hasVehicles;

        } catch (SQLException e) {
            System.out.println("Error displaying vehicles: " + e.getMessage());
            return false;
        }
    }

    // --- Lookup Helpers for Validation ---

    public static String getIdleDriverName(String driverId) {
        createDriverTable();
        String sql = "SELECT name FROM driver WHERE UPPER(id) = UPPER(?) AND UPPER(status) = 'IDLE'";
        try (Connection conn = DatabaseConnection.getDriverConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        } catch (SQLException e) {
            System.out.println("Error validating driver: " + e.getMessage());
            return null;
        }
    }

    public static String getIdleVehicleType(String vehicleId) {
        createVehicleTable();
        String sql = "SELECT type FROM vehicle WHERE UPPER(id) = UPPER(?) AND UPPER(status) = 'IDLE'";
        try (Connection conn = DatabaseConnection.getVehicleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("type") : null;
            }
        } catch (SQLException e) {
            System.out.println("Error validating vehicle: " + e.getMessage());
            return null;
        }
    }

    public static String[] getIdleRouteDetails(String routeId) {
        createRouteTable();
        String sql = "SELECT origin, destination FROM route WHERE UPPER(id) = UPPER(?) AND UPPER(status) = 'IDLE'";
        try (Connection conn = DatabaseConnection.getRouteConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, routeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("origin"), rs.getString("destination")};
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating route: " + e.getMessage());
        }
        return null;
    }

    // --- Status Update & Ongoing Route Save ---

    public static boolean updateDriverStatus(String driverId, String newStatus) {
        createDriverTable();
        String sql = "UPDATE driver SET status = ? WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getDriverConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, driverId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating driver status: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateVehicleStatus(String vehicleId, String newStatus) {
        createVehicleTable();
        String sql = "UPDATE vehicle SET status = ? WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getVehicleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, vehicleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating vehicle status: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateRouteStatus(String routeId, String newStatus) {
        createRouteTable();
        String sql = "UPDATE route SET status = ? WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getRouteConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, routeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating route status: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayAllTrips() {
        createOngoingRouteTable();
        String sql = "SELECT id, route_id, origin, destination, vehicle_id, vehicle_type, driver_id, driver, assigned_by, status FROM route WHERE UPPER(TRIM(id)) LIKE 'TRP-%'";

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================================================================");
            System.out.println("                                                        TRIPS LIST                                                               ");
            System.out.println("=================================================================================================================================");
            System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                    "Trip ID", "Route ID", "Origin", "Destination", "Veh ID", "Vehicle Type", "Driver ID", "Driver", "Assigned By", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("route_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("vehicle_id"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_id"),
                        rs.getString("driver"),
                        rs.getString("assigned_by"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No trips found.");
            }
            System.out.println("=================================================================================================================================");
            return found;

        } catch (SQLException e) {
            System.out.println("Error displaying trips: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a trip's status. When status is COMPLETED, CANCELLED, or IDLE,
     * automatically switches the linked Driver, Vehicle, and Route back to IDLE.
     */
    public static boolean updateTripStatus(String tripId, String newStatus) {
        createOngoingRouteTable();
        String selectSql = "SELECT driver_id, vehicle_id, route_id FROM Ongoingroute WHERE UPPER(TRIM(id)) = UPPER(TRIM(?))";
        String updateTripSql = "UPDATE Ongoingroute SET status = ? WHERE UPPER(TRIM(id)) = UPPER(TRIM(?))";

        String driverId = null;
        String vehicleId = null;
        String routeId = null;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setString(1, tripId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[ERROR] Trip ID '" + tripId + "' not found.");
                    return false;
                }
                driverId = rs.getString("driver_id");
                vehicleId = rs.getString("vehicle_id");
                routeId = rs.getString("route_id");
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateTripSql)) {
                updateStmt.setString(1, newStatus);
                updateStmt.setString(2, tripId);
                if (updateStmt.executeUpdate() <= 0) {
                    return false;
                }
                if ("COMPLETED".equalsIgnoreCase(newStatus)) {
                    boolean transferred = transferTrip(tripId);
                    if (transferred) {
                        System.out.println("[INFO] Trip '" + tripId + "' transferred from Ongoingroute to completed_trip.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error updating trip status: " + e.getMessage());
            return false;
        }
        if("Cancel".equals(newStatus)){
            System.out.println("returning...");


        }

        if ("COMPLETED".equalsIgnoreCase(newStatus)
                || "CANCELLED".equalsIgnoreCase(newStatus)
                || "IDLE".equalsIgnoreCase(newStatus)) {

            if (driverId != null && !driverId.isBlank()) {
                updateDriverStatus(driverId, "IDLE");
            }
            if (vehicleId != null && !vehicleId.isBlank()) {
                updateVehicleStatus(vehicleId, "IDLE");
            }
            if (routeId != null && !routeId.isBlank()) {
                updateRouteStatus(routeId, "IDLE");
            }
            System.out.println("[INFO] Linked Driver (" + driverId + "), Vehicle (" + vehicleId + "), and Route (" + routeId + ") switched to IDLE.");
        }

        return true;
    }

    public static boolean saveOngoingRoute(
            String tripId,
            String routeId,
            String origin,
            String destination,
            String vehicleId,
            String vehicleType,
            String driverId,
            String driverName,
            String assignedBy,
            String status
    ) {
        createOngoingRouteTable();
        String sql = """
                INSERT INTO Ongoingroute (
                    id, route_id, origin, destination,
                    vehicle_id, vehicle_type, driver_id, driver,
                    assigned_by, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tripId);
            pstmt.setString(2, routeId.toUpperCase());
            pstmt.setString(3, origin);
            pstmt.setString(4, destination);
            pstmt.setString(5, vehicleId.toUpperCase());
            pstmt.setString(6, vehicleType);
            pstmt.setString(7, driverId.toUpperCase());
            pstmt.setString(8, driverName);
            pstmt.setString(9, assignedBy);
            pstmt.setString(10, status);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database Error saving ongoing route: " + e.getMessage());
            return false;
        }
    }

    public static String generateNextVehicleId() {
        createVehicleTable();
        String sql = "SELECT id FROM vehicle WHERE id LIKE 'VEH-%'";
        int maxNum = 0;

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("VEH-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating vehicle ID: " + e.getMessage());
        }

        return String.format("VEH-%03d", maxNum + 1);
    }

    public static boolean isPlateExists(String plateNumber) {
        createVehicleTable();
        String sql = "SELECT 1 FROM vehicle WHERE UPPER(TRIM(plate_number)) = UPPER(TRIM(?)) LIMIT 1";

        try (Connection conn = DatabaseConnection.getVehicleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plateNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking plate number: " + e.getMessage());
            return false;
        }
    }

    public static boolean isLicenseExists(String license) {
        createDriverTable();
        String sql = "SELECT 1 FROM driver WHERE UPPER(TRIM(license)) = UPPER(TRIM(?)) LIMIT 1";

        try (Connection conn = DatabaseConnection.getDriverConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, license);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking license number: " + e.getMessage());
            return false;
        }
    }

    public static String generateNextRouteId() {
        createRouteTable();
        String sql = "SELECT id FROM route WHERE id LIKE 'RTE-%'";
        int maxNum = 0;

        try (Connection conn = DatabaseConnection.getRouteConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("RTE-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating route ID: " + e.getMessage());
        }

        return String.format("RTE-%03d", maxNum + 1);
    }

    public static String generateNextTripId() {
        createOngoingRouteTable();
        String sql = "SELECT id FROM route WHERE id LIKE 'TRP-%'";
        int maxNum = 0;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("TRP-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating trip ID: " + e.getMessage());
        }

        return String.format("TRP-%03d", maxNum + 1);
    }

    public static String generateNextDriverId() {
        createDriverTable();
        String sql = "SELECT id FROM driver WHERE id LIKE 'DRV-%'";
        int maxNum = 0;

        try (Connection conn = DatabaseConnection.getDriverConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("DRV-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating driver ID: " + e.getMessage());
        }

        return String.format("DRV-%03d", maxNum + 1);
    }

    public static boolean displayOngoingRoutes() {
        createOngoingRouteTable();
        String sql = """
                SELECT id, route_id, origin, destination, vehicle_id, vehicle_type, driver_id, driver, assigned_by, status
                FROM Ongoingroute
                WHERE UPPER(TRIM(status)) = 'ONGOING'
                  AND UPPER(TRIM(id)) LIKE 'TRP-%'
                  AND TRIM(COALESCE(driver, '')) <> ''
                  AND TRIM(COALESCE(vehicle_id, '')) <> ''
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================================================================");
            System.out.println("                                                      ONGOING ROUTES LIST                                                        ");
            System.out.println("=================================================================================================================================");
            System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-8s%n",
                    "Trip ID", "Route ID", "Origin", "Destination", "Veh ID", "Vehicle Type", "Driver ID", "Driver", "Assigned By", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-8s%n",
                        rs.getString("id"),
                        rs.getString("route_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("vehicle_id"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_id"),
                        rs.getString("driver"),
                        rs.getString("assigned_by"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No ongoing routes found.");
            }
            System.out.println("=================================================================================================================================");
            return true;

        } catch (SQLException e) {
            System.out.println("Error displaying ongoing routes: " + e.getMessage());
            return false;
        }
    }

    public static boolean displayAllDrivers() {
        createDriverTable();
        String sql = "SELECT id, name, license, contact_number, role, status FROM driver";

        try (Connection conn = DatabaseConnection.getDriverConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=========================================================================================================");
            System.out.println("                                        REGISTERED DRIVERS LIST                                          ");
            System.out.println("=========================================================================================================");
            System.out.printf("%-10s | %-18s | %-14s | %-15s | %-15s | %-10s%n",
                    "ID", "Name", "License", "Contact", "Role", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s | %-18s | %-14s | %-15s | %-15s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("license"),
                        rs.getString("contact_number"),
                        rs.getString("role"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No registered drivers found.");
            }
            System.out.println("=========================================================================================================");
            return true;

        } catch (SQLException e) {
            System.out.println("Error displaying drivers: " + e.getMessage());
            return false;
        }
    }
    public static boolean displayAllRoutes() {
        createRouteTable();
        String sql = "SELECT id, origin, destination, status FROM route WHERE UPPER(TRIM(id)) NOT LIKE 'TRP-%'";

        try (Connection conn = DatabaseConnection.getRouteConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=====================================================================");
            System.out.println("                        REGISTERED ROUTES LIST                       ");
            System.out.println("=====================================================================");
            System.out.printf("%-10s | %-20s | %-20s | %-10s%n", "ID", "Origin", "Destination", "Status");
            System.out.println("---------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s | %-20s | %-20s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No registered routes found.");
            }
            System.out.println("=====================================================================");
            return true;

        } catch (SQLException e) {
            System.out.println("Error displaying routes: " + e.getMessage());
            return false;
        }
    }
    public static boolean displayActiveTrips() {
        createOngoingRouteTable();
        String sql = """
                SELECT id, route_id, origin, destination, vehicle_id, vehicle_type, driver_id, driver, assigned_by, status
                FROM Ongoingroute
                WHERE UPPER(TRIM(COALESCE(status, ''))) <> 'COMPLETED'
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================================================================");
            System.out.println("                                                     ACTIVE TRIPS LIST                                                           ");
            System.out.println("=================================================================================================================================");
            System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                    "Trip ID", "Route ID", "Origin", "Destination", "Veh ID", "Vehicle Type", "Driver ID", "Driver", "Assigned By", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("route_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("vehicle_id"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_id"),
                        rs.getString("driver"),
                        rs.getString("assigned_by"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No active trips available to update.");
            }
            System.out.println("=================================================================================================================================");
            return found;

        } catch (SQLException e) {
            System.out.println("Error displaying active trips: " + e.getMessage());
            return false;
        }
    }
    public static boolean displayCompletedTrips() {
        createCompletedTripTable();
        String sql = """
                SELECT id, route_id, origin, destination, vehicle_id, vehicle_type, driver_id, driver, assigned_by, status
                FROM completed_trip
                """;

        try (Connection conn = DatabaseConnection.getCompletedRoute();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================================================================================================================");
            System.out.println("                                                     COMPLETED TRIPS LIST                                                        ");
            System.out.println("=================================================================================================================================");
            System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                    "Trip ID", "Route ID", "Origin", "Destination", "Veh ID", "Vehicle Type", "Driver ID", "Driver", "Assigned By", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8s | %-8s | %-15s | %-15s | %-8s | %-12s | %-10s | %-15s | %-12s | %-10s%n",
                        rs.getString("id"),
                        rs.getString("route_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("vehicle_id"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_id"),
                        rs.getString("driver"),
                        rs.getString("assigned_by"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No completed trips found.");
            }
            System.out.println("=================================================================================================================================");
            return found;

        } catch (SQLException e) {
            System.out.println("Error displaying completed trips: " + e.getMessage());
            return false;
        }
    }
    public static boolean transferTrip(String tripId) {
        createOngoingRouteTable();
        createCompletedTripTable();

        String insertSql = """
                INSERT OR IGNORE INTO completed_trip (
                    id, route_id, origin, destination,
                    vehicle_id, vehicle_type, driver_id, driver,
                    assigned_by, status
                )
                SELECT
                    id, route_id, origin, destination,
                    vehicle_id, vehicle_type, driver_id, driver,
                    assigned_by, 'COMPLETED'
                FROM Ongoingroute
                WHERE UPPER(TRIM(id)) = UPPER(TRIM(?))
                  AND UPPER(TRIM(status)) = 'COMPLETED'
                """;

        String deleteSql = """
                DELETE FROM Ongoingroute
                WHERE UPPER(TRIM(id)) = UPPER(TRIM(?))
                  AND UPPER(TRIM(status)) = 'COMPLETED'
                """;

        try (Connection conn = DatabaseConnection.getOngoingRoute()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                insertStmt.setString(1, tripId);
                deleteStmt.setString(1, tripId);

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    deleteStmt.executeUpdate();
                }

                conn.commit();
                return rowsInserted > 0;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Database Error transferring trip: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Connection Error transferring trip: " + e.getMessage());
            return false;
        }
    }





}