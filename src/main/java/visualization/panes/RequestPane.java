package visualization.panes;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.util.function.Consumer;


public class RequestPane
    extends CustomStackPane {

    private final FlowPane childPane;
    private final Color fgColor;
    private final Color bgColor;
    private final ScrollPane scrollPane;

    public RequestPane(int width, int height, PanePosition pos, Color fgColor, Color bgColor) {
        super(pos);
        this.bgColor = bgColor;
        this.setWidth(width);
        this.setHeight(height);

        this.fgColor = fgColor;
        this.childPane = new FlowPane();
        this.childPane.setColumnHalignment(HPos.CENTER);
        this.childPane.setAlignment(Pos.BASELINE_CENTER);
        this.childPane.setOrientation(Orientation.VERTICAL);
        this.childPane.setVgap(10);
        this.childPane.setPrefWrapLength(Double.MAX_VALUE);
        this.childPane.setPadding(new Insets(25));

        this.scrollPane = new ScrollPane(childPane);
        scrollPane.setMinWidth(width);
        scrollPane.setMaxWidth(width);
        scrollPane.setMinHeight(height);
        scrollPane.setMaxHeight(height);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        String colorStr = bgColor.toString().replace("0x", "#") + "; ";
        scrollPane.getStyleClass().add("request-pane");
        scrollPane.setStyle("-fx-background-color: " + colorStr +
                            "-fx-background: " + colorStr +
                            "-fx-opacity: " + bgColor.getOpacity() + ";" +
                            "-fx-border-radius: 7.5px; -fx-background-radius: 7.5px;");

        scrollPane.setPadding(Insets.EMPTY);
        this.getChildren().add(scrollPane);
        this.setPadding(Insets.EMPTY);
    }

    public void updateViewportStyle() {
        Node viewport = this.scrollPane.lookup(".viewport");
        System.out.println("viewport = " + viewport);
        viewport.setStyle("-fx-border-radius: 7.5px;-fx-background-radius: 7.5px;");
    }

    public void addRequestButton(String uri, Consumer<Void> onClick) {
        Color ogColor = bgColor.brighter().brighter();
        Color clickColor = ogColor.brighter().brighter();

        String btnStyle = "-fx-background-color: " + ogColor.toString().replace("0x", "#") + "; ";
        Button button = new Button(uri);
        button.setMinWidth(this.getWidth() - 50);
        button.setPrefWidth(this.getWidth() - 50);
        button.setMaxWidth(this.getWidth() - 50);
        button.setTextFill(this.fgColor);
        button.setStyle(btnStyle);
        this.childPane.getChildren().add(button);
        button.setOnMousePressed(
            e -> button.setStyle("-fx-background-color: " + clickColor.toString().replace("0x", "#") + "; "));
        button.setOnMouseReleased(event -> {
            onClick.accept(null);
            button.setStyle(btnStyle);
        });
    }

}
