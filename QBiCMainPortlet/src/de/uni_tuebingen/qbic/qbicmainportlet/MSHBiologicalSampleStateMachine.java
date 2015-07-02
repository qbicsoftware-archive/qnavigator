package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.UglyToPrettyNameMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import main.OpenBisClient;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class MSHBiologicalSampleStateMachine  {
  MSHBiologicalSampleStates currentState;

  private HorizontalLayout injectLayout = new HorizontalLayout();
  
  private UglyToPrettyNameMapper uglytoPretty = new UglyToPrettyNameMapper();

  private OpenBisClient openbisClient;

  private SampleView sampleViewRef;

  private String sampleID;

  logging.Logger stateMachineLogging = new Log4j2Logger(MSHBiologicalSampleStates.class);

  public MSHBiologicalSampleStateMachine(OpenBisClient obClient, SampleView viewRef) {
    openbisClient = obClient;
    sampleViewRef = viewRef;
  }

  public void setSampleID(String sampleIdentifier) {
    sampleID = sampleIdentifier;
  }

  public String retrieveCurrentStateFromOpenBIS() {
    Map<String,String> sampleProperties = openbisClient.getSampleByIdentifier(sampleID).getProperties();

    return sampleProperties.get("Q_CURRENT_PROCESS_STATE");
  }




  public void setState(String state) {
    try {
      currentState = MSHBiologicalSampleStates.valueOf(state);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      //e.printStackTrace();
      stateMachineLogging.warn(state + ": sample process state was not initialized or state was unknown");
      // set current state to undefined
      this.setState("MSH_UNDEFINED_STATE");
    }

    stateMachineLogging.debug("current state set to:" + currentState.name());

  }

  //  public void setState(String state) {
  //    switch(state) {
  //      case "MSH_UNDEFINED_STATE":
  //        currentState = MSHBiologicalSampleStates.MSH_UNDEFINED_STATE;
  //        break;
  //      case "MSH_SURGERY_SAMPLE_TAKEN":
  //        currentState = MSHBiologicalSampleStates.MSH_SURGERY_SAMPLE_TAKEN;
  //        break;
  //      case "MSH_SENT_TO_PATHOLOGY":
  //        currentState = MSHBiologicalSampleStates.MSH_SENT_TO_PATHOLOGY;
  //        break;
  //      case "MSH_PATHOLOGY_UNDER_REVIEW":
  //        currentState = MSHBiologicalSampleStates.MSH_PATHOLOGY_UNDER_REVIEW;
  //        break;
  //      default:
  //        currentState = MSHBiologicalSampleStates.MSH_UNDEFINED_STATE;   
  //    }
  //  }

  public MSHBiologicalSampleStates getState()
  {
    return currentState;
  }

  public void buildCurrentInterface() {
    

    // reset the layout for update
    injectLayout.removeAllComponents();

    currentState.buildUserInterface();
    injectLayout = currentState.getUserInterface();

    Button nextButton = new Button("Next State");

    nextButton.addClickListener(new Button.ClickListener() {
      public void buttonClick(ClickEvent event) {
        traverseToNextState(sampleID);

      }
    });

    injectLayout.addComponent(nextButton);

  }

  public HorizontalLayout getCurrentInterface() {
    return injectLayout;
  }

  public boolean traverseToNextState(String sampleID) {
    // first, check if all conditions are met before traversing into next state
    if (currentState.checkConditions()) {
      String fromState = new String(currentState.name());
      String toState = new String(currentState.nextState().name());

      stateMachineLogging.debug("traversing from " + fromState + " to " + toState);

      // first check if OpenBIS is still in the currentState
      String mostRecentStateName = retrieveCurrentStateFromOpenBIS();

      if (mostRecentStateName != null && !fromState.equals(mostRecentStateName)) {
        sampleViewRef.updateContent();

        Notification errorStateMoved = new Notification("The sample's status has changed in the meantime!",
            "<i>Most likely, someone else in your group is working on the same data.</i>",
            Type.ERROR_MESSAGE, true);

        errorStateMoved.setHtmlContentAllowed(true);
        errorStateMoved.show(Page.getCurrent());
        // this should redraw the current state

        //this.setState(mostRecentStateName);
        //this.buildCurrentInterface();

        return false;
      }

      updateOpenBISCurrentProcessState(sampleID, toState);
      sampleViewRef.updateMSHBiologicalSampleStateSection();
      
      notifyUsersOfTransition(fromState, toState);

      return true;
    }

    return false;
  }

  private void notifyUsersOfTransition(String fromState, String toState) {
    Map<String, Object> emailData = new HashMap<String, Object>();
    emailData.put("fromState", uglytoPretty.getPrettyName(fromState));
    emailData.put("toState", uglytoPretty.getPrettyName(toState));
    emailData.put("sampleCode", sampleViewRef.getCurrentBean().getCode());
    
    this.openbisClient.triggerIngestionService("msh-notify-service", emailData);
  }

  private void updateOpenBISCurrentProcessState(String sampleID, String toState) {
    Map<String, String> statusMap = new HashMap<String, String>(); 
    statusMap.put(sampleID, toState); 

    Map<String, Object> params = new HashMap<String, Object>(); 
    List<String> ids = new ArrayList<String>(statusMap.keySet()); 
    List<String> types = new ArrayList<String>(Arrays.asList("Q_CURRENT_PROCESS_STATE")); 
    params.put("identifiers", ids); params.put("types", types); 
    params.put("Q_CURRENT_PROCESS_STATE", statusMap); 
    openbisClient.ingest("DSS1", "update-sample-metadata", params);
  }


}
