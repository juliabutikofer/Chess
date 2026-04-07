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
import websockethandler.WebSocketHandler;
import websocket.commands.UserGameCommand;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private final WebSocketHandler wsHandler;
    private final Gson gson = new Gson();

    public Server() {
        UserDAO userDAO = new SQLUserDAO();
        AuthDAO authDAO = new SQLAuthDAO();
        GameDAO gameDAO = new SQLGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        wsHandler = new WebSocketHandler(gameService);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new GsonMapper(gson));

            config.jetty.modifyWebSocketServletFactory(factory -> {
                factory.setIdleTimeout(Duration.ofMinutes(15));
            });
        });

        // HTTP endpoints
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.delete("/db", this::clear);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.put("/game/observe", this::observeGame);

        // WebSocket endpoint
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> System.out.println("[WS] Connected: " + ctx.sessionId()));
            ws.onClose(ctx -> wsHandler.removeClient(ctx));
            ws.onError(ctx -> System.out.println("[WS ERROR] " + ctx.error()));
            ws.onMessage(ctx -> {
                try {
                    UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);
                    wsHandler.handleCommand(cmd, ctx);
                } catch (Exception e) {
                    System.out.println("[WS ERROR] " + e.getMessage());
                }
            });
        });
    }

    public int run(int desiredPort) {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeTables();
        } catch (DataAccessException e) {
            e.printStackTrace();
            return -1;
        }
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() { javalin.stop(); }

    private String requireAuthToken(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        if (token == null) throw new DataAccessException("unauthorized");
        return token;
    }

    private void register(Context ctx) {
        try {
            RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

            if (req.username() == null || req.username().isEmpty() ||
                    req.password() == null || req.password().isEmpty() ||
                    req.email() == null || req.email().isEmpty()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            ctx.status(200).json(userService.register(req));

        } catch (DataAccessException e) {
            handleDataAccessException(e, ctx);
        } catch (Exception e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    private void login(Context ctx) {
        try {
            LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

            if (req.username() == null || req.password() == null) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            ctx.status(200).json(userService.login(req));

        } catch (DataAccessException e) {
            handleDataAccessException(e, ctx);
        } catch (Exception e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    private void logout(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            userService.logout(new LogoutRequest(token));
            ctx.status(200).json(Map.of());
        } catch (DataAccessException e) { handleDataAccessException(e, ctx); }
    }

    private void listGames(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            ctx.status(200).json(gameService.listGames(token));
        } catch (DataAccessException e) { handleDataAccessException(e, ctx); }
    }

    private void createGame(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            CreateGameRequest req = ctx.bodyAsClass(CreateGameRequest.class);
            ctx.status(200).json(gameService.createGame(req, token));
        } catch (DataAccessException e) { handleDataAccessException(e, ctx); }
    }

    private void joinGame(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            JoinGameRequest req = ctx.bodyAsClass(JoinGameRequest.class);
            gameService.joinGame(req, token);
            ctx.status(200).json(Map.of());
        } catch (DataAccessException e) {
            handleDataAccessException(e, ctx);
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR IN JOIN: " + e.getMessage());
            e.printStackTrace();
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    private void observeGame(Context ctx) {
        try {
            String token = requireAuthToken(ctx);
            ObserveGameRequest req = ctx.bodyAsClass(ObserveGameRequest.class);
            gameService.observeGame(req, token);
            ctx.status(200).json(Map.of());
        } catch (DataAccessException e) { handleDataAccessException(e, ctx); }
    }

    private void clear(Context ctx) {
        try {
            clearService.clear();

            wsHandler.removeClient(null);

            ctx.status(200).json(Map.of());
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private void handleDataAccessException(DataAccessException e, Context ctx) {
        String msg = e.getMessage(); // Removed .toLowerCase() for a more precise check

        if (msg.contains("bad request")) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
        else if (msg.equals("already taken")) {
            ctx.status(403).json(Map.of("message", "Error: already taken"));
        }
        else if (msg.contains("unauthorized")) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        }
        else {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public static class GsonMapper implements JsonMapper {
        private final Gson gson;
        public GsonMapper(Gson gson) { this.gson = gson; }
        @Override public <T> T fromJsonString(String json, Type targetType) { return gson.fromJson(json, targetType); }
        @Override public String toJsonString(Object obj, Type type) { return gson.toJson(obj); }
    }
}