package ttfe;

import java.util.Random;

public class Simulator implements SimulatorInterface {
    private int[][] board;
    private int boardWidth;
    private int boardHeight;
    private int numMoves;
    private int numPieces;
    private int points;
    private Random random;
    private boolean isComputerPlayer;
    private static final int MOVE_UPDATE_FREQUENCY = 1;

    public Simulator(int width, int height, Random r) {
        if (r == null) {
            throw new IllegalArgumentException("Random object cannot be null");
        }
        if (width < 2 || height < 2) {
            throw new IllegalArgumentException("Board dimensions must be at least 2x2");
        }
        this.boardWidth = width;
        this.boardHeight = height;
        this.random = r;
        this.board = new int[height][width];
        this.numMoves = 0;
        this.numPieces = 0;
        this.points = 0;

        // Place two initial pieces
        addPiece();
        addPiece();
    }

    // Copy constructor
    public Simulator(Simulator original) {
        this.boardWidth = original.boardWidth;
        this.boardHeight = original.boardHeight;
        this.random = new Random(original.random.nextLong()); // to keep the same seed
        this.board = new int[this.boardHeight][this.boardWidth];
        for (int i = 0; i < this.boardHeight; i++) {
            System.arraycopy(original.board[i], 0, this.board[i], 0, this.boardWidth);
        }
        this.numMoves = original.numMoves;
        this.numPieces = original.numPieces;
        this.points = original.points;
        this.isComputerPlayer = original.isComputerPlayer;
    }

    @Override
    public void addPiece() {
        if (!isSpaceLeft()) {
            throw new IllegalStateException("No space left to add a new piece");
        }
        int value = (random.nextDouble() < 0.9) ? 2 : 4;
        int x, y;
        do {
            x = random.nextInt(boardWidth);
            y = random.nextInt(boardHeight);
        } while (board[y][x] != 0);
        board[y][x] = value;
        numPieces++;
    }

    @Override
    public int getBoardHeight() {
        return boardHeight;
    }

    @Override
    public int getBoardWidth() {
        return boardWidth;
    }

    @Override
    public int getNumMoves() {
        return numMoves;
    }

    @Override
    public int getNumPieces() {
        return numPieces;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public int getPieceAt(int x, int y) {
        if (x < 0 || x >= boardWidth || y < 0 || y >= boardHeight) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return board[y][x];
    }

    @Override
    public void setPieceAt(int x, int y, int piece) {
        if (x < 0 || x >= boardWidth || y < 0 || y >= boardHeight || piece < 0) {
            throw new IllegalArgumentException("Invalid coordinates or piece value");
        }
        if (board[y][x] != 0 && piece == 0) {
            numPieces--;
        } else if (board[y][x] == 0 && piece != 0) {
            numPieces++;
        }
        board[y][x] = piece;
    }

    @Override
    public boolean isMovePossible() {
        for (MoveDirection direction : MoveDirection.values()) {
            if (isMovePossible(direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMovePossible(MoveDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j] != 0) {
                    int newX = j + getDx(direction);
                    int newY = i + getDy(direction);
                    if (isValidCoordinate(newX, newY) && (board[newY][newX] == 0 || board[newY][newX] == board[i][j])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSpaceLeft() {
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean performMove(MoveDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        boolean moved = false;

        // Sliding phase
        moved = slideTiles(direction);

        // Merging phase
        if (mergeTiles(direction)) {
            moved = true;
        }

        // Second sliding phase
        if (slideTiles(direction)) {
            moved = true;
        }

        if (moved) {
            numMoves++;
        }

        return moved;
    }

    /**
     * Check if the player has won by reaching the 2048 tile.
     * 
     * @return true if there is a 2048 tile on the board, false otherwise
     */
    public boolean hasWon() {
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (board[y][x] >= 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run(PlayerInterface player, UserInterface ui) {
        if (player == null || ui == null) {
            throw new IllegalArgumentException("Player and UI cannot be null");
        }

        isComputerPlayer = (player instanceof Player);
        int moveCount = 0;

        ui.updateScreen(this);
        while (isMovePossible()) {
            MoveDirection direction = player.getPlayerMove(this, ui);
            if (performMove(direction)) {
                moveCount++;
                
                // Check for victory immediately after the move
                if (hasWon()) {
                    ui.updateScreen(this);
                    ui.showMessage("Congratulations! You reached 2048 and won the game!");
                    return; // Exit the game immediately
                }
                
                addPiece();
                if (isComputerPlayer) {
                    if (moveCount % MOVE_UPDATE_FREQUENCY == 0) {
                        ui.updateScreen(this);
                    }
                } else {
                    ui.updateScreen(this);
                }
            }
        }
        
        // Only show game over if we didn't win
        ui.showGameOverScreen(this);
    }

    private boolean slideTiles(MoveDirection direction) {
        boolean moved = false;
        int dx = getDx(direction);
        int dy = getDy(direction);

        for (int i = 0; i < Math.max(boardWidth, boardHeight); i++) {
            for (int j = 0; j < Math.max(boardWidth, boardHeight); j++) {
                int x = (dx == 1) ? boardWidth - 1 - j : j;
                int y = (dy == 1) ? boardHeight - 1 - i : i;
                if (x >= 0 && x < boardWidth && y >= 0 && y < boardHeight && board[y][x] != 0) {
                    int newX = x;
                    int newY = y;
                    while (isValidCoordinate(newX + dx, newY + dy) && board[newY + dy][newX + dx] == 0) {
                        board[newY + dy][newX + dx] = board[newY][newX];
                        board[newY][newX] = 0;
                        newX += dx;
                        newY += dy;
                        moved = true;
                    }
                }
            }
        }

        return moved;
    }

    private boolean mergeTiles(MoveDirection direction) {
        boolean merged = false;
        int dx = getDx(direction);
        int dy = getDy(direction);
        boolean[][] mergedThisTurn = new boolean[boardHeight][boardWidth];

        for (int i = 0; i < Math.max(boardWidth, boardHeight); i++) {
            for (int j = 0; j < Math.max(boardWidth, boardHeight); j++) {
                int x = (dx == 1) ? boardWidth - 1 - j : j;
                int y = (dy == 1) ? boardHeight - 1 - i : i;
                if (x >= 0 && x < boardWidth && y >= 0 && y < boardHeight && board[y][x] != 0) {
                    int newX = x + dx;
                    int newY = y + dy;
                    if (isValidCoordinate(newX, newY) && board[newY][newX] == board[y][x] && !mergedThisTurn[newY][newX]) {
                        board[newY][newX] *= 2;
                        board[y][x] = 0;
                        points += board[newY][newX];
                        numPieces--;
                        mergedThisTurn[newY][newX] = true;
                        merged = true;
                    }
                }
            }
        }

        return merged;
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < boardWidth && y >= 0 && y < boardHeight;
    }

    private int getDx(MoveDirection direction) {
        switch (direction) {
            case EAST:
                return 1;
            case WEST:
                return -1;
            default:
                return 0;
        }
    }

    private int getDy(MoveDirection direction) {
        switch (direction) {
            case SOUTH:
                return 1;
            case NORTH:
                return -1;
            default:
                return 0;
        }
    }
}