package qbic.vaadincomponents;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;

public class QuantificationLabelComponent extends CustomComponent{
  private static final long serialVersionUID = -8953869755178358731L;
  private HorizontalLayout mainlayout;
  private OptionGroup light;
  private OptionGroup medium;
  private OptionGroup heavy;

  public QuantificationLabelComponent(){
    mainlayout = new HorizontalLayout();
    mainlayout.setCaption("Labels");
    mainlayout.setSpacing(true);
    light = labels("light labels");
    medium = labels("medium labels");
    heavy = labels("heavy labels");
    setCompositionRoot(mainlayout);
  }
  
  public void noLables() {
    mainlayout.setVisible(false);
  }

  public void lightAndHeavyLabels() {
    mainlayout.setVisible(true);
    mainlayout.removeAllComponents();
    mainlayout.addComponent(light);
    mainlayout.addComponent(heavy);
  }

  public void lightMediumAndHeavyLabels() {
    mainlayout.setVisible(true);
    mainlayout.removeAllComponents();
    mainlayout.addComponent(light);
    mainlayout.addComponent(medium);
    mainlayout.addComponent(heavy);
    
  }
  OptionGroup labels(String caption){
    OptionGroup labels = new OptionGroup(caption);
    labels.setMultiSelect(true);
    labels.addItems("Arg6", "Arg10", "Leu7", "Averigin");
    return labels;  
  }
  OptionGroup getLightLabels(){
    return light;
  }
  OptionGroup getMediumLabels(){
    return medium;
  }
  OptionGroup getHeavyLabels(){
    return heavy;
  }
  
  
}
