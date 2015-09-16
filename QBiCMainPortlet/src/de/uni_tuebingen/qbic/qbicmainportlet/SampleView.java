package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.UglyToPrettyNameMapper;
import helpers.Utils;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import model.SampleBean;

import org.tepi.filtertable.FilterTreeTable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import controllers.MultiscaleController;

public class SampleView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private logging.Logger LOGGER = new Log4j2Logger(SampleView.class);

  public final static String navigateToLabel = "sample";
  FilterTreeTable table;
  VerticalLayout vert;
  private HierarchicalContainer datasets;
  private Button export;

  private DataHandler datahandler;
  private String resourceUrl;
  private State state;
  private VerticalLayout sampview_content;
  private VerticalLayout buttonLayoutSection;
  private VerticalLayout biologicalSampleStateSection;
  private HorizontalLayout stateInjectLayout;
  private VerticalLayout sampleStateSectionContent;

  //private MSHBiologicalSampleStateMachine stateMachine;
  private UglyToPrettyNameMapper uglyToPretty = new UglyToPrettyNameMapper();

  private FileDownloader fileDownloader;
  private SampleBean currentBean;
  private ToolBar toolbar;
  private MenuItem downloadCompleteProjectMenuItem;
  private MenuItem datasetOverviewMenuItem;
  private MenuItem createBarcodesMenuItem;
  private Label sampleNameLabel;
  private Label sampleTypeLabel;
  private Label sampleParentLabel;
  private Label numberOfDatasetsLabel;
  private Label lastChangedDatasetLabel;
  private Label propertiesLabel;
  private Label experimentalFactorLabel;
  private Label currentSampleStateName;
  
  private String header;
  public String getHeader() {
    return header;
  }


  public void setHeader(String header) {
    this.header = header;
  }


  private VerticalLayout notesContent;
  private MultiscaleController controller;
  private MultiscaleComponent noteComponent;

  private TabSheet sampview_tab;

private HorizontalLayout tableSectionContent;

private Label sampleExtId;

private DatasetComponent datasetComponent;

  public SampleView(DataHandler datahandler, State state, String resourceurl, MultiscaleController controller) {
    this(datahandler, state, controller);
    this.resourceUrl = resourceurl;
  }


  public SampleView(DataHandler datahandler, State state, MultiscaleController controller) {
    this.controller = controller;
    this.datahandler = datahandler;
    this.state = state;
    resourceUrl = "javascript;";
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
    setWidth((browserWidth * 0.85f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() {
    sampview_content = new VerticalLayout();
    sampview_content.setMargin(new MarginInfo(true, true, false, false));
    
    sampview_tab = new TabSheet();
    sampview_tab.addStyleName(ValoTheme.TABSHEET_FRAMED);
    sampview_tab.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
    sampview_tab.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
    
    header = "";
    
    datasetComponent = new DatasetComponent(datahandler, state, resourceUrl);
    
    sampview_tab.addTab(initDescription()).setIcon(FontAwesome.INFO_CIRCLE);
    sampview_tab.addTab(initStatistics()).setIcon(FontAwesome.BAR_CHART_O);
    sampview_tab.addTab(datasetComponent).setIcon(FontAwesome.DATABASE);
    sampview_tab.addTab(initNoteComponent()).setIcon(FontAwesome.PENCIL);

    sampview_tab.setImmediate(true);
    
    sampview_tab.addSelectedTabChangeListener(new SelectedTabChangeListener() {
      
      @Override
      public void selectedTabChange(SelectedTabChangeEvent event) {

         if (event.getTabSheet().getSelectedTab().getCaption().equals("Datasets")) {
          datasetComponent.updateUI(navigateToLabel, getCurrentBean().getId());
        }
      }
    });
      
    
    //sampview_content.addComponent(initToolBar());
    //sampview_content.addComponent(initHeadline());
    sampview_content.addComponent(sampview_tab);
    //sampview_content.addComponent(initDescription());
    //sampview_content.addComponent(initStatistics());
    //sampview_content.addComponent(initTable());
    //sampview_content.addComponent(initButtonLayout());
    //sampview_content.addComponent(initMSHBiologicalSampleStateSection());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    //sampview_content.setWidth("100%");
    this.addComponent(sampview_content);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
    //updateContentToolBar();
    updateHeadline();
    updateContentDescription();
    updateContentStatistics();
    updateContentTable();
    //updateContentButtonLayout();
    //updateMSHBiologicalSampleStateSection();
    updateNoteComponent();
  }

  /**
   * 
   * @return
   */
  Component initButtonLayout() {
    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.addComponent(this.export);
    buttonLayout.setMargin(new MarginInfo(false, false, true, true));
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, true, true));

    return buttonLayoutSection;
  }

  void updateContentButtonLayout() {
    if (fileDownloader != null)
      this.export.removeExtension(fileDownloader);
    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getDatasets()), currentBean.getId());
    fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
  }

  /**
   * 
   * @return
   */
  ToolBar initToolBar() {
    SearchBarView searchBarView = new SearchBarView(datahandler);
    toolbar = new ToolBar(resourceUrl, state, searchBarView);
    toolbar.init();
    return toolbar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentToolBar() {
    Boolean containsData = containsDatasets();
    toolbar.setDownload(containsData);
    toolbar.setWorkflow(containsData);
    toolbar.update(navigateToLabel, currentBean.getId());
  }

  boolean containsDatasets() {
    return currentBean.getDatasets() != null && currentBean.getDatasets().size() > 0;
  }
  
  /**
   * initializes the sampleview header (mainly name of sample)
   * @return
   */
  VerticalLayout initHeadline() {
    VerticalLayout headline = new VerticalLayout();
    headline.setMargin(new MarginInfo(true, false, true, false));
    
    sampleNameLabel = new Label("");
    sampleExtId = new Label("");
    sampleExtId.addStyleName("qlabel-huge");
    sampleNameLabel.addStyleName("qlabel-large");
    
    headline.addComponent(sampleExtId);
    headline.addComponent(sampleNameLabel);
    headline.setMargin(true);
    
    return headline;
  }
  
  void updateHeadline() {
	if(currentBean.getProperties().containsKey("Q_EXTERNALDB_ID") && !"".equals(currentBean.getProperties().get("Q_EXTERNALDB_ID"))){
	  header = String.format("%s\n%s",currentBean.getProperties().get("Q_EXTERNALDB_ID"), currentBean.getCode());
		//sampleExtId.setValue(currentBean.getProperties().get("Q_EXTERNALDB_ID"));
		//sampleNameLabel.setValue(currentBean.getCode());
		//sampleExtId.setVisible(true);
	}else{
	  header = String.format("%s", currentBean.getCode());
		//sampleNameLabel.setValue(currentBean.getCode());
		//sampleExtId.setVisible(false);		
	}
  }
  
  VerticalLayout initNoteComponent() {
    VerticalLayout notesLayout = new VerticalLayout();
    VerticalLayout notesLayoutContent = new VerticalLayout();

    notesContent = new VerticalLayout();
    
    notesContent.setIcon(FontAwesome.NAVICON);
    notesContent.setCaption("Sample Notes");
   
    noteComponent = new MultiscaleComponent(controller);
    notesContent.addComponent(noteComponent);
    notesContent.setMargin(new MarginInfo(true, false, false, true));
    notesLayoutContent.addComponent(notesContent);
    notesLayoutContent.setMargin(new MarginInfo(true, false, true, true));

    notesLayout.addComponent(notesContent);
    notesLayout.setMargin(new MarginInfo(false, false, true, true));
    
    return notesContent;
  }
  
  void updateNoteComponent() {
    noteComponent.updateUI(currentBean.getCode());
    notesContent.removeAllComponents();
    notesContent.addComponent(noteComponent);
  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout sampleDescription = new VerticalLayout();
    VerticalLayout sampleDescriptionContent = new VerticalLayout();
    
    
    // sampleDescriptionContent.setMargin(true);
    sampleDescription.setCaption("");
    
    //sampleDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);
    sampleTypeLabel = new Label("");
    sampleDescriptionContent.addComponent(sampleTypeLabel);
    sampleParentLabel = new Label("", ContentMode.HTML);
    sampleDescriptionContent.addComponent(sampleParentLabel);
    sampleDescriptionContent.setMargin(new MarginInfo(true, false, true, true));
    sampleDescription.addComponent(sampleDescriptionContent);
    sampleDescription.setMargin(new MarginInfo(true, false, true, true));
    return sampleDescription;
  }

  void updateContentDescription() {
    sampleTypeLabel.setValue(String.format("Sample type: %s.", uglyToPretty.getPrettyName(currentBean.getType())));
    sampleParentLabel.setValue(currentBean.getParentsFormattedString());
  }

  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {

    // Statistics of sample
    VerticalLayout statistics = new VerticalLayout();
    HorizontalLayout statContent = new HorizontalLayout();
    statistics.setCaption("Statistics");
    //statContent.setIcon(FontAwesome.BAR_CHART_O);
    numberOfDatasetsLabel = new Label("");
    statContent.addComponent(numberOfDatasetsLabel);
    lastChangedDatasetLabel = new Label("");
    statContent.addComponent(lastChangedDatasetLabel);
    statContent.setMargin(new MarginInfo(true, false, false, true));
    // statContent.setMargin(true);
    // statContent.setSpacing(true);

    statistics.addComponent(statContent);
    // statistics.setMargin(true);


    // Properties of sample
    VerticalLayout properties = new VerticalLayout();
    VerticalLayout propertiesContent = new VerticalLayout();
   // propertiesContent.setCaption("Properties");
   // propertiesContent.setIcon(FontAwesome.LIST_UL);
    propertiesLabel = new Label("", ContentMode.HTML);

    propertiesContent.addComponent(propertiesLabel);
    propertiesContent.setMargin(new MarginInfo(true, false, false, true));

    properties.addComponent(propertiesContent);
    // properties.setMargin(true);
    statistics.addComponent(properties);

    // Experimental factors of sample
    VerticalLayout experimentalFactors = new VerticalLayout();
    VerticalLayout experimentalFactorsContent = new VerticalLayout();
    //experimentalFactorsContent.setCaption("Experimental Factors");
    //experimentalFactorsContent.setIcon(FontAwesome.TH);
    experimentalFactorLabel = new Label("", ContentMode.HTML);
    experimentalFactorsContent.addComponent(experimentalFactorLabel);
    experimentalFactorsContent.setMargin(new MarginInfo(true, false, true, true));

    experimentalFactors.addComponent(experimentalFactorsContent);
    statistics.addComponent(experimentalFactors);
    statistics.setSpacing(true);
    statistics.setMargin(new MarginInfo(true, false, true, true));


    return statistics;
  }


  /**
   * 
   */
  void updateContentStatistics() {
    int numberOfDatasets = currentBean.getDatasets().size();
    numberOfDatasetsLabel.setValue(String.format("%s dataset(s). ", numberOfDatasets));
    if (numberOfDatasets > 0) {

      String lastDataset = "";//"No Datasets available!";
      if (currentBean.getLastChangedDataset() != null) {
        lastDataset = currentBean.getLastChangedDataset().toString();
        lastChangedDatasetLabel.setValue(String.format("Last Change: %s",
            String.format("Dataset added on %s", lastDataset)));
      } else {
        lastChangedDatasetLabel.setValue(lastDataset);
      }
    }

    try {
      propertiesLabel.setValue(currentBean.generatePropertiesFormattedString());
      experimentalFactorLabel.setValue(currentBean.generateXMLPropertiesFormattedString());
    } catch (JAXBException e) {
      LOGGER.error(
          String.format("failed to parse experimental factors for sample %s", currentBean.getId()),
          e);
    }

  }


  VerticalLayout initTable() {
    this.table = this.buildFilterTable();
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSection.setCaption("Experiments");
    //tableSectionContent.setCaption("Registered Experiments");
    //tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(new MarginInfo(true, false, false, true));
    tableSection.setMargin(new MarginInfo(true, false, false, true));
    this.table.setWidth("100%");
    tableSection.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    
    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.addComponent(this.export);
    buttonLayout.setMargin(new MarginInfo(false, false, true, false));
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, true, true));

    tableSection.addComponent(buttonLayoutSection);

    return tableSection;
  }


  void updateContentTable() {
    // Nothing to do here at the moment
    // table is already set in setdataresource
  }

  public void setResourceUrl(String resourceurl) {
    this.resourceUrl = resourceurl;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public String getNavigatorLabel() {
    return navigateToLabel;
  }

  public void setContainerDataSource(SampleBean sampleBean) {
    //this.currentBean = sampleBean;

    this.table.setContainerDataSource(sampleBean.getDatasets());
    this.table.setVisibleColumns(new Object[] {"name", "type", "registrationDate", "fileSize"});

    int rowNumber = this.table.size();

    if (rowNumber == 0) {
      this.table.setVisible(false);
     // this.export.setVisible(false);
      tableSectionContent.removeAllComponents();
      tableSectionContent.addComponent(new Label("No datasets registered."));
    } else {
      tableSectionContent.removeAllComponents();
      tableSectionContent.addComponent(table);
      this.table.setVisible(true);
      //this.export.setVisible(true);
      this.table.setPageLength(Math.min(rowNumber+1, 10));
    }
  }

  private FilterTreeTable buildFilterTable() {
    FilterTreeTable filterTable = new FilterTreeTable();

    // filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);
    filterTable.setMultiSelect(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    if (this.datasets != null) {
      filterTable.setContainerDataSource(this.datasets);
    }

    filterTable.setColumnHeader("name", "Name");
    filterTable.setColumnHeader("type", "Type");
    filterTable.setColumnHeader("registrationDate", "Registration Date");
    filterTable.setColumnHeader("fileSize", "Size");

    return filterTable;
  }

  /*
  VerticalLayout initMSHBiologicalSampleStateSection() {
    stateMachine = new MSHBiologicalSampleStateMachine(datahandler.getOpenBisClient(), this);

    // initialize with wrong state (will be updated immediately)
    stateMachine.setState("MSH_WRONG_STATE");
    currentSampleStateName =
        new Label("<b>Current sample process status:</b> <font color=\"green\">"
            + uglyToPretty.getPrettyName(stateMachine.getState().name()) + "</font>",
            ContentMode.HTML);

    biologicalSampleStateSection = new VerticalLayout();
    sampleStateSectionContent = new VerticalLayout();


    sampleStateSectionContent.setCaption("MultiscaleHCC sample status");
    sampleStateSectionContent.setIcon(FontAwesome.AMBULANCE);
    sampleStateSectionContent.addComponent(currentSampleStateName);
    // sampleStateSectionContent.setMargin(new MarginInfo(false, false, false, true));

    stateInjectLayout = stateMachine.getCurrentInterface();



    sampleStateSectionContent.addComponent(stateInjectLayout);

    biologicalSampleStateSection.addComponent(sampleStateSectionContent);
    biologicalSampleStateSection.setMargin(new MarginInfo(true, false, false, true));

    return biologicalSampleStateSection;
  }
*/
  /*
  void updateMSHBiologicalSampleStateSection() {
    String fullSampleIdentifier = currentBean.getId();
    String sampleType = currentBean.getType();

    // update the sampleID of the state machine before retrieving info from openBIS
    stateMachine.setSampleID(fullSampleIdentifier);
    String currentStateTmp = stateMachine.retrieveCurrentStateFromOpenBIS();
    stateMachine.setState(currentStateTmp);
    stateMachine.buildCurrentInterface();

    // sampleStateSectionContent.removeAllComponents();
    currentSampleStateName.setValue("<b>Current sample process status:</b> <font color=\"green\">"
        + uglyToPretty.getPrettyName(stateMachine.getState().name()) + "</font>");

    sampleStateSectionContent.removeComponent(stateInjectLayout);
    stateInjectLayout = stateMachine.getCurrentInterface();
    sampleStateSectionContent.addComponent(stateInjectLayout);


    if (fullSampleIdentifier.startsWith("/MULTISCALEHCC/")
        && sampleType.equals("Q_BIOLOGICAL_SAMPLE")) {
      biologicalSampleStateSection.setVisible(true);
    } else {
      biologicalSampleStateSection.setVisible(false);
    }
  }
*/

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    // TODO updateContent only if currentExperiment is not equal to newExperiment
    //this.table.unselect(this.table.getValue());
    //this.setContainerDataSource(datahandler.getSample(currentValue));
    
    this.currentBean = datahandler.getSample(currentValue);
    updateContent();
  }

  public SampleBean getCurrentBean() {
    return currentBean;
  }


  /**
   * Enables or disables the component. The user can not interact disabled components, which are
   * shown with a style that indicates the status, usually shaded in light gray color. Components
   * are enabled by default.
   */
  public void setEnabled(boolean enabled) {
    this.export.setEnabled(enabled);
    this.table.setEnabled(enabled);
    // this.createBarcodesMenuItem.getParent().setEnabled(false);
    // this.downloadCompleteProjectMenuItem.getParent().setEnabled(false);
    this.toolbar.setEnabled(enabled);
  }

}
