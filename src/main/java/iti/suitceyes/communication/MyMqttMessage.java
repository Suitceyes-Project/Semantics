package iti.suitceyes.communication;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ibt.ortc.extensibility.OrtcClient;
import iti.suitceyes.JSON.JSONImpl;
import iti.suitceyes.ontology.RemoteGraphDB;

public class MyMqttMessage {
	
	//private OrtcClient sender;
	private MqttClient sender;
	//private String channel;
	private String topic;
	private String message;
	private RemoteGraphDB remoteOntology;
	private String timestamp;
	
	public MyMqttMessage(MqttClient sender,	String topic, String message,	RemoteGraphDB remoteOntology) throws ParseException{
		this.sender = sender;
		this.topic = topic;
		this.message = message;
		this.remoteOntology = remoteOntology;
		
		//set timestamp of message
		this.timestamp = JSONImpl.parseJSONforTimestamp(((JSONObject) new JSONParser().parse(message)).toString(), topic);
	}
	
	public MqttClient getSender(){
		return this.sender;
	}
	
	public String getTopic(){
		return this.topic;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public RemoteGraphDB getRemoteOntology(){
		return this.remoteOntology;
	}
	
	public String getTimestamp(){
		return this.timestamp;
	}

}
