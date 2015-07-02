package de.uni_tuebingen.qbic.qbicmainportlet;

import logging.Log4j2Logger;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public enum MSHBiologicalSampleStates implements SampleState {
  MSH_UNDEFINED_STATE {

    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = true;


    @Override
    public boolean checkConditions() {
      // TODO Auto-generated method stub
      sampleStatesLogging.debug("Conditions for MSH_UNDEFINED_STATE were checked: " + isChecked);
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
//      if (layoutToInject != null) {
//
//      }
      
      //layoutToInject = new HorizontalLayout();
      layoutToInject.removeAllComponents();
      
      Panel textPanel = new Panel("Checklist");
      VerticalLayout panelContent = new VerticalLayout();
      Label initText = new Label("This sample has not been added to the Multiscale HCC workflow yet. Please click 'Next' to do so!");
      panelContent.addComponent(initText);
      panelContent.setMargin(true);
      textPanel.setContent(panelContent);

      layoutToInject.addComponent(textPanel);
      layoutToInject.setSpacing(true);

      return ;
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }


  }, MSH_SURGERY_SAMPLE_TAKEN {

    private HorizontalLayout layoutToInject;

    @Override
    public boolean checkConditions() {
      // TODO Auto-generated method stub
      System.out.println("This is the MSH_SURGERY_SAMPLE_TAKEN");
      return false;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      layoutToInject = new HorizontalLayout();

      CheckListPanel clp = new CheckListPanel("TEST");
      String[] strlist = {"eins", "zwei", "drei"};
      
      clp.setContentDescription("Please check the following requirements before proceeding to the next state!");
      clp.buildCheckList(strlist);

      layoutToInject.addComponent(clp);
      
      layoutToInject.addComponent(new Label("Hallo hier ist Klaus!"));
      layoutToInject.setSpacing(true);
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_SENT_TO_PATHOLOGY {
    private HorizontalLayout layoutToInject;

    @Override
    public boolean checkConditions() {
      // TODO Auto-generated method stub
      System.out.println("This is the MSH_SENT_TO_PATHOLOGY");
      return false;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub

    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_PATHOLOGY_REVIEW_STARTED {
    private HorizontalLayout layoutToInject;

    @Override
    public boolean checkConditions() {
      System.out.println("This is the MSH_PATHOLOGY_UNDER_REVIEW");
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      this.layoutToInject = new HorizontalLayout();
      
      this.layoutToInject.addComponent(new Label("the new stuff"));
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  };

  private static MSHBiologicalSampleStates[] enumValues = values();
  
  public MSHBiologicalSampleStates nextState() {
    int stateIndex = this.ordinal();
    
    if (stateIndex >= 0 && stateIndex < enumValues.length) {
      stateIndex += 1;
    }
    
    return enumValues[stateIndex];
  }
  
  private static logging.Logger sampleStatesLogging = new Log4j2Logger(MSHBiologicalSampleStates.class);
}
