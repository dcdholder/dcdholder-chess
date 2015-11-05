package dcdholder.chess;
import java.util.*;
import java.util.regex.*;
import java.io.*;
//import java.util.concurrent.*;

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
	List<Piece> chessPieces;
	//this is a list because pieces that have been captured may have identical state at the point of capture to pieces that were captured earlier
	List<Piece> graveyard;
	
	PieceColour currentPlayer;
	PieceColour winner;
	boolean gameOver,isCheckmate,isStalemate;
	int numPlies = 0;
	int fiftyMoveRuleCounter = 0;
	static int FIFTY_MOVE_RULE_MAX = 100;
	
	Player whitePlayer = new Human(PieceColour.WHITE);
	Player blackPlayer = new Human(PieceColour.BLACK);
	
	public static void main (String[] args) {
		GameState currentGame = new GameState();
		currentGame.drawStartScreenCli();
		currentGame.drawConfigScreenCli();
		//Comment in for benchmarking
		//System.out.println(System.currentTimeMillis());
		while(true) {
			currentGame.updateGameState();
			if(!currentGame.gameOver) {
				currentGame.drawBoardCli();
				currentGame.currentPlayerMakeMove();
				//System.out.println(currentGame.numPlies);
			} else {
				break;	
			}
		}
		//System.out.println(System.currentTimeMillis());
		currentGame.drawBoardCli();
		currentGame.drawEndgameScreenCli();
	}

	//TODO: see if I can make this a little less verbose
	public void drawConfigScreenCli() {
		//TODO: decide whether I want to do this with try-catch clauses or not
		while(true) {
			try {
				//will look something like this -> h-8 (human white player, 8-ply-depth black player)
				System.out.println("Enter a player config, or enter 'q' to quit: ");
				BufferedReader cliInput = new BufferedReader(new InputStreamReader(System.in));
				String inputLine = "";
				
				try {
					inputLine = cliInput.readLine();
				} catch(IOException e) {
					e.printStackTrace();
				}
				
				Pattern tmpConfigRegex = Pattern.compile("(?<whitePlayer>[hr1-7])-(?<blackPlayer>[hr1-7])");
				Matcher tmpConfigMatch = tmpConfigRegex.matcher(inputLine);
				
				if(inputLine.equals("q")) {
					System.exit(0);
				}
				if(tmpConfigMatch.find()) {
					if(tmpConfigMatch.group("whitePlayer").equals("h")) {
						whitePlayer = new Human(PieceColour.WHITE);
					} else if(tmpConfigMatch.group("whitePlayer").equals("r")) {
						whitePlayer = new RngAi(PieceColour.WHITE);
					} else {
						whitePlayer = new StaticPlyAiWithThreading(PieceColour.WHITE, Integer.parseInt(tmpConfigMatch.group("whitePlayer")));
					}
					if(tmpConfigMatch.group("blackPlayer").equals("h")) {
						blackPlayer = new Human(PieceColour.BLACK);
					} else if(tmpConfigMatch.group("blackPlayer").equals("r")) {
						blackPlayer = new RngAi(PieceColour.BLACK);
					} else {
						blackPlayer = new StaticPlyAiWithThreading(PieceColour.BLACK, Integer.parseInt(tmpConfigMatch.group("blackPlayer")));
					}
					break;
				} else {
					throw new IllegalArgumentException("Wrong config format - try again");
				}
			} catch(IllegalArgumentException wrongConfigFormat) {
				System.out.println(wrongConfigFormat.getMessage());
			}
		}
		System.out.println("");
	}
	
	public void drawStartScreenCli() {
		System.out.println("GLUGGLE-CHESS");
		System.out.println("-------------");
		System.out.println("");
	}
	
	public void drawEndgameScreenCli() {
		if(isCheckmate) {
			System.out.println(winner + " wins after " + numPlies + " plies");
		} else if(isStalemate) {
			System.out.println("Game ends in stalemate after " + numPlies + " plies");
		} else {
			throw new IllegalStateException("Game was found to be over, but neither 'isCheckmate' nor 'isStalemate' were set");
		}
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
		//add rank indicators
		if(currentPlayer==PieceColour.BLACK) {
			for(int j=1;j<=8;j++) {boardLine[j-1] = j + boardLine[j-1];}
		} else if(currentPlayer==PieceColour.WHITE) {
			for(int j=1;j<=8;j++) {boardLine[j-1] = 9-j + boardLine[j-1];}
		}
		//transform into a single multi-line string
		for(int j=1;j<=8;j++) {
			boardString = boardString + boardLine[j-1] + "\n";
		}
		
		int ASCII_CODE_FOR_a = 97;
		char fileChar;
		String fileString = "";
		for(int i=1;i<=8;i++) {
			fileChar = (char)(i+ASCII_CODE_FOR_a-1);
			fileString = fileString + fileChar;
		}
		if(currentPlayer==PieceColour.BLACK) {
			fileString = new StringBuilder(fileString).reverse().toString();
		}
		boardString = boardString + " " + fileString + "\n";
		
		return boardString;
	}
	
	public void drawBoardCli() {
		System.out.println("Ply " + numPlies + ":");
		System.out.println("-------");
		System.out.println(createBoardString());
	}
	
	public Set<Move> getAllLegalMovesWithCheck() {
		Set<Move> allLegalMoves = new HashSet<Move>();
		
		for(Piece piece : chessPieces) {
			allLegalMoves.addAll(piece.getLegalMovesWithCheck());
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
	
	public void updateGameState() {
		if(!gameOver) {
			if(fiftyMoveRuleCounter==FIFTY_MOVE_RULE_MAX) { //"fifty moves" corresponds to 100 plies, which are easier to keep track of
				gameOver = true;
				isStalemate = true;
			} else if(getAllLegalMovesWithCheck().size()==0) {
				gameOver = true;
				if(currentIsInCheck()) {
					isCheckmate = true;
					winner = getOpponentColour();
				} else {
					isStalemate = true;
				}
			}
		}
		//update en passant state - don't bother doing this if the game is already over
		if(!gameOver) {
			for(Piece piece : chessPieces) {
				if(piece instanceof Pawn) {
					Pawn pawn = (Pawn)piece;
					if(currentPlayer==pawn.pieceColour) {
						pawn.lastMoveWasDouble = false;
					}
				}
			}
		}
		//also increment the ply counter
		if(!gameOver) {
			numPlies++;
		}
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
		movePieceAtLocationWithoutSwitching(moveAttempt);
		switchPlayer(); //switch to next player for next turn
	}
	//necessary for castling
	public void movePieceAtLocationWithoutSwitching(Move moveAttempt) {
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
			drawBoardCli();
			throw new IllegalArgumentException("Was not able to move piece at location " + initialCoord.toString() + " since none exists");
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
		
		return true;
	}
	
	public boolean isMoveToSquareLegalCheckless(Coord destCoord) {
		for(Piece testPiece : chessPieces) {
			if(Move.wouldBeLegalMoveObject(testPiece.pieceCoord,destCoord)) {
				if(testPiece.isMoveLegalPieceSpecific(new Move(testPiece.pieceCoord,destCoord))) {
					return true;
				}
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
	
	public boolean rowRangeOccupied(int fileBoundA, int fileBoundB, int rank) {
		Set<Coord> coordsBetweenFiles = Coord.allCoordsBetweenFiles(fileBoundA,fileBoundB,rank);
		for(Coord coordBetweenFiles : coordsBetweenFiles) {
			if(coordContainsPiece(coordBetweenFiles)) {
				return true;
			}
		}
		return false;
	}

	public boolean rowRangeUnderAttackByOpponent(int fileBoundA, int fileBoundB, int rank) {
		Set<Coord> coordsBetweenFiles = Coord.allCoordsBetweenFiles(fileBoundA,fileBoundB,rank);
		for(Coord coordBetweenFiles : coordsBetweenFiles) {
			if(squareUnderAttackByOpponent(coordBetweenFiles)) {
				return true;
			}
		}
		return false;
	}
	
	public void switchPlayer() {currentPlayer = getOpponentColour();}
	public boolean squareUnderAttackByOpponent(Coord evalCoord) {
		//the same as squareUnderAttackByCurrent(), but currentPlayer is to the opposing player and back again
		boolean squareIsUnderAttackByOpponent;
		
		switchPlayer();
		squareIsUnderAttackByOpponent = squareUnderAttackByCurrent(evalCoord);
		switchPlayer();
		
		return squareIsUnderAttackByOpponent;
	}
	public boolean squareUnderAttackByCurrent(Coord evalCoord) {
		//check if there is an opponent piece already at that position
		//if not, generate an opponent pawn there (remove once done)
		//if a move to capture that piece meets checkless legality criteria, the square is under attack
		GameState tmpGame;
		
		if(coordContainsPiece(evalCoord)) {
			Piece evalPiece = getPieceAtLocation(evalCoord);
			if(evalPiece.pieceColour==getOpponentColour()) {
				if(isMoveToSquareLegalCheckless(evalCoord)) {return true;} else {return false;} //shallow move legality checking
			} else {
				return false; //the piece is owned by the current player, the current player cannot attack it
			}
		} else {
			tmpGame = new GameState(this);
			tmpGame.chessPieces.add(new Pawn(getOpponentColour(),evalCoord)); //temporarily add a pawn to check if it can be captured by the current player
			if(tmpGame.isMoveToSquareLegalCheckless(evalCoord)) {return true;} else {return false;}
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
			throw new IllegalStateException("No king of colour " + getOpponentColour() + " found on the board");
		}
		return opponentInCheck;
	}
	public boolean currentIsInCheck() {
		boolean currentInCheck;
		
		switchPlayer();
		currentInCheck = opponentIsInCheck();
		switchPlayer();
		
		return currentInCheck;
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
		
		Player(Player copyPlayer) {
			this.playerColour = copyPlayer.playerColour;
		}
		Player(PieceColour playerColour) {
			this.playerColour = playerColour;
		}
	}
	class Human extends Player {
		String TMP_MOVE_PATTERN = "(?<initX>[a-h])(?<initY>[1-8])-(?<destX>[a-h])(?<destY>[1-8])";
		
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
				//convert file characters into ints, use regex to build Move from input
				int ASCII_CODE_FOR_a = 97;
				int initX = (int)tmpMoveMatch.group("initX").charAt(0)-ASCII_CODE_FOR_a+1;
				int destX = (int)tmpMoveMatch.group("destX").charAt(0)-ASCII_CODE_FOR_a+1;
				
				Coord init = new Coord(initX,Integer.parseInt(tmpMoveMatch.group("initY")));
				Coord dest = new Coord(destX,Integer.parseInt(tmpMoveMatch.group("destY")));
				
				System.out.println("");
				
				return new Move(init,dest);
			} else {
				throw new IllegalArgumentException("Wrong move format");
			}
		}
		//TODO: rewrite the try-catch clauses
		public void getAndMakeNextMove() {
			Move nextMove;
			//drawBoardCli();
			
			while(true) {
				try {
					nextMove = getNextMove();
					break;
				} catch(IllegalArgumentException wrongMoveFormat) {
					System.out.println("Wrong move format - try again");
				}
			}
			while(!isMoveLegalWithCheck(nextMove)) { //!!!!!!!!!!!!!!!!!!!!!!!!!! change to isMoveLegalCheckless() to get this working again
				//System.out.println(nextMove.toString()); //TESTING
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
		
		Human(Human copyHuman) {super(copyHuman);}
		Human(PieceColour playerColour) {
			super(playerColour);
		}
	}
	//TODO: implement an AI which simply plays random legal moves from the legal move list
	//TODO: consider having a main AI abstract class to subclass specific AIs from
	class RngAi extends Player {
		public Move getNextMove() {
			Set<Move> moveSet = getAllLegalMovesWithCheck();
			Move[] moveArray = (Move[])moveSet.toArray(new Move[moveSet.size()]);
			int randomIndex = new Random().nextInt(moveArray.length);
			return moveArray[randomIndex];
		}
		public void getAndMakeNextMove() {
			movePieceAtLocation(getNextMove());
		}
		
		RngAi(RngAi copyRngAi) {super(copyRngAi);}
		RngAi(PieceColour playerColour) {
			super(playerColour);
		}
	}
	class StaticPlyAi extends Player {
		private int plyDepth;
		
		public Move getNextMove() {
			EvaluationEngine engine = new EvaluationEngine(GameState.this,"alphaBeta",plyDepth,false);
			return engine.alphaBetaPlyMove();
		}
		public void getAndMakeNextMove() {
			movePieceAtLocation(getNextMove());
		}
		StaticPlyAi(StaticPlyAi copyStaticPlyAi) {
			super(copyStaticPlyAi);
			this.plyDepth=copyStaticPlyAi.plyDepth;
		}
		StaticPlyAi(PieceColour playerColour,int plyDepth) {
			super(playerColour);
			this.plyDepth=plyDepth;
		}
	}
	class StaticPlyAiWithThreading extends Player {
		private int plyDepth;
		
		public Move getNextMove() {
			EvaluationEngine engine = new EvaluationEngine(GameState.this,"alphaBeta",plyDepth,true);
			return engine.alphaBetaPlyMove();
		}
		public void getAndMakeNextMove() {
			movePieceAtLocation(getNextMove());
		}
		StaticPlyAiWithThreading(StaticPlyAiWithThreading copyStaticPlyAiWithThreading) {
			super(copyStaticPlyAiWithThreading);
			this.plyDepth=copyStaticPlyAiWithThreading.plyDepth;
		}
		StaticPlyAiWithThreading(PieceColour playerColour,int plyDepth) {
			super(playerColour);
			this.plyDepth=plyDepth;
		}
	}
	class PgnPlayer extends Player { //use this to recover saved games and to run rules-engine tests
		PgnParser gameParser;
		
		public Move getNextMove() {
			if(isMoveLegalWithCheck(gameParser.getMovePlyNumber(numPlies))) {
				return gameParser.getMovePlyNumber(numPlies);
			} else {
				throw new IllegalArgumentException("PGN contains illegal move");
			}
		}
		public void getAndMakeNextMove() {
			movePieceAtLocation(getNextMove());
		}
		PgnPlayer(PgnPlayer copyPgnPlayer) {
			super(copyPgnPlayer);
			this.gameParser = copyPgnPlayer.gameParser;
		}
		PgnPlayer(PieceColour playerColour,String pgnFilename) {
			super(playerColour);
			gameParser = new PgnParser(pgnFilename);
		}
	}
	
	//TODO: format/locate this better
	//static Piece Coord list generation reduce should help reduce performance penalty of board construction
	static Set<Coord> whitePawnRelativeCoords = new HashSet<Coord>();
	static Set<Coord> blackPawnRelativeCoords = new HashSet<Coord>();
	static Set<Coord> kingRelativeCoords      = new HashSet<Coord>();
	static Set<Coord> queenRelativeCoords     = new HashSet<Coord>();
	static Set<Coord> knightRelativeCoords    = new HashSet<Coord>();
	static Set<Coord> bishopRelativeCoords    = new HashSet<Coord>();
	static Set<Coord> rookRelativeCoords      = new HashSet<Coord>();
	static {
		//WHITE PAWN MOVES
		//forward moves
		whitePawnRelativeCoords.add(new Coord(0,1));
		whitePawnRelativeCoords.add(new Coord(0,2));
		//capture moves
		whitePawnRelativeCoords.add(new Coord(1,1));
		whitePawnRelativeCoords.add(new Coord(-1,1));

		//BLACK PAWN MOVES
		//forward moves
		blackPawnRelativeCoords.add(new Coord(0,-1));
		blackPawnRelativeCoords.add(new Coord(0,-2));
		//capture moves
		blackPawnRelativeCoords.add(new Coord(1,-1));
		blackPawnRelativeCoords.add(new Coord(-1,-1));

		//KNIGHT MOVES
		//"vertical" knight moves
		knightRelativeCoords.add(new Coord(1,2));
		knightRelativeCoords.add(new Coord(1,-2));
		knightRelativeCoords.add(new Coord(-1,2));
		knightRelativeCoords.add(new Coord(-1,-2));
		//"horizontal" knight moves
		knightRelativeCoords.add(new Coord(2,1));
		knightRelativeCoords.add(new Coord(2,-1));
		knightRelativeCoords.add(new Coord(-2,1));
		knightRelativeCoords.add(new Coord(-2,-1));

		//BISHOP MOVES
		for(int horVert=1;horVert<=7;horVert++) {
			bishopRelativeCoords.add(new Coord(horVert,horVert));
			bishopRelativeCoords.add(new Coord(-horVert,horVert));
			bishopRelativeCoords.add(new Coord(horVert,-horVert));
			bishopRelativeCoords.add(new Coord(-horVert,-horVert));
		}

		//ROOK MOVES
		for(int horVert=-7;horVert<=7;horVert++) {
			if(horVert!=0) {
				rookRelativeCoords.add(new Coord(horVert,0));
				rookRelativeCoords.add(new Coord(0,horVert));
			}
		}

		//QUEEN MOVES
		//vertical and horizontal moves
		for(int horVert=-7;horVert<=7;horVert++) {
			if(horVert!=0) {
				queenRelativeCoords.add(new Coord(horVert,0));
				queenRelativeCoords.add(new Coord(0,horVert));
			}
		}
		//diagonal moves
		for(int horVert=1;horVert<=7;horVert++) {
			queenRelativeCoords.add(new Coord(horVert,horVert));
			queenRelativeCoords.add(new Coord(-horVert,horVert));
			queenRelativeCoords.add(new Coord(horVert,-horVert));
			queenRelativeCoords.add(new Coord(-horVert,-horVert));
		}
		
		//KING MOVES
		//diagonal moves
		kingRelativeCoords.add(new Coord(1,1));
		kingRelativeCoords.add(new Coord(1,-1));
		kingRelativeCoords.add(new Coord(-1,1));
		kingRelativeCoords.add(new Coord(-1,-1));
		//horizontal and vertical moves
		kingRelativeCoords.add(new Coord(0,1));
		kingRelativeCoords.add(new Coord(0,-1));
		kingRelativeCoords.add(new Coord(1,0));
		kingRelativeCoords.add(new Coord(-1,0));
		//castling
		kingRelativeCoords.add(new Coord(2,0));
		kingRelativeCoords.add(new Coord(-2,0));
	}
	
	abstract public class Piece implements PieceSpecific {
		//REP_CHAR is set by pieces individually according to type and colour
		char REP_CHAR = 'X';
		Coord pieceCoord;
		PieceColour pieceColour;
		boolean collisionChecking;
		Set<Coord> relativeCoords;
		
		public PieceColour getColour() {return this.pieceColour;}
		public Coord getPieceCoord() {return this.pieceCoord;}
		public char getRepChar() {return this.REP_CHAR;}
		
		public Set<Move> getLegalMovesWithCheck() {
			Set<Move> initialMoves = formMovesFromRelativeCoordList();
			Set<Move> finalMoves = new HashSet<>();
			
			for(Move move : initialMoves) {
				if(isMoveLegalWithCheck(move)) { //TESTING
					finalMoves.add(move);
				}
			}
			return finalMoves;
		}
		public Set<Move> getLegalMovesCheckless() {
			Set<Move> initialMoves = formMovesFromRelativeCoordList();
			Set<Move> finalMoves = new HashSet<>();
			
			for(Move move : initialMoves) {
				if(isMoveLegalCheckless(move)) {
					finalMoves.add(move);
				}
			}
			return finalMoves;
		}
		public Set<Move> formMovesFromRelativeCoordList() {
			Set<Move> testableMoveSet = new HashSet<Move>();
			
			if(pieceColour==currentPlayer) {
				try {
					for(Coord relativeCoord : relativeCoords) {
						if(Move.wouldBeLegalMoveObjectFromRelative(this.pieceCoord,relativeCoord)) {
							testableMoveSet.add(new Move(new Coord(this.pieceCoord),Coord.addCoords(this.pieceCoord,relativeCoord)));
						}
					}
				} catch(NullPointerException e) {
					System.out.println(relativeCoords.toString());
					throw new NullPointerException();
				}
			}
			
			return testableMoveSet;
		}
		public boolean moveAgreesWithRelativeCoordList(Move testMove) {
			if(pieceColour==currentPlayer&&Move.wouldBeLegalMoveObject(testMove.getInit(),testMove.getDest())) {
				if(relativeCoords.contains(Move.coordDeltaFromMove(testMove))) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
		public void makeMovePieceCommon(Move move) {
			if(coordContainsPiece(move.getDest())) {
				removePieceAtLocation(move.getDest());
				fiftyMoveRuleCounter = 0;
			} else {
				fiftyMoveRuleCounter++;
			}
			pieceCoord = new Coord(move.getDest());
		}
		//TODO: this should probably call pieceSpecificArbiter, not the other way around
		//TODO: rename "moveLegal" to "arbiter"
		public boolean isMoveLegalCommon(Move moveAttempt) {
			boolean checkHorizontal = false, checkVertical = false;
			int incHorizontal=0,             incVertical=0;
			int horizontalShift=0,           verticalShift=0;
			int xToCheck,                    yToCheck;
			
			//check if the initial coords match the piece's
			if(!moveAttempt.getInit().equals(this.pieceCoord)) {
				return false;
			}
			//cannot move opponent's piece!
			if(pieceColour!=currentPlayer) {
				return false;
			}
			//check if the move is in the potential move list
			if(!moveAgreesWithRelativeCoordList(moveAttempt)) {
				//for(Move tmpMove : formMovesFromRelativeCoordSet()) {
				//	System.out.println(tmpMove.hashCode() + " " + tmpMove.equals(moveAttempt)); //TESTING
				//}
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
						if(moveAttempt.getDest().getX()>moveAttempt.getInit().getX()) {
							incHorizontal=1;
						} else {
							incHorizontal=-1;
						}
						if(moveAttempt.getDest().getY()>moveAttempt.getInit().getY()) {
							incVertical=1;
						} else {
							incVertical=-1;
						}
						for(int i=1;i<Move.absCoordDeltaFromMove(moveAttempt).getX();i++) {
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
					if(moveAttempt.getDest().getX()>moveAttempt.getInit().getX()) {
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
					//moveAttempt.getDest().getY()-moveAttempt.getInit().getY()>0 //TESTING
					if(moveAttempt.getDest().getY()>moveAttempt.getInit().getY()) {
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
			this.relativeCoords = copyPiece.relativeCoords;
		}
		Piece(PieceColour pieceColour, Coord pieceCoord) {
			this.pieceColour = pieceColour;
			this.collisionChecking = true;
			this.pieceCoord = new Coord(pieceCoord);
		}
	}
	
	private interface PieceSpecific {
		//public attemptMovePieceSpecific(Move attemptMove);
		public void makeMovePieceSpecific(Move move);
		//TODO: rename instances of moveAttempt to attemptMove
		public boolean isMoveLegalPieceSpecific(Move moveAttempt);
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
			//check if the move results in pawn promotion... if so, create new piece and remove self
			if((pieceColour==PieceColour.WHITE && move.getDest().getY()==8) || (pieceColour==PieceColour.BLACK && move.getDest().getY()==1)) {
				//produce a new queen at current location and delete self
				chessPieces.add(new Queen(pieceColour,move.getDest()));
				chessPieces.remove(this);
			}
			fiftyMoveRuleCounter = 0;
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
			super.relativeCoords = whitePawnRelativeCoords;
		}
		
		Pawn(PieceColour pieceColour,Coord pieceCoord) {
			super(pieceColour,pieceCoord);
			if(pieceColour==PieceColour.WHITE) {
				super.REP_CHAR = '♙';
				super.relativeCoords = whitePawnRelativeCoords;
			} else if(pieceColour==PieceColour.BLACK) {
				super.REP_CHAR = '♟';
				super.relativeCoords = blackPawnRelativeCoords;
			}
			this.lastMoveWasDouble = false;
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
			super.relativeCoords = rookRelativeCoords;
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
			super.relativeCoords = bishopRelativeCoords;
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
			super.relativeCoords = knightRelativeCoords;
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
		
		public int hashCode() {return super.hashCode();}
		public boolean equals(Object o) {
			if(this == o) {return true;}
			else if(o instanceof Queen) {
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
			super.relativeCoords = queenRelativeCoords;
		}
	}
	public class King extends Piece {
		boolean hasMoved;
		
		public void makeMovePieceSpecific(Move move) {
			makeMovePieceCommon(move);
			this.hasMoved = true;
			//check if you are castling
			if(Move.absCoordDeltaFromMove(move).equals(new Coord(2,0))) {
				//castle with the rook located at the rook's origin which is in the direction of the castle
				if(move.moveIsToDirection("right")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocationWithoutSwitching(new Move(new Coord(8,1),new Coord(6,1)));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocationWithoutSwitching(new Move(new Coord(8,8),new Coord(6,8)));
					}
				} else if (move.moveIsToDirection("left")) {
					if(pieceColour==PieceColour.WHITE) {
						movePieceAtLocationWithoutSwitching(new Move(new Coord(1,1),new Coord(4,1)));
					} else if(pieceColour==PieceColour.BLACK) {
						movePieceAtLocationWithoutSwitching(new Move(new Coord(1,8),new Coord(4,8)));
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
				Coord legalKingsideRookLocation  = new Coord();
				Coord rookLocation               = new Coord();
				
				//get the legal rook castling positions for the piece's colour
				if(pieceColour==PieceColour.WHITE) {
					legalQueensideRookLocation = new Coord(1,1);
					legalKingsideRookLocation  = new Coord(8,1);
				} else if(pieceColour==PieceColour.BLACK) {
					legalQueensideRookLocation = new Coord(1,8);
					legalKingsideRookLocation  = new Coord(8,8);
				}
				//check that the rook being moved towards exists
				if(moveAttempt.moveIsToDirection("right")) {
					rookLocation = legalKingsideRookLocation;
				}
				if(moveAttempt.moveIsToDirection("left")) {
					rookLocation = legalQueensideRookLocation;
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
				//TODO: get castling working: this code could cause trouble
				//if any of the squares between the two are occupied
				if(rookLocation.equals(legalKingsideRookLocation)) {
					if(rowRangeOccupied(moveAttempt.getInit().getX()+1,moveAttempt.getDest().getX()-1,moveAttempt.getInit().getY())) {
						return false;
					}
				} else if(rookLocation.equals(legalQueensideRookLocation)) {
					if(rowRangeOccupied(moveAttempt.getInit().getX()-1,moveAttempt.getDest().getX()+1,moveAttempt.getInit().getY())) {
						return false;
					}
				}
				//if any of the squares between the two (and including those two) are under attack
				if(rowRangeUnderAttackByOpponent(moveAttempt.getInit().getX(),moveAttempt.getDest().getX(),moveAttempt.getInit().getY())) {
					return false;
				}
			}
			return true;
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
			super.relativeCoords = kingRelativeCoords;
		}
	}
	
	GameState(GameState gameCopy) {
		this.currentPlayer        = gameCopy.currentPlayer;
		this.gameOver             = gameCopy.gameOver;
		this.fiftyMoveRuleCounter = gameCopy.fiftyMoveRuleCounter;
		
		//TODO: IMPORTANT - deep copy of pieces
		this.chessPieces = new ArrayList<Piece>();
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
		if(gameCopy.whitePlayer instanceof Human) {this.whitePlayer = new Human((Human)gameCopy.whitePlayer);
		} else if(gameCopy.whitePlayer instanceof RngAi) {this.whitePlayer = new RngAi((RngAi)gameCopy.whitePlayer);}
		if(gameCopy.blackPlayer instanceof Human) {this.whitePlayer = new Human((Human)gameCopy.blackPlayer);
		} else if(gameCopy.blackPlayer instanceof RngAi) {this.whitePlayer = new RngAi((RngAi)gameCopy.blackPlayer);}
	}
	GameState(String debugMode) {
		this();
		chessPieces.clear();
		
		switch(debugMode) {
			case "mateInOne": //black king constrained to a single rank by a rook, white to move with another rook at his disposal (requires 1/2 lookahead)
				chessPieces.add(new King(PieceColour.WHITE,new Coord(5,1)));
				chessPieces.add(new King(PieceColour.BLACK,new Coord(5,8)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(1,7)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,1)));
				break;
			case "mateInTwo": //essentially the "move before" mateInOne, king not yet constrained to his home rank (requires 3/4 lookahead)
				chessPieces.add(new King(PieceColour.WHITE,new Coord(5,1)));
				chessPieces.add(new King(PieceColour.BLACK,new Coord(4,8)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(1,1))); //white rook has not yet constrained black king to home rank
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,1)));
				break;
			case "mateInThree": //mating in the center of a mostly-empty board, three rooks looking on (requires 5/6 lookahead)
				chessPieces.add(new King(PieceColour.WHITE,new Coord(5,1)));
				chessPieces.add(new King(PieceColour.BLACK,new Coord(3,5)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,1)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,2)));
				chessPieces.add(new Rook(PieceColour.WHITE,new Coord(8,3)));
				break;
			default:
				throw new IllegalArgumentException("Invalid board setup debug mode");
		}
	}
	GameState() {
		this.currentPlayer = PieceColour.WHITE;
		this.gameOver = false;
		
		this.chessPieces = new ArrayList<Piece>();
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
