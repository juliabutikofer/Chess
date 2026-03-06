package dataaccess;

import model.UserData;

public class TestSQLUserDAO {
    public static void main(String[] args) throws DataAccessException {
        SQLUserDAO userDAO = new SQLUserDAO();

        // Clear existing users
        userDAO.clear();
        System.out.println("Users table cleared.");

        // Insert a new user
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.insertUser(user);
        System.out.println("Inserted user: " + user.username());

        // Retrieve the user
        UserData retrieved = userDAO.getUser("alice");
        if (retrieved != null) {
            System.out.println("Retrieved user: " + retrieved.username() + ", " + retrieved.email());
        } else {
            System.out.println("User not found!");
        }
    }
}
