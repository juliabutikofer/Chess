package ui;

import client.ServerFacade;
import client.dtos.*;
import java.util.Scanner;

public class PreloginUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    private final int serverPort = 8080; // Set this to the port your server runs on

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to Chess Client!");
        System.out.println("Type 'help' to see commands");

        while (true) {
            System.out.print("prelogin> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "login" -> login();
                case "register" -> register();
                case "quit" -> {
                    System.out.println("Exiting program. Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help     - show this help message");
        System.out.println("  login    - log in to your account");
        System.out.println("  register - create a new account");
        System.out.println("  quit     - exit the program");
    }

    private void login() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Error: username and password cannot be empty");
                return;
            }

            facade.login(username, password);
            System.out.println("Login successful!");

            PostloginUI postlogin = new PostloginUI(facade, scanner, serverPort);
            postlogin.start();

        } catch (Exception e) {
            printServerMessage(e);
        }
    }

    private void register() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                System.out.println("Error: username, password, and email cannot be empty");
                return;
            }

            facade.register(username, password, email);
            System.out.println("Registration successful! You are now logged in.");

            PostloginUI postlogin = new PostloginUI(facade, scanner, serverPort);
            postlogin.start();

        } catch (Exception e) {
            printServerMessage(e);
        }
    }

    private void printServerMessage(Exception e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("\"message\"")) {
            int start = msg.indexOf(":\"") + 2;
            int end = msg.lastIndexOf("\"");
            if (start >= 0 && end > start) {
                System.out.println(msg.substring(start, end));
            } else {
                System.out.println("Unknown error");
            }
        } else {
            System.out.println("Unknown error");
        }
    }
}