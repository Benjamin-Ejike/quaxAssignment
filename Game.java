public class Game {

    private Board board;
    private Colour currentPlayer;

    // Track game state for Pie Rule
    private int moveCount;
    private boolean pieRuleUsed;

    public Game() {
        startGame();
    }

    public void startGame() {
        board = new Board();
        currentPlayer = Colour.BLACK; // BLACK starts
        moveCount = 0;
        pieRuleUsed = false;
    }

    public Colour getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getBoard() {
        return board;
    }

    public void switchTurn() {
        if (currentPlayer == Colour.BLACK) {
            currentPlayer = Colour.WHITE;
        } else {
            currentPlayer = Colour.BLACK;
        }
    }

    // Place stone
    public boolean placeStone(int row, int col) {

        if (!board.isValidPosition(row, col)) {
            return false;
        }

        Cell cell = board.getCell(row, col);

        if (!cell.isEmpty()) {
            return false;
        }

        cell.setColour(currentPlayer);

        moveCount++;

        switchTurn();

        return true;
    }

    // Apply Pie Rule
    public void applyPieRule() {

        if (moveCount == 1 && !pieRuleUsed) {

            switchTurn();

            pieRuleUsed = true;
        }
    }

    public boolean isFirstMove() {
        return moveCount == 1;
    }

    public boolean isPieRuleUsed() {
        return pieRuleUsed;
    }
}