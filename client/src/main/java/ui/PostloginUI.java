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
                case "reset" -> {
                    resetDatabase();
                    return; // ⭐ return to prelogin menu after reset
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void resetDatabase() {
        try {
            facade.clearDatabase();   // DELETE /db
            System.out.println("Database cleared!");
            // ⭐ DO NOT try to modify authToken or state here.
            // ⭐ Just return to main UI (handled by the return in start()).
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
            System.out.println("Game created successfully!");
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
                System.out.printf("%d. %s (Players: %s)%n",
                        i + 1, g.name(), String.join(", ", g.players()));
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
            GameData game = lastGames.get(number - 1);

            System.out.print("Enter color (white/black): ");
            String color = scanner.nextLine().trim().toLowerCase();

            facade.joinGame(game.id(), color);
            System.out.println("Joined game as " + color + "!");

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
            GameData game = lastGames.get(number - 1);

            facade.observeGame(game.id());
            System.out.println("Observing game!");

            ChessBoardPrinter.drawInitialBoard("white");

        } catch (Exception e) {
            System.out.println("Observe game failed: " + e.getMessage());
        }
    }
}
