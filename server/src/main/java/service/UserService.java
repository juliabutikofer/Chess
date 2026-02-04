package service;

import server.LogoutRequest;
import server.RegisterRequest;

public class UserService {
    public RegisterResult register(RegisterRequest registerRequest) {}
    public LoginResult login(LoginRequest loginRequest) {}
    public void logout(LogoutRequest logoutRequest) {}
}


record LoginRequest(
        String username,
        String password
) {}


record LoginResult(
        String username,
        String authtoken
) {}



