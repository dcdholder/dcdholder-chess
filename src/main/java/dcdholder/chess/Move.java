package dcdholder.chess;

public class Move {
	private Coord init;
	private Coord dest;
	
	public Coord getInit() {return this.init;}
	public Coord getDest() {return this.dest;}
	
	public static boolean wouldBeLegalMoveObject(Coord init, Coord dest) {
		if(init.bothDimsBetween1And8()&&dest.bothDimsBetween1And8()&&!init.equals(dest)) {
			return true;
		} else {
			return false;
		}
	}
	public static boolean wouldBeLegalMoveObjectFromRelative(Coord init, Coord relative) {
		return wouldBeLegalMoveObject(init,Coord.addCoords(init,relative));
	}
	
	static Coord coordDeltaFromMove(Move move) {
		return Coord.subCoords(move.dest,move.init);
	}
	
	static Coord absCoordDeltaFromMove(Move move) {
		Coord returnCoord = coordDeltaFromMove(move);
		
		return returnCoord.absCoord();
	}
	
	public boolean moveIsToDirection(String direction) {
		boolean isToDirection = false;
		
		switch (direction) {
			case "right":
				if(this.dest.getX()>this.init.getX()) {isToDirection=true;}
				break;
			case "left":
				if(this.dest.getX()<this.init.getX()) {isToDirection=true;}
				break;
			case "up":
				if(this.dest.getY()>this.init.getY()) {isToDirection=true;}
				break;
			case "down":
				if(this.dest.getY()<this.init.getY()) {isToDirection=true;}
				break;
			case "diagonal":
				if(absCoordDeltaFromMove(this).getX()==absCoordDeltaFromMove(this).getY()) {isToDirection=true;}
				break;
		}
		
		return isToDirection;
	}
	
	public String toString() {
		return "Initial coord: {" + this.init.toString() + "}, Destination coord: {" + this.dest.toString() + "}";
	}
	
	public int hashCode() {
		return this.init.hashCode() + this.dest.hashCode();
	}
	public boolean equals(Object inputMove) {
		if(inputMove instanceof Move) {
			Move testMove = (Move)inputMove;
			if(this.init.equals(testMove.init) && this.dest.equals(testMove.dest)) {return true;} else {return false;}
		} else {
			return false;
		}
	}
	
	//basically exists just to allow you to call "static" methods without explicitly creating a move
	Move() {
		this.init = new Coord(0,0);
		this.dest = new Coord(0,0);
	}
	
	//try to find a way to create moves from relative Coords
	Move(Coord init, Coord dest) {
		//test whether calling the copy constructor is really necessary
		//prevent out-of-bounds moves from being created at all
		if(init.bothDimsBetween1And8()) {
			this.init = new Coord(init);
		} else {
			throw new IllegalArgumentException("Initial position out of bounds: {" + init.toString() + "}");
		}
		if(dest.bothDimsBetween1And8()) {
			this.dest = new Coord(dest);
		} else {
			throw new IllegalArgumentException("Final position out of bounds: {" + dest.toString() + "}");
		}
		if(init.equals(dest)) {
			throw new IllegalArgumentException("Initial and final position cannot be identical");
		}
	}
}
