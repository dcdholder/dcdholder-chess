package dcdholder.chess;
import java.util.*;

//All conceivable moves for a given square are added to a list
//This list is passed into an arbiter, which spits out the list of moves which are legal
//This step is repeated for every piece on the board
//Game consists of a list of pieces, perhaps mapped to Coords
//Pieces themselves store some data (has this rook already moved? etc.)
public class GameState {
	public class Coord {
		private byte x,y;
		
		public byte getX() {return this.x;}
		public byte getY() {return this.y;}
		
		//get it to stop bugging me for casts
		Coord(int x, int y) {this((byte)x,(byte)y);}
		Coord(byte x, int y) {this(x,(byte)y);}
		Coord(int x, byte y) {this((byte)x,y);}
		
		Coord(byte x, byte y) {
			this.x = x;
			this.y = y;
		}
		Coord(Coord copyCoord) {
			this(copyCoord.x,copyCoord.y);
		}
	}
	
	public class Move {
		public Coord init;
		public Coord dest;
		
		public Coord getInit() {return this.init;}
		public Coord getDest() {return this.dest;}
		
		Move(Coord init, Coord dest) {
			//test whether calling the copy constructor is really necessary
			this.init = new Coord(init);
			this.dest = new Coord(dest);
		}
	}
	
	private interface Piece {
		//specify whether the piece is white or black
		//specify
		enum PieceColour {
			WHITE,BLACK;
		}

		public Set<Coord> testableRelativeCoord();
	}
	
	public class Pawn implements Piece {
		boolean lastMoveWasDouble;
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			//forward moves
			testableRelativeCoord.add(new Coord(0,1));
			testableRelativeCoord.add(new Coord(0,2));
			//capture moves
			testableRelativeCoord.add(new Coord(1,1));
			testableRelativeCoord.add(new Coord(-1,1));
			
			return testableRelativeCoord;
		}
		
		Pawn() {
			this.lastMoveWasDouble = false;
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
	public class Rook implements Piece {
		boolean hasMoved;
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			for(int horVert=-7;horVert<=7;horVert++) {
				if(horVert!=0) {
					testableRelativeCoord.add(new Coord(horVert,0));
					testableRelativeCoord.add(new Coord(0,horVert));
				}
			}
			
			return testableRelativeCoord;
		}
		
		Rook() {
			this.hasMoved = false;
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
	public class Bishop implements Piece {
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			for(int horVert=1;horVert<=7;horVert++) {
				testableRelativeCoord.add(new Coord(horVert,horVert));
				testableRelativeCoord.add(new Coord(-horVert,horVert));
				testableRelativeCoord.add(new Coord(horVert,-horVert));
				testableRelativeCoord.add(new Coord(-horVert,-horVert));
			}
			
			return testableRelativeCoord;
		}
		
		Bishop() {
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
	public class Knight implements Piece {
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			//"vertical" knight moves
			testableRelativeCoord.add(new Coord(1,2));
			testableRelativeCoord.add(new Coord(1,-2));
			testableRelativeCoord.add(new Coord(-1,2));
			testableRelativeCoord.add(new Coord(-1,-2));
			//"horizontal" knight moves
			testableRelativeCoord.add(new Coord(2,1));
			testableRelativeCoord.add(new Coord(2,-1));
			testableRelativeCoord.add(new Coord(-2,1));
			testableRelativeCoord.add(new Coord(-2,-1));
			
			return testableRelativeCoord;
		}
		
		Knight() {
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
	public class Queen implements Piece {
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			//vertical and horizontal moves
			for(int horVert=-7;horVert<=7;horVert++) {
				if(horVert!=0) {
					testableRelativeCoord.add(new Coord(horVert,0));
					testableRelativeCoord.add(new Coord(0,horVert));
				}
			}
			//diagonal moves
			for(int horVert=1;horVert<=7;horVert++) {
				testableRelativeCoord.add(new Coord(horVert,horVert));
				testableRelativeCoord.add(new Coord(-horVert,horVert));
				testableRelativeCoord.add(new Coord(horVert,-horVert));
				testableRelativeCoord.add(new Coord(-horVert,-horVert));
			}
			
			return testableRelativeCoord;
		}
		
		Queen() {
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
	public class King implements Piece {
		boolean hasMoved;
		Set<Coord> relativeMoveSet;
		
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();

			//standard moves
			testableRelativeCoord.add(new Coord(1,1));
			testableRelativeCoord.add(new Coord(1,-1));
			testableRelativeCoord.add(new Coord(-1,1));
			testableRelativeCoord.add(new Coord(-1,-1));
			//castling
			testableRelativeCoord.add(new Coord(2,0));
			testableRelativeCoord.add(new Coord(-2,0));
			
			return testableRelativeCoord;
		}
		
		King() {
			this.hasMoved = false;
			this.relativeMoveSet = testableRelativeCoord();
		}
	}
}
