package ChessBoardPrinter;

import chess.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChessBoardPrinter {

    private static final String RESET_COLOR = "\u001b[0m";
    private static final String WHITE_BG = "\u001b[47m"; // white squares
    private static final String BLACK_BG = "\u001b[40m"; // black squares
    private static final String RED_TEXT = "\u001b[31m";  // white pieces
    private static final String BLUE_TEXT = "\u001b[34m"; // black pieces

    public static void drawBoard(ChessGame game, String perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean whitePerspective = perspective.equalsIgnoreCase("white");
        ChessBoard board = game.getBoard();

        if (board == null) {
            out.println("Error: Board data is missing.");
            return;
        }

        printFiles(out, whitePerspective);
        out.println();

        try {
            var f = ChessBoard.class.getDeclaredField("squares");
            f.setAccessible(true);
            ChessPiece[][] arr = (ChessPiece[][]) f.get(board);
            String firstSquare = (arr != null && arr[0][0] != null)
                    ? arr[0][0].getPieceType().toString()
                    : "null";
            out.println("[PRINTER DEBUG] squares[0][0] = " + firstSquare);
            out.println();
        } catch (Exception ignore) {
        }

        for (int i = 0; i < 8; i++) {
            int row = whitePerspective ? 8 - i : i + 1;
            out.print(row + " ");

            for (int j = 0; j < 8; j++) {
                int col = whitePerspective ? j + 1 : 8 - j;

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = null;

                try {
                    var f = ChessBoard.class.getDeclaredField("squares");
                    f.setAccessible(true);
                    ChessPiece[][] arr = (ChessPiece[][]) f.get(board);
                    if (arr != null) {
                        int rIndex = row - 1;
                        int cIndex = col - 1;
                        if (rIndex >= 0 && rIndex < arr.length &&
                                cIndex >= 0 && cIndex < arr[rIndex].length) {
                            piece = arr[rIndex][cIndex];
                        }
                    }
                } catch (Exception e) {
                    piece = board.getPiece(pos);
                }

                boolean isWhiteSquare = (row + col) % 2 != 0;
                printSquare(out, piece, isWhiteSquare);
            }
            out.println(" " + row);
        }

        out.println();
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
}
