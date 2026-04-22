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

    // Game is always Human vs Bot
    private final boolean vsBot = true;

    // true while the start screen is showing, false once a mode is chosen
    private boolean onStartScreen = true;

    // ── Start screen button geometry ──────────────────────────────────────────
    // Both buttons same size, centred symmetrically around x=500
    // gap between buttons = 20px, so each starts at 500 - 20/2 - 220 = 270 and 500 + 10 = 510
    private static final double BTN_W        = 220;
    private static final double BTN_H        = 55;
    private static final double BTN_VS_BOT_X = 390;
    private static final double BTN_VS_BOT_Y = 420;


    // UI state
    private boolean pieRuleAvailable  = false;
    private boolean pieRuleHandled    = false;
    private boolean firstMoveDone     = false;
    private String  errorMessage      = "";

    // Bot and path analysis
    private boolean isBotThinking     = false;
    private String  connectionMessage = "Chains -> BLACK: 0 | WHITE: 0";
    private int     lastMoveRow       = -1;
    private int     lastMoveCol       = -1;
    private boolean lastMoveIsRhombic = false;

    // Rhombic cell stones [row][col], row,col in [0..9]
    private final Colour[][] rhombicStones = new Colour[10][10];

    // ── Pie rule button geometry (top-right corner) ───────────────────────────
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

        Canvas canvas = new Canvas(1000, 900);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // ===== Board geometry =====
        double startX = 230;
        double startY = 180;
        double size   = 24;
        double cut    = size * 0.55;
        double stepX  = size * 2;
        double stepY  = size * 2;

        // ===== Mouse handling =====
        canvas.setOnMouseClicked(e -> {

            double mx = e.getX();
            double my = e.getY();
            game.setRhombicStones(rhombicStones);

            // ── Start screen ──────────────────────────────────────────────────
            if (onStartScreen) {

                // Human vs Bot button
                if (mx >= BTN_VS_BOT_X && mx <= BTN_VS_BOT_X + BTN_W
                        && my >= BTN_VS_BOT_Y && my <= BTN_VS_BOT_Y + BTN_H) {
                    onStartScreen = false;
                    drawUI(gc, startX, startY, size, cut, stepX, stepY);
                    // bot plays BLACK and goes first — trigger immediately
                    triggerBotMove(gc, startX, startY, size, cut, stepX, stepY);
                    return;
                }

                return; // ignore clicks elsewhere on start screen
            }

            // ── Block input while bot thinks, game is over, or it is bot's turn ──
            if (isBotThinking || game.isGameOver()) return;
            if (vsBot && game.getCurrentPlayer() == Colour.BLACK) return;

            // ── Pie rule buttons ──────────────────────────────────────────────
            if (pieRuleAvailable && !pieRuleHandled) {

                // SWAP button
                if (mx >= PIE_SWAP_X && mx <= PIE_SWAP_X + PIE_SWAP_W
                        && my >= PIE_SWAP_Y && my <= PIE_SWAP_Y + PIE_SWAP_H) {
                    game.getBoard().swapAllColours();
                    game.switchTurn();
                    pieRuleHandled   = true;
                    pieRuleAvailable = false;
                    errorMessage     = "";
                    // after swap, turn is now BLACK (bot) — trigger the bot move
                    handleTurnEnd(gc, startX, startY, size, cut, stepX, stepY);
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
            double  rhombRadius  = cut * 0.65;
            boolean clickedRhomb = false;

            for (int r = 0; r < ROWS - 1 && !clickedRhomb; r++) {
                for (int c = 0; c < COLS - 1 && !clickedRhomb; c++) {
                    double rx = startX + (c + 1) * stepX;
                    double ry = startY + (r + 1) * stepY;
                    if (Math.abs(mx - rx) + Math.abs(my - ry) <= rhombRadius) {
                        if (game.placeRhombus(r, c)) {
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

                if (!firstMoveDone) {
                    firstMoveDone    = true;
                    pieRuleAvailable = true;
                } else {
                    pieRuleAvailable = false;
                    pieRuleHandled   = true;
                }
                handleTurnEnd(gc, startX, startY, size, cut, stepX, stepY);
            }

            drawUI(gc, startX, startY, size, cut, stepX, stepY);
        });

        BorderPane root = new BorderPane(canvas);
        stage.setTitle("Quax Game");
        stage.setScene(new Scene(root));
        stage.show();

        // show the start screen first
        drawStartScreen(gc);
    }

    // =========================================================================
    //  START SCREEN
    // =========================================================================

    private void drawStartScreen(GraphicsContext gc) {

        // dark background
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, 1000, 950);

        // title
        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText(" QUAX", 370, 200);

        // subtitle
        gc.setFill(Color.web("#cccccc"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.fillText("    Choose your game mode", 340, 260);

        // decorative line
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(2);
        gc.strokeLine(200, 290, 800, 290);

        // rule reminder
        gc.setFill(Color.web("#aaaaaa"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        gc.fillText("           BLACK connects Top to Bottom     |     WHITE connects Left to Right", 215, 370);

        // ── Human vs Bot button (orange = default / recommended) ──────────────
        gc.setFill(Color.ORANGE);
        gc.fillRoundRect(BTN_VS_BOT_X, BTN_VS_BOT_Y, BTN_W, BTN_H, 12, 12);
        gc.setStroke(Color.web("#cc6600"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(BTN_VS_BOT_X, BTN_VS_BOT_Y, BTN_W, BTN_H, 12, 12);
        gc.setFill(Color.web("#1a1a2e"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("   Human  vs  Bot", BTN_VS_BOT_X + 28, BTN_VS_BOT_Y + 34);



        // instruction
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText("          Click a button to start", 390, 530);
    }

    // =========================================================================
    //  TURN MANAGEMENT
    // =========================================================================

    private void recordMove(int r, int c, boolean isRhombic) {
        lastMoveRow       = r;
        lastMoveCol       = c;
        lastMoveIsRhombic = isRhombic;
        errorMessage      = "";
    }

    private void handleTurnEnd(GraphicsContext gc,
                               double sx, double sy,
                               double s,  double c,
                               double tx, double ty) {
        int b = game.getLongestChain(Colour.BLACK);
        int w = game.getLongestChain(Colour.WHITE);
        connectionMessage = String.format("Chains -> BLACK: %d | WHITE: %d", b, w);

        drawUI(gc, sx, sy, s, c, tx, ty);

        // only auto-trigger bot in vs-bot mode when it is BLACK's turn (bot plays BLACK)
        if (vsBot && !game.isGameOver() && game.getCurrentPlayer() == Colour.BLACK) {
            triggerBotMove(gc, sx, sy, s, c, tx, ty);
        }
    }

    private void triggerBotMove(GraphicsContext gc,
                                double sx, double sy,
                                double s,  double c,
                                double tx, double ty) {
        isBotThinking = true;
        drawUI(gc, sx, sy, s, c, tx, ty);

        // short delay so "Bot is thinking..." renders before the move runs
        PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
        pause.setOnFinished(event -> {
            makeBotMove();
            isBotThinking = false;

            // if this was the bot's very first move, offer the pie rule to the human
            if (!firstMoveDone && game.getCurrentPlayer() == Colour.WHITE) {
                firstMoveDone    = true;
                pieRuleAvailable = true;
                pieRuleHandled   = false;
            }

            handleTurnEnd(gc, sx, sy, s, c, tx, ty);
        });
        pause.play();
    }

    private void makeBotMove() {
        int[] move = game.makeBotMove();
        if (move != null) {
            recordMove(move[0], move[1], false);
        }
    }

    private boolean isPartOfWinningPath(int r, int c) {
        if (!game.isGameOver()) return false;
        List<int[]> path = game.getWinningPath();
        if (path == null) return false;
        for (int[] pos : path) {
            if (pos[0] == r && pos[1] == c) return true;
        }
        return false;
    }

    // =========================================================================
    //  DRAW
    // =========================================================================

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
                    boolean isWinning = isPartOfWinningPath(r, c);
                    drawRhombicStone(gc, rx, ry, rhombicStones[r][c], cut, highlight, isWinning);
                }
            }
        }

        // ===== Coloured edge strips =====
        double boardW = COLS * stepX;
        double boardH = ROWS * stepY;
        double strip  = 22;

        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(startX, startY - strip, boardW, strip);         // TOP (BLACK)
        gc.fillRect(startX, startY + boardH, boardW, strip);        // BOTTOM (BLACK)

        gc.setFill(Color.web("#e8e8e8"));
        gc.fillRect(startX - strip, startY, strip, boardH);         // LEFT (WHITE)
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1);
        gc.strokeRect(startX - strip, startY, strip, boardH);

        gc.setFill(Color.web("#e8e8e8"));
        gc.fillRect(startX + boardW, startY, strip, boardH);        // RIGHT (WHITE)
        gc.strokeRect(startX + boardW, startY, strip, boardH);

        // ===== Labels inside strips =====
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 11; i++) {
            char   letter = (char) ('A' + i);
            double x      = startX + i * stepX + stepX / 2 - 5;
            gc.fillText(String.valueOf(letter), x, startY - strip + 15);
            gc.fillText(String.valueOf(letter), x, startY + boardH + 15);
        }

        gc.setFill(Color.web("#1a1a1a"));
        for (int i = 0; i < 11; i++) {
            int    num   = 11 - i;
            String label = String.valueOf(num);
            double y     = startY + i * stepY + stepY / 2 + 5;
            gc.fillText(label, startX - strip + (num < 10 ? 8 : 3), y);
            gc.fillText(label, startX + boardW + (num < 10 ? 8 : 3), y);
        }

        // ===== Title =====
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("QUAX", 430, 70);

        // active game mode label
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.fillText("Human vs Bot", 530, 75);

        // ===== Turn indicator =====
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String turnText;
        if (game.isGameOver()) {
            turnText = "GAME OVER";
        } else if (isBotThinking) {
            turnText = "Bot is thinking...";
        } else if (vsBot) {
            // bot plays BLACK, human plays WHITE
            turnText = game.getCurrentPlayer() == Colour.BLACK
                    ? "Current turn: Bot (BLACK)"
                    : "Current turn: User (WHITE)";
        }
        gc.fillText(turnText, 400, 120);

        // ===== Pie Rule UI =====
        if (pieRuleAvailable && !pieRuleHandled && !game.isGameOver()) {

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Pie Rule: Swap colours?", PIE_LABEL_X, PIE_LABEL_Y);

            gc.setFill(Color.LIGHTGRAY);
            gc.fillRoundRect(PIE_SWAP_X, PIE_SWAP_Y, PIE_SWAP_W, PIE_SWAP_H, 8, 8);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(PIE_SWAP_X, PIE_SWAP_Y, PIE_SWAP_W, PIE_SWAP_H, 8, 8);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("SWAP", PIE_SWAP_X + 42, PIE_SWAP_Y + 22);

            gc.setFill(Color.LIGHTGRAY);
            gc.fillRoundRect(PIE_CONT_X, PIE_CONT_Y, PIE_CONT_W, PIE_CONT_H, 8, 8);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(PIE_CONT_X, PIE_CONT_Y, PIE_CONT_W, PIE_CONT_H, 8, 8);
            gc.setFill(Color.BLACK);
            gc.fillText("CONTINUE", PIE_CONT_X + 22, PIE_CONT_Y + 22);
        }

        // ===== Winner announcement =====
        if (game.isGameOver()) {
            gc.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
            gc.setFill(game.getWinner() == Colour.BLACK ? Color.BLACK : Color.DARKBLUE);
            gc.fillText(game.getWinner() + " WINS!", 300, 820);
        }

        // ===== Analysis area =====
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

    // =========================================================================
    //  SHAPES
    // =========================================================================

    private void drawOctagon(GraphicsContext gc, double cx, double cy,
                             double size, double cornerCut) {

        // 8 vertices clockwise from top-left of top edge
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

    private void drawStone(GraphicsContext gc, double cx, double cy,
                           Colour colour, boolean highlight, boolean isWinning) {

        // stone radius fits inside the octagon
        double radius = 13;

        // winning path glow
        if (isWinning) {
            gc.setStroke(colour == Colour.BLACK ? Color.PURPLE : Color.DEEPSKYBLUE);
            gc.setLineWidth(8);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
        } else if (highlight) {
            // gold ring for last move
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(4);
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

    private void drawRhombicStone(GraphicsContext gc, double cx, double cy,
                                  Colour colour, double cut,
                                  boolean highlight, boolean isWinning) {

        // circle fits snugly inside the diamond gap
        double radius = cut * 0.55;

        // winning glow
        if (isWinning) {
            gc.setStroke(colour == Colour.BLACK ? Color.PURPLE : Color.DEEPSKYBLUE);
            gc.setLineWidth(6);
            gc.strokeOval(cx - radius - 1, cy - radius - 1, (radius + 1) * 2, (radius + 1) * 2);
        } else if (highlight) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(4);
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
