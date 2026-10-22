package edu.assignment3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MstAlgorithms {
    private MstAlgorithms() {}

    public record Result(List<Graph.Edge> edges, long totalWeight, int components) {}

    public static Result kruskal(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        ArrayList<Graph.Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            metrics.comparisons++;
            int byWeight = Integer.compare(a.weight(), b.weight());
            if (byWeight != 0) {
                return byWeight;
            }
            int byFrom = Integer.compare(a.from(), b.from());
            return byFrom != 0 ? byFrom : Integer.compare(a.to(), b.to());
        });
        DisjointSet sets = new DisjointSet(graph.vertexCount(), metrics);
        ArrayList<Graph.Edge> tree = new ArrayList<>();
        long total = 0;
        for (Graph.Edge edge : sorted) {
            metrics.edgeScans++;
            if (sets.union(edge.from(), edge.to())) {
                tree.add(edge);
                total += edge.weight();
                if (sets.components() <= 1) {
                    break;
                }
            }
        }
        return new Result(List.copyOf(tree), total, sets.components());
    }

    private record Candidate(int from, int to, int weight) {}

    public static Result prim(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        int n = graph.vertexCount();
        boolean[] inTree = new boolean[n];
        Comparator<Candidate> byWeight = (a, b) -> {
            metrics.comparisons++;
            int byEdgeWeight = Integer.compare(a.weight(), b.weight());
            if (byEdgeWeight != 0) {
                return byEdgeWeight;
            }
            int byFrom = Integer.compare(a.from(), b.from());
            return byFrom != 0 ? byFrom : Integer.compare(a.to(), b.to());
        };
        PriorityQueue<Candidate> queue = new PriorityQueue<>(byWeight);
        ArrayList<Graph.Edge> forest = new ArrayList<>();
        long total = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            addOutgoing(graph, start, inTree, queue, metrics);
            while (!queue.isEmpty()) {
                Candidate edge = queue.remove();
                if (inTree[edge.to()]) {
                    continue;
                }
                inTree[edge.to()] = true;
                forest.add(new Graph.Edge(edge.from(), edge.to(), edge.weight()));
                total += edge.weight();
                addOutgoing(graph, edge.to(), inTree, queue, metrics);
            }
        }
        return new Result(List.copyOf(forest), total, components);
    }

    private static void addOutgoing(Graph graph, int vertex, boolean[] inTree,
                                    PriorityQueue<Candidate> queue, Metrics metrics) {
        for (Graph.Arc arc : graph.neighbors(vertex)) {
            metrics.edgeScans++;
            if (!inTree[arc.to()]) queue.add(new Candidate(vertex, arc.to(), arc.weight()));
        }
    }

    private static void requireUndirected(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("MST requires an undirected graph");
        }
    }
}
