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
    void insertAndRetrieveUser_success() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.insertUser(user);

        UserData retrieved = userDAO.getUser("alice");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
        assertEquals("alice@example.com", retrieved.email());
        assertNotEquals("password123", retrieved.password()); // password is hashed
    }

    @Test
    void clearUsers_success() throws DataAccessException {
        UserData user = new UserData("bob", "pwd", "bob@example.com");
        userDAO.insertUser(user);

        userDAO.clear();
        assertNull(userDAO.getUser("bob"));
    }

    // Negative Tests
    @Test
    void insertDuplicateUser_shouldThrowException() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.insertUser(user);

        UserData duplicate = new UserData("alice", "password456", "alice2@example.com");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userDAO.insertUser(duplicate));
        assertTrue(ex.getMessage().contains("Failed to insert user"));
    }

    @Test
    void getNonExistentUser_shouldReturnNull() throws DataAccessException {
        UserData user = userDAO.getUser("nonexistent");
        assertNull(user);
    }
}