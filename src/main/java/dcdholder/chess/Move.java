package dcdholder.chess;

//TODO: fix Move methods which "fake" the static modifier, and calls to Coord methods which do the same
public class Move {
	public Coord init;
	public Coord dest;
	
	public Coord getInit() {return this.init;}
	public Coord getDest() {return this.dest;}
	
	//figure out if there's a way to make this static
	public boolean wouldBeLegalMoveObject(Coord init, Coord dest) {
		if(init.bothDimsBetween1And8()&&dest.bothDimsBetween1And8()&&!init.equals(dest)) {
			return true;
		} else {
			return false;
		}
	}
	public boolean wouldBeLegalMoveObjectFromRelative(Coord init, Coord relative) {
		Coord dummyCoord = new Coord();
		
		return wouldBeLegalMoveObject(init,dummyCoord.addCoords(init,relative));
	}
	
	public Coord coordDeltaFromMove(Move move) {
		Coord dummyCoord = new Coord();
		
		return dummyCoord.subCoords(move.dest,move.init);
	}
	
	public Coord absCoordDeltaFromMove(Move move) {
		Coord returnCoord = coordDeltaFromMove(move);
		
		return returnCoord.absCoord();
	}
	
	public boolean moveIsToDirection(Move move, String direction) {
		boolean isToDirection = false;
		
		switch (direction) {
			case "right":
				if(move.dest.getX()>move.init.getX()) {isToDirection=true;}
				break;
			case "left":
				if(move.dest.getX()<move.init.getX()) {isToDirection=true;}
				break;
			case "up":
				if(move.dest.getY()>move.init.getY()) {isToDirection=true;}
				break;
			case "down":
				if(move.dest.getY()<move.init.getY()) {isToDirection=true;}
				break;
		}
		
		return isToDirection;
	}
	
	public String toString() {
		return "Initial coord: {" + this.init.toString() + "}, Destination coord: {" + this.dest.toString() + "}";
	}
	
	public boolean equals(Move inputMove) {
		if(this.init.equals(inputMove.init) && this.init.equals(inputMove.dest)) {return true;} else {return false;}
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
