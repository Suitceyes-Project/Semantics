package iti.suitceyes.util;

public class Report {
	
	private String sensor;
	private String entityName;
	private String entityType;
	
	private Boolean reportedValue; //True (found), False (not found)
	private String reportedTimestamp;
	private String reportedDistance;
	private String reportedXPosition;
	private String reportedScene;

	public Report(String sensor, String entityName, String entityType, 
			Boolean reportedValue, 
			String reportedTimestamp, String reportedDistance, 
			String reportedXPosition, String reportedScene) {
		
		this.setSensor(sensor);
		this.setEntityName(entityName);
		this.setEntityType(entityType);
		this.setReportedValue(reportedValue);
		this.setReportedTimestamp(reportedTimestamp);
		this.setReportedDistance(reportedDistance);
		this.setReportedXPosition(reportedXPosition);
		this.setReportedScene(reportedScene);
		
	}
	
//
//
	public Report(String sensor, String entityName, String entityType, Boolean reportedValue){
		this.setSensor(sensor);
		this.setEntityName(entityName);
		this.setEntityType(entityType);
		this.setReportedValue(reportedValue);
		this.setReportedTimestamp(null);
		this.setReportedDistance(null);
		this.setReportedXPosition(null);
		this.setReportedScene(null);
	}
	

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Boolean getReportedValue() {
		return reportedValue;
	}

	public void setReportedValue(Boolean reportedValue) {
		this.reportedValue = reportedValue;
	}

	public String getReportedTimestamp() {
		return reportedTimestamp;
	}

	public void setReportedTimestamp(String reportedTimestamp) {
		this.reportedTimestamp = reportedTimestamp;
	}
	
	public String getReportedDistance(){
		return reportedDistance;
	}
	
	public void setReportedDistance(String reportedDistance) {
		this.reportedDistance = reportedDistance;
	}
	
	public String getReportedXPosition(){
		return reportedXPosition;
	}
	
	public void setReportedXPosition(String reportedXPosition) {
		this.reportedXPosition = reportedXPosition;
	}
	
	public String getReportedScene(){
		return reportedScene;
	}
	
	public void setReportedScene(String reportedScene) {
		this.reportedScene = reportedScene;
	}

}
