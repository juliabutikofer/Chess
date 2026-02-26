package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable ex) {
        super(message, ex);
    }
}


//create subclasses that represent more specific errors
//clear, create user, get user, create game, get game, list game, update game, create auth, get, auth, delete auth
