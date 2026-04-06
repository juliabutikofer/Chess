package websockethandler;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import service.GameService;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    private final Map<Integer, Set<WsContext>> gameClients = new ConcurrentHashMap<>();

    public WebSocketHandler(Javalin app, GameService gameService) {
        this.gameService = gameService;

        app.ws("/game", ws -> {

            ws.onConnect(ctx -> {
                System.out.println("[WS] Client connected: " + ctx.sessionId());
            });

            ws.onClose(ctx -> {
                removeClient(ctx);
                System.out.println("[WS] Client disconnected: " + ctx.sessionId());
            });

            ws.onMessage(ctx -> {
                try {
                    UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);
                    handleCommand(cmd, ctx);
                } catch (Exception e) {
                    System.out.println("[WS] Failed to parse command: " + e.getMessage());
                    try {
                        ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), true)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        });
    }

    private void handleCommand(UserGameCommand cmd, WsContext ctx) {
        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(cmd, ctx);
            case MAKE_MOVE -> handleMove(cmd, ctx);
            case LEAVE -> handleLeave(cmd, ctx);
            case RESIGN -> handleResign(cmd, ctx);
        }
    }

    private void handleConnect(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();
        gameClients.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(ctx);

        try {
            ChessGame game = gameService.getGame(gameId);

            ServerMessage loadGameMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
            try {
                ctx.send(gson.toJson(loadGameMsg));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("[WS] Client " + ctx.sessionId() + " connected to game " + gameId);
        } catch (Exception e) {
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), true)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleMove(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();
        String move = cmd.getMove();

        try {
            ChessGame updatedGame = gameService.makeMove(gameId, move, cmd.getAuthToken());
            ServerMessage loadGameMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
            broadcastToGame(gameId, loadGameMsg);

            System.out.println("[WS] Move made in game " + gameId + ": " + move);
        } catch (Exception e) {
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), true)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleLeave(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();
        removeClient(ctx);
        System.out.println("[WS] Client " + ctx.sessionId() + " left game " + gameId);
    }

    private void handleResign(UserGameCommand cmd, WsContext ctx) {
        int gameId = cmd.getGameID();

        try {
            ChessGame updatedGame = gameService.resignGame(gameId, cmd.getAuthToken());
            ServerMessage loadGameMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
            broadcastToGame(gameId, loadGameMsg);

            System.out.println("[WS] Player resigned in game " + gameId);
        } catch (Exception e) {
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), true)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void broadcastToGame(int gameId, ServerMessage msg) {
        Set<WsContext> clients = gameClients.get(gameId);
        if (clients != null) {
            String json = gson.toJson(msg);
            for (WsContext client : clients) {
                try {
                    client.send(json);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void removeClient(WsContext ctx) {
        gameClients.values().forEach(set -> set.remove(ctx));
    }
}