package iti.suitceyes.ontology;

public class MyOntologyBinding {
	
	String bindingName;
	String bindingValue;	

	public MyOntologyBinding(String bindingName, String bindingValue) {
		this.setBindingName(bindingName);
		this.setBindingValue(bindingValue);
	}
	
	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;		
	}

	public void setBindingValue(String bindingValue) {
		this.bindingValue = bindingValue;
		
	}
	
	public String getBindingName(){
		return this.bindingName;
	}
	
	public String getBindingValue(){
		return this.bindingValue;
	}

}
