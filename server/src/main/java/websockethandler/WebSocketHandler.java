package websockethandler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.*;
import service.GameService;
import io.javalin.websocket.WsContext;
import typeadapter.JSONSerializer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final GameService gameService;
    private final Gson gson = JSONSerializer.getGson();
    private final Map<Integer, Set<WsContext>> gameClients = new ConcurrentHashMap<>();

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCommand(UserGameCommand cmd, WsContext ctx) {
        if (cmd == null || cmd.getCommandType() == null) {
            return;
        }
        System.out.println("[HANDLER] Processing: " + cmd.getCommandType());

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
                throw new DataAccessException("unauthorized");
            }

            GameData gameData = gameService.getGame(gameId);
            if (gameData == null) {
                throw new DataAccessException("game not found");
            }

            ChessGame game = gameData.game();

            gameClients.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(ctx);

            ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game)));

            String username = auth.username();
            String roleMsg;

            if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
                roleMsg = username + " joined as white";
            } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
                roleMsg = username + " joined as black";
            } else {
                roleMsg = username + " joined as observer";
            }

            broadcastToOthers(
                    gameId,
                    ctx.sessionId(),
                    new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, roleMsg, false)
            );

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }


    private void handleMove(UserGameCommand cmd, WsContext ctx) {
        try {
            AuthData auth = gameService.getAuth(cmd.getAuthToken());
            System.out.println("[HANDLER] Making move for " + auth.username());

            ChessGame updatedGame = gameService.makeMove(cmd.getGameID(), cmd.getMove(), cmd.getAuthToken());

            System.out.println("[HANDLER] Move successful, checking for check...");

            ChessGame.TeamColor toMove = updatedGame.getTeamTurn();

            if (!updatedGame.isOver() && updatedGame.isInCheck(toMove)) {
                broadcastToGame(
                        cmd.getGameID(),
                        new ServerMessage(
                                ServerMessage.ServerMessageType.NOTIFICATION,
                                (toMove == ChessGame.TeamColor.WHITE ? "White" : "Black") + " is in check!",
                                false
                        )
                );
            }

            System.out.println("[HANDLER] Broadcasting board...");
            broadcastToGame(cmd.getGameID(),
                    new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame));

            broadcastToOthers(cmd.getGameID(), ctx.sessionId(),
                    new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            auth.username() + " made a move.", false));

            if (updatedGame.isOver()) {
                System.out.println("[HANDLER] Game over detected, broadcasting NOTIFICATION...");

                String winner;

                if (updatedGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                    winner = "Black wins!";
                } else if (updatedGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                    winner = "White wins!";
                } else {
                    winner = "Game over.";
                }

                broadcastToGame(cmd.getGameID(),
                        new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                "Checkmate! " + winner, false));
            }

        } catch (Exception e) {
            System.out.println("[HANDLER MOVE ERROR] " + e.getMessage());
            e.printStackTrace();
            sendError(ctx, e.getMessage());
        }
    }


    private void handleResign(UserGameCommand cmd, WsContext ctx) {
        try {
            gameService.resignGame(cmd.getGameID(), cmd.getAuthToken());

            GameData gameData = gameService.getGame(cmd.getGameID());
            ChessGame game = gameData.game();

            String loser = game.getResignedBy();
            String winner;

            if (loser.equals(gameData.whiteUsername())) {
                winner = gameData.blackUsername();
            } else {
                winner = gameData.whiteUsername();
            }

            broadcastToGame(cmd.getGameID(),
                    new ServerMessage(
                            ServerMessage.ServerMessageType.NOTIFICATION,
                            loser + " resigned. " + winner + " wins!",
                            false));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }


    private void handleLeave(UserGameCommand cmd, WsContext ctx) {
        try {
            Set<WsContext> clients = gameClients.getOrDefault(cmd.getGameID(), ConcurrentHashMap.newKeySet());
            clients.remove(ctx);

            gameService.leaveGame(cmd.getGameID(), cmd.getAuthToken());

            AuthData auth = gameService.getAuth(cmd.getAuthToken());
            String username = (auth != null) ? auth.username() : "Unknown user";

            broadcastToOthers(cmd.getGameID(), ctx.sessionId(),
                    new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, username + " has left the game.", false));

            System.out.println("[HANDLER] " + username + " left game " + cmd.getGameID());

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }


    private void sendError(WsContext ctx, String err) {
        System.out.println("[WS ERROR SENT TO CLIENT]: " + err);

        String json = gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR,  err, true));
        ctx.send(json);
    }

    private void broadcastToGame(int gameId, ServerMessage msg) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients == null) {
            return;
        }

        String json;
        try {
            json = gson.toJson(msg);
        } catch (Exception e) {
            System.out.println("[BROADCAST CRITICAL] Failed to serialize: " + e.getMessage());
            return;
        }

        System.out.println("[BROADCAST] Sending to " + clients.size() + " clients. JSON size: " + json.length());

        java.util.List<WsContext> clientsCopy = new java.util.ArrayList<>(clients);

        for (WsContext client : clientsCopy) {
            if (!client.session.isOpen()) {
                clients.remove(client);
                continue;
            }

            try {
                client.send(json);
            } catch (Exception e) {
                System.out.println("[BROADCAST ERROR] Failed to send: " + e.getMessage());
                clients.remove(client);
            }
        }
    }


    private void broadcastToOthers(int gameId, String excludeId, ServerMessage msg) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients == null) {
            return;
        }

        String json;
        try {
            json = gson.toJson(msg);
        } catch (Exception e) {
            System.out.println("[BROADCAST OTHERS CRITICAL] " + e.getMessage());
            return;
        }

        java.util.List<WsContext> clientsCopy = new java.util.ArrayList<>(clients);

        for (WsContext client : clientsCopy) {
            boolean open = client.session.isOpen();
            boolean notExcluded = !client.sessionId().equals(excludeId);

            if (!open || !notExcluded) {
                if (!open) {
                    clients.remove(client);
                }
                continue;
            }

            try {
                client.send(json);
            } catch (Exception e) {
                System.out.println("[BROADCAST OTHERS ERROR] " + e.getMessage());
                clients.remove(client);
            }
        }
    }


    public void removeClient(WsContext ctx) {
        gameClients.values().forEach(v -> v.remove(ctx));
    }
}