package server;

import DTO.RegisterRequest;
import io.javalin.*;
import model.AuthData;
import dataaccess.MemoryDataAccess;

public class Server {

    private final Javalin javalin;
    private final ServerFacade facade;

    public Server() {
        facade = new ServerFacade(new MemoryDataAccess());
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/register", ctx -> {
            var req = ctx.bodyAsClass(RegisterRequest.class);
            AuthData auth = facade.registerUser(req.username(), req.password(), req.email());
            ctx.json(auth);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}