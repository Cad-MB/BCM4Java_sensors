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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
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
    private StackPane infoPane;
    private ScrollPane scrollPane;

    CVM cvm;

    boolean darkMode = true;
    private String configName;

    public static void main(String[] args) {
        processingNodeMap = Collections.synchronizedMap(processingNodeMap);

        if (args.length < 1) {
            System.out.println("missing config name");
            System.exit(1);
        }

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
        colors.put(Color.LIGHTBLUE, 0);
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

    void resetCVM() throws Exception {
        cvm = new CVM(sensorDataAll, sensorData, this.configName);
        Thread cvmThread = new Thread(() -> {
            cvm.startStandardLifeCycle(20000000L);
            Platform.exit();
            System.exit(0);
        });
        cvmThread.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parameters parameters = getParameters();
        this.configName = parameters.getRaw().get(0);
        resetCVM();

        CVM.SensorRandomizer randomizer = new CVM.SensorRandomizer(sensorDataAll);

        primaryStage.setTitle("Graphics");

        Group root = new Group();
        scrollPane = new ScrollPane(root);
        scrollPane.setVvalue(.42);
        scrollPane.setHvalue(.65);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.vvalueProperty().addListener(e -> this.updateInfoPane());
        scrollPane.hvalueProperty().addListener(e -> this.updateInfoPane());

        if (darkMode) {
            scrollPane.setStyle("-fx-background: rgb(74, 77, 74)");
        } else {
            scrollPane.setStyle("-fx-background: rgb(196, 247, 195)");
        }

        canvas = new Canvas(4000, 4000);
        canvas.setScaleX(1);
        canvas.setScaleY(1);

        infoPane = new StackPane();

        Text infoText = new Text("Right click on a node to focus");
        infoText.setStroke(Color.WHITE);
        Rectangle infoRect = new Rectangle(infoText.getLayoutBounds().getWidth() + 20, 250, Color.rgb(48, 48, 48, 0.8));
        infoRect.setArcHeight(10);
        infoRect.setArcWidth(10);

        infoPane.getChildren().add(infoRect);
        infoPane.getChildren().add(infoText);

        infoPane.setLayoutX(-scrollPane.getViewportBounds().getMinX());
        infoPane.setLayoutY(-scrollPane.getViewportBounds().getMinY());

        root.getChildren().add(canvas);
        root.getChildren().add(infoPane);

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

        canvas.setOnScroll(event -> {
            if (event.isShiftDown()) {
                scrollPane.setHvalue(scrollPane.getHvalue() - ((event.getDeltaY() / 10000) * 3));
                event.consume();
            }
        });

        canvas.requestFocus();
        canvas.setOnMouseClicked((MouseEvent e) -> {
            canvas.requestFocus();
            this.updateInfoPane();
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

        timeline(gc, infoText).play();

        randomizer.setCallback((sensorId, sData) -> {
            if (focusedNodeId.equals(((SensorDataI) sData).getNodeIdentifier())) {
                Platform.runLater(() -> infoText.setText(tooltipStr(focusedNodeId)));
            }
        });
        randomizer.start();
    }

    private Timeline timeline(final GraphicsContext gc, final Text infoText) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), ae -> {
            try {
                draw(gc);
                updateInfoPane();
                if (focusedNodeId.isEmpty()) {
                    infoText.setText("Right click on a node to focus ");
                    infoText.setText("Right click on a node to focus");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    void drawAxis(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);

        // y
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        // x
        gc.strokeLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);
    }

    void updateInfoPane() {
        infoPane.setLayoutX(-scrollPane.getViewportBounds().getMinX());
        infoPane.setLayoutY(-scrollPane.getViewportBounds().getMinY());
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
                    gc.setStroke(Color.GRAY);
                    gc.setFill(Color.GRAY);
                } else {
                    gc.setStroke(nodeColors.get(nodeInfo.nodeIdentifier()));
                    gc.setFill(nodeColors.get(nodeInfo.nodeIdentifier()));
                    if (!focusedNodeId.isEmpty()) {
                        gc.setLineWidth(3);
                    }
                }

                gc.fillOval(x - 5, y - 5, 10, 10);
                gc.strokeOval(x - nodeInfo.nodeRange(), y - nodeInfo.nodeRange(),
                              nodeInfo.nodeRange() * 2, nodeInfo.nodeRange() * 2);

                Text idText = new Text(nodeInfo.nodeIdentifier());
                gc.fillText(nodeInfo.nodeIdentifier(), x - (idText.getBoundsInLocal().getWidth() / 2), y - 15);

                if (!nodeBounds.containsKey(nodeInfo.nodeIdentifier())) {
                    nodeBounds.put(nodeInfo.nodeIdentifier(), new Rectangle(x - 5, y - 5, 10, 10));
                }

                processingNode.getNeighbours().forEach(neighbour -> {
                    Position nPos = (Position) neighbour.nodePosition();
                    double ny = (canvas.getHeight() / 2) - nPos.getY();
                    double nx = (canvas.getWidth() / 2) + nPos.getX();
                    gc.strokeLine(x, y, nx, ny);
                });
                gc.setLineWidth(1);
            }
        }
    }

    String tooltipStr(String nodeId) {
        StringBuilder sb = new StringBuilder();

        Position pos;
        synchronized (nodeInfoMap) {
            NodeInfoI nodeInfo = nodeInfoMap.get(nodeId);
            if (nodeInfo == null) {
                focusedNodeId = "";
                return "";
            }
            pos = (Position) nodeInfo.nodePosition();
        }
        String title = String.format("%s (%.2f, %.2f)", nodeId, pos.getX(), pos.getY());
        sb.append(title).append("\n");

        sb.append("\nsensors:\n");
        for (SensorDataI sensorData : sensorData.get(nodeId)) {
            Serializable value = sensorData.getValue();
            assert value instanceof Boolean || value instanceof Number;

            String format = value instanceof Boolean
                ? value.toString()
                : String.format("%.2f", ((Number) value).doubleValue());

            sb.append(" - ")
              .append(sensorData.getSensorIdentifier())
              .append(" : ")
              .append(format)
              .append("\n");
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
