package iti.suitceyes.JSON;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import iti.suitceyes.components.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import iti.suitceyes.communication.MyThreadServeMessage;

public class JSONImpl {
//	public static SimpleDateFormat sdtformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	public static SimpleDateFormat sdtformat = new SimpleDateFormat("dd-MM-yy hh:mm:ss");

	public static String readJSONFromURL(String url) throws IOException {

		URL objectUrl = new URL(url);

		HttpURLConnection connection = (HttpURLConnection) objectUrl.openConnection();
		int responseCode = connection.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);
		MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Sending 'GET' request to URL: " + url);
		MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Response Code: " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
//		System.out.println(response.toString());
		MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Response Data: " + response.toString());

		return response.toString();
	}
	
	public static String getLinkFromJSON(String json) throws ParseException{
		String json_link="";
		
		JSONObject json_message = (JSONObject) new JSONParser().parse(json.toString());
		if (json_message.containsKey("body")) {
			JSONObject json_body = (JSONObject) json_message.get("body");
			
			if (json_body.containsKey("data")){
				json_link = json_body.get("data").toString();
			}
		}		
		
		return json_link;
		
	}

	public static VAComponentResults parseJSONforVA(String json, String component) throws ParseException {

		// TODO - case 1: VA -- create parseJSON for other components as well
		JSONObject responseJsonObject = null;

		responseJsonObject = (JSONObject) new JSONParser().parse(json.toString());

		// parse JSON - component = VA
		VAComponentResults vaComponentResults = new VAComponentResults();

		if (responseJsonObject.containsKey("image")) {
			JSONObject resultsVAJson = (JSONObject) responseJsonObject.get("image");

			// timestamp
			if (resultsVAJson.containsKey("timestamp")) {
				vaComponentResults.setTimestamp(resultsVAJson.get("timestamp").toString());
			}

			if (resultsVAJson.containsKey("name")) {
				vaComponentResults.setImageName(resultsVAJson.get("name").toString());
			}
			
			// image width, height
			if (resultsVAJson.containsKey("width")){
				vaComponentResults.setImageWidth(Integer.parseInt(resultsVAJson.get("width").toString()));
			}
			if (resultsVAJson.containsKey("height")){
				vaComponentResults.setImageHeight(Integer.parseInt(resultsVAJson.get("height").toString()));
			}

			// target
			if (resultsVAJson.containsKey("target")) {

				JSONArray targetsArrayJson = (JSONArray) resultsVAJson.get("target");

				Iterator<JSONObject> iteratorTargets = targetsArrayJson.iterator();
				while (iteratorTargets.hasNext()) {
					JSONObject targetX = iteratorTargets.next();
					vaComponentResults.addNewVAEntity(targetX.get("type").toString(),
							Float.valueOf(targetX.get("confidence").toString()),
							Float.valueOf(targetX.get("distance").toString()),
							Integer.valueOf(targetX.get("top").toString()),
							Integer.valueOf(targetX.get("left").toString()),
							Integer.valueOf(targetX.get("width").toString()),
							Integer.valueOf(targetX.get("height").toString())
							//String.valueOf(targetX.get("facemask").toString())
							);
				}
			}
			// scene
			if (resultsVAJson.containsKey("scene_type")) {
				vaComponentResults.setSceneType(resultsVAJson.get("scene_type").toString());
			}
			// scene score
			if (resultsVAJson.containsKey("scene_score")) {
				vaComponentResults.setSceneScore(Float.valueOf(resultsVAJson.get("scene_score").toString()));
			}

		}

		return vaComponentResults;

	}
	
	public static String parseJSONforTimestamp(String json, String component) throws ParseException{
		
		JSONObject responseJsonObject = (JSONObject) new JSONParser().parse(json.toString());
		
		if (component.equals("VA_KBS_channel")){
			if (responseJsonObject.containsKey("header")) {
				JSONObject headerJson = (JSONObject) responseJsonObject.get("header");
				// timestamp
				if (headerJson.containsKey("timestamp")) {
					return headerJson.get("timestamp").toString();
				}
				else
					return "";
			}
			else
				return "";
		}
		else if (component.equals("IB_KBS_channel")){
			// timestamp
			if (responseJsonObject.containsKey("timestamp")) {
				return responseJsonObject.get("timestamp").toString();
			}
			else
				return "";
		}
		else return "";


		
	}
	public static IBComponentResults parseJSONforIB(String json, String component) throws ParseException {

		JSONObject responseJsonObject = null;

		responseJsonObject = (JSONObject) new JSONParser().parse(json.toString());

		// parse JSON - component = IB
		IBComponentResults IBComponentResults = new IBComponentResults();

		// timestamp
//		if (component.equals("IBtest")){
//			if (responseJsonObject.containsKey("timestamp")) {
////				IBComponentResults.setTimestamp(sdtformat.format(new Date(System.currentTimeMillis()).toString()));
//				IBComponentResults.setTimestamp(sdtformat.format(new Date(System.currentTimeMillis())));
//			}			
//		}
//		else if (component.equals("IB")){
			if (responseJsonObject.containsKey("timestamp")) {
				IBComponentResults.setTimestamp(responseJsonObject.get("timestamp").toString());
			}
//		}

		// data
		if (responseJsonObject.containsKey("data")) {
			JSONArray iBeaconArrayJson = (JSONArray) responseJsonObject.get("data");

			Iterator<JSONObject> iteratorData = iBeaconArrayJson.iterator();

			while (iteratorData.hasNext()) {
				JSONObject tmp_iBeacon = iteratorData.next();

				IBComponentResults.addNewIBeacon(new IBeacon(
						Integer.valueOf(tmp_iBeacon.get("id").toString()),
						tmp_iBeacon.get("name").toString(), 
						Float.valueOf(tmp_iBeacon.get("positionX").toString()),
						Float.valueOf(tmp_iBeacon.get("positionY").toString()),
						Float.valueOf(tmp_iBeacon.get("rssi").toString()),
						Float.valueOf(tmp_iBeacon.get("distance").toString()),
						tmp_iBeacon.get("message").toString().toLowerCase()));
			}
		}

		return IBComponentResults;
	}


	public static UserComponentResults parseJSONforUser(String json, String component) throws ParseException {

		JSONObject responseJsonObject = null;

		responseJsonObject = (JSONObject) new JSONParser().parse(json.toString());

		UserComponentResults userComponentResults = new UserComponentResults();

		if (responseJsonObject.containsKey("timestamp")) {
			userComponentResults.setTimestamp(responseJsonObject.get("timestamp").toString());
		}

		// data
		if (responseJsonObject.containsKey("data")) {
			userComponentResults.setQuery(responseJsonObject.get("data").toString());
		}

		if (responseJsonObject.containsKey("message")) {
			System.out.println("eoeoeoeo");
			userComponentResults.setQuery(responseJsonObject.get("message").toString());
			System.out.println(responseJsonObject.get("message").toString());
		}

		return userComponentResults;
	}

	public static ActionFeedbackResults parseJSONforActionFeedbackResults(String json, String component) throws ParseException {

		JSONObject responseJsonObject = null;

		responseJsonObject = (JSONObject) new JSONParser().parse(json.toString());

		ActionFeedbackResults actionFeedbackResults = new ActionFeedbackResults();

		if (responseJsonObject.containsKey("entity")) {
			actionFeedbackResults.setEntity(responseJsonObject.get("entity").toString());
		}

		// data
		if (responseJsonObject.containsKey("move_command")) {
			actionFeedbackResults.setCommand(responseJsonObject.get("move_command").toString());
		}

		return actionFeedbackResults;
	}


	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {

		VAComponentResults componentResults1 = parseJSONforVA(
				readJSONFromURL("http://160.40.50.243:8008/upload/storage/VA_da6d0d0034c648f89e8570682e63e522.json"), "VA");
//		VAComponentResults componentResults2 = parseJSON(
//				readJSONFromURL("http://160.40.51.22/mklab_ontologies/SUITCEYES/6300_orig_output.json"), "VA");

		System.out.println(componentResults1.toString());
//		System.out.println(componentResults2.toString());
		
		System.out.println(getLinkFromJSON("http://160.40.50.243:8008/upload/storage/VA_da6d0d0034c648f89e8570682e63e522.json"));

		
		IBComponentResults componentResults = parseJSONforIB(
				readJSONFromURL("http://160.40.51.22/mklab_ontologies/SUITCEYES/tmp_iBeacons.json"), "IB");
		
		System.out.println(componentResults.toString());

	}

}
