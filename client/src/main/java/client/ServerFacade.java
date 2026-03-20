package client;
import client.dtos.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import com.google.gson.Gson;

public class ServerFacade {

    private final String baseUrl;
    private String authToken;
    private final HttpClient client;
    private final Gson gson;

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public LoginResult login(String username, String password) throws Exception {
        LoginRequest requestObj = new LoginRequest(username, password);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/session"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Login failed: " + response.body());
        }

        LoginResult result = gson.fromJson(response.body(), LoginResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public RegisterResult register(String username, String password, String email) throws Exception {
        RegisterRequest requestObj = new RegisterRequest(username, password, email);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Registration failed: " + response.body());
        }

        RegisterResult result = gson.fromJson(response.body(), RegisterResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public void logout() throws Exception {
        if (authToken == null) {
            throw new IllegalStateException("Not logged in");
        }

        LogoutRequest requestObj = new LogoutRequest(authToken);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/session"))
                .header("Authorization", authToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Logout failed: " + response.body());
        }

        authToken = null;
    }

    public void createGame(String gameName) throws Exception {
        if (authToken == null) {
            throw new IllegalStateException("Not logged in");
        }

        CreateGameRequest requestObj = new CreateGameRequest(gameName);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Create game failed: " + response.body());
        }
    }

    public List<GameData> listGames() throws Exception {
        if (authToken == null) {
            throw new IllegalStateException("Not logged in");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Authorization", authToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("List games failed: " + response.body());
        }

        ListGamesResult result = gson.fromJson(response.body(), ListGamesResult.class);

        List<GameData> games = new ArrayList<>(result.games());
        games.sort(Comparator.comparingInt(GameData::id)); // FIX

        return games;
    }


    public void joinGame(int gameId, String color) throws Exception {
        if (authToken == null) {
            throw new IllegalStateException("Not logged in");
        }

        JoinGameRequest requestObj = new JoinGameRequest(color, gameId);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Join game failed: " + response.body());
        }
    }

    public void observeGame(int gameId) throws Exception {
        if (authToken == null) {
            throw new IllegalStateException("Not logged in");
        }

        ObserveGameRequest requestObj = new ObserveGameRequest(gameId);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("OBSERVE RESPONSE: " + response.body());
            throw new Exception("Observe game failed: " + response.body());
        }
    }



    public void clearDatabase() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/db"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Database reset failed: " + response.body());
        }
    }

    public String getAuthToken() {
        return authToken;
    }
}