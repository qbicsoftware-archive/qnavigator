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
  VerticalLayout checkList;
  String contentDescription;
  

  public CheckListPanel(String caption) {
    super(caption);
    // TODO Auto-generated constructor stub
  }

  
  public void buildCheckList(String[] checkListItems) {
    checkList = new VerticalLayout();
   
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
      
      if (!((AbstractField<Boolean>) i).getValue()) {
        return false;
      }
    }
    
    return true;
  }
}
