package websockethandler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import chess.ChessMove;
import service.GameService;
import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();
    private final Map<Integer, Set<WsContext>> gameClients = new ConcurrentHashMap<>();

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCommand(UserGameCommand cmd, WsContext ctx) {
        if (cmd == null || cmd.getCommandType() == null) return;

        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(cmd, ctx);
            case MAKE_MOVE -> handleMove(cmd, ctx);
            case LEAVE -> handleLeave(cmd, ctx);
            case RESIGN -> handleResign(cmd, ctx);
        }
    }

    private void handleConnect(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();
        String authToken = cmd.getAuthToken();

        try {
            AuthData auth = gameService.getAuth(authToken);
            if (auth == null) {
                throw new DataAccessException("Error: unauthorized");
            }

            gameClients.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(ctx);

            ChessGame game = gameService.getGame(gameId);
            if (game == null) throw new DataAccessException("Error: game not found");

            ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game)));

            String message = String.format("%s joined the game", auth.username());
            broadcastToOthers(gameId, ctx.sessionId(), new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, false));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleMove(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();
        ChessMove move = cmd.getMove();

        if (move == null) {
            sendError(ctx, "Error: Move data was missing or malformed.");
            return;
        }

        try {
            AuthData auth = gameService.getAuth(cmd.getAuthToken());
            if (auth == null) throw new DataAccessException("Error: unauthorized");

            ChessGame updatedGame = gameService.makeMove(gameId, move, cmd.getAuthToken());

            broadcastToGame(gameId, new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame));

            String noteText = String.format("%s moved %s", auth.username(), move.toString());
            broadcastToOthers(gameId, ctx.sessionId(), new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, noteText, false));

            checkGameState(gameId, updatedGame);

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleResign(UserGameCommand cmd, WsContext ctx) {
        try {
            AuthData auth = gameService.getAuth(cmd.getAuthToken());
            if (auth == null) throw new DataAccessException("Error: unauthorized");

            gameService.resignGame(cmd.getGameID(), cmd.getAuthToken());

            String message = String.format("%s has resigned. Game over.", auth.username());
            broadcastToGame(cmd.getGameID(), new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, false));
        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleLeave(UserGameCommand cmd, WsContext ctx) {
        try {
            int gameId = cmd.getGameID();
            String authToken = cmd.getAuthToken();
            AuthData auth = gameService.getAuth(authToken);

            gameService.leaveGame(gameId, authToken);

            removeClient(ctx, gameId);

            if (auth != null) {
                String message = String.format("%s left the game", auth.username());
                broadcastToOthers(gameId, ctx.sessionId(), new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, false));
            }
        } catch (Exception e) {
        }
    }

    private void checkGameState(int gameId, ChessGame game) {
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            broadcastToGame(gameId, new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "White is in checkmate!", false));
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            broadcastToGame(gameId, new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Black is in checkmate!", false));
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            broadcastToGame(gameId, new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "White is in check!", false));
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            broadcastToGame(gameId, new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Black is in check!", false));
        }
    }

    private void sendError(WsContext ctx, String errorMessage) {
        String msg = errorMessage.toLowerCase().contains("error") ? errorMessage : "Error: " + errorMessage;
        ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, msg, true)));
    }

    private void broadcastToGame(int gameId, ServerMessage msg) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients != null) {
            String json = gson.toJson(msg);
            for (WsContext client : clients) {
                try {
                    client.send(json);
                } catch (Exception e) {
                }
            }
        }
    }

    private void broadcastToOthers(int gameId, String excludeId, ServerMessage msg) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients != null) {
            String json = gson.toJson(msg);
            for (WsContext client : clients) {
                if (!client.sessionId().equals(excludeId)) {
                    try {
                        client.send(json);
                    } catch (Exception e) {
                        // Handled
                    }
                }
            }
        }
    }

    public void removeClient(WsContext ctx, int gameId) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients != null) clients.remove(ctx);
    }

    public void removeClient(WsContext ctx) {
        if (ctx == null) {
            gameClients.clear();
        } else {
            gameClients.values().forEach(set -> set.remove(ctx));
        }
    }
}