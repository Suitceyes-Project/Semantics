package iti.suitceyes.communication;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import iti.suitceyes.ontology.Inference;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ibt.ortc.api.Ortc;
import ibt.ortc.api.Proxy;
import ibt.ortc.extensibility.OnConnected;
import ibt.ortc.extensibility.OnDisconnected;
import ibt.ortc.extensibility.OnException;
import ibt.ortc.extensibility.OnMessage;
import ibt.ortc.extensibility.OnReconnected;
import ibt.ortc.extensibility.OnReconnecting;
import ibt.ortc.extensibility.OnSubscribed;
import ibt.ortc.extensibility.OnUnsubscribed;
import ibt.ortc.extensibility.OrtcClient;
import ibt.ortc.extensibility.OrtcFactory;
import iti.suitceyes.ontology.RemoteGraphDB;
import iti.suitceyes.threads.CreateRepositoryThread;
import iti.suitceyes.threads.FindingEntityThread;
import iti.suitceyes.util.MyFileLogger;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

public class Messaging {

	//private static final String defaultServerUrl = "http://ortc-developers.realtime.co/server/2.1";
	private static final String broker = "ssl://mqtt.ably.io:8883";
	private static final boolean defaultIsBalancer = true;
	//private static final String defaultApplicationKey = "c2dNjc";
	private static final String username = "xLDdSw.QHVEQg";
	private static final String password = "TSyIx1VpZQBj15fR";
//	private static final String username = "ruLRjg.KAJS7g";
//	private static final String password = "kbNeoY67LNpgS-X6";
	private static final String clientId = "Ontology";
	public static final MemoryPersistence persistence = new MemoryPersistence();
	//private static final String defaultAuthenticationToken = "RealtimeDemo";
	private static Proxy proxy;
	public static final String repositoryID = "DEFAULT";
	private static final RemoteGraphDB remoteOntology = new RemoteGraphDB(repositoryID);

	public static MyThreadServeMessage serveMessageThread;
	public static LinkedList<MyMqttMessage> messages = new LinkedList<MyMqttMessage>();

	public static MyFileLogger logIfEntityFound = new MyFileLogger(
			Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
			"logIfEntityFound.log");

	public static FindingEntityThread runnableFindingEntity;


	public static void main(String[] args) throws Exception {

		CreateRepositoryThread start = new CreateRepositoryThread();
		Thread thread = new Thread(start);
		thread.start();

		MqttClient client = connectMqttClient();
//		try {
//			MqttMessage uto_message = new MqttMessage("Where is my laptop?".getBytes());
//			//client.publish("User_to_Ontology_channel", uto_message);
//			client.publish("VA_KBS_channel", uto_message);
//		} catch (MqttException e) {
//			System.out.println("Exception caused");
//			e.printStackTrace();
//		}

//		int i = 0;
//		while(i<100000){
//			Thread.sleep(5000);
//			JSONObject interfaceObj = new JSONObject();
//			interfaceObj.put("Query", "Where are the drawers?");
//			interfaceObj.put("Answer", "The drawers are in the bedroom, on your left side, 3.5m from you " + i);
//			MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
//			//interface_message.setQos(2);
//			client.publish("Query_Answer_Interface_channel_2", interface_message);
//			Thread.sleep(5000);
//			JSONObject interfaceObj2 = new JSONObject();
//			interfaceObj2.put("Entity", "Drawer");
//			interfaceObj2.put("Command", "Search");
//			MqttMessage interface_message2 = new MqttMessage(interfaceObj2.toJSONString().getBytes());
//			//interface_message.setQos(2);
//			client.publish("Active_Object_Search_Interface_channel_2", interface_message2);
//			i++;
//
//		}
//		i=0;
//		while(i<5){
//			JSONObject interfaceObj = new JSONObject();
//			interfaceObj.put("Query", "Where is the laptop?");
//			interfaceObj.put("Answer", "The laptop in the kitchen, on your left side, 3.5m from you" );
//			MqttMessage interface_message = new MqttMessage(interfaceObj.toJSONString().getBytes());
//			client.publish("Query_Answer_Interface_channel", interface_message);
//			System.out.println("Defterh loupa");
//			JSONObject interfaceObj3 = new JSONObject();
//			interfaceObj3.put("Entity", "Laptop");
//			interfaceObj3.put("Command", "Rotate right");
//			MqttMessage interface_message3 = new MqttMessage(interfaceObj3.toJSONString().getBytes());
//			client.publish("Active_Object_Search_Interface_channel", interface_message3);
//			i++;
//		}


		//OrtcClient client = connectOrtcClient();
		Messaging.serveMessageThread = new MyThreadServeMessage();
	}

	public static MqttClient connectMqttClient() throws Exception {
		// Thread.sleep(3000);
		MqttClient client = new MqttClient(broker, clientId, persistence);
		client.setCallback(new MqttCallbackExtended() {
			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
//				System.out.println(String.format(String.format("[" + (formatter).format(time_now) + "] " +
//						"Connected to %s", client.getServerURI().toString()));
				System.out.println(String.format(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
						"Connected to %s", client.getServerURI().toString()));
				try {
					client.subscribe("VA_KBS_channel");
					client.subscribe("Action_Feedback_channel");
//					subscribeChannel(client, "VA_KBS_channel");
					client.subscribe("IB_KBS_channel");
					client.subscribe("KBS_ACT_channel");
					client.subscribe("KBS_SCR_channel");
					client.subscribe("KBS_INFO_channel" );
					client.subscribe("User_to_Ontology_channel");
					client.subscribe("SVA_KBS_channel" );
					client.subscribe("VA_Feedback_channel");
//					client.subscribe("suitceyes/ontology/query");
				} catch (MqttException e) {
					e.printStackTrace();
				}

				// **** hard-coded **** //
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							readMenuCommand(client);
						} catch (ParseException | IOException | MqttException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void connectionLost(Throwable cause) {
				System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
						"Disconnected");
				EventQueue.invokeLater(new Runnable() {

					public void run() {
						try {
							try {
								readMenuCommand(client);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} catch (FileNotFoundException | ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				MyThreadAddMessage addMessageThread;
//				System.out.println(String.format("\n##############################################################################################################################"));
//				System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
//						String.format("Message received on channel %s:\n%s", topic, message.toString()));
//				MyMqttMessage mymqtt = new MyMqttMessage(client, topic, message.toString(), remoteOntology);
//				addMessageThread = new MyThreadAddMessage(mymqtt);
				addMessageThread = new MyThreadAddMessage(new MyMqttMessage(client, topic, message.toString(), remoteOntology));
				addMessageThread.run();

				if (topic.equals("VA_KBS_channel")) {
					//System.out.println("Message received in VA_KBS_channel!");
				}
				if (topic.equals("suitceyes/ontology/query")) {
					System.out.println("Message received from tablet!");
				}
				if (topic.equals("User_to_Ontology_channel")) {
					//System.out.println("Message received in User_To_Ontology_channel!");
					//MqttMessage uto_message = new MqttMessage("Hello James!".getBytes());
					//client.publish("User_To_Ontology_channel", uto_message);
				}
				if (topic.equals("SVA_KBS_channel")) {
					//System.out.println("Message received in SVA_KBS_channel!");
				}
				if (topic.equals("IB_KBS_channel")) {
					//System.out.println("Message received in IB_KBS_channel!");
					//MqttMessage uto_message = new MqttMessage("Hello Shaq!".getBytes());
					//client.publish("User_To_Ontology_channel", uto_message);
					//System.out.println("Sent Hello Shaq to IB_KBS_channel");
				}
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
//				System.out.println("Message delivered");
			}

		});

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		System.out.println("Connecting to broker: " + broker);
		client.connect(connOpts);
		System.out.println("Connected");

		return client;
	}
		
	private static Integer readCommandIndex() {
		Scanner in = new Scanner(System.in);
		return in.nextInt();
	}

	private static void readMenuCommand(MqttClient client) throws ParseException, IOException, MqttException, InterruptedException {

		System.out.println(
				  "Enter command ------------------------------------v\n"
				+ "1:Where am I located? \n"
				+ "2:Where is my cup? \n"
						  + "3:Where is the water bottle? \n"
				+ "4:Where is my backpack? \n"
				+ "5:Does the person near me wear a facemask? \n"
				+ "--------------------------------------------------^"
		);


		int command = readCommandIndex();

		Scanner in = new Scanner(System.in);

		String channel = "";
		String topic = "";
		String message = "";
		String entityName = "";
		String entityType = "";

		JSONObject queryObj;
		MqttMessage query_message;
		switch (command) {
			case 3:
				queryObj = new JSONObject();
				queryObj.put("data", "Where is the water bottle?");
				queryObj.put("timestamp", "2021-05-31T10:15:43.432");
				query_message = new MqttMessage(queryObj.toJSONString().getBytes());
				System.out.println(query_message);
				client.publish("User_to_Ontology_channel", query_message);
				break;

			case 1:
				queryObj = new JSONObject();
				queryObj.put("data", "Where am I?");
				queryObj.put("timestamp", "2021-05-31T10:15:43.432");
				query_message = new MqttMessage(queryObj.toJSONString().getBytes());
				client.publish("User_to_Ontology_channel", query_message);
				break;

			case 2:
				queryObj = new JSONObject();
				queryObj.put("data", "Where is my coffee cup?");
				queryObj.put("timestamp", "2021-05-31T10:15:43.432");
				query_message = new MqttMessage(queryObj.toJSONString().getBytes());
				client.publish("User_to_Ontology_channel", query_message);
				//JSONObject queryObj2 = new JSONObject();
				//queryObj2.put("move_command", "Search");
				//queryObj2.put("entity", "Cup");
				//MqttMessage query_message2 = new MqttMessage(queryObj2.toJSONString().getBytes());
				//client.publish("Action_Feedback_channel", query_message2);
				break;

			case 4:
				queryObj = new JSONObject();
				queryObj.put("data", "Where is my backpack?");
				queryObj.put("timestamp", "2021-05-31T10:15:43.432");
				query_message = new MqttMessage(queryObj.toJSONString().getBytes());
				client.publish("User_to_Ontology_channel", query_message);
				break;
			
			case 5:
				queryObj = new JSONObject();
				queryObj.put("data", "Does the person near me wear a facemask?");
				queryObj.put("timestamp", "2021-05-31T10:15:43.432");
				//query_message = new MqttMessage("Yes".getBytes());
				query_message = new MqttMessage(queryObj.toJSONString().getBytes());
				client.publish("User_to_Ontology_channel", query_message);
				Thread.sleep(2000);
				break;
			
			
			default:
				System.out.println("Invalid command");
				readMenuCommand(client);
				break;
			}
			readMenuCommand(client);
	}

//	public static void subscribeChannel(MqttClient client, String channel) {
//		try {
//			client.subscribe("VA_KBS_channel");
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
//
//
//		client.subscribe(channel, true, new OnMessage() {
//
//			public void run(OrtcClient sender, String channel, String message) {
//
//				MyThreadAddMessage addMessageThread;
//
//				/*
//				System.out.println(String.format("\n##############################################################################################################################"));
//				System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis()))) + "] " +
//						String.format("Message received on channel %s:\n%s", channel, message));
//				*/
//				try {
//					addMessageThread = new MyThreadAddMessage(new MyMqttMessage(sender, channel, message, remoteOntology));
//					addMessageThread.run();
////					addMessageThread.destroy();
//				} catch (ParseException e1) {
//					e1.printStackTrace();
//				}
//			}
//		});
//	}

	


	private static void sendingMultipleMessagesToChannel(MqttClient client, String topic)
			throws ParseException, FileNotFoundException {

		LinkedList<String> messages_fifo = new LinkedList<String>();

		// read messages from messages.json in resources
		JSONObject message_json = new JSONObject();
		Scanner scanner = null;
		if (topic.toLowerCase().contains("ib"))
			scanner = new Scanner(new File("C:\\Users\\kass\\Desktop\\suitceyes\\src\\main\\resources\\messagesIB.json"));
		else if (topic.toLowerCase().contains("va"))
			scanner = new Scanner(new File("C:\\Users\\kass\\Documents\\IntelliJProjects\\suitceyes\\src\\main\\resources\\messagesVA.json"));
			

		while (scanner.hasNextLine()) {
			message_json = (JSONObject) new JSONParser().parse(scanner.nextLine());
			messages_fifo.add(message_json.toJSONString());
		}

		scanner.close();

		while (!messages_fifo.isEmpty()) {
			try {
				MqttMessage msg = new MqttMessage();
				msg.setPayload(messages_fifo.removeFirst().getBytes());
				client.publish(topic, msg);
			} catch (MqttException ex) {
				ex.printStackTrace();
			}
		}
	}
}
