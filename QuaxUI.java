import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class QuaxUI extends Application {

    static final int ROWS = 11;
    static final int COLS = 11;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(1000, 950);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 950);

        // ===== Board geometry =====
        double startX = 200;
        double startY = 150;

        double size = 26;   // base square half-size
        double cut  = size * 0.42;  // corner cut (45°)

        double cellW = size * 2;
        double cellH = size * 2;

        double stepX = cellW;
        double stepY = cellH;

        // ===== Draw board =====
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                double cx = startX + col * stepX;
                double cy = startY + row * stepY;

                drawOctagon(gc, cx, cy, size, cut);
            }
        }

        // ===== Labels =====
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(16));

        // A–K top
        for (int i = 0; i < 11; i++) {
            char letter = (char) ('A' + i);
            double x = startX + i * stepX - 6;
            double y = startY - 35;
            gc.fillText(String.valueOf(letter), x, y);
        }

        // A–K bottom
        for (int i = 0; i < 11; i++) {
            char letter = (char) ('A' + i);
            double x = startX + i * stepX - 6;
            double y = startY + (ROWS - 1) * stepY + 55;
            gc.fillText(String.valueOf(letter), x, y);
        }

        // 1–11 left
        for (int i = 0; i < 11; i++) {
            int num = 11 - i;
            double x = startX - 60;
            double y = startY + i * stepY + 6;
            gc.fillText(String.valueOf(num), x, y);
        }

        // 1–11 right
        for (int i = 0; i < 11; i++) {
            int num = 11 - i;
            double x = startX + (COLS - 1) * stepX + 55;
            double y = startY + i * stepY + 6;
            gc.fillText(String.valueOf(num), x, y);
        }

        // ===== Title =====
        gc.setFont(Font.font(28));
        gc.fillText("QUAX", 470, 80);

        // ===== Turn indicator =====
        gc.setFont(Font.font(20));
        gc.fillText("BLACK to play", 440, 900);

        BorderPane root = new BorderPane(canvas);
        stage.setTitle("Quax Game");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ===== True regular octagon (flat sides) =====
    private void drawOctagon(GraphicsContext gc, double cx, double cy, double s, double c) {

        double[] x = {
                cx - s + c, cx + s - c,
                cx + s,     cx + s,
                cx + s - c, cx - s + c,
                cx - s,     cx - s
        };

        double[] y = {
                cy - s,     cy - s,
                cy - s + c, cy + s - c,
                cy + s,     cy + s,
                cy + s - c, cy - s + c
        };

        // fill
        gc.setFill(Color.ORANGE);
        gc.fillPolygon(x, y, 8);

        // border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokePolygon(x, y, 8);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
