/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects. Copyright (C) "2016”
 * Christopher Mohr, David Wojnar, Andreas Friedrich
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.HTMLConverter;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class EditableLabel extends VerticalLayout {
  /**
   * 
   */
  private static final long serialVersionUID = -2770589094081457177L;
  private Label label = new Label();
  private TextArea editArea = new TextArea();

  public EditableLabel(String value) {
    label.setContentMode(ContentMode.HTML);
    if (value.isEmpty()) {
      label.setHeight(100, Unit.PIXELS);
      editArea.setHeight(100, Unit.PERCENTAGE);
    } else {
      label.setHeightUndefined();
      editArea.setHeightUndefined();
    }
    editArea.setConverter(new HTMLConverter());

    editArea.setValue(value);
    label.setValue(editArea.getConvertedValue().toString());

    label.setWidth(100, Unit.PERCENTAGE);
    label.setResponsive(true);
    editArea.setResponsive(true);
    setResponsive(true);
    setMargin(true);
    editArea.setWidth(100, Unit.PERCENTAGE);
    setDescription("Double click to change value");
    addComponent(label);
    addListeners();
  }

  private void addListeners() {


    addLayoutClickListener(new LayoutClickListener() {

      @Override
      public void layoutClick(LayoutClickEvent event) {
        if (event.isDoubleClick() && event.getClickedComponent() instanceof Label) {
          removeComponent(label);
          editArea.setConvertedValue(label.getValue().toString());
          addComponent(editArea);
          editArea.focus();
        }
      }
    });

    editArea.addBlurListener(new BlurListener() {

      @Override
      public void blur(BlurEvent event) {
        removeComponent(editArea);
        label.setValue(editArea.getConvertedValue().toString());
        addComponent(label);
      }
    });
  }

  public String getValue() {
    return editArea.getValue();
  }

  public void setValue(String desc) {
    label.setValue(desc);
  }

  public void addBlurListener(BlurListener l) {
    editArea.addBlurListener(l);
  }

  public TextArea getTextField() {
    return editArea;
  }

  public void setTextArea(TextArea textField) {
    this.editArea = textField;
  }
}
