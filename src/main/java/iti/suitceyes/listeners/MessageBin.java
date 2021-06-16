package iti.suitceyes.listeners;

import java.util.Observable;

public class MessageBin extends Observable {
	  private int someVariable = 0;

	  public void setSomeVariable(int someVariable) {
	    synchronized (this) {
	      this.someVariable = someVariable;
	    }
	    setChanged();
	    notifyObservers();
	  }

	  public synchronized int getSomeVariable() {
	    return someVariable;
	  }
	  
	  public static void main(String[] args) {
			
	  }

}


