package com.example.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Controller {
    public ArrayList<Town> towns = new ArrayList<>();
    public HashMap<Circle, Town> circles = new HashMap<>();
    public ArrayList<ArrayList<Pair<Integer, Float>>> roads = new ArrayList<>();
    public ArrayList<Circle> clicked = new ArrayList<>();
    public ArrayList<Line> lines = new ArrayList<>();

    @FXML
    public AnchorPane root;
    public Label distance;
    public Button runButton;
    public VBox pathVBox;
    public Label timeComp;
    public Label spaceComp;

    @FXML
    public void readFiles(ActionEvent event) {
        towns.add(null);
        String line = "";
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader("Towns.csv"));
            while ((line = br.readLine()) != null) {
                String[] t = line.split(splitBy);
                towns.add(new Town(Integer.parseInt(t[0]), t[1], Integer.parseInt(t[2]), Integer.parseInt(t[3])));
                addCircleOnMap(t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < towns.size(); i++) {
            roads.add(new ArrayList<>());
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader("Roads.csv"));
            while ((line = br.readLine()) != null) {
                String[] r = line.split(splitBy);
                roads.get(Integer.parseInt(r[0])).add(new Pair<>(Integer.parseInt(r[1]), Float.parseFloat(r[2])));
                roads.get(Integer.parseInt(r[1])).add(new Pair<>(Integer.parseInt(r[0]), Float.parseFloat(r[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button b = (Button) event.getSource();
        b.setDisable(true);
    }

    private void addCircleOnMap(String[] t) {
        Circle c = new Circle();
        c.setCenterX(Integer.parseInt(t[2]) * 0.8);
        c.setCenterY(Integer.parseInt(t[3]) * 0.8);
        c.setRadius(20);
        c.setStyle("-fx-opacity: 35%");
        root.getChildren().add(c);
        circles.put(c, towns.get(Integer.parseInt(t[0])));
    }

    @FXML
    void CircleClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        if (node instanceof Circle) {
            if (clicked.size() == 2) {
                clicked.get(0).setStyle("-fx-opacity: 25%;");
                clicked.remove(0);
            }
            node.setStyle("-fx-opacity: 60%;");
            clicked.add((Circle) node);
            if (clicked.size() == 2 && clicked.get(0) == clicked.get(1)) {
                clicked.remove(1);
            }
        }
        runButton.setDisable(clicked.size() != 2);
    }

    @FXML
    public void run() {

        initializeElements();

        ArrayList<Town> paths = new ArrayList<>();
        for (int i = 0; i < towns.size(); i++) {
            paths.add(null);
        }

        PriorityQueue<Town> checked = new PriorityQueue<>();
        PriorityQueue<Town> waiting = new PriorityQueue<>();

        // clicked circles -> circles objects are keys in the hashmap
        Town start = circles.get(clicked.get(0));
        Town target = circles.get(clicked.get(1));

        // real distance G(x) at the starting point is 0, F(x) is G(x) + heuristic
        Town.GofX.set(start.id, 0.0F);
        Town.FofX.set(start.id, Town.GofX.get(start.id) + start.hDistance(target));

        // these variables to store time and space complexity
        int maxSpace = 0;
        int time = 0;

        waiting.add(start);
        while (!waiting.isEmpty()) {
            maxSpace = Math.max(maxSpace, waiting.size() + checked.size());

            Town here = waiting.peek();
            if (here == target) {
                distance.setText(Town.GofX.get(target.id).toString());
                spaceComp.setText(Integer.toString(maxSpace));
                timeComp.setText(Integer.toString(time));
                printPath(paths, target);
                return;
            }

            for (Pair<Integer, Float> road : roads.get(here.id)) {
                time++; // for each road (neighbour we check, the time is added by 1)

                // in road Pair > the key is the town id, the value is the distance
                Town neighbor = towns.get(road.getKey());
                float totalWeight = Town.GofX.get(here.id) + road.getValue();

                if (!waiting.contains(neighbor) && !checked.contains(neighbor)) {
                    paths.set(neighbor.id, here);
                    Town.GofX.set(neighbor.id, totalWeight);
                    Town.FofX.set(neighbor.id, Town.GofX.get(neighbor.id) + neighbor.hDistance(target));
                    waiting.add(neighbor);
                } else {
                    if (totalWeight < Town.GofX.get(neighbor.id)) {
                        paths.set(neighbor.id, here);
                        Town.GofX.set(neighbor.id, totalWeight);
                        Town.FofX.set(neighbor.id, Town.GofX.get(neighbor.id) + neighbor.hDistance(target));

                        if (checked.contains(neighbor)) {
                            checked.remove(neighbor);
                            waiting.add(neighbor);
                        }
                    }
                }
            }
            waiting.remove(here);
            checked.add(here);
        }
    }

    private void initializeElements() {
        distance.setText("0.0");
        timeComp.setText("00");
        spaceComp.setText("00");
        for (int i = pathVBox.getChildren().size() - 1; i >= 2; i--) {
            pathVBox.getChildren().remove(i);
        }

        for (Line line : lines) {
            root.getChildren().remove(line);
        }
        lines.clear();

        Town.FofX.clear();
        Town.GofX.clear();
        for (int i = 0; i < towns.size(); i++) {
            Town.FofX.add(Float.MAX_VALUE);
            Town.GofX.add(Float.MAX_VALUE);
        }
    }

    private void printPath(ArrayList<Town> paths, Town target) {
        if (target == null) return;

        printPath(paths, paths.get(target.id));
        putTownOnPath(target);

        Town toGo = paths.get(target.id);
        if (toGo != null) {
            drawLine(target, toGo);
        }
    }

    private void putTownOnPath(Town target) {
        Label town = new Label(target.name);
        town.setStyle("-fx-alignment: center;");
        town.setTextFill(Color.web("94753d"));
        town.setPrefWidth(176.0);
        pathVBox.getChildren().add(town);
    }

    private void drawLine(Town from, Town toGo) {
        Line line = new Line();
        line.setStartX(from.x * 0.8);
        line.setStartY(from.y * 0.8);
        line.setEndX(toGo.x * 0.8);
        line.setEndY(toGo.y * 0.8);
        line.setStrokeWidth(2.2);
        lines.add(line);
        root.getChildren().add(line);
    }
}