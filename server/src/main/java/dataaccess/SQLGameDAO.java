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

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Inserting game failed, no rows affected.");
            }

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
                    return new GameData(
                            rs.getInt("id"),
                            rs.getString("whitePlayer"),
                            rs.getString("blackPlayer"),
                            rs.getString("gameName"),
                            gson.fromJson(rs.getString("gameState"), ChessGame.class)
                    );
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT id, whitePlayer, blackPlayer, gameName, gameState FROM Games";
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("whitePlayer"),
                        rs.getString("blackPlayer"),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("gameState"), ChessGame.class)
                ));
            }
            return games;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
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