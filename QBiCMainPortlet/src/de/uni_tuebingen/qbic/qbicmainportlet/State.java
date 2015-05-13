package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import com.vaadin.ui.UI;

public class State extends Observable implements Serializable {



  /**
   * 
   */
  private static final long serialVersionUID = -8448995433087062650L;

	public void notifyObservers(ArrayList<String> message) {
		// TODO Auto-generated method stub
		this.setChanged();
		super.notifyObservers(message);
		if(message.get(1).contains("IVAC") & !(message.get(2).equals("barcodeview"))) {
		  System.out.println(message.get(1) + " " + message.get(2));
	      UI.getCurrent().getNavigator().navigateTo(String.format("ivac%s/%s", message.get(2).toLowerCase(), message.get(1)));
		}
		else {
	        UI.getCurrent().getNavigator().navigateTo(String.format("%s/%s", message.get(2).toLowerCase(), message.get(1)));
		}
	}

  public State() {

  }
}
