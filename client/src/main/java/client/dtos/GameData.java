package client.dtos;

import java.util.List;

public record GameData(int gameId, String gameName, List<String> players) {}