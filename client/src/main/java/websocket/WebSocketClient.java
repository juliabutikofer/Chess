package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketClient {

    private WebSocket webSocket;
    private final Gson gson = new Gson();

    private ChessGame currentGame;
    private String perspective;
    private final UserGameCommand connectCommand;

    private final CompletableFuture<Void> connected = new CompletableFuture<>();
    private final Object printLock = new Object();

    // Listeners
    private final List<BoardUpdateListener> boardListeners = new ArrayList<>();
    private BoardUpdateListener mainListener;

    public WebSocketClient(String serverUrl,
                           UserGameCommand connectCommand,
                           ChessGame game,
                           String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
        this.connectCommand = connectCommand;

        try {
            System.out.println("Connecting to WebSocket at: " + serverUrl);
            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(serverUrl), new Listener())
                    .join();
        } catch (Exception e) {
            synchronized (printLock) {
                System.out.println("[WS ERROR] Could not connect: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void sendCommand(UserGameCommand command) {
        connected.thenRun(() -> {
            try {
                String json = gson.toJson(command);
                webSocket.sendText(json, true);
            } catch (Exception e) {
                synchronized (printLock) {
                    System.out.println("[WS ERROR] Failed to send command: " + e.getMessage());
                }
            }
        });
    }

    public void setGame(ChessGame game, String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
    }

    public CompletableFuture<Void> getConnectedFuture() {
        return connected;
    }

    public void addBoardUpdateListener(BoardUpdateListener listener) {
        boardListeners.add(listener);
    }

    public void setBoardUpdateListener(BoardUpdateListener listener) {
        this.mainListener = listener;
        addBoardUpdateListener(listener);
    }

    public interface BoardUpdateListener {
        void onBoardUpdate(ChessGame updatedGame);
        void onNotification(String message);
        void onError(String errorMessage);
    }

    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            synchronized (printLock) {
                System.out.println("[WS] Connected!");
            }
            WebSocketClient.this.webSocket = webSocket;
            connected.complete(null);

            // Send the initial connect command
            if (connectCommand != null) {
                sendCommand(connectCommand);
            }
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket,
                                         CharSequence data,
                                         boolean last) {
            synchronized (printLock) {
                try {
                    ServerMessage msg = gson.fromJson(data.toString(), ServerMessage.class);

                    switch (msg.getServerMessageType()) {
                        case LOAD_GAME -> {
                            if (msg.game != null) {
                                currentGame = msg.game;
                                for (BoardUpdateListener listener : boardListeners) {
                                    listener.onBoardUpdate(msg.game);
                                }
                            }
                        }
                        case NOTIFICATION -> {
                            if (msg.message != null) {
                                for (BoardUpdateListener listener : boardListeners) {
                                    listener.onNotification(msg.message);
                                }
                            }
                        }
                        case ERROR -> {
                            if (msg.errorMessage != null) {
                                for (BoardUpdateListener listener : boardListeners) {
                                    listener.onError(msg.errorMessage);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("[WS ERROR] Bad message: " + e.getMessage());
                }
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            synchronized (printLock) {
                System.out.println("[WS ERROR] " + error.getMessage());
                error.printStackTrace();
                for (BoardUpdateListener listener : boardListeners) {
                    listener.onError(error.getMessage());
                }
            }
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket,
                                          int statusCode,
                                          String reason) {
            synchronized (printLock) {
                System.out.println("[WS] Closed: " + reason);
            }
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }
    public String getConnectToken() {
        return connectCommand != null ? connectCommand.getAuthToken() : null;
    }

    public Integer getGameID() {
        return connectCommand != null ? connectCommand.getGameID() : null;
    }

}