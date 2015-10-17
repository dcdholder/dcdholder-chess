package dcdholder.chess;
import java.util.*;
import java.math.*;

//All conceivable moves for a given square are added to a list
//This list is passed into an arbiter, which spits out the list of moves which are legal
//This step is repeated for every PieceSpecific on the board
//Game consists of a list of PieceSpecifics, perhaps mapped to Coords
//PieceSpecifics themselves store some data (has this rook already moved? etc.)
//TODO: write all of the arbiter methods for each piece
//TODO: figure out a way to allow promotion to arbitrary piece type, not just queen
//TODO: begin testing once CLI is done and arbiters have been implemented
//TODO: think about how you're going to implement and test all of that server-side shit
//TODO: run game verifier against every game in a massive database
//TODO: fix calls to Coord and Move methods which "fake" static modifier
public class GameState {
	Set<Piece> chessPieces;
	//this is a list because pieces that have been captured may have identical state at the point of capture to pieces that were captured earlier
	List<Piece> graveyard;
	PieceColour currentPlayer;
	
	public String createBoardString() {
		String boardString = new String();
		String[] boardLine = new String[8];
		//first, draw the board from the perspective of the white player, then draw depending on the current player
		//board is initially in the correct left-to-right order with respect to white
		for(int j=1;j<=8;j++) {
			for(int i=1;i<=8;i++) {
				if(coordContainsPiece(new Coord(i,j))) {
					boardLine[j-1] = boardLine + Character.toString(getPieceAtLocation(new Coord(i,j)).getRepChar());
				} else {
					if((i+j)%2==0) {
						boardLine[j-1] = boardLine + Character.toString('■');
					} else {
						boardLine[j-1] = boardLine + Character.toString('□');
					}
				}
			}
		}
		//orient correctly for the current player
		if(currentPlayer==PieceColour.WHITE) {
			for(int j=1;j<=8;j++) {
				List<String> tmpList = Arrays.asList(boardLine);
				Collections.reverse(tmpList);
				boardLine = tmpList.toArray(boardLine);
			}
		} else if(currentPlayer==PieceColour.BLACK) {
			for(int j=1;j<=8;j++) {
				boardLine[j] = new StringBuilder(boardLine[j]).reverse().toString();
			}
		}
		//transform into a single multiline string, so that this 
		for(int j=1;j<=8;j++) {
			boardString = boardString + boardLine[j] + "\n";
		}
		
		return boardString;
	}
	
	public void drawBoardCli() {System.out.println(createBoardString());}
	
	public Set<Move> getAllLegalMoves() {
		Set<Move> allLegalMoves = new HashSet<Move>();
		
		//fill this in
		
		return allLegalMoves;
	}
	
	public void removePieceAtLocation(Coord removeCoord) {
		for(Piece chessPiece : chessPieces) {
			if(chessPiece.getPieceCoord().equals(removeCoord)) {
				chessPieces.remove(chessPiece);
				graveyard.add(chessPiece);
			}
		}
	}
	
	public void movePieceAtLocation(Coord initialCoord, Coord destCoord) {
		boolean pieceFound = false;
		
		for(Piece chessPiece : chessPieces) {
			if(chessPiece.getPieceCoord().equals(initialCoord)) {
				chessPiece.makeMovePieceSpecific(new Move(initialCoord,destCoord));
				pieceFound = true;
				break;
			}
		}
		
		if(pieceFound==false) {
			throw new IllegalArgumentException("Was not able to move piece at location " + initialCoord.toString() + " since none exists");
		}
	}
	
	public Piece getPieceAtLocation(Coord pieceCoord) {
		boolean pieceFound = false;
		Piece desiredPiece = new Pawn();
		
		for(Piece chessPiece : chessPieces) {
			if(chessPiece.getPieceCoord().equals(pieceCoord)) {
				desiredPiece = chessPiece;
				pieceFound = true;
				break;
			}
		}
		
		if(pieceFound==false) {
			throw new IllegalArgumentException("Was not able to get piece at location " + pieceCoord.toString() + " since none exists");
		}
		return desiredPiece;
	}
	
	public boolean coordContainsPiece(Coord pieceCoord) {
		boolean pieceExistsAtCoord = false;
		
		for(Piece chessPiece : chessPieces) {
			if(chessPiece.getPieceCoord().equals(pieceCoord)) {
				pieceExistsAtCoord = true;
				break;
			}
		}
		
		return pieceExistsAtCoord;
	}
	
	enum PieceColour {
		WHITE,BLACK;
	}
	
	abstract public class Piece implements PieceSpecific {
		//REP_CHAR is set by pieces individually according to type and colour
		char REP_CHAR = 'X';
		Coord pieceCoord;
		PieceColour pieceColour;
		Set<Coord> relativeCoordSet;
		
		public Coord getPieceCoord() {return this.pieceCoord;}
		public char getRepChar() {return this.REP_CHAR;}
		
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
		
		public void makeMovePieceCommon(Move move) {
			removePieceAtLocation(move.dest);
			pieceCoord = new Coord(move.getDest());
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
		//public attemptMovePieceSpecific(Move attemptMove);
		//TODO: implement move legality checking in a meaningful way for each piece type
		public void makeMovePieceSpecific(Move move);
		//TODO: rename instances of moveAttempt to attemptMove
		public boolean isMoveLegalPieceSpecific(Move moveAttempt);
		public Set<Coord> testableRelativeCoord();
	}
	
	public class Pawn extends Piece {
		private boolean lastMoveWasDouble;
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return false;
			}
		}
		//makeMove should only be called through attemptMove, which should validate it
		public void makeMovePieceSpecific(Move move) {
			Move dummyMove = new Move();
			
			//check if move is an en passant... if so, remove other piece
			if(dummyMove.absCoordDeltaFromMove(move).equals(new Coord(1,1)) && !coordContainsPiece(move.dest)) {
				if(dummyMove.moveIsToDirection(move,"right")) {
					removePieceAtLocation(new Coord(move.getInit().getX()+1,move.getInit().getY()));
				} else if (dummyMove.moveIsToDirection(move,"left")) {
					removePieceAtLocation(new Coord(move.getInit().getX()-1,move.getInit().getY()));
				}
			}
			makeMovePieceCommon(move);
			//check if the last move was a double move
			if(dummyMove.absCoordDeltaFromMove(move).equals(new Coord(0,2))) {this.lastMoveWasDouble=true;}
			//take double moves off the table if the piece has already been moved
			relativeCoordSet.remove(new Coord(0,2));
			//check if the move results in pawn promotion... if so, create new piece and remove self
			if((pieceColour==PieceColour.WHITE && move.dest.getY()==8) || (pieceColour==PieceColour.BLACK && move.dest.getY()==1)) {
				//produce a new queen at current location and delete self
				chessPieces.add(new Queen(pieceColour,move.getDest()));
				chessPieces.remove(this);
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
		
		//TODO: only used when dummy pieces are necessary: find a way to obviate
		Pawn() {
			super(PieceColour.WHITE,new Coord(1,1));
			this.lastMoveWasDouble = false;
			//potential for performance penalty
			this.relativeCoordSet = testableRelativeCoord();
		}
		
		Pawn(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♙';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♟';
			}
			this.lastMoveWasDouble = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Rook extends Piece {
		boolean hasMoved;
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
			this.hasMoved = true;
		}
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
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♖';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♜';
			}
			this.hasMoved = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Bishop extends Piece {
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
		}
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
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♗';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♝';
			}
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Knight extends Piece {
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
		}
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
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♘';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♞';
			}
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class Queen extends Piece {
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
		}
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
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♕';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♛';
			}
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	public class King extends Piece {
		boolean hasMoved;
		
		public void makeMovePieceSpecific(Move move) {
			Move dummyMove = new Move();
			
			makeMovePieceCommon(move);
			this.hasMoved = true;
			//remove castle from list of possible moves
			relativeCoordSet.remove(new Coord(2,0));
			relativeCoordSet.remove(new Coord(-2,0));
			//check if you are castling
			if(dummyMove.absCoordDeltaFromMove(move)==new Coord(2,0)) {
				//castle with the rook located at the rook's origin which is in the direction of the castle
				if(dummyMove.moveIsToDirection(move,"right")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocation(new Coord(8,1),new Coord(6,1));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocation(new Coord(8,8),new Coord(6,8));
					}
				} else if (dummyMove.moveIsToDirection(move,"left")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocation(new Coord(1,1),new Coord(4,1));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocation(new Coord(1,8),new Coord(4,8));
					}
				}
			}
		}
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
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♔';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♚';
			}
			this.hasMoved = false;
			this.relativeCoordSet = testableRelativeCoord();
		}
	}
	
	GameState() {
		this.chessPieces = new HashSet<Piece>();
		this.graveyard = new ArrayList<Piece>();
		
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
