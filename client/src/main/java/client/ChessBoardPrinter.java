package client;

import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChessBoardPrinter {

    private static final int SQUARE_WIDTH = 3;

    private static final String RESET_COLOR = "\u001b[0m";
    private static final String WHITE_BG = "\u001b[47m"; // white squares
    private static final String BLACK_BG = "\u001b[40m"; // black squares
    private static final String RED_TEXT = "\u001b[31m";  // bottom of white perspective
    private static final String BLUE_TEXT = "\u001b[34m"; // bottom of black perspective / opponent side

    public static void drawBoard(ChessGame game, String perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean whitePerspective = perspective.equalsIgnoreCase("white");
        ChessBoard board = game.getBoard();

        printFiles(out, whitePerspective);

        for (int row = 1; row <= 8; row++) {
            int displayRow = whitePerspective ? 9 - row : row;
            out.print(displayRow + " ");

            for (int col = 1; col <= 8; col++) {
                int actualRow = whitePerspective ? row : 9 - row;
                int actualCol = whitePerspective ? col : 9 - col;

                ChessPosition pos = new ChessPosition(actualRow, actualCol);
                ChessPiece piece = board.getPiece(pos);

                boolean isWhiteSquare = (actualRow + actualCol) % 2 == 0;

                printSquare(out, piece, isWhiteSquare, row, whitePerspective);
            }

            out.println(" " + displayRow);
        }

        printFiles(out, whitePerspective);
        out.println();
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

    private static void printSquare(PrintStream out, ChessPiece piece, boolean isWhiteSquare,
                                    int row, boolean whitePerspective) {

        out.print(isWhiteSquare ? WHITE_BG : BLACK_BG);

        String symbol = " ";
        if (piece != null) {
            switch (piece.getPieceType()) {
                case KING -> symbol = "K";
                case QUEEN -> symbol = "Q";
                case ROOK -> symbol = "R";
                case BISHOP -> symbol = "B";
                case KNIGHT -> symbol = "N";
                case PAWN -> symbol = "P";
            }
        }

        boolean bottomSide = whitePerspective ? row >= 7 : row <= 2;
        String textColor = bottomSide ? RED_TEXT : BLUE_TEXT;

        out.print(textColor + " " + symbol + " " + RESET_COLOR);
    }
}