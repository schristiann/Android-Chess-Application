package chess.chessapp;

/**
 * Class representing a Knight piece
 * @author annafaytelson
 *
 */
public class Knight extends Piece{
	
	public Knight(char color, int instance) {
		super(color, 'N', instance);
	}

	@Override
	protected Piece makeCopy() {
		Knight knight = new Knight(this.getColor(), this.getInstance());
		knight.setPieceMoved(this.isPieceMoved());
		return knight;
	}
}
