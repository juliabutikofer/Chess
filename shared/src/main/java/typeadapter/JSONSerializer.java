package typeadapter;

import chess.ChessPiece;
import chess.ChessGame;
import com.google.gson.*;
import java.lang.reflect.Type;

public class JSONSerializer {
    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(ChessPiece.class, new JsonDeserializer<ChessPiece>() {
            @Override
            public ChessPiece deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
                try {
                    JsonObject obj = el.getAsJsonObject();

                    JsonElement colorElem = obj.get("pieceColor");
                    if (colorElem == null) colorElem = obj.get("teamColor");

                    JsonElement typeElem = obj.get("type");
                    if (typeElem == null) typeElem = obj.get("pieceType");

                    if (colorElem == null || typeElem == null) {
                        System.out.println("[DEBUG] JSON Piece missing fields! Found: " + obj);
                        return null;
                    }

                    ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(colorElem.getAsString().toUpperCase());
                    ChessPiece.PieceType pieceType = ChessPiece.PieceType.valueOf(typeElem.getAsString().toUpperCase());

                    return new ChessPiece(teamColor, pieceType);

                } catch (Exception e) {
                    System.out.println("[DEBUG] Failed to deserialize ChessPiece: " + e.getMessage());
                    return null;
                }
            }
        });

        return builder.create();
    }
}