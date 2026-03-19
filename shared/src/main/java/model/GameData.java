package model;

import chess.ChessGame;
import java.util.ArrayList;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
        // add this to the record
) {
}