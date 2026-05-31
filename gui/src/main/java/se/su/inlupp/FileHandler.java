package se.su.inlupp;
import java.io.*;
import java.util.*;

public class FileHandler {

    public void save(File file, Collection<String> games, Map<String, javafx.scene.layout.StackPane> nodeViews, 
                     Map<String, String> gameImages, GameGraphModel model) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (String game : games) {
                var node = nodeViews.get(game);
                String imgPath = gameImages.getOrDefault(game, "NONE");
                writer.println("GAME: " + game + "," + node.getLayoutX() + "," + node.getLayoutY() + "," + imgPath);
            }
            for (String game : games) {
                for (Edge<String> edge : model.getEdgesFrom(game)) {
                    writer.println("EDGE:" + game + "," + edge.getDestination() + "," + edge.getName() + "," + edge.getWeight());
                }
            }
        }
    }

    public List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
        }
        return lines;
    }
}