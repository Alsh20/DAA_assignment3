package edu.assignment3;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class TopologicalSort {
    private TopologicalSort() {}

    public record Result(boolean acyclic, int[] order) {}

    public static Result kahn(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("topological sorting expects a directed graph");
        }

        int n = graph.vertexCount();
        int[] remainingInDegree = graph.copyInDegrees();
        ArrayDeque<Integer> ready = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (remainingInDegree[v] == 0) {
                ready.addLast(v);
            }
        }

        int[] order = new int[n];
        int used = 0;
        while (!ready.isEmpty()) {
            int vertex = ready.removeFirst();
            order[used++] = vertex;
            for (Graph.Arc arc : graph.neighbors(vertex)) {
                metrics.edgeScans++;
                if (--remainingInDegree[arc.to()] == 0) ready.addLast(arc.to());
            }
        }
        return new Result(used == n, used == n ? order : Arrays.copyOf(order, used));
    }

    public static boolean isValidOrder(Graph graph, int[] order) {
        if (order.length != graph.vertexCount()) {
            return false;
        }

        int[] position = new int[order.length];
        Arrays.fill(position, -1);
        for (int i = 0; i < order.length; i++) {
            int v = order[i];
            if (v < 0 || v >= order.length || position[v] != -1) {
                return false;
            }
            position[v] = i;
        }

        for (Graph.Edge edge : graph.edges()) {
            if (position[edge.from()] >= position[edge.to()]) {
                return false;
            }
        }
        return true;
    }
}
