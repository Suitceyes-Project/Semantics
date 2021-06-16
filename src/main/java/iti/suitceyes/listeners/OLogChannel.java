package iti.suitceyes.listeners;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class OLogChannel implements Observer {
	
	private String logScene;
	private HashSet<String> logObjects;

	public OLogChannel() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public void update(Observable o, Object object) {
		
		
	}

}
