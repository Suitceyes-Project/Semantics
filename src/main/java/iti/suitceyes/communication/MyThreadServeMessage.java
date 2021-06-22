package iti.suitceyes.communication;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import iti.suitceyes.components.ActionFeedbackResults;
import iti.suitceyes.components.UserComponentResults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import iti.suitceyes.JSON.JSONImpl;
import iti.suitceyes.components.IBComponentResults;
import iti.suitceyes.components.VAComponentResults;
import iti.suitceyes.ontology.Inference;
import iti.suitceyes.util.MyFileLogger;

public class MyThreadServeMessage extends Thread{
	
	private String message = "";
	
	public static final MyFileLogger logMessagesServed = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logMessagesServed.log");
	
	public static final MyFileLogger logOntologyPopulationAndInferenceVA = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logOntologyPopulationAndInferenceVA.log");
	
	public static final MyFileLogger logOntologyPopulationAndInferenceIB = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logOntologyPopulationAndInferenceIB.log");

	public static final MyFileLogger logUserQueryData = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logUserQueryData.log");
//	public static final MyFileLogger logOntologyPopulationAndInference = new MyFileLogger(
//			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\", 
//			"logOntologyPopulationAndInference.log");
	

	public MyThreadServeMessage() {
		
		System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
									"Thread: ServeMessage is created");
		this.run();
		
	}
	
	public void run(){
		MyMqttMessage tmpMqttMessage = null;
		String myLog = "";
		
		while (Boolean.TRUE){
			
			try {
				Thread.sleep(70);//1000 - 100
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!Messaging.messages.isEmpty()){
				//*************************//
				// Handle always the first
				//*************************//

				tmpMqttMessage = Messaging.messages.removeFirst();
				
				try {
					logMessagesServed.appendNewLineWithTimestamp("Message from: " + tmpMqttMessage.getTopic() + " with timestamp: " + tmpMqttMessage.getTimestamp() + " now gets processed...");
					if (tmpMqttMessage.getTopic().contains("VA_KBS_channel")){
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Processing message...");
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("-- " + tmpMqttMessage.getMessage());
						
					}
					else if (tmpMqttMessage.getTopic().contains("IB_KBS_channel")){
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Processing message...");
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("-- " + tmpMqttMessage.getMessage());
					}
//					logOntologyPopulationAndInference.appendNewLineWithTimestamp("Processing message...");
//					logOntologyPopulationAndInference.appendNewLineWithTimestamp("-- " + tmpMessage.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}

				JSONObject message_json = new JSONObject();					
				
				if (tmpMqttMessage.getTopic().contains("IB_KBS_channel")){//***********************************
					try {
						//Parse data//
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Parsing IB data...");
						message_json = (JSONObject) new JSONParser().parse(tmpMqttMessage.getMessage());
						IBComponentResults componentResults = JSONImpl.parseJSONforIB(message_json.toJSONString(), "IB");//***************************************SOS
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Parsing completed...");
						//Populate data//
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Populating data from the IB to the KB...");
						tmpMqttMessage.getRemoteOntology().populateIBDataWithSPARQL(componentResults);
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Population completed...");
						//Run Inference//
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Inferring new data regarding latest IB detection...");
						// Infer entities that are detected
						Inference.runInferenceEntityTypeDetection(tmpMqttMessage.getRemoteOntology(), "iBeacon");
						//Infer those items that are far (>6000) from you.
						Float distance_threshold = new Float(4000.0);
						Inference.runInferenceEntitiesFarFromUser(tmpMqttMessage.getRemoteOntology(), distance_threshold, "iBeacon");
						logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp("Inference completed...\n\n");
						
						/*
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
								String.format("Message response on channel %s: %s", tmpMessage.getChannel(), "{\"info\": \"ok\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));
						*/
						
					}
					catch (ParseException | java.text.ParseException ex) {
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
								String.format("Message response on channel %s: %s", tmpMqttMessage.getTopic(), "{\"info\": \"error\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
								
				else if (tmpMqttMessage.getTopic().contains("VA_KBS_channel")){
					try {	
						//Parse data//
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Parsing VA data...");
						message_json = (JSONObject) new JSONParser().parse(JSONImpl.readJSONFromURL(JSONImpl.getLinkFromJSON(tmpMqttMessage.getMessage())));
						//message_json = (JSONObject) new JSONParser().parse(tmpMqttMessage.getMessage());
						VAComponentResults componentResults = JSONImpl.parseJSONforVA(message_json.toJSONString(), "VA");
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Parsing completed...");
						//Populate data//
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Populating data from the VA to the KB...");
						tmpMqttMessage.getRemoteOntology().populateVADataWithSPARQL(componentResults);
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Population completed...");
						//Run Inference//
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Inferring new data regarding latest VA detection...");
						// Infer where user is located
						Inference.runInferenceSceneDetection(tmpMqttMessage.getRemoteOntology());
						// Infer entities that are detected
						Inference.runInferenceEntityTypeDetection(tmpMqttMessage.getRemoteOntology(), "Camera");
						//Infer those items that are far (>6000) from you.
						Float distance_threshold = new Float(4000.0);
						Inference.runInferenceEntitiesFarFromUser(tmpMqttMessage.getRemoteOntology(), distance_threshold, "Camera");
						logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp("Inference completed...\n\n");
						
						/*
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
								String.format("Message response on channel %s: %s", tmpMessage.getChannel(), "{\"info\": \"ok\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));
						*/

					} catch (ParseException ex) {
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
								String.format("Message response on channel %s: %s", tmpMqttMessage.getTopic(), "{\"info\": \"error\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if (tmpMqttMessage.getTopic().contains("Action_Feedback_channel")){
					try {
						//Parse data//
						message_json = (JSONObject) new JSONParser().parse(tmpMqttMessage.getMessage());
//						System.out.println("Json message" + message_json);
						ActionFeedbackResults actionFeedbackResults = JSONImpl.parseJSONforActionFeedbackResults(message_json.toJSONString(), "Action_feedback");

//						System.out.println("Entity:" + actionFeedbackResults.getEntity());
//						System.out.println("Command:" + actionFeedbackResults.getCommand());

						JSONObject interfaceObj = new JSONObject();
						interfaceObj.put("Entity", actionFeedbackResults.getEntity());
						interfaceObj.put("Command", actionFeedbackResults.getCommand());
						MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
						tmpMqttMessage.getSender().publish("Active_Object_Search_Interface_channel_2", interface_message);

						/*
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
								String.format("Message response on channel %s: %s", tmpMessage.getChannel(), "{\"info\": \"ok\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));
						*/

					} catch (ParseException ex) {
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
										String.format("Message response on channel %s: %s", tmpMqttMessage.getTopic(), "{\"info\": \"error\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));

					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
				else if (tmpMqttMessage.getTopic().contains("KBS_ACT_channel")){
					/*
					System.out.println(String.format("Message received on channel %s: %s %s", tmpMessage.getChannel(), tmpMessage.getMessage(), " {\"info\": \"ok\"}"));
					*/ 						
				}//end of if-else VA/IB/ACT
//				else if (tmpMqttMessage.getTopic().contains("suitceyes/ontology/query")){
//					System.out.println("oooooooooooooo");
//				}
//				else if (tmpMqttMessage.getTopic().contains("User_to_Ontology_channel")){
				else if (tmpMqttMessage.getTopic().contains("suitceyes/ontology/query")){
					//System.out.println("bhkeeeee");
					Map<String,String> queryAnswers = new HashMap<String,String>();
					String interfaceQAnswer;
					try {
						//Parse data//
						logUserQueryData.appendNewLineWithTimestamp("Parsing User Queries...");
						//message_json = (JSONObject) new JSONParser().parse(JSONImpl.readJSONFromURL(JSONImpl.getLinkFromJSON(tmpMqttMessage.getMessage())));
						message_json = (JSONObject) new JSONParser().parse(tmpMqttMessage.getMessage());
						UserComponentResults componentResults = JSONImpl.parseJSONforUser(message_json.toJSONString(), "User");
						logUserQueryData.appendNewLineWithTimestamp("Parsing completed...");
						//Process Query//
						if (componentResults.getQuery().equals("Where is the drawers?"))
						{
//							System.out.println("bhka");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "Drawer");
//							System.out.println("interface final answer" + interfaceQAnswer);

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where are the drawers?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);

							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "Drawer");
							JSONObject feedbackObj = new JSONObject();
							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//							System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println("Feedback final json: " + feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("where is the water bottle?"))
						{
							System.out.println("bhka");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "Bottle");



							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "Bottle");
//							System.out.println("Gyrisa to answer");

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where is the water bottle?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);
							JSONObject feedbackObj = new JSONObject();

							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//							System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println(feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("Where is door?"))
						{
//							System.out.println("bhka");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "Door");

							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "Door");
//							System.out.println("Gyrisa to answer");

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where is the door?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);

							JSONObject feedbackObj = new JSONObject();
							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//							System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println(feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("where is my coffee cup?"))
						{
							System.out.println("bhka οεοεοεο");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "Cup");

							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "Cup");
//							System.out.println("Gyrisa to answer");

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where is my coffee cup?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);

							JSONObject feedbackObj = new JSONObject();
							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
							//System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println(feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("Where is my backpack?"))
						{
//							System.out.println("bhka");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "backpack");

							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "backpack");
//							System.out.println("Gyrisa to answer");

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where is my backpack?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);

							JSONObject feedbackObj = new JSONObject();
							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//							System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println(feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("Where is the chair?"))
						{
//							System.out.println("bhka");
							interfaceQAnswer = Inference.runInferenceFindLatestDetectionOfEntity(tmpMqttMessage.getRemoteOntology(), "Chair");

							queryAnswers = Inference.runInferenceFindEntity(tmpMqttMessage.getRemoteOntology(), "Chair");
//							System.out.println("Gyrisa to answer");

							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where is the chair?");
							interfaceObj.put("Answer", interfaceQAnswer);
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);

							JSONObject feedbackObj = new JSONObject();
							Date currentTimestamp = new Date(System.currentTimeMillis()- 3600 * 2000);
							Timestamp utccurrenttime = new Timestamp(currentTimestamp.getTime());
//							System.out.println(queryAnswers.get("answer"));
							if(queryAnswers.get("answer").equals("not found")){
								feedbackObj.put("timestamp", utccurrenttime.toString());
								feedbackObj.put("VA_detection", false);
								feedbackObj.put("offset", -9999);
								feedbackObj.put("distance", -0.1);
							}
							else {
								feedbackObj.put("timestamp", queryAnswers.get("timestamp"));
								feedbackObj.put("VA_detection", true);
								feedbackObj.put("offset", Integer.parseInt(queryAnswers.get("offset")));
								feedbackObj.put("distance", Float.parseFloat(queryAnswers.get("distance")));
							}
//							System.out.println(feedbackObj.toJSONString());
							MqttMessage feedback_message = new MqttMessage(feedbackObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("VA_Feedback_channel", feedback_message);
						}
						else if (componentResults.getQuery().equals("where am i?"))
						{
//							System.out.println("bhka2");
							queryAnswers = Inference.runInferenceFindUserLocation(tmpMqttMessage.getRemoteOntology());
//							System.out.println(queryAnswers.get("answer"));
							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Where am i located?");
							interfaceObj.put("Answer", queryAnswers.get("answer"));
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);
						}
						else if (componentResults.getQuery().equals("Does the person near me wear a facemask?"))
						{
							JSONObject interfaceObj = new JSONObject();
							interfaceObj.put("Query", "Does the person near me wear a facemask?");

							System.out.println("1 or 2");
							Scanner in = new Scanner(System.in);
							if (in.nextInt() == 6)
								interfaceObj.put("Answer", "Yes");
							else
								interfaceObj.put("Answer", "No");
							MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
							tmpMqttMessage.getSender().publish("Query_Answer_Interface_channel_2", interface_message);
						}

					} catch (ParseException ex) {
						System.out.println(
								String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
										String.format("Message response on channel %s: %s", tmpMqttMessage.getTopic(), "{\"info\": \"error\"}"));
						System.out.println(String.format("##############################################################################################################################\n"));

					} catch (IOException e) {
						e.printStackTrace();
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
				
			}// end of isMessagingListEmpty
			else{
//				System.out.println("Message served: \t null");
//				try {
//					logMessagesServed.appendNewLineWithTimestamp("Message: null");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}	
	}
}
