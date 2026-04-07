package typeadapter;

import chess.*;
import com.google.gson.*;
import java.lang.reflect.Type;

public class JSONSerializer {

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

        builder.registerTypeAdapter(ChessPiece.class, (JsonDeserializer<ChessPiece>) (el, type, ctx) -> {
            try {
                JsonObject obj = el.getAsJsonObject();
                JsonElement colorElem = obj.has("pieceColor") ? obj.get("pieceColor") : obj.get("teamColor");
                JsonElement typeElem = obj.has("type") ? obj.get("type") : obj.get("pieceType");

                if (colorElem == null || typeElem == null) return null;

                ChessGame.TeamColor teamColor =
                        ChessGame.TeamColor.valueOf(colorElem.getAsString().toUpperCase());
                ChessPiece.PieceType pieceType =
                        ChessPiece.PieceType.valueOf(typeElem.getAsString().toUpperCase());

                return new ChessPiece(teamColor, pieceType);
            } catch (Exception e) {
                return null;
            }
        });

        builder.registerTypeAdapter(ChessPosition.class, (JsonDeserializer<ChessPosition>) (el, t, ctx) -> {
            JsonObject o = el.getAsJsonObject();
            int row = o.get("row").getAsInt();
            int col = o.get("col").getAsInt();
            return new ChessPosition(row, col);
        });

        builder.registerTypeAdapter(ChessBoard.class, (JsonDeserializer<ChessBoard>) (el, t, ctx) -> {
            ChessBoard board = new ChessBoard();
            JsonObject obj = el.getAsJsonObject();

            if (obj.has("squares")) {
                JsonArray rows = obj.getAsJsonArray("squares");
                for (int r = 0; r < rows.size() && r < 8; r++) {
                    JsonArray row = rows.get(r).getAsJsonArray();
                    for (int c = 0; c < row.size() && c < 8; c++) {
                        JsonElement cell = row.get(c);
                        if (!cell.isJsonNull()) {
                            ChessPiece piece = ctx.deserialize(cell, ChessPiece.class);
                            if (piece != null) {
                                board.addPiece(new ChessPosition(r + 1, c + 1), piece);
                            }
                        }
                    }
                }
            }
            return board;
        });

        return builder.create();
    }
}
