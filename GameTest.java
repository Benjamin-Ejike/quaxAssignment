import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

	@Test
	void constructor_startsWithBlackAndBoardReady() {
	    // checks that a new game creates a board and sets the first player to black
	    Game game = new Game();
	    assertNotNull(game.getBoard());
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());
	}

	@Test
	void startGame_resetsPlayerAndBoard() {
	    // checks that startGame resets the board and sets the turn back to black
	    Game game = new Game();
	    game.placeStone(0, 0);

	    game.startGame();
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());
	    assertTrue(game.getBoard().isCellEmpty(0, 0));
	}

	@Test
	void getCurrentPlayer_returnsCurrentTurn() {
	    // checks that getCurrentPlayer returns the player whose turn it currently is
	    Game game = new Game();
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());
	}

	@Test
	void switchTurn_togglesBetweenPlayers() {
	    // checks that switchTurn changes from black to white and back again
	    Game game = new Game();
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());

	    game.switchTurn();
	    assertEquals(Colour.WHITE, game.getCurrentPlayer());

	    game.switchTurn();
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());
	}

	@Test
	void placeStone_placesStoneAndSwitchesTurn() {
	    // checks that a valid move places a stone on the board and switches the turn
	    Game game = new Game();

	    assertTrue(game.placeStone(3, 3));
	    assertEquals(Colour.BLACK, game.getBoard().getCell(3, 3).getColor());
	    assertEquals(Colour.WHITE, game.getCurrentPlayer());
	}

	@Test
	void placeStone_failsOnOccupied_andDoesNotSwitchTurn() {
	    // checks that placing a stone on an occupied cell fails and does not change the turn
	    Game game = new Game();

	    assertTrue(game.placeStone(2, 2));
	    assertEquals(Colour.WHITE, game.getCurrentPlayer());

	    assertFalse(game.placeStone(2, 2));
	    assertEquals(Colour.WHITE, game.getCurrentPlayer());
	}

	@Test
	void placeStone_failsOutOfBounds() {
	    // checks that placing a stone outside the board boundaries returns false
	    Game game = new Game();
	    assertFalse(game.placeStone(-1, 0));
	    assertFalse(game.placeStone(0, 11));
	}

	@Test
	void getBoard_returnsSameBoardInstance() {
	    // checks that getBoard always returns the same board object, not a new one
	    Game game = new Game();
	    Board a = game.getBoard();
	    Board b = game.getBoard();
	    assertSame(a, b);
	}
	
	@Test
	void illegalMove_doesNotPlaceStone() {
	    Game game = new Game();
	    game.placeStone(4,4);
	    boolean result = game.placeStone(4,4);
	    assertFalse(result);
	    assertEquals(Colour.BLACK, game.getBoard().getCell(4,4).getColor());
}
	
	@Test
	void placeStone_twoValidMoves_placesBlackThenWhite() {
	    Game game = new Game();
	    assertTrue(game.placeStone(0, 0)); // BLACK
	    assertTrue(game.placeStone(0, 1)); // WHITE
	    assertEquals(Colour.BLACK, game.getBoard().getCell(0, 0).getColor());
	    assertEquals(Colour.WHITE, game.getBoard().getCell(0, 1).getColor());
	    assertEquals(Colour.BLACK, game.getCurrentPlayer());
	}
	

	    @Test
	    void blackWins_returnsFalseOnEmptyBoard() {
	        // checks that black has not won on an empty board
	        Game game = new Game();
	        assertFalse(game.blackWins());
	    }

	    @Test
	    void whiteWins_returnsFalseOnEmptyBoard() {
	        // checks that white has not won on an empty board
	        Game game = new Game();
	        assertFalse(game.whiteWins());
	    }

	    @Test
	    void blackWins_trueForStraightTopToBottomPath() {
	        // checks that black wins with a full vertical path
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int row = 0; row < board.getSize(); row++) {
	            board.placeStone(row, 0, Colour.BLACK);
	        }

	        assertTrue(game.blackWins());
	        assertFalse(game.whiteWins());
	    }

	    @Test
	    void whiteWins_trueForStraightLeftToRightPath() {
	        // checks that white wins with a full horizontal path
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int col = 0; col < board.getSize(); col++) {
	            board.placeStone(0, col, Colour.WHITE);
	        }

	        assertTrue(game.whiteWins());
	        assertFalse(game.blackWins());
	    }

	    @Test
	    void blackWins_falseWhenPathIsBroken() {
	        // checks that black does not win if one part of the chain is missing
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int row = 0; row < board.getSize(); row++) {
	            if (row != 5) {
	                board.placeStone(row, 0, Colour.BLACK);
	            }
	        }

	        assertFalse(game.blackWins());
	    }

	    @Test
	    void whiteWins_falseWhenPathIsBroken() {
	        // checks that white does not win if one part of the chain is missing
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int col = 0; col < board.getSize(); col++) {
	            if (col != 5) {
	                board.placeStone(0, col, Colour.WHITE);
	            }
	        }

	        assertFalse(game.whiteWins());
	    }

	    @Test
	    void blackWins_trueUsingDiagonalRhombicConnections() {
	        // checks that black can win through diagonal connections when matching rhombic tiles exist
	        Game game = new Game();
	        Board board = game.getBoard();

	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        board.placeStone(0, 0, Colour.BLACK);
	        board.placeStone(1, 1, Colour.BLACK);
	        board.placeStone(2, 2, Colour.BLACK);
	        board.placeStone(3, 3, Colour.BLACK);
	        board.placeStone(4, 4, Colour.BLACK);
	        board.placeStone(5, 5, Colour.BLACK);
	        board.placeStone(6, 6, Colour.BLACK);
	        board.placeStone(7, 7, Colour.BLACK);
	        board.placeStone(8, 8, Colour.BLACK);
	        board.placeStone(9, 9, Colour.BLACK);
	        board.placeStone(10, 10, Colour.BLACK);

	        for (int i = 0; i < 10; i++) {
	            rhombs[i][i] = Colour.BLACK;
	        }

	        assertTrue(game.blackWins());
	    }

	    @Test
	    void whiteWins_trueUsingDiagonalRhombicConnections() {
	        // checks that white can win through diagonal connections when matching rhombic tiles exist
	        Game game = new Game();
	        Board board = game.getBoard();

	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        board.placeStone(10, 0, Colour.WHITE);
	        board.placeStone(9, 1, Colour.WHITE);
	        board.placeStone(8, 2, Colour.WHITE);
	        board.placeStone(7, 3, Colour.WHITE);
	        board.placeStone(6, 4, Colour.WHITE);
	        board.placeStone(5, 5, Colour.WHITE);
	        board.placeStone(4, 6, Colour.WHITE);
	        board.placeStone(3, 7, Colour.WHITE);
	        board.placeStone(2, 8, Colour.WHITE);
	        board.placeStone(1, 9, Colour.WHITE);
	        board.placeStone(0, 10, Colour.WHITE);

	        rhombs[9][0] = Colour.WHITE;
	        rhombs[8][1] = Colour.WHITE;
	        rhombs[7][2] = Colour.WHITE;
	        rhombs[6][3] = Colour.WHITE;
	        rhombs[5][4] = Colour.WHITE;
	        rhombs[4][5] = Colour.WHITE;
	        rhombs[3][6] = Colour.WHITE;
	        rhombs[2][7] = Colour.WHITE;
	        rhombs[1][8] = Colour.WHITE;
	        rhombs[0][9] = Colour.WHITE;

	        assertTrue(game.whiteWins());
	    }

	    @Test
	    void blackWins_falseIfDiagonalRhombicTilesAreMissing() {
	        // checks that diagonal stones alone do not count without matching rhombic tiles
	        Game game = new Game();
	        Board board = game.getBoard();

	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        board.placeStone(0, 0, Colour.BLACK);
	        board.placeStone(1, 1, Colour.BLACK);
	        board.placeStone(2, 2, Colour.BLACK);
	        board.placeStone(3, 3, Colour.BLACK);
	        board.placeStone(4, 4, Colour.BLACK);
	        board.placeStone(5, 5, Colour.BLACK);
	        board.placeStone(6, 6, Colour.BLACK);
	        board.placeStone(7, 7, Colour.BLACK);
	        board.placeStone(8, 8, Colour.BLACK);
	        board.placeStone(9, 9, Colour.BLACK);
	        board.placeStone(10, 10, Colour.BLACK);

	        assertFalse(game.blackWins());
	    }

	    @Test
	    void placeStone_setsGameOverWhenBlackWins() {
	        // checks that placing the winning stone sets game over and winner
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int row = 0; row < 10; row++) {
	            board.placeStone(row, 0, Colour.BLACK);
	        }

	        assertTrue(game.placeStone(10, 0));
	        assertTrue(game.isGameOver());
	        assertEquals(Colour.BLACK, game.getWinner());
	    }

	    @Test
	    void placeStone_setsGameOverWhenWhiteWins() {
	        // checks that placing the winning stone sets game over and winner
	        Game game = new Game();
	        Board board = game.getBoard();

	        game.switchTurn();

	        for (int col = 0; col < 10; col++) {
	            board.placeStone(0, col, Colour.WHITE);
	        }

	        assertTrue(game.placeStone(0, 10));
	        assertTrue(game.isGameOver());
	        assertEquals(Colour.WHITE, game.getWinner());
	    }

	    @Test
	    void placeStone_failsAfterGameOver() {
	        // checks that no more stones can be placed after game over
	        Game game = new Game();
	        Board board = game.getBoard();

	        for (int row = 0; row < 10; row++) {
	            board.placeStone(row, 0, Colour.BLACK);
	        }

	        assertTrue(game.placeStone(10, 0));
	        assertTrue(game.isGameOver());

	        assertFalse(game.placeStone(5, 5));
	    }

	    @Test
	    void placeRhombus_placesTileAndSwitchesTurn() {
	        // checks that a valid rhombic placement works and switches turn
	        Game game = new Game();
	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        assertTrue(game.placeRhombus(2, 2));
	        assertEquals(Colour.BLACK, rhombs[2][2]);
	        assertEquals(Colour.WHITE, game.getCurrentPlayer());
	    }

	    @Test
	    void placeRhombus_failsWhenOccupied() {
	        // checks that an occupied rhombic position cannot be used again
	        Game game = new Game();
	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        assertTrue(game.placeRhombus(2, 2));
	        assertFalse(game.placeRhombus(2, 2));
	    }

	    @Test
	    void placeRhombus_failsOutOfBounds() {
	        // checks that invalid rhombic positions return false
	        Game game = new Game();
	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        assertFalse(game.placeRhombus(-1, 0));
	        assertFalse(game.placeRhombus(10, 0));
	        assertFalse(game.placeRhombus(0, -1));
	        assertFalse(game.placeRhombus(0, 10));
	    }

	    @Test
	    void placeRhombus_failsAfterGameOver() {
	        // checks that rhombic placement is blocked after game over
	        Game game = new Game();
	        Board board = game.getBoard();
	        Colour[][] rhombs = new Colour[10][10];
	        game.setRhombicStones(rhombs);

	        for (int row = 0; row < 10; row++) {
	            board.placeStone(row, 0, Colour.BLACK);
	        }

	        assertTrue(game.placeStone(10, 0));
	        assertTrue(game.isGameOver());

	        assertFalse(game.placeRhombus(2, 2));
	    }
    @Test
    void swapAllColours_flipsCorrectly() {
        Board board = new Board();
        board.placeStone(0,0,Colour.BLACK);
        board.placeStone(1,1,Colour.WHITE);

        board.swapAllColours();

        assertEquals(Colour.WHITE, board.getCell(0,0).getColor());
        assertEquals(Colour.BLACK, board.getCell(1,1).getColor());
    }
    @Test
    void singleStone_doesNotWin() {
        Game game = new Game();
        game.getBoard().placeStone(0, 0, Colour.BLACK);

        assertFalse(game.blackWins());
    }
    @Test
    void diagonalFails_ifRhombusWrongColour() {
        Game game = new Game();
        Board board = game.getBoard();

        Colour[][] rhombs = new Colour[10][10];
        game.setRhombicStones(rhombs);

        board.placeStone(0, 0, Colour.BLACK);
        board.placeStone(1, 1, Colour.BLACK);

        rhombs[0][0] = Colour.WHITE; // wrong colour

        assertFalse(game.blackWins());
    }


	}
