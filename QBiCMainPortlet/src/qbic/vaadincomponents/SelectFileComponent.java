package qbic.vaadincomponents;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;

public class SelectFileComponent extends CustomComponent {
  private static final long serialVersionUID = -8740268090810853563L;
  private Button toLeft;
  private Button toRight;
  private Grid available;
  private Grid selected;

  public SelectFileComponent(String mainCaption, String info, String sourceCaption, String destinationCaption, BeanItemContainer<?> source, BeanItemContainer<?> destination){
    setCaption(mainCaption);
    VerticalLayout files = new VerticalLayout();
    files.setSpacing(true);
    

    // info label
    Label rawFilesInfo = new Label(info);
    rawFilesInfo.addStyleName(ValoTheme.LABEL_COLORED);
    files.addComponent(rawFilesInfo);

    // available files in openbis
    available = new Grid(source);
    available.setCaption(sourceCaption);
    available.setSelectionMode(SelectionMode.MULTI);
    // selected files for anaylsis
    selected = new Grid(destination);

    selected.setCaption(destinationCaption);
    selected.setSelectionMode(SelectionMode.MULTI);

    // selectedFiles.set
    // buttons to add or remove files
    VerticalLayout buttons = new VerticalLayout();
    toLeft = new Button();
    toLeft.setIcon(FontAwesome.ARROW_LEFT);

    toRight = new Button();
    toRight.setIcon(FontAwesome.ARROW_RIGHT);
    buttons.addComponent(toRight);
    buttons.addComponent(toLeft);

    HorizontalLayout grids = new HorizontalLayout();
    grids.setSpacing(true);
    grids.addComponent(available);
    grids.addComponent(buttons);
    grids.addComponent(selected);
    grids.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);

    files.addComponent(grids);
    
    this.setCompositionRoot(files);
    
  }

  /**
   * returns the button that should move files from left to right. Basically add to destination
   * @return
   */
  public Button getToRightButton() {
    return toRight;
  }
  /**
   * returns the button to should move files from right to left. Basically remove from destination
   * @return
   */ 
  public Button getToLeftButton() {
    return toLeft;
  }

  public Grid getSource() {
    return available;
  }

  public Grid getDestination() {
    return selected;
  }

  
}
