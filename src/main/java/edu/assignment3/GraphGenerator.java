package edu.assignment3;

import java.util.Random;

public final class GraphGenerator {
    private GraphGenerator() {}

    public static Graph undirectedForMst(int n, int m) {
        if (n <= 0 || m < n - 1) {
            throw new IllegalArgumentException("MST graph must have n > 0 and m >= n - 1");
        }

        Random random = new Random(42);
        int[] permutation = shuffledVertices(n, random);
        Graph graph = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            int from = permutation[random.nextInt(i)];
            int to = permutation[i];
            graph.addEdge(from, to, nextWeight(random));
        }

        while (graph.edgeCount() < m) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);
            while (to == from) {
                to = random.nextInt(n);
            }
            graph.addEdge(from, to, nextWeight(random));
        }
        return graph;
    }

    public static Graph directedForScc(int n, int m) {
        Random random = new Random(42);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);
            while (to == from) {
                to = random.nextInt(n);
            }
            graph.addEdge(from, to, nextWeight(random));
        }
        return graph;
    }

    public static Graph dag(int n, int m) {
        if (n <= 0) {
            throw new IllegalArgumentException("DAG must have n > 0");
        }
        Random random = new Random(42);
        int[] rank = shuffledVertices(n, random);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);
            while (from == to) {
                to = random.nextInt(n);
            }
            if (rank[from] > rank[to]) {
                int swap = from;
                from = to;
                to = swap;
            }
            graph.addEdge(from, to, nextWeight(random));
        }
        return graph;
    }

    private static int[] shuffledVertices(int n, Random random) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
        return values;
    }

    private static int nextWeight(Random random) {
        return 1 + random.nextInt(1000);
    }
}
