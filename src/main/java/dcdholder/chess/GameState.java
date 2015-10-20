package dcdholder.chess;
import java.util.*;
import java.util.regex.*;
import java.io.*;

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
	
	Player whitePlayer = new Human(PieceColour.WHITE);
	Player blackPlayer = new Human(PieceColour.BLACK);
	
	ArbiterLogger logger = new ArbiterLogger();
	
	public static void main (String[] args) {
		GameState currentGame = new GameState();
		currentGame.drawStartScreenCli();
		while(true) {
			currentGame.drawBoardCli();
			currentGame.currentPlayerMakeMove();
			//resetEnPassants();
		}
	}
	/*
	public void configScreenCli() {
		System.out.println("Enter a player config, or enter 'q' to quit: ");
		
		//TODO: add some console input and regexing here, do some looping
		//will look something like this -> h-c (human white player, computer black player)
		
		if(whiteConfig=="Human") {
			whitePlayer = new Human(PieceColour.WHITE);
		} else if(whiteConfig=="RngCpu") {
			whitePlayer = new RngCpu(PieceColour.WHITE);
		}
		if(blackConfig=="Human") {
			blackPlayer = new Human(PieceColour.BLACK);
		} else if(blackConfig=="RngCpu") {
			blackPlayer = new RngCpu(PiceColour.BLACK);
		}
		
		drawStartScreenCli();
	 */
	
	public void drawStartScreenCli() {
		System.out.println("DC-CHESS");
		System.out.println("--------");
		System.out.println("");
	}
	
	public String createBoardString() {
		String boardString = new String();
		String[] boardLine = new String[8];
		//first, draw the board from the perspective of the white player, then draw depending on the current player
		//board is initially in the correct left-to-right order with respect to white
		for(int j=1;j<=8;j++) {
			boardLine[j-1] = "";
			for(int i=1;i<=8;i++) {
				if(coordContainsPiece(new Coord(i,j))) {
					boardLine[j-1] = boardLine[j-1] + Character.toString(getPieceAtLocation(new Coord(i,j)).getRepChar());
				} else {
					if((i+j)%2==0) {
						boardLine[j-1] = boardLine[j-1] + Character.toString('■');
					} else {
						boardLine[j-1] = boardLine[j-1] + Character.toString('□');
					}
				}
			}
		}
		//orient correctly for the current player
		if(currentPlayer==PieceColour.WHITE) {
			List<String> tmpList = Arrays.asList(boardLine);
			Collections.reverse(tmpList);
			boardLine = tmpList.toArray(boardLine);
		} else if(currentPlayer==PieceColour.BLACK) {
			for(int j=1;j<=8;j++) {
				boardLine[j-1] = new StringBuilder(boardLine[j-1]).reverse().toString();
			}
		}
		//transform into a single multi-line string, so that this 
		for(int j=1;j<=8;j++) {
			boardString = boardString + boardLine[j-1] + "\n";
		}
		
		return boardString;
	}
	
	public void drawBoardCli() {
		System.out.println(createBoardString());
	}
	
	public Set<Move> getAllLegalMoves() {
		Set<Move> allLegalMoves = new HashSet<Move>();
		
		for(Piece piece : chessPieces) {
			allLegalMoves.addAll(piece.getLegalMoves());
		}
		
		return allLegalMoves;
	}
	
	public Set<Move> getAllLegalMovesCheckless() {
		Set<Move> allLegalMovesCheckless = new HashSet<Move>();
		
		for(Piece piece : chessPieces) {
			allLegalMovesCheckless.addAll(piece.getLegalMovesCheckless());
		}
		
		return allLegalMovesCheckless;
	}
	
	public void removePieceAtLocation(Coord removeCoord) {
		for(Piece chessPiece : chessPieces) {
			if(chessPiece.getPieceCoord().equals(removeCoord)) {
				chessPieces.remove(chessPiece);
				graveyard.add(chessPiece);
				break;
			}
		}
	}
	
	public void movePieceAtLocation(Move moveAttempt) {
		Coord initialCoord = moveAttempt.getInit();
		Coord destCoord = moveAttempt.getDest();
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
		//switch to next player for next turn
		if(currentPlayer==PieceColour.WHITE) {
			currentPlayer=PieceColour.BLACK;
		} else {
			currentPlayer=PieceColour.WHITE;
		}
	}
	
	public void currentPlayerMakeMove() {
		if(currentPlayer==PieceColour.WHITE) {
			whitePlayer.getAndMakeNextMove();
		} else {
			blackPlayer.getAndMakeNextMove();
		}
	}
	
	public boolean isMoveLegalCheckless(Move moveAttempt) {
		if(coordContainsPiece(moveAttempt.getInit())) {
			return getPieceAtLocation(moveAttempt.getInit()).isMoveLegalPieceSpecific(moveAttempt);
		} else {
			return false;
		}
	}
	
	//TODO: force failure until check checking is complete
	public boolean isMoveLegalWithCheck(Move moveAttempt) {
		if(!isMoveLegalCheckless(moveAttempt)) {
			return false;
		}
		GameState checkVerificationGame = new GameState(this); //create temporary instance of game so as to not risk corrupting game state
		if(coordContainsPiece(moveAttempt.getInit())) {
			checkVerificationGame.movePieceAtLocation(moveAttempt); //simulate making the move in a temporary game instance
		} else {
			return false;
		}
		if(checkVerificationGame.opponentIsInCheck()) {
			return false;
		}
		
		return false; //TODO: change to 'return true' once everything is ready
	}
	
	public boolean isMoveToSquareLegalCheckless(Coord destCoord) {
		Set<Move> legalMoves = getAllLegalMovesCheckless();
		for(Move move : legalMoves) {
			if(move.getDest().equals(destCoord)) {
				return true;
			}
		}
		return false;
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
	
	/*TODO: implement this
	public boolean rowRangeOccupied(int fileBoundA, int fileBoundB, int rank) {
		
		
		return true;
	}
	*/
	/*TODO: implement this
	//public boolean rowRangeUnderAttack(int fileBoundA, int fileBoundB, int rank) {
		
		
		return true;
	}
	*/
	
	//TODO: cut down on code duplication
	//public boolean squareUnderAttackByOpponent() {
		//the same as squareUnderAttackByCurrent(), but currentPlayer is manually switched in the new game
	//}
	public boolean squareUnderAttackByCurrent(Coord evalCoord) {
		//check if there is an opponent piece already at that position
		//if not, generate an opponent pawn there (remove once done)
		//if a move to capture that piece meets checkless legality criteria, the square is under attack
		if(coordContainsPiece(evalCoord)) {
			Piece evalPiece = getPieceAtLocation(evalCoord);
			if(evalPiece.pieceColour==getOpponentColour()) {
				if(isMoveToSquareLegalCheckless(evalCoord)) {return true;} else {return false;}//shallow move legality checking
			} else {
				return false; //the piece is owned by the current player, the current player cannot attack it
			}
		} else {
			chessPieces.add(new Pawn(getOpponentColour(),evalCoord)); //temporarily add a pawn to check if it can be captured by the current player
			if(isMoveToSquareLegalCheckless(evalCoord)) {return true;} else {return false;}
		}
	}
	
	public boolean opponentIsInCheck() {
		//find the opponent's king, check if it is under attack by the current player
		boolean kingFound=false;
		boolean opponentInCheck=false;
		
		for(Piece piece : chessPieces) {
			if(piece.pieceColour==getOpponentColour() && (piece instanceof King)) {
				opponentInCheck = squareUnderAttackByCurrent(piece.pieceCoord);
				kingFound=true;
				break;
			}
		}
		if(!kingFound) {
			throw new IllegalStateException("No opposing king of colour " + getOpponentColour() + " found on the board");
		}
		return opponentInCheck;
	}
	
	public PieceColour getOpponentColour() {
		if(currentPlayer==PieceColour.WHITE) {
			return PieceColour.BLACK;
		} else {
			return PieceColour.WHITE;
		}
	}
	
	enum PieceColour {
		WHITE,BLACK;
	}
	
	interface playerSpecific {
		Move getNextMove();
		void getAndMakeNextMove();
	}
	abstract public class Player implements playerSpecific {
		PieceColour playerColour;
		
		//TODO: rewrite the try-catch clauses
		public void getAndMakeNextMove() {
			Move nextMove;
			while(true) {
				try {
					nextMove = getNextMove();
					break;
				} catch(IllegalArgumentException wrongMoveFormat) {
					System.out.println("Wrong move format - try again");
				}
			}
			while(!isMoveLegalCheckless(nextMove)) {
				System.out.println(nextMove.toString());
				System.out.println("Illegal move - try again");
				while(true) {
					try {
						nextMove = getNextMove();
						break;
					} catch(IllegalArgumentException wrongMoveFormat) {
						System.out.println("Wrong move format - try again");
					}
				}
			}
			movePieceAtLocation(nextMove);
		}
		
		Player(Player copyPlayer) {
			this.playerColour = copyPlayer.playerColour;
		}
		Player(PieceColour playerColour) {
			this.playerColour = playerColour;
		}
	}
	class Human extends Player {
		String TMP_MOVE_PATTERN = "(?<initX>[0-9])(?<initY>[0-9])-(?<destX>[0-9])(?<destY>[0-9])";
		
		public Move getNextMove() {
			BufferedReader cliInput = new BufferedReader(new InputStreamReader(System.in));
			String inputLine = "";
			
			if(currentPlayer==PieceColour.WHITE) {
				System.out.println("White to move: ");
			} else if(currentPlayer==PieceColour.BLACK) {
				System.out.println("Black to move: ");
			}
			
			try {
				inputLine = cliInput.readLine();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			//TODO: use pgnParser format, this is just something temporary for initial testing
			Pattern tmpMoveRegex = Pattern.compile(TMP_MOVE_PATTERN);
			Matcher tmpMoveMatch = tmpMoveRegex.matcher(inputLine);
			
			if(inputLine.equals("q")) {
				System.exit(0);
			}
			if(tmpMoveMatch.find()) {
				Coord init = new Coord(Integer.parseInt(tmpMoveMatch.group("initX")),Integer.parseInt(tmpMoveMatch.group("initY")));
				Coord dest = new Coord(Integer.parseInt(tmpMoveMatch.group("destX")),Integer.parseInt(tmpMoveMatch.group("destY")));
				
				System.out.println("");
				
				return new Move(init,dest);
			} else {
				throw new IllegalArgumentException("Wrong move format");
			}
		}
		
		Human(Human copyHuman) {super(copyHuman);}
		Human(PieceColour playerColour) {
			super(playerColour);
		}
	}
	//TODO: implement an AI which simply plays random legal moves from the legal move list
	//abstract class Ai extends Player {
	//	public Move getNextMove() {
	//		
	//	}
	//}
	//class rngAi extends Ai {
	//}
	//class oneFoldAi extends Ai {
	//}
	
	class ArbiterLogger {
		//must be able to toggle to prevent AI move attempts from flooding output
		boolean loggerOn;
		
		//will be made more sophisticated later
		public void log(String arbiterMessage) {
			System.out.println("");
		}
		
		//does nothing extra for now, may need to copy log information in the future
		ArbiterLogger(ArbiterLogger copyLogger) {
			this();
		}
		ArbiterLogger() {
			loggerOn = true;
		}
	}
	
	abstract public class Piece implements PieceSpecific {
		//REP_CHAR is set by pieces individually according to type and colour
		char REP_CHAR = 'X';
		Coord pieceCoord;
		PieceColour pieceColour;
		boolean collisionChecking;
		Set<Coord> relativeCoordSet;
		
		public Coord getPieceCoord() {return this.pieceCoord;}
		public char getRepChar() {return this.REP_CHAR;}
		
		public Set<Move> getLegalMoves() {
			Set<Move> initialMoves = formMovesFromRelativeCoordSet();
			Set<Move> finalMoves = new HashSet<>();
			
			for(Move move : initialMoves) {
				if(isMoveLegalWithCheck(move)) { //TESTING
					finalMoves.add(move);
				}
			}
			return finalMoves;
		}
		public Set<Move> getLegalMovesCheckless() {
			Set<Move> initialMoves = formMovesFromRelativeCoordSet();
			Set<Move> finalMoves = new HashSet<>();
			
			for(Move move : initialMoves) {
				if(isMoveLegalCheckless(move)) {
					finalMoves.add(move);
				}
			}
			return finalMoves;
		}
		public Set<Move> formMovesFromRelativeCoordSet() {
			Set<Move> testableMoveSet = new HashSet<Move>();
			
			for(Coord relativeCoord : relativeCoordSet) {
				if(Move.wouldBeLegalMoveObjectFromRelative(this.pieceCoord,relativeCoord)) {
					testableMoveSet.add(new Move(new Coord(this.pieceCoord),Coord.addCoords(this.pieceCoord,relativeCoord)));
				}
			}
			
			return testableMoveSet;
		}
		
		public void makeMovePieceCommon(Move move) {
			removePieceAtLocation(move.getDest());
			pieceCoord = new Coord(move.getDest());
		}
		//TODO: this should probably call pieceSpecificArbiter, not the other way around
		//TODO: rename "moveLegal" to "arbiter"
		public boolean isMoveLegalCommon(Move moveAttempt) {
			@SuppressWarnings("unused") Piece currentPiece = getPieceAtLocation(moveAttempt.getInit());
			boolean checkVertical = false, checkHorizontal = false;
			int incHorizontal=0, incVertical=0;
			int horizontalShift=0, verticalShift=0;
			int xToCheck, yToCheck;
			
			//check if the initial coords match the piece's
			if(!moveAttempt.getInit().equals(this.pieceCoord)) {
				return false;
			}
			//cannot move opponent's piece!
			if(pieceColour!=currentPlayer) {
				return false;
			}
			//check if the move is in the potential move list
			if(!formMovesFromRelativeCoordSet().contains(moveAttempt)) {
				for(Move tmpMove : formMovesFromRelativeCoordSet()) {
					System.out.println(tmpMove.hashCode() + " " + tmpMove.equals(moveAttempt));
				}
				return false;
			}
			//cannot capture own piece!
			if(coordContainsPiece(moveAttempt.getDest())) {
				if(getPieceAtLocation(moveAttempt.getDest()).pieceColour==currentPlayer) {
					return false;
				}
			}
			//check if there would be a collision
			if(collisionChecking) {
				if(moveAttempt.getInit().getX()!=moveAttempt.getDest().getX()) {
					checkHorizontal = true;
				}
				if(moveAttempt.getInit().getY()!=moveAttempt.getDest().getY()) {
					checkVertical = true;
				}
				
				if(checkHorizontal&&checkVertical) {
					if(Move.absCoordDeltaFromMove(moveAttempt).getX()==Move.absCoordDeltaFromMove(moveAttempt).getY()) {
						//diagonal move
						if(moveAttempt.getDest().getX()-moveAttempt.getInit().getX()>0) {
							incHorizontal=1;
						} else {
							incHorizontal=-1;
						}
						if(moveAttempt.getDest().getY()-moveAttempt.getInit().getY()>0) {
							incVertical=1;
						} else {
							incVertical=-1;
						}
						for(int i=moveAttempt.getInit().getX()+1;i<moveAttempt.getDest().getX();i++) {
							horizontalShift+=incHorizontal;
							verticalShift+=incVertical;
							
							xToCheck = moveAttempt.getInit().getX()+horizontalShift;
							yToCheck = moveAttempt.getInit().getY()+verticalShift;
							if(coordContainsPiece(new Coord(xToCheck,yToCheck))) {
								return false;
							}
						}
					} else {
						throw new IllegalArgumentException("Invalid move direction with collision checking enabled");
					}
				} else if(checkHorizontal&&!checkVertical) {
					//horizontal move
					if(moveAttempt.getDest().getX()-moveAttempt.getInit().getX()>0) {
						for(int i=moveAttempt.getInit().getX()+1;i<moveAttempt.getDest().getX();i++) {
							if(coordContainsPiece(new Coord(i,moveAttempt.getInit().getY()))) {
								return false;
							}
						}
					} else {
						for(int i=moveAttempt.getInit().getX()-1;i>moveAttempt.getDest().getX();i--) {
							if(coordContainsPiece(new Coord(i,moveAttempt.getInit().getY()))) {
								return false;
							}
						}
					}
				} else if(!checkHorizontal&&checkVertical) {
					//vertical move
					if(moveAttempt.getDest().getY()-moveAttempt.getInit().getY()>0) {
						for(int j=moveAttempt.getInit().getY()+1;j<moveAttempt.getDest().getY();j++) {
							if(coordContainsPiece(new Coord(moveAttempt.getInit().getX(),j))) {
								return false;
							}
						}
					} else {
						for(int j=moveAttempt.getInit().getY()-1;j>moveAttempt.getDest().getY();j--) {
							if(coordContainsPiece(new Coord(moveAttempt.getInit().getX(),j))) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}
		
		public int hashCode() {
			//the coordinates should be enough to identify a piece, but just to be safe...
			int hash = pieceCoord.hashCode();
			if(pieceColour==PieceColour.WHITE) {
				hash++;
			}
			return hash;
		}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Piece) {
				Piece testO = (Piece)o;
				if(this.pieceCoord.equals(testO.pieceCoord)&&this.pieceColour==testO.pieceColour) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Piece(Piece copyPiece) {
			this.pieceColour = copyPiece.pieceColour;
			this.collisionChecking = copyPiece.collisionChecking;
			this.pieceCoord = new Coord(copyPiece.pieceCoord);
		}
		Piece(PieceColour pieceColour, Coord pieceCoord) {
			this.pieceColour = pieceColour;
			this.collisionChecking = false;
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
		public boolean wasLastMoveDouble() {
			if(lastMoveWasDouble) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			}
			//only allow double moves from the home row
			if(Move.absCoordDeltaFromMove(moveAttempt).equals(new Coord(0,2))) {
				if(pieceColour==PieceColour.WHITE && moveAttempt.getInit().getY()!=2) {
					return false;
				} else if(pieceColour==PieceColour.BLACK && moveAttempt.getInit().getY()!=7) {
					return false;
				}
			}
			//only allow captures on diagonal moves
			if(!moveAttempt.moveIsToDirection("diagonal")&&coordContainsPiece(moveAttempt.getDest())) {
				return false;
			}
			//only allow captures moves if there is a piece to capture in the first place, or if it is an en passant
			if(Move.absCoordDeltaFromMove(moveAttempt).equals(new Coord(1,1))) {
				//only allow en passants if the other piece is a pawn, and its lastMoveWasDouble variable is set (needs to be reset every turn)
				if(!coordContainsPiece(moveAttempt.getDest())) {
					if(coordContainsPiece(new Coord(moveAttempt.getDest().getX(),moveAttempt.getInit().getY()))) {
						Piece pieceToCapture = getPieceAtLocation(new Coord(moveAttempt.getDest().getX(),moveAttempt.getInit().getY()));
						//determine whether the piece to be en-passanted is a pawn in the first place
						if(pieceToCapture instanceof Pawn) {
							//TODO: see if there's a cleaner way to do this than checking for subtype and making a cast
							Pawn pawnToCapture = (Pawn)pieceToCapture;
							//check if the last move of the opponent was a double move with the pawn to be captured
							if(!pawnToCapture.lastMoveWasDouble) {
								return false;
							}
						} else {
							return false;
						}
					} else { //not an en-passant and is a capture move, but no piece to capture
						return false;
					}
				}
			}
			return true;
		}
		//makeMove should only be called through attemptMove, which should validate it
		public void makeMovePieceSpecific(Move move) {
			//check if move is an en passant... if so, remove other piece
			if(Move.absCoordDeltaFromMove(move).equals(new Coord(1,1)) && !coordContainsPiece(move.getDest())) {
				if(move.moveIsToDirection("right")) {
					removePieceAtLocation(new Coord(move.getInit().getX()+1,move.getInit().getY()));
				} else if (move.moveIsToDirection("left")) {
					removePieceAtLocation(new Coord(move.getInit().getX()-1,move.getInit().getY()));
				}
			}
			makeMovePieceCommon(move);
			//check if the last move was a double move
			if(Move.absCoordDeltaFromMove(move).equals(new Coord(0,2))) {this.lastMoveWasDouble=true;}
			//take double moves off the table if the piece has already been moved
			relativeCoordSet.remove(new Coord(0,2));
			//check if the move results in pawn promotion... if so, create new piece and remove self
			if((pieceColour==PieceColour.WHITE && move.getDest().getY()==8) || (pieceColour==PieceColour.BLACK && move.getDest().getY()==1)) {
				//produce a new queen at current location and delete self
				chessPieces.add(new Queen(pieceColour,move.getDest()));
				chessPieces.remove(this);
			}
		}
		public Set<Coord> testableRelativeCoord() {
			Set<Coord> testableRelativeCoord = new HashSet<Coord>();
			
			//forward moves
			if(pieceColour==PieceColour.WHITE) {
				testableRelativeCoord.add(new Coord(0,1));
				testableRelativeCoord.add(new Coord(0,2));
			} else if(pieceColour==PieceColour.BLACK) {
				testableRelativeCoord.add(new Coord(0,-1));
				testableRelativeCoord.add(new Coord(0,-2));
			}
			//capture moves
			testableRelativeCoord.add(new Coord(1,1));
			testableRelativeCoord.add(new Coord(-1,1));
			
			return testableRelativeCoord;
		}
		
		public int hashCode() {
			return super.hashCode();
		}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Pawn) {
				Pawn testPawn = (Pawn)o;
				if(super.equals(testPawn)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Pawn(Piece copyPawn) {
			super(copyPawn);
			Pawn castPawn = (Pawn)copyPawn;
			this.lastMoveWasDouble = castPawn.lastMoveWasDouble;
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
		public boolean hasRookMoved() {
			if(hasMoved) {
				return true;
			} else {
				return false;
			}
		}
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
			this.hasMoved = true;
		}
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			} else {
				return true;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Rook) {
				Rook testRook = (Rook)o;
				if(super.equals(testRook)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Rook(Piece copyRook) {
			super(copyRook);
			Rook castRook = (Rook)copyRook;
			this.hasMoved = castRook.hasMoved;
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
				return true;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Bishop) {
				Bishop testBishop = (Bishop)o;
				if(super.equals(testBishop)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Bishop(Piece copyBishop) {
			super(copyBishop);
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
				return true;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Knight) {
				Knight testKnight = (Knight)o;
				if(super.equals(testKnight)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Knight(Piece copyKnight) {
			super(copyKnight);
		}
		Knight(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♘';
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♞';
			}
			this.relativeCoordSet = testableRelativeCoord();
			this.collisionChecking = false;
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
				return true;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Knight) {
				Queen testQueen = (Queen)o;
				if(super.equals(testQueen)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		Queen(Piece copyQueen) {
			super(copyQueen);
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
			makeMovePieceCommon(move);
			this.hasMoved = true;
			//remove castle from list of possible moves
			relativeCoordSet.remove(new Coord(2,0));
			relativeCoordSet.remove(new Coord(-2,0));
			//check if you are castling
			if(Move.absCoordDeltaFromMove(move).equals(new Coord(2,0))) {
				//castle with the rook located at the rook's origin which is in the direction of the castle
				if(move.moveIsToDirection("right")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocation(new Move(new Coord(8,1),new Coord(6,1)));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocation(new Move(new Coord(8,8),new Coord(6,8)));
					}
				} else if (move.moveIsToDirection("left")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocation(new Move(new Coord(1,1),new Coord(4,1)));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocation(new Move(new Coord(1,8),new Coord(4,8)));
					}
				}
			}
		}
		public boolean isMoveLegalPieceSpecific(Move moveAttempt) {
			if(!isMoveLegalCommon(moveAttempt)) {
				return false;
			}
			//check if the move is an attempted castle
			if(Move.absCoordDeltaFromMove(moveAttempt).equals(new Coord(2,0))) {
				Coord legalQueensideRookLocation = new Coord();
				Coord legalKingsideRookLocation = new Coord();
				Coord rookLocation = new Coord();
				
				//get the legal rook castling positions for the piece's colour
				if(pieceColour==PieceColour.WHITE) {
					legalQueensideRookLocation = new Coord(8,1);
					legalKingsideRookLocation = new Coord(1,1);
				} else if(pieceColour==PieceColour.BLACK) {
					legalQueensideRookLocation = new Coord(8,8);
					legalKingsideRookLocation = new Coord(1,8);
				}
				//check that the rook being moved towards exists
				if(moveAttempt.moveIsToDirection("right")) {
					rookLocation = new Coord(legalQueensideRookLocation);
				}
				if(moveAttempt.moveIsToDirection("left")) {
					rookLocation = new Coord(legalKingsideRookLocation);
				}
				//check that a piece exists at that location and is in fact a rook
				//check that the rook being moved towards has not yet moved
				if(coordContainsPiece(rookLocation)) {
					if(getPieceAtLocation(rookLocation) instanceof Rook) {
						//TODO: see if there's a way to work around having to cast
						//TODO: consider adding rook colour checking, even though enemy rooks in this position would fail hasMoved check
						Rook castlingRook = (Rook)getPieceAtLocation(rookLocation);
						if(castlingRook.hasRookMoved()) {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
				//if the king has already moved
				if(hasMoved) {
					return false;
				}
				//TODO: get castling working
				//TODO: if any of the squares between the two are occupied
				//if(rowRangeOccupied(moveAttempt.getInit().getX(),moveAttempt.getDest().getX())) {
				//	return false;
				//}
				//TODO: if any of the squares between the two (and including those two) are under attack
				//if(rowRangeUnderAttack(moveAttempt.getInit().getX(),moveAttempt.getDest().getX())) {
				//	return false;
				//}
			}
			return true;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof King) {
				King testKing = (King)o;
				if(super.equals(testKing)) {return true;} else {return false;}
			} else {
				return false;
			}
		}
		
		King(Piece copyKing) {
			super(copyKing);
			King castKing = (King)copyKing;
			this.hasMoved = castKing.hasMoved;
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
	
	GameState(GameState gameCopy) {
		this.currentPlayer = gameCopy.currentPlayer;
		
		//deep copy of pieces
		this.chessPieces = new HashSet<Piece>();
		this.graveyard = new ArrayList<Piece>();
		//TODO: EXTREMELY inelegant, can we work around inability to instantiate abstract class?
		for(Piece chessPiece : gameCopy.chessPieces) {
			if(chessPiece instanceof Pawn) {this.chessPieces.add(new Pawn(chessPiece));
			} else if(chessPiece instanceof Knight) {this.chessPieces.add(new Knight(chessPiece));
			} else if(chessPiece instanceof Bishop) {this.chessPieces.add(new Bishop(chessPiece));
			} else if(chessPiece instanceof Rook) {this.chessPieces.add(new Rook(chessPiece));
			} else if(chessPiece instanceof Queen) {this.chessPieces.add(new Queen(chessPiece));
			} else if(chessPiece instanceof King) {this.chessPieces.add(new King(chessPiece));}
		}
		for(Piece graveyardPiece : gameCopy.graveyard) {
			if(graveyardPiece instanceof Pawn) {this.graveyard.add(new Pawn(graveyardPiece));
			} else if(graveyardPiece instanceof Knight) {this.graveyard.add(new Knight(graveyardPiece));
			} else if(graveyardPiece instanceof Bishop) {this.graveyard.add(new Bishop(graveyardPiece));
			} else if(graveyardPiece instanceof Rook) {this.graveyard.add(new Rook(graveyardPiece));
			} else if(graveyardPiece instanceof Queen) {this.graveyard.add(new Queen(graveyardPiece));
			} else if(graveyardPiece instanceof King) {this.graveyard.add(new King(graveyardPiece));}
		}
		
		//TODO: fix instantiation here before computer players become an option
		this.whitePlayer = new Human((Human)gameCopy.whitePlayer);
		this.blackPlayer = new Human((Human)gameCopy.blackPlayer);
		
		this.logger = new ArbiterLogger(gameCopy.logger);
	}
	
	GameState() {
		this.currentPlayer = PieceColour.WHITE;
		
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
		chessPieces.add(new Queen(PieceColour.BLACK,new Coord(4,8)));
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
		chessPieces.add(new Knight(PieceColour.WHITE,new Coord(2,1)));
		chessPieces.add(new Knight(PieceColour.WHITE,new Coord(7,1)));
		chessPieces.add(new Knight(PieceColour.BLACK,new Coord(2,8)));
		chessPieces.add(new Knight(PieceColour.BLACK,new Coord(7,8)));
	}
}
