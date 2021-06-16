package iti.suitceyes.components;

public class IBeacon {
	
	private Integer ID;
	private String IBname;
	private Float rssi;
	private Float positionX, positionY;
	private Float distance;
	private String distance_class;

	public IBeacon(Integer ID, String IBname, Float positionX, Float positionY, Float rssi, Float distance, String distance_class) {
		this.setID(ID);
		this.setIBname(IBname);
		this.setPositionX(positionX);
		this.setPositionY(positionY);
		this.setRssi(rssi);
		this.setDistance(distance);
		this.setDistance_class(distance_class);
	}
	

	public Integer getID() {
		return ID;
	}


	public void setID(Integer ID) {
		this.ID = ID;
	}


	public String getIBname() {
		return IBname;
	}


	public void setIBname(String IBname) {
		this.IBname = IBname;
	}


	public Float getRssi() {
		return rssi;
	}


	public void setRssi(Float rssi) {
		this.rssi = rssi;
	}


	public Float getPositionX() {
		return positionX;
	}


	public void setPositionX(Float positionX) {
		this.positionX = positionX;
	}
	
	public Float getPositionY() {
		return positionX;
	}


	public void setPositionY(Float positionY) {
		this.positionX = positionY;
	}


	public Float getDistance() {
		return distance;
	}


	public void setDistance(Float distance) {
		this.distance = distance;
	}


	public String getDistance_class() {
		return distance_class;
	}


	public void setDistance_class(String distance_class) {
		this.distance_class = distance_class;
	}

}
