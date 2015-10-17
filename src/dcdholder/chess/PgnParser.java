package dcdholder.chess;
import java.util.*;

//TODO: we have the definitions, now we need the implementations (ez)
//TODO: always try to throw an exception for the FIRST error you encounter in the file
//TODO: create tons of pgn files to go with the tests
public class PgnParser {
	//private static TEST_BANK_PARSER = "../resources/pgn/pgnParserTests";
	//private static TEST_BANK_VALID = "../resources/pgn/valid";

	//TODO: figure out how to properly escape these regex strings
	//private static String TAG_PATTERN = "\[([0-9]+\.[^\]]*)\]";
	//private static String TAG_BLOCK_PATTERN = "\[([^\]]*)\]";
	//private static String COMMENT_PATTERN = "[0-9]+\.[^\{]+\{([^\}]*)\}";
	//private static String COMMENT_BLOCK_PATTERN = "[0-9]+\.[^\{]+(\{[^\}]*\})";
	//private static String SAN_PATTERN = "[0-9]+\.[\t ]+([^\t\n ]+)[\t ]+([^\t\n ]+)[\t\n ]"

	//TODO: consider moving the SAN-Move translation code throughout class to the Move object for improved class portability, or create an intermediate object
	//public static List<Move> getMoveObjectsFile(String fileName) {
	//	return getMoveObjectsString(loadPgnFileToString(fileName));
	//}
	public static Map<Integer,String> getMoveSanFile(String fileName) {
		return getMoveSanString(loadPgnFileToString(fileName));
	}
	public static Map<Integer,String> getCommentMapFile(String fileName) {
		return getCommentMapString(loadPgnFileToString(fileName));
	}
	public static Map<String,String> getTagMapFile(String fileName) {
		return getTagMapString(loadPgnFileToString(fileName));
	}
	public static boolean isValidPgnFile(String fileName) {
		return false;
	}
	//public static Move convertSanStringToMoveObject(String sanString) {
	//}
	//remove tags and comments from file
	public void stripMetadataFile(String fileName, String newFileName) {
		loadStringToPgnFile(stripMetadataString(loadPgnFileToString(fileName)),newFileName);
	}	
	
	//private static List<Move> getMoveObjectsString(String parseString) {
	//	List<Move> pgnMoveObjects = new ArrayList<Move>();
	//	nakedString = stripMetadataString(parseString);
	//}
	private static Map<Integer,String> getMoveSanString(String parseString) {
		Map<Integer,String> pgnMoveSans = new HashMap<Integer,String>(); //map move number to SAN string - 'SAN'=='Standard Algebraic Notation'
		String nakedString = stripMetadataString(parseString);
		
		return pgnMoveSans;
	}
	private static Map<Integer,String> getCommentMapString(String parseString) {
		Map<Integer,String> pgnMoveComments = new HashMap<Integer,String>(); //map move number to move comment
		
		return pgnMoveComments;
	}
	private static Map<String,String> getTagMapString(String parseString) {
		Map<String,String> pgnTags = new HashMap<String,String>(); //map tag id to tag value (could be useful for storing test data)
		
		return pgnTags;
	}
	private static boolean isValidPgnString(String fileName) {
		//gradually remove all of the components from the file
		//if there's nothing left, and everything you've removed is valid, then the file is valid
		return false;
	}
	private static String stripMetadataString(String parseString) {
		return "";
	}
	private static String loadPgnFileToString(String fileName) {
		//load pgn file to a string		
		//add a space character at the end to make parsing easier (should be harmless)
		return "";
	}
	private static String loadStringToPgnFile(String parseString, String newFileName) {
		return "";
	}

	//public writePgnFileFromMoveObjects(String fileName, List<Move> moveObjects) {
	//}

	//these will all use variations of the same file
	//simply determine whether loadPgnFileToString works as intended
	//first confirm that isValidPgnFileString succeeds or fails, then read specific exceptions
	public void validTags() {} //take valid game file with tags and not much else
	public void unclosedTags() {} //take valid game file, neglect to close a square bracket
	public void wrongOrderTags() {} //place tags halfway through an otherwise valid file
	public void nestedCurlyBracesTags() {} //add an extra curly brace 	

	public void validComments() {} //short file, two arbitrarily-placed valid comments
	public void beforeTagsComments() {} //comments placed among tags instead of where they should be
	public void unclosedComments() {} //open a comment in a valid location, but neglect to close it
	
	public void validSan() {} //read multiple valid files with no (or minimal) metadata
	public void noSan() {} //tags exist, but no SANs
	public void missingMoveNumberSan() {} //skip a move number
	public void wrongMoveNumberOrderSan() {} //get move numbers in the wrong order
	public void coordinatesNewlineSplitSan() {} //detect a move split in two with newlines
	public void invalidContentsSan() {} //detect SANs which cannot be resolved into move objects

	public void readWriteGeneratesSameMoveObjects() {
		//read from file
		//hold onto move object contents of file
		//write to file (disregard comments and tags)
		//read from new file
		//hold onto move object contents of new file
		//compare to the original file's contents (order and contents must match)
		//repeat this for a list of different games (use shared "good games" bank)
	}
}
