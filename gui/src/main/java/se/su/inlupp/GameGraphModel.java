package se.su.inlupp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GameGraphModel {

  public enum SearchAlgorithm {
    BFS,
    DFS
  }

  private Graph<String> graph;
  private PathFinder<String> pathFinder;
  private boolean unsavedChanges;
  private static final int MIN_SIMILARITY_SCORE = 1;
  private static final int MAX_SIMILARITY_SCORE = 10;

  public GameGraphModel() {
    pathFinder = new BFSPathFinder<>();
    resetToDefaultGraph();
  }

  public void resetToDefaultGraph() {
    graph = new ListGraph<>();
    addDefaultGames();
    unsavedChanges = false;
  }

  public void clearGraph() {
    graph = new ListGraph<>();
    unsavedChanges = true;
  }

  public Set<String> getGames() {
    return graph.getNodes();
  }

  public Collection<Edge<String>> getEdgesFrom(String game) {
    return graph.getEdgesFrom(game);
  }

  public boolean hasGame(String gameName) {
    return graph.hasNode(gameName);
  }

  public void addGame(String gameName) {
    validateGameName(gameName);
    if (graph.hasNode(gameName)) {
      throw new IllegalStateException("Game already exists.");
    }
    graph.add(gameName);
    unsavedChanges = true;
  }

  public void removeGame(String gameName) {
    graph.remove(gameName);
    unsavedChanges = true;
  }

  public void connectGames(String game1, String game2, String connectionName, int weight) {
    validateGameName(connectionName);
    validateSimilarityScore(weight);
    graph.connect(game1, game2, connectionName, weight);
    unsavedChanges = true;
  }

  public Path<String> findPath(String from, String to) {
    return pathFinder.findPath(graph, from, to);
  }

  public void setSearchAlgorithm(SearchAlgorithm algorithm) {
    if (algorithm == SearchAlgorithm.DFS) {
      pathFinder = new DFSPathFinder<>();
    } else {
      pathFinder = new BFSPathFinder<>();
    }
  }

  public boolean hasUnsavedChanges() {
    return unsavedChanges;
  }

  public void markSaved() {
    unsavedChanges = false;
  }

  public List<String> getSimilarityTypes() {
    return List.of(
        "soulslike",
        "boss focused",
        "dark fantasy",
        "sandbox crafting",
        "survival",
        "open world rpg",
        "metroidvania",
        "exploration",
        "platforming",
        "sci-fi rpg",
        "other");
  }

  public boolean isValidSimilarityScore(int score) {
    return score >= MIN_SIMILARITY_SCORE && score <= MAX_SIMILARITY_SCORE;
  }

  private void validateGameName(String gameName) {
    if (gameName == null || gameName.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty.");
    }
  }

  private void validateSimilarityScore(int score) {
    if (!isValidSimilarityScore(score)) {
      throw new IllegalArgumentException("Similarity score must be between 1 and 10.");
    }
  }

  private void addDefaultGames() {
    List<String> similarityTypes = getSimilarityTypes();
    String soulslike = similarityTypes.get(0);
    String bossFocused = similarityTypes.get(1);
    String darkFantasy = similarityTypes.get(2);
    String sandboxCrafting = similarityTypes.get(3);
    String survival = similarityTypes.get(4);
    String openWorldRpg = similarityTypes.get(5);
    String metroidvania = similarityTypes.get(6);
    String exploration = similarityTypes.get(7);
    String platforming = similarityTypes.get(8);
    String sciFiRpg = similarityTypes.get(9);

    graph.add("Elden Ring");
    graph.add("Dark Souls");
    graph.add("Sekiro");
    graph.add("Bloodborne");
    graph.add("Lies of P");

    graph.connect("Elden Ring", "Dark Souls", soulslike, 10);
    graph.connect("Dark Souls", "Sekiro", bossFocused, 8);
    graph.connect("Elden Ring", "Bloodborne", darkFantasy, 8);
    graph.connect("Dark Souls", "Bloodborne", darkFantasy, 9);
    graph.connect("Sekiro", "Bloodborne", bossFocused, 6);
    graph.connect("Lies of P", "Bloodborne", darkFantasy, 9);
    graph.connect("Lies of P", "Dark Souls", soulslike, 8);
    graph.connect("Lies of P", "Elden Ring", soulslike, 7);

    graph.add("Minecraft");
    graph.add("Terraria");
    graph.add("Valheim");
    graph.add("Ark");
    graph.add("The Forest");

    graph.connect("Minecraft", "Valheim", survival, 7);
    graph.connect("Minecraft", "Ark", survival, 6);
    graph.connect("Minecraft", "Terraria", sandboxCrafting, 9);
    graph.connect("Valheim", "Terraria", sandboxCrafting, 6);
    graph.connect("Valheim", "The Forest", survival, 7);
    graph.connect("Ark", "Valheim", survival, 8);
    graph.connect("Ark", "The Forest", survival, 8);

    graph.add("Hollow Knight");
    graph.add("Blasphemous");
    graph.add("Dead Cells");
    graph.add("Ori and the blind forest");
    graph.add("Celeste");

    graph.connect("Hollow Knight", "Dead Cells", metroidvania, 7);
    graph.connect("Hollow Knight", "Blasphemous", metroidvania, 8);
    graph.connect("Ori and the blind forest", "Hollow Knight", exploration, 7);
    graph.connect("Ori and the blind forest", "Dead Cells", platforming, 5);
    graph.connect("Blasphemous", "Dead Cells", metroidvania, 6);
    graph.connect("Celeste", "Ori and the blind forest", platforming, 7);
    graph.connect("Celeste", "Hollow Knight", platforming, 5);

    graph.add("Skyrim");
    graph.add("The Witcher 3");
    graph.add("CyberPunk 2077");
    graph.add("Starfield");
    graph.add("Fallout 4");

    graph.connect("Skyrim", "The Witcher 3", openWorldRpg, 8);
    graph.connect("Skyrim", "Fallout 4", openWorldRpg, 7);
    graph.connect("Starfield", "Fallout 4", sciFiRpg, 8);
    graph.connect("Starfield", "CyberPunk 2077", sciFiRpg, 6);
    graph.connect("Starfield", "Skyrim", openWorldRpg, 7);
    graph.connect("The Witcher 3", "CyberPunk 2077", openWorldRpg, 9);
    graph.connect("Fallout 4", "CyberPunk 2077", sciFiRpg, 5);

    graph.connect("Elden Ring", "Skyrim", openWorldRpg, 4);
    graph.connect("Terraria", "Hollow Knight", exploration, 4);
    graph.connect("Fallout 4", "The Forest", survival, 4);
    graph.connect("Ori and the blind forest", "Sekiro", bossFocused, 5);
    graph.connect("Valheim", "Skyrim", exploration, 6);
  }
}
