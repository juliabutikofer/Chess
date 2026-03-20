package client;

import ui.PreloginUI;

public class ClientMain {
    public static void main(String[] args) {

        int port = 8080; // your server port

        ServerFacade facade = new ServerFacade(port);

        PreloginUI prelogin = new PreloginUI(facade);

        prelogin.start();
    }
}
