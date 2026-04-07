package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import typeadapter.JSONSerializer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketClient {

    private WebSocket webSocket;
    private final Gson gson = JSONSerializer.getGson();
    private ChessGame currentGame;
    private final UserGameCommand connectCommand;
    private final CompletableFuture<Void> connected = new CompletableFuture<>();
    private BoardUpdateListener mainListener;
    private String perspective;

    public WebSocketClient(String serverUrl, UserGameCommand connectCommand, ChessGame game, String perspective) {
//        this.currentGame = null;
        this.connectCommand = connectCommand;
        this.perspective = perspective;

        try {
//            System.out.println("[WS CLIENT] Attempting to connect to: " + serverUrl);
            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(serverUrl), new Listener())
                    .join();
//            System.out.println("[WS CLIENT] Listener registered.");
        } catch (Exception e) {
            System.out.println("[WS ERROR] Connection failed: " + e.getMessage());
        }
    }

    public void setGame(ChessGame game, String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
    }

    public ChessGame getGame() { return currentGame; }

    public void sendCommand(UserGameCommand command) {
        connected.thenRun(() -> {
            try {
                String json = gson.toJson(command);
                webSocket.sendText(json, true);
            } catch (Exception e) {
                System.out.println("[WS ERROR] Failed to send command: " + e.getMessage());
            }
        });
    }

    public void joinGame(int gameId) {
        UserGameCommand joinCmd = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                getConnectToken(),
                gameId
        );
        sendCommand(joinCmd);
    }

    public void sendMove(ChessMove move) {
        connected.thenRun(() -> {
            try {
                UserGameCommand cmd = new UserGameCommand(
                        UserGameCommand.CommandType.MAKE_MOVE,
                        getConnectToken(),
                        getGameID()
                );
                cmd.setMove(move);
                String json = gson.toJson(cmd);
//                System.out.println("[WS CLIENT] Sending Move: " + json);
                webSocket.sendText(json, true);
            } catch (Exception e) {
                System.out.println("[WS ERROR] Move sending failed: " + e.getMessage());
            }
        });
    }

    public void setBoardUpdateListener(BoardUpdateListener listener) {
        this.mainListener = listener;
//        System.out.println("[WS CLIENT] New Listener attached.");
    }

    public interface BoardUpdateListener {
        void onBoardUpdate(ChessGame updatedGame);
        void onNotification(String message);
        void onError(String errorMessage);
    }

    private class Listener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
//            System.out.println("!!! WEBSOCKET OPENED SUCCESSFULLY !!!");
            connected.complete(null);
            webSocket.request(1); // keep listening
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String json = data.toString();
//            System.out.println("\n*****************************************");
//            System.out.println(">>> onText triggered (" + json.length() + " chars)");
//            System.out.println("[WS CLIENT] Raw JSON: " + json);
//            System.out.println("*****************************************\n");

            try {
                ServerMessage msg = gson.fromJson(json, ServerMessage.class);

                if (msg == null) {
//                    System.out.println(">>> Gson returned null ServerMessage");
                } else {
//                    System.out.println(">>> Gson parsed msgType=" + msg.getServerMessageType());
//                    System.out.println(">>> Game object is " + (msg.game == null ? "NULL" : "NON-NULL"));
                }

                if (msg != null && mainListener != null) {
                    switch (msg.getServerMessageType()) {
                        case LOAD_GAME -> {
                            if (msg.game != null) {
                                try {
                                    if (msg.game.getBoard() != null) {
                                        msg.game.getBoard().rebuild();
                                    }
                                } catch (Exception e) {
                                    System.out.println("[WS CLIENT DEBUG] Board rebuild skipped / not implemented.");
                                }

//                                System.out.println("[WS CLIENT] Updating game state and redrawing board...");
                                WebSocketClient.this.currentGame = msg.game;
                                mainListener.onBoardUpdate(msg.game);
                            } else {
                                System.err.println("[WS CLIENT] ERROR: LOAD_GAME but game == null");
                            }
                        }
                        case NOTIFICATION -> mainListener.onNotification(msg.message);
                        case ERROR -> mainListener.onError(msg.errorMessage);
                    }
                }
            } catch (Exception e) {
                System.err.println("[WS CLIENT] JSON parse error: " +
                        e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace(System.err);
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            String msg = error.getMessage();
            if (msg != null && msg.contains("invalid move")) {
                return;
            }

            System.out.println("[WS ERROR] " + msg);
        }


        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("!!! WEBSOCKET CLOSED !!! Reason: " + reason + " (Code: " + statusCode + ")");
            return null;
        }
    }


    public String getConnectToken() { return connectCommand.getAuthToken(); }
    public Integer getGameID() { return connectCommand.getGameID(); }
    public CompletableFuture<Void> getConnectedFuture() { return connected; }
    public String getPerspective() { return perspective; }
}
