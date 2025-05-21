/* This Class implements the actual game.
   It controls the state of the gomoku game, deals with the players' moves, and checks for the winning/draw state.
   Remember, to play the game. you may run the Main Class.
* */


package gomoku;

public class Board {
    // Array to store where the pieces were placed
    private int[][] board;

    // The turn of the players
    private boolean isPlayer1Turn;

    // Whether it is draw
    private boolean isDraw = false;

    // Whether it is game over
    private boolean isGameOver = false;

    // Players' total moves
    private int player1Move = 0;
    private int player2Move = 0;

    // The text of the move
    private String moveText;

    // Initialize the board
    public Board(int rw, int cl) {
        board = new int[rw][cl];
        isPlayer1Turn = true;
        moveText = "";
    }

    // Helper constructor to copy the current board
    public Board(Board b) {
        this.board = new int[b.board.length][b.board[0].length];
        for (int i = 0; i < b.board.length; i++) {
            for (int j = 0; j < b.board[0].length; j++) {
                this.board[i][j] = b.getBoardItem(i, j);
            }
        }
        this.isPlayer1Turn = b.getPlayer1Turn();
        this.isDraw = b.isDraw();
        this.isGameOver = b.isGameOver();
        this.player1Move = b.player1Move();
        this.player2Move = b.player2Move();
        moveText = b.moveText();
    }

    // Set the move text
    public void setMoveText(String moveText) {
        this.moveText = moveText;
    }

    // Get the move text
    public String moveText() {
        return moveText;
    }

    // Get the turn
    public boolean getPlayer1Turn() {
        return isPlayer1Turn;
    }

    // Get whether it is draw
    public boolean isDraw() {
        return isDraw;
    }

    // Get whether it is game over
    public boolean isGameOver() {
        return isGameOver;
    }

    // Get the players' total moves
    public int player1Move() {
        return player1Move;
    }

    public int player2Move() {
        return player2Move;
    }

    // Switch the turn
    public void switchTurn() {
        isPlayer1Turn = !isPlayer1Turn;
    }

    // Get an item from the board
    public int getBoardItem(int r, int c) {
        return board[r][c];
    }

    // Place a piece
    public void setBoardItem(int r, int c, int value, int totalRow, int totalColumn) {
        board[r][c] = value;
        if (isPlayer1Turn) {
            player1Move++;
        }
        else  {
            player2Move++;
        }
    }

    // Set the game over state
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    // Check whether Black wins
    public boolean checkBlackWinningState() {
        int blackMax = getBlackMaxLength();
        if (blackMax >= 5) {
            isGameOver = true;
            return true;
        }
        return false;
    }

    // Check whether White wins
    public boolean checkWhiteWinningState() {
        int whiteMax = getWhiteMaxLength();
        if (whiteMax >= 5) {
            isGameOver = true;
            return true;
        }
        return false;
    }

    // Check whether it's draw
    public boolean checkDrawState() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Get the max lengths of the players
    public int getBlackMaxLength() {
        return getMaxLength(1);
    }

    public int getWhiteMaxLength() {
        return getMaxLength(-1);
    }

    // Helper method to get the max length
    private int getMaxLength(int isBlack) {
        int maxLength = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == isBlack) {
                    int vBlack = vLength(i, j, isBlack);
                    int hBlack = hLength(i, j, isBlack);
                    int diagonalRightBlack = diagonalRightLength(i, j, isBlack);
                    int diagonalLeftBlack = diagonalLeftLength(i, j, isBlack);
                    int currentLength = Math.max(Math.max(vBlack, hBlack), Math.max(diagonalRightBlack, diagonalLeftBlack));
                    maxLength = Math.max(maxLength, currentLength);
                }
            }
        }
        return maxLength;
    }

    // Helper method to calculate the maximum number of connected pieces in a column.
    private int vLength(int rw, int cl, int piece) {
        int vLength = 1;
        for (int i = rw + 1; i < board.length; i++) {
            if (board[i][cl] == piece) {
                vLength++;
            }
            else {
                break;
            }
        }
        return vLength;
    }

    // Helper method to calculate the maximum number of connected pieces in a row.
    private int hLength(int rw, int cl, int piece) {
        int hLength = 1;
        for (int j = cl + 1; j < board[0].length; j++) {
            if  (board[rw][j] == piece) {
                hLength++;
            }
            else {
                break;
            }
        }
        return hLength;
    }

    // Helper method to calculate the maximum number of connected pieces in a diagonal pointing right and down.
    private int diagonalRightLength(int rw, int cl, int piece) {
        int diagonalLength = 1;
        for (int i = 1; rw +  i < board.length && cl + i < board[0].length; i++) {
            if (board[rw + i][cl + i] == piece) {
                diagonalLength++;
            }
            else {
                break;
            }
        }
        return diagonalLength;
    }

    // Helper method to calculate the maximum number of connected pieces in a diagonal pointing left and down.
    private int diagonalLeftLength(int rw, int cl, int piece) {
        int diagonalLength = 1;
        for (int i = 1; rw +  i < board.length && cl - i >= 0; i++) {
            if (board[rw + i][cl - i] == piece) {
                diagonalLength++;
            }
            else {
                break;
            }
        }
        return diagonalLength;
    }

}

