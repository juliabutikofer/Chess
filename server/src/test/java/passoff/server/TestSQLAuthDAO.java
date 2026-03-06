package dataaccess;

import model.AuthData;

public class TestSQLAuthDAO {
    public static void main(String[] args) throws DataAccessException {
        SQLAuthDAO authDAO = new SQLAuthDAO();

        // Clear auth table
        authDAO.clear();
        System.out.println("Auth table cleared.");

        // Insert an auth token
        AuthData auth = new AuthData("token123", "alice");
        authDAO.insertAuth(auth);
        System.out.println("Inserted auth token: " + auth.authToken());

        // Retrieve the auth token
        AuthData retrieved = authDAO.getAuth("token123");
        if (retrieved != null) {
            System.out.println("Retrieved auth: " + retrieved.authToken() + " for user " + retrieved.username());
        } else {
            System.out.println("Auth token not found!");
        }

        // Delete the auth token
        authDAO.deleteAuth("token123");
        System.out.println("Auth token deleted.");
    }
}
