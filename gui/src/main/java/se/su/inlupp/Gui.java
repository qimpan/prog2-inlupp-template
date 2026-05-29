package se.su.inlupp;

import javafx.application.Application;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.PrintWriter;

public class Gui extends Application {

  private Pane graphPane = new Pane();

  private Map<String, StackPane> nodeViews = new HashMap<>();
  private Map<String, List<Line>> connectedLines = new HashMap<>();
  private Map<Line, String[]> lineConnections = new HashMap<>();
  private Map<String, Line> edgeLines = new HashMap<>();
  
  private Map<String, String> gameImages = new HashMap<>();

  private String selectedNode1 = null;
  private String selectedNode2 = null;
  private final GameGraphModel model = new GameGraphModel();

  private boolean hasUnsavedChanges = false;

  public void start(Stage stage) {
    Pane root = new Pane();
    root.getChildren().add(graphPane);

    root.setOnMouseClicked(event -> {
      if (event.getTarget() == root || event.getTarget() == graphPane) {
        resetSelection();
      }
    });

    createMenuBar(root);
    Map<String, double[]> positions = createInitialPositions();
    drawInitialGraph(graphPane, positions);

    Button addNodeButton = createAddNodeButton(model, root);
    addNodeButton.setLayoutX(10);
    addNodeButton.setLayoutY(90);
    root.getChildren().add(addNodeButton);

    Button removeNodeButton = createRemoveNodeButton(root);
    root.getChildren().add(removeNodeButton);

    Button searchButton = createSearchButton(root);
    root.getChildren().add(searchButton);

    Button connectButton = createConnectButton(root);
    root.getChildren().add(connectButton);

    Button addImageButton = createAddImageButton(root);
    root.getChildren().add(addImageButton);

    stage.setOnCloseRequest(event -> {
      if (!checkUnsavedChanges()) {
        event.consume();
      }
    });

    ScrollPane scrollPane = new ScrollPane(root);
    Scene scene = new Scene(scrollPane, 1600, 900);
    root.setStyle("-fx-background-color: burlywood;");
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private boolean checkUnsavedChanges() {
    if (!hasUnsavedChanges) {
      return true;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Osparade ändringar");
    alert.setHeaderText("Varning: Du har osparade ändringar!");
    alert.setContentText("Vill du fortsätta ändå och förlora dina ändringar?");

    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void drawInitialGraph(Pane container, Map<String, double[]> positions) {
    for (String game : model.getGames()) {
      double[] pos = positions.get(game);
      double x = pos[0];
      double y = pos[1];

      StackPane node = createGameNode(game, game, getGameColor(game), x, y, "NONE");
      connectedLines.put(game, new ArrayList<>());
      nodeViews.put(game, node);
      container.getChildren().add(node);
    }

    for (String game : model.getGames()) {
      for (Edge<String> edge : model.getEdgesFrom(game)) {
        addEdgeView(container, game, edge.getDestination(), edge.getName(), edge.getWeight());
      }
    }
    bringNodesToFront();

    hasUnsavedChanges = false;
  }

  private Button createRemoveNodeButton(Pane root) {
    Button removeNodeButton = new Button("Remove Game");
    removeNodeButton.setLayoutX(10);
    removeNodeButton.setLayoutY(130);

    removeNodeButton.setOnAction(event -> {
      if (selectedNode1 != null) {
        String gameToRemove = selectedNode1;

        try {
          model.removeGame(gameToRemove);
          removeGameView(graphPane, gameToRemove);
          gameImages.remove(gameToRemove);
          resetSelection();

          hasUnsavedChanges = true;
        } catch (Exception e) {
          showError("Error removing game", e.getMessage());
        }
      } else {
        showError("No game selected", "Select a game to remove.");
      }
    });

    return removeNodeButton;
  }

  private Button createAddNodeButton(GameGraphModel model, Pane root) {
    Button addNodeButton = new Button("Add Game");

    addNodeButton.setOnAction(event -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add Game");
      dialog.setHeaderText("Add a new Game");
      dialog.setContentText("Game name;");

      Optional<String> result = dialog.showAndWait();

      if (result.isPresent()) {
        String gameName = result.get().trim();

        if (gameName.isEmpty()) {
          showError("Invalid input", "Game name cannot be empty.");
          return;
        }

        if (model.hasGame(gameName)) {
          showError("Invalid input", "That game already exists.");
          return;
        }
        model.addGame(gameName);

        double x = 100 + (nodeViews.size() % 5) * 180;
        double y = 150 + (nodeViews.size() / 5) * 100;

        StackPane node = createGameNode(gameName, gameName, getGameColor(gameName), x, y, "NONE");

        connectedLines.put(gameName, new ArrayList<>());
        nodeViews.put(gameName, node);
        graphPane.getChildren().add(node);
        node.toFront();

        hasUnsavedChanges = true;
      }
    });
    return addNodeButton;
  }

  private Button createAddImageButton(Pane root) {
    Button addImageButton = new Button("Add Image");
    addImageButton.setLayoutX(10);
    addImageButton.setLayoutY(210);

    addImageButton.setOnAction(event -> {
      if (selectedNode1 == null) {
        showError("No game selected", "Select a game node first to add an image to it.");
        return;
      }

      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Välj bild för " + selectedNode1);
      fileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Bildfiler", "*.png", "*.jpg", "*.jpeg")
      );

      File file = fileChooser.showOpenDialog(root.getScene().getWindow());
      if (file != null) {
        try {
          String imagePath = file.toURI().toString();
          String game = selectedNode1;

          gameImages.put(game, imagePath);

          StackPane node = nodeViews.get(game);
          
          node.getChildren().removeIf(child -> child instanceof ImageView);

          Image img = new Image(imagePath);
          ImageView imgView = new ImageView(img);
          imgView.setFitWidth(35);
          imgView.setFitHeight(35);
          imgView.setPreserveRatio(true);
          imgView.setTranslateX(-45);

          node.getChildren().add(imgView);
          hasUnsavedChanges = true;
          
          resetSelection();
        } catch (Exception e) {
          showError("Fel vid bildladdning", "Kunde inte visa bilden: " + e.getMessage());
        }
      }
    });

    return addImageButton;
  }

  private Button createSearchButton(Pane root) {
    Button searchButton = new Button("Find Path");
    searchButton.setLayoutX(10);
    searchButton.setLayoutY(50);

    searchButton.setOnAction(event -> {
      if (selectedNode1 != null && selectedNode2 != null) {
        resetLines();
        Path<String> path = model.findPath(selectedNode1, selectedNode2);

        if (path != null) {
          highlightPath(path);
          showPathInfo(path);
        } else {
          showError("No path found", "No path found between " + selectedNode1 + " and " + selectedNode2 + ".");
        }

        resetSelection();
      } else {
        showError("No nodes selected", "Select two games before searching for a path.");
      }
    });

    return searchButton;
  }

  private Button createConnectButton(Pane root) {
    Button connectButton = new Button("Connect Games");
    connectButton.setLayoutX(10);
    connectButton.setLayoutY(170);

    connectButton.setOnAction(event -> {
      if (selectedNode1 == null || selectedNode2 == null) {
        showError("No games selected", "Select two games to connect.");
        return;
      } else {
        String from = selectedNode1;
        String to = selectedNode2;

        ChoiceDialog<String> nameDialog = new ChoiceDialog<>("other", model.getSimilarityTypes());
        nameDialog.setTitle("Similarity type");
        nameDialog.setHeaderText("Connect " + from + " and " + to);
        nameDialog.setContentText("Choose similarity type:");

        Optional<String> nameResult = nameDialog.showAndWait();

        if (nameResult.isEmpty()) {
          return;
        }

        String connectionName = nameResult.get();

        TextInputDialog scoreDialog = new TextInputDialog();
        scoreDialog.setTitle("Similarity score");
        scoreDialog.setHeaderText("Connect " + from + " and " + to);
        scoreDialog.setContentText("Enter similarity score (1-10):");

        Optional<String> scoreResult = scoreDialog.showAndWait();

        if (scoreResult.isEmpty()) {
          return;
        }

        int similarityScore;

        try {
          similarityScore = Integer.parseInt((scoreResult.get().trim()));
        } catch (NumberFormatException e) {
          showError("Invalid input", "Similarity score must be a number.");
          return;
        }

        if (!model.isValidSimilarityScore(similarityScore)) {
          showError("Invalid input", "Similarity score must be between 1 and 10.");
          return;
        }

        try {
          model.connectGames(from, to, connectionName, similarityScore);
          addEdgeView(graphPane, from, to, connectionName, similarityScore);
          resetSelection();

          hasUnsavedChanges = true;

        } catch (IllegalStateException e) {
          showError("Connection already exists", from + " and " + to + " are already connected.");
        } catch (Exception e) {
          showError("Could not connect games", e.getMessage());
        }
      }
    });
    return connectButton;
  }

  private void bringNodesToFront() {
    for (StackPane node : nodeViews.values()) {
      node.toFront();
    }
  }

  private void updateLine(Line line, StackPane gameNode, StackPane gameNode2) {
    int middlePosX = 75;
    int middlePosY = 30;

    line.setStartX(gameNode.getLayoutX() + middlePosX);
    line.setStartY(gameNode.getLayoutY() + middlePosY);

    line.setEndX(gameNode2.getLayoutX() + middlePosX);
    line.setEndY(gameNode2.getLayoutY() + middlePosY);
  }

  private StackPane createGameNode(String game, String title, Color color, double x, double y, String imagePath) {
    StackPane gameNode = new StackPane();
    Rectangle gameBox = new Rectangle(150, 60);
    gameBox.setFill(color);
    Text gameText = new Text(title);
    gameNode.getChildren().addAll(gameBox, gameText);
    gameNode.setLayoutX(x);
    gameNode.setLayoutY(y);

    if (imagePath != null && !imagePath.equals("NONE")) {
      try {
        Image img = new Image(imagePath);
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(35);
        imgView.setFitHeight(35);
        imgView.setPreserveRatio(true);
        imgView.setTranslateX(-45);
        gameNode.getChildren().add(imgView);
        gameImages.put(game, imagePath);
      } catch (Exception e) {
        System.out.println("Kunde inte ladda bild för " + game + ": " + imagePath);
      }
    }

    final double[] mouseOffset = new double[2];
    gameNode.setOnMousePressed(event -> {
      mouseOffset[0] = event.getSceneX() - gameNode.getLayoutX();
      mouseOffset[1] = event.getSceneY() - gameNode.getLayoutY();
    });
    gameNode.setOnMouseDragged(event -> {
      gameNode.setLayoutX(event.getSceneX() - mouseOffset[0]);
      gameNode.setLayoutY(event.getSceneY() - mouseOffset[1]);
      for (Line line : connectedLines.get(game)) {
        String[] endpoints = lineConnections.get(line);
        StackPane node1 = nodeViews.get(endpoints[0]);
        StackPane node2 = nodeViews.get(endpoints[1]);
        updateLine(line, node1, node2);
      }

      hasUnsavedChanges = true;
    });

    gameNode.setOnMouseClicked(event -> {
      Rectangle rect = (Rectangle) gameNode.getChildren().get(0);

      if (title.equals(selectedNode1) || title.equals(selectedNode2)) {
        resetSelection();
        event.consume();
        return;
      }

      if (selectedNode1 == null) {
        selectedNode1 = title;
        rect.setStroke(Color.GOLD);
        rect.setStrokeWidth(5);
      } else if (selectedNode2 == null && !title.equals(selectedNode1)) {
        selectedNode2 = title;
        rect.setStroke(Color.GOLD);
        rect.setStrokeWidth(5);
      }
      event.consume();
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
    List<Edge<String>> edges = path.getEdges();

    for (int i = 0; i < nodes.size() - 1; i++) {
      String from = nodes.get(i);
      String to = nodes.get(i + 1);

      String key = from + "->" + to;
      Line line = edgeLines.get(key);

      if (line == null) {
        key = to + "->" + from;
        line = edgeLines.get(key);
      }

      if (line != null) {
        line.setStroke(getPathColor(edges.get(i).getWeight()));
        line.setStrokeWidth(6);
      }
      line.toFront();
      bringNodesToFront();
    }
  }

  private Color getPathColor(int weight) {
    if (weight < 4) {
      return Color.TOMATO;
    }
    if (weight <= 6) {
      return Color.GOLD;
    }
    return Color.MEDIUMSEAGREEN;
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

  private void showError(String header, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(header);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showInfo(String header, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Info");
    alert.setHeaderText(header);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showPathInfo(Path<String> path) {
    StringBuilder message = new StringBuilder();

    message.append("Games:\n");
    for (String node : path.getNodes()) {
      message.append(node).append("\n");
    }

    message.append("\nLinks:\n");
    String current = path.getStart();
    for (Edge<String> edge : path.getEdges()) {
      message.append(current)
          .append(" -> ")
          .append(edge.getDestination())
          .append(" (")
          .append(edge.getName())
          .append(", Similarity Score: ")
          .append(edge.getWeight())
          .append("/10")
          .append(")\n");
      current = edge.getDestination();
    }

    message.append("\nCombined path score: ").append(path.getTotalWeight());

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Path found");
    alert.setHeaderText("Path from " + path.getStart() + " to " + path.getEnd());
    alert.setContentText(message.toString());
    alert.showAndWait();
  }

  private void removeGameView(Pane container, String gameName) {
    StackPane node = nodeViews.get(gameName);
    if (node != null) {
      List<Line> linesToRemove = connectedLines.get(gameName);
      if (linesToRemove != null) {
        for (Line line : new ArrayList<>(linesToRemove)) {
          container.getChildren().remove(line);

          String[] endpoints = lineConnections.get(line);
          if (endpoints != null) {
            String nodeA = endpoints[0];
            String nodeB = endpoints[1];

            if (connectedLines.containsKey(nodeA)) {
              connectedLines.get(nodeA).remove(line);
            }
            if (connectedLines.containsKey(nodeB)) {
              connectedLines.get(nodeB).remove(line);
            }

            edgeLines.remove(nodeA + "->" + nodeB);
            edgeLines.remove(nodeB + "->" + nodeA);
          }
          lineConnections.remove(line);
        }
      }

      container.getChildren().remove(node);
      nodeViews.remove(gameName);
      connectedLines.remove(gameName);
    } else {
      showError("Error", "Game node not found in view.");
    }
  }

  private void addEdgeView(Pane container, String from, String to, String connectionName, int similarityScore) {
    StackPane fromNode = nodeViews.get(from);
    StackPane toNode = nodeViews.get(to);

    if (fromNode == null || toNode == null) {
      showError("Error", "One or both game nodes not found in view");
      return;
    }

    Line line = new Line();
    line.setStrokeWidth(4);
    line.setStroke(Color.DARKSLATEGRAY);

    connectedLines.get(from).add(line);
    connectedLines.get(to).add(line);

    lineConnections.put(line, new String[] { from, to });
    updateLine(line, fromNode, toNode);

    edgeLines.put(from + "->" + to, line);

    Tooltip tooltip = new Tooltip(connectionName + ", Similarity: " + similarityScore);
    Tooltip.install(line, tooltip);

    container.getChildren().add(line);

    for (StackPane node : nodeViews.values()) {
      node.toFront();
    }
  }

  private void createMenuBar(Pane root) {
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem newItem = new MenuItem("New");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem loadItem = new MenuItem("Load");
    MenuItem exitItem = new MenuItem("Exit");

    exitItem.setOnAction(event -> {
      if (checkUnsavedChanges()) {
        Platform.exit();
      }
    });

    newItem.setOnAction(event -> {
      if (!checkUnsavedChanges()) {
        return;
      }

      graphPane.getChildren().clear();
      model.clearGraph();
      nodeViews.clear();
      connectedLines.clear();
      lineConnections.clear();
      edgeLines.clear();
      gameImages.clear();

      hasUnsavedChanges = false;
    });

    saveItem.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showSaveDialog(root.getScene().getWindow());

      if (file != null) {
        try (PrintWriter writer = new PrintWriter(file)) {
          for (String game : model.getGames()) {
            StackPane node = nodeViews.get(game);
            double x = node.getLayoutX();
            double y = node.getLayoutY();
            String imgPath = gameImages.getOrDefault(game, "NONE");
            
            writer.println("GAME: " + game + "," + x + "," + y + "," + imgPath);
          }

          for (String game : model.getGames()) {
            for (Edge<String> edge : model.getEdgesFrom(game)) {
              writer.println(
                  "EDGE:" + game + "," + edge.getDestination() + "," + edge.getName() + "," + edge.getWeight());
            }
          }

          hasUnsavedChanges = false;

        } catch (java.io.FileNotFoundException e) {
          showError("Fel", "Kunde inte hitta filen: " + e.getMessage());
        }
      }
    });

    loadItem.setOnAction(event -> {
      if (!checkUnsavedChanges()) {
        return;
      }

      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showOpenDialog(root.getScene().getWindow());

      if (file != null) {
        try {
          List<String> lines = new ArrayList<>();
          try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
              lines.add(scanner.nextLine());
            }
          }

          graphPane.getChildren().clear();
          model.clearGraph();
          nodeViews.clear();
          connectedLines.clear();
          lineConnections.clear();
          edgeLines.clear();
          gameImages.clear();

          for (String line : lines) {
            if (line.startsWith("GAME: ")) {
              String data = line.replace("GAME: ", "").trim();
              String[] nodeParts = data.split(",");

              String game = nodeParts[0];
              double x = Double.parseDouble(nodeParts[1]);
              double y = Double.parseDouble(nodeParts[2]);
              
              String imagePath = "NONE";
              if (nodeParts.length > 3) {
                imagePath = nodeParts[3];
              }

              model.addGame(game);
              StackPane node = createGameNode(game, game, getGameColor(game), x, y, imagePath);
              nodeViews.put(game, node);
              connectedLines.put(game, new ArrayList<>());
              graphPane.getChildren().add(node);
              node.toFront();
            }
          }

          for (String line : lines) {
            if (line.startsWith("EDGE:")) {
              String data = line.replace("EDGE:", "").trim();
              String[] edgeParts = data.split(",");

              String from = edgeParts[0];
              String to = edgeParts[1];
              String name = edgeParts[2];
              int weight = Integer.parseInt(edgeParts[3].trim());

              if (nodeViews.containsKey(from) && nodeViews.containsKey(to)) {
                if (edgeLines.containsKey(from + "->" + to) || edgeLines.containsKey(to + "->" + from)) {
                  continue;
                }
                model.connectGames(from, to, name, weight);
                addEdgeView(graphPane, from, to, name, weight);
              }
            }
          }

          hasUnsavedChanges = false;

        } catch (Exception e) {
          showError("Fel vid laddning", e.getMessage());
          e.printStackTrace();
        }
      }
    });

    fileMenu.getItems().addAll(newItem, saveItem, loadItem, exitItem);

    Menu AlgorithmMenu = new Menu("Algorithm");
    MenuItem BFSItem = new MenuItem("BFS");
    MenuItem DFSItem = new MenuItem("DFS");

    BFSItem.setOnAction(event -> {
      model.setSearchAlgorithm(GameGraphModel.SearchAlgorithm.BFS);
      showInfo("Algorithm changed", "Using BFS");
    });

    DFSItem.setOnAction(event -> {
      model.setSearchAlgorithm(GameGraphModel.SearchAlgorithm.DFS);
      showInfo("Algorithm changed", "Using DFS");
    });

    AlgorithmMenu.getItems().addAll(BFSItem, DFSItem);

    menuBar.getMenus().addAll(fileMenu, AlgorithmMenu);
    menuBar.setLayoutX(10);
    menuBar.setLayoutY(10);
    root.getChildren().add(menuBar);
  }

  private Map<String, double[]> createInitialPositions() {
    Map<String, double[]> positions = new HashMap<>();

    positions.put("Bloodborne", new double[] { 150, 100 });
    positions.put("Dark Souls", new double[] { 350, 200 });
    positions.put("Sekiro", new double[] { 550, 100 });
    positions.put("Elden Ring", new double[] { 350, 20 });
    positions.put("Lies of P", new double[] { 350, 380 });

    positions.put("Minecraft", new double[] { 150, 550 });
    positions.put("Terraria", new double[] { 350, 650 });
    positions.put("Valheim", new double[] { 550, 550 });
    positions.put("Ark", new double[] { 350, 450 });
    positions.put("The Forest", new double[] { 350, 820 });

    positions.put("Hollow Knight", new double[] { 850, 250 });
    positions.put("Blasphemous", new double[] { 1050, 350 });
    positions.put("Dead Cells", new double[] { 850, 450 });
    positions.put("Ori and the blind forest", new double[] { 650, 350 });
    positions.put("Celeste", new double[] { 850, 620 });

    positions.put("Skyrim", new double[] { 1350, 150 });
    positions.put("The Witcher 3", new double[] { 1350, 350 });
    positions.put("CyberPunk 2077", new double[] { 1350, 550 });
    positions.put("Fallout 4", new double[] { 1550, 250 });
    positions.put("Starfield", new double[] { 1550, 450 });

    return positions;
  }
}