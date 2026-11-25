package com.scc;

import com.scc.dao.ActivityLogDAO;
import com.scc.dao.UserDAO;
import com.scc.dao.adminDAO;
import com.scc.dao.initializeDAO;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final adminDAO ADMIN_DAO = new adminDAO();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final initializeDAO ROUTE_DAO = new initializeDAO();
    private static final ActivityLogDAO ACTIVITY_LOG_DAO = new ActivityLogDAO();

    public static void main(String[] args) {
        initializeDAO.initializeTables();

        while (true) {
            System.out.println("\n================================");
            System.out.println("           FLEETIFY");
            System.out.println("================================");
            System.out.println("1. LOGIN ");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n================================");
                    System.out.println("            LOGIN");
                    System.out.println("================================");
                    System.out.print("Enter username: ");
                    String loginuser = sc.nextLine();
                    System.out.print("Enter Password: ");
                    String loginPass = sc.nextLine();

                    boolean isloggedIn = UserDAO.login(loginuser, loginPass);

                    if (isloggedIn) {
                        String userRole = USER_DAO.role(loginuser, loginPass);
                        ACTIVITY_LOG_DAO.LogLogin(loginuser, "LOGIN");

                        if (userRole != null) {
                            if (userRole.equalsIgnoreCase("ADMIN")) {
                                String currentLoggedInAdmin = loginuser;

                                dashboardloop:
                                while (true) {
                                    System.out.println("\n========== FLEETIFY ==========");
                                    System.out.println("[1] Register");
                                    System.out.println("[2] Trips");
                                    System.out.println("[3] View");
                                    System.out.println("[4] Update");
                                    System.out.println("[0] Logout");
                                    System.out.println("==============================");
                                    System.out.print("Select an option: ");
                                    String choicee = sc.nextLine().trim();

                                    switch (choicee) {
                                        case "1":
                                            handleAdminRegisterMenu(currentLoggedInAdmin);
                                            break;
                                        case "2":
                                            assignTrip(currentLoggedInAdmin);
                                            break;
                                        case "3":
                                            handleAdminViewMenu();
                                            break;
                                        case "4":
                                            handleAdminUpdateMenu();
                                            break;
                                        case "0":
                                            System.out.println("Returning...");
                                            ACTIVITY_LOG_DAO.loglogout(loginuser, "LOGOUT");
                                            break dashboardloop;
                                        default:
                                            System.out.println("Error: Invalid Option.");
                                            break;
                                    }
                                }
                            } else {
                                System.out.println("Logged in as " + userRole + ".");
                            }
                        }
                    } else {
                        System.out.println("Login failed. Please try again.");
                    }
                    break;

                case "2":
                    System.out.println("\n================================");
                    System.out.println("            REGISTER");
                    System.out.println("================================");

                    System.out.print("Enter Username: ");
                    String username = sc.nextLine().trim();

                    System.out.print("Enter Password: ");
                    String password = sc.nextLine().trim();

                    if (!USER_DAO.adminExists()) {
                        String Role = "ADMIN";
                        System.out.println("First Account detected!");

                        boolean success = USER_DAO.registerUser(username, password, Role);
                        if (!success) {
                            System.out.println("\n[FAILED] Registration rejected.");
                        } else {
                            System.out.println("\n[SUCCESS] Admin account created!");
                        }
                    } else {
                        System.out.print("Enter role (Fleet Dispatcher / Transit Scheduler): ");
                        String role = sc.nextLine().trim();

                        if ("Fleet Dispatcher".equalsIgnoreCase(role) || "Transit Scheduler".equalsIgnoreCase(role)) {
                            role = "Fleet Dispatcher".equalsIgnoreCase(role) ? "Fleet Dispatcher" : "Transit Scheduler";

                            boolean success = USER_DAO.registerUser(username, password, role);

                            if (success) {
                                System.out.println("\n[SUCCESS] Registration completed!");

                                if (!USER_DAO.isUserActive(username)) {
                                    boolean verified = verifyWithCode(username);
                                    ACTIVITY_LOG_DAO.registerlog(username, role, null, "Registered");

                                    if (!verified) {
                                        System.out.println("\n[LOCKED/FAILED] Verification failed. Ask the admin for activating the account.");
                                        ACTIVITY_LOG_DAO.registerlog(username, role, null, "Not Active");
                                    }
                                }
                            } else {
                                System.out.println("\n[FAILED] Registration rejected.");
                            }
                        } else if ("ADMIN".equalsIgnoreCase(role)) {
                            System.out.println("Registration failed: An admin account already exists.");
                        } else {
                            System.out.println("\n[ERROR] Invalid role. Only 'Fleet Dispatcher' or 'Transit Scheduler' are allowed.");
                        }
                    }
                    break;

                case "3":
                    System.out.println("Exiting Program. Goodbye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Error: Invalid main menu option.");
                    break;
            }
        }
    }

    private static void handleAdminRegisterMenu(String currentLoggedInAdmin) {
        while (true) {
            System.out.println("\n================================");
            System.out.println("        REGISTER OPTIONS");
            System.out.println("================================");
            System.out.println("1. Register User");
            System.out.println("2. Register Driver");
            System.out.println("3. Register Vehicle");
            System.out.println("4. Register Route");
            System.out.println("0. Back");
            System.out.println("================================");
            System.out.print("Select option: ");

            String regChoice = sc.nextLine().trim();

            switch (regChoice) {
                case "1":
                    registerUserByAdmin(currentLoggedInAdmin);
                    break;
                case "2":
                    registerDriver();
                    break;
                case "3":
                    registerVehicle();
                    break;
                case "4":
                    registerRoute();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Error: Invalid register option.");
                    break;
            }
        }
    }

    private static void registerUserByAdmin(String currentLoggedInAdmin) {
        System.out.println("\n================================");
        System.out.println("         REGISTER USER");
        System.out.println("================================");

        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        System.out.print("Enter role (ADMIN, Fleet Dispatcher, Transit Scheduler): ");
        String role = sc.nextLine().trim();

        if ("ADMIN".equalsIgnoreCase(role)) {
            if (USER_DAO.adminExists()) {
                System.out.println("Registration failed: An admin account already exists.");
                return;
            }
            role = "ADMIN";
        } else if ("Transit Scheduler".equalsIgnoreCase(role)) {
            role = "Transit Scheduler";
        } else if ("Fleet Dispatcher".equalsIgnoreCase(role)) {
            role = "Fleet Dispatcher";
        } else {
            System.out.println("\n[ERROR] Invalid role.");
            return;
        }

        boolean success = USER_DAO.registerUser(username, password, role);

        if (success) {
            System.out.println("\n[SUCCESS] Registration completed!");

            if (!USER_DAO.isUserActive(username)) {
                boolean verified = verifyWithCode(username);
                ACTIVITY_LOG_DAO.registerlog(username, role, currentLoggedInAdmin, "Registered");

                if (!verified) {
                    System.out.println("\n[LOCKED/FAILED] Verification failed. Ask the admin for activating the account.");
                }
            } else {
                System.out.println("First account is automatically active. No code required!");
            }
        } else {
            System.out.println("\n[FAILED] Registration rejected.");
        }
    }

    private static void registerDriver() {
        System.out.println("\n================================");
        System.out.println("        REGISTER DRIVER");
        System.out.println("================================");

        System.out.print("Enter Driver Name: ");
        String driverName = sc.nextLine().trim();
        if (driverName.isEmpty()) {
            System.out.println("[ERROR] Driver name cannot be empty.");
            return;
        }

        String licenseNo;
        while (true) {
            System.out.print("Enter License Number (or type 'cancel' to exit): ");
            licenseNo = sc.nextLine().trim().toUpperCase();

            if (licenseNo.equalsIgnoreCase("CANCEL")) {
                System.out.println("Driver registration cancelled.");
                return;
            }
            if (licenseNo.isEmpty()) {
                System.out.println("[ERROR] License number cannot be empty.");
                continue;
            }
            if (initializeDAO.isLicenseExists(licenseNo)) {
                System.out.println("[ERROR] License '" + licenseNo + "' already exists. Try another.");
                continue;
            }
            break;
        }

        System.out.print("Enter Contact Number: ");
        String contactNo = sc.nextLine().trim();
        if (contactNo.isEmpty()) {
            System.out.println("[ERROR] Contact number cannot be empty.");
            return;
        }

        String driverId = "DRV-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String role = "Driver";
        String status = "IDLE";

        boolean saved = initializeDAO.registerDriver(driverId, driverName, licenseNo, contactNo, role, status);

        if (saved) {
            System.out.println("[SUCCESS] Driver '" + driverName + "' (" + licenseNo + ") registered with ID: " + driverId + " [Status: " + status + "]");
        } else {
            System.out.println("[FAILED] Could not register driver.");
        }
    }

    private static void registerVehicle() {
        System.out.println("\n================================");
        System.out.println("       REGISTER VEHICLE");
        System.out.println("================================");

        String plateNo;
        while (true) {
            System.out.print("Enter Plate Number (or type 'cancel' to exit): ");
            plateNo = sc.nextLine().trim().toUpperCase();

            if (plateNo.equalsIgnoreCase("CANCEL")) {
                System.out.println("Vehicle registration cancelled.");
                return;
            }
            if (plateNo.isEmpty()) {
                System.out.println("[ERROR] Plate number cannot be empty.");
                continue;
            }
            if (initializeDAO.isPlateExists(plateNo)) {
                System.out.println("[ERROR] Plate number '" + plateNo + "' already exists. Enter a different one.");
                continue;
            }
            break;
        }

        System.out.print("Enter Vehicle Type/Model: ");
        String vehicleType = sc.nextLine().trim();
        if (vehicleType.isEmpty()) {
            System.out.println("[ERROR] Vehicle type/model cannot be empty.");
            return;
        }

        int capacity;
        while (true) {
            System.out.print("Enter Seating Capacity: ");
            String capacityInput = sc.nextLine().trim();
            try {
                capacity = Integer.parseInt(capacityInput);
                if (capacity <= 0) {
                    System.out.println("[ERROR] Capacity must be greater than 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number for seating capacity.");
            }
        }

        String vehicleId = initializeDAO.generateNextVehicleId();
        String status = "IDLE";

        boolean saved = initializeDAO.registerVehicle(vehicleId, plateNo, capacity, vehicleType, status);

        if (saved) {
            System.out.println("[SUCCESS] Vehicle '" + plateNo + "' (" + vehicleType + ") registered with ID: " + vehicleId + " [Status: " + status + "]");
        } else {
            System.out.println("[FAILED] Could not register vehicle.");
        }
    }

    private static void registerRoute() {
        System.out.println("\n================================");
        System.out.println("        REGISTER ROUTE");
        System.out.println("================================");

        System.out.print("Enter Origin: ");
        String origin = sc.nextLine().trim();

        System.out.print("Enter Destination: ");
        String destination = sc.nextLine().trim();

        System.out.print("Enter Distance (km): ");
        String distance = sc.nextLine().trim();

        if (origin.isEmpty() || destination.isEmpty() || distance.isEmpty()) {
            System.out.println("[ERROR] All fields are required.");
            return;
        }

        String routeId = initializeDAO.generateNextRouteId();
        String status = "IDLE";

        boolean saved = initializeDAO.registerRoute(routeId, origin, destination, status);

        if (saved) {
            System.out.println("[SUCCESS] Route '" + origin + " -> " + destination + "' (" + distance + " km) registered with ID: " + routeId + " [Status: " + status + "]");
        } else {
            System.out.println("[FAILED] Could not register route.");
        }
    }

    private static void assignTrip(String currentLoggedInAdmin) {
        System.out.println("\n================================");
        System.out.println("          ASSIGN TRIP");
        System.out.println("================================");

        boolean hasDrivers = initializeDAO.displayIdleDrivers();
        if (!hasDrivers) {
            System.out.println("\n[INFO] No IDLE drivers available. Cannot assign a trip.");
            return;
        }

        System.out.print("\nEnter Driver ID to assign: ");
        String driverId = sc.nextLine().trim().toUpperCase();
        String driverName = initializeDAO.getIdleDriverName(driverId);
        if (driverName == null) {
            System.out.println("[ERROR] Invalid Driver ID or driver is not IDLE.");
            return;
        }

        boolean hasVehicles = initializeDAO.displayIdleVehicles();
        if (!hasVehicles) {
            System.out.println("\n[INFO] No IDLE vehicles available. Cannot assign a trip.");
            return;
        }

        System.out.print("\nEnter Vehicle ID to assign: ");
        String vehicleId = sc.nextLine().trim().toUpperCase();
        String vehicleType = initializeDAO.getIdleVehicleType(vehicleId);
        if (vehicleType == null) {
            System.out.println("[ERROR] Invalid Vehicle ID or vehicle is not IDLE.");
            return;
        }

        boolean hasRoutes = initializeDAO.displayIdleRoutes();
        if (!hasRoutes) {
            System.out.println("\n[INFO] No IDLE routes available. Cannot assign a trip.");
            return;
        }

        System.out.print("\nEnter Route ID to assign: ");
        String routeId = sc.nextLine().trim().toUpperCase();
        String[] routeDetails = initializeDAO.getIdleRouteDetails(routeId);
        if (routeDetails == null) {
            System.out.println("[ERROR] Invalid Route ID or route is not IDLE.");
            return;
        }

        String origin = routeDetails[0];
        String destination = routeDetails[1];
        String tripId = initializeDAO.generateNextTripId();
        String ongoingStatus = "ONGOING";

        boolean savedOngoing = initializeDAO.saveOngoingRoute(
                tripId,
                routeId,
                origin,
                destination,
                vehicleId,
                vehicleType,
                driverId,
                driverName,
                currentLoggedInAdmin,
                ongoingStatus
        );

        if (!savedOngoing) {
            System.out.println("[FAILED] Could not save ongoing route. Statuses were not changed.");
            return;
        }

        boolean driverUpdated = initializeDAO.updateDriverStatus(driverId, ongoingStatus);
        boolean vehicleUpdated = initializeDAO.updateVehicleStatus(vehicleId, ongoingStatus);
        boolean routeUpdated = initializeDAO.updateRouteStatus(routeId, ongoingStatus);

        if (driverUpdated && vehicleUpdated && routeUpdated) {
            System.out.println("\n[SUCCESS] Trip Assigned!");
            System.out.println("Trip ID     : " + tripId);
            System.out.println("Route       : " + routeId + " (" + origin + " -> " + destination + ")");
            System.out.println("Vehicle     : " + vehicleId + " (" + vehicleType + ")");
            System.out.println("Driver      : " + driverId + " (" + driverName + ")");
            System.out.println("Assigned By : " + currentLoggedInAdmin);
            System.out.println("Status      : " + ongoingStatus);
        } else {
            System.out.println("[WARNING] Ongoing route saved, but one or more statuses failed to update.");
        }
    }

    private static void handleAdminViewMenu() {
        while (true) {
            System.out.println("\n================================");
            System.out.println("          VIEW OPTIONS");
            System.out.println("================================");
            System.out.println("1. Ongoing Route");
            System.out.println("2. Idle Vehicle");
            System.out.println("3. Driver");
            System.out.println("4. Route");
            System.out.println("5. Users");
            System.out.println("6. Register Logs");
            System.out.println("7. Login Logs");
            System.out.println("8. Completed Trips");
            System.out.println("0. Back");
            System.out.println("================================");
            System.out.print("Select option: ");

            String viewChoice = sc.nextLine().trim();

            switch (viewChoice) {
                case "1":
                    initializeDAO.displayOngoingRoutes();
                    break;
                case "2":
                    initializeDAO.displayIdleVehicles();
                    break;
                case "3":
                    initializeDAO.displayAllDrivers();
                    break;
                case "4":
                    initializeDAO.displayAllRoutes();
                    break;
                case "5":
                    USER_DAO.displayAllUsers();
                    break;
                case "6":
                    System.out.println("\n[INFO] Hook up Register Logs view to your ActivityLogDAO.");
                    break;
                case "7":
                    System.out.println("\n[INFO] Hook up Login Logs view to your ActivityLogDAO.");
                    break;
                case "8":
                    initializeDAO.displayCompletedTrips();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Error: Invalid view option.");
                    break;
            }
        }
    }

    private static void handleAdminUpdateMenu() {
        while (true) {
            System.out.println("\n================================");
            System.out.println("         UPDATE STATUS");
            System.out.println("================================");
            System.out.println("1. Update User Status");
            System.out.println("2. Update Driver Status");
            System.out.println("3. Update Trip Status (Resets Driver, Vehicle & Route to IDLE)");
            System.out.println("4. Update Vehicle Status");
            System.out.println("0. Back");
            System.out.println("================================");
            System.out.print("Select option: ");

            String updateChoice = sc.nextLine().trim();

            switch (updateChoice) {
                case "1":
                    updateUserStatusMenu();
                    break;
                case "2":
                    updateDriverStatusMenu();
                    break;
                case "3":
                    updateTripStatusMenu();
                    break;
                case "4":
                    updateVehicleStatusMenu();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Error: Invalid update option.");
                    break;
            }
        }
    }

    private static void updateUserStatusMenu() {
        System.out.println("\n--- UPDATE USER STATUS ---");
        USER_DAO.displayAllUsers();

        System.out.print("\nEnter Username: ");
        String targetUsername = sc.nextLine().trim();
        if (targetUsername.isEmpty()) {
            System.out.println("[ERROR] Username cannot be empty.");
            return;
        }

        if (!USER_DAO.isUserExists(targetUsername)) {
            System.out.println("[ERROR] User '" + targetUsername + "' does not exist.");
            return;
        }

        System.out.print("Enter New Status (ACTIVE / INACTIVE): ");
        String newStatus = sc.nextLine().trim().toUpperCase();
        if (!"ACTIVE".equals(newStatus) && !"INACTIVE".equals(newStatus)) {
            System.out.println("[ERROR] Invalid status. Use ACTIVE or INACTIVE.");
            return;
        }

        if ("ACTIVE".equals(newStatus)) {
            if (USER_DAO.isUserActive(targetUsername)) {
                System.out.println("[INFO] User '" + targetUsername + "' is already ACTIVE.");
                return;
            }

            boolean codeSent = USER_DAO.sendActivationCode(targetUsername);
            if (!codeSent) {
                System.out.println("[FAILED] Could not send verification code.");
                return;
            }

            boolean verified = verifyWithCode(targetUsername);
            if (!verified) {
                System.out.println("[FAILED] Verification failed. User '" + targetUsername + "' remains INACTIVE.");
            }
        } else {
            boolean updated = USER_DAO.updateUserStatus(targetUsername, "INACTIVE");
            if (updated) {
                System.out.println("[SUCCESS] User '" + targetUsername + "' status updated to INACTIVE.");
            } else {
                System.out.println("[FAILED] Could not update user status.");
            }
        }
    }

    private static void updateDriverStatusMenu() {
        System.out.println("\n--- UPDATE DRIVER STATUS ---");
        initializeDAO.displayAllDrivers();

        System.out.print("\nEnter Driver ID: ");
        String driverId = sc.nextLine().trim().toUpperCase();
        if (driverId.isEmpty()) {
            System.out.println("[ERROR] Driver ID cannot be empty.");
            return;
        }

        System.out.print("Enter New Status (IDLE / ONGOING / INACTIVE): ");
        String newStatus = sc.nextLine().trim().toUpperCase();
        if (!"IDLE".equals(newStatus) && !"ONGOING".equals(newStatus) && !"INACTIVE".equals(newStatus)) {
            System.out.println("[ERROR] Invalid status. Use IDLE, ONGOING, or INACTIVE.");
            return;
        }

        boolean updated = initializeDAO.updateDriverStatus(driverId, newStatus);
        if (updated) {
            System.out.println("[SUCCESS] Driver '" + driverId + "' status updated to " + newStatus + ".");
        } else {
            System.out.println("[FAILED] Could not update driver status. Check if Driver ID exists.");
        }
    }

    private static void updateTripStatusMenu() {
        System.out.println("\n--- UPDATE TRIP STATUS ---");
        boolean hasTrips = initializeDAO.displayActiveTrips();
        if (!hasTrips) {
            return;
        }

        System.out.print("\nEnter Trip ID (e.g., TRP-001): ");
        String tripId = sc.nextLine().trim().toUpperCase();
        if (tripId.isEmpty()) {
            System.out.println("[ERROR] Trip ID cannot be empty.");
            return;
        }

        System.out.print("Enter New Status (COMPLETED / CANCELLED / ONGOING / IDLE): ");
        String newStatus = sc.nextLine().trim().toUpperCase();
        if (!"COMPLETED".equals(newStatus)
                && !"CANCELLED".equals(newStatus)
                && !"ONGOING".equals(newStatus)
                && !"IDLE".equals(newStatus)) {
            System.out.println("[ERROR] Invalid status. Use COMPLETED, CANCELLED, ONGOING, or IDLE.");
            return;
        }

        boolean updated = initializeDAO.updateTripStatus(tripId, newStatus);
        if (updated) {
            System.out.println("[SUCCESS] Trip '" + tripId + "' status updated to " + newStatus + ".");
        } else {
            System.out.println("[FAILED] Could not update trip status.");
        }
    }

    private static void updateVehicleStatusMenu() {
        System.out.println("\n--- UPDATE VEHICLE STATUS ---");
        initializeDAO.displayvehicle();

        System.out.print("\nEnter Vehicle ID: ");
        String vehicleId = sc.nextLine().trim().toUpperCase();
        if (vehicleId.isEmpty()) {
            System.out.println("[ERROR] Vehicle ID cannot be empty.");
            return;
        }

        System.out.print("Enter New Status (IDLE / ONGOING / MAINTENANCE / INACTIVE): ");
        String newStatus = sc.nextLine().trim().toUpperCase();
        if (!"IDLE".equals(newStatus)
                && !"ONGOING".equals(newStatus)
                && !"MAINTENANCE".equals(newStatus)
                && !"INACTIVE".equals(newStatus)) {
            System.out.println("[ERROR] Invalid status. Use IDLE, ONGOING, MAINTENANCE, or INACTIVE.");
            return;
        }

        boolean updated = initializeDAO.updateVehicleStatus(vehicleId, newStatus);
        if (updated) {
            System.out.println("[SUCCESS] Vehicle '" + vehicleId + "' status updated to " + newStatus + ".");
        } else {
            System.out.println("[FAILED] Could not update vehicle status. Check if Vehicle ID exists.");
        }
    }

    private static boolean verifyWithCode(String username) {
        int maxAttempts = 5;
        boolean verified = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.print("Enter the 6-digit security code sent to the admin email (Attempt " + attempt + "/" + maxAttempts + "): ");
            String enteredCode = sc.nextLine().trim();

            verified = USER_DAO.verifyAccount(username, enteredCode);
            if (verified) {
                System.out.println("Account is now ACTIVE. You can log in.");
                break;
            } else {
                int attemptsLeft = maxAttempts - attempt;
                if (attemptsLeft > 0) {
                    System.out.println("Incorrect code. You have " + attemptsLeft + " attempt(s) remaining.\n");
                }
            }
        }
        return verified;
    }
}