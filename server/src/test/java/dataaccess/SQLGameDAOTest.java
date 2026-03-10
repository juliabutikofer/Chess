package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    // Positive Tests

    @Test
    void insertAndRetrieveGame() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData game = new GameData(0, "alice", "bob", "Epic Match", chess);

        int gameID = gameDAO.insertGame(game);
        assertTrue(gameID > 0);

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("Epic Match", retrieved.gameName());
        assertEquals("alice", retrieved.whiteUsername());
        assertEquals("bob", retrieved.blackUsername());
    }

    @Test
    void listGames() throws DataAccessException {
        gameDAO.insertGame(new GameData(0, "alice", "bob", "G1", new ChessGame()));
        gameDAO.insertGame(new GameData(0, "carol", "dave", "G2", new ChessGame()));

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void updateGame() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData game = new GameData(0, "alice", "bob", "Match", chess);
        int gameID = gameDAO.insertGame(game);

        game = new GameData(gameID, "alice", "bob", "Updated Match", chess);
        gameDAO.updateGame(game);

        GameData updated = gameDAO.getGame(gameID);
        assertEquals("Updated Match", updated.gameName());
    }

    @Test
    void clearGames() throws DataAccessException {
        gameDAO.insertGame(new GameData(0, "alice", "bob", "G1", new ChessGame()));
        gameDAO.clear();
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    // Negative Tests

    @Test
    void getNonExistentGame() throws DataAccessException {
        GameData game = gameDAO.getGame(9999);
        assertNull(game);
    }

    @Test
    void updateNonExistentGame() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData ghostGame = new GameData(9999, "alice", "bob", "Ghost", chess);

        gameDAO.updateGame(ghostGame);

        GameData retrieved = gameDAO.getGame(9999);
        assertNull(retrieved, "Updating a non-existent game should not create it");
    }
}
