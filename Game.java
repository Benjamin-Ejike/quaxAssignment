public class Game {

    private Board board;
    private Colour currentPlayer;

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
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
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
            return true;
        }

        if (currentPlayer == Colour.WHITE && whiteWins()) {
            gameOver = true;
            winner = Colour.WHITE;
            return true;
        }

        switchTurn();
        return true;
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
}