package dto;

import chess.ChessPiece;

public record MakeMoveRequest(
        int gameID,
        String move,         // e.g., "e2e4"
        ChessPiece.PieceType promotion
) {}