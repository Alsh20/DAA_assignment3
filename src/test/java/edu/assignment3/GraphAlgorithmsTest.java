package edu.assignment3;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GraphAlgorithmsTest {
    @Test
    void dsuMatchesReferencePartitionAfterRandomUnions() {
        int n = 150;
        int[] reference = new int[n];
        for (int i = 0; i < n; i++) {
            reference[i] = i;
        }

        DisjointSet dsu = new DisjointSet(n, new Metrics());
        Random random = new Random(91);
        for (int i = 0; i < 2_000; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            boolean expectedMerge = reference[a] != reference[b];
            String old = Integer.toString(reference[b]);
            if (expectedMerge) {
                int from = reference[b];
                int to = reference[a];
                for (int j = 0; j < n; j++) {
                    if (reference[j] == from) {
                        reference[j] = to;
                    }
                }
            }
            assertEquals(expectedMerge, dsu.union(a, b));
            assertEquals(numberOfLabels(reference), dsu.components(), old);
        }

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                assertEquals(reference[x] == reference[y], dsu.find(x) == dsu.find(y));
            }
        }
    }

    @Test
    void kruskalAndPrimAgreeOnOneHundredConnectedGraphs() {
        Random random = new Random(101);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(25);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) {
                graph.addEdge(v, random.nextInt(v), 1 + random.nextInt(100));
            }
            for (int i = 0; i < n; i++) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if (a != b) {
                    graph.addEdge(a, b, 1 + random.nextInt(100));
                }
            }
            MstAlgorithms.Result k = MstAlgorithms.kruskal(graph, new Metrics());
            MstAlgorithms.Result p = MstAlgorithms.prim(graph, new Metrics());
            assertEquals(k.totalWeight(), p.totalWeight());
            assertEquals(n - 1, k.edges().size());
            assertEquals(n - 1, p.edges().size());
        }
    }

    @Test
    void kosarajuMatchesMutualReachabilityOnRandomGraphs() {
        Random random = new Random(202);
        for (int trial = 0; trial < 60; trial++) {
            int n = 1 + random.nextInt(40);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < 4 * n; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(20) - 10);
            }

            KosarajuScc.Result actual = KosarajuScc.compute(graph, new Metrics());
            boolean[][] reach = transitiveReachability(graph);
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    assertEquals(reach[a][b] && reach[b][a],
                            actual.componentOf()[a] == actual.componentOf()[b]);
                }
            }
        }
    }

    @Test
    void kahnReturnsValidOrderAndDetectsCycles() {
        Graph dag = new Graph(6, true);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 2, 1);
        dag.addEdge(2, 4, 1);
        dag.addEdge(3, 5, 1);
        TopologicalSort.Result order = TopologicalSort.kahn(dag, new Metrics());
        assertTrue(order.acyclic());
        assertTrue(TopologicalSort.isValidOrder(dag, order.order()));
        Graph cyclic = new Graph(3, true);
        cyclic.addEdge(0, 1, 1);
        cyclic.addEdge(1, 2, 1);
        cyclic.addEdge(2, 0, 1);
        assertFalse(TopologicalSort.kahn(cyclic, new Metrics()).acyclic());
    }

    @Test
    void dagPathsMatchBellmanFordWithNegativeWeightsAndRestorePaths() {
        Random random = new Random(303);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(35);
            int[] rank = new int[n];
            for (int i = 0; i < n; i++) {
                rank[i] = i;
            }
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int swap = rank[i];
                rank[i] = rank[j];
                rank[j] = swap;
            }
            int[] position = new int[n];
            for (int i = 0; i < n; i++) {
                position[rank[i]] = i;
            }

            Graph graph = new Graph(n, true);
            for (int i = 0; i < 5 * n; i++) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if (a == b) {
                    continue;
                }
                if (position[a] > position[b]) {
                    int swap = a;
                    a = b;
                    b = swap;
                }
                graph.addEdge(a, b, random.nextInt(41) - 20);
            }

            int[] order = TopologicalSort.kahn(graph, new Metrics()).order();
            DagShortestPaths.Result actual =
                    DagShortestPaths.compute(graph, order, new Metrics());
            long[] expected = bellmanFordFromAllSources(graph);
            assertArrayEquals(expected, actual.distances());
            for (int v = 0; v < n; v++) {
                List<Integer> path = actual.pathTo(v);
                assertFalse(path.isEmpty());
                assertEquals(v, path.get(path.size() - 1));
            }
        }
    }

    @Test
    void iterativeSccHandlesOneHundredThousandVertexPath() {
        int n = 100_000;
        Graph path = new Graph(n, true);
        for (int v = 0; v + 1 < n; v++) {
            path.addEdge(v, v + 1, 1);
        }
        Metrics metrics = new Metrics();
        KosarajuScc.Result result = assertDoesNotThrow(() -> KosarajuScc.compute(path, metrics));
        assertEquals(n, result.count());
        assertEquals(n, metrics.maxDepth);
        assertEquals(2L * (n - 1), metrics.edgeScans);
    }

    @Test
    void loopsParallelEdgesDisconnectedForestsAndIsolatedVerticesAreSupported() {
        Graph graph = new Graph(5, false);
        graph.addEdge(0, 0, -2);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 3);

        MstAlgorithms.Result k = MstAlgorithms.kruskal(graph, new Metrics());
        MstAlgorithms.Result p = MstAlgorithms.prim(graph, new Metrics());
        assertEquals(3, k.components());
        assertEquals(3, p.components());
        assertEquals(5, k.totalWeight());
        assertEquals(k.totalWeight(), p.totalWeight());
        assertEquals(2, k.edges().size());
    }

    private static int numberOfLabels(int[] labels) {
        return (int) Arrays.stream(labels).distinct().count();
    }

    private static boolean[][] transitiveReachability(Graph graph) {
        int n = graph.vertexCount();
        boolean[][] reach = new boolean[n][n];
        for (int source = 0; source < n; source++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            reach[source][source] = true;
            queue.add(source);
            while (!queue.isEmpty()) {
                int v = queue.removeFirst();
                for (Graph.Arc arc : graph.neighbors(v)) {
                    if (!reach[source][arc.to()]) {
                        reach[source][arc.to()] = true;
                        queue.addLast(arc.to());
                    }
                }
            }
        }
        return reach;
    }

    private static long[] bellmanFordFromAllSources(Graph graph) {
        int n = graph.vertexCount();
        long inf = Long.MAX_VALUE / 4;
        long[] distance = new long[n];
        Arrays.fill(distance, inf);
        for (int v = 0; v < n; v++) {
            if (graph.inDegree(v) == 0) {
                distance[v] = 0;
            }
        }

        for (int pass = 0; pass < n - 1; pass++) {
            boolean changed = false;
            for (Graph.Edge edge : graph.edges()) {
                if (distance[edge.from()] < inf) {
                    long candidate = distance[edge.from()] + edge.weight();
                    if (candidate < distance[edge.to()]) {
                        distance[edge.to()] = candidate;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
        return distance;
    }
}
