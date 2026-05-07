package se.su.inlupp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ListGraph<T> implements Graph<T> {
  private Map<T, List<Edge<T>>> adjacencyList = new HashMap<>();

  @Override
  public void add(T node) {
    if (!adjacencyList.containsKey(node)) {
      adjacencyList.put(node, new ArrayList<>());
    }
  }

  @Override
  public void remove(T node) {
    if (!hasNode(node)) {
      throw new NoSuchElementException();
    }
    for (Edge<T> edge : new ArrayList<>(adjacencyList.get(node))) {
      disconnect(node, edge.getDestination());
    }
    adjacencyList.remove(node);
  }

  @Override
  public boolean hasNode(T node) {
    if (adjacencyList.containsKey(node)) {
      return true;
    }
    return false;
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    if (!hasNode(node1) || !hasNode(node2)) {
      throw new NoSuchElementException();
    }

    if (weight < 0) {
      throw new IllegalArgumentException();
    }

    if (getEdgeBetween(node1, node2) != null) {
      throw new IllegalStateException();
    }

    ListEdge edge = new ListEdge(node2, name, weight);
    adjacencyList.get(node1).add(edge);

    ListEdge reverseEdge = new ListEdge(node1, name, weight);
    adjacencyList.get(node2).add(reverseEdge);

  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!hasNode(node1) || !hasNode(node2)) {
      throw new NoSuchElementException();
    }

    if (getEdgeBetween(node1, node2) == null) {
      throw new IllegalStateException();
    }

    List<Edge<T>> edgesFromNode1 = adjacencyList.get(node1);
    edgesFromNode1.remove(getEdgeBetween(node1, node2));
    List<Edge<T>> edgesFromNode2 = adjacencyList.get(node2);
    edgesFromNode2.remove(getEdgeBetween(node2, node1));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (!hasNode(node1) || !hasNode(node2) || getEdgeBetween(node1, node2) == null) {
      throw new NoSuchElementException();
    }
    if (weight < 0) {
      throw new IllegalArgumentException();
    }
    getEdgeBetween(node1, node2).setWeight(weight);
    getEdgeBetween(node2, node1).setWeight(weight);
  }

  @Override
  public Set<T> getNodes() {
    Set<T> nodes = new HashSet<>(adjacencyList.keySet());
    return nodes;
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    if (!hasNode(node)) {
      throw new NoSuchElementException();
    }
    Collection<Edge<T>> collectionCopy = new ArrayList<>(adjacencyList.get(node));
    return collectionCopy;
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    if (!hasNode(node1) || !hasNode(node2)) {
      throw new NoSuchElementException();
    }
    Collection<Edge<T>> edges = new ArrayList<>(adjacencyList.get(node1));
    for (Edge<T> edge : edges) {
      if (edge.getDestination().equals(node2)) {
        return edge;
      }
    }
    return null;
  }

  @Override
  public Iterator<T> iterator() {
    return adjacencyList.keySet().iterator();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (T node : adjacencyList.keySet()) {
      sb.append(node.toString()).append(": ");

      for (Edge<T> edge : adjacencyList.get(node)) {
        sb.append(edge.getDestination().toString())
            .append("(")
            .append(edge.getWeight())
            .append(") ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private class ListEdge implements Edge<T> {

    private T destination;
    private String name;
    private int weight;

    public ListEdge(T destination, String name, int weight) {

      if (weight < 0) {
        throw new IllegalArgumentException();
      }
      this.destination = destination;
      this.name = name;
      this.weight = weight;
    }

    @Override
    public T getDestination() {
      return destination;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getWeight() {
      return weight;
    }

    @Override
    public void setWeight(int weight) {
      if (weight < 0) {
        throw new IllegalArgumentException();
      }
      this.weight = weight;
    }

    @Override
    public String toString() {
      return "till " + destination + " med " + name + " tar " + weight;
    }
  }
}