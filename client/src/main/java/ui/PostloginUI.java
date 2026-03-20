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
        System.out.println("Welcome!");
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
                case "reset" -> {
                    resetDatabase();
                    return; // return to prelogin menu after reset
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void resetDatabase() {
        try {
            facade.clearDatabase();
            System.out.println("Database cleared! Returning to prelogin menu.");
        } catch (Exception e) {
            System.out.println("Reset failed: " + e.getMessage());
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
        System.out.println("  reset        - reset all games");
    }

    private void logout() {
        try {
            facade.logout();
            System.out.println("Logged out successfully!");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void createGame() {
        System.out.print("Enter new game name: ");
        String name = scanner.nextLine().trim();
        try {
            facade.createGame(name);
            System.out.println("Game '" + name + "' created successfully!");
        } catch (Exception e) {
            System.out.println("Create game failed: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            lastGames = facade.listGames();
            if (lastGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }

            for (int i = 0; i < lastGames.size(); i++) {
                GameData g = lastGames.get(i);
                String players = g.players().isEmpty() ? "No players yet" : String.join(", ", g.players());
                System.out.printf("%d. %s (Players: %s)%n", i + 1, g.name(), players);
            }
        } catch (Exception e) {
            System.out.println("List games failed: " + e.getMessage());
        }
    }

    private void playGame() {
        if (lastGames == null || lastGames.isEmpty()) {
            System.out.println("No games to play. Use 'list games' first.");
            return;
        }

        try {
            System.out.print("Enter game number to join: ");
            int number = Integer.parseInt(scanner.nextLine());
            if (number < 1 || number > lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData game = lastGames.get(number - 1);

            System.out.print("Enter color (white/black): ");
            String color = scanner.nextLine().trim().toLowerCase();
            if (!color.equals("white") && !color.equals("black")) {
                System.out.println("Invalid color. Choose 'white' or 'black'.");
                return;
            }

            facade.joinGame(game.id(), color);
            System.out.println("Joined game '" + game.name() + "' as " + color + "!");

            ChessBoardPrinter.drawInitialBoard(color);

        } catch (Exception e) {
            System.out.println("Play game failed: " + e.getMessage());
        }
    }

    private void observeGame() {
        if (lastGames == null || lastGames.isEmpty()) {
            System.out.println("No games to observe. Use 'list games' first.");
            return;
        }

        try {
            System.out.print("Enter game number to observe: ");
            int number = Integer.parseInt(scanner.nextLine());
            if (number < 1 || number > lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData game = lastGames.get(number - 1);

            // Prevent observing a game you are already playing
            String username = facade.getUsername(); // must return logged-in username
            if (game.players().contains(username)) {
                System.out.println("Cannot observe this game: you are already a player.");
                return;
            }

            // Prevent observing a game with no players
            if (game.players().isEmpty()) {
                System.out.println("Cannot observe a game with no players yet.");
                return;
            }

            facade.observeGame(game.id());
            System.out.println("Observing game '" + game.name() + "'!");

            ChessBoardPrinter.drawInitialBoard("white");

        } catch (Exception e) {
            System.out.println("Observe game failed: " + e.getMessage());
        }
    }
}