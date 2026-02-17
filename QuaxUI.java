import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class QuaxUI extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(700, 700);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw empty board
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, 700, 700);

        // Draw grid
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= 11; i++) {
            gc.strokeLine(70 + i * 40, 70, 70 + i * 40, 70 + 11 * 40);
            gc.strokeLine(70, 70 + i * 40, 70 + 11 * 40, 70 + i * 40);
        }

        // Draw labels
        gc.setFont(Font.font(16));
        for (int i = 0; i < 11; i++) {
            // A-K on top/bottom
            gc.fillText(String.valueOf((char)('A' + i)), 70 + i * 40 + 15, 50);
            gc.fillText(String.valueOf((char)('A' + i)), 70 + i * 40 + 15, 70 + 11 * 40 + 25);

            // 1-11 on left/right
            gc.fillText(String.valueOf(i + 1), 40, 70 + i * 40 + 15);
            gc.fillText(String.valueOf(i + 1), 70 + 11 * 40 + 10, 70 + i * 40 + 15);
        }

        // Draw turn indicator
        gc.setFill(Color.BLACK);
        gc.fillText("⚫ BLACK to play", 300, 650);

        // Title
        gc.setFont(Font.font(24));
        gc.fillText("QUAX", 300, 30);

        BorderPane root = new BorderPane(canvas);
        stage.setTitle("Quax Game");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}