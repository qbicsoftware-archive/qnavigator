package qbic.vaadincomponents;

import java.util.HashMap;

import qbic.model.maxquant.Group;
import qbic.model.maxquant.GroupSpecificParameters;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


/**
 * extends vaadins CustomComponent and represents the UI of Maxquants Group spefic parameters which
 * define parameters for a group of raw files. The groups can be updated via the update method.
 * 
 * @author wojnar
 * 
 */
public class GroupSpecificParameterComponent extends CustomComponent {
  private static final long serialVersionUID = -1912407218446455308L;

  public static String CAPTION = "Group-specific parameters";
  private NativeSelect groupSelection;

  private NativeSelect typeSelect;

  private QuantificationLabelComponent labels;

  private FormLayout parameterlayout;

  private TwinColSelect variableModifications;

  private NativeSelect digestionMode;

  private TwinColSelect enzyme;

  private TextField missedcleavage;

  private HashMap<Integer, Group> groups;

  private NativeSelect multiplicitySelect;

  private NativeSelect matchType;

  public GroupSpecificParameterComponent() {

    this.setCaption(CAPTION);


    this.setCompositionRoot(init());

  }

  public Component init() {
    // Group specific parameters

    FormLayout groupSpecificParameters = new FormLayout();

    // does not need to be initialized more than once.
    if (groupSelection == null) {
      groupSelection = new NativeSelect("Group:");
      groupSelection.setNullSelectionAllowed(false);
      groupSpecificParameters.addComponent(groupSelection);
      groupSelection.addValueChangeListener(new ValueChangeListener() {
        private static final long serialVersionUID = -2948673674040494963L;

        @Override
        public void valueChange(ValueChangeEvent event) {
          if(event.getProperty() != null && event.getProperty().getValue() != null){
            updateParameters((Integer) event.getProperty().getValue());
          } 
        }
      });
    }
    parameterlayout = initParameters();
    groupSpecificParameters.addComponent(parameterlayout);
    parameterlayout.setVisible(false);
    return groupSpecificParameters;
  }


  private FormLayout initParameters() {
    // select the type
    FormLayout parameterLayout = new FormLayout();
    typeSelect = new NativeSelect();
    typeSelect.setCaption("Type");
    typeSelect.addItem("Standard");
    typeSelect.addItem("Reporter Ion");
    // add it
    parameterLayout.addComponent(typeSelect);
    // select multiplicity.
    multiplicitySelect = new NativeSelect();
    multiplicitySelect.setCaption("Multiplicity");
    multiplicitySelect.addItem("1 - label-free quantification");
    multiplicitySelect.addItem("2 - light and heavy labels");
    multiplicitySelect.addItem("3 - light, medium, and heavy lables");
    multiplicitySelect.setNullSelectionAllowed(false);
    parameterLayout.addComponent(multiplicitySelect);
    // add labels
    labels = new QuantificationLabelComponent();
    parameterLayout.addComponent(labels);
    multiplicitySelect.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = 2788813707970284834L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        String multiplicity = (String) event.getProperty().getValue();
        if (multiplicity.startsWith("1")) {
          labels.noLables();
        } else if (multiplicity.startsWith("2")) {
          labels.lightAndHeavyLabels();
        } else if (multiplicity.startsWith("3")) {
          labels.lightMediumAndHeavyLabels();
        }

      }
    });
    parameterLayout.addComponent(labels);

    // variable modifications
    variableModifications = new TwinColSelect("variable modifications");
    
    variableModifications.addItems("Acetyl (L)", "Oxidation (M)", "Ala->Arg");
    parameterLayout.addComponent(variableModifications);
    // digestion mode
    digestionMode = new NativeSelect("Digestion mode");
    digestionMode.addItem("Specific");
    parameterLayout.addComponent(digestionMode);
    // enzyme
    enzyme = new TwinColSelect("Enzyme");
    enzyme.addItems("Trypsin/P", "ArgC", "Trypsin", "GluN");
    parameterLayout.addComponent(enzyme);
    // missed cleavage
    missedcleavage = new TextField("max missed cleavage");
    parameterLayout.addComponent(missedcleavage);
    matchType = new NativeSelect("Match type");
    matchType.addItem("MatchFromAndTo");
    parameterLayout.addComponent(matchType);
    return parameterLayout;
  }

  public void update(HashMap<Integer, Group> groups) {
    groupSelection.removeAllItems();
    this.resetParams();
    parameterlayout.setVisible(false);
    if (groups == null || groups.isEmpty()) return;
    for (Integer key : groups.keySet()) {
      groupSelection.addItem(key);
    }
    this.groups = groups;
  }

  private void resetParams() {
    // TODO Auto-generated method stub

  }

  void updateParameters(int value) {
    parameterlayout.setVisible(true);
    BeanFieldGroup<GroupSpecificParameters> binder =
        new BeanFieldGroup<GroupSpecificParameters>(GroupSpecificParameters.class);
    binder.setItemDataSource(groups.get(value).getParameters());
    binder.bind(typeSelect, "type");
    binder.bind(multiplicitySelect, "multiplicity");
    binder.bind(variableModifications, "variableModifications");
    binder.bind(digestionMode, "digestionMode");
    binder.bind(enzyme, "enzymes");
    binder.bind(missedcleavage, "maxMissedCleavage");
    // binder.bind(TODO, "matchType");
    binder.bind(labels.getLightLabels(), "lightLabels");
    binder.bind(labels.getMediumLabels(), "mediumLabels");
    binder.bind(labels.getHeavyLabels(), "heavyLabels");
    binder.bind(matchType, "matchType");

    // update values immediately
    binder.setBuffered(false);
  }
  
  

}
