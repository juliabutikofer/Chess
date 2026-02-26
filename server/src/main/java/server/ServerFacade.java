package server;

import model.*;
import dataaccess.*;

public class ServerFacade {

    private final DataAccess dataAccess;

    public ServerFacade(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // Example: Register a user
    public AuthData registerUser(String username, String password, String email) throws DataAccessException {
        // Check for nulls or invalid input
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        // Create user
        UserData user = new UserData(username, password, email);
        dataAccess.insertUser(user);

        // Generate auth token (simple example)
        String token = username + "-token";  // you’ll want a proper unique token
        AuthData auth = new AuthData(token, username);
        dataAccess.insertAuth(auth);

        return auth;
    }

    // TODO: Add other methods for login, logout, createGame, joinGame, listGames...
}