package dcdholder.chess;
import java.util.*;
import java.util.regex.*;

//TODO: we have the definitions, now we need the implementations (ez)
//TODO: always try to throw an exception for the FIRST error you encounter in the file
//TODO: create tons of pgn files to go with the tests
//TODO: run this against every game in a massive database
public class PgnParser {
	//private static TEST_BANK_PARSER = "../resources/pgn/pgnParserTests";
	//private static TEST_BANK_VALID = "../resources/pgn/valid";

	//TODO: figure out how to properly escape these regex strings
	//private static String TAG_PATTERN = "\[([0-9]+\.[^\]]*)\]";
	//private static String TAG_BLOCK_PATTERN = "\[([^\]]*)\]";
	//private static String COMMENT_PATTERN = "[0-9]+\.[^\{]+\{([^\}]*)\}";
	//private static String COMMENT_BLOCK_PATTERN = "[0-9]+\.[^\{]+(\{[^\}]*\})";
	//private static String SAN_PATTERN = "[0-9]+\.[\t ]+([^\t\n ]+)[\t ]+([^\t\n ]+)[\t\n ]";
	
	//private static String STANDARD_MOVEWORD_PATTERN = "(([QKRBN]?)([a-g]?)([1-8]?)([x]?)([a-g]?)([1-8]?))";

	//TODO: consider moving the SAN-Move translation code throughout class to the Move object for improved class portability, or create an intermediate object
	public static List<Move> getMoveObjectsFile(String fileName) {
		return getMoveObjectsString(loadPgnFileToString(fileName));
	}
	public static Map<Integer,String> getMoveSanFile(String fileName) {
		return getMoveSanString(loadPgnFileToString(fileName));
	}
	public static Map<Integer,String> getCommentMapFile(String fileName) {
		return getCommentMapString(loadPgnFileToString(fileName));
	}
	public static Map<String,String> getTagMapFile(String fileName) {
		return getTagMapString(loadPgnFileToString(fileName));
	}
	public static boolean isValidPgnFileShallow(String fileName) {
		return isValidPgnStringShallow(loadPgnFileToString(fileName));
	}
	public static boolean isValidPgnFileDeep(String fileName) {
		return isValidPgnStringDeep(loadPgnFileToString(fileName));
	}
	public static Move convertSanStringToMoveObject(String sanString) {
		return new Move();
	}
	//remove tags and comments from file
	public static void stripMetadataFile(String fileName, String newFileName) {
		loadStringToPgnFile(stripMetadataString(loadPgnFileToString(fileName)),newFileName);
	}	
	
	private static List<Move> getMoveObjectsString(String parseString) {
		List<Move> pgnMoveObjects = new ArrayList<Move>();
		String nakedString = stripMetadataString(parseString);
		
		return pgnMoveObjects;
	}
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
	//does not require a full-blown chess engine to play through the game as it is read
	private static boolean isValidPgnStringShallow(String parseString) {
		//gradually strip all of the file components from the file
		//if there's nothing left, and everything you've removed is valid, then the file is valid
		//ensure that no exceptions would be thrown were we to attempt to collect the data fields
		try {
			getMoveSanString(parseString);
			getCommentMapString(parseString);
			getTagMapString(parseString);
		} catch(Exception e) {
			return false;
		}
		parseString=stripMetadataString(parseString);
		parseString=stripSanString(parseString);
		//TODO: check that the string now contains only whitespace
		if(nothingButWhitespace) {
			return true;
		} else {
			return false;
		}
	}
	//requires a full-blown chess rules engine to play through the game as it is read
	private static boolean isValidPgnStringDeep(String parseString) {
		if(!isValidPgnStringShallow(parseString)) {return false;}
		
		return false;
	}
	//creates a string with only action data
	private static String stripMetadataString(String parseString) {
		return "";
	}
	private static String stripSanString(String parseString) {
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

	public static void writePgnFileFromMoveObjects(String fileName, List<Move> moveObjects) {
	}
	
	//used for deep pgn validation with a full rules engine
	//TODO: write a method to return a map of these
	public class Moveword {
		//TODO: figure out a way to make this static...
		private Map<String,String> PIECE_CHAR_TO_TYPE_STRING = new HashMap<String,String>();
		
		private static String STANDARD_MOVEWORD_PATTERN = "((?<pieceType>[QKRBN]?)(?<initFile>[a-g]?)(?<initRank>[1-8]?)(?<isCaptureMove>[x]?)(?<destFile>[a-g])(?<destRank>[1-8])(?<isPawnPromotion>=?)(?<pieceTypePromotion>[QRBNP]?)(?<modifiers>[#+]?)";
		private static String CASTLING_MOVEWORD_PATTERN = "(O-O)";
		
		String initFile="",initRank="";
		String destFile="",destRank="";
		
		String pieceType="";
		String pieceTypePromotion="";
		String playerColour="";
		
		int moveNumber=0; //tag used in the pgn file for parent move pair
		
		boolean initFileSpecified=false,initRankSpecified=false;
		
		boolean isCaptureMove=false;
		boolean isPawnPromotion=false;
		boolean isCheckMove=false;
		boolean isCheckmateMove=false;
		boolean isKingsideCastle=false;
		boolean isQueensideCastle=false;
		
		private void initializePieceCharToTypeStringMap() {
			this.PIECE_CHAR_TO_TYPE_STRING.put("P","pawn");
			this.PIECE_CHAR_TO_TYPE_STRING.put("N","knight");
			this.PIECE_CHAR_TO_TYPE_STRING.put("B","bishop");
			this.PIECE_CHAR_TO_TYPE_STRING.put("R","rook");
			this.PIECE_CHAR_TO_TYPE_STRING.put("Q","queen");
			this.PIECE_CHAR_TO_TYPE_STRING.put("K","king");
		}
		
		Moveword(int moveNumber, String playerColour, String moveWordString) {
			this.initializePieceCharToTypeStringMap();
			
			Pattern castlingPattern = Pattern.compile(CASTLING_MOVEWORD_PATTERN);
			Pattern standardPattern = Pattern.compile(STANDARD_MOVEWORD_PATTERN);
			
			Matcher castlingMatch = castlingPattern.matcher(moveWordString);
			Matcher standardMatch = standardPattern.matcher(moveWordString);
			
			//TODO: consider adding the code path handled by the exception to the regex
			if(castlingMatch.group(0)!="") {
				if(castlingMatch.group(0)=="O-O") {
					this.isKingsideCastle=true;
				} else if(castlingMatch.group(0)=="O-O-O") {
					this.isQueensideCastle=true;
				} else {
					throw new IllegalArgumentException("Castling moveword " + castlingMatch.group(0) + " did not match correct castling format (O-O or O-O-O)");
				}
			} else if(standardMatch.group(0)!="") {
				if(standardMatch.group("pieceType")!="") {
					this.pieceType = this.PIECE_CHAR_TO_TYPE_STRING.get(standardMatch.group("pieceType"));
				} else {
					this.pieceType="pawn";
				}
				if(standardMatch.group("initFile")!="") {this.initFile=standardMatch.group("initFile");}
				if(standardMatch.group("initRank")!="") {this.initRank=standardMatch.group("initRank");}
				if(standardMatch.group("isCaptureMove")!="") {this.isCaptureMove=true;}
				if(standardMatch.group("destFile")!="") {
					this.destFile = standardMatch.group("destFile");
				} else {
					throw new IllegalArgumentException("Mandatory pgn argument missing (destination file): " + moveWordString);
				}
				if(standardMatch.group("destRank")!="") {
					this.destRank = standardMatch.group("destRank");
				} else {
					throw new IllegalArgumentException("Mandatory pgn argument missing (destination rank): " + moveWordString);
				}
				if(standardMatch.group("isPawnPromotion")!="") {this.isPawnPromotion=true;}
				if(standardMatch.group("pieceTypePromotion")!="") {this.pieceTypePromotion=this.PIECE_CHAR_TO_TYPE_STRING.get(standardMatch.group("pieceTypePromotion"));}
				if(standardMatch.group("modifiers")!="") {
					if(standardMatch.group("modifiers")=="+") {
						isCheckMove = true;
					} else if(standardMatch.group("modifiers")=="#") {
						isCheckmateMove = true;
					}
				}
			} else {
				throw new IllegalArgumentException("Moveword " + moveWordString + " does not match initial filter for standard/castling format");
			}
		}
	}
}
