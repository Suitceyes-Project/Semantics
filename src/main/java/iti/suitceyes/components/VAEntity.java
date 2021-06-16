package iti.suitceyes.components;

public class VAEntity {
	// Entity: either Object or Person
	
	private String type;
	private Float confidence;
	private Integer totalNo;
	private Float distance;
	//Occupied area
	private Integer top;
	private Integer left;
	private Integer width;
	private Integer height;
	private String positionInXGridSpace;
	private Integer offset;
	private String facemask;
	
	public VAEntity (String type, Float confidence, Integer totalNo, Float distance, Integer top, Integer left, Integer width, Integer height){
		this.type = type;
		this.confidence = confidence;
		this.totalNo = totalNo;
		this.distance = distance;
		this.top = top;
		this.left = left;
		this.width = width;
		this.height = height;
		this.facemask = facemask;
	}
	
	public String getPositionInXGridSpace(){
		return this.positionInXGridSpace;
	}
	
	public String getType(){
		return this.type;
	}
	
	public Float getConfidence(){
		return this.confidence;
	}
	
	public Float getDistance(){
		return this.distance;
	}
	
	public Integer getTotalNo(){
		return this.totalNo;
	}
	
	public Integer getTop(){
		return this.top;
	}
	
	public Integer getLeft(){
		return this.left;
	}
	
	public Integer getWidth(){
		return this.width;
	}

	public String getFacemask() {
		return this.facemask;
	}

	public Integer getHeight(){
		return this.height;
	}

	public void incrementTotalNo() {
		this.totalNo = this.totalNo + 1;
		
	}

	public Integer getOffset(){
		return this.offset;
	}

	public void setPositionInXGridSpace(Integer imageWidth) {
		Integer halfPoint = (int) Math.round(imageWidth/2.0);
		Integer objCenterX = (int) Math.round(this.left + this.width / 2.0);
		Integer offset = halfPoint - objCenterX;

		this.offset = offset;

		double objCenterX_rel = (double) objCenterX / imageWidth;
		if (objCenterX_rel <= 0.45) {
			this.positionInXGridSpace = "left";
		}
		else if (objCenterX_rel > 0.45 && objCenterX_rel <= 0.55) {
			this.positionInXGridSpace = "middle";
		}
		else if (objCenterX_rel > 0.55) {
			this.positionInXGridSpace = "right";
		}
		
//		if (this.left > halfPoint)
//			this.positionInXGridSpace = "right";
//		else{
//
//			Integer leftPart = halfPoint - this.left;
////			Integer rightPart = this.width - leftPart;
//
//			double leftPortion = leftPart * 100.0 / this.width;
//
//			if (leftPortion <= 45.0)
//				this.positionInXGridSpace = "left";
//			else if ((leftPortion > 45.0) && (leftPortion < 55.0))
//				this.positionInXGridSpace = "middle";
//			else if (leftPortion >= 55.0)
//				this.positionInXGridSpace = "right";
//
//		}
		
		
		
		
	}	
	
}
