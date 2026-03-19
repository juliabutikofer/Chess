package dto;

public class ObserveGameRequest {

    private final String role;
    private final int gameId;

    // Constructor for observers
    public ObserveGameRequest(int gameId) {
        this.role = "observer"; // tells the server this is an observer
        this.gameId = gameId;
    }

    // Getters (needed for JSON serialization)
    public String getRole() {
        return role;
    }

    public int getGameId() {
        return gameId;
    }
}
