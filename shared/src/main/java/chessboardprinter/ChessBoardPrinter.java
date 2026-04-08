package chessboardprinter;

import chess.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ChessBoardPrinter {

    private static final String RESET_COLOR = "\u001b[0m";
    private static final String WHITE_BG = "\u001b[47m"; // white squares
    private static final String BLACK_BG = "\u001b[40m"; // black squares
    private static final String RED_TEXT = "\u001b[31m";  // white pieces
    private static final String BLUE_TEXT = "\u001b[34m"; // black pieces
    private static final String SELECT_BG = "\u001b[43m"; // yellow
    private static final String MOVE_BG   = "\u001b[42m"; // green
    private static final String BLACK_TEXT = "\u001b[30m";


    public static void drawBoard(ChessGame game, String perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean whitePerspective = perspective.equalsIgnoreCase("white");
        ChessBoard board = game.getBoard();

        if (board == null) {
            out.println("Error: Board data is missing.");
            return;
        }

        printFiles(out, whitePerspective);

        try {
            var f = ChessBoard.class.getDeclaredField("squares");
            f.setAccessible(true);
            ChessPiece[][] arr = (ChessPiece[][]) f.get(board);
            if (arr != null && arr.length > 0 && arr[0].length > 0) {
                String firstSquare = arr[0][0] != null
                        ? arr[0][0].getPieceType().toString()
                        : "null";
            }
        } catch (Exception ignore) {
        }

        ChessPiece[][] squares = null;
        try {
            var f = ChessBoard.class.getDeclaredField("squares");
            f.setAccessible(true);
            squares = (ChessPiece[][]) f.get(board);
        } catch (Exception ignore) {
        }

        for (int i = 0; i < 8; i++) {
            int row = whitePerspective ? 8 - i : i + 1;
            out.print(row + " ");

            for (int j = 0; j < 8; j++) {
                int col = whitePerspective ? j + 1 : 8 - j;

                ChessPiece piece = null;

                if (squares != null) {
                    int rIndex = row - 1;
                    int cIndex = col - 1;

                    boolean validRow = rIndex >= 0 && rIndex < squares.length;
                    boolean validCol = validRow && cIndex >= 0 && cIndex < squares[rIndex].length;

                    if (validCol) {
                        piece = squares[rIndex][cIndex];
                    }
                }

                if (piece == null) {
                    piece = board.getPiece(new ChessPosition(row, col));
                }

                boolean isWhiteSquare = (row + col) % 2 != 0;
                printSquare(out, piece, isWhiteSquare);
            }

            out.println(" " + row);
        }

        printFiles(out, whitePerspective);
        out.flush();
    }

    private static void printFiles(PrintStream out, boolean whitePerspective) {
        out.print("  ");
        if (whitePerspective) {
            for (char file = 'a'; file <= 'h'; file++) {
                out.print(" " + file + " ");
            }
        } else {
            for (char file = 'h'; file >= 'a'; file--) {
                out.print(" " + file + " ");
            }
        }
        out.println();
    }

    private static void printSquare(PrintStream out, ChessPiece piece, boolean isWhiteSquare) {
        out.print(isWhiteSquare ? WHITE_BG : BLACK_BG);

        if (piece == null) {
            out.print("   " + RESET_COLOR);
            return;
        }

        String symbol = switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };

        String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? RED_TEXT : BLUE_TEXT;
        out.print(textColor + " " + symbol + " " + RESET_COLOR);
    }

    public static void drawBoardWithHighlights(ChessGame game,
                                               String perspective,
                                               ChessPosition selectedSquare,
                                               Collection<ChessPosition> highlights) {

        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean whitePerspective = perspective.equalsIgnoreCase("white");
        ChessBoard board = game.getBoard();

        if (board == null) {
            out.println("Error: Board data is missing.");
            return;
        }

        printFiles(out, whitePerspective);

        ChessPiece[][] squares = null;
        try {
            var f = ChessBoard.class.getDeclaredField("squares");
            f.setAccessible(true);
            squares = (ChessPiece[][]) f.get(board);
        } catch (Exception ignore) {}

        for (int i = 0; i < 8; i++) {
            int row = whitePerspective ? 8 - i : i + 1;
            out.print(row + " ");

            for (int j = 0; j < 8; j++) {
                int col = whitePerspective ? j + 1 : 8 - j;

                ChessPiece piece = null;
                if (squares != null) {
                    int rIndex = row - 1;
                    int cIndex = col - 1;
                    if (rIndex >= 0 && rIndex < squares.length &&
                            cIndex >= 0 && cIndex < squares[rIndex].length) {
                        piece = squares[rIndex][cIndex];
                    }
                }
                if (piece == null) {
                    piece = board.getPiece(new ChessPosition(row, col));
                }

                boolean isWhiteSquare = (row + col) % 2 != 0;

                boolean isSelected = selectedSquare != null &&
                        selectedSquare.getRow() == row &&
                        selectedSquare.getColumn() == col;

                boolean isHighlighted = false;
                if (highlights != null) {
                    for (ChessPosition hp : highlights) {
                        if (hp.getRow() == row && hp.getColumn() == col) {
                            isHighlighted = true;
                            break;
                        }
                    }
                }

                printSquareWithHighlight(out, piece, isWhiteSquare, isSelected, isHighlighted);
            }

            out.println(" " + row);
        }

        printFiles(out, whitePerspective);
        out.flush();
    }
    private static void printSquareWithHighlight(PrintStream out,
                                                 ChessPiece piece,
                                                 boolean isWhiteSquare,
                                                 boolean isSelected,
                                                 boolean isHighlighted) {

        if (isSelected) {
            out.print(SELECT_BG);

        } else if (isHighlighted) {
            if (isWhiteSquare) {
                out.print("\u001b[102m");
            } else {
                out.print("\u001b[42m");
            }

        } else {
            out.print(isWhiteSquare ? WHITE_BG : BLACK_BG);
        }

        String textColor;
        if (isSelected || isHighlighted) {
            textColor = BLACK_TEXT;
        } else if (piece != null) {
            textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                    ? RED_TEXT
                    : BLUE_TEXT;
        } else {
            textColor = "";
        }

        if (piece == null) {
            out.print("   " + RESET_COLOR);
            return;
        }

        String symbol = switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };

        out.print(textColor + " " + symbol + " " + RESET_COLOR);
    }

}
