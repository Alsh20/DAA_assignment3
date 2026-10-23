# DAA Assignment 3 - Alisher Ospanov - SE-2526

## Build and tests
    mvn test
Seven JUnit 5 tests: DSU against a simple reference, Kruskal vs Prim on 100
random graphs, SCC against brute-force reachability, Kahn (valid order and
cycle detection), DAG shortest paths against Bellman-Ford with negative
weights, and SCC on a 100,000-vertex path (no stack overflow)

## Input format
First line: n m directed|undirected, then m lines "u v w". Vertices are
0..n-1. Sample graphs are in data

## Counters
dsu_ops = find calls + successful unions (union makes two finds)
edge_scans = adjacency or candidate edges examined. comparisons = weight
comparisons in sorting, priority queues and relaxation. max_depth = largest
size of the explicit DFS stack (0 for non-DFS algorithms)