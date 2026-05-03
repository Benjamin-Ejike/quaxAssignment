import java.util.ArrayList;
import java.util.List;

public class Game {

    private Board board;
    private Colour currentPlayer;

    // Field to store the winning path for the UI glow
    private List<int[]> winningPath = new ArrayList<>();

    // The lane the bot is currently committed to driving through.
    // -1 = not yet chosen. Reset only when the lane becomes fully blocked.
    // BLACK: committedLane = column index (0-10)
    // WHITE: committedLane = row index (0-10)
    private int committedLane = -1;

    // store reference to rhombic tiles from ui
    private Colour[][] rhombicStones;

    // added to keep pie rule state in the game layer instead of the ui
    private boolean pieRuleAvailable = false;
    private boolean pieRuleHandled = false;
    private boolean firstMoveDone = false;

    // added to track bot/player roles correctly after swap
    private Colour botColour = Colour.BLACK;
    private Colour humanColour = Colour.WHITE;

    public Game() {
        startGame();
    }

    public void startGame() {
        board = new Board();
        currentPlayer = Colour.BLACK; // black starts
        gameOver = false;
        winner = null;
        rhombicStones = new Colour[10][10];
        committedLane = -1;
        winningPath.clear();

        // added to reset pie rule and role state on new game
        pieRuleAvailable = false;
        pieRuleHandled = false;
        firstMoveDone = false;
        botColour = Colour.BLACK;
        humanColour = Colour.WHITE;
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

    public void applyPieRule() {
        if (!pieRuleAvailable || pieRuleHandled) return;

        Colour temp = botColour;
        botColour = humanColour;
        humanColour = temp;

        // keep currentPlayer unchanged, because the next colour to act
        // after the first move is still WHITE
        // after the swap, WHITE now belongs to the bot

        committedLane = -1;
        pieRuleHandled = true;
        pieRuleAvailable = false;
    }
    // added so the ui can close pie rule state without owning it
    public void declinePieRule() {
        if (!pieRuleAvailable || pieRuleHandled) return;
        pieRuleHandled = true;
        pieRuleAvailable = false;
    }

    public boolean isPieRuleAvailable() {
        return pieRuleAvailable;
    }

    public boolean isPieRuleHandled() {
        return pieRuleHandled;
    }

    public Colour getBotColour() {
        return botColour;
    }

    public Colour getHumanColour() {
        return humanColour;
    }

    public boolean placeStone(int row, int col) {

        if (gameOver) return false;

        boolean success = board.placeStone(row, col, currentPlayer);
        if (!success) return false;

        // added to make pie rule state part of the game logic
        if (!firstMoveDone) {
            firstMoveDone = true;
            pieRuleAvailable = true;
            pieRuleHandled = false;
        } else {
            pieRuleAvailable = false;
            pieRuleHandled = true;
        }

        if (currentPlayer == Colour.BLACK && blackWins()) {
            gameOver = true; winner = Colour.BLACK;
            calculateWinningPath();
            return true;
        }
        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true; winner = Colour.WHITE;
            calculateWinningPath();
            return true;
        }

        switchTurn();
        return true;
    }

    // helper method
    private boolean hasMatchingRhombusBetween(int row1, int col1, int row2, int col2, Colour colour) {
        if (Math.abs(row1 - row2) != 1 || Math.abs(col1 - col2) != 1) return false;
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
            if (start != null && start.getColor() == Colour.BLACK)
                if (searchBlack(0, col, visited)) return true;
        }
        return false;
    }

    private boolean searchBlack(int row, int col, boolean[][] visited) {
        int size = board.getSize();
        if (row == size - 1) return true;
        visited[row][col] = true;

        int[][] ortho = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : ortho) {
            int nr = row+d[0], nc = col+d[1];
            if (nr>=0&&nr<size&&nc>=0&&nc<size&&!visited[nr][nc]) {
                Cell next = board.getCell(nr, nc);
                if (next!=null&&next.getColor()==Colour.BLACK)
                    if (searchBlack(nr, nc, visited)) return true;
            }
        }

        int[][] diag = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : diag) {
            int nr = row+d[0], nc = col+d[1];
            if (nr>=0&&nr<size&&nc>=0&&nc<size&&!visited[nr][nc]) {
                Cell next = board.getCell(nr, nc);
                if (next!=null&&next.getColor()==Colour.BLACK)
                    if (hasMatchingRhombusBetween(row,col,nr,nc,Colour.BLACK))
                        if (searchBlack(nr, nc, visited)) return true;
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
            if (start != null && start.getColor() == Colour.WHITE)
                if (searchWhite(row, 0, visited)) return true;
        }
        return false;
    }

    private boolean searchWhite(int row, int col, boolean[][] visited) {
        int size = board.getSize();
        if (col == size - 1) return true;
        visited[row][col] = true;

        int[][] ortho = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : ortho) {
            int nr = row+d[0], nc = col+d[1];
            if (nr>=0&&nr<size&&nc>=0&&nc<size&&!visited[nr][nc]) {
                Cell next = board.getCell(nr, nc);
                if (next!=null&&next.getColor()==Colour.WHITE)
                    if (searchWhite(nr, nc, visited)) return true;
            }
        }

        int[][] diag = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : diag) {
            int nr = row+d[0], nc = col+d[1];
            if (nr>=0&&nr<size&&nc>=0&&nc<size&&!visited[nr][nc]) {
                Cell next = board.getCell(nr, nc);
                if (next!=null&&next.getColor()==Colour.WHITE)
                    if (hasMatchingRhombusBetween(row,col,nr,nc,Colour.WHITE))
                        if (searchWhite(nr, nc, visited)) return true;
            }
        }
        return false;
    }

    public void setRhombicStones(Colour[][] stones) { this.rhombicStones = stones; }

    public Colour[][] getRhombicStones() { return rhombicStones; }

    public boolean placeRhombus(int row, int col) {
        if (gameOver) return false;
        //if no rhombus exist- reject move
        if (rhombicStones == null) {
            rhombicStones = new Colour[10][10];
        }
        if (row < 0 || row >= 10 || col < 0 || col >= 10) return false;
        if (rhombicStones[row][col] != null) return false;

        // A rhombic cell at [row][col] sits at the gap between four octagons.
        // It can be claimed if EITHER diagonal pair belongs to the current player:
        //   \ diagonal: oct[row][col] and oct[row+1][col+1]
        //   /  diagonal: oct[row][col+1] and oct[row+1][col]
        Cell tl = board.getCell(row,     col);       // top-left
        Cell br = board.getCell(row + 1, col + 1);   // bottom-right
        Cell tr = board.getCell(row,     col + 1);   // top-right
        Cell bl = board.getCell(row + 1, col);       // bottom-left

        boolean backslash = tl != null && tl.getColor() == currentPlayer
                && br != null && br.getColor() == currentPlayer;
        boolean slash     = tr != null && tr.getColor() == currentPlayer
                && bl != null && bl.getColor() == currentPlayer;

        if (!backslash && !slash) return false;

        rhombicStones[row][col] = currentPlayer;

        // added to make pie rule state part of the game logic
        if (!firstMoveDone) {
            firstMoveDone = true;
            pieRuleAvailable = true;
            pieRuleHandled = false;
        } else {
            pieRuleAvailable = false;
            pieRuleHandled = true;
        }

        if (currentPlayer == Colour.BLACK && blackWins()) {
            gameOver = true; winner = Colour.BLACK; calculateWinningPath(); return true;
        }
        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true; winner = Colour.WHITE; calculateWinningPath(); return true;
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
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (!visited[r][c] && board.getCell(r,c).getColor()==col)
                    max = Math.max(max, countChainRecursive(r,c,col,visited));
        return max;
    }

    private int countChainRecursive(int r, int c, Colour col, boolean[][] visited) {
        visited[r][c] = true;
        int count = 1;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : dirs) {
            int nr=r+d[0], nc=c+d[1];
            if (nr>=0&&nr<11&&nc>=0&&nc<11&&!visited[nr][nc]&&board.getCell(nr,nc).getColor()==col) {
                if (Math.abs(d[0])==1&&Math.abs(d[1])==1) {
                    if (hasMatchingRhombusBetween(r,c,nr,nc,col))
                        count += countChainRecursive(nr,nc,col,visited);
                } else {
                    count += countChainRecursive(nr,nc,col,visited);
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
            for (int c = 0; c < size; c++)
                if (board.getCell(0,c).getColor()==Colour.BLACK)
                    if (findPathTrace(0,c,Colour.BLACK,true,new boolean[size][size])) break;
        } else if (winner == Colour.WHITE) {
            for (int r = 0; r < size; r++)
                if (board.getCell(r,0).getColor()==Colour.WHITE)
                    if (findPathTrace(r,0,Colour.WHITE,false,new boolean[size][size])) break;
        }
    }

    private boolean findPathTrace(int r, int c, Colour col, boolean isBlack, boolean[][] v) {
        v[r][c] = true;
        winningPath.add(new int[]{r,c});
        if ((isBlack&&r==board.getSize()-1)||(!isBlack&&c==board.getSize()-1)) return true;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : dirs) {
            int nr=r+d[0], nc=c+d[1];
            if (nr>=0&&nr<11&&nc>=0&&nc<11&&!v[nr][nc]&&board.getCell(nr,nc).getColor()==col)
                if (Math.abs(d[0])!=1||Math.abs(d[1])!=1||hasMatchingRhombusBetween(r,c,nr,nc,col))
                    if (findPathTrace(nr,nc,col,isBlack,v)) return true;
        }
        winningPath.remove(winningPath.size()-1);
        return false;
    }

    public List<int[]> getWinningPath() { return winningPath; }

    // =========================================================================
    //  BOT LOGIC
    // =========================================================================

    public int[] makeBotMove() {
        // Use the current player so the bots still works correctly after pie rule
        Colour botColour   = currentPlayer;
        Colour humanColour = (botColour == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;

        // Priority 0: win or set up a win using rhombic tiles
        int[] rhombWin = findWinningRhombicMove(botColour);
        if (rhombWin != null) { placeRhombus(rhombWin[0], rhombWin[1]); return rhombWin; }

        int[] rhombSetup = findRhombicSetupMove(botColour);
        if (rhombSetup != null) { placeRhombus(rhombSetup[0], rhombSetup[1]); return rhombSetup; }

        // Priority 1: win immediately with a stone
        int[] winMove = findWinningMove(botColour);
        if (winMove != null) { placeStone(winMove[0], winMove[1]); return winMove; }

        // Priority 2: block human from winning immediately
        int[] blockMove = findWinningMove(humanColour);
        if (blockMove != null) { placeStone(blockMove[0], blockMove[1]); return blockMove; }

        // Priority 3: lane-based attack — find the clearest lane and advance through it
        int[] laneMove = findLaneAdvance(botColour, humanColour);
        if (laneMove != null) { placeStone(laneMove[0], laneMove[1]); return laneMove; }

        // Priority 4: block the human's strongest threat
        int[] threatBlock = findThreatBlock(humanColour);
        if (threatBlock != null) { placeStone(threatBlock[0], threatBlock[1]); return threatBlock; }

        // Priority 5: fallback — extend any existing bot chain
        int[] extension = findExtensionMove(botColour);
        if (extension != null) { placeStone(extension[0], extension[1]); return extension; }

        return null;
    }

    // -------------------------------------------------------------------------
    //  LANE-BASED ATTACK
    // -------------------------------------------------------------------------

    //Advances along a chosen lane towards its goal
    //If blocked, switches to a nearby lane and continues forward

    private int[] findLaneAdvance(Colour botColour, Colour humanColour) {
        int size   = board.getSize();
        int centre = size / 2;

        // pick committed lane on first move only — centre column for BLACK, centre row for WHITE
        if (committedLane == -1) {
            committedLane = centre;
        }

        // find the frontier: how far we have already gone in the committed lane
        int frontier = 0;
        for (int pos = 0; pos < size; pos++) {
            Cell cell = (botColour == Colour.BLACK)
                    ? board.getCell(pos, committedLane)
                    : board.getCell(committedLane, pos);
            if (cell != null && cell.getColor() == botColour) frontier = pos + 1;
        }

        // try to place the next stone straight ahead in the committed lane
        for (int pos = frontier; pos < size; pos++) {
            int r = (botColour == Colour.BLACK) ? pos            : committedLane;
            int c = (botColour == Colour.BLACK) ? committedLane  : pos;

            Cell cell = board.getCell(r, c);
            if (cell == null) continue;

            if (cell.isEmpty()) {
                // straight ahead is clear — go here
                return new int[]{r, c};
            }

            if (cell.getColor() == humanColour) {
                // human blocking this cell — step one lane sideways at this same row/col
                // prefer the adjacent lane with fewer human stones
                int laneA = committedLane - 1;
                int laneB = committedLane + 1;
                int[] adj = sortByDistanceToCenter(laneA, laneB, centre, size);
                int[] best = pickClearerLane(adj, botColour, humanColour, size);

                for (int adjLane : best) {
                    int dr = (botColour == Colour.BLACK) ? pos     : adjLane;
                    int dc = (botColour == Colour.BLACK) ? adjLane : pos;
                    if (dr >= 0 && dr < size && dc >= 0 && dc < size && board.isCellEmpty(dr, dc)) {
                        committedLane = adjLane;  // switch lane and stay there
                        return new int[]{dr, dc};
                    }
                }
            }
            // own stone — already placed, keep scanning forward
        }

        return null;
    }


    // -------------------------------------------------------------------------
    //  BLOCK HUMAN THREAT
    // -------------------------------------------------------------------------

    // Blocks the human's most advanced chain toward their goal
    private int[] findThreatBlock(Colour humanColour) {
        int size            = board.getSize();
        int dangerThreshold = 2;
        int centre          = size / 2;

        // find the human stone furthest toward their goal
        int frontierRow  = -1;
        int frontierCol  = -1;
        int bestProgress = -1;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = board.getCell(r, c);
                if (cell == null || cell.getColor() != humanColour) continue;
                int progress = (humanColour == Colour.BLACK) ? r : c;
                if (progress > bestProgress) {
                    bestProgress = progress;
                    frontierRow  = r;
                    frontierCol  = c;
                }
            }
        }

        if (bestProgress < dangerThreshold || frontierRow == -1) return null;

        if (humanColour == Colour.BLACK) {
            // BLACK heading downward — try multiple steps ahead
            int[][] forwardDirs = {{1,0},{1,-1},{1,1},{2,0},{2,-1},{2,1}};
            for (int[] d : forwardDirs) {
                int nr = frontierRow+d[0], nc = frontierCol+d[1];
                if (nr>=0&&nr<size&&nc>=0&&nc<size&&board.isCellEmpty(nr,nc))
                    return new int[]{nr,nc};
            }
            // all forward cells occupied: place beside frontier
            int[] diagCols = sortByDistanceToCenter(frontierCol-1, frontierCol+1, centre, size);
            for (int dc : diagCols)
                if (board.isCellEmpty(frontierRow, dc)) return new int[]{frontierRow, dc};

        } else {
            // WHITE heading rightward
            int[][] forwardDirs = {{0,1},{-1,1},{1,1},{0,2},{-1,2},{1,2}};
            for (int[] d : forwardDirs) {
                int nr = frontierRow+d[0], nc = frontierCol+d[1];
                if (nr>=0&&nr<size&&nc>=0&&nc<size&&board.isCellEmpty(nr,nc))
                    return new int[]{nr,nc};
            }
            int[] diagRows = sortByDistanceToCenter(frontierRow-1, frontierRow+1, centre, size);
            for (int dr : diagRows)
                if (board.isCellEmpty(dr, frontierCol)) return new int[]{dr, frontierCol};
        }

        return null;
    }

    // -------------------------------------------------------------------------
    //  HELPERS
    // -------------------------------------------------------------------------

    // checks if placing a rhombic tile creates an immediate stone win on the next move
    // i.e. after placing the rhombic, does findWinningMove return non-null?
    private int[] findRhombicSetupMove(Colour colour) {
        if (rhombicStones == null) return null;
        int size = board.getSize();

        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                if (rhombicStones[r][c] != null) continue;

                // check \ diagonal: bridges oct[r][c] and oct[r+1][c+1]
                Cell tl = board.getCell(r,     c);
                Cell br = board.getCell(r + 1, c + 1);
                boolean drValid = tl != null && tl.getColor() == colour
                        && br != null && br.getColor() == colour;

                // check / diagonal: bridges oct[r][c+1] and oct[r+1][c]
                Cell tr = board.getCell(r,     c + 1);
                Cell bl = board.getCell(r + 1, c);
                boolean dlValid = tr != null && tr.getColor() == colour
                        && bl != null && bl.getColor() == colour;

                if (!drValid && !dlValid) continue;

                // temporarily place the rhombic tile
                rhombicStones[r][c] = colour;

                // check if a winning stone move now exists
                int[] stoneWin = findWinningMove(colour);

                // revert simulated move
                rhombicStones[r][c] = null;

                if (stoneWin != null) return new int[]{r, c};
            }
        }
        return null;
    }


    // Finds a rhombic move that sets up a winning stone on the next turn
    private int[] findWinningRhombicMove(Colour colour) {
        if (rhombicStones == null) return null;
        int size = board.getSize();

        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                if (rhombicStones[r][c] != null) continue;

                // check \ diagonal: bridges oct[r][c] and oct[r+1][c+1]
                Cell tl = board.getCell(r,     c);
                Cell br = board.getCell(r + 1, c + 1);
                if (tl != null && tl.getColor() == colour
                        && br != null && br.getColor() == colour) {
                    rhombicStones[r][c] = colour;
                    boolean wins = (colour == Colour.BLACK) ? blackWins() : whiteWins();
                    rhombicStones[r][c] = null;
                    if (wins) return new int[]{r, c};
                }

                // check / diagonal: bridges oct[r][c+1] and oct[r+1][c]
                Cell tr = board.getCell(r,     c + 1);
                Cell bl = board.getCell(r + 1, c);
                if (tr != null && tr.getColor() == colour
                        && bl != null && bl.getColor() == colour) {
                    rhombicStones[r][c] = colour;
                    boolean wins2 = (colour == Colour.BLACK) ? blackWins() : whiteWins();
                    rhombicStones[r][c] = null;
                    if (wins2) return new int[]{r, c};
                }
            }
        }
        return null;
    }

    //  FInds a rhombic move that results in an immediate win
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

    // Extends an existing chain by placing near friendly stones
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

    // Checks if a lane is fully blocked ahead of the bot's progress
    private boolean isLaneFullyBlocked(int lane, Colour botColour, Colour humanColour, int size) {
        if (lane < 0 || lane >= size) return true;

        // Find frontier: the furthest position the bot has reached in this lane
        int frontier = 0;
        for (int pos = 0; pos < size; pos++) {
            Cell cell = (botColour == Colour.BLACK)
                    ? board.getCell(pos, lane)
                    : board.getCell(lane, pos);
            if (cell != null && cell.getColor() == botColour) frontier = pos + 1;
        }

        // Count empty cells ahead to determine if the lane is blocked
        int emptyCellsAhead = 0;
        for (int pos = frontier; pos < size; pos++) {
            Cell cell = (botColour == Colour.BLACK)
                    ? board.getCell(pos, lane)
                    : board.getCell(lane, pos);
            if (cell != null && cell.isEmpty()) emptyCellsAhead++;
        }

        int totalAhead = size - frontier;
        return totalAhead > 0 && emptyCellsAhead == 0;
    }

    // Chooses the better adjacent lane for a detour based on blockers and position
    private int[] pickClearerLane(int[] lanes, Colour botColour, Colour humanColour, int size) {
        if (lanes.length < 2) return lanes;

        int blockersA = countHumanInLane(lanes[0], botColour, humanColour, size);
        int blockersB = countHumanInLane(lanes[1], botColour, humanColour, size);

        // if one lane clearly has fewer blockers, pick it
        if (blockersA < blockersB) return new int[]{lanes[0], lanes[1]};
        if (blockersB < blockersA) return new int[]{lanes[1], lanes[0]};


        if (botColour == Colour.BLACK) {
            // prefer higher lane index (further right column)
            if (lanes[1] > lanes[0]) return new int[]{lanes[1], lanes[0]};
        }

        return lanes;
    }

    // Count human stones in a lane (col for BLACK, row for WHITE)
    private int countHumanInLane(int lane, Colour botColour, Colour humanColour, int size) {
        int count = 0;
        for (int pos = 0; pos < size; pos++) {
            Cell cell = (botColour == Colour.BLACK)
                    ? board.getCell(pos, lane)   // BLACK uses column as lane
                    : board.getCell(lane, pos);  // WHITE uses row as lane
            if (cell != null && cell.getColor() == humanColour) count++;
        }
        return count;
    }

    // Returns lane options sorted by distance from the centre
    private int[] sortByDistanceToCenter(int laneA, int laneB, int centre, int size) {
        List<Integer> options = new ArrayList<>();
        if (laneA >= 0 && laneA < size) options.add(laneA);
        if (laneB >= 0 && laneB < size) options.add(laneB);
        options.sort((a, b) -> Math.abs(a - centre) - Math.abs(b - centre));
        int[] result = new int[options.size()];
        for (int i = 0; i < options.size(); i++) result[i] = options.get(i);
        return result;
    }

    // Counts adjacent friendly stones around the cell
    private int countFriendlyNeighbours(int row, int col, Colour colour) {
        int count = 0;
        int size  = board.getSize();
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : directions) {
            int nr = row+d[0], nc = col+d[1];
            if (nr>=0&&nr<size&&nc>=0&&nc<size) {
                Cell nb = board.getCell(nr, nc);
                if (nb != null && nb.getColor() == colour) count++;
            }
        }
        return count;
    }

}
