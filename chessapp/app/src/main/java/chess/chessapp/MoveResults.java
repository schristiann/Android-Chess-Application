package chess.chessapp;

public class MoveResults {
    boolean gameOver;
    String checkMessage;
    Square oldRookSquare;
    Square newRookSquare;

    public Square getEnpassantSquare() {
        return enpassantSquare;
    }

    public void setEnpassantSquare(Square enpassantSquare) {
        this.enpassantSquare = enpassantSquare;
    }

    Square enpassantSquare;

    public MoveResults() {
        this(false, null);
    }

    public MoveResults(boolean gameOver, String checkMessage) {
        this.gameOver = gameOver;
        this.checkMessage = checkMessage;
    }

    public Square getOldRookSquare() {
        return oldRookSquare;
    }

    public void setOldRookSquare(Square oldRookSquare) {
        this.oldRookSquare = oldRookSquare;
    }

    public Square getNewRookSquare() {
        return newRookSquare;
    }

    public void setNewRookSquare(Square newRookSquare) {
        this.newRookSquare = newRookSquare;
    }
}