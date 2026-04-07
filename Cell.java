//represents one octagonal tile

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
    } // if colour is empty ,returns true

    public void setColour(Colour colour) {
        this.colour = colour;
    } // sets the colour to either black or white

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