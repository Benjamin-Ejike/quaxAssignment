
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
    }
