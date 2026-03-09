package dataaccess;

import model.GameData;
import chess.ChessGame;
import java.util.List;

public class TestOutputSQLGameDAO {
    public static void main(String[] args) throws DataAccessException {
        SQLGameDAO gameDAO = new SQLGameDAO();

        // Clear games table
        gameDAO.clear();
        System.out.println("Games table cleared.");

        // Create a dummy ChessGame object
        ChessGame chess = new ChessGame(); // make sure ChessGame has a default constructor

        // Insert a game
        GameData game = new GameData(0, "alice", "bob", "Epic Match", chess);
        int gameID = gameDAO.insertGame(game);
        System.out.println("Inserted game with ID: " + gameID);

        // Retrieve the game
        GameData retrieved = gameDAO.getGame(gameID);
        System.out.println("Retrieved game: " + retrieved.gameName() + " between " + retrieved.whiteUsername() + " and " + retrieved.blackUsername());

        // List all games
        List<GameData> games = gameDAO.listGames();
        System.out.println("Number of games in DB: " + games.size());
    }
}