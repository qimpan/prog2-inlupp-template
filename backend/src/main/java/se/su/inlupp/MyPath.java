package se.su.inlupp;
import java.util.*;

public class MyPath<T> implements Path<T> {
    
    private T start;
    private List<Edge<T>> edges;

    public MyPath(T start, List<Edge<T>> edges) {
        this.start = start;
        this.edges = edges;
    }

    @Override
    public T getStart() {
        return start;
    }

    @Override
    public List<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public Iterator<Edge<T>> iterator() {
        return edges.iterator();
    }

    @Override
    public T getEnd() {
        if (edges.isEmpty()) {
            return start;
        }
        return edges.get(edges.size() - 1).getDestination();
    }

    @Override
    public int getTotalWeight() {
        int total = 0;
        for (Edge<T> edge : edges) {
            total += edge.getWeight();
        }
        return total;
    }

    @Override
    public List<T> getNodes() {
        List<T> nodes = new ArrayList<>();
        nodes.add(start);
        for (Edge<T> edge : edges) {
            nodes.add(edge.getDestination());
        }
        return nodes;
    }
}