package edu.assignment3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GraphIO {
    private GraphIO() {}

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        ArrayList<String> tokens = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            for (String token : trimmed.split("\\s+")) {
                tokens.add(token);
            }
        }

        if (tokens.size() < 3) {
            throw new IllegalArgumentException("missing graph header in " + path);
        }
        int n = Integer.parseInt(tokens.get(0));
        int m = Integer.parseInt(tokens.get(1));
        String mode = tokens.get(2).toLowerCase();
        boolean directed = switch (mode) {
            case "directed" -> true;
            case "undirected" -> false;
            default -> throw new IllegalArgumentException(
                    "expected directed or undirected, got " + mode);
        };
        if (tokens.size() != 3 + 3 * m) {
            throw new IllegalArgumentException("expected exactly " + m + " edges in " + path);
        }

        Graph graph = new Graph(n, directed);
        for (int i = 0; i < m; i++) {
            int offset = 3 + 3 * i;
            graph.addEdge(
                    Integer.parseInt(tokens.get(offset)),
                    Integer.parseInt(tokens.get(offset + 1)),
                    Integer.parseInt(tokens.get(offset + 2)));
        }
        return graph;
    }
}
