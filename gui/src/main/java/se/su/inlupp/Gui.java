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
import java.util.Map;

public class Gui extends Application {

  private Map<String, StackPane> nodeViews = new HashMap<>();

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();

    // Soulslike
    graph.add("Elden Ring");
    graph.add("Dark Souls");
    graph.add("Sekiro");
    graph.add("Bloodborne");

    // Sandbox/Survival
    graph.add("Minecraft");
    graph.add("Terraria");
    graph.add("Valheim");

    // Metroidvania
    graph.add("Hollow Knight");
    graph.add("Blasphemous");
    graph.add("Dead Cells");

    // RPG/Open World
    graph.add("Skyrim");
    graph.add("The Witcher 3");
    graph.add("CyberPunk 2077");

    // Connections
    graph.connect("Elden Ring", "Dark Souls", "similar", 10);
    graph.connect("Dark Souls", "Sekiro", "similar", 8);
    graph.connect("Minecraft", "Terraria", "similar", 9);
    graph.connect("Hollow Knight", "Dead Cells", "similar", 7);
    graph.connect("Skyrim", "The Witcher 3", "similar", 8);
    graph.connect("The Witcher 3", "CyberPunk 2077", "similar", 9);

    // Connections bridges
    graph.connect("Elden Ring", "Skyrim", "similar", 4);
    graph.connect("Terraria", "Hollow Knight", "similar", 4);

    Pane root = new Pane();

    double x = 100;
    double y = 100;

    for (String game : graph.getNodes()) {
      StackPane node = createGameNode(game, Color.LIGHTBLUE, x, y);
      nodeViews.put(game, node);
      root.getChildren().add(node);

      x += 200;
      if (x > 600) {
        x = 100;
        y += 150;
      }
    }

    for (String game : graph.getNodes()) {

      for (Edge<String> edge : graph.getEdgesFrom(game)) {
        StackPane fromNode = nodeViews.get(game);
        StackPane toNode = nodeViews.get(edge.getDestination());

        Line line = new Line();
        updateLine(line, fromNode, toNode);

        root.getChildren().add(line);
      }
    }
    for (StackPane node : nodeViews.values()) {
      node.toFront();
    }

    Scene scene = new Scene(root, 640, 480);
    stage.setScene(scene);
    stage.show();
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
    return gameNode;
  }

}
