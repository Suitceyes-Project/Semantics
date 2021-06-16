package iti.suitceyes.communication;

import java.util.HashMap;

import ibt.ortc.extensibility.OrtcClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {
	
	private HashMap<String,Boolean> channels;
	private MqttClient client;

	public Subscriber(MqttClient client) {
		channels = new HashMap<String,Boolean>();
		this.client = client;
	}
	
//	public Subscriber(HashMap<String,Boolean> channels, OrtcClient client){
//		this.channels = channels;
//		this.client = client;
//	}
	
	public void addChannel(String channel_name, Boolean subscribed){
		this.channels.put(channel_name, subscribed);
//		if (subscribed)
//			Messaging.subscribeChannel(client, channel_name);
		if (subscribed) {
			try {
				client.subscribe(channel_name);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		
		MqttClient client = Messaging.connectMqttClient();
		
		Subscriber subscriber = new Subscriber(client);

		subscriber.addChannel("VA_KBS_channel", Boolean.TRUE);
		subscriber.addChannel("IB_KBS_channel", Boolean.TRUE);
		subscriber.addChannel("KBS_ACT_channel", Boolean.TRUE);
		subscriber.addChannel("User_to_Ontology_channel", Boolean.TRUE);
		System.out.println("end");


	}

}
