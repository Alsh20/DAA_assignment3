package edu.assignment3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class Benchmark {
    private static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    private static final int[] DENSITIES = {4, 20};
    private static final int REPEATS = 5;
    private static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";
    private static final long[][] EXPECTED = {
            {147_970, 27, 396_274}, {30_363, 1, 252_411},
            {468_911, 137, 1_271_911}, {97_589, 1, 723_157},
            {1_486_139, 373, 3_941_828}, {309_550, 1, 2_381_342},
            {4_715_515, 1_188, 12_551_891}, {968_517, 1, 7_376_612},
            {15_012_094, 3_923, 40_096_539}, {3_056_681, 1, 23_512_087}
    };

    private Benchmark() {}

    private record Measurement<T>(T value, Metrics metrics, double medianMillis) {}

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0
                ? Path.of("results", "results.csv")
                : Path.of(args[0]);
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ArrayList<String> rows = new ArrayList<>(50);
        int caseIndex = 0;
        for (int n : SIZES) {
            for (int multiplier : DENSITIES) {
                int m = Math.multiplyExact(n, multiplier);
                long[] expected = EXPECTED[caseIndex++];
                runMstRows(rows, n, m, multiplier, expected[0]);
                runSccRow(rows, n, m, multiplier, expected[1]);
                runDagRows(rows, n, m, multiplier, expected[2]);
                System.out.printf("checked n=%d m=%d: MST=%d SCC=%d DAG-distance-sum=%d%n",
                        n, m, expected[0], expected[1], expected[2]);
                System.gc();
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
        if (rows.size() != 50) {
            throw new IllegalStateException("expected 50 rows, wrote " + rows.size());
        }
        System.out.println(
                "Wrote " + rows.size() + " verified rows to " + output.toAbsolutePath());
    }

    private static void runMstRows(
            List<String> rows, int n, int m, int multiplier, long expectedWeight) {
        Graph graph = GraphGenerator.undirectedForMst(n, m);
        Measurement<MstAlgorithms.Result> kruskal =
                measure(metrics -> MstAlgorithms.kruskal(graph, metrics));
        Measurement<MstAlgorithms.Result> prim =
                measure(metrics -> MstAlgorithms.prim(graph, metrics));
        require(kruskal.value().totalWeight() == prim.value().totalWeight(),
                "Kruskal/Prim disagreement for n=" + n + ", m=" + m);
        require(kruskal.value().totalWeight() == expectedWeight,
                "MST reference mismatch for n=" + n + ", m=" + m + ": "
                        + kruskal.value().totalWeight());
        require(kruskal.value().components() == 1 && prim.value().components() == 1,
                "generated MST graph is not connected");
        add(rows, "Kruskal", "undirected-" + density(multiplier), n, m, kruskal,
                kruskal.value().totalWeight());
        add(rows, "Prim", "undirected-" + density(multiplier), n, m, prim,
                prim.value().totalWeight());
    }

    private static void runSccRow(
            List<String> rows, int n, int m, int multiplier, long expectedCount) {
        Graph graph = GraphGenerator.directedForScc(n, m);
        Measurement<KosarajuScc.Result> scc =
                measure(metrics -> KosarajuScc.compute(graph, metrics));
        require(scc.value().count() == expectedCount,
                "SCC reference mismatch for n=" + n + ", m=" + m + ": " + scc.value().count());
        require(scc.metrics().edgeScans == 2L * m,
                "Kosaraju should scan exactly 2m edges, got " + scc.metrics().edgeScans);
        add(rows, "Kosaraju", "directed-" + density(multiplier), n, m, scc, scc.value().count());
    }

    private static void runDagRows(
            List<String> rows, int n, int m, int multiplier, long expectedDistanceSum) {
        Graph graph = GraphGenerator.dag(n, m);
        Measurement<TopologicalSort.Result> topological =
                measure(metrics -> TopologicalSort.kahn(graph, metrics));
        require(topological.value().acyclic(), "DAG generator produced a cycle");
        require(TopologicalSort.isValidOrder(graph, topological.value().order()),
                "Kahn returned an invalid order");
        require(topological.metrics().edgeScans == m,
                "Kahn should scan exactly m edges, got " + topological.metrics().edgeScans);
        Measurement<DagShortestPaths.Result> paths = measure(
                metrics -> DagShortestPaths.compute(graph, topological.value().order(), metrics));
        require(paths.metrics().edgeScans == m,
                "DAG relaxation should scan exactly m edges, got " + paths.metrics().edgeScans);
        long sum = paths.value().sumOfDistances();
        require(sum == expectedDistanceSum,
                "DAG distance reference mismatch for n=" + n + ", m=" + m + ": " + sum);
        add(rows, "Kahn", "dag-" + density(multiplier), n, m, topological,
                topological.value().order().length);
        add(rows, "DagShortestPath", "dag-" + density(multiplier), n, m, paths, sum);
    }

    private static <T> Measurement<T> measure(Function<Metrics, T> operation) {
        long[] elapsed = new long[REPEATS];
        T firstValue = null;
        Metrics firstMetrics = null;
        for (int run = 0; run < REPEATS; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            T value = operation.apply(metrics);
            elapsed[run] = System.nanoTime() - start;
            if (run == 0) {
                firstValue = value;
                firstMetrics = metrics;
            }
        }
        Arrays.sort(elapsed);
        return new Measurement<>(firstValue, firstMetrics, elapsed[REPEATS / 2] / 1_000_000.0);
    }

    private static void add(List<String> rows, String algorithm, String graph, int n, int m,
                            Measurement<?> measurement, long result) {
        Metrics x = measurement.metrics();
        rows.add(String.join(",", algorithm, graph, Integer.toString(n), Integer.toString(m),
                String.format(java.util.Locale.ROOT, "%.3f", measurement.medianMillis()),
                Long.toString(x.dsuOps), Long.toString(x.edgeScans), Long.toString(x.comparisons),
                Integer.toString(x.maxDepth), Long.toString(result)));
    }

    private static String density(int multiplier) {
        return multiplier == 4 ? "sparse" : "dense";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
