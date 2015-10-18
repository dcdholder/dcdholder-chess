package dcdholder.chess;
import java.util.*;

public class PgnParserTest {
	private static String TEST_BANK_PARSER = "../resources/pgn/pgnParser";
	private static String TEST_BANK_VALID = "../resources/pgn/gg";
	
	//first confirm that isValidPgnFile() succeeds or fails, then read specific exceptions
	
	//test tag parsing
	public final void test_validTags() { //take valid game file with tags and not much else
		String testPgn = "test_validTags.pgn";
		assertTrue(PgnParser.isValidPgnFileShallow(testPgn));
		try {int x;} catch(Exception e) {int y;}
	} 
	public final void test_unclosedTags() { //take valid game file, neglect to close a square bracket
		String testPgn = "test_unclosedTags.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public final void test_wrongOrderTags() { //place tags halfway through an otherwise valid file
		String testPgn = "test_wrongOrderTags.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public final void test_nestedCurlyBracesTags() { //add an extra curly brace
		String testPgn = "test_nestedCurlyBracesTags.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}

	//test comment parsing
	public final void test_validComments() { //short file, two arbitrarily-placed valid comments
		String testPgn = "test_validComments.pgn";
		assertTrue(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public final void test_beforeTagsComments() { //comments placed among tags instead of where they should be
		String testPgn = "test_beforeTagsComments.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public final void test_unclosedComments() { //open a comment in a valid location, but neglect to close it
		String testPgn = "test_unclosedComments.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	
	//test SAN parsing
	public void test_validSan() { //read multiple valid files with no (or minimal) metadata 
		String testPgn = "test_validSan.pgn";
		assertTrue(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public void test_noSan() { //tags exist, but no SANs
		String testPgn = "test_noSan.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public void test_missingMoveNumberSan() { //skip a move number
		String testPgn = "test_missingMoveNumberSan.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public void test_wrongMoveNumberOrderSan() { //get move numbers in the wrong order
		String testPgn = "test_wrongMoveNumberOrderSan.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public void test_coordinatesNewlineSplitSan() { //detect a move split in two with newlines
		String testPgn = "test_coordinatesNewlineSplitSan.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}
	public void test_invalidContentsSan() { //detect SANs which cannot be resolved into move objects - invalid characters, etc.
		String testPgn = "test_invalidContentsSan.pgn";
		assertFalse(PgnParser.isValidPgnFileShallow(testPgn));
	}

	public void test_readWriteGeneratesSameMoveObjects() {
		//read from file
		//hold onto move object contents of file
		//write to file (disregard comments and tags)
		//read from new file
		//hold onto move object contents of new file
		//compare to the original file's contents (order and contents must match)
		//repeat this for a list of different games (use shared "good games" bank)
	}
	
	public class MovewordTest {
		//movewords stored in text files, number of valid and invalid lines in file are counted and mapped to truth values
		private static String TEST_BANK_MOVEWORD = "../resources/txt/movewordLists";
		
		//returns either "PASS" or the first failing line in the test file
		static String movewordValidFile(String fileName,boolean expectValid) {
			String failureString = "PASS";
			
			for(movewordLine : moveWordLines) {
				if(!movewordValid(movewordLine)&&expectValid || movewordValid(movewordLine)&&!expectValid) {
					failureString = movewordLine;
					break;
				}
			}
			
			return failureString;
		}
		
		//positive tests
		public void test_pawnMoveword() {} //valid pawn movewords
		public void test_knightMoveword() {} //valid knight movewords
		public void test_bishopMoveword() {} //valid bishop movewords
		public void test_rookMoveword() {} //valid rook movewords
		public void test_queenMoveword() {} //valid queen movewords
		public void test_kingMoveword() {} //valid king movewords
		public void test_complexMoveword() {} //combined pawn capture/promotion which results in a checkmate, etc.
		public void test_initialRankMoveword() {} //movewords with specified initial rank
		public void test_initialFileMoveword() {} //movewords with specified initial file
		public void test_initialRankAndFileMoveword() {} //movewords where both are specified
		public void test_captureOnlyMoveword() {} //movewords unusual only in that they are captures
		public void test_promotionOnlyMoveword() {} //movewords unusual only in that they are promotions 
		public void test_modifierOnlyMoveword() {} //movewords unusual only in that they have modifiers (check/checkmate)
		
		//negative tests
		public void test_invalidTypeMoveword() {assertFalse(movewordValidityFile("invalidTypeMoveword.txt"));} //use an incorrect symbol for the piece type, etc.
		public void test_invalidMoveMoveword() {assertFalse(movewordValidityFile("invalidMoveMoveword.txt"));} //use invalid coordinates
		public void test_invalidPromotionMoveword() {assertFalse(movewordValidityFile("invalidPromotionMoveword.txt"));} //different valid promotions
		public void test_invalidModifierMoveword() { //use weird symbols, etc.
			assertFalse(movewordValidityFile("invalidModifierMoveword.txt"),);
		}
	}
}
