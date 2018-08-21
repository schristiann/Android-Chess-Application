package chess.chessapp;

/**
 * Class representing a Pawn piece
 * @author annafaytelson
 *
 */
public class Pawn extends Piece {
	char enpassantColor;
	public Pawn(char color, int instance) {
		super(color, 'p', instance);
	}

	@Override
	protected Piece makeCopy() {
		Pawn pawn = new Pawn(this.getColor(), this.getInstance());
		pawn.setPieceMoved(this.isPieceMoved());
		pawn.enpassantColor = this.enpassantColor;
		return pawn;
	}
}
