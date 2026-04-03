package ui;

import client.ServerFacade;
import client.dtos.GameData;
import ChessBoardPrinter.ChessBoardPrinter;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;

import java.util.List;
import java.util.Scanner;

public class PostloginUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    private List<GameData> lastGames;

    private WebSocketClient wsClient;
    private Integer currentGameID;

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
                    return;
                }
                case "create game" -> createGame();
                case "list games" -> listGames();
                case "play game" -> playGame();
                case "make move" -> makeMove();
                case "observe game" -> observeGame();
                case "reset" -> {
                    resetDatabase();
                    return;
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
            printServerError(e);
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help         - show this help message");
        System.out.println("  logout       - log out and return to prelogin menu");
        System.out.println("  create game  - create a new game");
        System.out.println("  list games   - list all existing games");
        System.out.println("  play game    - join a game to play");
        System.out.println("  make move    - make a move (e.g., e2e4)");
        System.out.println("  observe game - observe a game (white perspective)");
        System.out.println("  reset        - reset all games");
    }

    private void logout() {
        try {
            facade.logout();
            System.out.println("Logged out successfully!");
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

            wsClient = new WebSocketClient(
                    "ws://localhost:8080/game",
                    new UserGameCommand(UserGameCommand.CommandType.CONNECT, facade.getAuthToken(), gameData.id()),
                    gameData.game(),
                    color
            );

            wsClient.setGame(gameData.game(), color);

            System.out.println("Connected to game via WebSocket. Waiting for updates...");
            ChessBoardPrinter.drawBoard(gameData.game(), color);

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input: please enter an integer");
        } catch (Exception e) {
            printServerError(e);
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

            System.out.println("Observing game '" + gameData.name() + "' (white perspective):");

            wsClient = new WebSocketClient(
                    "ws://localhost:8080/game",
                    new UserGameCommand(UserGameCommand.CommandType.CONNECT, facade.getAuthToken(), gameData.id()),
                    gameData.game(),
                    "white"
            );

            wsClient.setGame(gameData.game(), "observe");

            ChessBoardPrinter.drawBoard(gameData.game(), "white");

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input: please enter an integer");
        } catch (Exception e) {
            printServerError(e);
        }
    }

    private void printServerError(Exception e) {
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

    private void makeMove() {
        if (wsClient == null || currentGameID == null) {
            System.out.println("You are not in a game.");
            return;
        }

        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine().trim();

        // Basic validation
        if (!move.matches("^[a-h][1-8][a-h][1-8]$")) {
            System.out.println("Invalid move format. Example: e2e4");
            return;
        }

        wsClient.sendCommand(new UserGameCommand(
                UserGameCommand.CommandType.MAKE_MOVE,
                facade.getAuthToken(),
                currentGameID,
                move
        ));
    }
}