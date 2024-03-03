import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import requests.NodeInfo;
import requests.Position;
import requests.SensorData;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;


public class Visualisation
    extends Application {

    public static void main(String[] args) {
        launch(Visualisation.class, args);
    }

    HashMap<String, Set<SensorDataI>> sensorData = new HashMap<>();
    HashMap<String, NodeInfoI> nodeInfos = new HashMap<>();

    Stage primaryStage;
    Canvas canvas;
    HashMap<String, Rectangle> tooltipBounds = new HashMap<>();
    HashMap<String, Color> nodeColors = new HashMap<>();
    String focusedNodeId = "";

    HashMap<Color, Integer> colors = new HashMap<>();

    void initColors() {
        colors.put(Color.BLUE, 0);
        colors.put(Color.PURPLE, 0);
        colors.put(Color.HOTPINK, 0);
        colors.put(Color.BROWN, 0);
        colors.put(Color.ORANGE, 0);
        colors.put(Color.GOLD, 0);
        colors.put(Color.TURQUOISE, 0);
    }

    Color getNextColor() {
        // noinspection OptionalGetWithoutIsPresent
        Color color = colors.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
        colors.put(color, colors.get(color) + 1);
        return color;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initColors();

        URL fileUrl = getClass().getClassLoader().getResource("json/foret2_test.json");
        assert fileUrl != null;
        ArrayList<ParsedData.Node> nodeDataList = JsonParser.parse(new File(fileUrl.toURI()));
        Set<SensorDataI> sensorDataAll = new HashSet<>();
        for (ParsedData.Node parsedData : nodeDataList) {
            Position nodePos = new Position(parsedData.position.x, parsedData.position.y);
            NodeInfo nodeInfo = new NodeInfo(parsedData.range, parsedData.id, nodePos);
            for (ParsedData.Sensor parsedSensor : parsedData.sensors) {
                if (!sensorData.containsKey(nodeInfo.nodeIdentifier())) {
                    sensorData.put(nodeInfo.nodeIdentifier(), new HashSet<>());
                }

                SensorData<Float> sData = new SensorData<>(
                    nodeInfo.nodeIdentifier(),
                    parsedSensor.id,
                    parsedSensor.value,
                    Instant.now()
                );
                sensorData.get(nodeInfo.nodeIdentifier()).add(sData);
                sensorDataAll.add(sData);
            }
            nodeInfos.put(nodeInfo.nodeIdentifier(), nodeInfo);
            nodeColors.put(nodeInfo.nodeIdentifier(), getNextColor());
        }

        CVM.SensorRandomizer randomizer = new CVM.SensorRandomizer(sensorDataAll);


        this.primaryStage = primaryStage;
        primaryStage.setTitle("Graphics");

        canvas = new Canvas(5000, 5000);
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        Group root = new Group();
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setVvalue(.5);
        scrollPane.setHvalue(.5);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // drawAxis(gc);
        draw(gc);

        // drawCircles(root);

        root.getChildren().add(canvas);
        root.getChildren().add(new Circle());
        primaryStage.setScene(new Scene(scrollPane));
        primaryStage.setHeight(800);
        primaryStage.setWidth(1200);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            randomizer.interrupt();
            Platform.exit();
            System.exit(0);
        });

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.INDEFINITE);
        tooltip.setAutoHide(false);
        Tooltip.install(canvas, tooltip);

        canvas.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.isShiftDown()) {
                scrollPane.setHvalue(scrollPane.getHvalue() - (event.getDeltaY() / 10000));
                event.consume();
            }
        });

        canvas.requestFocus();
        canvas.addEventFilter(MouseEvent.ANY, (e) -> canvas.requestFocus());
        canvas.setOnMouseMoved(e -> {
            for (Map.Entry<String, Rectangle> entry : tooltipBounds.entrySet()) {
                String id = entry.getKey();
                Rectangle bounds = entry.getValue();
                if (bounds.contains(e.getX(), e.getY())) {
                    Point2D p = canvas.localToScreen(0, 0);
                    tooltip.setText(tooltipStr(id));
                    tooltip.show(primaryStage, p.getX() + e.getX() * canvas.getScaleX(),
                                 p.getY() + e.getY() * canvas.getScaleY());
                    focusedNodeId = id;
                    return;
                }
            }
            tooltip.hide();
            focusedNodeId = "";
        });

        canvas.setOnKeyReleased(e -> {
            if (e.isControlDown()) {
                if (e.getText().equals("+")) {
                    canvas.setScaleX(canvas.getScaleX() + 0.05);
                    canvas.setScaleY(canvas.getScaleY() + 0.05);
                }
                if (e.getText().equals("-")) {
                    canvas.setScaleX(canvas.getScaleX() - 0.05);
                    canvas.setScaleY(canvas.getScaleY() - 0.05);
                }
                e.consume();
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ae -> draw(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.focusedProperty().addListener(obv -> tooltip.hide());

        randomizer.setCallback((sensorId, sData) -> {
            if (focusedNodeId.equals(sData.getNodeIdentifier())) {
                Platform.runLater(() -> tooltip.setText(tooltipStr(focusedNodeId)));
            }
        });
        randomizer.start();
    }

    void drawAxis(GraphicsContext gc) {
        // y
        gc.setStroke(Color.GRAY);
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        // x
        gc.strokeLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);
    }

    public void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawAxis(gc);
        for (NodeInfoI nodeInfo : nodeInfos.values()) {
            Position position = (Position) nodeInfo.nodePosition();
            double y = (canvas.getHeight() / 2) - position.getY();
            double x = (canvas.getWidth() / 2) + position.getX();

            if (!focusedNodeId.isEmpty() && !focusedNodeId.equals(nodeInfo.nodeIdentifier())) {
                gc.setStroke(Color.LIGHTGRAY);
                gc.setFill(Color.LIGHTGRAY);
            } else {
                gc.setStroke(nodeColors.get(nodeInfo.nodeIdentifier()));
                gc.setFill(nodeColors.get(nodeInfo.nodeIdentifier()));
            }

            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.strokeOval(x - nodeInfo.nodeRange(), y - nodeInfo.nodeRange(), nodeInfo.nodeRange() * 2,
                          nodeInfo.nodeRange() * 2);
            Text idText = new Text(nodeInfo.nodeIdentifier());
            gc.fillText(nodeInfo.nodeIdentifier(), x - (idText.getBoundsInLocal().getWidth() / 2), y - 15);

            tooltipBounds.put(nodeInfo.nodeIdentifier(), new Rectangle(x - 5, y - 5, 10, 10));
        }
    }

    String tooltipStr(String nodeId) {
        StringBuilder sb = new StringBuilder();

        Position pos = (Position) nodeInfos.get(nodeId).nodePosition();
        String title = String.format("%s (%.2f, %.2f)", nodeId, pos.getX(), pos.getY());
        sb.append(title).append("\n\n");

        for (SensorDataI sensorData : sensorData.get(nodeId)) {
            sb.append(sensorData.getSensorIdentifier())
              .append(" : ")
              .append(String.format("%.2f", sensorData.getValue()))
              .append("\n");
        }

        return sb.toString();
    }

}
