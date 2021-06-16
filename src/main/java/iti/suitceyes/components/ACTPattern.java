package iti.suitceyes.components;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ACTPattern {
	
	public static String getPatternWhenBothFoundTrue() throws FileNotFoundException, IOException, ParseException{
		//use in parallel (long beep - long stop - long beep - long stop )
		return readPatternFromJSONFile("VAtrue_IBtrue_pattern.json").get("patterns").toString();
	}
	
	public static String getPatternWhenBothFoundFalse() throws FileNotFoundException, IOException, ParseException{
		//in parallel (short beep - short stop - short beep - short stop)
		return readPatternFromJSONFile("VAfalse_IBfalse_pattern.json").get("patterns").toString();
	}
	
	public static String getPatternWhenOnlyVAFoundTrue() throws FileNotFoundException, IOException, ParseException{
		//flash 0,1,2
		return readPatternFromJSONFile("VAtrue_IBfalse_pattern.json").get("patterns").toString();
	}
	
	public static String getPatternWhenOnlyIBFoundTrue() throws FileNotFoundException, IOException, ParseException{
		//flash 3,4,5
		return readPatternFromJSONFile("VAfalse_IBtrue_pattern.json").get("patterns").toString();

	}
	
	private static JSONObject readPatternFromJSONFile(String filename) throws FileNotFoundException, IOException, ParseException{
		JSONParser parser = new JSONParser();		
		return (JSONObject) parser.parse(new FileReader(
											Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() 
											+ "\\src\\main\\resources/"
											+ filename));
		
	}

}
