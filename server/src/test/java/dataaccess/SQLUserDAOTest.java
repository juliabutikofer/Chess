package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    // Positive Tests

    @Test
    void insertUser() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.insertUser(user);

        UserData retrieved = userDAO.getUser("alice");
        assertNotNull(retrieved, "User should be inserted and retrievable");
        assertEquals("alice", retrieved.username());
    }

    @Test
    void getUser() throws DataAccessException {
        UserData user = new UserData("carol", "pwd", "carol@example.com");
        userDAO.insertUser(user);

        UserData retrieved = userDAO.getUser("carol");
        assertNotNull(retrieved, "User should be found");
        assertEquals("carol", retrieved.username());
    }

    @Test
    void clearUsers() throws DataAccessException {
        userDAO.insertUser(new UserData("bob", "pwd", "bob@example.com"));
        userDAO.clear();
        assertNull(userDAO.getUser("bob"), "User table should be empty after clear");
    }

    // Negative Tests

    @Test
    void insertDuplicateUser() throws DataAccessException {
        userDAO.insertUser(new UserData("alice", "password123", "alice@example.com"));

        UserData duplicate = new UserData("alice", "password456", "alice2@example.com");
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userDAO.insertUser(duplicate));

        assertTrue(ex.getMessage().contains("Failed to insert user"),
                "Duplicate username should cause insert to fail");
    }

    @Test
    void getNonExistentUser() throws DataAccessException {
        UserData user = userDAO.getUser("nonexistent");
        assertNull(user, "Non-existent user should return null");
    }

    @Test
    void clearOnEmpty() throws DataAccessException {
        userDAO.clear(); // should not throw
        assertNull(userDAO.getUser("anything"),
                "Clearing an empty table should still leave it empty");
    }
}
