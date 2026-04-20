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
            calculateWinningPath();
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
            calculateWinningPath();
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

        if (rhombicStones == null) return false;

        Colour rhomb = rhombicStones[r][c];
        return rhomb != null && rhomb == colour;
    }

    // check if black has made a full connection from the top row to the bottom row
    public boolean blackWins() {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        for (int col = 0; col < size; col++) {
            Cell start = board.getCell(0, col);
            if (start != null && start.getColor() == Colour.BLACK) {
                if (searchBlack(0, col, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchBlack(int row, int col, boolean[][] visited) {
        int size = board.getSize();

        if (row == size - 1) {
            return true;
        }

        visited[row][col] = true;

        int[][] orthogonalDirections = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
        };

        for (int[] d : orthogonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);
                    if (next != null && next.getColor() == Colour.BLACK) {
                        if (searchBlack(newRow, newCol, visited)) {
                            return true;
                        }
                    }
                }
            }
        }

        int[][] diagonalDirections = {
                {-1, -1}, // up left
                {-1, 1},  // up right
                {1, -1},  // down left
                {1, 1}    // down right
        };

        for (int[] d : diagonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);
                    if (next != null && next.getColor() == Colour.BLACK) {
                        if (hasMatchingRhombusBetween(row, col, newRow, newCol, Colour.BLACK)) {
                            if (searchBlack(newRow, newCol, visited)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // check if white has done a full connection from the left column to the right column
    public boolean whiteWins() {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        for (int row = 0; row < size; row++) {
            Cell start = board.getCell(row, 0);
            if (start != null && start.getColor() == Colour.WHITE) {
                if (searchWhite(row, 0, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchWhite(int row, int col, boolean[][] visited) {
        int size = board.getSize();

        if (col == size - 1) {
            return true;
        }

        visited[row][col] = true;

        int[][] orthogonalDirections = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
        };

        for (int[] d : orthogonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);
                    if (next != null && next.getColor() == Colour.WHITE) {
                        if (searchWhite(newRow, newCol, visited)) {
                            return true;
                        }
                    }
                }
            }
        }

        int[][] diagonalDirections = {
                {-1, -1}, // up left
                {-1, 1},  // up right
                {1, -1},  // down left
                {1, 1}    // down right
        };

        for (int[] d : diagonalDirections) {
            int newRow = row + d[0];
            int newCol = col + d[1];

            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                if (!visited[newRow][newCol]) {
                    Cell next = board.getCell(newRow, newCol);
                    if (next != null && next.getColor() == Colour.WHITE) {
                        if (hasMatchingRhombusBetween(row, col, newRow, newCol, Colour.WHITE)) {
                            if (searchWhite(newRow, newCol, visited)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // store reference to rhombic tiles from ui
    private Colour[][] rhombicStones;

    public void setRhombicStones(Colour[][] rhombicStones) {
        this.rhombicStones = rhombicStones;
    }

    public boolean placeRhombus(int row, int col) {

        if (gameOver) return false;
        if (rhombicStones == null) return false;
        if (row < 0 || row >= 10 || col < 0 || col >= 10) return false;
        if (rhombicStones[row][col] != null) return false;

        rhombicStones[row][col] = currentPlayer;

        if (currentPlayer == Colour.BLACK && blackWins()) {
            gameOver = true;
            winner = Colour.BLACK;
            calculateWinningPath();
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
            calculateWinningPath();
            return true;
        }

        switchTurn();
        return true;
    }

    private boolean gameOver = false;
    private Colour winner = null;

    public boolean isGameOver() { return gameOver; }
    public Colour getWinner()   { return winner; }
    public Board getBoard()     { return board; }

    // =========================================================================
    //  CHAIN ANALYSIS
    // =========================================================================

    public int getLongestChain(Colour col) {
        int max = 0;
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!visited[r][c] && board.getCell(r, c).getColor() == col) {
                    max = Math.max(max, countChainRecursive(r, c, col, visited));
                }
            }
        }
        return max;
    }

    private int countChainRecursive(int r, int c, Colour col, boolean[][] visited) {
        visited[r][c] = true;
        int count = 1;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];

            if (nr >= 0 && nr < 11 && nc >= 0 && nc < 11
                    && !visited[nr][nc]
                    && board.getCell(nr, nc).getColor() == col) {

                if (Math.abs(d[0]) == 1 && Math.abs(d[1]) == 1) {
                    if (hasMatchingRhombusBetween(r, c, nr, nc, col)) {
                        count += countChainRecursive(nr, nc, col, visited);
                    }
                } else {
                    count += countChainRecursive(nr, nc, col, visited);
                }
            }
        }
        return count;
    }

    // =========================================================================
    //  WINNING PATH
    // =========================================================================

    private void calculateWinningPath() {
        winningPath.clear();
        int size = board.getSize();

        if (winner == Colour.BLACK) {
            for (int c = 0; c < size; c++) {
                if (board.getCell(0, c).getColor() == Colour.BLACK) {
                    if (findPathTrace(0, c, Colour.BLACK, true, new boolean[size][size])) break;
                }
            }
        } else if (winner == Colour.WHITE) {
            for (int r = 0; r < size; r++) {
                if (board.getCell(r, 0).getColor() == Colour.WHITE) {
                    if (findPathTrace(r, 0, Colour.WHITE, false, new boolean[size][size])) break;
                }
            }
        }
    }

    private boolean findPathTrace(int r, int c, Colour col, boolean isBlack, boolean[][] v) {
        v[r][c] = true;
        winningPath.add(new int[]{r, c});

        if ((isBlack && r == board.getSize() - 1) || (!isBlack && c == board.getSize() - 1)) {
            return true;
        }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];

            if (nr >= 0 && nr < 11 && nc >= 0 && nc < 11
                    && !v[nr][nc]
                    && board.getCell(nr, nc).getColor() == col) {

                if (Math.abs(d[0]) != 1 || Math.abs(d[1]) != 1
                        || hasMatchingRhombusBetween(r, c, nr, nc, col)) {
                    if (findPathTrace(nr, nc, col, isBlack, v)) {
                        return true;
                    }
                }
            }
        }

        winningPath.remove(winningPath.size() - 1);
        return false;
    }

    public List<int[]> getWinningPath() { return winningPath; }

    // =========================================================================
    //  BOT LOGIC
    // =========================================================================

    public int[] makeBotMove() {

        Colour botColour   = currentPlayer;
        Colour humanColour = (botColour == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

        // Priority 1: win immediately
        int[] winMove = findWinningMove(botColour);
        if (winMove != null) {
            placeStone(winMove[0], winMove[1]);
            return winMove;
        }

        // Priority 2: block human from winning immediately
        int[] blockMove = findWinningMove(humanColour);
        if (blockMove != null) {
            placeStone(blockMove[0], blockMove[1]);
            return blockMove;
        }

        // Priority 3: advance own path with diagonal awareness
        int[] advanceMove = findAdvanceMove(botColour, humanColour);
        if (advanceMove != null) {
            placeStone(advanceMove[0], advanceMove[1]);
            return advanceMove;
        }

        // Priority 4: block the human's strongest threat with diagonal intercept
        int[] threatBlock = findThreatBlock(humanColour);
        if (threatBlock != null) {
            placeStone(threatBlock[0], threatBlock[1]);
            return threatBlock;
        }

        // Priority 5: fallback — any empty cell adjacent to own chain
        int[] extension = findExtensionMove(botColour);
        if (extension != null) {
            placeStone(extension[0], extension[1]);
            return extension;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    //  ADVANCE OWN PATH
    // -------------------------------------------------------------------------

    /**
     * Finds the best move to push the bot's own crossing path forward.
     *
     * For each step forward (straight or diagonal), the bot checks:
     *   1. Can I go straight ahead? → do it
     *   2. Is straight ahead blocked by the human? → try diagonal detour
     *   3. Prefer the diagonal closest to the centre lane
     *
     * WHITE advances left → right (by column).
     * BLACK advances top → bottom (by row).
     */
    private int[] findAdvanceMove(Colour botColour, Colour humanColour) {
        int size   = board.getSize();
        int centre = size / 2;

        // find the frontier: where the bot's chain has reached so far
        int frontierProgress = -1;  // furthest column (WHITE) or row (BLACK)
        int frontierLane     = centre; // row (WHITE) or column (BLACK) of the frontier stone

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = board.getCell(r, c);
                if (cell == null || cell.getColor() != botColour) continue;

                int progress = (botColour == Colour.WHITE) ? c : r;
                int lane     = (botColour == Colour.WHITE) ? r : c;

                if (progress > frontierProgress) {
                    frontierProgress = progress;
                    frontierLane     = lane;
                }
            }
        }

        // no stones yet: start from the correct starting edge at centre lane
        if (frontierProgress == -1) {
            if (botColour == Colour.WHITE) {
                return findEmptyInLane(0, centre, true, size);
            } else {
                return findEmptyInLane(centre, 0, false, size);
            }
        }

        // try to advance 1 or 2 steps forward from the frontier
        for (int step = 1; step <= 2; step++) {
            int nextProgress = frontierProgress + step;
            if (nextProgress >= size) continue;

            // ── straight ahead (same lane) ────────────────────────────────────
            int[] straight = (botColour == Colour.WHITE)
                    ? new int[]{frontierLane, nextProgress}
                    : new int[]{nextProgress, frontierLane};

            if (board.isCellEmpty(straight[0], straight[1])) {
                return straight;
            }

            // straight is occupied — check if it is the human blocking us
            Cell blocker = board.getCell(straight[0], straight[1]);
            boolean humanIsBlocking = (blocker != null && blocker.getColor() == humanColour);

            if (humanIsBlocking) {
                // ── diagonal detour ───────────────────────────────────────────
                // try one lane above and one below, prefer closer to centre
                int[] diagonalLanes = sortByDistanceToCenter(
                        frontierLane - 1, frontierLane + 1, centre, size);

                for (int diagLane : diagonalLanes) {
                    int[] diagCell = (botColour == Colour.WHITE)
                            ? new int[]{diagLane, nextProgress}
                            : new int[]{nextProgress, diagLane};

                    if (board.isCellEmpty(diagCell[0], diagCell[1])) {
                        return diagCell;
                    }
                }
            }
        }

        return null;
    }

    // returns the two lane options sorted so the one closer to centre comes first
    private int[] sortByDistanceToCenter(int laneA, int laneB, int centre, int size) {
        List<Integer> options = new ArrayList<>();
        if (laneA >= 0 && laneA < size) options.add(laneA);
        if (laneB >= 0 && laneB < size) options.add(laneB);

        options.sort((a, b) -> Math.abs(a - centre) - Math.abs(b - centre));

        int[] result = new int[options.size()];
        for (int i = 0; i < options.size(); i++) result[i] = options.get(i);
        return result;
    }

    // find the first empty cell in a lane starting from a given position
    private int[] findEmptyInLane(int startRow, int startCol, boolean isWhite, int size) {
        if (isWhite) {
            // WHITE: scan across the row
            for (int c = startCol; c < size; c++) {
                if (board.isCellEmpty(startRow, c)) return new int[]{startRow, c};
            }
        } else {
            // BLACK: scan down the column
            for (int r = startRow; r < size; r++) {
                if (board.isCellEmpty(r, startCol)) return new int[]{r, startCol};
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    //  BLOCK HUMAN THREAT
    // -------------------------------------------------------------------------

    /**
     * Finds the human's most advanced chain and places a blocking stone
     * directly in its path — straight or diagonally if that path is also blocked.
     */
    private int[] findThreatBlock(Colour humanColour) {
        int size             = board.getSize();
        int dangerThreshold  = 3;
        int centre           = size / 2;

        if (humanColour == Colour.BLACK) {
            // BLACK goes top → bottom, track deepest row per column
            int bestCol   = -1;
            int bestDepth = 0;

            for (int c = 0; c < size; c++) {
                int depth = 0;
                for (int r = 0; r < size; r++) {
                    Cell cell = board.getCell(r, c);
                    if (cell != null && cell.getColor() == Colour.BLACK) depth = r + 1;
                }
                if (depth > bestDepth) { bestDepth = depth; bestCol = c; }
            }

            if (bestDepth >= dangerThreshold && bestCol != -1) {

                // try straight block: same column, just below the chain
                for (int r = bestDepth; r < size; r++) {
                    if (board.isCellEmpty(r, bestCol)) return new int[]{r, bestCol};
                }

                // straight column is full: try diagonal intercept
                // place at the same depth but in an adjacent column
                int[] diagCols = sortByDistanceToCenter(bestCol - 1, bestCol + 1, centre, size);
                for (int dc : diagCols) {
                    if (board.isCellEmpty(bestDepth, dc)) return new int[]{bestDepth, dc};
                    if (bestDepth - 1 >= 0 && board.isCellEmpty(bestDepth - 1, dc)) {
                        return new int[]{bestDepth - 1, dc};
                    }
                }
            }

        } else {
            // WHITE goes left → right, track furthest column per row
            int bestRow   = -1;
            int bestDepth = 0;

            for (int r = 0; r < size; r++) {
                int depth = 0;
                for (int c = 0; c < size; c++) {
                    Cell cell = board.getCell(r, c);
                    if (cell != null && cell.getColor() == Colour.WHITE) depth = c + 1;
                }
                if (depth > bestDepth) { bestDepth = depth; bestRow = r; }
            }

            if (bestDepth >= dangerThreshold && bestRow != -1) {

                // try straight block: same row, just ahead of the chain
                for (int c = bestDepth; c < size; c++) {
                    if (board.isCellEmpty(bestRow, c)) return new int[]{bestRow, c};
                }

                // straight row is full: try diagonal intercept
                int[] diagRows = sortByDistanceToCenter(bestRow - 1, bestRow + 1, centre, size);
                for (int dr : diagRows) {
                    if (board.isCellEmpty(dr, bestDepth)) return new int[]{dr, bestDepth};
                    if (bestDepth - 1 >= 0 && board.isCellEmpty(dr, bestDepth - 1)) {
                        return new int[]{dr, bestDepth - 1};
                    }
                }
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    //  HELPERS
    // -------------------------------------------------------------------------

    // checks if placing a stone for [colour] anywhere wins immediately
    private int[] findWinningMove(Colour colour) {
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!board.isCellEmpty(r, c)) continue;

                board.placeStone(r, c, colour);
                boolean wins = (colour == Colour.BLACK) ? blackWins() : whiteWins();
                board.getCell(r, c).setColour(null);

                if (wins) return new int[]{r, c};
            }
        }
        return null;
    }

    // fallback: find any empty cell next to an existing friendly stone
    // that moves in the right direction
    private int[] findExtensionMove(Colour colour) {
        int size   = board.getSize();
        int centre = size / 2;
        int best   = Integer.MIN_VALUE;
        int[] result = null;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!board.isCellEmpty(r, c)) continue;
                if (countFriendlyNeighbours(r, c, colour) == 0) continue;

                int score = (colour == Colour.WHITE)
                        ? c * 10 + (size - Math.abs(r - centre)) * 2
                        : r * 10 + (size - Math.abs(c - centre)) * 2;

                if (score > best) { best = score; result = new int[]{r, c}; }
            }
        }
        return result;
    }

    // counts how many friendly stones are adjacent to (row, col)
    private int countFriendlyNeighbours(int row, int col, Colour colour) {
        int count = 0;
        int size  = board.getSize();

        int[][] directions = {
                {-1,0},{1,0},{0,-1},{0,1},
                {-1,-1},{-1,1},{1,-1},{1,1}
        };

        for (int[] d : directions) {
            int nr = row + d[0], nc = col + d[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                Cell neighbour = board.getCell(nr, nc);
                if (neighbour != null && neighbour.getColor() == colour) count++;
            }
        }
        return count;
    }
}
