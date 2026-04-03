package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import ChessBoardPrinter.ChessBoardPrinter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketClient {

    private WebSocket webSocket;
    private final Gson gson = new Gson();

    private ChessGame currentGame;
    private String perspective = "white";
    private UserGameCommand connectCommand; // store the initial connect command

    public WebSocketClient(String serverUrl, UserGameCommand connectCommand, ChessGame game, String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
        this.connectCommand = connectCommand;

        // Build WebSocket asynchronously
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), new Listener())
                .thenAccept(ws -> this.webSocket = ws);
    }

    public void sendCommand(UserGameCommand command) {
        if (webSocket != null) {
            String json = gson.toJson(command);
            webSocket.sendText(json, true);
        }
    }

    public void setGame(ChessGame game, String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
    }

    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("Connected to server WebSocket!");
            // Send CONNECT immediately
            if (connectCommand != null) sendCommand(connectCommand);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            ServerMessage msg = gson.fromJson(data.toString(), ServerMessage.class);
            if (msg.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME && currentGame != null) {
                ChessBoardPrinter.drawBoard(currentGame, perspective);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("WebSocket error: " + error.getMessage());
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("WebSocket closed: " + statusCode + " " + reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }
}