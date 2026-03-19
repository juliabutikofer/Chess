//package client;
//
//import chess.*;
//
//public class ClientMain {
//    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
//    }
//}
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
