package visualization.panes;

import fr.sorbonne_u.utils.Pair;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.function.Function;

public abstract class CustomStackPane
    extends StackPane {

    protected Function<ScrollPane, Pair<Double, Double>> posFunc;
    private TranslateTransition transition;

    public CustomStackPane(PanePosition pos) {
        this.setPosFunc(pos);
    }

    private void setPosFunc(final PanePosition pos) {
        switch (pos) {
            case TOP_LEFT:
                this.posFunc = (pane) -> new Pair<>(
                    -pane.getViewportBounds().getMinX() - this.getLayoutX() + 10,
                    -pane.getViewportBounds().getMinY() - this.getLayoutY() + 10
                );
                break;
            case TOP_RIGHT:
                this.posFunc = (pane) -> {
                    Bounds bounds = pane.getViewportBounds();
                    return new Pair<>(
                        -bounds.getMinX() + bounds.getWidth() - this.getLayoutX() - this.getWidth() - 10,
                        -bounds.getMinY() - this.getLayoutY() + 10
                    );
                };
                break;
            case BOTTOM_RIGHT:
                this.posFunc = (pane) -> {
                    Bounds bounds = pane.getViewportBounds();
                    return new Pair<>(
                        -bounds.getMinX() + bounds.getWidth() - this.getLayoutX() - this.getWidth() - 10,
                        -bounds.getMinY() + bounds.getHeight() - this.getLayoutY() - this.getHeight() - 10
                    );
                };
                break;
            case BOTTOM_LEFT:
                this.posFunc = (pane) -> {
                    Bounds bounds = pane.getViewportBounds();
                    return new Pair<>(
                        -bounds.getMinX() - this.getLayoutX() + 10,
                        -bounds.getMinY() + bounds.getHeight() - this.getLayoutY() - this.getHeight() - 10
                    );
                };
                break;
        }
    }


    public synchronized void updatePosition(ScrollPane pane) {
        if (transition == null || transition.getStatus() == Animation.Status.STOPPED) {
            Pair<Double, Double> pos = posFunc.apply(pane);
            transition = new TranslateTransition(Duration.millis(200), this);
            transition.setToX(pos.getFirst());
            transition.setToY(pos.getSecond());
            transition.play();
        } else {
            Pair<Double, Double> pos = posFunc.apply(pane);
            transition.setToX(pos.getFirst());
            transition.setToY(pos.getSecond());
        }
    }

}
