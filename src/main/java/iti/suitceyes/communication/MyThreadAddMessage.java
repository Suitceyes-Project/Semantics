package iti.suitceyes.communication;

import java.io.IOException;
import java.nio.file.Paths;

import iti.suitceyes.util.MyFileLogger;

public class MyThreadAddMessage extends Thread {
	
	private MyMqttMessage message;
	
	public MyThreadAddMessage(MyMqttMessage myMessage){
//		System.out.println("Thread: AddMessage is created");
		this.message = myMessage;
	}

	public void run(){
		
		MyFileLogger logMessagesArrived = new MyFileLogger(
				Paths.get(".").toAbsolutePath().normalize().toFile().getAbsolutePath() + "\\src\\main\\resources\\",
				"logMessagesArrived.log");
		
		Messaging.messages.add(this.message);

		try {
			logMessagesArrived.appendNewLineWithTimestamp("Message from: " + message.getTopic() + " with timestamp: " + message.getTimestamp() + "arrived now...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
