package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;

public class State extends Observable {
	
	
	
	public State() {
		
	}
	

	public void notifyObservers(ArrayList<String> message) {
		// TODO Auto-generated method stub
		this.setChanged();
		super.notifyObservers(message);
	}
}
