package ui;

import client.ServerFacade;
import client.dtos.GameData;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;
import chess.ChessGame;

import java.util.List;
import java.util.Scanner;

public class PostloginUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    private List<GameData> lastGames;
    private final int serverPort;

    private WebSocketClient wsClient;
    private Integer currentGameID;
    private String currentPerspective; // "white", "black", "observe"

    public PostloginUI(ServerFacade facade, Scanner scanner, int serverPort) {
        this.facade = facade;
        this.scanner = scanner;
        this.serverPort = serverPort;
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
                    return;
                }
                case "create game" -> createGame();
                case "list games" -> listGames();
                case "play game" -> playGame();
                case "observe game" -> observeGame();
                case "reset" -> {
                    resetDatabase();
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                  help         - show this help message
                  logout       - log out
                  create game  - create a new game
                  list games   - list existing games
                  play game    - join a game
                  observe game - observe a game
                  reset        - reset all games
                """);
    }

    private void logout() {
        try {
            facade.logout();
            System.out.println("Logged out successfully!");
        } catch (Exception e) {
            printServerError(e);
        }
    }

    private void resetDatabase() {
        try {
            facade.clearDatabase();
            System.out.println("Database cleared!");
        } catch (Exception e) {
            printServerError(e);
        }
    }

    private void createGame() {
        System.out.print("Enter new game name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: game name cannot be empty");
            return;
        }
        try {
            facade.createGame(name);
            System.out.println("Game '" + name + "' created successfully!");
        } catch (Exception e) {
            printServerError(e);
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
            printServerError(e);
        }
    }

    private String getWsUrl() {
        return "ws://localhost:" + serverPort + "/ws";
    }

    private void playGame() {
        if (lastGames == null || lastGames.isEmpty()) {
            System.out.println("No games to play. Use 'list games' first.");
            return;
        }

        try {
            System.out.print("Enter game number to join: ");
            int number = Integer.parseInt(scanner.nextLine().trim());
            if (number < 1 || number > lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData gameData = lastGames.get(number - 1);
            currentGameID = gameData.id();

            System.out.print("Enter color (white/black): ");
            String color = scanner.nextLine().trim().toLowerCase();
            if (!color.equals("white") && !color.equals("black")) {
                System.out.println("Invalid color. Choose 'white' or 'black'.");
                return;
            }

            facade.joinGame(gameData.id(), color);
            System.out.println("Joined game '" + gameData.name() + "' as " + color + "!");
            currentPerspective = color;

            wsClient = new WebSocketClient(
                    getWsUrl(),
                    new UserGameCommand(UserGameCommand.CommandType.CONNECT, facade.getAuthToken(), gameData.id()),
                    gameData.game(),
                    color
            );

            wsClient.getConnectedFuture().join();

            GamePlayUI gameplay = new GamePlayUI(wsClient, gameData.game(), color);
            gameplay.start();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void observeGame() {
        if (lastGames == null || lastGames.isEmpty()) {
            System.out.println("No games to observe. Use 'list games' first.");
            return;
        }

        try {
            System.out.print("Enter game number to observe: ");
            int number = Integer.parseInt(scanner.nextLine().trim());
            if (number < 1 || number > lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData gameData = lastGames.get(number - 1);
            currentGameID = gameData.id();

            // Create WebSocket client
            wsClient = new WebSocketClient(
                    getWsUrl(),
                    new UserGameCommand(UserGameCommand.CommandType.CONNECT, facade.getAuthToken(), gameData.id()),
                    gameData.game(),
                    "observe"
            );

            // Launch GamePlayUI in observer mode
            GamePlayUI ui = new GamePlayUI(wsClient, gameData.game(), "white");
            ui.start();

        } catch (Exception e) {
            System.out.println("[WS ERROR] " + e.getMessage());
        }
    }


    private void printServerError(Exception e) {
        System.out.println("Server error: " + e.getMessage());
    }
}