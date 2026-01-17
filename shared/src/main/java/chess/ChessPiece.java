package chess;

import java.util.Collection;
import java.util.Objects;

import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

     public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    //hardcoded, have to fix
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case BISHOP -> getBishopMoves(board, myPosition);
            case ROOK -> getRookMoves(board, myPosition);
            case QUEEN -> getQueenMoves(board, myPosition);
            case KNIGHT -> getKnightMoves(board, myPosition);
            case KING -> getKingMoves(board, myPosition);
            case PAWN -> getPawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        //bishop
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] dir: directions) {
            int row = startRow;
            int col = startCol;
            while (true) {
                row += dir[0];
                col += dir[1];
                if (!onBoard(row, col)) break;
                if (isEmpty(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                }
                else if (isEnemy(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    break;
                }
                else {
                    break;
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        //rook
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir: directions) {
            int row = startRow;
            int col = startCol;
            while (true) {
                row += dir[0];
                col += dir[1];
                if (!onBoard(row, col)) break;
                if (isEmpty(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                } else if (isEnemy(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    break;
                } else {
                    break;
                }
            }
        }
            return moves;
    }

    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        //queen
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

        for (int[] dir: directions) {
            int row = startRow;
            int col = startCol;
            while (true) {
                row += dir[0];
                col += dir[1];
                if (!onBoard(row, col)) break;
                if (isEmpty(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                } else if (isEnemy(board, row, col)) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    break;
                } else {
                    break;
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        //knight
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
        for (int[] dir : directions) {
            int row = startRow;
            int col = startCol;
            row += dir[0];
            col += dir[1];
            if (!onBoard(row, col)) continue;
            if (isEmpty(board, row, col)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            } else if (isEnemy(board, row, col)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        //king
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        for (int[] dir : directions) {
            int row = startRow;
            int col = startCol;
            row += dir[0];
            col += dir[1];
            if (!onBoard(row, col)) continue;
            if (isEmpty(board, row, col)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            } else if (isEnemy(board, row, col)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        //pawn
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        Collection<ChessMove> moves = new ArrayList<>();

        int dir;
        int startRowForTwoSteps;
        int promotionRow;

        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            dir = 1;
            startRowForTwoSteps = 2;
            promotionRow = 8;
        } else {
            dir = -1;
            startRowForTwoSteps = 7;
            promotionRow = 1;
        }

        int oneStep = startRow + dir;
        if (onBoard(oneStep, startCol) && isEmpty(board, oneStep, startCol)) {
            if (oneStep == promotionRow) {
                for (ChessPiece.PieceType promo : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT,
                        ChessPiece.PieceType.ROOK}) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(oneStep, startCol), promo));
                }
            } else {
                moves.add(new ChessMove(myPosition, new ChessPosition(oneStep, startCol), null));
            }

            int twoStep = startRow + 2 * dir;
            if (startRow == startRowForTwoSteps && isEmpty(board, twoStep, startCol)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(twoStep, startCol), null));
            }
        }

        int diagRow = startRow + dir;
        for (int diagCol : new int[]{startCol - 1, startCol + 1}) {
            if (onBoard(diagRow, diagCol) && isEnemy(board, diagRow, diagCol)) {
                if (diagRow == promotionRow) {
                    for (ChessPiece.PieceType promo : new ChessPiece.PieceType[]{
                            ChessPiece.PieceType.QUEEN,
                            ChessPiece.PieceType.BISHOP,
                            ChessPiece.PieceType.KNIGHT,
                            ChessPiece.PieceType.ROOK}) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(diagRow, diagCol), promo));
                    }
                } else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(diagRow, diagCol), null));
                }
            }
        }
        return moves;
    }

    private boolean isEmpty(ChessBoard board, int row , int col) {
        return board.getPiece(new ChessPosition(row, col)) == null;
    }

    private boolean isEnemy(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return false;
        }
        return piece.getTeamColor() != this.pieceColor;
    }

    private boolean onBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
