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
        this.connectCommand = connectCommand;
        this.perspective = perspective;

        try {
            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(serverUrl), new Listener())
                    .join();
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
                webSocket.sendText(json, true);
            } catch (Exception e) {
                System.out.println("[WS ERROR] Move sending failed: " + e.getMessage());
            }
        });
    }

    public void setBoardUpdateListener(BoardUpdateListener listener) {
        this.mainListener = listener;
    }

    public interface BoardUpdateListener {
        void onBoardUpdate(ChessGame updatedGame);
        void onNotification(String message);
        void onError(String errorMessage);
    }

    private class Listener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            connected.complete(null);

            // Send the initial CONNECT command
            String json = gson.toJson(connectCommand);
            webSocket.sendText(json, true);

            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String json = data.toString();

            ServerMessage msg;
            try {
                msg = gson.fromJson(json, ServerMessage.class);
            } catch (Exception e) {
                System.err.println("[WS CLIENT] JSON parse error: " +
                        e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace(System.err);
                webSocket.request(1);
                return null;
            }

            if (msg == null) {
                webSocket.request(1);
                return null;
            }

            if (mainListener == null) {
                webSocket.request(1);
                return null;
            }

            switch (msg.getServerMessageType()) {

                case LOAD_GAME -> {
                    if (msg.game == null) {
                        System.err.println("[WS CLIENT] ERROR: LOAD_GAME but game == null");
                        break;
                    }

                    try {
                        if (msg.game.getBoard() != null) {
                            msg.game.getBoard().rebuild();
                        }
                    } catch (Exception e) {
                        System.out.println("[WS CLIENT DEBUG] Board rebuild skipped / not implemented.");
                    }

                    WebSocketClient.this.currentGame = msg.game;
                    mainListener.onBoardUpdate(msg.game);

                    System.out.print("gameplay> ");
                }

                case NOTIFICATION -> {
                    mainListener.onNotification(msg.message);

                    System.out.print("gameplay> ");
                }

                case ERROR -> {
                    mainListener.onError(msg.errorMessage);

                    System.out.print("gameplay> ");
                }
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
