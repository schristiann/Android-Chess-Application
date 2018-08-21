package chess.chessapp;


import java.io.Serializable;
import java.util.Objects;

import static chess.chessapp.ChessConstants.BLACK;
import static chess.chessapp.ChessConstants.WHITE;

/**
 * Class representing a simple Square in the chess board - left blank if no piece there
 */

public class Square implements Serializable {
    private boolean occupied;
    private Piece occupyingPiece;
    private final char color;
    private int row;
    private int col;
    char enpassantPlayer;
    private static final long serialVersionUID = 1L;
    public Square(int row, int col) {
        if ((col % 2 == 1 && row % 2 == 0) || (col % 2 == 0 && row % 2 == 1)) {
            color = BLACK;
        } else {
            color = WHITE;
        }
        this.row = row;
        this.col = col;
        enpassantPlayer = '\0';
    }

    /**
     * Copy constructor
     * @param square
     */
    public Square(Square square) {

        if (square == null) {
            throw new IllegalArgumentException("square parameter cannot be null");
        }

        this.row = square.getRow();
        this.col = square.getCol();

        if (square.occupyingPiece != null)
            this.occupyingPiece = square.occupyingPiece.makeCopy();
        this.color = square.color;
    }

    @Override
    public String toString() {
        return "Square{" +
                "occupied=" + occupied +
                ", occupyingPiece=" + occupyingPiece +
                ", color='" + color + '\'' +
                ", row=" + row +
                ", col=" + col +
                ", enpassantPlayer=" + enpassantPlayer +
                '}';
    }

    public Square() {
        color = '\0';
        occupyingPiece = null;
    }

    public Piece getPiece() {
        return occupyingPiece;
    }

    public void setPiece(Piece chessPiece) {
        this.occupyingPiece = chessPiece;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getColor() {
        return color;
    }

    public char getPieceColor() {
        return occupyingPiece.getColor();
    }

    public char getPieceRank() {
        return occupyingPiece.getRank();
    }

    public boolean isEmpty() {
        return occupyingPiece == null;
    }
}
