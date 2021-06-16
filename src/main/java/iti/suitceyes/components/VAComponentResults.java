package iti.suitceyes.components;

import java.util.ArrayList;
import java.util.Iterator;

public class VAComponentResults {

	private String sceneType;
	private Float sceneScore;
	private String timestamp;
	private String imageName;
	private Integer imageWidth;
	private Integer imageHeight;
	private ArrayList<VAEntity> entitiesFound;

	public VAComponentResults() {
		this.sceneType = "";
		this.sceneScore = null;
		this.timestamp = "0000-00-00T00:00:00";
		this.imageName = "";
		this.imageWidth = null;
		this.imageWidth = null;
		this.entitiesFound = new ArrayList<VAEntity>();

	}

	public void addNewVAEntity(VAEntity entity) {
		this.entitiesFound.add(entity);
	}

	public void addNewVAEntity(String type, Float confidence, Float distance, Integer top, Integer left, Integer width, Integer height) {
		
//		for (VAEntity entity:this.entitiesFound){
//			if (entity.getType().equals(type)){
//				entity.incrementTotalNo();//if entity already exists in list (and found) then increase counter
//				return;
//			}			
//		}
		VAEntity entity = new VAEntity(type, confidence, 1, distance, top, left, width, height);
		entity.setPositionInXGridSpace(this.imageWidth);
		this.entitiesFound.add(entity);
	}
	
	public void setImageWidth(Integer width){
		this.imageWidth = width;
	}
	
	public void setImageHeight(Integer height){
		this.imageHeight = height;
	}
	
	public Integer getImageWidth(){
		return this.imageWidth;
	}
	
	public Integer getImageHeight(){
		return this.imageHeight;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	

	public String getTimestamp() {
		return this.timestamp;
	}

	public String getImageName() {
		return this.imageName;
	}

	public void setSceneType(String sceneType) {
		this.sceneType = sceneType;
	}
	
	public String getSceneType() {
		return this.sceneType;
	}
	
	public void setSceneScore(Float sceneScore){
		this.sceneScore = sceneScore;
	}

	
	public Float getSceneScore(){
		return this.sceneScore;
	}


	public ArrayList<VAEntity> getEntitiesFound() {
		return this.entitiesFound;
	}

	public String entitiesFoundToString() {
		String concatResults = "";
		Iterator<VAEntity> iterator = this.entitiesFound.iterator();
		while (iterator.hasNext()) {
			VAEntity vaEntity = iterator.next();
			concatResults = concatResults + "\ntype: " + vaEntity.getType() + "\tconfidence: " + vaEntity.getConfidence();
		}
		
		return concatResults;
	}

	public String toString(){
		
		String resultsToString = "----- VA Component -----\n"
				+ "timestamp: " + this.getTimestamp() + "\n" + "scene_type: " + getSceneType() + "\n" + "scene_score: " + getSceneScore().toString() + "image_name: " + getImageName() + "\n"
				+ entitiesFoundToString();
		
		return resultsToString;
		
	}

}
