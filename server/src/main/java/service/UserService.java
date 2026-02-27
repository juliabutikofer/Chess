package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import DTO.RegisterRequest;
import DTO.RegisterResult;
import DTO.LoginRequest;
import DTO.LoginResult;
import DTO.LogoutRequest;

import java.util.UUID;

public class UserService {

    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            throw new DataAccessException("bad request");
        }

        UserData existing = users.getUser(req.getUsername());
        if (existing != null) {
            throw new DataAccessException("already taken");
        }

        UserData user = new UserData(req.getUsername(), req.getPassword(), req.getEmail());
        users.insertUser(user);

        String token = UUID.randomUUID().toString();
        auths.insertAuth(new AuthData(token, user.username()));

        return new RegisterResult(user.username(), token);
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        UserData user = users.getUser(req.getUsername());
        if (user == null || !user.password().equals(req.getPassword())) {
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        auths.insertAuth(new AuthData(token, user.username()));

        return new LoginResult(user.username(), token);
    }

    public void logout(LogoutRequest req) throws DataAccessException {

        if (req == null || req.getAuthToken() == null) {
            throw new DataAccessException("unauthorized");
        }

        AuthData auth = auths.getAuth(req.getAuthToken());

        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        auths.deleteAuth(req.getAuthToken());
    }
}
