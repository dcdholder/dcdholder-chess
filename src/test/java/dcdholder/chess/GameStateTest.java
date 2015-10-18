package dcdholder.chess;
import java.util.*;

//pgnReaderWithFailComment() is the same as pgnReader, except it keeps going if a regex detects FAIL in the failing line's comment
public class GameStateTest {
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
	
	//each test needs to cover positive and negative cases
	//starting from a fresh board, get into position to test as quickly as possible
	//add a "board snapshot" feature to store test results in a file
	//load all of these tests from files; there should be a moveSequence test directory
	//piece function testing might take something like 200 moves, which would cost about 25kB to store the boards
	//consider storing multiple boards on the same line... otherwise it'll be something like 1600 lines
	//if I stick to the 80 character limit, that gives me either 8 or 9 boards per line...
	public void test_Pawn_normalMove() {
		//positive: march pawns on both teams up to the center of the board (32 turns)
		//negative: along the way, query each pawn's movement to every position on the board, except for forward
	}
	public void test_Pawn_doubleMove() {
		//positive: on every pawn on the board, perform a double move from the starting position (16 turns)
		//negative: immediately afterwards, attempt to perform a double move, then settle for a single move (16 turns)
	}
	public void test_Pawn_captureMove() {
		//positive: get into position as quickly as possible, use a pawn to capture a piece from each side (6 turns)
		//negative: after killing pawns, attempt to make killing moves on empty squares (and fail)
	}
	public void test_Pawn_enPassantMove() {
		//positive: get into position (on each side), capture a piece (5+ turns)
		//negative: wait an extra move after a double move to attempt another en passant on each side (and fail)
	}
	public void test_Pawn_promotion() {
		//positive: move pieces on both sides out of the way so that pawns can traverse the board, confirm that a queen is created (probably 10-12 moves)
		//negative: make sure that the pawn doesn't get promoted as it moves
	}
	public void test_Knight_normalMove() {
		//positive: move all 4 knights in the 8 different possible ways - no need to move anything else, confirm that they can capture (32 moves)
		//negative: confirm that every other move is not possible
	}
	public void test_Bishop_normalMove() {
		//positive: move bishops to the middle of the board, confirm that they can capture (like 8 moves)
		//negative: make sure that the possible move list is as expected
	}
	public void test_Rook_normalMove() {
		//positive: move rooks around middle of board, start capturing pieces (like 8 moves)
		//negative: make sure that the possible move list is as expected
	}
	public void test_Queen_normalMove() {
		//positive: move queens around middle of board, start capturing pieces (like 16 moves)
		//negative: make sure that the possible move list is as expected
	}
	public void test_King_normalMove() {
		//positive: move kings around middle of board, start capturing pieces (be wary of checks, stalemates, checkmates) (like 8 moves)
		//negative: make sure that the possible move list is as expected
	}
	public void test_King_castling() {
		//positive: move pieces out of the way, then castle (like 8 moves)
		//negative: move rooks around, then back into position before attempting to castle (8 moves)
		//reset the board, move the king again, then try again (8 moves)
		//then try to do it with pieces in the way
	}
	public void test_fastCheck() {
		
	}
	public void test_fastStalemate() {
		
	}
	public void test_fastCheckmate() {
		
	}
	public void test_fast50MoveRule() {
		
	}
	public void test_fastThreefoldRepetition() {
		
	}
	public void test_attemptMovingInCheck() {
		
	}
}