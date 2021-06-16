package iti.suitceyes.threads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ibt.ortc.extensibility.OrtcClient;
import iti.suitceyes.components.ACTPattern;
import iti.suitceyes.ontology.Inference;
import iti.suitceyes.ontology.RemoteGraphDB;
import iti.suitceyes.util.MyFileLogger;
import iti.suitceyes.util.Report;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class FindingEntityThread implements Runnable{
	
	private String entityName;
	private String entityType;

	private Boolean currentVAdetectedValue;
	private Boolean currentIBdetectedValue;
	
	private Report reportVA;
	private Report reportIB;
	
	private RemoteGraphDB remoteOntology;	
	//private OrtcClient client;
	private MqttClient client;
	
	private Boolean run;
	
	private Long thresholdDiff = (long) 30.0;
	
	MyFileLogger logIfEntityFound = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logIfEntityFound.log");
	
	MyFileLogger logTimestampDiff= new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logTimestampDiff.log");
	
	SimpleDateFormat sdtformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	SimpleDateFormat utcformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	SimpleDateFormat londonformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	

	public FindingEntityThread(String entityName, String entityType, RemoteGraphDB remoteOntology, MqttClient client) {
		
		
		this.entityName = entityName;
		this.entityType = entityType;
		
		run = Boolean.TRUE;
		
		currentVAdetectedValue = Boolean.FALSE;
		currentIBdetectedValue = Boolean.FALSE;

		reportVA = new Report("VA", entityName, entityType, currentVAdetectedValue);
		reportIB = new Report("IB", entityName, entityType, currentIBdetectedValue);
		
		this.remoteOntology = remoteOntology;
		this.client = client;
		
		System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
							"Thread SearchingEntity " + entityName + " is created");
		
	}
	
	@Override	
	public synchronized void run(){		

		utcformat.setTimeZone(TimeZone.getTimeZone("UTC"));
		londonformat.setTimeZone(TimeZone.getTimeZone("Etc/GMT-4")); //was-5
		Date currentTimestamp = new Date(System.currentTimeMillis());
//		sdtformat.setTimeZone(TimeZone.getTimeZone("UTC"));//************************************************		
		
		Boolean firstReport = Boolean.TRUE;
					
		while (run){
			
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// 1. query the KB for latest detection of entity (object or person?) through VA, IB			
//			System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())) + "] " +
//					"Inferring new data ... Finding given entity"));
			
			HashMap<String, String> resultsVA = Inference.runInferenceIsEntityFoundFromSensor(remoteOntology, "VA", entityName, entityType);
			HashMap<String, String> resultsIB = Inference.runInferenceIsEntityFoundFromSensor(remoteOntology, "IB", entityName, entityType);
			
			String printLine2Screen = "";
			String currentVAdetectionURI = null; String currentVAtimestamp = null;
			String currentIBdetectionURI = null; String currentIBtimestamp = null;
			String currentVAentityURI = null; String currentIBentityURI = null;
			String currentVAdistance = null; String currentIBdistance = null;
			String currentVAXposition = null; String currentVAscene = null;
			
			Timestamp tmp_timestampVA = null;
			Timestamp tmp_timestampIB = null; 
			currentTimestamp = new Date(System.currentTimeMillis());
			long diff;
			Long diffSecOverallVA = null; Long diffSecOverallIB = null;	
			
			
			if (!resultsVA.isEmpty()){
				currentVAdetectedValue = Boolean.TRUE;
				currentVAdetectionURI = resultsVA.get("detection");
				currentVAtimestamp = resultsVA.get("timestamp").replaceAll("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
				currentVAentityURI = resultsVA.get("entity");
				currentVAdistance = resultsVA.get("distance").replaceAll("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#float>", "");
				currentVAXposition = resultsVA.get("xGridPosition").replaceAll("\"", "");
				currentVAscene = resultsVA.get("semanticLabel").replaceAll("\"", "");
				try {
//					tmp_timestampVA = new Timestamp(sdtformat.parse(currentVAtimestamp).getTime());
					tmp_timestampVA = new Timestamp(utcformat.parse(currentVAtimestamp).getTime());
					Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//					diff = currentTimestamp.getTime() - tmp_timestampVA.getTime();
					diff =  utccurrenttime.getTime() - tmp_timestampVA.getTime();
					
					diffSecOverallVA = 	( diff / 1000 % 60 ) + 
									  	( diff / (60 * 1000) % 60) * 60 +
									  	( diff / (60 * 60 * 1000) % 24 ) * 60 * 60 ;
					try {
//						logTimestampDiff.appendNewLineWithTimestamp("VA detected time: " + tmp_timestampVA + ", current:" + sdtformat.format(currentTimestamp) 
//							+ "differenceVA in sec overall: "  + diffSecOverallVA);
						logTimestampDiff.appendNewLineWithTimestamp("VA detected time: " + tmp_timestampVA + ", current:" + utccurrenttime 
						+ "differenceVA in sec overall: "  + diffSecOverallVA);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
//				System.out.println(currentVAdetectionURI + currentVAdetectedValue + currentVAtimestamp + "\t" + tmp_timestampVA);
			}

			
			if (!resultsIB.isEmpty()){
				currentIBdetectedValue = Boolean.TRUE;
				currentIBdetectionURI = resultsIB.get("detection");
				currentIBtimestamp = resultsIB.get("timestamp").replaceAll("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
				currentIBentityURI = resultsIB.get("entity"); 
				currentIBdistance = resultsIB.get("distance").replaceAll("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#string>", "");
				try {
//					tmp_timestampIB = new Timestamp(sdtformat.parse(currentIBtimestamp).getTime());
					tmp_timestampIB = new Timestamp(utcformat.parse(currentIBtimestamp).getTime());

//					Timestamp londoncurrenttime = new Timestamp(londonformat.parse(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(System.currentTimeMillis()))).getTime());
					Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//					diff =  londoncurrenttime.getTime() - tmp_timestampIB.getTime();
					diff =  utccurrenttime.getTime() - tmp_timestampIB.getTime();

					
					diffSecOverallIB = 	( diff / 1000 % 60 ) + 
										( diff / (60 * 1000) % 60) * 60 +
										( diff / (60 * 60 * 1000) % 24 ) * 60 * 60 ;

					try {
//						logTimestampDiff.appendNewLineWithTimestamp("IB detected time: " + tmp_timestampIB + ", current:" + sdtformat.format(currentTimestamp) 
//						+ "differenceIB in sec overall: "  + diffSecOverallIB);
//						logTimestampDiff.appendNewLineWithTimestamp("IB detected time: " + tmp_timestampIB + ", current:" + londoncurrenttime 
						logTimestampDiff.appendNewLineWithTimestamp("IB detected time: " + tmp_timestampIB + ", current:" + utccurrenttime 
						+ "differenceIB in sec overall: "  + diffSecOverallIB);
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
					

				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
//				System.out.println(currentIBdetectionURI + currentIBdetectedValue + currentIBtimestamp + "\t" + tmp_timestampIB);

			}


//			System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())) + "] " +
//					"Inference completed..."));
			
				
			//if abs(currentVAtimestamp - currentIBtimestamp) < threshold then proceed else break :... 10sec
			// and if max(currenttimenstap) - nowtimestamp < threshold...
			if (Objects.nonNull(diffSecOverallVA)){
				if (diffSecOverallVA > this.thresholdDiff){
					currentVAdetectedValue = Boolean.FALSE;
					currentVAdistance = null;
					currentVAXposition = null;
					currentVAscene = null;
				}
				
			}
			if (Objects.nonNull(diffSecOverallIB)){
				if (diffSecOverallIB > this.thresholdDiff){
					currentIBdetectedValue = Boolean.FALSE;
					currentIBdistance = null;
				}				
			}
			/*
			if (Objects.nonNull(currentVAtimestamp) && Objects.nonNull(currentIBtimestamp)){
				try{
//					System.out.println("timeVA: " + sdtformat.parse(currentVAtimestamp) + ", timeIB: " + sdtformat.parse(currentIBtimestamp));
					long detectionDiff = Math.abs(sdtformat.parse(currentVAtimestamp).getTime() - sdtformat.parse(currentIBtimestamp).getTime());
					long detectionDiffSecOverall = 	( detectionDiff / 1000 % 60 ) + 
										( detectionDiff / (60 * 1000) % 60) * 60 +
										( detectionDiff / (60 * 60 * 1000) % 24 ) * 60 * 60 ;
//					System.out.println("Difference in detection: " + detectionDiffSecOverall);
//					if (
//							(Math.abs(
//									(new Timestamp(sdtformat.parse(currentVAtimestamp).getTime()).getTime()) - 
//									(new Timestamp(sdtformat.parse(currentIBtimestamp).getTime())).getTime())) 
//							> this.thresholdDiff)
//					{
					if (detectionDiffSecOverall > 10.0){
//						System.out.println(">10");
						currentVAdetectedValue = Boolean.FALSE;
						currentVAdistance = null;
						currentVAXposition = null;
						currentVAscene = null;
						currentIBdetectedValue = Boolean.FALSE;	
						currentIBdistance = null;
					}					
				}
				catch(java.text.ParseException e) {
					e.printStackTrace();
					
				}
				
			}
			*/

			
			if (firstReport){
				// inform the user if it is the first iteration
				try {
					printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("########## SEARCHING FOR ENTITY(" + entityType + "): " + entityName + " ##########");
					sendMessageToScreen(printLine2Screen);
					printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("1VAfound: " + currentVAdetectedValue + 
							" (distanceVA=" + currentVAdistance + ", position=" + currentVAXposition + ", scenetype=" + currentVAscene + "), " +  
							"IBfound: " + currentIBdetectedValue + 
							" (distanceIB=" + currentIBdistance + ")"
							);
					sendMessageToScreen(printLine2Screen);
//					sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, completed);
					sendMessageToActuators(currentVAdetectedValue, currentVAdistance, currentIBdetectedValue, currentIBdistance, entityName);
					
					if (currentVAdetectedValue && currentIBdetectedValue){ // if found from the beginning (TRUE, TRUE)
						if ((currentIBdistance.toLowerCase().equals("immediate")) || (currentIBdistance.toLowerCase().equals("near"))){
							printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("########## END ##########\n");
							sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.TRUE);
	//						sendMessageToScreen(printLine2Screen); - not needed
							//4. break the loop
							makeRunFalse();
						}
						else if (currentIBdistance.toLowerCase().equals("far"))
							sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);
					}
					else
						sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);

					//update values
					reportVA.setReportedValue(currentVAdetectedValue);
					reportVA.setReportedDistance(currentVAdistance);
					reportIB.setReportedValue(currentIBdetectedValue);
					reportIB.setReportedDistance(currentIBdistance);
					currentTimestamp = new Date(System.currentTimeMillis());
					reportVA.setReportedTimestamp(sdtformat.format(currentTimestamp));
					reportIB.setReportedTimestamp(sdtformat.format(currentTimestamp));
					reportVA.setReportedXPosition(currentVAXposition);
					reportVA.setReportedScene(currentVAscene);
					firstReport = Boolean.FALSE;	
					
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
							
			}
			else { //not first report, so compare current with reported				
				
				//if current + reported are the same
				if ((Boolean.compare(currentVAdetectedValue, reportVA.getReportedValue()) == 0) && (Boolean.compare(currentIBdetectedValue, reportIB.getReportedValue()) == 0)){ //returns the value 0 if x == y; a value less than 0 if !x && y; and a value greater than 0 if x && !y
					//update timestamp values anyway
					reportVA.setReportedTimestamp(currentVAtimestamp);
					reportIB.setReportedTimestamp(currentIBtimestamp);
					if((!Objects.equals(reportIB.getReportedDistance(), currentIBdistance))){// and distances are not the same, just print
						try {
							printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("2VAfound: " + currentVAdetectedValue + 
									" (distanceVA=" + currentVAdistance + ", position=" + currentVAXposition + ", scenetype=" + currentVAscene + "), " +  
									"IBfound: " + currentIBdetectedValue + 
									" (distanceIB=" + currentIBdistance + ")"
									);
//							sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, completed);
							sendMessageToScreen(printLine2Screen);
							//but DO NOT send any message to actuators -- via the sendMessageToActuators() because patterns should be more complicated now
							if (Boolean.logicalAnd(currentVAdetectedValue, currentIBdetectedValue)){	//only if both are true							
								if ((currentIBdistance.toLowerCase().equals("immediate")) || (currentIBdistance.toLowerCase().equals("near"))){
									printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("########## END ##########\n");
									sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.TRUE);
			//						sendMessageToScreen(printLine2Screen); - not needed
									sendMessageToActuators(currentVAdetectedValue, currentVAdistance, currentIBdetectedValue, currentIBdistance, entityName);
									//4. break the loop
									makeRunFalse();
								}
								else if (currentIBdistance.toLowerCase().equals("far"))
									sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);

							}
							else
								sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);


							//update values
							reportVA.setReportedValue(currentVAdetectedValue);
							reportVA.setReportedDistance(currentVAdistance);
							reportVA.setReportedTimestamp(currentVAtimestamp);
							reportIB.setReportedValue(currentIBdetectedValue);
							reportIB.setReportedDistance(currentIBdistance);
							reportIB.setReportedTimestamp(currentIBtimestamp);
							reportVA.setReportedXPosition(currentVAXposition);
							reportVA.setReportedScene(currentVAscene);
						} catch (IOException | ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}					
				
				}
				else { //reported and current detection values are not the same
									
					// If both VAfound, IBfound = True, finalise the loop
					if (Boolean.logicalAnd(currentVAdetectedValue, currentIBdetectedValue)){ 
						try {
							printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("3VAfound: " + currentVAdetectedValue + 
									" (distanceVA=" + currentVAdistance + ", position=" + currentVAXposition + ", scenetype=" + currentVAscene + "), " +  
									"IBfound: " + currentIBdetectedValue + 
									" (distanceIB=" + currentIBdistance + ")"
									);
//							sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, completed);
							sendMessageToScreen(printLine2Screen);
							
							//update values
							reportVA.setReportedValue(currentVAdetectedValue);
							reportVA.setReportedDistance(currentVAdistance);
							if (!Objects.equals(currentVAtimestamp, null))
								reportVA.setReportedTimestamp(currentVAtimestamp);
							reportIB.setReportedValue(currentIBdetectedValue);
							reportIB.setReportedDistance(currentIBdistance);
							if (!Objects.equals(currentIBtimestamp, null))
								reportIB.setReportedTimestamp(currentIBtimestamp);
							reportVA.setReportedXPosition(currentVAXposition);
							reportVA.setReportedScene(currentVAscene);
												
							if ((currentIBdistance.toLowerCase().equals("immediate")) || (currentIBdistance.toLowerCase().equals("near"))){
								printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("########## END ##########\n");
		//						sendMessageToScreen(printLine2Screen); - not needed
								sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.TRUE);
								sendMessageToActuators(currentVAdetectedValue, currentVAdistance, currentIBdetectedValue, currentIBdistance, entityName);
								//4. break the loop
								makeRunFalse();
							}
							else if (currentIBdistance.toLowerCase().equals("far"))
								sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);

						} catch (ParseException | IOException e) {
							e.printStackTrace();
						}		
					}
					else{ // VA or IB not found AND first report = false
						try {
							printLine2Screen = logIfEntityFound.appendNewLineWithTimestamp("4VAfound: " + currentVAdetectedValue + 
									" (distanceVA=" + currentVAdistance + ", position=" + currentVAXposition + ", scenetype=" + currentVAscene + "), " +  
									"IBfound: " + currentIBdetectedValue + 
									" (distanceIB=" + currentIBdistance + ")"
									);
							sendMessageToInfoChannel(currentVAdetectedValue, currentVAXposition, currentIBdetectedValue, currentIBdistance, entityName, Boolean.FALSE);
							sendMessageToScreen(printLine2Screen);
							sendMessageToActuators(currentVAdetectedValue, currentVAdistance, currentIBdetectedValue, currentIBdistance, entityName);	
							
							// Record current values to reported ones
							reportVA.setReportedValue(currentVAdetectedValue);
							if (!Objects.equals(currentVAtimestamp, null))
								reportVA.setReportedTimestamp(currentVAtimestamp);
							reportVA.setReportedDistance(currentVAdistance);
							reportIB.setReportedValue(currentIBdetectedValue);
							if (!Objects.equals(currentIBtimestamp, null))
								reportIB.setReportedTimestamp(currentIBtimestamp);
							reportIB.setReportedDistance(currentIBdistance);
							reportVA.setReportedXPosition(currentVAXposition);
							reportVA.setReportedScene(currentVAscene);
						
						} catch (ParseException | IOException e) {
							e.printStackTrace();
						}		
						
					}				
					
				}				
			}			
		}
	}
	
	private void sendMessageToScreen(String line){
		JSONObject screenMessage = new JSONObject();
		screenMessage.put("data", line.replaceAll("\\n", ""));
		try {
			MqttMessage msg = new MqttMessage();
			msg.setPayload(screenMessage.toJSONString().getBytes());
			client.publish("KBS_SCR_channel", msg);
		} catch (MqttException ex) {
			ex.printStackTrace();
		}

	}
	
	private void sendMessageToInfoChannel(Boolean VAfound, String VAposition, Boolean IBfound, String distanceIB, String entityName, Boolean completed){
		JSONObject infoMessageJSONObject = new JSONObject();
		infoMessageJSONObject.put("timestamp", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(System.currentTimeMillis())).toString()));
		infoMessageJSONObject.put("entity", entityName);
		infoMessageJSONObject.put("completed", completed);
		//add message
		if (VAfound && IBfound && (!distanceIB.toLowerCase().equals("far"))) //found by both and distance is near or immediate
			infoMessageJSONObject.put("message", "found");
		else if ((!VAfound) && (!IBfound)) //not found by anyone
			infoMessageJSONObject.put("message", "not found");
		else if ((!VAfound) && IBfound && (distanceIB.toLowerCase().equals("far"))) //found by IB only but distance is far
			infoMessageJSONObject.put("message", "not found");
		else if ((!VAfound) && IBfound && (distanceIB.toLowerCase().equals("immediate"))) //found by IB only but distance is immediate
			infoMessageJSONObject.put("message", "rotate");
		else if((!VAfound) && IBfound && (distanceIB.toLowerCase().equals("near"))) //found by IB only but distance is near
			infoMessageJSONObject.put("message", "closer");
		//add info 
		JSONArray infoJSONArray = new JSONArray();
		JSONObject infoVAJSONObject = new JSONObject();
		infoVAJSONObject.put("component_ID", "VA");
		infoVAJSONObject.put("found", VAfound);
		infoVAJSONObject.put("distance", null);
		infoVAJSONObject.put("position", VAposition);
		JSONObject infoIBJSONObject = new JSONObject();
		infoIBJSONObject.put("component_ID", "IB");
		infoIBJSONObject.put("found", IBfound);
		infoIBJSONObject.put("distance", distanceIB);
		infoIBJSONObject.put("position", null);
		infoJSONArray.add(infoVAJSONObject);
		infoJSONArray.add(infoIBJSONObject);
		infoMessageJSONObject.put("info", infoJSONArray);

		try {
			MqttMessage msg = new MqttMessage();
			msg.setPayload(infoMessageJSONObject.toJSONString().getBytes());
			client.publish("KBS_INFO_channel", msg);
			System.out.println("Sent message to KBS_INFO_channel");
		} catch (MqttException ex) {
			ex.printStackTrace();
		}
		//client.send("KBS_INFO_channel", infoMessageJSONObject.toJSONString());
		System.out.println("Message sent to KBS_INFO_channel: \n" + infoMessageJSONObject.toJSONString());		
	
	}

	private void sendMessageToActuators(Boolean VAfound, String distanceVA, Boolean IBfound, String distanceIB, String entityName) throws FileNotFoundException, ParseException, IOException {
		
		JSONObject actMessageJSONObject = new JSONObject();
		actMessageJSONObject.put("timestamp", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(System.currentTimeMillis())).toString()));
		//add info 
		JSONArray infoJSONArray = new JSONArray();
		JSONObject infoJSONObject = new JSONObject();
		//add sensors
		JSONArray sensorsJSONArray = new JSONArray();
		JSONObject sensorVAJSONObject = new JSONObject();
		sensorVAJSONObject.put("VA", VAfound);
		sensorVAJSONObject.put("distance", distanceVA);
		JSONObject sensorIBJSONObject = new JSONObject();
		sensorIBJSONObject.put("IB", IBfound);
		sensorIBJSONObject.put("distance", distanceIB);
		sensorsJSONArray.add(sensorVAJSONObject);
		sensorsJSONArray.add(sensorIBJSONObject);
		infoJSONObject.put("entity", entityName);
		infoJSONObject.put("sensor", sensorsJSONArray);
		
		infoJSONArray.add(infoJSONObject);
		actMessageJSONObject.put("info", infoJSONArray);
		
		//add patterns
		JSONArray patternFromFile = null;
		if (VAfound && IBfound)
			patternFromFile = (JSONArray) (new JSONParser().parse(ACTPattern.getPatternWhenBothFoundTrue()));
						
		else if ((!VAfound) && (!IBfound))
			patternFromFile = (JSONArray) (new JSONParser().parse(ACTPattern.getPatternWhenBothFoundFalse()));
		
		else if ((VAfound) && (!IBfound))
			patternFromFile = (JSONArray) (new JSONParser().parse(ACTPattern.getPatternWhenOnlyVAFoundTrue()));

		else if ((!VAfound) && (IBfound))
			patternFromFile = (JSONArray) (new JSONParser().parse(ACTPattern.getPatternWhenOnlyIBFoundTrue()));

		actMessageJSONObject.put("patterns", patternFromFile);
		try {
			MqttMessage msg = new MqttMessage();
			msg.setPayload(actMessageJSONObject.toJSONString().getBytes());
			client.publish("KBS_ACT_channel", msg);
			System.out.println("Sent message to KBS_ACT_channel");
		} catch (MqttException ex) {
			ex.printStackTrace();
		}
		//client.send("KBS_ACT_channel", actMessageJSONObject.toJSONString());
//		System.out.println("Message sent to KBS_ACT_channel: \n" + actMessageJSONObject.toJSONString());		
	}

	public void makeRunFalse() {
		this.run = Boolean.FALSE; 		
	}
}
