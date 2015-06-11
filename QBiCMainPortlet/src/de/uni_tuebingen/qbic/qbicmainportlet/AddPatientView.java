package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.NewIvacSampleBean;

import com.google.gwt.validation.client.constraints.NotNullValidator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AddPatientView extends VerticalLayout implements View {

  static String navigateTolabel = "addivacproject";

  VerticalLayout addPatientViewContent;

  private String resourceUrl;
  private DataHandler datahandler;
  private State state;

  private Button registerPatients;

  private VerticalLayout buttonLayoutSection;

  private ComboBox projects = new ComboBox();
  private ComboBox typingMethod = new ComboBox("Typing Method");
  private TextField numberOfPatients = new TextField();
  private TextField secondaryNames = new TextField();
  private TextField description = new TextField();
  private CheckBox registerHLAI = new CheckBox("MHC Class I");
  private CheckBox registerHLAII = new CheckBox("MHC Class II");

  private TextArea hlaItypes = new TextArea();
  private TextArea hlaIItypes = new TextArea();

  private MenuBar menubar;

  private BeanItemContainer sampleOptions = new BeanItemContainer<NewIvacSampleBean>(
      NewIvacSampleBean.class);


  public AddPatientView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }

  public AddPatientView(DataHandler datahandler, State state) {
    this.datahandler = datahandler;
    this.state = state;
    resourceUrl = "javascript;";
    sampleOptions.addBean(new NewIvacSampleBean("Normal", 1, "", false, false, false, ""));
    sampleOptions.addBean(new NewIvacSampleBean("Tumor", 1, "", false, false, false, ""));

    initView();
  }

  /**
   * updates view, if height, width or the browser changes.
   * 
   * @param browserHeight
   * @param browserWidth
   * @param browser
   */
  public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
    setWidth((browserWidth * 0.6f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() {
    addPatientViewContent = new VerticalLayout();
    addPatientViewContent.addComponent(initMenuBar());

    addPatientViewContent.addComponent(initExperimentalSetupLayout());
    addPatientViewContent.addComponent(initOptionLayout());

    addPatientViewContent.addComponent(hlaTypingLayout());

    addPatientViewContent.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.6f,
        Unit.PIXELS);
    addPatientViewContent.setMargin(true);
    this.addComponent(addPatientViewContent);
    this.addComponent(initButtonLayout());
  }

  /**
   * 
   * @return
   */
  VerticalLayout initOptionLayout() {
    VerticalLayout optionLayoutSection = new VerticalLayout();
    optionLayoutSection.setWidth("100%");

    VerticalLayout optionLayout = new VerticalLayout();

    Button addSample = new Button("Add Sample");
    addSample.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        sampleOptions.addBean(new NewIvacSampleBean("", 0, "", false, false, false, ""));
      }
    });

    optionLayout.setMargin(new MarginInfo(true, false, false, true));
    optionLayout.setHeight(null);
    optionLayout.setWidth("100%");
    optionLayout.setSpacing(true);


    final Grid optionGrid = new Grid();
    // optionGrid.setCaption("Which biological samples are available for the patient(s) and which experiments will be performed?");
    Label gridInfo =
        new Label(
            "Which biological samples are available for the patient(s) and which experiments will be performed?");
    gridInfo.setStyleName("info");
    gridInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    // optionGrid.setSelectionMode(SelectionMode.MULTI);
    optionGrid.setEditorEnabled(true);
    optionGrid.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    optionGrid.setContainerDataSource(sampleOptions);
    optionGrid.setColumnOrder("type", "tissue", "amount", "dnaSeq", "rnaSeq", "deepSeq");

    optionLayout.addComponent(gridInfo);
    optionLayout.addComponent(optionGrid);
    optionLayout.addComponent(addSample);

    final GridEditForm form =
        new GridEditForm(datahandler.openBisClient.getVocabCodesForVocab("Q_PRIMARY_TISSUES"),
            datahandler.openBisClient.getVocabCodesForVocab("Q_SEQUENCER_DEVICES"));

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
    registerPatients.setWidth("100%");
    registerPatients.setStyleName(ValoTheme.BUTTON_FRIENDLY);

    registerPatients.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        callPatientRegistration();
      }
    });

    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.setWidth("100%");

    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayout.addComponent(this.registerPatients);
    return buttonLayoutSection;
  }

  /**
   * 
   * @return
   */
  MenuBar initMenuBar() {
    menubar = new MenuBar();
    menubar.setWidth(100.0f, Unit.PERCENTAGE);
    menubar.addStyleName("user-menu");

    // set to true for the hack below
    menubar.setHtmlContentAllowed(true);
    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_higher.png"));
    downloadProject.addSeparator();
    /*
     * this.downloadCompleteProjectMenuItem = downloadProject .addItem( "<a href=\"" + resourceUrl +
     * "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project</a>"
     * , null);
     */
    // Open DatasetView
    // this.datasetOverviewMenuItem = downloadProject.addItem("Dataset Overview", null);
    downloadProject.addItem("Dataset Overview", null);
    downloadProject.setEnabled(false);
    
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));
    manage.setEnabled(false);

    // Another submenu item with a sub-submenu
    // this.createBarcodesMenuItem = manage.addItem("Create Barcodes", null, null);
    // Another top-level item
    manage.addItem("Create Barcodes", null, null);
    //MenuItem workflows = menubar.addItem("Run workflows", null, null);
    //workflows.setIcon(new ThemeResource("dna_higher.png"));
    //workflows.setEnabled(false);

    // Yet another top-level item
    //MenuItem analyze = menubar.addItem("Analyze your data", null, null);
    //analyze.setIcon(new ThemeResource("graph_higher.png"));
    //analyze.setEnabled(false);
    return menubar;
  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initExperimentalSetupLayout() {
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

    projDescription.setWidth("100%");
    projDescriptionContent.setWidth("100%");

    projDescriptionContent.setSpacing(true);
    projDescriptionContent.setMargin(new MarginInfo(true, false, false, true));

    Label projectInfo = new Label("Add Patient(s) to the following project");
    projectInfo.setStyleName("info");
    projectInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    Label numberInfo = new Label("How many patients with the same setup should be registered?");
    numberInfo.setStyleName("info");
    numberInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    Label namesInfo = new Label("Please provide a list of comma separated IDs");
    namesInfo.setStyleName("info");
    namesInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    Label descInfo = new Label("Please provide a general description for the new patient cases");
    descInfo.setStyleName("info");
    descInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    projDescriptionContent.addComponent(projectInfo);
    projDescriptionContent.addComponent(projects);

    List visibleSpaces = new ArrayList<String>();

    for (String spaceCode : datahandler.openBisClient.listSpaces()) {
      if (spaceCode.startsWith("IVAC")) {
        visibleSpaces.add(spaceCode);
      }
    }

    projects.addItems(visibleSpaces);
    projects.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    projects.setRequired(true);
    projects.setRequiredError("Please choose one project.");
    projects.setImmediate(true);
    
    projDescriptionContent.addComponent(numberInfo);
    projDescriptionContent.addComponent(numberOfPatients);
    numberOfPatients
        .setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    //numberOfPatients.setValue(null);
    numberOfPatients.setRequired(true);
    //numberOfPatients.addValidator(new NullValidator("Please provide the number of patients.", false));
    //numberOfPatients.addValidator(new IntegerValidator("Only integer values are allowed."));
    //numberOfPatients.setValidationVisible(true);
    numberOfPatients.setImmediate(true);
    numberOfPatients.setRequiredError("Please provide the number of patients.");
    numberOfPatients.setNullRepresentation("");
    
    projDescriptionContent.addComponent(namesInfo);
    projDescriptionContent.addComponent(secondaryNames);
    //secondaryNames.setValue(null);
    secondaryNames.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    secondaryNames.setRequired(true);
    //secondaryNames.addValidator(new NullValidator("Please provide a comma separated list of secondary IDs.", false));
    secondaryNames.setImmediate(true);
    secondaryNames.setRequiredError("Please provide a comma separated list of secondary IDs.");
    secondaryNames.setNullRepresentation("");
    
    projDescriptionContent.addComponent(descInfo);
    projDescriptionContent.addComponent(description);
    description.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);
    description.setValue("");

    numberOfPatients.setConverter(new StringToIntegerConverter());
    secondaryNames.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    projDescriptionContent.setCaption("Patient Registration");
    projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);

    return projDescription;
  }

  /**
   * initializes the hla typing registration layout
   * 
   * @return
   */
  VerticalLayout hlaTypingLayout() {

    VerticalLayout hlaTypingSection = new VerticalLayout();
    hlaTypingSection.setWidth("100%");

    VerticalLayout typingLayout = new VerticalLayout();

    typingLayout.setMargin(new MarginInfo(true, false, true, true));
    typingLayout.setHeight(null);
    typingLayout.setWidth("100%");
    typingLayout.setSpacing(true);

    Label hlaInfo =
        new Label("Register available HLA typing for this patient (one allele per line)");
    hlaInfo.setStyleName("info");
    hlaInfo.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f, Unit.PIXELS);

    typingLayout.addComponent(hlaInfo);

    HorizontalLayout hlalayout = new HorizontalLayout();
    hlalayout.addComponent(registerHLAI);
    hlalayout.addComponent(hlaItypes);
    hlalayout.addComponent(registerHLAII);
    hlalayout.addComponent(hlaIItypes);
    hlalayout.setSpacing(true);

    typingMethod.addItems(datahandler.openBisClient.getVocabCodesForVocab("Q_HLA_TYPING_METHODS"));
    typingLayout.addComponent(typingMethod);
    typingLayout.addComponent(hlalayout);

    hlaTypingSection.addComponent(typingLayout);

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
      //datahandler.registerNewPatients(numberPatients, secondaryIDs, sampleOptions, projects.getValue().toString(), description.getValue(), hlaTyping);
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
      
      System.out.println("DeepSeq " + sampleBean.getDeepSeq());
      System.out.println("Tissue " + sampleBean.getTissue());
      
      expsSpecified = ((sampleBean.getDeepSeq() == true) | (sampleBean.getDnaSeq() == true) | (sampleBean.getRnaSeq() == true));
      tissueSpecified = (!sampleBean.getTissue().equals(""));
      instrumentSpecified = (!sampleBean.getSeqDevice().equals(""));
      
      valid = valid & (expsSpecified & tissueSpecified & instrumentSpecified);
      System.out.println(expsSpecified);
      System.out.println(tissueSpecified);
      System.out.println(instrumentSpecified);

    }

    return valid;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();

    System.out.println(currentValue);
    // this.setContainerDataSource(datahandler.getProject(currentValue));
    // updateContent();

  }

}
