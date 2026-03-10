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
        currentPlayer = Colour.BLACK;   // BLACK always starts
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

    // Place stone in octagonal cell
    public boolean placeStone(int row, int col) {

        if (!board.isValidPosition(row, col)) {
            return false;
        }

        Cell cell = board.getCell(row, col);

        if (!cell.isEmpty()) {
            return false; // illegal move (already occupied)
        }

        cell.setColour(currentPlayer);

        moveCount++;

        switchTurn();

        return true;
    }

    // Apply the Pie Rule
    public void applyPieRule() {

        // Pie rule only allowed after the first move
        if (moveCount == 1 && !pieRuleUsed) {

            // Swap players
            if (currentPlayer == Colour.WHITE) {
                currentPlayer = Colour.BLACK;
            } else {
                currentPlayer = Colour.WHITE;
            }

            pieRuleUsed = true;
        }
    }

    // Helper methods (useful for UI)
    public boolean isFirstMove() {
        return moveCount == 1;
    }

    public boolean isPieRuleUsed() {
        return pieRuleUsed;
    }

}