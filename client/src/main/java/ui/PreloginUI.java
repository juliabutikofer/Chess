package ui;

import client.ServerFacade;
import client.dtos.*;
import java.util.Scanner;

public class PreloginUI {

    private final ServerFacade facade;
    private final Scanner scanner;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to Chess Client!");
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

            facade.login(username, password);
            System.out.println("Login successful!");

            PostloginUI postlogin = new PostloginUI(facade, scanner);
            postlogin.start();

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
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

            facade.register(username, password, email);
            System.out.println("Registration successful! You are now logged in.");

            PostloginUI postlogin = new PostloginUI(facade, scanner);
            postlogin.start();

        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }
}