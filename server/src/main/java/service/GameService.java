package service;

import chess.*;
import dto.*;
import dataaccess.*;
import model.AuthData;
import model.GameData;

public class GameService {
    private final GameDAO games;
    private final AuthDAO auths;

    public GameService(GameDAO games, AuthDAO auths) {
        this.games = games;
        this.auths = auths;
    }

    public void joinGame(JoinGameRequest req, String authToken) throws DataAccessException {
        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = games.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String username = auth.username();
        String color = (req.playerColor() != null) ? req.playerColor().toUpperCase() : null;
        if (color == null || color.isBlank()) {
            throw new DataAccessException("bad request");
        }

        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();

        if (color.equals("WHITE")) {
            if (whiteUser != null && !whiteUser.isBlank() && !whiteUser.equalsIgnoreCase(username)) {
                throw new DataAccessException("already taken");
            }
            whiteUser = username;
        } else if (color.equals("BLACK")) {
            if (blackUser != null && !blackUser.isBlank() && !blackUser.equalsIgnoreCase(username)) {
                throw new DataAccessException("already taken");
            }
            blackUser = username;
        } else {
            throw new DataAccessException("bad request");
        }

        games.updateGame(new GameData(game.gameID(), whiteUser, blackUser, game.gameName(), game.game()));
    }

    public ChessGame makeMove(int gameId, ChessMove move, String authToken) throws DataAccessException {
        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData gameData = games.getGame(gameId);
        if (gameData == null) {
            throw new DataAccessException("bad request");
        }

        ChessGame chess = gameData.game();

        if (chess.isOver()) {
            throw new DataAccessException("Error: game is over");
        }

        String username = auth.username();
        boolean isWhite = (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username));
        boolean isBlack = (gameData.blackUsername() != null && gameData.blackUsername().equals(username));

        if (!isWhite && !isBlack) {
            throw new DataAccessException("Error: you are an observer");
        }

        ChessGame.TeamColor turn = chess.getTeamTurn();
        if ((turn == ChessGame.TeamColor.WHITE && !isWhite) ||
                (turn == ChessGame.TeamColor.BLACK && !isBlack)) {
            throw new DataAccessException("Error: not your turn");
        }

        try {
            chess.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new DataAccessException("Error: invalid move");
        }

        GameData updatedData = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                chess
        );

        games.updateGame(updatedData);
        return chess;
    }

    public void observeGame(ObserveGameRequest req, String token) throws DataAccessException {
        if (games.getGame(req.gameID()) == null) {
            throw new DataAccessException("bad request");
        }
        if (auths.getAuth(token) == null) {
            throw new DataAccessException("unauthorized");
        }
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        if (auths.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        return new ListGamesResult(games.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest req, String authToken) throws DataAccessException {
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }
        if (auths.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        int id = games.insertGame(new GameData(0, null, null, req.gameName(), new ChessGame()));
        return new CreateGameResult(id);
    }

    public ChessGame resignGame(int gameId, String authToken) throws DataAccessException {
        GameData gd = games.getGame(gameId);
        AuthData ad = auths.getAuth(authToken);

        if (gd == null) {
            throw new DataAccessException("bad request");
        }
        if (ad == null) {
            throw new DataAccessException("unauthorized");
        }

        if (gd.game().isOver()) {
            throw new DataAccessException("Error: game already over");
        }

        if (!ad.username().equalsIgnoreCase(gd.whiteUsername()) &&
                !ad.username().equalsIgnoreCase(gd.blackUsername())) {
            throw new DataAccessException("Error: observers cannot resign");
        }

        gd.game().resign(ad.username());
        games.updateGame(gd);
        return gd.game();
    }

    public void leaveGame(int gameID, String authToken) throws DataAccessException {
        AuthData auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = games.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String username = auth.username();
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (username.equals(white)) {
            white = null;
        } else if (username.equals(black)) {
            black = null;
        }

        games.updateGame(new GameData(game.gameID(), white, black, game.gameName(), game.game()));
    }

    public AuthData getAuth(String token) throws DataAccessException {
        return auths.getAuth(token);
    }

    public ChessGame getGame(int id) throws DataAccessException {
        return games.getGame(id).game();
    }
}
