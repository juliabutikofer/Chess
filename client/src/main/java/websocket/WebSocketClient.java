package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketClient {

    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private ChessGame currentGame;
    private final UserGameCommand connectCommand;
    private final CompletableFuture<Void> connected = new CompletableFuture<>();
    private BoardUpdateListener mainListener;
    private String perspective;

    public WebSocketClient(String serverUrl, UserGameCommand connectCommand, ChessGame game, String perspective) {
        this.currentGame = game;
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

    public void sendCommand(UserGameCommand command) {
        connected.thenRun(() -> {
            webSocket.sendText(gson.toJson(command), true);
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
                System.out.println("[WS ERROR] Failed to send move: " + e.getMessage());
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
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String json = data.toString();
            ServerMessage msg = gson.fromJson(json, ServerMessage.class);

            if (mainListener != null) {
                switch (msg.getServerMessageType()) {
                    case LOAD_GAME -> {
                        currentGame = msg.game;
                        mainListener.onBoardUpdate(msg.game);
                    }
                    case NOTIFICATION -> mainListener.onNotification(msg.message);
                    case ERROR -> mainListener.onError(msg.errorMessage);
                }
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }

    public String getConnectToken() { return connectCommand.getAuthToken(); }
    public Integer getGameID() { return connectCommand.getGameID(); }
    public CompletableFuture<Void> getConnectedFuture() { return connected; }
    public String getPerspective() { return perspective; }
}