package edu.assignment3;

import java.util.ArrayList;
import java.util.Arrays;

public final class KosarajuScc {
    private KosarajuScc() {}

    public record Result(int count, int[] componentOf) {}

    public static Result compute(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC expects a directed graph");
        }

        int n = graph.vertexCount();
        boolean[] seen = new boolean[n];
        int[] stackVertex = new int[n];
        int[] nextNeighbor = new int[n];
        ArrayList<Integer> finishOrder = new ArrayList<>(n);

        for (int root = 0; root < n; root++) {
            if (seen[root]) {
                continue;
            }
            int top = 0;
            stackVertex[0] = root;
            nextNeighbor[0] = 0;
            seen[root] = true;
            metrics.observeDepth(1);
            while (top >= 0) {
                int vertex = stackVertex[top];
                if (nextNeighbor[top] < graph.neighbors(vertex).size()) {
                    Graph.Arc arc = graph.neighbors(vertex).get(nextNeighbor[top]++);
                    metrics.edgeScans++;
                    if (!seen[arc.to()]) {
                        seen[arc.to()] = true;
                        top++;
                        stackVertex[top] = arc.to();
                        nextNeighbor[top] = 0;
                        metrics.observeDepth(top + 1);
                    }
                } else {
                    finishOrder.add(vertex);
                    top--;
                }
            }
        }

        Arrays.fill(seen, false);
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        int[] stack = new int[n];
        int componentCount = 0;
        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int root = finishOrder.get(i);
            if (seen[root]) {
                continue;
            }
            int top = 0;
            stack[0] = root;
            seen[root] = true;
            componentOf[root] = componentCount;
            metrics.observeDepth(1);
            while (top >= 0) {
                int vertex = stack[top--];
                for (Graph.Arc arc : graph.reverseNeighbors(vertex)) {
                    metrics.edgeScans++;
                    if (!seen[arc.to()]) {
                        seen[arc.to()] = true;
                        componentOf[arc.to()] = componentCount;
                        stack[++top] = arc.to();
                        metrics.observeDepth(top + 1);
                    }
                }
            }
            componentCount++;
        }
        return new Result(componentCount, componentOf);
    }
}
