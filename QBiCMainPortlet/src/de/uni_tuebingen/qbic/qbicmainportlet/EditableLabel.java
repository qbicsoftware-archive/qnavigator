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

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class EditableLabel extends VerticalLayout {
  /**
   * 
   */
  private static final long serialVersionUID = -2770589094081457177L;
  private Label label = new Label();
  private TextField textField = new TextField();

  public EditableLabel(String value) {
    label.setValue(value);
    label.setSizeUndefined();
    textField.setPropertyDataSource(label);
    setDescription("Double click field to change value");
    addComponent(label);
    addListeners();

  }

  private void addListeners() {


    addLayoutClickListener(new LayoutClickListener() {

      @Override
      public void layoutClick(LayoutClickEvent event) {
        if (event.isDoubleClick() && event.getClickedComponent() instanceof Label) {
          removeComponent(label);
          addComponent(textField);
          textField.focus();
        }
      }
    });

    textField.addBlurListener(new BlurListener() {

      @Override
      public void blur(BlurEvent event) {
        removeComponent(textField);
        addComponent(label);
      }
    });
  }

  public void setValue(String desc) {
    label.setValue(desc);
  }

  public TextField getTextField() {
    return textField;
  }

  public void setTextField(TextField textField) {
    this.textField = textField;
  }
}
