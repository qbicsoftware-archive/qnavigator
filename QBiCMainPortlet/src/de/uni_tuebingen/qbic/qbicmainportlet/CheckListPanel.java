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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class CheckListPanel extends Panel {
  /**
   * 
   */
  private static final long serialVersionUID = 3082824690604972817L;
  VerticalLayout checkList = new VerticalLayout();
  String contentDescription;


  public CheckListPanel(String caption) {
    super(caption);
    // TODO Auto-generated constructor stub
  }


  public void buildCheckList(String[] checkListItems) {
    checkList.removeAllComponents();
    
    if (contentDescription != null) {
      checkList.addComponent(new Label(contentDescription));
    }

    for (final String s : checkListItems) {
      final CheckBox check = new CheckBox(s);

      //      check.addValueChangeListener(new ValueChangeListener() {
      //        
      //        @Override
      //        public void valueChange(ValueChangeEvent event) {
      //          // TODO Auto-generated method stub
      //          allChecked();
      //          
      //          //checkList.addComponent(new Label(s + " was clicked and bool is " + check.getValue().toString()));
      //        }
      //      });

      checkList.addComponent(check);
    }
    checkList.setMargin(true);

    this.setContent(checkList);
  }

  public void setContentDescription(String contentDesc) {
    contentDescription = contentDesc;
  }

  public boolean allChecked() {
    for (Component i : checkList) {
      
      //System.out.println(((AbstractField<Boolean>) i).getValue().toString());
      if (i.getClass().equals(CheckBox.class)) {
        System.out.println(i.getClass() + "");
        CheckBox tmp = (CheckBox) i;
        if (!tmp.getValue()) {
          return false;
        }
      }
    }

    return true;
  }
}
