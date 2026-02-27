package dataaccess;

import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void clear() {
        games.clear();
        nextGameID = 1;
    }

    @Override
    public int insertGame(GameData game) {
        int id = nextGameID++;
        GameData stored = new GameData(
                id,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );
        games.put(id, stored);
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }
}
