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
}