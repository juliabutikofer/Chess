package service;

import DTO.*;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import java.util.Collection;

public class GameService {

    private final GameDAO games;
    private final AuthDAO auths;

    public GameService(GameDAO games, AuthDAO auths) {
        this.games = games;
        this.auths = auths;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        Collection<GameData> list = games.listGames();
        return new ListGamesResult(list);
    }

    public CreateGameResult createGame(CreateGameRequest req, String authToken) throws DataAccessException {
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }

        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = new GameData(
                0,
                null,
                null,
                req.gameName(),
                new ChessGame()
        );

        int gameID = games.insertGame(game);
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest req, String authToken) throws DataAccessException {
        if (req == null || req.playerColor() == null) {
            throw new DataAccessException("bad request");
        }

        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = games.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String username = auth.username();
        String color = req.playerColor().toUpperCase();

        switch (color) {
            case "WHITE" -> {
                if (game.whiteUsername() != null) {
                    throw new DataAccessException("already taken");
                }

                game = new GameData(
                        game.gameID(),
                        username,
                        game.blackUsername(),
                        game.gameName(),
                        game.game()
                );
            }
            case "BLACK" -> {
                if (game.blackUsername() != null) {
                    throw new DataAccessException("already taken");
                }

                game = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        username,
                        game.gameName(),
                        game.game()
                );
            }
            default -> throw new DataAccessException("bad request");
        }

        games.updateGame(game);
    }
}