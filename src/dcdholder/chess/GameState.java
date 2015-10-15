package dcdholder.chess;
import java.util.*;

//All conceivable moves for a given square are added to a list
//This list is passed into an arbiter, which spits out the list of moves which are legal
//This step is repeated for every PieceSpecific on the board
//Game consists of a list of PieceSpecifics, perhaps mapped to Coords
//PieceSpecifics themselves store some data (has this rook already moved? etc.)
public class GameState {
	Set<Piece> chessPieces;
	
	public Set<Move> getAllLegalMoves() {
		Set<Move> allLegalMoves = new HashSet<Move>();
		
		//fill this in
		
		return allLegalMoves;
	}
	
	enum PieceColour {
		WHITE,BLACK;
	}
	
	public class Coord {
		private byte x,y;
		
		public byte getX() {return this.x;}
		public byte getY() {return this.y;}
		
		//get it to stop bugging me for casts
		Coord(int x, int y) {this((byte)x,(byte)y);}
		Coord(byte x, int y) {this(x,(byte)y);}
		Coord(int x, byte y) {this((byte)x,y);}
		
		boolean bothDimsBetween1And8() {if((this.x<=8&&this.x>=1)&&(this.y<=8&&this.y>=1)) {return true;} else {return false;}}
		Coord addCoords(Coord coordA, Coord coordB) {
			int newCoordX = coordA.x + coordB.x;
			int newCoordY = coordA.y + coordB.y;
			
			return new Coord(newCoordX,newCoordY);
		}
		
		public String toString() {return "X value: " + this.x + " ,Y value: " + this.y;}
		
		public boolean equals(Coord inputCoord) {
			if(this.x==inputCoord.x && this.y==inputCoord.y) {return true;} else {return false;}
		}
		
		Coord() {
			this.x = 0;
			this.y = 0;
		}
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
	
	abstract public class Piece {
		Coord pieceCoord;
		PieceColour pieceColour;
		Set<Coord> relativeCoordSet;
		
		public Set<Move> formMovesFromRelativeCoordSet() {
			Set<Move> testableMoveSet = new HashSet<Move>();
			Move dummyMove = new Move();
			Coord dummyCoord = new Coord();
			
			for(Coord relativeCoord : relativeCoordSet) {
				if(dummyMove.wouldBeLegalMoveObjectFromRelative(this.pieceCoord,relativeCoord)) {
					testableMoveSet.add(new Move(this.pieceCoord,dummyCoord.addCoords(this.pieceCoord,relativeCoord)));
				}
			}
			
			return testableMoveSet;
		}
		
		public boolean isMoveLegalCommon(Move moveAttempt) {
			//if the initial coordinates do not match the piece's
			if(!moveAttempt.getInit().equals(this.pieceCoord)) {
				return false;
			} else if(!formMovesFromRelativeCoordSet().contains(moveAttempt)) { //check whether move is in movelist
				return false;
			} else {
				return true;
			}
		}
		
		Piece(PieceColour pieceColour, Coord pieceCoord) {
			this.pieceColour = pieceColour;
			this.pieceCoord = new Coord(pieceCoord);
		}
	}
	
	private interface PieceSpecific {
		//TODO: implement move legality checking in a meaningful way for each piece type
		public boolean isMoveLegalPieceSpecific(Move moveAttempt);
		public Set<Coord> testableRelativeCoord();
	}
	
	public class Pawn extends Piece implements PieceSpecific {
		boolean lastMoveWasDouble;
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		Pawn(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.lastMoveWasDouble = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Rook extends Piece implements PieceSpecific {
		boolean hasMoved;
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		Rook(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.hasMoved = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Bishop extends Piece implements PieceSpecific {
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		Bishop(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Knight extends Piece implements PieceSpecific {
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		Knight(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Queen extends Piece implements PieceSpecific {
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		Queen(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class King extends Piece implements PieceSpecific {
		boolean hasMoved;
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
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
		
		King(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			this.hasMoved = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	
	public void test_confirmInitialLegalMoveList() {
		GameState thisGame = new GameState();
		Set<Move> expectedLegalMoves = new HashSet<Move>();
		Set<Move> actualLegalMoves = thisGame.getAllLegalMoves();
		Set<Move> movesIntersection = new HashSet<Move>();
		Set<Move> movesOnlyInExpected = new HashSet<Move>();
		Set<Move> movesOnlyInActual = new HashSet<Move>();
		
		//these all correspond to white pieces
		Move pawnSingleMove, pawnDoubleMove;
		Move leftKnightLeftMove = new Move(new Coord(2,1),new Coord(1,3));
		Move leftKnightRightMove = new Move(new Coord(2,1),new Coord(3,3));
		Move rightKnightLeftMove = new Move(new Coord(7,1),new Coord(6,3));
		Move rightKnightRightMove = new Move(new Coord(7,1),new Coord(8,3));
		
		for(int i=1;i<=8;i++) {
			pawnSingleMove = new Move(new Coord(i,2),new Coord(i,3));
			pawnDoubleMove = new Move(new Coord(i,2),new Coord(i,4));
			expectedLegalMoves.add(pawnSingleMove);
			expectedLegalMoves.add(pawnDoubleMove);
		}
		expectedLegalMoves.add(leftKnightLeftMove);
		expectedLegalMoves.add(leftKnightRightMove);
		expectedLegalMoves.add(rightKnightLeftMove);
		expectedLegalMoves.add(rightKnightRightMove);
		
		if(!expectedLegalMoves.equals(actualLegalMoves)) {
			//get the elements only in expected move set
			movesOnlyInExpected.addAll(expectedLegalMoves);
			movesOnlyInExpected.removeAll(actualLegalMoves);
			//get the elements only in actual move set
			movesOnlyInExpected.addAll(actualLegalMoves);
			movesOnlyInExpected.removeAll(expectedLegalMoves);
			//get the intersection of the expected and actual move sets
			movesIntersection.addAll(expectedLegalMoves);
			movesIntersection.retainAll(actualLegalMoves);
			
			//TODO: find a better exception to throw; use a unit testing framework
			throw new IllegalStateException("Moves which are only in expected move set: " + movesOnlyInExpected.toString() + 
			                                " Moves which are only in actual move set: " + movesOnlyInActual.toString() + 
			                                " Moves which are in both: " + movesIntersection.toString());
		}
	}
	
	GameState() {
		this.chessPieces = new HashSet<Piece>();
		
		//add pawns for both sides
		for(int i=1;i<=8;i++) {
			chessPieces.add(new Pawn(PieceColour.WHITE,new Coord(i,2)));
			chessPieces.add(new Pawn(PieceColour.BLACK,new Coord(i,7)));
		}
		//add the kings
		chessPieces.add(new King(PieceColour.WHITE,new Coord(5,1)));
		chessPieces.add(new King(PieceColour.BLACK,new Coord(5,8)));
		//add the queens
		chessPieces.add(new Queen(PieceColour.WHITE,new Coord(4,1)));
		chessPieces.add(new Queen(PieceColour.WHITE,new Coord(4,8)));
		//add the bishops
		chessPieces.add(new Bishop(PieceColour.WHITE,new Coord(3,1)));
		chessPieces.add(new Bishop(PieceColour.WHITE,new Coord(6,1)));
		chessPieces.add(new Bishop(PieceColour.BLACK,new Coord(3,8)));
		chessPieces.add(new Bishop(PieceColour.BLACK,new Coord(6,8)));
		//add the rooks
		chessPieces.add(new Rook(PieceColour.WHITE,new Coord(1,1)));
		chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,1)));
		chessPieces.add(new Rook(PieceColour.BLACK,new Coord(1,8)));
		chessPieces.add(new Rook(PieceColour.BLACK,new Coord(8,8)));
		//add the knights
		chessPieces.add(new Rook(PieceColour.WHITE,new Coord(2,1)));
		chessPieces.add(new Rook(PieceColour.WHITE,new Coord(7,1)));
		chessPieces.add(new Rook(PieceColour.BLACK,new Coord(2,8)));
		chessPieces.add(new Rook(PieceColour.BLACK,new Coord(7,8)));
	}
}
