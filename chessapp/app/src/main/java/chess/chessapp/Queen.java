package chess.chessapp;

/**
 * Class representing a Queen piece
 * @author annafaytelson
 *
 */
public class Queen extends Piece {
	
	public Queen(char color, int instance) {
		super(color, 'Q', instance);
	}

	@Override
	protected Piece makeCopy() {
		Queen queen = new Queen(this.getColor(), this.getInstance());
		queen.setPieceMoved(this.isPieceMoved());
		return queen;
	}
}
