
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void getSize_returns11() {
        // checks that the board size is fixed at 11
        Board board = new Board();
        assertEquals(11, board.getSize());
    }

    @Test
    void createBoard_makesAllCellsEmpty() {
        // checks that when a board is created, all cells start empty
        Board board = new Board();
        assertTrue(board.isCellEmpty(0, 0));
        assertTrue(board.isCellEmpty(10, 10));
    }

    @Test
    void isValidPosition_trueInsideBoard_falseOutside() {
        // checks that valid positions inside the board return true
        // and positions outside the boundaries return false
        Board board = new Board();
        assertTrue(board.isValidPosition(0, 0));
        assertTrue(board.isValidPosition(10, 10));
        assertFalse(board.isValidPosition(-1, 0));
        assertFalse(board.isValidPosition(0, -1));
        assertFalse(board.isValidPosition(11, 0));
        assertFalse(board.isValidPosition(0, 11));
    }

    @Test
    void getCell_returnsCellForValid_andNullForInvalid() {
        // checks that getCell returns a cell object for valid coordinates
        // and returns null for invalid coordinates
        Board board = new Board();

        Cell c = board.getCell(3, 4);
        assertNotNull(c);
        assertEquals(3, c.getRow());
        assertEquals(4, c.getCol());

        assertNull(board.getCell(-1, 0));
        assertNull(board.getCell(0, 11));
    }

    @Test
    void isCellEmpty_trueThenFalseAfterPlaceStone() {
        // checks that a cell starts empty and becomes not empty after placing a stone
        Board board = new Board();
        assertTrue(board.isCellEmpty(5, 5));

        boolean placed = board.placeStone(5, 5, Colour.BLACK);
        assertTrue(placed);
        assertFalse(board.isCellEmpty(5, 5));
    }

    @Test
    void isCellEmpty_returnsFalseForInvalidPosition() {
        // checks that invalid positions return false instead of causing an error
        Board board = new Board();
        assertFalse(board.isCellEmpty(-1, 0));
        assertFalse(board.isCellEmpty(0, 11));
    }

    @Test
    void placeStone_placesColourAndRejectsSecondPlacement() {
        // checks that placeStone sets the correct colour
        // and prevents placing another stone on the same cell
        Board board = new Board();

        assertTrue(board.placeStone(2, 2, Colour.WHITE));
        assertEquals(Colour.WHITE, board.getCell(2, 2).getColor());

        assertFalse(board.placeStone(2, 2, Colour.BLACK));
    }

    @Test
    void placeStone_returnsFalseForInvalidPosition() {
        // checks that placeStone returns false if the position is outside the board
        Board board = new Board();
        assertFalse(board.placeStone(-1, 0, Colour.BLACK));
        assertFalse(board.placeStone(0, 11, Colour.WHITE));
    }

    @Test
    void createBoard_resetsAfterPlacingStone() {
        // checks that calling createBoard clears all previously placed stones
        Board board = new Board();
        board.placeStone(1, 1, Colour.BLACK);

        board.createBoard();
        assertTrue(board.isCellEmpty(1, 1));
    }

    @Test
    void swapAllColours_flipsCorrectly() {
        // checks that the Pie Rule logic correctly flips BLACK to WHITE and vice versa
        Board board = new Board();
        board.placeStone(0, 0, Colour.BLACK);
        board.placeStone(1, 1, Colour.WHITE);

        board.swapAllColours();

        assertEquals(Colour.WHITE, board.getCell(0, 0).getColor());
        assertEquals(Colour.BLACK, board.getCell(1, 1).getColor());
    }

    @Test
    void swapAllColours_ignoresEmptyCells() {
        // checks that swapping colours doesn't crash on empty cells and leaves them empty
        Board board = new Board();
        board.placeStone(2, 2, Colour.BLACK); // Place one stone, leave rest empty

        board.swapAllColours();

        // The placed stone should be flipped
        assertEquals(Colour.WHITE, board.getCell(2, 2).getColor());

        // An empty cell should remain empty (null colour)
        assertTrue(board.isCellEmpty(3, 3));
        assertNull(board.getCell(3, 3).getColor());
    }
}