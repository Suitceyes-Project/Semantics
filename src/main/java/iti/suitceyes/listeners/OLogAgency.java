package iti.suitceyes.listeners;

import java.util.HashSet;
import java.util.Observable;

public class OLogAgency extends Observable {
	
	private String logScene;
	private HashSet<String> logObjects;

	public void setScene(String scene) {
        this.logScene = scene;
        setChanged();
        notifyObservers(scene);
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
