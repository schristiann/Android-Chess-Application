package chess.chessapp;

public class ChessConstants {

    public enum PLAYER  {WHITE("w"), BLACK("b");

        String playerId;
        PLAYER(String id) {
            this.playerId = id;
        }
        public String getPlayerId() { return playerId;}
    };

    public static final char WHITE = 'w';
    public static final char BLACK = 'b';
}
