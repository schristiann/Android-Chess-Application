package chess.chessapp;

/**
 * Class representing a Rook piece
 * @author annafaytelson
 *
 */
public class Rook extends Piece{

	public Rook(char color, int instance) {
		super(color, 'R', instance);
	}

	@Override
	protected Piece makeCopy() {
		Rook rook = new Rook(this.getColor(), this.getInstance());
		rook.setPieceMoved(this.isPieceMoved());
		return rook;
	}
}
