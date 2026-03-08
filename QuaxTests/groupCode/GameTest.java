package groupCode;

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
}