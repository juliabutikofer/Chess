package server;

import dto.*;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JsonMapper;
import service.GameService;
import service.UserService;
import service.ClearService;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.Map;
import java.sql.*;
import dto.RegisterRequest;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        //UserDAO userDAO = new SQLUserDAO();
        //AuthDAO authDAO = new SQLAuthDAO();
        //GameDAO gameDAO = new SQLGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);

        Gson gson = new Gson();

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new GsonMapper(gson));
        });

        // endpoints
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.delete("/db", this::clear);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private String requireAuthToken(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }
        return token;
    }

    private void handleRequest(RunnableWithException action, Context ctx) {
        try {
            action.run();
        } catch (DataAccessException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    // handlers
    private void listGames(Context ctx) {
        handleRequest(() -> {
            String token = requireAuthToken(ctx);
            ListGamesResult result = gameService.listGames(token);
            ctx.status(200).json(result);
        }, ctx);
    }

    private void createGame(Context ctx) {
        handleRequest(() -> {
            String token = requireAuthToken(ctx);
            CreateGameRequest req = ctx.bodyAsClass(CreateGameRequest.class);

            if (req == null || req.gameName() == null || req.gameName().isBlank()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            CreateGameResult result = gameService.createGame(req, token);
            ctx.status(200).json(result);
        }, ctx);
    }

    private void joinGame(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            JoinGameRequest req = ctx.bodyAsClass(JoinGameRequest.class);

            gameService.joinGame(req, token);
            ctx.status(200).json(Map.of());

        } catch (DataAccessException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("bad request")) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
            } else if (msg.contains("taken")) {
                ctx.status(403).json(Map.of("message", "Error: already taken"));
            } else {
                ctx.status(401).json(Map.of("message", "Error: unauthorized"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private void register(Context ctx) {
        try {
            RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

            if (req.username() == null || req.password() == null || req.email() == null) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            RegisterResult result = userService.register(req);
            ctx.status(200).json(result);

        } catch (DataAccessException e) {
            if (e.getMessage().toLowerCase().contains("already taken")) {
                ctx.status(403).json(Map.of("message", "Error: already taken"));
            } else {
                ctx.status(401).json(Map.of("message", "Error: unauthorized"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private void login(Context ctx) {
        handleRequest(() -> {
            LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

            if (req.username() == null || req.password() == null) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            LoginResult result = userService.login(req);
            ctx.status(200).json(result);
        }, ctx);
    }

    private void logout(Context ctx) {
        handleRequest(() -> {
            String token = requireAuthToken(ctx);
            LogoutRequest req = new LogoutRequest(token);
            userService.logout(req);
            ctx.status(200).json(Map.of());
        }, ctx);
    }

    private void clear(Context ctx) {
        handleRequest(() -> {
            clearService.clear();
            ctx.status(200).json(Map.of());
        }, ctx);
    }

    public static class GsonMapper implements JsonMapper {

        private final Gson gson;

        public GsonMapper(Gson gson) {
            this.gson = gson;
        }

        @Override
        public <T> T fromJsonString(String json, Type targetType) {
            return gson.fromJson(json, targetType);
        }

        @Override
        public String toJsonString(Object obj, Type type) {
            return gson.toJson(obj);
        }
    }
}