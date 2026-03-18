package client;
import client.dtos.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        if (response.statusCode() != 200) throw new Exception("Login failed: " + response.body());

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
        if (response.statusCode() != 200) throw new Exception("Registration failed: " + response.body());

        RegisterResult result = gson.fromJson(response.body(), RegisterResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public void logout() throws Exception {
        if (authToken == null) throw new IllegalStateException("Not logged in");

        LogoutRequest requestObj = new LogoutRequest(authToken);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/session"))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception("Logout failed: " + response.body());

        authToken = null;
    }

    public void createGame(String gameName) throws Exception {
        if (authToken == null) throw new IllegalStateException("Not logged in");

        CreateGameRequest requestObj = new CreateGameRequest(gameName);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception("Create game failed: " + response.body());
    }

    public List<GameData> listGames() throws Exception {
        if (authToken == null) throw new IllegalStateException("Not logged in");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Authorization", authToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception("List games failed: " + response.body());

        ListGamesResult result = gson.fromJson(response.body(), ListGamesResult.class);
        Collection<GameData> games = result.games();
        return new ArrayList<>(games);
    }

    public void joinGame(int gameId, String color) throws Exception {
        if (authToken == null) throw new IllegalStateException("Not logged in");

        JoinGameRequest requestObj = new JoinGameRequest(color, gameId);
        String requestBody = gson.toJson(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception("Join game failed: " + response.body());
    }

    public void observeGame(int gameId) throws Exception {
        if (authToken == null) throw new IllegalStateException("Not logged in");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/game/" + gameId))
                .header("Authorization", authToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception("Observe game failed: " + response.body());
    }

    public String getAuthToken() {
        return authToken;
    }
}