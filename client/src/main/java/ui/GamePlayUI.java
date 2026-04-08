package ui;

import chess.*;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;
import chessboardprinter.ChessBoardPrinter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class GamePlayUI {

    private final WebSocketClient wsClient;
    private ChessGame game;
    private final String perspective;
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;
    private boolean lastMoveSucceeded = false;
    private Collection<ChessPosition> highlightedSquares = new ArrayList<>();
    private ChessPosition selectedSquare = null;



    public GamePlayUI(WebSocketClient wsClient, ChessGame game, String perspective) {
        this.wsClient = wsClient;
        this.game = game;
        this.perspective = perspective;

        wsClient.setBoardUpdateListener(new WebSocketClient.BoardUpdateListener() {
            @Override
            public void onBoardUpdate(ChessGame updatedGame) {
                lastMoveSucceeded = true;

                GamePlayUI.this.game = updatedGame;
                drawBoard();

                var turn = updatedGame.getTeamTurn() == ChessGame.TeamColor.WHITE
                        ? "White"
                        : "Black";

                System.out.println("\n" + turn + " to move");
                System.out.print("gameplay> ");
            }


            @Override
            public void onNotification(String message) {
                System.out.println("\n[WS] " + message);
            }


            @Override
            public void onError(String errorMessage) {
                lastMoveSucceeded = false;
                System.out.println("\n" + errorMessage);
            }


        });
    }

    public void start() {
        System.out.println("Gameplay started! Type 'help' for commands.");

        if (this.game == null && wsClient.getGame() != null) {
            this.game = wsClient.getGame();
        }

        promptLoop();
    }

    private void drawBoard() {
        if (game == null) {
            return;
        }

        System.out.println();
        ChessBoardPrinter.drawBoard(game, perspective);
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
            case "redraw" -> {
                drawBoard();
                System.out.print("gameplay> ");
            }
            case "leave" -> leaveGame();
            case "resign" -> resignGame();
            case "move" -> makeMove();
            case "highlight" -> highlightMoves();
            default -> {
                System.out.println("Unknown command. Type 'help'.");
                System.out.print("gameplay> ");
            }
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
        wsClient.sendCommand(new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                wsClient.getConnectToken(),
                wsClient.getGameID()
        ));
    }

    private void resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("You resigned.");
            wsClient.sendCommand(new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN,
                    wsClient.getConnectToken(),
                    wsClient.getGameID()
            ));
        } else {
            System.out.println("Resign cancelled.");
            System.out.print("gameplay> ");
        }
    }

    private void makeMove() {
        lastMoveSucceeded = false;

        while (true) {
            System.out.print("Enter move (e.g., e2e4): ");
            String moveStr = scanner.nextLine().trim().toLowerCase();

            try {
                if (moveStr.length() < 4) {
                    throw new Exception("Invalid format. Use e2e4.");
                }

                ChessPosition from = ChessPosition.fromString(moveStr.substring(0, 2));
                ChessPosition to = ChessPosition.fromString(moveStr.substring(2, 4));

                wsClient.sendMove(new ChessMove(from, to, null));

                Thread.sleep(150);

                if (lastMoveSucceeded) {
                    break;
                } else {
                    break;
                }

            } catch (Exception e) {
                String msg = e.getMessage().toLowerCase();

                if (msg.contains("not your turn")) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }

                System.out.println("Error: " + e.getMessage());
            }
        }
//        System.out.print("gameplay> ");
    }



    private void highlightMoves() {
        System.out.print("Enter piece position (e.g., e2): ");
        String posStr = scanner.nextLine().trim().toLowerCase();

        try {
            ChessPosition pos = ChessPosition.fromString(posStr);
            ChessPiece piece = game.getBoard().getPiece(pos);

            if (piece == null) {
                System.out.println("No piece at that position.");
                highlightedSquares.clear();
                selectedSquare = null;
            } else {
                selectedSquare = pos;

                highlightedSquares = new ArrayList<>();
                for (ChessMove move : game.validMoves(pos)) {
                    highlightedSquares.add(move.getEndPosition());
                }

                ChessBoardPrinter.drawBoardWithHighlights(
                        game, perspective, selectedSquare, highlightedSquares);
            }

        } catch (Exception e) {
            System.out.println("Invalid position.");
            highlightedSquares.clear();
            selectedSquare = null;
        }

        System.out.print("gameplay> ");
    }

}

