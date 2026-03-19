package client.dtos;

public class ObserveGameRequest {
    private final String playerColor; // must be null for observer
    private final int gameID;         // must match server field name

    public ObserveGameRequest(int gameID) {
        this.playerColor = null;
        this.gameID = gameID;
    }
}
