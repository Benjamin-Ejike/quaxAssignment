public class Game {

    private Board board;
    private Colour currentPlayer;

    public Game() {
        startGame();
    }

    public void startGame() {
        board = new Board();
        currentPlayer = Colour.BLACK; // BLACK starts (spec)
    }

    public Colour getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchTurn() {
        if (currentPlayer == Colour.BLACK) {
            currentPlayer = Colour.WHITE;
        } else {
            currentPlayer = Colour.BLACK;
        }
    }

    public boolean placeStone(int row, int col) {

        if (!board.isValidPosition(row, col)) {
            return false;
        }

        Cell cell = board.getCell(row, col);

        if (!cell.isEmpty()) {
            return false; // illegal move (occupied)
        }

        cell.setColour(currentPlayer);
        switchTurn();
        return true;
    }

    public Board getBoard() {
        return board;
    }
}
