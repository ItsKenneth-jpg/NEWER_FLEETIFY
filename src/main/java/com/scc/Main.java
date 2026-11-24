package com.scc;

import com.scc.dao.ActivityLogDAO;
import com.scc.dao.UserDAO;
import com.scc.dao.adminDAO;
import com.scc.dao.initializeDAO;
import com.scc.model.ActivityLog;

import java.util.Scanner;

public class Main {
    //  single shared Scanner for everything
    private static final Scanner sc = new Scanner(System.in);
    private static final adminDAO ADMIN_DAO = new adminDAO();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final initializeDAO ROUTE_DAO = new initializeDAO();
    private static Object system;

    static void main(String[] args) {

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

                    ActivityLogDAO activityLogDAO = new ActivityLogDAO();
                    UserDAO userDAOO = new UserDAO();

                    boolean isloggedIn = UserDAO.login(loginuser, loginPass);

                    if (isloggedIn) {
                        String userRole = userDAOO.role(loginuser, loginPass);
                        activityLogDAO.LogLogin(loginuser, "LOGIN");

                        if (userRole != null) {
                            if (userRole.equalsIgnoreCase("ADMIN")) {
                                dashboardloop:
                                while (true) {
                                    String currentLoggedInAdmin = loginuser;
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
                                            UserDAO userDAO = new UserDAO();
                                            ActivityLogDAO activityLogDAO11 = new ActivityLogDAO();
                                            Scanner sc = new Scanner(System.in);


                                            System.out.println("\n================================");
                                            System.out.println("            REGISTER");
                                            System.out.println("================================");

                                            System.out.print("Enter Username: ");
                                            String username = sc.nextLine().trim();

                                            System.out.print("Enter Password: ");
                                            String password = sc.nextLine().trim();


                                            System.out.print("Enter role (e.g., ADMIN, USER): ");
                                            String role = sc.nextLine().trim();

                                            if ("ADMIN".equalsIgnoreCase(role)) {
                                                if (userDAO.adminExists()) {
                                                    System.out.println("Registration failed: An admin account already exists.");
                                                    break;
                                                }
                                            }

// Call registerUser with 4 arguments including email
                                            boolean success = userDAO.registerUser(username, password, role);

                                            if (success) {
                                                System.out.println("\n[SUCCESS] Registration completed!");

                                                // OPTIONAL HELPER: Check if this specific user needs activation (i.e., they are INACTIVE)
                                                // If they are the first user, their status is ACTIVE, so we skip the prompt entirely!
                                                if (!userDAO.isUserActive(username)) {
                                                    int maxAttempts = 5;
                                                    boolean verified = false;

                                                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                                                        System.out.print("Enter the 6-digit security code sent to the admin email (Attempt " + attempt + "/" + maxAttempts + "): ");
                                                        String enteredCode = sc.nextLine().trim();

                                                        verified = userDAO.verifyAccount(username, enteredCode);
                                                        if (verified) {
                                                            System.out.println("Account is now ACTIVE. You can log in.");
                                                            break; // Exit loop immediately on success
                                                        } else {
                                                            int attemptsLeft = maxAttempts - attempt;
                                                            if (attemptsLeft > 0) {
                                                                System.out.println("Incorrect code. You have " + attemptsLeft + " attempt(s) remaining.\n");
                                                            }
                                                        }
                                                    }
                                                    String loggedAdmin = "ADMIN";
                                                    activityLogDAO11.registerlog(username, role , loggedAdmin, "Registered");

                                                    // Triggers if they fail all 5 attempts
                                                    if (!verified) {
                                                        System.out.println("\n[LOCKED/FAILED] Verification failed. Ask the admin for activating the account.");
                                                    }

                                                } else {
                                                    System.out.println("First account is automatically active. No code required!");
                                                }

                                            } else {
                                                System.out.println("\n[FAILED] Registration rejected.");
                                            }
                                            break;
                                        case "2":
                                            break;
                                        case "3":
                                            break;
                                        case "4":
                                            break;
                                        case "0":
                                            System.out.println("returning...");
                                            activityLogDAO.loglogout(loginuser, "LOGOUT");
                                            break dashboardloop;
                                        default:
                                            System.out.println("Error: Invalid Option.");
                                            break dashboardloop;
                                    }
                                }

                            }
                        }
                    }
                    else {
                        System.out.println("Login failed. Please try again.");
                        break;
                    }
                    break;

                case "2":
                    UserDAO userDAO = new UserDAO();
                    ActivityLogDAO activityLogDAO1 = new ActivityLogDAO();
                    Scanner sc = new Scanner(System.in);

                    System.out.println("\n================================");
                    System.out.println("            REGISTER");
                    System.out.println("================================");

                    System.out.print("Enter Username: ");
                    String username = sc.nextLine().trim();

                    System.out.print("Enter Password: ");
                    String password = sc.nextLine().trim();


                    String Role;

                    if(!userDAO.adminExists()){
                        Role = "ADMIN";
                        System.out.println("First Account detected!");



                        boolean success = userDAO.registerUser(username, password, Role);



                        if(!success){



                            System.out.println("\n[FAILED] Registration rejected.");

                        }
                        break;

                    }
                    else {
                        System.out.print("Enter role (e.g., ADMIN, USER): ");
                        String role = sc.nextLine().trim();

                        if(role.equals("Fleet Dispatcher") || role.equals("Transit Scheduler")){


// Call registerUser with 4 arguments including email
                            boolean success = userDAO.registerUser(username, password, role);

                            if (success) {
                                System.out.println("\n[SUCCESS] Registration completed!");

                                // OPTIONAL HELPER: Check if this specific user needs activation (i.e., they are INACTIVE)
                                // If they are the first user, their status is ACTIVE, so we skip the prompt entirely!
                                if (!userDAO.isUserActive(username)) {
                                    int maxAttempts = 5;
                                    boolean verified = false;

                                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                                        System.out.print("Enter the 6-digit security code sent to the admin email (Attempt " + attempt + "/" + maxAttempts + "): ");
                                        String enteredCode = sc.nextLine().trim();

                                        verified = userDAO.verifyAccount(username, enteredCode);
                                        if (verified) {
                                            System.out.println("Account is now ACTIVE. You can log in.");

                                            break; // Exit loop immediately on success
                                        } else {
                                            int attemptsLeft = maxAttempts - attempt;
                                            if (attemptsLeft > 0) {
                                                System.out.println("Incorrect code. You have " + attemptsLeft + " attempt(s) remaining.\n");
                                            }
                                        }
                                    }
                                    String loggedAdmin = null;
                                    activityLogDAO1.registerlog(username, role , loggedAdmin, "Registered");


                                    // Triggers if they fail all 5 attempts
                                    if (!verified) {
                                        System.out.println("\n[LOCKED/FAILED] Verification failed. Ask the admin for activating the account.");
                                        String loggedAdminn = null;
                                        activityLogDAO1.registerlog(username, role , loggedAdminn, "Not Active");
                                    }

                                }

                            } else {
                                System.out.println("\n[FAILED] Registration rejected.");
                            }
                        }
                        else if ("ADMIN".equalsIgnoreCase(role)) {
                            if (userDAO.adminExists()) {
                                System.out.println("Registration failed: An admin account already exists.");
                                break;
                            }
                        }
                        else {
                            System.out.println("\n[ERROR] Invalid role. Only 'FLEET DISPATCHER' or 'TRANSIT SCHEDULER' are allowed.");

                            break;
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
}