import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

public class QuaxUI extends Application {

    static final int ROWS = 11;
    static final int COLS = 11;

    private final Game game = new Game();

    // UI state
    private boolean pieRuleAvailable = false;
    private boolean pieRuleHandled = false;
    private boolean firstMoveDone = false;
    private String errorMessage = "";

    //Bot logic and Path Analysis
    private boolean isBotThinking = false;
    private String connectionMessage = "Chains -> BLACK: 0 | WHITE: 0";
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    private boolean lastMoveIsRhombic = false;

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
            //Disable clicks if Bot is moving or game ended
            if (isBotThinking || game.isGameOver()) return;

            // ── Pie rule button hit-tests (top-right corner) ──────────────────
            if (pieRuleAvailable && !pieRuleHandled) {

                // SWAP button
                if (mx >= PIE_SWAP_X && mx <= PIE_SWAP_X + PIE_SWAP_W
                        && my >= PIE_SWAP_Y && my <= PIE_SWAP_Y + PIE_SWAP_H) {
                    game.getBoard().swapAllColours(); // logic: swap existing stones
                    game.switchTurn();
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
                        if (game.placeRhombus(r, c)) { // Use logic method
                            // Sprint 4: Dismiss Pie Rule if player makes a move
                            if (pieRuleAvailable) { pieRuleAvailable = false; pieRuleHandled = true; }
                            recordMove(r, c, true);
                            handleTurnEnd(gc, startX, startY, size, cut, stepX, stepY);
                        } else {
                            errorMessage = "Illegal move: Tile already occupied";
                        }
                        clickedRhomb = true;
                        drawUI(gc, startX, startY, size, cut, stepX, stepY);
                    }
                }
            }
            if (clickedRhomb) return;

            game.setRhombicStones(rhombicStones);

            // ── Octagon cell click ────────────────────────────────────────────
            int col = (int) ((mx - startX) / stepX);
            int row = (int) ((my - startY) / stepY);

            if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

            boolean success = game.placeStone(row, col);

            if (!success) {
                errorMessage = "Illegal move: Tile already occupied";
            } else {
                errorMessage = "";
                recordMove(row, col, false);

                // After BLACK's first move the turn switches to WHITE –
                // offer the pie rule to WHITE exactly once.
                if (!firstMoveDone) {
                    firstMoveDone    = true;
                    pieRuleAvailable = true;
                } else {
                    // Sprint 4: Rule must disappear after move 2
                    pieRuleAvailable = false;
                    pieRuleHandled = true;
                }
                handleTurnEnd(gc, startX, startY, size, cut, stepX, stepY);
            }

            drawUI(gc, startX, startY, size, cut, stepX, stepY);
        });

        BorderPane root = new BorderPane(canvas);
        stage.setTitle("Quax Game");
        stage.setScene(new Scene(root));
        stage.show();

        drawUI(gc, startX, startY, size, cut, stepX, stepY);
    }




    // Saves the details of the most recent move so we can highlight it on the board
    private void recordMove(int r, int c, boolean isRhombic) {
        lastMoveRow = r;
        lastMoveCol = c;
        lastMoveIsRhombic = isRhombic;

        // Clear any previous error messages since a valid move was just made
        errorMessage = "";
    }


    // Called after every move to update the UI and pass the turn to the bot if needed
    private void handleTurnEnd(GraphicsContext gc, double sx, double sy, double s, double c, double tx, double ty) {
        // Calculate the longest connected chains for both players
        int b = game.getLongestChain(Colour.BLACK);
        int w = game.getLongestChain(Colour.WHITE);

        // Update the text displayed at the bottom of the screen
        this.connectionMessage = String.format("Chains -> BLACK: %d | WHITE: %d", b, w);

        // Redraw the board to show the new stone and updated text
        drawUI(gc, sx, sy, s, c, tx, ty);

        // If the game isn't over and it is now White's turn, trigger the bot
        if (!game.isGameOver() && game.getCurrentPlayer() == Colour.WHITE) {
            triggerBotMove(gc, sx, sy, s, c, tx, ty);
        }
    }


    // Creates a short delay before the bot moves so it looks like it's "thinking"
    private void triggerBotMove(GraphicsContext gc, double sx, double sy, double s, double c, double tx, double ty) {
        // Set to true so the user can't click the board while the bot takes its turn
        isBotThinking = true;

        // Redraw immediately to show the "Bot is thinking..." message
        drawUI(gc, sx, sy, s, c, tx, ty);

        // Create a 0.8-second timer
        PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
        pause.setOnFinished(event -> {

            // Once the timer finishes, actually place the bot's stone
            makeBotMove();
            isBotThinking = false; // The bot is done, unlock the board for the human

            // If the pie rule was available but the bot just moved, the pie rule expires
            if (pieRuleAvailable) {
                pieRuleAvailable = false;
                pieRuleHandled = true;
            }

            // End the bot's turn and update the UI again
            handleTurnEnd(gc, sx, sy, s, c, tx, ty);
        });

        // Start the timer
        pause.play();
    }


    // The actual brain of the bot: decides exactly where to place its stone
    private void makeBotMove() {
        // Main Strategy: Try to build a straight horizontal line across the middle (row 5)
        int targetRow = 5;
        for (int col = 0; col < COLS; col++) {
            // Find the first empty hole in row 5 from left to right
            if (game.getBoard().isCellEmpty(targetRow, col)) {
                game.placeStone(targetRow, col);
                recordMove(targetRow, col, false);
                return; // Move is done, exit the method
            }
        }

        // Fallback Strategy: If row 5 is completely full, just find ANY empty hole on the board
        for (int r = 0; r < ROWS; r++) {
            for (int col = 0; col < COLS; col++) {
                if (game.getBoard().isCellEmpty(r, col)) {
                    game.placeStone(r, col);
                    recordMove(r, col, false);
                    return; // Move is done, exit the method
                }
            }
        }
    }


    // Checks if a specific cell is part of the final winning connection
    // Used by the drawing code to make the winning stones glow

    private boolean isPartOfWinningPath(int r, int c) {
        // If the game is still playing, there is no winning path yet
        if (!game.isGameOver()) return false;

        // Get the list of winning coordinates from the game logic
        List<int[]> path = game.getWinningPath();
        if (path == null) return false;

        // Look through the list. If our row (r) and column (c) match, it is a winning stone!
        for (int[] pos : path) {
            if (pos[0] == r && pos[1] == c) return true;
        }

        return false;
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
                    // Calculate Sprint 4 highlights
                    boolean highlight = (!lastMoveIsRhombic && lastMoveRow == row && lastMoveCol == col);
                    boolean isWinning = isPartOfWinningPath(row, col);
                    drawStone(gc, cx, cy, cell.getColor(), highlight, isWinning);
                }
            }
        }

        // ===== Rhombic stones =====
        for (int r = 0; r < ROWS - 1; r++) {
            for (int c = 0; c < COLS - 1; c++) {
                if (rhombicStones[r][c] != null) {
                    double rx = startX + (c + 1) * stepX;
                    double ry = startY + (r + 1) * stepY;
                    boolean highlight = (lastMoveIsRhombic && lastMoveRow == r && lastMoveCol == c);
                    boolean isWinning = isPartOfWinningPath(r, c); // Path logic handles both
                    drawRhombicStone(gc, rx, ry, rhombicStones[r][c], cut, highlight, isWinning);
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
        // Dynamic Turn and Bot Thinking Status
        String turnText = isBotThinking ? "Bot is thinking..." :
                (game.getCurrentPlayer() == Colour.BLACK ? "Current turn: User (BLACK)" : "Current turn: Bot (WHITE)");
        if (game.isGameOver()) turnText = "GAME OVER";
        gc.fillText(turnText, 400, 120);

        // ===== Pie Rule UI – top-right corner =====
        // Appears once only: after BLACK's first move, for WHITE to decide.
        // Draw positions use the same PIE_* constants as the hit-test above.
        // SPRINT 4: Hidden if game is over
        if (pieRuleAvailable && !pieRuleHandled && !game.isGameOver()) {

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

        // ===== Winner Announcement =====
        // Display winning player in giant letters
        if (game.isGameOver()) {
            gc.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
            gc.setFill(game.getWinner() == Colour.BLACK ? Color.BLACK : Color.DARKBLUE);
            gc.fillText(game.getWinner() + " WINS!", 300, 820);
        }

        // =====Analysis Area =====
        gc.setFill(Color.web("#f0f0f0"));
        gc.fillRoundRect(230, 850, 540, 60, 10, 10);
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRoundRect(230, 850, 540, 60, 10, 10);
        gc.setFill(Color.BLUE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.fillText(connectionMessage, 245, 885);

        // ===== Error message =====
        if (!errorMessage.isEmpty() && !game.isGameOver()) {
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



    private void drawStone(GraphicsContext gc, double cx, double cy, Colour colour, boolean highlight, boolean isWinning) {
        double radius = 13;

        //Winning Path Glow for octagonal stone
        if (isWinning) {
            gc.setStroke(colour == Colour.BLACK ? Color.PURPLE : Color.DEEPSKYBLUE);
            gc.setLineWidth(8);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
            gc.setStroke(Color.WHITE); gc.setLineWidth(2);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
        } else if (highlight) {
            // Gold highlight for Bot/Human last move
            gc.setStroke(Color.GOLD); gc.setLineWidth(4);
            gc.strokeOval(cx - radius - 2, cy - radius - 2, (radius + 2) * 2, (radius + 2) * 2);
        }

        // fill stone
        gc.setFill(colour == Colour.BLACK ? Color.BLACK : Color.WHITE);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // draw outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    private void drawRhombicStone(GraphicsContext gc, double cx, double cy, Colour colour, double cut, boolean highlight, boolean isWinning) {
        double radius = cut * 0.55;

        // Rhombic Glow
        if (isWinning) {
            gc.setStroke(colour == Colour.BLACK ? Color.PURPLE : Color.DEEPSKYBLUE);
            gc.setLineWidth(6);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
        } else if (highlight) {
            gc.setStroke(Color.GOLD); gc.setLineWidth(4);
            gc.strokeOval(cx - radius - 2, cy - radius - 2, (radius + 2) * 2, (radius + 2) * 2);
        }

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
