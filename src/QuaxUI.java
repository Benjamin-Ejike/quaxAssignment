import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QuaxUI extends Application {

    static final int ROWS = 11;
    static final int COLS = 11;

    private final Game game = new Game();

    // UI state
    private boolean pieRuleAvailable = false;
    private boolean pieRuleHandled = false;
    private boolean firstMoveDone = false;
    private String errorMessage = "";

    // Rhombic cell stones – [row][col] where row,col in [0..9]
    // Each entry is the colour placed, or null if empty.
    // rhombicStones[r][c] sits at the intersection between
    // oct[r][c], oct[r][c+1], oct[r+1][c], oct[r+1][c+1].
    private final Colour[][] rhombicStones = new Colour[10][10];

    // ── Pie rule button geometry (top-right corner) ───────────────────────────
    // Single source of truth: draw code and hit-test both use these constants.
    private static final double PIE_LABEL_X = 830;
    private static final double PIE_LABEL_Y = 30;

    private static final double PIE_SWAP_X  = 830;
    private static final double PIE_SWAP_Y  = 40;
    private static final double PIE_SWAP_W  = 130;
    private static final double PIE_SWAP_H  = 34;

    private static final double PIE_CONT_X  = 830;
    private static final double PIE_CONT_Y  = 82;
    private static final double PIE_CONT_W  = 130;
    private static final double PIE_CONT_H  = 34;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(1000, 950);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // ===== Board geometry =====
        double startX = 230;
        double startY = 180;

        // Octagons share edges with no gap between them.
        // The rhombic diamond at each 4-cell corner is formed purely by the
        // cut corners.  cut = size * 0.414 makes the 8 sides equal length
        // (regular octagon). We use a slightly larger cut so the diamond gap
        // is prominent and easy to click.
        double size  = 24;            // octagon half-size
        double cut   = size * 0.55;  // larger cut → bigger rhombic diamond

        double stepX = size * 2;     // no gap: octagons share edges
        double stepY = size * 2;

        // ===== Mouse handling =====
        canvas.setOnMouseClicked(e -> {

            double mx = e.getX();
            double my = e.getY();

            // ── Block all input during bot's turn ─────────────────────────────
            if (game.getCurrentPlayer() == Colour.WHITE) return;

            // ── Pie rule button hit-tests (top-right corner) ──────────────────
            if (pieRuleAvailable && !pieRuleHandled) {

                // SWAP button
                if (mx >= PIE_SWAP_X && mx <= PIE_SWAP_X + PIE_SWAP_W
                        && my >= PIE_SWAP_Y && my <= PIE_SWAP_Y + PIE_SWAP_H) {
                    swapColours();
                    pieRuleHandled   = true;
                    pieRuleAvailable = false;
                    errorMessage     = "";
                    drawUI(gc, startX, startY, size, cut, stepX, stepY);
                    return;
                }

                // CONTINUE button
                if (mx >= PIE_CONT_X && mx <= PIE_CONT_X + PIE_CONT_W
                        && my >= PIE_CONT_Y && my <= PIE_CONT_Y + PIE_CONT_H) {
                    pieRuleHandled   = true;
                    pieRuleAvailable = false;
                    errorMessage     = "";
                    drawUI(gc, startX, startY, size, cut, stepX, stepY);
                    return;
                }
            }

            // ── Rhombic cell click ────────────────────────────────────────────
            // Rhombic intersections sit exactly at the grid corners:
            //   centreX = startX + col * stepX   (col in 1..10)
            //   centreY = startY + row * stepY   (row in 1..10)
            // We store them as rhombicStones[r][c] with r,c in [0..9],
            // so the intersection between rows r,r+1 and cols c,c+1 is [r][c].
            double rhombRadius = cut * 0.65;  // click tolerance = fits inside diamond
            boolean clickedRhomb = false;
            for (int r = 0; r < ROWS - 1 && !clickedRhomb; r++) {
                for (int c = 0; c < COLS - 1 && !clickedRhomb; c++) {
                    double rx = startX + (c + 1) * stepX;
                    double ry = startY + (r + 1) * stepY;
                    // Diamond (L1) hit-test: |dx|+|dy| <= radius
                    if (Math.abs(mx - rx) + Math.abs(my - ry) <= rhombRadius) {
                        if (rhombicStones[r][c] == null) {
                            rhombicStones[r][c] = game.getCurrentPlayer();
                            errorMessage = "";
                        } else {
                            errorMessage = "Illegal move: Tile already occupied";
                        }
                        clickedRhomb = true;
                        drawUI(gc, startX, startY, size, cut, stepX, stepY);
                    }
                }
            }
            if (clickedRhomb) return;

            // ── Octagon cell click ────────────────────────────────────────────
            int col = (int) ((mx - startX) / stepX);
            int row = (int) ((my - startY) / stepY);

            if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

            boolean success = game.placeStone(row, col);

            if (!success) {
                errorMessage = "Illegal move: Tile already occupied";
            } else {
                errorMessage = "";

                // After BLACK's first move the turn switches to WHITE –
                // offer the pie rule to WHITE exactly once.
                if (!firstMoveDone && game.getCurrentPlayer() == Colour.WHITE) {
                    firstMoveDone    = true;
                    pieRuleAvailable = true;
                }
            }

            drawUI(gc, startX, startY, size, cut, stepX, stepY);
        });

        BorderPane root = new BorderPane(canvas);
        stage.setTitle("Quax Game");
        stage.setScene(new Scene(root));
        stage.show();

        drawUI(gc, startX, startY, size, cut, stepX, stepY);
    }

    // ================= UI LOGIC =================

    private void swapColours() {
        // simple swap for UI test mode
    }

    // ================= DRAW =================

    private void drawUI(GraphicsContext gc,
                        double startX, double startY,
                        double size,   double cut,
                        double stepX,  double stepY) {

        // background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 950);

        // ===== Board =====
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                double cx = startX + col * stepX + stepX / 2;
                double cy = startY + row * stepY + stepY / 2;

                drawOctagon(gc, cx, cy, size, cut);

                Cell cell = game.getBoard().getCell(row, col);
                if (cell != null && !cell.isEmpty()) {
                    drawStone(gc, cx, cy, cell.getColor());
                }
            }
        }

        // ===== Rhombic stones =====
        for (int r = 0; r < ROWS - 1; r++) {
            for (int c = 0; c < COLS - 1; c++) {
                if (rhombicStones[r][c] != null) {
                    double rx = startX + (c + 1) * stepX;
                    double ry = startY + (r + 1) * stepY;
                    drawRhombicStone(gc, rx, ry, rhombicStones[r][c], cut);
                }
            }
        }
        // ===== Coloured edge strips =====
        // BLACK owns TOP and BOTTOM → dark strip
        // WHITE owns LEFT and RIGHT → light strip with dark border
        double boardW  = COLS * stepX;
        double boardH  = ROWS * stepY;
        double strip   = 22;   // thickness of the coloured border band

        // TOP strip (BLACK)
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(startX, startY - strip, boardW, strip);

        // BOTTOM strip (BLACK)
        gc.fillRect(startX, startY + boardH, boardW, strip);

        // LEFT strip (WHITE)
        gc.setFill(Color.web("#e8e8e8"));
        gc.fillRect(startX - strip, startY, strip, boardH);
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1);
        gc.strokeRect(startX - strip, startY, strip, boardH);

        // RIGHT strip (WHITE)
        gc.setFill(Color.web("#e8e8e8"));
        gc.fillRect(startX + boardW, startY, strip, boardH);
        gc.setStroke(Color.web("#888888"));
        gc.strokeRect(startX + boardW, startY, strip, boardH);

        // ===== Labels inside strips =====
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        // A–K top (white text on black strip)
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 11; i++) {
            char letter = (char) ('A' + i);
            double x = startX + i * stepX + stepX / 2 - 5;
            double y = startY - strip + 15;
            gc.fillText(String.valueOf(letter), x, y);
        }

        // A–K bottom (white text on black strip)
        for (int i = 0; i < 11; i++) {
            char letter = (char) ('A' + i);
            double x = startX + i * stepX + stepX / 2 - 5;
            double y = startY + boardH + 15;
            gc.fillText(String.valueOf(letter), x, y);
        }

        // 11–1 left (dark text on white strip)
        gc.setFill(Color.web("#1a1a1a"));
        for (int i = 0; i < 11; i++) {
            int num = 11 - i;
            String label = String.valueOf(num);
            double x = startX - strip + (num < 10 ? 8 : 3);
            double y = startY + i * stepY + stepY / 2 + 5;
            gc.fillText(label, x, y);
        }

        // 11–1 right (dark text on white strip)
        for (int i = 0; i < 11; i++) {
            int num = 11 - i;
            String label = String.valueOf(num);
            double x = startX + boardW + (num < 10 ? 8 : 3);
            double y = startY + i * stepY + stepY / 2 + 5;
            gc.fillText(label, x, y);
        }


        // ===== Title =====
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("QUAX", 430, 70);

        // Game mode text
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.fillText("Human vs Bot", 540, 75);

        // ===== Turn indicator =====
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String turnText = (game.getCurrentPlayer() == Colour.BLACK)
                ? "Current turn: User (BLACK)"
                : "Current turn: Bot (WHITE)";
        gc.fillText(turnText, 400, 120);

        // ===== Pie Rule UI – top-right corner =====
        // Appears once only: after BLACK's first move, for WHITE to decide.
        // Draw positions use the same PIE_* constants as the hit-test above.
        if (pieRuleAvailable && !pieRuleHandled) {

            // Label
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Pie Rule: Swap colours?", PIE_LABEL_X, PIE_LABEL_Y);

            // SWAP button
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRoundRect(PIE_SWAP_X, PIE_SWAP_Y, PIE_SWAP_W, PIE_SWAP_H, 8, 8);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(PIE_SWAP_X, PIE_SWAP_Y, PIE_SWAP_W, PIE_SWAP_H, 8, 8);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("SWAP", PIE_SWAP_X + 42, PIE_SWAP_Y + 22);

            // CONTINUE button
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRoundRect(PIE_CONT_X, PIE_CONT_Y, PIE_CONT_W, PIE_CONT_H, 8, 8);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(PIE_CONT_X, PIE_CONT_Y, PIE_CONT_W, PIE_CONT_H, 8, 8);
            gc.setFill(Color.BLACK);
            gc.fillText("CONTINUE", PIE_CONT_X + 22, PIE_CONT_Y + 22);
        }

        // ===== Error message =====
        if (!errorMessage.isEmpty()) {
            gc.setFill(Color.RED);
            gc.setFont(Font.font(16));
            gc.fillText(errorMessage, 720, 120);
        }
    }

    // ================= SHAPES =================

    private void drawOctagon(GraphicsContext gc, double cx, double cy, double size, double cornerCut) {

        // 8 vertices clockwise from top-left of top edge
        // cx, cy is the centre of the cell
        double[] xPoints = {
                cx - size + cornerCut,   // 0: top-left of top flat
                cx + size - cornerCut,   // 1: top-right of top flat
                cx + size,               // 2: top of right flat
                cx + size,               // 3: bottom of right flat
                cx + size - cornerCut,   // 4: bottom-right of bottom flat
                cx - size + cornerCut,   // 5: bottom-left of bottom flat
                cx - size,               // 6: bottom of left flat
                cx - size                // 7: top of left flat
        };

        double[] yPoints = {
                cy - size,               // 0: top-left of top flat
                cy - size,               // 1: top-right of top flat
                cy - size + cornerCut,   // 2: top of right flat
                cy + size - cornerCut,   // 3: bottom of right flat
                cy + size,               // 4: bottom-right of bottom flat
                cy + size,               // 5: bottom-left of bottom flat
                cy + size - cornerCut,   // 6: bottom of left flat
                cy - size + cornerCut    // 7: top of left flat
        };

        // fill with board colour
        gc.setFill(Color.ORANGE);
        gc.fillPolygon(xPoints, yPoints, 8);

        // draw outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokePolygon(xPoints, yPoints, 8);
    }



    private void drawStone(GraphicsContext gc, double cx, double cy, Colour colour) {

        // stone radius fits inside octagon
        double radius = 13;

        // fill stone
        gc.setFill(colour == Colour.BLACK ? Color.BLACK : Color.WHITE);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // draw outline
        gc.setStroke(Color.BLACK);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    private void drawRhombicStone(GraphicsContext gc, double cx, double cy, Colour colour, double cut) {

        // circle fits snugly inside the diamond gap
        double radius = cut * 0.55;

        // fill stone
        gc.setFill(colour == Colour.BLACK ? Color.BLACK : Color.WHITE);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // draw outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.2);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}