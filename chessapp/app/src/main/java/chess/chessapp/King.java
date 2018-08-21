package chess.chessapp;

/**
 * Class representing a King piece
 * @author annafaytelson
 *
 */
public class King extends Piece {
	
		boolean queenSideCastle;
		boolean kingSideCastle;
		boolean check;
		
		public King(char color) {
			super(color, 'K',0);
			check=false;
		}
		
		public boolean isQueenSideCastle() {
			return queenSideCastle;
		}
		
		public boolean isKingSideCastle() {
			return kingSideCastle;
		}
		
		public void setKingSideCastle(boolean kingSideCastle) {
			this.kingSideCastle=kingSideCastle;
			
		}
		
		public void setQueenSideCastle(boolean queenSideCastle) {
			this.queenSideCastle=queenSideCastle;
		}
		
		public boolean isCheck() {
			return check;
		}
		
		public void setCheck(boolean check){
			this.check=check;
		}

	@Override
	protected Piece makeCopy() {
		King king = new King(this.getColor());
		king.setCheck(this.check);
		king.setKingSideCastle(this.kingSideCastle);
		king.setQueenSideCastle(this.queenSideCastle);
		king.setPieceMoved(this.isPieceMoved());
		return king;
	}
}
