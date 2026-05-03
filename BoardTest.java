
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void getSize_returns11() {

        Board board = new Board();
        assertEquals(11, board.getSize());
    }

    @Test
    void createBoard_makesAllCellsEmpty() {

        Board board = new Board();
        assertTrue(board.isCellEmpty(0, 0));
        assertTrue(board.isCellEmpty(10, 10));
    }

    @Test
    void isValidPosition_trueInsideBoard_falseOutside() {

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

        Board board = new Board();
        assertTrue(board.isCellEmpty(5, 5));

        boolean placed = board.placeStone(5, 5, Colour.BLACK);
        assertTrue(placed);
        assertFalse(board.isCellEmpty(5, 5));
    }

    @Test
    void isCellEmpty_returnsFalseForInvalidPosition() {

        Board board = new Board();
        assertFalse(board.isCellEmpty(-1, 0));
        assertFalse(board.isCellEmpty(0, 11));
    }

    @Test
    void placeStone_placesColourAndRejectsSecondPlacement() {
        Board board = new Board();

        assertTrue(board.placeStone(2, 2, Colour.WHITE));
        assertEquals(Colour.WHITE, board.getCell(2, 2).getColor());

        assertFalse(board.placeStone(2, 2, Colour.BLACK));
    }

    @Test
    void placeStone_returnsFalseForInvalidPosition() {
        Board board = new Board();
        assertFalse(board.placeStone(-1, 0, Colour.BLACK));
        assertFalse(board.placeStone(0, 11, Colour.WHITE));
    }

    @Test
    void createBoard_resetsAfterPlacingStone() {

        Board board = new Board();
        board.placeStone(1, 1, Colour.BLACK);

        board.createBoard();
        assertTrue(board.isCellEmpty(1, 1));
    }

    @Test
    void swapAllColours_flipsCorrectly() {

        Board board = new Board();
        board.placeStone(0, 0, Colour.BLACK);
        board.placeStone(1, 1, Colour.WHITE);

        board.swapAllColours();

        assertEquals(Colour.WHITE, board.getCell(0, 0).getColor());
        assertEquals(Colour.BLACK, board.getCell(1, 1).getColor());
    }

    @Test
    void swapAllColours_ignoresEmptyCells() {

        Board board = new Board();
        board.placeStone(2, 2, Colour.BLACK); // Place one stone, leave rest empty

        board.swapAllColours();

        assertEquals(Colour.WHITE, board.getCell(2, 2).getColor());

        assertTrue(board.isCellEmpty(3, 3));
        assertNull(board.getCell(3, 3).getColor());
    }
}