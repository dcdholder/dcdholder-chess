package dcdholder.chess;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import dcdholder.chess.GameState.Bishop;
import dcdholder.chess.GameState.King;
import dcdholder.chess.GameState.Knight;
import dcdholder.chess.GameState.Pawn;
import dcdholder.chess.GameState.Piece;
import dcdholder.chess.GameState.PieceColour;
import dcdholder.chess.GameState.Queen;
import dcdholder.chess.GameState.Rook;

//TODO: get the threading implementation all worked out
public class EvaluationEngine implements Callable<Map<Integer,Move>> {
	boolean    threadingEnabled=false;
	static int MAX_THREADS=4;
	
	String engineType;
	int maxDepth;
	GameState gameCopy;
	
	int seedDepth;
	int seedMax;
	int seedMin;
	
	public Map<Integer,Move> call() {
		if(engineType=="alphaBeta")    {return alphaBetaPly(seedDepth,maxDepth,seedMin,seedMax,gameCopy.getOpponentColour());}
		//else if(engineType=="miniMax") {return;}
		else {throw new IllegalArgumentException("Invalid engine type");}
	}
	public Move alphaBetaPlyMove() {
		Move bestMove = new Move();
		Map<Integer,Move> scoreMoveMap = alphaBetaPly(0,this.maxDepth,Integer.MAX_VALUE,Integer.MIN_VALUE,gameCopy.currentPlayer);
		for(Move tmpMove : scoreMoveMap.values()) {bestMove = tmpMove;}
		return bestMove;
	}
	public Move bruteForcePlyMove() {
		Move bestMove = new Move();
		Map<Integer,Move> scoreMoveMap = bruteForcePly(1,this.maxDepth);
		for(Move tmpMove : scoreMoveMap.values()) {bestMove = tmpMove;}
		return bestMove;
	}
	public Map<Integer,Move> alphaBetaPly(int currentDepth,int maxDepth,int minimumGuaranteed,int maximumGuaranteed,PieceColour initialPlayer) {
		Map<Integer,Move> moveScorePair = new HashMap<Integer,Move>();
		EvaluationEngine simEngine;
		
		if(currentDepth==maxDepth) {
			moveScorePair.put(evalBoard(initialPlayer,currentDepth,false),new Move()); //use dummy value for move
			return moveScorePair;
		} else {
			Set<Move> allLegalMovesSet = gameCopy.getAllLegalMovesWithCheck();
			Move[] allLegalMoves = allLegalMovesSet.toArray(new Move[allLegalMovesSet.size()]); //randomizing move order to make openings more interesting
			
			//if(currentDepth==0) System.out.println(allLegalMoves.length); //TESTING
			//int branchesExamined = 0;
			if(branchIsDead(allLegalMovesSet)) {
				moveScorePair.put(this.evalBoard(initialPlayer,currentDepth+1,true),new Move()); //use dummy value for move
				return moveScorePair;
			}
			if(gameCopy.currentPlayer!=initialPlayer) {
				int score = 0;
				int smallestScoreInBatch=Integer.MAX_VALUE;
				Move smallestScoreInBatchMove = new Move();
				for(Move evalMove: allLegalMoves) {
					simEngine = new EvaluationEngine(this);
					simEngine.gameCopy.movePieceAtLocation(evalMove);
					//search for dead games here
					Map<Integer,Move> scoreMoveMap = simEngine.alphaBetaPly(currentDepth+1,maxDepth,minimumGuaranteed,maximumGuaranteed,initialPlayer);
					for(Integer tmpScore : scoreMoveMap.keySet()) {score = tmpScore;}
					//System.out.println(scoreMoveMap.size());
					
					//branchesExamined++; //
					if(score<=smallestScoreInBatch) {
						smallestScoreInBatch=score;
						smallestScoreInBatchMove=evalMove;
					}
					if(smallestScoreInBatch<=minimumGuaranteed) {
						minimumGuaranteed=smallestScoreInBatch;
					}
					if(minimumGuaranteed<=maximumGuaranteed) {
						break;
					}
				}
				//System.out.println(branchesExamined + "/" + numPossMoves);
				moveScorePair.put(minimumGuaranteed,smallestScoreInBatchMove);
				return moveScorePair;
			} else if(gameCopy.currentPlayer==initialPlayer) {
				int score = 0;
				int largestScoreInBatch=Integer.MIN_VALUE;
				Move largestScoreInBatchMove = new Move();
				
				if(threadingEnabled && currentDepth==0) {
					//evaluate a batch of moves at a time, rather than just one
					//compare game speed with same move sequence and core count, also without multithreading
					int moveIndex=0;
					
					while(moveIndex<allLegalMoves.length) { //first create groups of moves to be evaluated simultaneously
						Move[] moveGroup;
						Thread[] evaluationThreadGroup;
						FutureTask<Map<Integer,Move>>[] evaluationTaskGroup;
						Map<Integer,Move>[] finalResult;
						
						if(moveIndex+MAX_THREADS<=allLegalMoves.length) {
							moveGroup = new Move[MAX_THREADS];
							evaluationThreadGroup = new Thread[MAX_THREADS];
							for(int i=0;i<MAX_THREADS;i++) {
								moveGroup[i] = allLegalMoves[moveIndex];
							}
							moveIndex+=MAX_THREADS;
						} else {
							moveGroup = new Move[allLegalMoves.length-moveIndex];
							evaluationThreadGroup = new Thread[allLegalMoves.length-moveIndex];
							for(int i=0;i<allLegalMoves.length-moveIndex;i++) {
								moveGroup[i] = allLegalMoves[moveIndex];
							}
							moveIndex=allLegalMoves.length; //signifies that this is the last move group
						}
						
						//System.out.println(moveGroup.length); //TESTING
						evaluationThreadGroup = new Thread[moveGroup.length];
						evaluationTaskGroup = new FutureTask[moveGroup.length];
						finalResult = (Map<Integer, Move>[]) new HashMap[moveGroup.length];
						
						for(int i=0;i<moveGroup.length;i++) { //generate the threads, initiate evaluation
							simEngine = new EvaluationEngine(this,1,minimumGuaranteed,maximumGuaranteed);
							simEngine.gameCopy.movePieceAtLocation(moveGroup[i]);
							evaluationTaskGroup[i] = new FutureTask<Map<Integer,Move>>(simEngine);
							evaluationThreadGroup[i] = new Thread(evaluationTaskGroup[i]);
							evaluationThreadGroup[i].start();
						}
						for(int i=0;i<moveGroup.length;i++) { //collect the results from the evaluation and put them to use
							try {
								finalResult[i] = evaluationTaskGroup[i].get();
							} catch (InterruptedException e) {  
								e.printStackTrace();  
							} catch (ExecutionException e) {  
						    	e.printStackTrace();  
							}
							for(Integer tmpScore : finalResult[i].keySet()) {score = tmpScore;}
							//System.out.println(score); //TESTING
							if(score>largestScoreInBatch) {
								largestScoreInBatchMove=moveGroup[i];
							}
							if(score>=largestScoreInBatch) {
								largestScoreInBatch=score;
							}
							if(largestScoreInBatch>=maximumGuaranteed) {
								maximumGuaranteed=largestScoreInBatch;
							}
							//because we are at ply depth 0, we have to evaluate every move anyway and there's no need for breaking
						}
					}
				} else {
					for(Move evalMove: allLegalMoves) {
						simEngine = new EvaluationEngine(this);
						simEngine.gameCopy.movePieceAtLocation(evalMove);
						//search for dead games here
						Map<Integer,Move> scoreMoveMap = simEngine.alphaBetaPly(currentDepth+1,maxDepth,minimumGuaranteed,maximumGuaranteed,initialPlayer);
						for(Integer tmpScore : scoreMoveMap.keySet()) {score = tmpScore;}
						//scoreMoveMap.size();
						//System.out.println(score);
						
						//branchesExamined++; //
						if(score>largestScoreInBatch) {
							largestScoreInBatchMove=evalMove;
						}
						if(score>=largestScoreInBatch) {
							largestScoreInBatch=score;
						}
						if(largestScoreInBatch>=maximumGuaranteed) {
							maximumGuaranteed=largestScoreInBatch;
						}
						//System.out.println(score);
						//System.out.println(maximumGuaranteed);
						if(minimumGuaranteed<=maximumGuaranteed) {
							break;
						}
					}
					//System.out.println(branchesExamined + "/" + numPossMoves);
				}
				moveScorePair.put(maximumGuaranteed,largestScoreInBatchMove);
				return moveScorePair;
			}
		}
		return moveScorePair;
	}
	public Map<Integer,Move> bruteForcePly(int currentDepth,int maxDepth) {
		Map<Integer,Move> moveScorePair = new HashMap<Integer,Move>();
		Set<Move> allLegalMoves         = gameCopy.getAllLegalMovesWithCheck();
		int bestScore = -1000000, worstScore = 1000000, boardScore = 0;
		Move bestMove = new Move(), worstMove = new Move();
		Move arbitraryMove = bestMove;
		PieceColour initialColour;
		EvaluationEngine simEngine;
		
		if(currentDepth%2==1) {initialColour=gameCopy.currentPlayer;
		} else                {initialColour=gameCopy.getOpponentColour();}
		
		if(branchIsDead(allLegalMoves)) {
			moveScorePair.put(this.evalBoard(initialColour,currentDepth,true),arbitraryMove); //it doesn't matter which move we return in this case
			return moveScorePair;
		}
		
		if(currentDepth==maxDepth) {
			for(Move legalMove : allLegalMoves) {
				simEngine = new EvaluationEngine(this);
				simEngine.gameCopy.movePieceAtLocation(legalMove);
				boardScore = simEngine.evalBoard(initialColour,currentDepth,false);
				
				if(boardScore<worstScore) {
					worstScore = boardScore;
					worstMove  = legalMove;
				}
				if(boardScore>bestScore) {
					bestScore  = boardScore;
					bestMove   = legalMove;
				}
			}
		} else if(currentDepth!=maxDepth) {
			for(Move legalMove : allLegalMoves) {
				Map<Integer,Move> tmpMovePair = new HashMap<>();
				
				simEngine = new EvaluationEngine(this);
				simEngine.gameCopy.movePieceAtLocation(legalMove);
				tmpMovePair = simEngine.bruteForcePly(currentDepth+1,maxDepth);
				
				for(Integer tmpScore : tmpMovePair.keySet()) {boardScore = tmpScore;} //kind of a kludgy way of doing it...
				
				if(boardScore<worstScore) {
					worstScore = boardScore;
					worstMove = legalMove;
				}
				if(boardScore>bestScore) {
					bestScore  = boardScore;
					bestMove = legalMove;
				}
			}
		}
		if(currentDepth%2==0) {
			moveScorePair.put(worstScore,worstMove);
		} else if(currentDepth%2==1) {
			moveScorePair.put(bestScore,bestMove);
		}
		return moveScorePair;
	}
	public boolean branchIsDead(Set<Move> moveSet) {
		return moveSet.size()==0 || gameCopy.fiftyMoveRuleCounter==GameState.FIFTY_MOVE_RULE_MAX;
	}
	public int evalBoard(PieceColour checkColour, int plyDepth, boolean deadBranch) {
		//just use standard values for now
		final int PAWN_SCORE   = 100;
		final int KNIGHT_SCORE = 300;
		final int BISHOP_SCORE = 300;
		final int ROOK_SCORE   = 500;
		final int QUEEN_SCORE  = 900;
		final int KING_SCORE   = 100000;
		//final int FUZZ_CENTIPAWNS = 4;
		
		int MATE_SPEED_BONUS = KING_SCORE-(KING_SCORE/10)*plyDepth;
		//int FUZZ_FACTOR      = ThreadLocalRandom.current().nextInt(FUZZ_CENTIPAWNS); //do I need to double up on randomization?
		int absScore;
		int total = 0;
		
		for(Piece evalPiece : gameCopy.chessPieces) {
			if(evalPiece instanceof Pawn) {          absScore = PAWN_SCORE;
			} else if(evalPiece instanceof Knight) { absScore = KNIGHT_SCORE;
			} else if(evalPiece instanceof Bishop) { absScore = BISHOP_SCORE;
			} else if(evalPiece instanceof Rook) {   absScore = ROOK_SCORE;
			} else if(evalPiece instanceof Queen) {  absScore = QUEEN_SCORE;
			} else {                                 absScore = 0;
			}
			if(evalPiece.getColour()==checkColour) {
				total+=absScore;
			} else {
				total-=absScore;
			}
		}
		int tmpScore = 0;
		for(Piece testPiece : gameCopy.chessPieces) {
			if(testPiece instanceof King) {
				tmpScore = testPiece.getLegalMovesWithCheck().size()*PAWN_SCORE/10;
				if(testPiece.pieceColour!=checkColour) {
					total-=tmpScore;
				}
			}
		}
		//TODO: AI should be able to evaluate potential checkmates
		//only checks if the INITIAL player is in check...
		boolean initialIsInCheck,opponentIsInCheck;
		if(checkColour==gameCopy.currentPlayer) { //a little confusing...
			initialIsInCheck  = gameCopy.currentIsInCheck();
			opponentIsInCheck = gameCopy.opponentIsInCheck();
		} else                         {
			initialIsInCheck  = gameCopy.opponentIsInCheck();
			opponentIsInCheck = gameCopy.currentIsInCheck();
		}
		//if(deadBranch) System.out.println(deadBranch);
		if(initialIsInCheck) {
			total-=PAWN_SCORE / 2; //"willing to trade" half a pawn to avoid getting into check
		} else if(opponentIsInCheck) {
			total+=PAWN_SCORE / 2;
		}
		if(deadBranch && initialIsInCheck) {
			total-=KING_SCORE;
		} else if(deadBranch && opponentIsInCheck) {
			total+=KING_SCORE;
			total+=MATE_SPEED_BONUS;
		}
		if(deadBranch && !initialIsInCheck && !opponentIsInCheck) {
			total=-total-KNIGHT_SCORE; //return value depends on likelihood of a win - intended to discourage stalemates
		}
		return total;
	}
	
	EvaluationEngine(EvaluationEngine evaluationEngineCopy,int seedDepth,int seedMin,int seedMax) { //used for threading
		this(evaluationEngineCopy);
		
		this.seedDepth=seedDepth;
		this.seedMin=seedMin;
		this.seedMax=seedMax;
	}
	EvaluationEngine(EvaluationEngine evaluationEngineCopy) {
		this.gameCopy=new GameState(evaluationEngineCopy.gameCopy);
		this.engineType=evaluationEngineCopy.engineType;
		this.maxDepth=evaluationEngineCopy.maxDepth;
		this.threadingEnabled=evaluationEngineCopy.threadingEnabled;
	}
	EvaluationEngine(GameState gameCopy,String engineType,int maxDepth,boolean threadingEnabled) {
		this.gameCopy=new GameState(gameCopy); //create a deep copy of the current game
		this.engineType=engineType;
		this.maxDepth=maxDepth;
		this.threadingEnabled=threadingEnabled;
	}
}
