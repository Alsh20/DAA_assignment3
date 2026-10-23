package edu.assignment3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPaths {
    private static final long INF = Long.MAX_VALUE / 4;
    private DagShortestPaths() {}

    public record Result(long[] distances, int[] parent) {
        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= parent.length) {
                throw new IndexOutOfBoundsException("target " + target);
            }
            if (distances[target] >= INF) {
                return List.of();
            }

            ArrayList<Integer> path = new ArrayList<>();
            int current = target;
            while (current != -1) {
                path.add(current);
                current = parent[current];
            }
            Collections.reverse(path);
            return List.copyOf(path);
        }

        public long sumOfDistances() {
            long sum = 0;
            for (long distance : distances) {
                if (distance >= INF) {
                    throw new IllegalStateException(
                            "DAG vertex is unreachable from every source");
                }
                sum += distance;
            }
            return sum;
        }
    }

    public static Result compute(Graph graph, int[] topologicalOrder, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("DAG shortest paths expect a directed graph");
        }
        if (!isPermutation(topologicalOrder, graph.vertexCount())) {
            throw new IllegalArgumentException("a topological-order permutation is required");
        }

        long[] distance = new long[graph.vertexCount()];
        Arrays.fill(distance, INF);
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        for (int v = 0; v < graph.vertexCount(); v++) {
            if (graph.inDegree(v) == 0) {
                distance[v] = 0;
            }
        }

        for (int vertex : topologicalOrder) {
            for (Graph.Arc arc : graph.neighbors(vertex)) {
                metrics.edgeScans++;
                if (distance[vertex] >= INF) {
                    continue;
                }
                long candidate = distance[vertex] + arc.weight();
                metrics.comparisons++;
                if (candidate < distance[arc.to()]) {
                    distance[arc.to()] = candidate;
                    parent[arc.to()] = vertex;
                }
            }
        }
        return new Result(distance, parent);
    }

    private static boolean isPermutation(int[] order, int vertexCount) {
        if (order.length != vertexCount) {
            return false;
        }
        boolean[] seen = new boolean[vertexCount];
        for (int vertex : order) {
            if (vertex < 0 || vertex >= vertexCount || seen[vertex]) {
                return false;
            }
            seen[vertex] = true;
        }
        return true;
    }
}
