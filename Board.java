//crates and amanages the board
public class Board {

private static final int SIZE = 11;
private Cell [][] grid ;

public Board() {
    createBoard();
}

//create an empty 11x11 board
    public void createBoard() {
    grid = new Cell[SIZE][SIZE];
    for(int row = 0; row < SIZE; row++) {
        for(int col = 0; col < SIZE; col++) {
            grid[row][col] = new Cell(row, col);
        }
    }
    }
    //get specific cell
    public Cell getCell(int row, int col) {
    if (isValidPosition(row,col)){
        return grid[row][col];
    }
    return null;
    }
    //check if the coordinates are insde the board
    public boolean isValidPosition(int row, int col) {
    return (row >= 0 && row < SIZE &&
            col >= 0 && col < SIZE);
    }

    public boolean isCellEmpty(int row, int col) {
    if (!isValidPosition(row,col)) {
        return false;
    }
    return grid[row][col].isEmpty();
    }

    //place a stone only of its inside theboard and is empty
    public boolean placeStone(int row, int col, Colour colour) {
    if (!isValidPosition(row,col)) {
        return false;
    }
    if (!isCellEmpty(row,col)) {
        return false;
    }
    grid[row][col].setColour(colour);
    return true;
    }
    public int getSize() {
    return SIZE;
    }

}
