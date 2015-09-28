package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.NewIvacSampleBean;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.google.gwt.validation.client.constraints.NotNullValidator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class AddPatientView extends VerticalLayout implements View {

  static String navigateToLabel = "addivacproject";

  VerticalLayout addPatientViewContent;

  private String resourceUrl;
  private DataHandler datahandler;
  private State state;

  private Button registerPatients;

  private VerticalLayout buttonLayoutSection;

  private CustomVisibilityComponent projects;
  private ComboBox typingMethod = new ComboBox("Typing Method");
  private CustomVisibilityComponent numberOfPatients;
  private CustomVisibilityComponent secondaryNames;
  private CustomVisibilityComponent description;
  private CheckBox registerHLAI = new CheckBox("MHC Class I");
  private CheckBox registerHLAII = new CheckBox("MHC Class II");
  private VerticalLayout hlaTypingSection;

  private TextArea hlaItypes = new TextArea();
  private TextArea hlaIItypes = new TextArea();

  private String header;


  private BeanItemContainer sampleOptions = new BeanItemContainer<NewIvacSampleBean>(
      NewIvacSampleBean.class);

  private VerticalLayout optionLayoutSection;

  private CustomVisibilityComponent hlaInfo;

  private CustomVisibilityComponent gridInfo;



  public AddPatientView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }

  public AddPatientView(DataHandler datahandler, State state) {
    this.datahandler = datahandler;
    this.state = state;
    resourceUrl = "javascript;";
    sampleOptions.addBean(new NewIvacSampleBean("Normal", 1, "", false, false, false, "", ""));
    sampleOptions.addBean(new NewIvacSampleBean("Tumor", 1, "", false, false, false, "", ""));

    initView();
  }
  
  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * updates view, if height, width or the browser changes.
   * 
   * @param browserHeight
   * @param browserWidth
   * @param browser
   */
  public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
    setWidth((browserWidth * 0.85f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() { 
    header= "";
    
    addPatientViewContent = new VerticalLayout();

    addPatientViewContent.addComponent(initExperimentalSetupLayout());
    addPatientViewContent.addComponent(initOptionLayout());

    addPatientViewContent.addComponent(hlaTypingLayout());

    addPatientViewContent.setWidth("100%");
    addPatientViewContent.setMargin(new MarginInfo(true, false, false, false));
    this.addComponent(addPatientViewContent);
    this.addComponent(initButtonLayout());
  }

  /**
   * 
   * @return
   */
  VerticalLayout initOptionLayout() {
    optionLayoutSection = new VerticalLayout();
    optionLayoutSection.setWidth("100%");
    optionLayoutSection.setVisible(false);

    VerticalLayout optionLayout = new VerticalLayout();

    Button addSample = new Button("Add Sample");
    addSample.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        sampleOptions.addBean(new NewIvacSampleBean("", 0, "", false, false, false, "", ""));
      }
    });

    optionLayout.setMargin(new MarginInfo(true, false, false, false));
    optionLayout.setHeight(null);
    optionLayout.setWidth("100%");
    optionLayout.setSpacing(true);


    final Grid optionGrid = new Grid();
    optionGrid.setWidth("80%");
    // optionGrid.setCaption("Which biological samples are available for the patient(s) and which experiments will be performed?");
    
    gridInfo = new CustomVisibilityComponent(new Label(""));
    ((Label) gridInfo.getInnerComponent()).addStyleName(ValoTheme.LABEL_LARGE);

    Component gridInfoContent =
        Utils.questionize(
            gridInfo,
                "Which biological samples are available for the patient(s) and which experiments will be performed?",
                "Extracted Samples");
    
    // optionGrid.setSelectionMode(SelectionMode.MULTI);
    optionGrid.setEditorEnabled(true);

    optionGrid.setContainerDataSource(sampleOptions);
    optionGrid.setColumnOrder("type", "secondaryName", "tissue", "amount", "dnaSeq", "rnaSeq", "deepSeq");
    
    optionLayout.addComponent(gridInfoContent);
    optionLayout.addComponent(optionGrid);
    optionLayout.addComponent(addSample);

    final GridEditForm form =
        new GridEditForm(datahandler.getOpenBisClient().getVocabCodesForVocab("Q_PRIMARY_TISSUES"),
            datahandler.getOpenBisClient().getVocabCodesForVocab("Q_SEQUENCER_DEVICES"));

    optionLayout.addComponent(form);
    form.setVisible(false);

    optionGrid.addSelectionListener(new SelectionListener() {

      @Override
      public void select(SelectionEvent event) {
        BeanItem<NewIvacSampleBean> item = sampleOptions.getItem(optionGrid.getSelectedRow());
        form.fieldGroup.setItemDataSource(item);
        form.setVisible(true);
      }
    });

    optionLayoutSection.addComponent(optionLayout);
    return optionLayoutSection;
  }

  /**
   * 
   * @return
   */
  VerticalLayout initButtonLayout() {
    registerPatients = new Button("Register Patients");
    //registerPatients.setWidth("100%");
    registerPatients.setStyleName(ValoTheme.BUTTON_FRIENDLY);
    registerPatients.setVisible(false);

    registerPatients.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        callPatientRegistration();
      }
    });

    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setMargin(new MarginInfo(true, false, true, false));
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.setWidth("100%");

    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayout.addComponent(registerPatients);
    
    return buttonLayoutSection;
  }
  
  

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initExperimentalSetupLayout() {
    VerticalLayout projDescriptionContent = new VerticalLayout();

    projDescriptionContent.setWidth("100%");

    projDescriptionContent.setSpacing(true);
    projDescriptionContent.setMargin(new MarginInfo(true, false, false, false));

    List<String> visibleSpaces = new ArrayList<String>();

    for (String space: datahandler.getOpenBisClient().listSpaces()) {
    	if(space.startsWith("IVAC")) {
    		
    	Set<String> users = datahandler.getOpenBisClient().getSpaceMembers(space);
    	
    	if(users.contains(LiferayAndVaadinUtils.getUser().getScreenName())) {
    		visibleSpaces.add(space);
    	}
    	}
    }
    
    //for (Project project : datahandler.getOpenBisClient().getOpenbisInfoService().listProjectsOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), LiferayAndVaadinUtils.getUser().getScreenName())) {
    //  if (project.getSpaceCode().startsWith("IVAC")) {
    //    visibleSpaces.add(project.getSpaceCode());
     // }
    //}
    
    projects = new CustomVisibilityComponent(new ComboBox("Select Project", visibleSpaces));
    ((ComboBox) projects.getInnerComponent()).setImmediate(true);
    
    Component context =
        Utils.questionize(
            projects,
                "Add Patient(s) to the following project",
                "Select Project");
    
    ((ComboBox) projects.getInnerComponent()).addValueChangeListener(new ValueChangeListener() {
      
      @Override
      public void valueChange(ValueChangeEvent event) {
        numberOfPatients.setVisible(true);
      }
    });
      
    projDescriptionContent.addComponent(context);
    
    numberOfPatients = new CustomVisibilityComponent(new TextField("Number of Patients"));  
    numberOfPatients.setVisible(false);
    ((TextField) numberOfPatients.getInnerComponent()).setImmediate(true);

    Component numberContext =
        Utils.questionize(
            numberOfPatients,
                "How many patients with the same setup should be registered?",
                "Number of Patients");
    
    ((TextField) numberOfPatients.getInnerComponent()).addTextChangeListener(new TextChangeListener() {
      
      @Override
      public void textChange(TextChangeEvent event) {
        secondaryNames.setVisible(true);
      }
    });
    
    projDescriptionContent.addComponent(numberContext);

    secondaryNames = new CustomVisibilityComponent(new TextField("Identifiers"));
    ((TextField) secondaryNames.getInnerComponent()).setImmediate(true);

    secondaryNames.setVisible(false);
    Component secondaryContext =
        Utils.questionize(
            secondaryNames,
                "Please provide a list of comma separated IDs.",
                "Identifiers");
    
 ((TextField) secondaryNames.getInnerComponent()).addTextChangeListener(new TextChangeListener() {
      
      @Override
      public void textChange(TextChangeEvent event) {
        description.setVisible(true);
      }
    });
 
    projDescriptionContent.addComponent(secondaryContext);
    
      description = new CustomVisibilityComponent(new TextField("Description"));  
      ((TextField) description.getInnerComponent()).setImmediate(true);
      description.setVisible(false);
      Component descriptionContext =
          Utils.questionize(
              description,
              "Please provide a general description for the new patient cases",
                  "Description");
      
      ((TextField) description.getInnerComponent()).addTextChangeListener(new TextChangeListener() {
        
        @Override
        public void textChange(TextChangeEvent event) {
          optionLayoutSection.setVisible(true);
          hlaTypingSection.setVisible(true);
          registerPatients.setVisible(true);
        }
      });
      projDescriptionContent.addComponent(descriptionContext);

    return projDescriptionContent;
  }

  /**
   * initializes the hla typing registration layout
   * 
   * @return
   */
  VerticalLayout hlaTypingLayout() {

    hlaTypingSection = new VerticalLayout();
    hlaTypingSection.setWidth("100%");
    hlaTypingSection.setVisible(false);


    hlaTypingSection.setMargin(new MarginInfo(true, false, false, false));
    hlaTypingSection.setHeight(null);
    hlaTypingSection.setSpacing(true);
    
    hlaInfo = new CustomVisibilityComponent(new Label("HLA Typing"));
    ((Label) hlaInfo.getInnerComponent()).setHeight("24px");

    Component hlaContext =
        Utils.questionize(
            hlaInfo,
                "Register available HLA typing for this patient (one allele per line)",
                "HLA Typing");
    
    hlaTypingSection.addComponent(hlaContext);

    HorizontalLayout hlalayout = new HorizontalLayout();
    
    VerticalLayout hlaLayout1 = new VerticalLayout(); 
    hlaLayout1.addComponent(registerHLAI);
    hlaLayout1.addComponent(hlaItypes);
    
    VerticalLayout hlaLayout2 = new VerticalLayout(); 
    hlaLayout2.addComponent(registerHLAII);
    hlaLayout2.addComponent(hlaIItypes);
    
    hlalayout.addComponent(hlaLayout1);
    hlalayout.addComponent(hlaLayout2);
    
    hlalayout.setSpacing(true);

    typingMethod.addItems(datahandler.getOpenBisClient().getVocabCodesForVocab("Q_HLA_TYPING_METHODS"));
    hlaTypingSection.addComponent(typingMethod);
    hlaTypingSection.addComponent(hlalayout);

    return hlaTypingSection;
  }

  public void callPatientRegistration() {
    
    List<String> secondaryIDs = Arrays.asList(secondaryNames.getValue().split("\\s*,\\s*"));
    Map<String, List<String>> hlaTyping = new HashMap<String, List<String>>();

    List<String> hlaTypingI = new ArrayList<String>();
    List<String> hlaTypingII = new ArrayList<String>();

    boolean hlaIvalid = true;
    boolean hlaIIvalid = true;
    
    if (registerHLAI.getValue()) {
      if (hlaItypes.getValue() != null & typingMethod.getValue() != null) {
        hlaTypingI.add(hlaItypes.getValue());
        hlaTypingI.add(typingMethod.getValue().toString());
        hlaTyping.put("MHC_CLASS_I", hlaTypingI);
      } else {
        Notification.show("HLA Typing not fully specified.", Type.ERROR_MESSAGE);
        hlaIvalid = false;
      }
    }

    if (registerHLAII.getValue()) {

      if (hlaIItypes.getValue() != null & typingMethod.getValue() != null) {
        hlaTypingII.add(hlaIItypes.getValue());
        hlaTypingII.add(typingMethod.getValue().toString());
        hlaTyping.put("MHC_CLASS_II", hlaTypingII);
      }
      else {
        Notification.show("HLA Typing not fully specified.", Type.ERROR_MESSAGE);
        hlaIIvalid = false;
      }
    }

    Integer numberPatients = Integer.parseInt(numberOfPatients.getValue());
    
    // Notification with default settings for a warning
    Notification sucess = new Notification("Patients successfully registered.", Type.TRAY_NOTIFICATION);
    Notification failure = new Notification("Registration failed. Number of Patients and secondary IDs has to be the same and tissues have to be fully specified.", Type.ERROR_MESSAGE);
    
    // Customize it
    sucess.setDelayMsec(20000);
    sucess.setStyleName(ValoTheme.NOTIFICATION_SUCCESS);
    sucess.setPosition(Position.TOP_CENTER);
    //sucess.setIcon(FontAwesome.CHECK);
    
    failure.setDelayMsec(20000);
    failure.setStyleName(ValoTheme.NOTIFICATION_FAILURE);
    failure.setPosition(Position.TOP_CENTER);
             
    
    if(numberPatients.equals(secondaryIDs.size()) & checkRegisteredSamplesTable() & hlaIvalid & hlaIIvalid) {
      datahandler.registerNewPatients(numberPatients, secondaryIDs, sampleOptions, projects.getValue().toString(), description.getValue(), hlaTyping);
      sucess.show(Page.getCurrent());
    }
    else {
      failure.show(Page.getCurrent());
    }
    
  }
  
  public boolean checkRegisteredSamplesTable() {
    boolean valid = true;
    
    if(sampleOptions.size() == 0) {
      return false;
    }
   

    for (Iterator iter = sampleOptions.getItemIds().iterator(); iter.hasNext();) {
      boolean expsSpecified = false;
      boolean tissueSpecified = false;
      boolean instrumentSpecified = false;
      
      NewIvacSampleBean sampleBean = (NewIvacSampleBean) iter.next();
 
      expsSpecified = ((sampleBean.getDeepSeq() == true) | (sampleBean.getDnaSeq() == true) | (sampleBean.getRnaSeq() == true));
      tissueSpecified = (!sampleBean.getTissue().equals(""));
      instrumentSpecified = (!sampleBean.getSeqDevice().equals(""));
      
      valid = valid & (expsSpecified & tissueSpecified & instrumentSpecified);
    }

    return valid;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    // this.setContainerDataSource(datahandler.getProject(currentValue));
    updateContent();

  }

  private void updateContent() {
    header = "Patient Registration";
    
    registerPatients.setVisible(false);
    numberOfPatients.setVisible(false);
    secondaryNames.setVisible(false);
    description.setVisible(false);
    hlaTypingSection.setVisible(false);
    optionLayoutSection.setVisible(false);
  }

}
