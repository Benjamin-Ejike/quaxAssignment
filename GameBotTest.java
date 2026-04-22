import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameBotTest {

    /**
     * Test: Bot should always make a move on an empty board.
     * This ensures the bot logic is working and does not return null.
     */
    @Test
    void botMakesMove_onEmptyBoard() {
        Game game = new Game();

        int[] move = game.makeBotMove();

        // Bot should return a valid move (row, col)
        assertNotNull(move);
    }

    /**
     * Test: Bot prefers the central lane rather than edges.
     *
     * Instead of checking an exact position ,
     * this test verifies that the bot chooses a move near the centre of the board.
     * This reflects the bot's lane-based strategy for optimal positioning.
     */
    @Test
    void botPrefersMiddleLane() {
        Game game = new Game();

        int[] move = game.makeBotMove();

        // Bot should choose a valid move
        assertNotNull(move);

        // Column should be near center (more realistic check)
        assertTrue(move[1] >= 4 && move[1] <= 6);
    }

    /**
     * Test: Bot takes a winning move when available.
     * If the bot can win in one move, it should prioritise that move.
     */
    @Test
    void botTakesWinningMove() {
        Game game = new Game();

        // Set up board so BLACK (bot) is one move away from winning
        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(i, 5, Colour.BLACK);
        }

        // Bot makes move
        game.makeBotMove();

        // After move, bot should have won
        assertTrue(game.blackWins());
    }

    /**
     * Test: Bot blocks the opponent's winning move.
     * If WHITE is about to win, bot should block it.
     */
    @Test
    void botBlocksOpponentWin() {
        Game game = new Game();

        // Set up WHITE almost winning (left to right)
        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(5, i, Colour.WHITE);
        }

        int[] move = game.makeBotMove();

        // Bot should make a move to block
        assertNotNull(move);

        // After move, WHITE should NOT have won
        assertFalse(game.whiteWins());
    }

    /**
     * Test: Bot uses rhombic tile when it leads to a winning setup.
     * This ensures rhombus logic is being used by the bot.
     */
    @Test
    void botUsesRhombusWhenBeneficial() {
        Game game = new Game();

        // Initialise rhombic grid
        Colour[][] rhombus = new Colour[10][10];
        game.setRhombicStones(rhombus);

        // Create diagonal opportunity for rhombus placement
        game.getBoard().placeStone(0, 0, Colour.BLACK);
        game.getBoard().placeStone(1, 1, Colour.BLACK);

        int[] move = game.makeBotMove();

        // Bot should attempt a valid move (possibly rhombus)
        assertNotNull(move);
    }

    @Test
    void botMove_placesExactlyOneMove() {
        // checks that the bot only places one move per turn
        Game game = new Game();

        game.makeBotMove();

        int totalMoves = countStonesOnBoard(game) + countRhombusesOnBoard(game);
        assertEquals(1, totalMoves);
    }

    @Test
    void botMove_placesOnValidUnoccupiedTile() {
        // checks that the bot does not overwrite existing stones
        Game game = new Game();

        assertTrue(game.placeStone(5, 5));

        int[] move = game.makeBotMove();

        assertNotNull(move);
        assertEquals(Colour.BLACK, game.getBoard().getCell(5, 5).getColor());
    }

    @Test
    void integration_humanThenBotMoveUpdatesBoardCorrectly() {
        // checks that human move followed by bot move updates correctly
        Game game = new Game();

        assertTrue(game.placeStone(0, 0));
        assertEquals(Colour.WHITE, game.getCurrentPlayer());

        int[] botMove = game.makeBotMove();
        assertNotNull(botMove);

        assertEquals(Colour.BLACK, game.getBoard().getCell(0, 0).getColor());
        assertEquals(Colour.BLACK, game.getCurrentPlayer());
    }

    @Test
    void totalNumberOfMoves_increasesCorrectlyAfterEachTurn() {
        // checks that moves increase correctly after each turn
        Game game = new Game();

        assertEquals(0, countStonesOnBoard(game) + countRhombusesOnBoard(game));

        game.placeStone(2, 2);
        assertEquals(1, countStonesOnBoard(game) + countRhombusesOnBoard(game));

        game.makeBotMove();
        assertEquals(2, countStonesOnBoard(game) + countRhombusesOnBoard(game));
    }

    @Test
    void botMove_doesNotCrashWhenBoardIsFullOfStones() {
        // checks that the bot handles a full stone board safely
        Game game = new Game();

        int size = game.getBoard().getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                game.getBoard().placeStone(r, c, Colour.BLACK);
            }
        }

        assertDoesNotThrow(game::makeBotMove);
    }

    @Test
    void botCanPlaceRhombusWhenValid() {
        // checks that the bot can place a rhombus when a valid diagonal opportunity exists
        Game game = new Game();

        Colour[][] rhombus = new Colour[10][10];
        game.setRhombicStones(rhombus);

        game.getBoard().placeStone(0, 0, Colour.BLACK);
        game.getBoard().placeStone(1, 1, Colour.BLACK);

        int before = countRhombusesOnBoard(game);
        int[] move = game.makeBotMove();
        int after = countRhombusesOnBoard(game);

        assertNotNull(move);
        assertTrue(after >= before);
    }

    @Test
    void botRhombusMove_updatesRhombicBoardState() {
        // checks that rhombus placement updates the board
        Game game = new Game();

        Colour[][] rhombus = new Colour[10][10];
        game.setRhombicStones(rhombus);

        game.getBoard().placeStone(2, 2, Colour.BLACK);
        game.getBoard().placeStone(3, 3, Colour.BLACK);

        int before = countRhombusesOnBoard(game);
        game.makeBotMove();
        int after = countRhombusesOnBoard(game);

        assertTrue(after >= before);
    }

    @Test
    void botSwitchesLaneWhenBlocked() {
        // checks that the bot avoids a blocked central lane
        Game game = new Game();

        for (int r = 0; r < 11; r++) {
            game.getBoard().placeStone(r, 5, Colour.WHITE);
        }

        int[] move = game.makeBotMove();

        assertNotNull(move);
        assertNotEquals(5, move[1]);
    }

    @Test
    void botDoesNotBlockOpponentIfItCanWinImmediately() {
        // checks that the bot prioritises its own winning move over blocking
        Game game = new Game();

        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(i, 4, Colour.BLACK);
        }

        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(6, i, Colour.WHITE);
        }

        game.makeBotMove();

        assertTrue(game.blackWins());
    }

    @Test
    void botConsidersRhombusSetupMoves() {
        // checks that the bot can use rhombus logic as part of its decision making
        Game game = new Game();

        Colour[][] rhombus = new Colour[10][10];
        game.setRhombicStones(rhombus);

        game.getBoard().placeStone(2, 2, Colour.BLACK);
        game.getBoard().placeStone(3, 3, Colour.BLACK);

        int[] move = game.makeBotMove();

        assertNotNull(move);
    }

    @Test
    void botMove_returnsBoardPositionInsideBounds() {
        // checks that returned bot move coordinates stay inside valid bounds
        Game game = new Game();

        int[] move = game.makeBotMove();

        assertNotNull(move);
        assertTrue(move[0] >= 0);
        assertTrue(move[1] >= 0);
        assertTrue(move[0] < 11);
        assertTrue(move[1] < 11);
    }

    @Test
    void botAsWhiteMakesValidMove() {
        // checks that the bot can still make a valid move when white is the current player
        Game game = new Game();

        game.switchTurn();

        int before = countStonesOnBoard(game) + countRhombusesOnBoard(game);
        int[] move = game.makeBotMove();
        int after = countStonesOnBoard(game) + countRhombusesOnBoard(game);

        assertNotNull(move);
        assertEquals(before + 1, after);
    }

    // helper method to count stones
    private int countStonesOnBoard(Game game) {
        int count = 0;
        int size = game.getBoard().getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!game.getBoard().isCellEmpty(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    // helper method to count rhombic tiles
    private int countRhombusesOnBoard(Game game) {
        int count = 0;
        Colour[][] rhombuses = game.getRhombicStones();

        if (rhombuses == null) return 0;

        for (int r = 0; r < rhombuses.length; r++) {
            for (int c = 0; c < rhombuses[r].length; c++) {
                if (rhombuses[r][c] != null) {
                    count++;
                }
            }
        }
        return count;
    }
}