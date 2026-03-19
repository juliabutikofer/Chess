package client;

public class ChessBoardPrinter {

    // Draws the initial chessboard from the given perspective
    public static void drawInitialBoard(String perspective) {
        System.out.println("\nInitial Chess Board (" + perspective + " perspective):");

        String[][] board = new String[8][8];

        // Empty squares
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = " . ";
            }
        }

        // Pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = "p "; // black
            board[6][i] = "P "; // white
        }

        // Rooks
        board[0][0] = board[0][7] = "R ";
        board[7][0] = board[7][7] = "r ";

        // Knights
        board[0][1] = board[0][6] = "N ";
        board[7][1] = board[7][6] = "n ";

        // Bishops
        board[0][2] = board[0][5] = "B ";
        board[7][2] = board[7][5] = "b ";

        // Queens
        board[0][3] = "Q ";
        board[7][3] = "q ";

        // Kings
        board[0][4] = "K ";
        board[7][4] = "k ";

        // Print board
        if (perspective.equalsIgnoreCase("white")) {
            printWhitePerspective(board);
        } else {
            printBlackPerspective(board);
        }
    }

    private static void printWhitePerspective(String[][] board) {
        System.out.println("  a b c d e f g h");
        for (int i = 0; i < 8; i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < 8; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println(" " + (8 - i));
        }
        System.out.println("  a b c d e f g h\n");
    }

    private static void printBlackPerspective(String[][] board) {
        System.out.println("  h g f e d c b a");
        for (int i = 7; i >= 0; i--) {
            System.out.print((8 - i) + " ");
            for (int j = 7; j >= 0; j--) {
                System.out.print(board[i][j]);
            }
            System.out.println(" " + (8 - i));
        }
        System.out.println("  h g f e d c b a\n");
    }
}