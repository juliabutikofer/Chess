package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            // Create the database if it doesn't exist
            DatabaseManager.createDatabase();

            // Initialize tables
            DatabaseManager.initializeTables();

            // Try to get a connection
            Connection conn = null;
            try {
                conn = DatabaseManager.getConnection();
                System.out.println("Connection successful!");
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }

        } catch (DataAccessException | SQLException e) {
            e.printStackTrace();
        }
    }
}