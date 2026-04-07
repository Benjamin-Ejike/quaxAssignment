import java.util.ArrayList;
import java.util.List;

public class Game {

    private Board board;
    private Colour currentPlayer;

    // Field to store the winning path for the UI glow
    private List<int[]> winningPath = new ArrayList<>();

    public Game() {
        startGame();
    }

    public void startGame() {
        board = new Board();
        currentPlayer = Colour.BLACK; // black starts
        gameOver = false;
        winner = null;
        winningPath.clear();
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

        // stop all stone placement after the game is over
        if (gameOver) {
            return false;
        }

        // try to place the stone on the board
        boolean success = board.placeStone(row, col, currentPlayer);

        // reject invalid move
        if (!success) {
            return false;
        }

        // after placing, check if the current player has won
        if (currentPlayer == Colour.BLACK && blackWins()) {
            gameOver = true;
            winner = Colour.BLACK;
            calculateWinningPath(); // SPRINT 4: Find the path after win detected
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
            calculateWinningPath(); // SPRINT 4: Find the path after win detected
            return true;
        }

        // only switch turn if nobody has won
        switchTurn();
        return true;
    }

    // helper method
    private boolean hasMatchingRhombusBetween(int row1, int col1, int row2, int col2, Colour colour) {

        if (Math.abs(row1 - row2) != 1 || Math.abs(col1 - col2) != 1) {
            return false;
        }

        int r = Math.min(row1, row2);
        int c = Math.min(col1, col2);

        if (rhombicStones == null)
            return false;

        Colour rhomb = rhombicStones[r][c];
        return rhomb != null && rhomb == colour;
    }

    // win conditions
    // check if black has made a full connection from the top row to the bottom row
    public boolean blackWins() {
        int size = board.getSize();

        // keeps track of which cells we have already checked
        // so we do not keep visiting the same cells again
        boolean[][] visited = new boolean[size][size];

        // black wins by connecting top to bottom
        // so checks starting from every black cell in the top row
        for (int col = 0; col < size; col++) {
            Cell start = board.getCell(0, col);

            // only begin searching from cells that actually contain a black stone
            if (start != null && start.getColor() == Colour.BLACK) {
                if (searchBlack(0, col, visited)) {
                    return true;
                }
            }
        }

        // if none of the searches reached the bottom row then black has not won
        return false;
    }

    // search for a black path from the current cell to the bottom row
    private boolean searchBlack(int row, int col, boolean[][] visited) {
        int size = board.getSize();

        // if we have already reached the bottom row then black has won
        if (row == size - 1) {
            return true;
        }

        // mark this cell as visited so not have to search it again
        visited[row][col] = true;

        // these are the normal orthogonal directions
        // black & white can always connect through these if the stone next to it is the same colour
        int[][] orthogonalDirections = {
            {-1, 0}, // up
            {1, 0},  // down
            {0, -1}, // left
            {0, 1}   // right
        };

        // first checks all orthogonal neighbours
        for (int[] d : orthogonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            // make sure the neighbour is still inside the board
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {

                // only continue if havent already visited that cell
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);

                    // if the neighbour has a black stone then it is part of the same chain
                    if (next != null && next.getColor() == Colour.BLACK) {
                        if (searchBlack(newRow, newCol, visited)) {
                            return true;
                        }
                    }
                }
            }
        }

        // checking diagonal neighbours
        // diagonal movement is only allowed if there is a matching black rhombic tile between the two cells
        int[][] diagonalDirections = {
            {-1, -1}, // up left
            {-1, 1},  // up right
            {1, -1},  // down left
            {1, 1}    // down right
        };

        for (int[] d : diagonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            // make sure the diagonal neighbour is inside the board
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {

                // only continue if we have not already visited that cell
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);

                    // the diagonal cell must contain a black stone
                    if (next != null && next.getColor() == Colour.BLACK) {

                        // there must also be a black rhombic connection between the current cell and the diagonal cell
                        if (hasMatchingRhombusBetween(row, col, newRow, newCol, Colour.BLACK)) {
                            if (searchBlack(newRow, newCol, visited)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        // if no orthogonal or diagonal route reaches the bottom row then this path does not win
        return false;
    }

    // checking if white has done a full connection from the left column to the right column
    public boolean whiteWins() {
        int size = board.getSize();

        // keeps track of the cells already searched
        boolean[][] visited = new boolean[size][size];

        // white wins by connecting left to right
        // so start from every white cell in the left column
        for (int row = 0; row < size; row++) {
            Cell start = board.getCell(row, 0);

            // only begin searching from cells that contain a white stone
            if (start != null && start.getColor() == Colour.WHITE) {
                if (searchWhite(row, 0, visited)) {
                    return true;
                }
            }
        }

        // if no search reached the right side then white hasnt won
        return false;
    }

    // search for a white path from the current cell to the right column
    private boolean searchWhite(int row, int col, boolean[][] visited) {
        int size = board.getSize();

        // if we have reached the right column then white has won
        if (col == size - 1) {
            return true;
        }

        // mark this cell as visited so don't have to search it again
        visited[row][col] = true;

        // these are the standard orthogonal directions
        int[][] orthogonalDirections = {
            {-1, 0}, // up
            {1, 0},  // down
            {0, -1}, // left
            {0, 1}   // right
        };

        // first check all orthogonal neighbours
        for (int[] d : orthogonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            // make sure neighbour is inside the board
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {

                // only continue if this cell hasnt already been visited
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);

                    // if the neighbour contains a white stone then it may continue the chain
                    if (next != null && next.getColor() == Colour.WHITE) {
                        if (searchWhite(newRow, newCol, visited)) {
                            return true;
                        }
                    }
                }
            }
        }

        // check diagonal neighbours
        // diagonal movement is only allowed when there is a matching white rhombic tile between the two cells
        int[][] diagonalDirections = {
            {-1, -1}, // up left
            {-1, 1},  // up right
            {1, -1},  // down left
            {1, 1}    // down right
        };

        for (int[] d : diagonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            // make sure diagonal neighbour is inside the board
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {

                // only continue if the diagonal cell has not already been visited
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);

                    // the diagonal cell must contain a white stone
                    if (next != null && next.getColor() == Colour.WHITE) {

                        // there must also be a white rhombic connection between the two cells
                        if (hasMatchingRhombusBetween(row, col, newRow, newCol, Colour.WHITE)) {
                            if (searchWhite(newRow, newCol, visited)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        // if no route reaches the right side then white has not won through this path
        return false;
    }

    // store reference to rhombic tiles from ui temporarily
    private Colour[][] rhombicStones;

    public void setRhombicStones(Colour[][] rhombicStones) {
        this.rhombicStones = rhombicStones;
    }

    public boolean placeRhombus(int row, int col) {

        // stop rhombic placement after the game is over
        if (gameOver) {
            return false;
        }

        // make sure rhombic storage exists
        if (rhombicStones == null) {
            return false;
        }

        // check bounds for the rhombic grid
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return false;
        }

        // reject occupied rhombic position
        if (rhombicStones[row][col] != null) {
            return false;
        }

        // place the rhombic tile
        rhombicStones[row][col] = currentPlayer;

        // only switch turn if nobody has won
        if (currentPlayer == Colour.BLACK && blackWins()) {
            gameOver = true;
            winner = Colour.BLACK;
            calculateWinningPath(); // SPRINT 4 addition
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
            calculateWinningPath(); // SPRINT 4 addition
            return true;
        }

        switchTurn();
        return true;
    }

    public void applyPieRule() {
    }

    // The actual brain of the bot: decides exactly where to place its stone
    public int[] makeBotMove() {
        // Main Strategy: Try to build a straight horizontal line across the middle (row 5)
        int targetRow = 5;
        for (int col = 0; col < board.getSize(); col++) {
            // Find the first empty hole in row 5 from left to right
            if (board.isCellEmpty(targetRow, col)) {
                if (placeStone(targetRow, col)) {
                    return new int[] {targetRow, col};
                }
            }
        }

        // Fallback Strategy: If row 5 is completely full, just find ANY empty hole on the board
        for (int r = 0; r < board.getSize(); r++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.isCellEmpty(r, col)) {
                    if (placeStone(r, col)) {
                        return new int[] {r, col};
                    }
                }
            }
        }

        return null;
    }

    // tracks whether the game has ended
    private boolean gameOver = false;

    // stores the winner when the game ends
    private Colour winner = null;

    // returns true if the game is finished
    public boolean isGameOver() {
        return gameOver;
    }

    // returns the winner, or null if nobody has won yet
    public Colour getWinner() {
        return winner;
    }

    public Board getBoard() {
        return board;
    }

    // Finds the largest number of connected stones (longest chain) for a specific player
    public int getLongestChain(Colour col) {
        int max = 0; // Stores the size of the biggest chain found so far
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size]; // Tracks which cells we've already counted

        // Scan every cell on the board
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // If we find an unvisited stone belonging to the requested colour
                if (!visited[r][c] && board.getCell(r, c).getColor() == col) {
                    // Count its entire connected group and update 'max' if it's the biggest one yet
                    max = Math.max(max, countChainRecursive(r, c, col, visited));
                }
            }
        }
        return max; // Return the size of the longest chain found
    }

    // Recursively explores and counts all connected stones in a single group
    private int countChainRecursive(int r, int c, Colour col, boolean[][] visited) {
        visited[r][c] = true; // Mark current stone as visited so we don't count it twice
        int count = 1;        // Start the count at 1 (for the current stone itself)

        // Directions to check: 4 straight (up/down/left/right) and 4 diagonal
        int[][] dirs = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1]; // Calculate the neighbour's coordinates

            // Check if the neighbour is on the board, hasn't been visited, and matches our colour
            if (nr >= 0 && nr < 11 && nc >= 0 && nc < 11 && !visited[nr][nc] && board.getCell(nr, nc).getColor() == col) {

                // If moving diagonally (both row and col change by 1)
                if (Math.abs(d[0]) == 1 && Math.abs(d[1]) == 1) {
                    // Only count it if there is a matching rhombic tile connecting them
                    if (hasMatchingRhombusBetween(r, c, nr, nc, col)) {
                        count += countChainRecursive(nr, nc, col, visited);
                    }
                } else {
                    // Straight moves are always connected, no rhombic tile needed
                    count += countChainRecursive(nr, nc, col, visited);
                }
            }
        }
        return count; // Return the total number of connected stones in this chain
    }

    // Determines the winning player and triggers the search to highlight their winning path
    private void calculateWinningPath() {
        winningPath.clear(); // Clear out any old path data
        int size = board.getSize();

        if (winner == Colour.BLACK) {
            // Black wins by connecting top to bottom.
            // Start searching from every black stone in the top row (row 0)
            for (int c = 0; c < size; c++) {
                if (board.getCell(0, c).getColor() == Colour.BLACK) {
                    // If we successfully trace a path to the bottom, stop searching
                    if (findPathTrace(0, c, Colour.BLACK, true, new boolean[size][size]))
                        break;
                }
            }
        } else if (winner == Colour.WHITE) {
            // White wins by connecting left to right.
            // Start searching from every white stone in the left column (col 0)
            for (int r = 0; r < size; r++) {
                if (board.getCell(r, 0).getColor() == Colour.WHITE) {
                    // If we successfully trace a path to the right side, stop searching
                    if (findPathTrace(r, 0, Colour.WHITE, false, new boolean[size][size]))
                        break;
                }
            }
        }
    }

    // Traces the exact steps of the winning connection, backing up (backtracking) if it hits a dead end
    private boolean findPathTrace(int r, int c, Colour col, boolean isBlack, boolean[][] v) {
        v[r][c] = true; // Mark this stone as visited during our search
        winningPath.add(new int[] {r, c}); // Temporarily add this stone to our winning path sequence

        // Base case: Did we reach the opposite side of the board?
        // Black looks for the bottom row; White looks for the rightmost column.
        if ((isBlack && r == board.getSize() - 1) || (!isBlack && c == board.getSize() - 1)) {
            return true; // We found the finish line! Keep the path.
        }

        // Try moving to all 8 surrounding neighbours
        int[][] dirs = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1]; // Neighbour coordinates

            // If neighbour is on the board, unvisited, and is the correct colour
            if (nr >= 0 && nr < 11 && nc >= 0 && nc < 11 && !v[nr][nc] && board.getCell(nr, nc).getColor() == col) {

                // If it's a straight move, OR if it's diagonal WITH a matching rhombus tile connecting them:
                if (Math.abs(d[0]) != 1 || Math.abs(d[1]) != 1 || hasMatchingRhombusBetween(r, c, nr, nc, col)) {

                    // Recursively move forward. If it eventually reaches the end, return true.
                    if (findPathTrace(nr, nc, col, isBlack, v)) {
                        return true;
                    }
                }
            }
        }

        // Dead End: We explored all options from this stone and didn't reach the end.
        // Remove this stone from the path list (backtrack) and return false.
        winningPath.remove(winningPath.size() - 1);
        return false;
    }

    //Identifies winning path and gives neon winning glow
    public List<int[]> getWinningPath() {
        return winningPath;
    }

}