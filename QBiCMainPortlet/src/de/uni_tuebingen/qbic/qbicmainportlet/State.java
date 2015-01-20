package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;

import com.vaadin.ui.UI;

public class State extends Observable {
	
	
	
	public State() {
		
	}
	

	public void notifyObservers(ArrayList<String> message) {
		// TODO Auto-generated method stub
		this.setChanged();
		super.notifyObservers(message);
		UI.getCurrent().getNavigator().navigateTo(String.format("%s/%s", message.get(2).toLowerCase(), message.get(1)));
	}
}
