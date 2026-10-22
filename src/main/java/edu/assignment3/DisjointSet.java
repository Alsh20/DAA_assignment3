package edu.assignment3;

public final class DisjointSet {
    private final int[] parent;
    private final byte[] rank;
    private final Metrics metrics;
    private int components;
    private long findCalls;
    private long successfulUnionCalls;

    public DisjointSet(int size, Metrics metrics) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be non-negative");
        }

        this.metrics = metrics;
        parent = new int[size];
        rank = new byte[size];
        components = size;
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        check(x);
        findCalls++;
        if (metrics != null) metrics.dsuOps++;
        int root = x;
        while (root != parent[root]) {
            root = parent[root];
        }

        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }

        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }
        components--;
        successfulUnionCalls++;
        if (metrics != null) metrics.dsuOps++;
        return true;
    }

    private void check(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("set index " + x);
        }
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnionCalls() {
        return successfulUnionCalls;
    }
}
