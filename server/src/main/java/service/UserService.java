package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import dto.RegisterRequest;
import dto.RegisterResult;
import dto.LoginRequest;
import dto.LoginResult;
import dto.LogoutRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() == null) {
            throw new DataAccessException("bad request");
        }

        UserData existing = users.getUser(req.username());
        if (existing != null) {
            throw new DataAccessException("already taken");
        }

        UserData user = new UserData(req.username(), req.password(), req.email());
        users.insertUser(user);

        String token = UUID.randomUUID().toString();
        auths.insertAuth(new AuthData(token, user.username()));

        return new RegisterResult(user.username(), token);
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        UserData user = users.getUser(req.username());
        if (user == null || !BCrypt.checkpw(req.password(), user.password())) {
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        auths.insertAuth(new AuthData(token, user.username()));

        return new LoginResult(user.username(), token);
    }

    public void logout(LogoutRequest req) throws DataAccessException {
        if (req == null || req.authToken() == null) {
            throw new DataAccessException("unauthorized");
        }

        AuthData auth = auths.getAuth(req.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        auths.deleteAuth(req.authToken());
    }
}