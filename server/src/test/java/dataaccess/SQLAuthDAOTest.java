package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SQLAuthDAOTest {

    private SQLAuthDAO authDAO;
    private SQLUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();

        authDAO.clear();
        userDAO.clear();

        userDAO.insertUser(new UserData("alice", "password123", "alice@example.com"));
    }

    // Positive test cases
    @Test
    void insertAuth_success() throws DataAccessException {
        AuthData auth = new AuthData("token123", "alice");
        authDAO.insertAuth(auth);

        AuthData retrieved = authDAO.getAuth("token123");
        assertNotNull(retrieved, "AuthData should be retrieved");
        assertEquals("alice", retrieved.username(), "Username should match");
        assertEquals("token123", retrieved.authToken(), "Token should match");
    }

    @Test
    void deleteAuth_success() throws DataAccessException {
        AuthData auth = new AuthData("token456", "alice");
        authDAO.insertAuth(auth);

        authDAO.deleteAuth("token456");

        AuthData retrieved = authDAO.getAuth("token456");
        assertNull(retrieved, "AuthData should be deleted");
    }

    @Test
    void clearAuthTokens_success() throws DataAccessException {
        authDAO.insertAuth(new AuthData("token789", "alice"));
        authDAO.insertAuth(new AuthData("tokenABC", "alice"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token789"));
        assertNull(authDAO.getAuth("tokenABC"));
    }

    // Negative test cases
    @Test
    void insertAuth_nonExistentUser_fails() {
        AuthData auth = new AuthData("badToken", "bob"); // 'bob' not in Users table

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.insertAuth(auth);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("failed to insert auth"),
                "Should fail because username doesn't exist");
    }

    @Test
    void getAuth_nonExistentToken_returnsNull() throws DataAccessException {
        AuthData retrieved = authDAO.getAuth("noTokenHere");
        assertNull(retrieved, "Retrieving non-existent token should return null");
    }

    @Test
    void deleteAuth_nonExistentToken_noError() throws DataAccessException {
        // Should not throw exception
        authDAO.deleteAuth("ghostToken");
    }
}