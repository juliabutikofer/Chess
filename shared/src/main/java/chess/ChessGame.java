package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public ChessGame(ChessBoard other) {
        board = new ChessBoard(other);
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return new ArrayList<>();
        }

        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            ChessBoard copy = new ChessBoard(board);
            applyMoveToBoard(copy, move, copy.getPiece(move.getStartPosition()));

            if (!isInCheck(piece.getTeamColor(), copy)) {
                possibleMoves.add(move);
            }
        }
        return possibleMoves;
    }

    private void applyMoveToBoard(ChessBoard board, ChessMove move, ChessPiece piece) {
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    private ChessPosition findKing(TeamColor teamColor, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {

                    return pos;
                }
            }
        }
        return null;
    }

    private boolean isSquareUnderAttack(
            ChessPosition target,
            TeamColor defendingTeam,
            ChessBoard board) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null || piece.getTeamColor() == defendingTeam) {
                    continue;
                }

                for (ChessMove move : piece.pieceMoves(board, pos)) {
                    if (move.getEndPosition().equals(target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

        /**
         * Makes a move in a chess game
         *
         * @param move chess move to perform
         * @throws InvalidMoveException if move is invalid
         */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Can't move piece or not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Move not valid");
        }

        applyMoveToBoard(board, move, piece);

        // Switch turn
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = findKing(teamColor, board);
        return isSquareUnderAttack(kingPosition, teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    private boolean hasAnyValidMove(TeamColor teamColor) {
        TeamColor originalTurn = teamTurn;
        teamTurn = teamColor;

        try {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {

                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(pos);

                    if (piece == null) continue;
                    if (piece.getTeamColor() != teamColor) continue;

                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            teamTurn = originalTurn;
        }
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasAnyValidMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasAnyValidMove(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn &&
                (Objects.equals(board, chessGame.board));
    }

    @Override
    public int hashCode() {
        int result = (board != null ? board.hashCode() : 0);
        result = 31 * result + (teamTurn != null ? teamTurn.hashCode() : 0);
        return result;
    }
}
