package se.su.inlupp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;

public class Gui extends Application {

  private Map<String, StackPane> nodeViews = new HashMap<>();

  //lista för linjer
  private Map<String, Line> edgeLines = new HashMap<>();

  private String selectedNode1 = null;
  private String selectedNode2 = null;
  private BFSPathFinder<String> bfsFinder = new BFSPathFinder<>();

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();

    //
    // Soulslike
    //
    graph.add("Elden Ring");
    graph.add("Dark Souls");
    graph.add("Sekiro");
    graph.add("Bloodborne");
    graph.add("Lies of P");

    graph.connect("Elden Ring", "Dark Souls", "similar", 10);
    graph.connect("Dark Souls", "Sekiro", "similar", 8);
    graph.connect("Elden Ring", "Bloodborne", "similar", 8);
    graph.connect("Dark Souls", "Bloodborne", "similar", 9);
    graph.connect("Sekiro", "Bloodborne", "similar", 6);
    graph.connect("Lies of P", "Bloodborne", "similar", 9);
    graph.connect("Lies of P", "Dark Souls", "similar", 8);
    graph.connect("Lies of P", "Elden Ring", "similar", 7);

    //
    // Sandbox/Survival
    //
    graph.add("Minecraft");
    graph.add("Terraria");
    graph.add("Valheim");
    graph.add("Ark");
    graph.add("The Forest");

    graph.connect("Minecraft", "Valheim", "similar", 7);
    graph.connect("Minecraft", "Ark", "similar", 6);
    graph.connect("Minecraft", "Terraria", "similar", 9);
    graph.connect("Valheim", "Terraria", "similar", 6);
    graph.connect("Valheim", "The Forest", "similar", 7);
    graph.connect("Ark", "Valheim", "similar", 8);
    graph.connect("Ark", "The Forest", "similar", 8);

    //
    // Metroidvania
    //
    graph.add("Hollow Knight");
    graph.add("Blasphemous");
    graph.add("Dead Cells");
    graph.add("Ori and the blind forest");
    graph.add("Celeste");

    graph.connect("Hollow Knight", "Dead Cells", "similar", 7);
    graph.connect("Hollow Knight", "Blasphemous", "similar", 8);
    graph.connect("Ori and the blind forest", "Hollow Knight", "similar", 7);
    graph.connect("Ori and the blind forest", "Dead Cells", "similar", 5);
    graph.connect("Blasphemous", "Dead Cells", "similar", 6);
    graph.connect("Celeste", "Ori and the blind forest", "similar", 7);
    graph.connect("Celeste", "Hollow Knight", "similar", 5);

    //
    // RPG/Open World
    //
    graph.add("Skyrim");
    graph.add("The Witcher 3");
    graph.add("CyberPunk 2077");
    graph.add("Starfield");
    graph.add("Fallout 4");

    graph.connect("Skyrim", "The Witcher 3", "similar", 8);
    graph.connect("Skyrim", "Fallout 4", "similar", 7);
    graph.connect("Starfield", "Fallout 4", "similar", 8);
    graph.connect("Starfield", "CyberPunk 2077", "similar", 6);
    graph.connect("Starfield", "Skyrim", "similar", 7);
    graph.connect("The Witcher 3", "CyberPunk 2077", "similar", 9);
    graph.connect("Fallout 4", "CyberPunk 2077", "similar", 5);

    //
    // Bridges between clusters
    //
    graph.connect("Elden Ring", "Skyrim", "similar", 4);
    graph.connect("Terraria", "Hollow Knight", "similar", 4);
    graph.connect("Fallout 4", "The Forest", "similar", 4);
    graph.connect("Ori and the blind forest", "Sekiro", "similar", 5);
    graph.connect("Valheim", "Skyrim", "similar", 6);

    Pane root = new Pane();

    // node positions
    Map<String, double[]> positions = new HashMap<>();

    positions.put("Bloodborne", new double[] { 50, 100 });
    positions.put("Dark Souls", new double[] { 250, 200 });
    positions.put("Sekiro", new double[] { 450, 100 });
    positions.put("Elden Ring", new double[] { 250, 20 });
    positions.put("Lies of P", new double[] { 250, 380 });

    positions.put("Minecraft", new double[] { 50, 550 });
    positions.put("Terraria", new double[] { 250, 650 });
    positions.put("Valheim", new double[] { 450, 550 });
    positions.put("Ark", new double[] { 250, 450 });
    positions.put("The Forest", new double[] { 250, 820 });

    positions.put("Hollow Knight", new double[] { 750, 250 });
    positions.put("Blasphemous", new double[] { 950, 350 });
    positions.put("Dead Cells", new double[] { 750, 450 });
    positions.put("Ori and the blind forest", new double[] { 550, 350 });
    positions.put("Celeste", new double[] { 750, 620 });

    positions.put("Skyrim", new double[] { 1250, 150 });
    positions.put("The Witcher 3", new double[] { 1250, 350 });
    positions.put("CyberPunk 2077", new double[] { 1250, 550 });
    positions.put("Fallout 4", new double[] { 1450, 250 });
    positions.put("Starfield", new double[] { 1450, 450 });

    // node loop
    for (String game : graph.getNodes()) {
      System.out.println(game);
      double[] pos = positions.get(game);

      double x = pos[0];
      double y = pos[1];

      StackPane node = createGameNode(game, getGameColor(game), x, y);
      nodeViews.put(game, node);
      root.getChildren().add(node);
    }

    // edge loop
    for (String game : graph.getNodes()) {

      for (Edge<String> edge : graph.getEdgesFrom(game)) {
        StackPane fromNode = nodeViews.get(game);
        StackPane toNode = nodeViews.get(edge.getDestination());

        Line line = new Line();
        line.setStrokeWidth(3);
        line.setStroke(Color.DARKSLATEGRAY);
        updateLine(line, fromNode, toNode);

        edgeLines.put(game + "->" + edge.getDestination(), line);

        Tooltip tooltip = new Tooltip("Similarity: " + edge.getWeight());
        Tooltip.install(line, tooltip);

        Text weightText = new Text(String.valueOf(edge.getWeight()));
        double middleX = (line.getStartX() + line.getEndX()) / 2;
        double middleY = (line.getStartY() + line.getEndY()) / 2;
        weightText.setX(middleX);
        weightText.setY(middleY);

        root.getChildren().addAll(line, weightText);
      }
    }

    // nodes to front
    for (StackPane node : nodeViews.values()) {
      node.toFront();
    }

    ScrollPane scrollPane = new ScrollPane(root);

    Scene scene = new Scene(scrollPane, 1600, 900);
    root.setStyle("-fx-background-color: burlywood;");
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    Button searchBtn = new Button("Hitta väg");
    searchBtn.setLayoutX(10);
    searchBtn.setLayoutY(10);

    searchBtn.setOnAction(event -> {
        if (selectedNode1 != null && selectedNode2 != null) {
            resetLines();
            Path<String> path = bfsFinder.findPath(graph, selectedNode1, selectedNode2);
            
            if (path != null) {
                highlightPath(path);
            } else {
                System.out.println("No path found between " + selectedNode1 + " and " + selectedNode2);
            }

            resetSelection();
        }
    });

    root.getChildren().add(searchBtn);

  }

  public static void main(String[] args) {
    launch(args);
  }

  private void updateLine(Line line, StackPane gameNode, StackPane gameNode2) {
    int middlePosX = 75;
    int middlePosY = 30;

    line.setStartX(gameNode.getLayoutX() + middlePosX);
    line.setStartY(gameNode.getLayoutY() + middlePosY);

    line.setEndX(gameNode2.getLayoutX() + middlePosX);
    line.setEndY(gameNode2.getLayoutY() + middlePosY);
  }

  private StackPane createGameNode(String title, Color color, double x, double y) {
    StackPane gameNode = new StackPane();
    Rectangle gameBox = new Rectangle(150, 60);
    gameBox.setFill(color);
    Text gameText = new Text(title);
    gameNode.getChildren().addAll(gameBox, gameText);
    gameNode.setLayoutX(x);
    gameNode.setLayoutY(y);

    final double[] mouseOffset = new double[2];
    gameNode.setOnMousePressed(event -> {
      mouseOffset[0] = event.getSceneX() - gameNode.getLayoutX();
      mouseOffset[1] = event.getSceneY() - gameNode.getLayoutY();
    });
    gameNode.setOnMouseDragged(event -> {
      gameNode.setLayoutX(event.getSceneX() - mouseOffset[0]);
      gameNode.setLayoutY(event.getSceneY() - mouseOffset[1]);
    });

    gameNode.setOnMouseClicked(event -> {
      Rectangle rect = (Rectangle) gameNode.getChildren().get(0);

      if (selectedNode1 == null) {
          selectedNode1 = title;
          rect.setStroke(Color.GOLD);
          rect.setStrokeWidth(5);
      } else if (selectedNode2 == null && !title.equals(selectedNode1)) {
          selectedNode2 = title;
          rect.setStroke(Color.GOLD); 
          rect.setStrokeWidth(5);
      }
});
    return gameNode;
  }

  private Color getGameColor(String game) {

    switch (game) {
      case "Elden Ring":
      case "Dark Souls":
      case "Sekiro":
      case "Bloodborne":
      case "Lies of P":
        return Color.LIGHTCORAL;

      case "Minecraft":
      case "Terraria":
      case "Valheim":
      case "Ark":
      case "The Forest":
        return Color.LIGHTGREEN;

      case "Skyrim":
      case "The Witcher 3":
      case "CyberPunk 2077":
      case "Starfield":
      case "Fallout 4":
        return Color.LIGHTBLUE;

      default:
        return Color.PLUM;
    }
  }

  private void highlightPath(Path<String> path) {
      List<String> nodes = path.getNodes();
      
      for (int i = 0; i < nodes.size() - 1; i++) {
          String from = nodes.get(i);
          String to = nodes.get(i + 1);
          
          String key = from + "->" + to;

          Line line = edgeLines.get(key);

          if(line == null){

            key = to + "->" + from;

            line = edgeLines.get(key);
          }

          if(line != null){
            line.setStroke(Color.GOLD);
            line.setStrokeWidth(6);
          }
      }
  }

  private void resetLines() {
    for (Line l : edgeLines.values()) {
        l.setStroke(Color.DARKSLATEGRAY);
        l.setStrokeWidth(3);
    }
  }

  private void resetSelection() {
    selectedNode1 = null;
    selectedNode2 = null;

    for (StackPane node : nodeViews.values()) {
        Rectangle rect = (Rectangle) node.getChildren().get(0);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(1);
    }
}
}
