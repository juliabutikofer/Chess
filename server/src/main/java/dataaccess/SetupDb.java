package dataaccess;

public class SetupDb {
    public static void main(String[] args) {
        try {
            // Make sure database exists
            DatabaseManager.createDatabase();

            // Drop and recreate all tables with correct schema
            DatabaseManager.initializeTables();

            System.out.println("Database and tables are ready!");
        } catch (DataAccessException e) {
            System.err.println("Setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
