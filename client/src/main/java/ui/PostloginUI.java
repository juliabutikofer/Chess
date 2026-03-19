package ui;

import client.ServerFacade;
import client.dtos.GameData;
import java.util.List;
import java.util.Scanner;
import client.ChessBoardPrinter;

public class PostloginUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    private List<GameData> lastGames; // stores last game list for selection

    public PostloginUI(ServerFacade facade, Scanner scanner) {
        this.facade = facade;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("Welcome! You are now logged in.");
        while (true) {
            System.out.print("postlogin> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "logout" -> {
                    logout();
                    return; // return to prelogin menu
                }
                case "create game" -> createGame();
                case "list games" -> listGames();
                case "play game" -> playGame();
                case "observe game" -> observeGame();
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help         - show this help message");
        System.out.println("  logout       - log out and return to prelogin menu");
        System.out.println("  create game  - create a new game");
        System.out.println("  list games   - list all existing games");
        System.out.println("  play game    - join a game to play");
        System.out.println("  observe game - join a game to observe");
    }

}