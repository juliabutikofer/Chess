package websocket.messages;

import com.google.gson.annotations.SerializedName;
import chess.ChessGame;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 */
public class ServerMessage {

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    // Explicitly match JSON field name
    @SerializedName("serverMessageType")
    private ServerMessageType serverMessageType;

    // Payloads
    public ChessGame game;       // for LOAD_GAME
    public String message;       // for NOTIFICATION
    public String errorMessage;  // for ERROR

    public ServerMessage() {}

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;
    }

    public ServerMessage(ServerMessageType type, String text, boolean isError) {
        this.serverMessageType = type;
        if (isError) this.errorMessage = text;
        else this.message = text;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public void setServerMessageType(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerMessage that)) return false;
        return serverMessageType == that.serverMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMessageType);
    }
}
