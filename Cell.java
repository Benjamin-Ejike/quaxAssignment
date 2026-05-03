
//Represents one octagonal tile on the board
public class Cell {

    private int row;
    private int col;
    private Colour colour; // null if empty

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.colour = null;
    }

    public boolean isEmpty() {
        return colour == null;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public Colour getColor() {
        return colour;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}