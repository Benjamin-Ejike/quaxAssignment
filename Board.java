
//Represents the 11 x 11 Quax game board and handles stone placement + pie rule logic
public class Board {

    private static final int SIZE = 11;
    private Cell[][] grid;

    public Board() {
        grid = new Cell[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = new Cell(r, c);
        }
    }

    public boolean isValidPosition(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    public Cell getCell(int r, int c) {
        if (r >= 0 && r < SIZE && c >= 0 && c < SIZE)
            return grid[r][c];
        return null;
    }

    public boolean isCellEmpty(int r, int c) {
        Cell cell = getCell(r, c);
        return cell != null && cell.isEmpty();
    }


    public boolean placeStone(int r, int c, Colour color) {
        if (isCellEmpty(r, c)) {
            grid[r][c].setColour(color);
            return true;
        }
        return false;
    }


    public void swapAllColours() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (!grid[r][c].isEmpty()) {
                    grid[r][c].setColour(
                        grid[r][c].getColor() == Colour.BLACK ? Colour.WHITE : Colour.BLACK
                    );
                }
            }
        }
    }

    public void createBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    public int getSize() {
        return SIZE;
    }
}