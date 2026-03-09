package dataaccess;

import model.UserData;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO implements UserDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM Users";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear Users table", e);
        }
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO Users (username, passwordHash, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.username());
            //hash the password
            stmt.setString(2, user.password());

            stmt.setString(3, user.email());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Inserting user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    // Optional: store or use the generated ID
                } else {
                    throw new DataAccessException("Inserting user failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT id, username, passwordHash, email FROM Users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("passwordHash"), // hashed password
                            rs.getString("email")
                    );
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }
}