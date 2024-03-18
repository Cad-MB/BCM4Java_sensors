package visualization;

import cvm.CVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import requests.Position;

import java.io.Serializable;
import java.util.*;


public class Visualisation
    extends Application {

    public static final HashMap<String, NodeInfoI> nodeInfoMap = new HashMap<>();
    private static final HashMap<String, Set<SensorDataI>> sensorData = new HashMap<>();
    private static final HashMap<String, Color> nodeColors = new HashMap<>();
    private static final HashMap<Color, Integer> colors = new HashMap<>();
    private static final Set<SensorDataI> sensorDataAll = new HashSet<>();
    private static Map<String, ProcessingNodeI> processingNodeMap = new HashMap<>();
    boolean darkMode = true;

    public static void main(String[] args) throws Exception {
        processingNodeMap = Collections.synchronizedMap(processingNodeMap);
        CVM cvm = new CVM(sensorDataAll, sensorData);

        new Thread(() -> {
            cvm.startStandardLifeCycle(20000000L);
            Platform.exit();
            System.exit(0);
        }).start();

        launch(Visualisation.class, args);
    }


    public static synchronized ProcessingNodeI getProcessingNode(String id) {
        return processingNodeMap.get(id);
    }

    public static synchronized void addProcessingNode(String id, ProcessingNodeI pn) {
        processingNodeMap.put(id, pn);

        if (nodeColors.isEmpty()) {
            initColors();
        }
        nodeColors.put(id, getNextColor());
    }


    Canvas canvas;
    HashMap<String, Rectangle> nodeBounds = new HashMap<>();
    String focusedNodeId = "";

    static void initColors() {
        colors.put(Color.BLUE, 0);
        colors.put(Color.PURPLE, 0);
        colors.put(Color.HOTPINK, 0);
        colors.put(Color.INDIANRED, 0);
        colors.put(Color.ORANGE, 0);
        colors.put(Color.GOLD, 0);
        colors.put(Color.TURQUOISE, 0);
    }

    static Color getNextColor() {
        // noinspection OptionalGetWithoutIsPresent
        Color color = colors.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
        colors.put(color, colors.get(color) + 1);
        return color;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CVM.SensorRandomizer randomizer = new CVM.SensorRandomizer(sensorDataAll);

        primaryStage.setTitle("Graphics");

        canvas = new Canvas(4000, 4000);
        canvas.setScaleX(1);
        canvas.setScaleY(1);

        Group root = new Group();
        root.getChildren().add(canvas);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setVvalue(.42);
        scrollPane.setHvalue(.65);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        if (darkMode) {
            scrollPane.setStyle("-fx-background: rgb(74, 77, 74)");
        } else {
            scrollPane.setStyle("-fx-background: rgb(196, 247, 195)");
        }

        primaryStage.setScene(new Scene(scrollPane));
        primaryStage.setHeight(800);
        primaryStage.setWidth(1200);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            randomizer.interrupt();
            Platform.exit();
            System.exit(0);
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();
        draw(gc);

        Tooltip tooltip = new Tooltip();
        tooltip.setAutoHide(false);
        Tooltip.install(canvas, tooltip);

        canvas.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.isShiftDown()) {
                scrollPane.setHvalue(scrollPane.getHvalue() - (event.getDeltaY() / 10000));
                event.consume();
            }
        });

        canvas.requestFocus();
        canvas.setOnMouseMoved(e -> {
            e.consume();
            if (focusedNodeId.isEmpty()) {
                tooltip.hide();
                return;
            }
            for (Map.Entry<String, Rectangle> entry : nodeBounds.entrySet()) {
                String id = entry.getKey();
                Rectangle bounds = entry.getValue();
                if (bounds.contains(e.getX(), e.getY())) {
                    Point2D p = canvas.localToScreen(0, 0);
                    tooltip.setText(tooltipStr(id));
                    tooltip.show(primaryStage, p.getX() + e.getX() * canvas.getScaleX() + 10,
                                 p.getY() + e.getY() * canvas.getScaleY() + 10);
                    return;
                }
            }
            tooltip.hide();
        });
        canvas.setOnMouseClicked((MouseEvent e) -> {
            canvas.requestFocus();
            if (e.getButton() != MouseButton.SECONDARY) {
                return;
            }
            for (Map.Entry<String, Rectangle> entry : nodeBounds.entrySet()) {
                String id = entry.getKey();
                Rectangle bounds = entry.getValue();
                if (bounds.contains(e.getX(), e.getY())) {
                    focusedNodeId = id;
                    return;
                }
            }
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

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ae -> {
            try {
                draw(gc);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.focusedProperty().addListener(obv -> tooltip.hide());

        randomizer.setCallback((sensorId, sData) -> {
            if (focusedNodeId.equals(((SensorDataI) sData).getNodeIdentifier())) {
                Platform.runLater(() -> tooltip.setText(tooltipStr(focusedNodeId)));
            }
        });
        randomizer.start();
    }

    void drawAxis(GraphicsContext gc) {
        // y
        if (darkMode) gc.setStroke(Color.GRAY);
        else gc.setStroke(Color.GRAY);

        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        // x
        gc.strokeLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);
    }


    public void draw(GraphicsContext gc) throws InterruptedException {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawAxis(gc);
        synchronized (nodeInfoMap) {
            for (NodeInfoI nodeInfo : nodeInfoMap.values()) {
                ProcessingNodeI processingNode = getProcessingNode(nodeInfo.nodeIdentifier());

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

                nodeBounds.put(nodeInfo.nodeIdentifier(), new Rectangle(x - 5, y - 5, 10, 10));

                processingNode.getNeighbours().forEach(neighbour -> {
                    Position nPos = (Position) neighbour.nodePosition();
                    double ny = (canvas.getHeight() / 2) - nPos.getY();
                    double nx = (canvas.getWidth() / 2) + nPos.getX();
                    gc.strokeLine(x, y, nx, ny);
                });
            }
        }
    }

    String tooltipStr(String nodeId) {
        StringBuilder sb = new StringBuilder();

        Position pos;
        synchronized (nodeInfoMap) {
            NodeInfoI nodeInfo = nodeInfoMap.get(nodeId);
            if (nodeInfo == null) {
                return "";
            }
            pos = (Position) nodeInfo.nodePosition();
        }
        String title = String.format("%s (%.2f, %.2f)", nodeId, pos.getX(), pos.getY());
        sb.append(title).append("\n\n");

        for (SensorDataI sensorData : sensorData.get(nodeId)) {
            Serializable value = sensorData.getValue();
            assert value instanceof Boolean || value instanceof Number;

            String format = value instanceof Boolean
                ? value.toString()
                : String.format("%.2f", ((Number) value).doubleValue());

            sb.append(sensorData.getSensorIdentifier())
              .append(" : ")
              .append(format)
              .append("\n");
        }

        return sb.toString();
    }

}
