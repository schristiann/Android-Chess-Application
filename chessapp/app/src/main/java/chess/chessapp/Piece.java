package chess.chessapp;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract class with move method that is a superclass of all the chess pieces 
 * @author annafaytelson
 *
 */
public abstract class Piece implements Serializable{

	private static final long serialVersionUID = 1L;

	public char getColor() {
		return color;
	}

	public char getRank() {
		return rank;
	}

	public String getName() {
		return name;
	}

	public boolean isPieceMoved() {
		return pieceMoved;
	}

	//each piece has a color, rank, name, and whether it has moved
	private char color;			//b - black, w - white
	private char rank;			//r - rook, n - knight, b - bishop, q - queen, k - king, p - pawn
	private String name;		//color + rank, for parsing output

	public int getInstance() {
		return instance;
	}

	private int instance;

	public void setPieceMoved(boolean pieceMoved) {
		this.pieceMoved = pieceMoved;
	}

	private boolean pieceMoved;	//false until this instance of this piece is moved for first time, then true
	
	public Piece(char color, char rank, int instance) {
		//use the necessary characters to represent the rank of each piece
		this.color = color;
		this.rank = rank;
		name = "" + color + rank;
		pieceMoved = false;
		this.instance = instance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Piece piece = (Piece) o;
		return color == piece.color &&
				rank == piece.rank &&
				instance == piece.instance;
	}

	@Override
	public int hashCode() {

		return Objects.hash(color, rank, instance);
	}

	@Override
	public String toString() {
		return "Piece{" +
				"color=" + color +
				", rank=" + rank +
				", name='" + name + '\'' +
				", instance=" + instance +
				", pieceMoved=" + pieceMoved +
				'}';
	}

	protected abstract Piece makeCopy();
}