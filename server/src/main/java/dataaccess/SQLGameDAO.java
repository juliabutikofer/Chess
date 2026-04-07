package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        String sqlGames = "DELETE FROM Games";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlGames);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear tables", e);
        }
    }

    @Override
    public int insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO Games (whitePlayer, blackPlayer, gameName, gameState) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new DataAccessException("Inserting game failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT id, whitePlayer, blackPlayer, gameName, gameState FROM Games WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String white = rs.getString("whitePlayer");
                    String black = rs.getString("blackPlayer");

                    return new GameData(
                            rs.getInt("id"),
                            (white == null || white.isBlank()) ? null : white.trim(),
                            (black == null || black.isBlank()) ? null : black.trim(),
                            rs.getString("gameName"),
                            gson.fromJson(rs.getString("gameState"), ChessGame.class)
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT id, whitePlayer, blackPlayer, gameName, gameState FROM Games";
        List<GameData> gamesList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String white = rs.getString("whitePlayer");
                String black = rs.getString("blackPlayer");

                gamesList.add(new GameData(
                        rs.getInt("id"),
                        (white == null || white.isBlank()) ? null : white.trim(),
                        (black == null || black.isBlank()) ? null : black.trim(),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("gameState"), ChessGame.class)
                ));
            }
            return gamesList;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // FIX: Ensure whitePlayer and blackPlayer are in the UPDATE statement!
        String sql = "UPDATE Games SET whitePlayer = ?, blackPlayer = ?, gameName = ?, gameState = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.setInt(5, game.gameID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }
}