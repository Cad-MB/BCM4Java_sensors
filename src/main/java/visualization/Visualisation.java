package visualization;

import cvm.CVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import sensor_network.Position;
import sensor_network.requests.ProcessingNode;
import visualization.panes.InfoPane;
import visualization.panes.PanePosition;
import visualization.panes.RequestPane;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Visualisation
    extends Application {

    private static final Map<String, NodeInfoI> nodeInfos = new ConcurrentHashMap<>();
    private static final HashMap<String, Color> nodeColors = new HashMap<>();
    private static final HashMap<Color, Integer> colorCounts = new HashMap<>();
    private static final Map<String, ProcessingNodeI> processingNodes = new ConcurrentHashMap<>();
    private static final HashMap<String, Rectangle> nodeBounds = new HashMap<>();
    private static final Map<String, List<String>> requests = new ConcurrentHashMap<>();
    private static final Map<String, Color> requestColors = new ConcurrentHashMap<>();
    private static final boolean darkMode = true;
    private static ScrollPane scrollPane;
    private static RequestPane requestPane;
    private CVM cvm;
    private String configName;
    private static Canvas canvas;
    private static String focusedNodeId = "";
    private static String focusedRequestId = "";
    private static InfoPane infoPane;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("missing config name");
            System.exit(1);
        }

        System.setProperty("javax.xml.accessExternalDTD", "all");
        Thread.currentThread().setName("Visualisation main");
        launch(Visualisation.class, args);
    }


    public static ProcessingNodeI getProcessingNode(String id) {
        return processingNodes.get(id);
    }

    public static void addProcessingNode(String id, ProcessingNodeI pn) {
        processingNodes.put(id, pn);
        if (nodeColors.isEmpty()) {
            initColors();
        }
        nodeColors.put(id, getNextColor());
    }


    public static void addNodeInfo(String nodeId, NodeInfoI nodeInfo) {
        nodeInfos.put(nodeId, nodeInfo);
        draw(canvas.getGraphicsContext2D());
    }

    public static void removeNodeInfo(String nodeId) {
        nodeInfos.remove(nodeId);
        draw(canvas.getGraphicsContext2D());
    }

    public static void addRequest(String uri, String nodeId) {
        List<String> nodeIds = requests.get(uri);
        if (nodeIds != null) {
            requests.get(uri).add(nodeId);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(nodeId);
            requests.put(uri, list);
            try {
                Platform.runLater(() -> requestPane.addRequestButton(uri, (e) -> {
                    focusedNodeId = "";
                    focusedRequestId = uri;
                    draw(canvas.getGraphicsContext2D());
                }));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        requestColors.put(uri, getNextColor());
    }


    static void initColors() {
        colorCounts.put(Color.LIGHTBLUE, 0);
        colorCounts.put(Color.PURPLE, 0);
        colorCounts.put(Color.HOTPINK, 0);
        colorCounts.put(Color.INDIANRED, 0);
        colorCounts.put(Color.ORANGE, 0);
        colorCounts.put(Color.GOLD, 0);
        colorCounts.put(Color.TURQUOISE, 0);
    }

    static Color getNextColor() {
        // noinspection OptionalGetWithoutIsPresent
        Color color = colorCounts.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
        colorCounts.put(color, colorCounts.get(color) + 1);
        return color;
    }

    void resetCVM() throws Exception {
        cvm = new CVM(this.configName);
        Thread cvmThread = new Thread(() -> {
            cvm.startStandardLifeCycle(20000000L);
            Platform.exit();
            System.exit(0);
        });
        cvmThread.start();
    }

    void setupCanvas(ScrollPane pane) {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        canvas.setOnScroll(event -> {
            if (event.isShiftDown()) {
                pane.setHvalue(pane.getHvalue() - ((event.getDeltaY() / 10000) * 3));
                event.consume();
            }
        });

        canvas.requestFocus();
        canvas.setOnMouseClicked((MouseEvent e) -> {
            canvas.requestFocus();
            if (e.getButton() != MouseButton.SECONDARY) {
                return;
            }
            focusedRequestId = "";
            for (Map.Entry<String, Rectangle> entry : nodeBounds.entrySet()) {
                String id = entry.getKey();
                Rectangle bounds = entry.getValue();
                if (bounds.contains(e.getX(), e.getY())) {
                    focusedNodeId = id;
                    infoPane.setText(id, tooltipStr(id));
                    draw(canvas.getGraphicsContext2D());
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
                if (e.getCode() == KeyCode.R) {
                    try {
                        resetCVM();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                e.consume();
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parameters parameters = getParameters();
        this.configName = parameters.getRaw().get(0);
        resetCVM();

        Group root = new Group();
        scrollPane = new ScrollPane(root);
        scrollPane.setVvalue(.42);
        scrollPane.setHvalue(.65);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));

        if (darkMode) {
            scrollPane.setStyle("-fx-background: #303030");
        } else {
            scrollPane.setStyle("-fx-background: white");
        }

        double canvasWidth = 4000;
        double canvasHeight = 4000;
        AxisCanvas axisCanvas = new AxisCanvas(canvasWidth, canvasHeight);
        canvas = new Canvas(canvasWidth, canvasHeight);
        Color paneBg = Color.rgb(26, 26, 26, 0.8);
        infoPane = new InfoPane(200, 250, PanePosition.BOTTOM_RIGHT, Color.WHITE, paneBg);
        requestPane = new RequestPane(200, 250, PanePosition.BOTTOM_LEFT, Color.WHITE, paneBg);

        root.getChildren().addAll(axisCanvas, canvas, infoPane, requestPane);

        Scene scene = new Scene(scrollPane);
        primaryStage.setTitle("Graphics");
        primaryStage.setScene(scene);
        primaryStage.setHeight(800);
        primaryStage.setWidth(1200);
        primaryStage.setOnShown(e -> requestPane.updateViewportStyle());
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

        draw(canvas.getGraphicsContext2D());

        setupTimeLines(canvas.getGraphicsContext2D(), infoPane, requestPane);
        setupCanvas(scrollPane);
    }

    private void setupTimeLines(GraphicsContext gc, InfoPane infoPane, RequestPane requestPane) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            requestPane.updatePosition(scrollPane);
            infoPane.updatePosition(scrollPane);
            draw(gc);
            if (focusedNodeId.isEmpty()) {
                // force refresh on scroll
                infoPane.setText("", InfoPane.DEFAULT_TEXT + " ");
                infoPane.setText("", InfoPane.DEFAULT_TEXT);
            }
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    public static void draw(GraphicsContext gc) {
        Collection<NodeInfoI> nodeInfos = Visualisation.nodeInfos.values();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (NodeInfoI nodeInfo : nodeInfos) {
            ProcessingNodeI processingNode = getProcessingNode(nodeInfo.nodeIdentifier());
            List<String> requestNodeIds = requests.get(focusedRequestId);

            Position position = (Position) nodeInfo.nodePosition();
            double y = (canvas.getHeight() / 2) - position.getY();
            double x = (canvas.getWidth() / 2) + position.getX();

            if (!focusedNodeId.isEmpty() && !focusedNodeId.equals(nodeInfo.nodeIdentifier())) {
                gc.setStroke(Color.GRAY);
                gc.setFill(Color.GRAY);
                gc.setLineWidth(1);
            } else if (!focusedRequestId.isEmpty()) {
                if (requestNodeIds != null && requestNodeIds.contains(nodeInfo.nodeIdentifier())) {
                    gc.setStroke(requestColors.get(focusedRequestId));
                    gc.setFill(requestColors.get(focusedRequestId));
                    gc.setLineWidth(2);
                } else {
                    gc.setStroke(Color.GRAY);
                    gc.setFill(Color.GRAY);
                    gc.setLineWidth(1);
                }
            } else {
                gc.setStroke(nodeColors.get(nodeInfo.nodeIdentifier()));
                gc.setFill(nodeColors.get(nodeInfo.nodeIdentifier()));
                if (!focusedNodeId.isEmpty()) gc.setLineWidth(5);
                else gc.setLineWidth(2);
                if (focusedNodeId.equals(nodeInfo.nodeIdentifier())) {
                    gc.strokeOval(x - nodeInfo.nodeRange(), y - nodeInfo.nodeRange(), nodeInfo.nodeRange() * 2,
                                  nodeInfo.nodeRange() * 2);
                }
            }

            gc.fillOval(x - 5, y - 5, 10, 10);

            Text idText = new Text(nodeInfo.nodeIdentifier());
            gc.fillText(nodeInfo.nodeIdentifier(), x - (idText.getBoundsInLocal().getWidth() / 2), y - 15);

            if (!nodeBounds.containsKey(nodeInfo.nodeIdentifier())) {
                nodeBounds.put(nodeInfo.nodeIdentifier(), new Rectangle(x - 5, y - 5, 10, 10));
            }

            Set<NodeInfoI> neighbours = processingNode.getNeighbours();
            if (!focusedRequestId.isEmpty()) {
                if (requestNodeIds == null) {
                    neighbours = new HashSet<>();
                } else {
                    neighbours = neighbours.stream()
                                           .filter(neighbour -> requestNodeIds.contains(neighbour.nodeIdentifier()))
                                           .collect(Collectors.toSet());
                }
            }
            neighbours.forEach(neighbour -> {
                Position nPos = (Position) neighbour.nodePosition();
                double ny = (canvas.getHeight() / 2) - nPos.getY();
                double nx = (canvas.getWidth() / 2) + nPos.getX();
                gc.strokeLine(x, y, nx, ny);
            });
        }

    }

    String tooltipStr(String nodeId) {
        StringBuilder sb = new StringBuilder();

        Position pos;
        NodeInfoI nodeInfo = nodeInfos.get(nodeId);
        if (nodeInfo == null) {
            focusedNodeId = "";
            return "";
        }
        pos = (Position) nodeInfo.nodePosition();
        String title = String.format("%s (%.2f, %.2f)", nodeId, pos.getX(), pos.getY());
        sb.append(title).append("\n");

        sb.append("\nsensors:\n");
        ProcessingNode pn = (ProcessingNode) processingNodes.get(nodeId);
        try {
            Field sensorDataField = pn.getClass().getDeclaredField("sensorData");
            sensorDataField.setAccessible(true);
            // noinspection unchecked
            for (SensorDataI sensorData : ((HashMap<String, SensorDataI>) sensorDataField.get(pn)).values()) {
                Serializable value = sensorData.getValue();
                assert value instanceof Boolean || value instanceof Number;

                String format = value instanceof Boolean
                    ? value.toString()
                    : String.format("%.2f", ((Number) value).doubleValue());

                sb.append(" - ").append(sensorData.getSensorIdentifier()).append(" : ").append(format).append("\n");
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        sb.append("\nneighbours:\n");
        getProcessingNode(nodeId).getNeighbours().forEach(neighbour -> {
            PositionI nPos = neighbour.nodePosition();
            sb.append(" - ")
              .append(neighbour.nodeIdentifier())
              .append(" : ")
              .append(pos.directionFrom(nPos))
              .append(" : ")
              .append(pos.distance(nPos))
              .append('\n');
        });

        return sb.toString();
    }

}
