package client;

import client.dtos.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static int serverPort;
    private static ServerFacade facade;

    @BeforeAll
    public static void initServer() {
        server = new Server();
        serverPort = server.run(0);
        System.out.println("Started test HTTP server on port " + serverPort);

        // Initialize facade once
        facade = new ServerFacade(serverPort);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void resetFacadeAuth() {
        // Logout if there's an auth token
        if (facade.getAuthToken() != null) {
            try {
                facade.logout();
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    // REGISTER
    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterResult result = facade.register("user_" + System.nanoTime(), "pass", "user@email.com");
        assertNotNull(result);
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        String username = "dupUser_" + System.nanoTime();
        facade.register(username, "pass", "dup@email.com");

        Exception ex = assertThrows(Exception.class, () -> {
            facade.register(username, "pass", "dup@email.com");
        });
        assertTrue(ex.getMessage().contains("failed"));
    }

    // LOGIN
    @Test
    public void testLoginSuccess() throws Exception {
        String username = "loginUser_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");
        LoginResult result = facade.login(username, "pass");
        assertNotNull(result);
        assertNotNull(result.authToken());
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        String username = "loginUser2_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");

        Exception ex = assertThrows(Exception.class, () -> facade.login(username, "wrongpass"));
        assertTrue(ex.getMessage().contains("failed"));
    }

    // LOGOUT
    @Test
    public void testLogoutSuccess() throws Exception {
        String username = "logoutUser_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");
        facade.logout();
        assertNull(facade.getAuthToken());
    }

    @Test
    public void testLogoutWithoutLogin() {
        ServerFacade newFacade = new ServerFacade(serverPort);
        IllegalStateException ex = assertThrows(IllegalStateException.class, newFacade::logout);
        assertTrue(ex.getMessage().contains("Not logged in"));
    }

    // CREATE GAME
    @Test
    public void testCreateGameSuccess() throws Exception {
        String username = "gameUser_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");
        facade.createGame("Test Game");
    }

    @Test
    public void testCreateGameWithoutLogin() {
        ServerFacade newFacade = new ServerFacade(serverPort);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> newFacade.createGame("NoLoginGame"));
        assertTrue(ex.getMessage().contains("Not logged in"));
    }

    // LIST GAMES
    @Test
    public void testListGamesSuccess() throws Exception {
        String username = "listUser_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");
        String gameName = "ListGame_" + System.nanoTime();
        facade.createGame(gameName);

        List<GameData> games = facade.listGames();
        assertFalse(games.isEmpty());

        boolean found = games.stream().anyMatch(g -> g.name().equals(gameName));
        assertTrue(found, "Created game not found in listGames");
    }

    @Test
    public void testListGamesWithoutLogin() {
        ServerFacade newFacade = new ServerFacade(serverPort);
        IllegalStateException ex = assertThrows(IllegalStateException.class, newFacade::listGames);
        assertTrue(ex.getMessage().contains("Not logged in"));
    }

    // JOIN GAME
    @Test
    public void testJoinGameSuccess() throws Exception {
        String creator = "joinCreator_" + System.nanoTime();
        facade.register(creator, "pass", creator + "@email.com");
        String gameName = "JoinGame_" + System.nanoTime();
        facade.createGame(gameName);

        int gameId = facade.listGames().stream()
                .filter(g -> g.name().equals(gameName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Game not found"))
                .id();

        ServerFacade player = new ServerFacade(serverPort);
        String joiner = "joinUser_" + System.nanoTime();
        player.register(joiner, "pass", joiner + "@email.com");

        player.joinGame(gameId, "white");
    }

    @Test
    public void testJoinGameWithoutLogin() throws Exception {
        String username = "joinUser2_" + System.nanoTime();
        facade.register(username, "pass", username + "@email.com");
        facade.createGame("JoinGame2");
        int gameId = facade.listGames().get(0).id();

        ServerFacade newFacade = new ServerFacade(serverPort);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> newFacade.joinGame(gameId, "white"));
        assertTrue(ex.getMessage().contains("Not logged in"));
    }

}