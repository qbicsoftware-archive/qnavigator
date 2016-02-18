package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import logging.Log4j2Logger;

import com.vaadin.ui.UI;

public class State extends Observable implements Serializable {



  /**
   * 
   */
  private static final long serialVersionUID = -8448995433087062650L;
  private logging.Logger LOGGER = new Log4j2Logger(State.class);

  public void notifyObservers(ArrayList<String> message) {

    try {
      String message2 = message.get(2).toLowerCase();
      if (message.get(1).contains("IVAC") && (message2.equals("project"))) {
        UI.getCurrent().getNavigator()
            .navigateTo(String.format("ivac%s/%s", message2, message.get(1)));
      } else {
        UI.getCurrent().getNavigator().navigateTo(String.format("%s/%s", message2, message.get(1)));
      }
      this.setChanged();
      super.notifyObservers(message);
    } catch (IllegalArgumentException e) {
      LOGGER.error(
          String.format("message1: %s, message2: %s, current View: %s.", message.get(1),
              message.get(2), "not available"), e.getStackTrace());
    }
  }

  public State() {

  }
}
