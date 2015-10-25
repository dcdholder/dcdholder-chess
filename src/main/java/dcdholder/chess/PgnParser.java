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
	String                pgnString;
	Map<Integer,Plyword>  plywords;    //mapped to ply numbers
	Map<Integer,String>   sanStrings;  //mapped to move numbers
	Map<Integer,String>   allTags;     //mapped to 
	Map<Integer,String>   allComments; //mapped according to order within the file
	Map<Integer,String>   plyComments; //mapped according to the ply on which the action occurs, whenever this is obvious
	String                gameResult;
	
	boolean               endsInStalemate;
	boolean               endsInCheckmate;
	boolean               unfinished;
	boolean               whiteWinner;
	boolean               blackWinner;

	/*
	private void collectTags() {
		
	}
	private void collectComments() {
		
	}
	private void collectPlywordsAndGameResult(String strippedPgnString) {
		final String SAN_PATTERN = "[0-9]+\\.[\t ]+([^\t\n ]+)[\t ]+([^\t\n ]+)[\t\n ]";
		
		Pattern sanPattern = Pattern.compile(SAN_PATTERN);
		Matcher sanMatcher = sanPattern.matcher(strippedPgnString);
		//generate all of the sanStrings, map the contained plies to a move object, 
		//map the result to gameResult (throw exception if it is not during the last move)
		int moveNum, lastMoveNum = 0, gameEndingNum = 0;
		String gameEndString;
		
		if(!sanMatcher.matches()) {
			throw new IllegalArgumentException("No SAN words found in PGN");
		}
		while(sanMatcher.find()) {
			moveNum = Integer.parseInt(sanMatcher.group("moveNumber"));
			if(moveNum!=lastMoveNum+1) {throw new IllegalArgumentException("PGN does not follow correct move ordering");}
			plywords.put((moveNum-1)*2+1,new Plyword(moveNum, "white", sanMatcher.group("whitePly")));
			if(sanMatcher.group("blackPly")!="") { //necessary since last ply may not contain a black ply
				plywords.put(moveNum*2,new Plyword(moveNum, "black", sanMatcher.group("blackPly")));
			}
			gameEndString = sanMatcher.group("gameResult");
			if(gameEndString!="") {
				setGameEnding(gameEndString);
				gameEndingNum=moveNum;
			}
			lastMoveNum=moveNum;
		}
		if(lastMoveNum!=gameEndingNum || gameEndingNum==0) {throw new IllegalArgumentException("Game endword not found in final moveword");}
		for(int i=0;i<Collections.max(plywords.keySet());i++) { //check whether every entry in the plyword map between the first and final ply is full
			if(!plywords.containsKey(i)) {throw new IllegalArgumentException("Ply " + i + " appears to be missing");}
		}
		//TODO: clear movewords
	}
	private void setGameEnding(String gameEndString) {
		switch(gameEndString) {
			case "*" :
				unfinished = true; endsInStalemate = false; endsInCheckmate = false; whiteWinner = false; blackWinner = false;
				break;
			case "1/2-1/2" :
				unfinished = false; endsInStalemate = true; endsInCheckmate = false; whiteWinner = false; blackWinner = false;
				break;
			case "0-1" :
				unfinished = false; endsInStalemate = false; endsInCheckmate = true; whiteWinner = false; blackWinner = true;
				break;
			case "1-0" :
				unfinished = false; endsInStalemate = false; endsInCheckmate = true; whiteWinner = true; blackWinner = false;
				break;
			default :
				throw new IllegalArgumentException("End game string not in the correct format");
		}
	}
	*/
	//TODO: consider moving the SAN-Move translation code throughout class to the Move object for improved class portability, or create an intermediate object
	public static List<Move> getPlywordObjectsFile(String fileName) {
		return getPlyObjectsString(loadPgnFileToString(fileName));
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
	
	private static List<Move> getPlyObjectsString(String parseString) {
		List<Move> pgnMoveObjects = new ArrayList<Move>();
		@SuppressWarnings("unused") String nakedString = stripMetadataString(parseString);
		
		return pgnMoveObjects;
	}
	private static Map<Integer,String> getMoveSanString(String parseString) {
		Map<Integer,String> pgnMoveSans = new HashMap<Integer,String>(); //map move number to SAN string - 'SAN'=='Standard Algebraic Notation'
		@SuppressWarnings("unused") String nakedString = stripMetadataString(parseString);
		
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
		//if(nothingButWhitespace) {
		//	return true;
		//} else {
		//	return false;
		//}
		return false;
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
	public class Plyword {
		//TODO: figure out a way to make this static...
		private Map<String,String> PIECE_CHAR_TO_TYPE_STRING = new HashMap<String,String>();
		
		private final String STANDARD_MOVEWORD_PATTERN = "((?<pieceType>[QKRBN]?)(?<initFile>[a-g]?)(?<initRank>[1-8]?)(?<isCaptureMove>[x]?)(?<destFile>[a-g])(?<destRank>[1-8])(?<isPawnPromotion>=?)(?<pieceTypePromotion>[QRBNP]?)(?<modifiers>[#+]?)";
		private final String CASTLING_MOVEWORD_PATTERN = "(O-O)";
		
		String initFile="",initRank="";
		String destFile="",destRank="";
		
		String pieceType="";
		String pieceTypePromotion="";
		String playerColour="";
		
		int plyNumber=0; //tag used in the pgn file for parent move pair
		
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
		
		Plyword(int moveNumber, String playerColour, String moveWordString) {
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
	
	PgnParser(String filename) {
		
	}
}
