package edu.assignment3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    public record Arc(int to, int weight) {}

    public record Edge(int from, int to, int weight) {}

    private final int vertexCount;
    private final boolean directed;
    private final ArrayList<Arc>[] adjacency;
    private final ArrayList<Arc>[] reverseAdjacency;
    private final ArrayList<Edge> edges;
    private final int[] inDegree;

    @SuppressWarnings("unchecked")
    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount must be non-negative");
        }

        this.vertexCount = vertexCount;
        this.directed = directed;
        adjacency = (ArrayList<Arc>[]) new ArrayList<?>[vertexCount];
        reverseAdjacency = directed ? (ArrayList<Arc>[]) new ArrayList<?>[vertexCount] : null;
        for (int i = 0; i < vertexCount; i++) {
            adjacency[i] = new ArrayList<>();
            if (directed) reverseAdjacency[i] = new ArrayList<>();
        }
        edges = new ArrayList<>();
        inDegree = new int[vertexCount];
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        adjacency[from].add(new Arc(to, weight));
        edges.add(new Edge(from, to, weight));
        if (directed) {
            reverseAdjacency[to].add(new Arc(from, weight));
            inDegree[to]++;
        } else {
            adjacency[to].add(new Arc(from, weight));
        }
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "vertex " + vertex + " outside 0.." + (vertexCount - 1));
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Arc> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency[vertex]);
    }

    public List<Arc> reverseNeighbors(int vertex) {
        if (!directed) {
            throw new IllegalStateException("reverse adjacency is available for directed graphs");
        }
        checkVertex(vertex);
        return Collections.unmodifiableList(reverseAdjacency[vertex]);
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int inDegree(int vertex) {
        checkVertex(vertex);
        return inDegree[vertex];
    }

    public int[] copyInDegrees() {
        return inDegree.clone();
    }
}
