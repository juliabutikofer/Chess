package client.dtos;

import chess.ChessGame;
import java.util.List;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) {
    public int id() {
        return gameID;
    }

    public String name() {
        return gameName;
    }

    public List<String> players() {
        return List.of(
                whiteUsername != null ? whiteUsername : "",
                blackUsername != null ? blackUsername : ""
        );
    }
}
