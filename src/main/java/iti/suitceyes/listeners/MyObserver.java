package iti.suitceyes.listeners;

import java.util.Observable;
import java.util.Observer;

public class MyObserver implements Observer{

	public void observe(Observable o) {
		    o.addObserver(this);
		  }
	
	@Override
	public void update(Observable o, Object arg) {
		int someVariable = ((MessageBin) o).getSomeVariable();
	    System.out.println("Some variable is now " + someVariable);
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
