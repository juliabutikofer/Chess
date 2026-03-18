package client.dtos;

import java.util.List;

public record GameData(int id, String name, List<String> players) {}