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
