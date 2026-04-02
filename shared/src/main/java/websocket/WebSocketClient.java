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
import java.util.concurrent.CountDownLatch;

public class WebSocketClient {

    private final WebSocket webSocket;
    private final Gson gson;
    private final CountDownLatch latch = new CountDownLatch(1);

    private ChessGame currentGame;
    private String perspective = "white"; // default

    public WebSocketClient(String serverUrl) {
        this.gson = new Gson();
        this.webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), new Listener())
                .join();
    }

    public void setGame(ChessGame game, String perspective) {
        this.currentGame = game;
        this.perspective = perspective;
    }

    // Send a command to the server
    public void sendCommand(UserGameCommand command) {
        String json = gson.toJson(command);
        webSocket.sendText(json, true);
    }

    // Close connection
    public void close() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Bye").thenRun(latch::countDown);
    }

    // Wait until connection closes
    public void awaitClose() throws InterruptedException {
        latch.await();
    }

    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("Connected to server WebSocket!");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String json = data.toString();
            ServerMessage msg = gson.fromJson(json, ServerMessage.class);
            System.out.println("Received message: " + msg.getServerMessageType());

            // Handle board update
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
            latch.countDown();
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }
}