package ui;

import chess.*;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;
import ChessBoardPrinter.ChessBoardPrinter;
import java.util.Scanner;

public class GamePlayUI {

    private final WebSocketClient wsClient;
    private ChessGame game;
    private String perspective; // "white", "black", "observe"
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;

    public GamePlayUI(WebSocketClient wsClient, ChessGame game, String perspective) {
        this.wsClient = wsClient;
        this.game = game;
        this.perspective = perspective;

        //register listener for updates
        wsClient.setBoardUpdateListener(new WebSocketClient.BoardUpdateListener() {
            @Override
            public void onBoardUpdate(ChessGame updatedGame) {
                updateBoard(updatedGame);
            }

            @Override
            public void onNotification(String message) {
                System.out.println("[WS] " + message);
                System.out.print("gameplay> ");
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("[WS ERROR] " + errorMessage);
                System.out.print("gameplay> ");
            }
        });
    }

    public void start() {
        System.out.println("Gameplay started! Type 'help' for commands.");
        drawBoard();
        promptLoop();
    }

    private void drawBoard() {
        System.out.println();
        ChessBoardPrinter.drawBoard(game, perspective);
        System.out.print("gameplay> ");
    }

    private void promptLoop() {
        while (running) {
            String input = scanner.nextLine().trim().toLowerCase();
            handleCommand(input);
        }
    }

    private void handleCommand(String cmd) {
        switch (cmd) {
            case "help" -> printHelp();
            case "redraw" -> drawBoard();
            case "leave" -> leaveGame();
            case "resign" -> resignGame();
            case "move" -> makeMove();
            case "highlight" -> highlightMoves();
            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                help       - Show this message
                redraw     - Redraw the chess board
                leave      - Leave the game
                move       - Make a move
                resign     - Resign the game
                highlight  - Highlight legal moves
                """);
        System.out.print("gameplay> ");
    }

    private void leaveGame() {
        System.out.println("Leaving game...");
        running = false;
        if (wsClient != null) {
            wsClient.sendCommand(new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE,
                    wsClient.getConnectToken(),
                    wsClient.getGameID()
            ));
        }
    }

    private void resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("You resigned.");
            running = false;
            if (wsClient != null) {
                wsClient.sendCommand(new UserGameCommand(
                        UserGameCommand.CommandType.RESIGN,
                        wsClient.getConnectToken(),
                        wsClient.getGameID()
                ));
            }
        } else {
            System.out.println("Resign cancelled.");
        }
        System.out.print("gameplay> ");
    }

    private void makeMove() {
        System.out.print("Enter move (e.g., e2e4): ");
        String moveStr = scanner.nextLine().trim().toLowerCase();

        try {
            ChessPosition from = ChessPosition.fromString(moveStr.substring(0, 2));
            ChessPosition to = ChessPosition.fromString(moveStr.substring(2, 4));
            ChessPiece piece = game.getBoard().getPiece(from);

            String promotion = null;
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    ((to.getRow() == 8 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ||
                            (to.getRow() == 1 && piece.getTeamColor() == ChessGame.TeamColor.BLACK))) {
                System.out.print("Promote pawn to (Q/R/B/N): ");
                promotion = scanner.nextLine().trim().toUpperCase();
            }

            wsClient.sendCommand(new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    wsClient.getConnectToken(),
                    wsClient.getGameID(),
                    moveStr
            ));

            System.out.println("Move sent: " + moveStr + " (waiting for server update...)");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void highlightMoves() {
        System.out.print("Enter piece position (e.g., e2): ");
        String posStr = scanner.nextLine().trim().toLowerCase();
        try {
            ChessPosition pos = ChessPosition.fromString(posStr);
            ChessPiece piece = game.getBoard().getPiece(pos);
            if (piece == null) {
                System.out.println("No piece at that position.");
            } else {
                System.out.println(piece.getPieceType() + " at " + pos + " legal moves:");
                for (ChessMove move : game.validMoves(pos)) System.out.println(move);
            }
        } catch (Exception e) {
            System.out.println("Invalid position.");
        }
        System.out.print("gameplay> ");
    }

    public void updateBoard(ChessGame updatedGame) {
        this.game = updatedGame;
        drawBoard();
    }

}