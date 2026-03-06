package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {

    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    static {
        loadPropertiesFromResources();
    }

    public static void createDatabase() throws DataAccessException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (Connection conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Database ensured: " + databaseName);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create database", ex);
        }
    }

    public static void initializeTables() throws DataAccessException {
        try (Connection conn = DriverManager.getConnection(connectionUrl + "/" + databaseName, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {

            // Create Users table if it doesn't exist
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                passwordHash VARCHAR(255) NOT NULL,
                email VARCHAR(100)
            );
        """);

            // Create Games table if it doesn't exist
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Games (
                id INT AUTO_INCREMENT PRIMARY KEY,
                whitePlayer VARCHAR(50),
                blackPlayer VARCHAR(50),
                gameName VARCHAR(100),
                gameState TEXT,
                status VARCHAR(20),
                createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """);

            // Create AuthTokens table if it doesn't exist
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS AuthTokens (
                token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE
            );
        """);

            System.out.println("Tables initialized successfully!");

        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize tables", e);
        }
    }

    static Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(connectionUrl + "/" + databaseName, dbUsername, dbPassword);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) throw new Exception("Unable to load db.properties");
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");
        String host = props.getProperty("db.host");
        int port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}