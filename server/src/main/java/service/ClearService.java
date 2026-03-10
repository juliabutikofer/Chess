package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

public class ClearService {

    private final UserDAO users;
    private final AuthDAO auths;
    private final GameDAO games;

    public ClearService(UserDAO users, AuthDAO auths, GameDAO games) {
        this.users = users;
        this.auths = auths;
        this.games = games;
    }

    public void clear() {
        try {
            if (users != null) {
                users.clear();
            }
            if (auths != null) {
                auths.clear();
            }
            if (games != null) {
                games.clear();
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clear database", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error during clear", e);
        }
    }
}
