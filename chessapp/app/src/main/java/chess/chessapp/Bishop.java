package chess.chessapp;

/**
 * Class that represents a Bishop piece
 * @author annafaytelson
 *
 */
public class Bishop extends Piece {
	
	public Bishop(char color, int instance) {
		super(color, 'B', instance);
	}

	@Override
	protected Piece makeCopy() {
		Bishop bishop = new Bishop(this.getColor(), this.getInstance());
		bishop.setPieceMoved(this.isPieceMoved());
		return bishop;
	}
}
