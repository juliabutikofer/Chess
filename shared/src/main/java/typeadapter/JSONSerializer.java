package typeadapter;

import chess.*;
import com.google.gson.*;

public class JSONSerializer {

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

        builder.registerTypeAdapter(ChessPiece.class, (JsonDeserializer<ChessPiece>) (el, type, ctx) -> {
            JsonObject obj;
            try {
                obj = el.getAsJsonObject();
            } catch (Exception e) {
                return null;
            }

            JsonElement colorElem = obj.has("pieceColor") ? obj.get("pieceColor") : obj.get("teamColor");
            JsonElement typeElem = obj.has("type") ? obj.get("type") : obj.get("pieceType");

            if (colorElem == null || typeElem == null) {
                return null;
            }

            try {
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
            return new ChessPosition(o.get("row").getAsInt(), o.get("col").getAsInt());
        });

        builder.registerTypeAdapter(ChessBoard.class, (JsonDeserializer<ChessBoard>) (el, t, ctx) -> {
            ChessBoard board = new ChessBoard();
            JsonObject obj = el.getAsJsonObject();

            if (!obj.has("squares")) {
                return board;
            }

            JsonArray rows = obj.getAsJsonArray("squares");
            int rowCount = Math.min(rows.size(), 8);

            for (int r = 0; r < rowCount; r++) {
                JsonArray row = rows.get(r).getAsJsonArray();
                int colCount = Math.min(row.size(), 8);

                for (int c = 0; c < colCount; c++) {
                    JsonElement cell = row.get(c);
                    if (cell.isJsonNull()) {
                        continue;
                    }

                    ChessPiece piece = ctx.deserialize(cell, ChessPiece.class);
                    if (piece != null) {
                        board.addPiece(new ChessPosition(r + 1, c + 1), piece);
                    }
                }
            }

            return board;
        });

        return builder.create();
    }
}
