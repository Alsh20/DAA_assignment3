package edu.assignment3;

public final class Metrics {
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void observeDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }
}
