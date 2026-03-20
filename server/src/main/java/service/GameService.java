package service;

import dto.*;
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

    public void observeGame(ObserveGameRequest req, String token) throws DataAccessException {
        GameData game = games.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        AuthData auth = auths.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        String username = auth.username();

        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername())) {
            throw new DataAccessException("Cannot observe: already a player");
        }
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
        if (req == null) {
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
        String color = req.playerColor();

        if (color == null || color.trim().isEmpty()) {
            throw new DataAccessException("bad request");
        }

        color = color.trim().toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new DataAccessException("bad request");
        }

        if (color.equals("WHITE")) {
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
        } else { // BLACK
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

        games.updateGame(game);
    }
}