package se.su.inlupp;

import java.util.*;

public class BFSPathFinder<T> implements PathFinder<T> {

    @Override
    public Path<T> findPath(Graph<T> graph, T from, T to) {
        if (!graph.hasNode(from) || !graph.hasNode(to)) {
            return null;
        }

        Queue<T> queue = new LinkedList<>();
        Map<T, Edge<T>> edgeFrom = new HashMap<>();
        Map<T, T> parentNodes = new HashMap<>();
        
        queue.add(from);
        parentNodes.put(from, null);

        while (!queue.isEmpty()) {
            T current = queue.poll();

            if (current.equals(to)) {
                return gatherPath(from, to, edgeFrom, parentNodes);
            }

            for (Edge<T> edge : graph.getEdgesFrom(current)) {
                T next = edge.getDestination();
                if (!parentNodes.containsKey(next)) {
                    parentNodes.put(next, current);
                    edgeFrom.put(next, edge);
                    queue.add(next);
                }
            }
        }

        return null;
    }

    private Path<T> gatherPath(T from, T to, Map<T, Edge<T>> edgeFrom, Map<T, T> parentNodes) {
        List<Edge<T>> path = new ArrayList<>();
        T current = to;

        while (current != null && !current.equals(from)) {
            Edge<T> edge = edgeFrom.get(current);
            path.add(0, edge);
            current = parentNodes.get(current);
        }

        return new MyPath<>(from, path);
    }
}