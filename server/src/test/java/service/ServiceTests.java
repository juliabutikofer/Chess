package service;
import dataaccess.*;
import model.*;
import dto.*;

import model.GameData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    //REGISTER

    //positive test
    @Test
    public void registerPositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        RegisterRequest req = new RegisterRequest("bob", "pass", "bob@email.com");
        RegisterResult result = service.register(req);

        assertNotNull(result);
        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }

    //negative test
    @Test
    public void registerDuplicate() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        RegisterRequest req = new RegisterRequest("bob", "pass", "email");
        service.register(req);

        assertThrows(DataAccessException.class,
                () -> service.register(req));
    }

    //LOGIN

    //positive test
    @Test
    public void loginPositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        service.register(new RegisterRequest("bob", "pass", "email"));

        LoginResult result =
                service.login(new LoginRequest("bob", "pass"));

        assertNotNull(result);
        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }

    //negative test
    @Test
    public void loginWrongPassword() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        service.register(new RegisterRequest("bob", "pass", "email"));

        assertThrows(DataAccessException.class,
                () -> service.login(new LoginRequest("bob", "wrong")));
    }

    //LOGOUT

    //positive test
    @Test
    public void logoutPositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        RegisterResult reg =
                service.register(new RegisterRequest("bob", "pass", "email"));

        service.logout(new LogoutRequest(reg.authToken()));

        assertNull(authDAO.getAuth(reg.authToken()));
    }

    //negative test
    @Test
    public void logoutInvalidToken() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        UserService service = new UserService(userDAO, authDAO);

        assertThrows(DataAccessException.class,
                () -> service.logout(new LogoutRequest("badToken")));
    }

    //GAME SERVICE

    //positive test
    @Test
    public void createGamePositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        RegisterResult reg =
                userService.register(new RegisterRequest("bob", "pass", "email"));

        CreateGameResult result =
                gameService.createGame(
                        new CreateGameRequest("TestGame"),
                        reg.authToken());

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    //negative test
    @Test
    public void createGameUnauthorized() {
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        GameService gameService = new GameService(gameDAO, authDAO);

        assertThrows(DataAccessException.class,
                () -> gameService.createGame(
                        new CreateGameRequest("TestGame"),
                        "badToken"));
    }

    //positive test
    @Test
    public void listGamesPositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        RegisterResult reg =
                userService.register(new RegisterRequest("bob", "pass", "email"));

        gameService.createGame(
                new CreateGameRequest("TestGame"),
                reg.authToken());

        ListGamesResult result =
                gameService.listGames(reg.authToken());

        assertEquals(1, result.games().size());
    }

    //negative test
    @Test
    public void listGamesUnauthorized() {
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        GameService gameService = new GameService(gameDAO, authDAO);

        assertThrows(DataAccessException.class,
                () -> gameService.listGames("badToken"));
    }

    //positive test
    @Test
    public void joinGamePositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        RegisterResult reg =
                userService.register(new RegisterRequest("bob", "pass", "email"));

        CreateGameResult game =
                gameService.createGame(
                        new CreateGameRequest("TestGame"),
                        reg.authToken());

        gameService.joinGame(new JoinGameRequest("WHITE", game.gameID()), reg.authToken());

        GameData updated = gameDAO.getGame(game.gameID());
        assertEquals("bob", updated.whiteUsername());
    }

    //negative test
    @Test
    public void joinGameTaken() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        RegisterResult reg1 =
                userService.register(new RegisterRequest("bob", "pass", "email"));

        RegisterResult reg2 =
                userService.register(new RegisterRequest("alice", "pass", "email"));

        CreateGameResult game =
                gameService.createGame(
                        new CreateGameRequest("TestGame"),
                        reg1.authToken());

        gameService.joinGame(new JoinGameRequest("WHITE", game.gameID()), reg1.authToken());

        assertThrows(DataAccessException.class,
                () -> gameService.joinGame(
                        new JoinGameRequest("WHITE", game.gameID()), reg2.authToken()));
    }

    //CLEAR

    //positive test
    @Test
    public void clearPositive() throws Exception {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        userDAO.insertUser(new UserData("bob", "pass", "email"));

        clearService.clear();

        assertNull(userDAO.getUser("bob"));
    }
}

