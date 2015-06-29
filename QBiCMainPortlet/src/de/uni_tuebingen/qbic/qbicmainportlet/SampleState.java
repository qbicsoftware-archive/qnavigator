package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public interface SampleState {

  /**
   * check if sample state conditions are fulfilled to allow the transition into the next state
   */
  public boolean checkConditions();
  
  
  /**
   * constructs the VAADIN interface specific to this state
   */
  public void buildUserInterface();
  
  public HorizontalLayout getUserInterface();
}
