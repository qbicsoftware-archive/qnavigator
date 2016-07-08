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

    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = false;
    CheckListPanel clp = new CheckListPanel("Checklist");

    @Override
    public boolean checkConditions() {
      // TODO Auto-generated method stub
      isChecked = clp.allChecked();
      sampleStatesLogging.debug("Conditions for MSH_SURGERY_SAMPLE_TAKEN were checked: " + isChecked);
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      //layoutToInject = new HorizontalLayout();
      layoutToInject.removeAllComponents();
      String[] strlist = {"Tumor punctions finished.", "Tumor sample is on dry ice, ready for transport."};

      clp.setContentDescription("Please check the following requirements before proceeding to the next state!");
      clp.buildCheckList(strlist);

      layoutToInject.addComponent(clp);

      layoutToInject.setSpacing(true);
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_SENT_TO_PATHOLOGY {
    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = false;
    CheckListPanel clp = new CheckListPanel("Checklist");

    @Override
    public boolean checkConditions() {
      // TODO Auto-generated method stub
      isChecked = clp.allChecked();
      sampleStatesLogging.debug("Conditions for MSH_SENT_TO_PATHOLOGY were checked: " + isChecked);
      
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      layoutToInject.removeAllComponents();
      String[] strlist = {"Tumor sample arrived at pathology.", "Tumor sample is prepared and ready for pathological review."};

      clp.setContentDescription("Please check the following requirements before proceeding to the next state!");
      clp.buildCheckList(strlist);

      layoutToInject.addComponent(clp);
      
      layoutToInject.setSpacing(true);
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_PATHOLOGY_REVIEW_STARTED {
    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = false;
    CheckListPanel clp = new CheckListPanel("Checklist");
    
    @Override
    public boolean checkConditions() {
      isChecked = clp.allChecked();
      sampleStatesLogging.debug("Conditions for MSH_PATHOLOGY_REVIEW_STARTED were checked: " + isChecked);
      
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      layoutToInject.removeAllComponents();
      String[] strlist = {"Tumor sample cut performed.", "Pathological review written."};

      clp.setContentDescription("Please check the following requirements before proceeding to the next state!");
      clp.buildCheckList(strlist);

      layoutToInject.addComponent(clp);
      
      layoutToInject.setSpacing(true);
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_PATHOLOGY_REVIEW_FINISHED {
    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = false;
    CheckListPanel clp = new CheckListPanel("Checklist");

    @Override
    public boolean checkConditions() {
      isChecked = clp.allChecked();
      sampleStatesLogging.debug("Conditions for MSH_PATHOLOGY_REVIEW_FINISHED were checked: " + isChecked);
      
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      layoutToInject.removeAllComponents();
      String[] strlist = {"Tumor sample is on dry ice, ready for transport."};

      clp.setContentDescription("Please check the following requirements before proceeding to the next state!");
      clp.buildCheckList(strlist);

      layoutToInject.addComponent(clp);
      
      layoutToInject.setSpacing(true);
    }

    @Override
    public HorizontalLayout getUserInterface() {
      // TODO Auto-generated method stub
      return layoutToInject;
    }

  }, MSH_SENT_TO_HUMAN_GENETICS {
    private HorizontalLayout layoutToInject = new HorizontalLayout();
    private boolean isChecked = false;
    CheckListPanel clp = new CheckListPanel("Checklist");
    
    @Override
    public boolean checkConditions() {
      isChecked = clp.allChecked();
      sampleStatesLogging.debug("Conditions for MSH_SENT_TO_HUMAN_GENETICS were checked: " + isChecked);
      
      return isChecked;
    }

    @Override
    public void buildUserInterface() {
      // TODO Auto-generated method stub
      layoutToInject.removeAllComponents();

      clp.setContentDescription("Happy End!");
      String[] emptyList = { };
      clp.buildCheckList(emptyList);

      layoutToInject.addComponent(clp);
      
      layoutToInject.setSpacing(true);
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

    if (stateIndex >= 0 && stateIndex < (enumValues.length - 1)) {
      stateIndex += 1;
    }

    return enumValues[stateIndex];
  }

  private static logging.Logger sampleStatesLogging = new Log4j2Logger(MSHBiologicalSampleStates.class);
}
