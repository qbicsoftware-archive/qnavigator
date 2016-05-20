/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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

  public void resetStateMachine(String sampleID) {
    String toState = "MSH_UNDEFINED_STATE";
    updateOpenBISCurrentProcessState(sampleID, toState);
    //setState(retrieveCurrentStateFromOpenBIS());
    sampleViewRef.updateContent();
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
    
    Button resetButton = new Button("RESET BUTTON");

    resetButton.addClickListener(new Button.ClickListener() {
      public void buttonClick(ClickEvent event) {
        resetStateMachine(sampleID);

      }
    });
    

    injectLayout.addComponent(nextButton);
    injectLayout.addComponent(resetButton);

  }

  public HorizontalLayout getCurrentInterface() {
    return injectLayout;
  }

  public boolean traverseToNextState(String sampleID) {
    // first, check if all conditions are met before traversing into next state
    String fromState = new String(currentState.name());
    String toState = new String(currentState.nextState().name());
    
    if (fromState.equals(toState)) {
      // nothing to do... however, we should notify the user
      
      Notification errorEndStateReached = new Notification("The current process seems to have reached it's end state.",
          "<i>Skipping this transition with no changes performed...</i>",
          Type.WARNING_MESSAGE, true);

      errorEndStateReached.setHtmlContentAllowed(true);
      errorEndStateReached.show(Page.getCurrent());
      
      return false;
    }
    
    
    if (currentState.checkConditions()) {
      

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
      sampleViewRef.updateContent();
      
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
