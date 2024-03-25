package visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AxisCanvas
    extends Canvas {

    public AxisCanvas(double width, double height) {
        super(width, height);
        init();
    }

    private void init() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setStroke(Color.GRAY);
        gc.strokeLine(this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight());  // y
        gc.strokeLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2); // x
    }

}
