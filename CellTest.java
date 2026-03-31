import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CellTest {

	@Test
	void constructor_setsRowAndCol_andStartsEmpty() {
	    // checks that the constructor correctly sets row and column
	    // and that a new cell starts empty with no colour
	    Cell cell = new Cell(4, 7);
	    assertEquals(4, cell.getRow());
	    assertEquals(7, cell.getCol());
	    assertTrue(cell.isEmpty());
	    assertNull(cell.getColor());
	}

	@Test
	void setColour_setsColourAndMakesNotEmpty() {
	    // checks that setting a colour makes the cell no longer empty
	    Cell cell = new Cell(0, 0);
	    cell.setColour(Colour.BLACK);

	    assertFalse(cell.isEmpty());
	    assertEquals(Colour.BLACK, cell.getColor());
	}

	@Test
	void getRow_returnsCorrectRow() {
	    // checks that getRow returns the correct row value
	    Cell cell = new Cell(9, 1);
	    assertEquals(9, cell.getRow());
	}

	@Test
	void getCol_returnsCorrectCol() {
	    // checks that getCol returns the correct column value
	    Cell cell = new Cell(9, 1);
	    assertEquals(1, cell.getCol());
	}

	@Test
	void isEmpty_trueBeforeSet_falseAfterSet() {
	    // checks that isEmpty is true before setting a colour
	    // and false after setting a colour
	    Cell cell = new Cell(2, 2);
	    assertTrue(cell.isEmpty());

	    cell.setColour(Colour.WHITE);
	    assertFalse(cell.isEmpty());
	}

	@Test
	void getColor_returnsNullThenColour() {
	    // checks that getColor returns null before a colour is set
	    // and returns the correct colour after setting it
	    Cell cell = new Cell(3, 3);
	    assertNull(cell.getColor());

	    cell.setColour(Colour.WHITE);
	    assertEquals(Colour.WHITE, cell.getColor());
	}
}