package iti.suitceyes.components;

import java.util.ArrayList;
import java.util.Iterator;

public class IBComponentResults {

	private String timestamp;
	private ArrayList<IBeacon> iBeaconsFound;

	public IBComponentResults() {
		this.timestamp = "0000-00-00T00:00:00";
		this.iBeaconsFound = new ArrayList<IBeacon>();

	}

	public void addNewIBeacon(IBeacon iBeacon) {
		this.iBeaconsFound.add(iBeacon);
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}


	public String getTimestamp() {
		return this.timestamp;
	}

	public ArrayList<IBeacon> getIBeaconsFound() {
		return this.iBeaconsFound;
	}

	public String iBeaconsFoundToString() {
		String concatResults = "";
		Iterator<IBeacon> iterator = this.iBeaconsFound.iterator();
		while (iterator.hasNext()) {
			IBeacon iBeacon = iterator.next();
			concatResults = concatResults + "\nID: " + iBeacon.getID() + "\tname: " + iBeacon.getIBname()  
					+  "\tpositionX: " + iBeacon.getPositionX() +  "\tpositionY: " + iBeacon.getPositionY() 
					+  "\trssi: " + iBeacon.getRssi() +  "\tdistance: " + iBeacon.getDistance() +  "\tdistance_class: " + iBeacon.getDistance_class();
		}
		
		return concatResults;
	}

	public String toString(){
		
		String resultsToString = "----- IBeacon Component -----\n"
				+ "timestamp: " + this.getTimestamp() + iBeaconsFoundToString();
		
		return resultsToString;
		
	}

}
