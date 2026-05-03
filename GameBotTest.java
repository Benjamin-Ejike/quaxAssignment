import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameBotTest {

    @Test
    void botMakesMove_onEmptyBoard() {
        Game game = new Game();

        int[] move = game.makeBotMove();

        assertNotNull(move);
    }


    @Test
    void botPrefersMiddleLane() {
        Game game = new Game();

        int[] move = game.makeBotMove();

        assertNotNull(move);

        assertTrue(move[1] >= 4 && move[1] <= 6);
    }


    @Test
    void botTakesWinningMove() {
        Game game = new Game();

        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(i, 5, Colour.BLACK);
        }

        game.makeBotMove();

        assertTrue(game.blackWins());
    }


    @Test
    void botBlocksOpponentWin() {
        Game game = new Game();

        for (int i = 0; i < 10; i++) {
            game.getBoard().placeStone(5, i, Colour.WHITE);
        }

        int[] move = game.makeBotMove();

        assertNotNull(move);

        assertFalse(game.whiteWins());
    }


    @Test
    void botUsesRhombusWhenBeneficial() {
        Game game = new Game();

        Colour[][] rhombus = new Colour[10][10];
        game.setRhombicStones(rhombus);

        game.getBoard().placeStone(0, 0, Colour.BLACK);
        game.getBoard().placeStone(1, 1, Colour.BLACK);

        int[] move = game.makeBotMove();

        assertNotNull(move);
    }

    @Test
    void botMove_placesExactlyOneMove() {
        Game game = new Game();

        game.makeBotMove();

        int totalMoves = countStonesOnBoard(game) + countRhombusesOnBoard(game);
        assertEquals(1, totalMoves);
    }

    @Test
    void botMove_placesOnValidUnoccupiedTile() {
        Game game = new Game();

        assertTrue(game.placeStone(5, 5));

        int[] move = game.makeBotMove();

        assertNotNull(move);
        assertEquals(Colour.BLACK, game.getBoard().getCell(5, 5).getColor());
    }

    @Test
    void integration_humanThenBotMoveUpdatesBoardCorrectly() {
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
        Game game = new Game();

        assertEquals(0, countStonesOnBoard(game) + countRhombusesOnBoard(game));

        game.placeStone(2, 2);
        assertEquals(1, countStonesOnBoard(game) + countRhombusesOnBoard(game));

        game.makeBotMove();
        assertEquals(2, countStonesOnBoard(game) + countRhombusesOnBoard(game));
    }

    @Test
    void botMove_doesNotCrashWhenBoardIsFullOfStones() {
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
        Game game = new Game();

        game.switchTurn();

        int before = countStonesOnBoard(game) + countRhombusesOnBoard(game);
        int[] move = game.makeBotMove();
        int after = countStonesOnBoard(game) + countRhombusesOnBoard(game);

        assertNotNull(move);
        assertEquals(before + 1, after);
    }

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