package visualization.panes;

import javafx.animation.FadeTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class InfoPane
    extends CustomStackPane {

    private String textHeader = "";
    public static final String DEFAULT_TEXT = "Right click on a node to focus";
    private final Text text;
    private boolean isTextAnimating = false;

    public InfoPane(int width, int height, PanePosition pos, Color fgColor, Color bgColor) {
        super(pos);

        Rectangle rectangle = new Rectangle(width, height, bgColor);
        rectangle.setArcWidth(15);
        rectangle.setArcHeight(15);

        this.text = new Text(DEFAULT_TEXT);
        this.text.setFill(fgColor);
        this.getChildren().addAll(rectangle, text);
    }

    public void setText(String header, String content) {
        if (isTextAnimating) return;
        if (header.equals(this.textHeader)) {
            this.text.setText(content);
            return;
        }
        isTextAnimating = true;
        this.textHeader = header;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), text);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();
        fadeOut.setOnFinished(e -> {
            this.text.setText(content);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), text);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            fadeIn.setOnFinished(ev -> isTextAnimating = false);
        });
    }

}
