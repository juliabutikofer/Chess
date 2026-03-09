package dataaccess;

import model.AuthData;

public class TestOutputSQLAuthDAO {
    public static void main(String[] args) throws DataAccessException {
        SQLAuthDAO authDAO = new SQLAuthDAO();

        authDAO.clear();
        System.out.println("Auth table cleared.");

        AuthData auth = new AuthData("token123", "alice");
        authDAO.insertAuth(auth);
        System.out.println("Inserted auth token: " + auth.authToken());

        AuthData retrieved = authDAO.getAuth("token123");
        if (retrieved != null) {
            System.out.println("Retrieved auth: " + retrieved.authToken() + " for user " + retrieved.username());
        } else {
            System.out.println("Auth token not found!");
        }

        authDAO.deleteAuth("token123");
        System.out.println("Auth token deleted.");
    }
}
