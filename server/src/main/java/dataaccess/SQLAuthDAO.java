package dataaccess;

import model.AuthData;
import java.sql.*;

public class SQLAuthDAO implements AuthDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM AuthTokens";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear AuthTokens table", e);
        }
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO AuthTokens (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Inserting auth failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT token, username FROM AuthTokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("token"),
                            rs.getString("username")
                    );
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM AuthTokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth", e);
        }
    }
}



