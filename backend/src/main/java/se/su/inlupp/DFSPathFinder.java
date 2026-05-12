package se.su.inlupp;

import java.util.*;

public class DFSPathFinder<T> implements PathFinder<T> {

    @Override
    public Path<T> findPath(Graph<T> graph, T from, T to) {
        if (!graph.hasNode(from) || !graph.hasNode(to)) {
            return null;
        }
        
        Set<T> visited = new HashSet<>();
        return dfs(graph, from, to, visited);
    }

    private Path<T> dfs(Graph<T> graph, T current, T to, Set<T> visited) {
        if (visited.contains(current)) {
            return null;
        }

        visited.add(current);

        if (current.equals(to)) {
            return new MyPath<>(current, new ArrayList<>());
        }

        for (Edge<T> edge : graph.getEdgesFrom(current)) {
            Path<T> res = dfs(graph, edge.getDestination(), to, visited);

            if (res != null) {
  
                res.getEdges().add(0, edge);
                return res;
            }
        }
        return null;
    }
}