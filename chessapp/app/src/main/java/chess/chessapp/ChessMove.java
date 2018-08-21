package chess.chessapp;

import java.io.Serializable;

public class ChessMove implements Serializable{

    private static final long serialVersionUID = 1L;
    public Square getFromSquare() {
        return fromSquare;
    }

    public Square getToSquare() {
        return toSquare;
    }

    private Square fromSquare;
    private Square toSquare;

    @Override
    public String toString() {
        return "ChessMove{" +
                "fromSquare=" + fromSquare +
                ", toSquare=" + toSquare +
                '}';
    }

    public ChessMove(Square fromSquare, Square toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
    }
}
