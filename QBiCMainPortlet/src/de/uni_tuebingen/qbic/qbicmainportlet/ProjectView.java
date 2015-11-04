package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentBean;
import model.ProjectBean;

import org.tepi.filtertable.FilterTable;

import submitter.Submitter;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.qbicmainportlet.PatientView.Worker;

@SuppressWarnings("serial")
public class ProjectView extends VerticalLayout implements View {

  public final static String navigateToLabel = "project";

  FilterTable table;
  VerticalLayout projectview_content;
  VerticalLayout buttonLayoutSection;


  private ProjectBean currentBean;

  private Button export;

  private DataHandler datahandler;

  private State state;

  private FileDownloader fileDownloader;

  private String resourceUrl;

  private ToolBar toolbar;

  private Label contact;

  private Label descContent;

  private VerticalLayout status;

  private HorizontalLayout statContent;

  private VerticalLayout graphSectionContent;

  private VerticalLayout membersSection;
  private StringBuilder memberString;

  private TabSheet projectview_tab;

  private HorizontalLayout membersLayout;

  private DatasetComponent datasetComponent;
  
  Map<String, String> members = new TreeMap<String, String>();
  Map<String, String> memberLetters = new HashMap<String, String>();

  private LevelComponent measuredSamplesComponent;

  private BiologicalSamplesComponent biologicalSamplesComponent;

  private LevelComponent resultsComponent;

  private static Logger LOGGER = new Log4j2Logger(ProjectView.class);
  
  private String headerLabel;

  private WorkflowViewController wfController;

  private WorkflowComponent workflowComponent;

private ConfigurationManager manager;

private UploadComponent uploadComponent;

private ProjInformationComponent projectInformation;


  public String getHeaderLabel() {
    return headerLabel;
  }

  public void setHeaderLabel(String headerLabel) {
    this.headerLabel = headerLabel;
  }

  public ProjectView(DataHandler datahandler, State state, String resourceurl, WorkflowViewController wfController, ConfigurationManager manager) {
    this(datahandler, state, wfController);
    this.resourceUrl = resourceurl;
    this.manager = manager;
  }

  public ProjectView(DataHandler datahandler, State state, WorkflowViewController wfController) {
    this.wfController = wfController;
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
    projectview_content = new VerticalLayout();
    projectview_content.setMargin(new MarginInfo(true, true, false, false));
    
    //labelContent = new VerticalLayout();
    //labelContent.setMargin(new MarginInfo(true, false, true, false));
    
    headerLabel = "";
    
    //labelContent.addComponent(headerLabel);
    //projectview_content.addComponent(labelContent);
    
    projectview_tab = new TabSheet();
    projectview_tab.setWidth("100%");
    projectview_tab.setHeight("100%");

    datasetComponent = new DatasetComponent(datahandler, state, resourceUrl);
    biologicalSamplesComponent = new BiologicalSamplesComponent(datahandler, state, resourceUrl, "Biological Samples");
    measuredSamplesComponent = new LevelComponent(datahandler, state, resourceUrl, "Measured Samples");
    resultsComponent = new LevelComponent(datahandler, state, resourceUrl, "Results");
    workflowComponent = new WorkflowComponent(wfController);
    uploadComponent = new UploadComponent();
    projectInformation = new ProjInformationComponent(datahandler, state, resourceUrl);
    
    //projectview_tab.addStyleName(ValoTheme.TABSHEET_ICONS_ON_TOP);
    projectview_tab.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
    projectview_tab.addStyleName(ValoTheme.TABSHEET_FRAMED);
    projectview_tab.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

    
    //projectview_tab.addTab(initDescription()).setIcon(FontAwesome.INFO_CIRCLE);
    projectview_tab.addTab(projectInformation).setIcon(FontAwesome.INFO_CIRCLE);
    projectview_tab.addTab(initGraph()).setIcon(FontAwesome.SITEMAP);
    projectview_tab.addTab(initMemberSection()).setIcon(FontAwesome.USERS);
    projectview_tab.addTab(initStatistics()).setIcon(FontAwesome.CHECK_CIRCLE);
    projectview_tab.addTab(initTable()).setIcon(FontAwesome.FLASK);
    projectview_tab.addTab(datasetComponent).setIcon(FontAwesome.DATABASE);
    projectview_tab.addTab(biologicalSamplesComponent).setIcon(FontAwesome.TINT);
    projectview_tab.addTab(measuredSamplesComponent).setIcon(FontAwesome.SIGNAL);
    projectview_tab.addTab(resultsComponent).setIcon(FontAwesome.TH_LARGE);
    projectview_tab.addTab(workflowComponent).setIcon(FontAwesome.COGS);
    projectview_tab.addTab(uploadComponent).setIcon(FontAwesome.UPLOAD);

    projectview_tab.setImmediate(true);

    
    projectview_tab.addSelectedTabChangeListener(new SelectedTabChangeListener() {
      
      @Override
      public void selectedTabChange(SelectedTabChangeEvent event) {
        if (event.getTabSheet().getSelectedTab().getCaption().equals("Project Graph")) {
          loadGraph();
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Datasets")) {
          datasetComponent.updateUI(navigateToLabel, getCurrentBean().getId());
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Measured Samples")) {
          measuredSamplesComponent.updateUI(navigateToLabel, getCurrentBean().getId(), "measured");
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Biological Samples")) {
          biologicalSamplesComponent.updateUI(getCurrentBean().getId());
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Results")) {
          resultsComponent.updateUI(navigateToLabel, getCurrentBean().getId(), "results");
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Workflows")) {
          Map<String,String> args = new HashMap<String,String>();
          args.put("id",getCurrentBean().getId());
          args.put("type", navigateToLabel);
          workflowComponent.update(args);
        }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("Upload Files")) {
            uploadComponent.updateUI(manager, getCurrentBean().getCode());
          }
        else if (event.getTabSheet().getSelectedTab().getCaption().equals("")) {
        	projectInformation.updateUI(getCurrentBean(), "project");
        }
      }
    });
      
    //projectview_content.addComponent(initMenuBar());
    projectview_content.addComponent(projectview_tab);
    //projectview_content.addComponent(initDescription());
    //projectview_content.addComponent(initStatistics());
    //projectview_content.addComponent(initTable());
    //projectview_content.addComponent(initButtonLayout());
    
    //projectview_content.addComponent(initGraph());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    //projectview_content.setWidth("100%");
    this.addComponent(projectview_content);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
   // updateContentToolBar();
    headerLabel = String.format("%s",getCurrentBean().getCode());
    
    //updateContentDescription();
    updateContentMemberSection();
    updateContentStatistics();
    updateContentTable();
    updateContentButtonLayout();
    
    projectInformation.updateUI(getCurrentBean(), "project");
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
    buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, false, true));

    return buttonLayoutSection;
  }

  void updateContentButtonLayout() {
    if (fileDownloader != null)
      this.export.removeExtension(fileDownloader);
    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getExperiments()),
            currentBean.getId());
    fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
  }

  /**
   * 
   * @return
   */
  ToolBar initMenuBar() {
    SearchBarView searchBarView = new SearchBarView(datahandler);
    toolbar = new ToolBar(resourceUrl, state, searchBarView);
    toolbar.init();
    return toolbar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentToolBar() {
    
    Boolean containsData = currentBean.getContainsData();
    toolbar.setDownload(containsData);
    toolbar.setWorkflow(containsData);
    toolbar.setBarcode(currentBean.getExperiments().size() >0);
    toolbar.update(navigateToLabel, currentBean.getId());
  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();
    
    projDescription.setCaption("");

    // String desc = currentBean.getDescription();
    // if (!desc.isEmpty()) {
    // descContent.setValue(desc);
    // }
    descContent = new Label("");
    // contact.setValue("<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
    // + currentBean.getId()
    // + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
    // + currentBean.getId() + "</a>");
    contact = new Label("", ContentMode.HTML);
    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);
    projDescriptionContent.setMargin(new MarginInfo(true, false, true, true));
    //projDescriptionContent.setCaption("Description");
    //projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);
    projDescriptionContent.setSpacing(true);
    projDescription.setMargin(new MarginInfo(true, false, true, true));
    projDescription.setWidth("100%");
    projDescription.setSpacing(true);
    projDescription.addComponent(projectInformation);
    
    return projDescription;
  }
  
  VerticalLayout initMemberSection() {
    VerticalLayout projMembers = new VerticalLayout();
    projMembers.setCaption("Members");
    
    membersSection = new VerticalLayout();
    Component membersContent = new VerticalLayout();

    //membersContent.setIcon(FontAwesome.USERS);
    //membersContent.setCaption("Members");
    membersSection.addComponent(membersContent);
    //membersSection.setMargin(new MarginInfo(false, false, false, true));
    membersSection.setWidth("100%");
    membersSection.setSpacing(true);
    
    membersSection.setMargin(new MarginInfo(true, false, true, true));
    projMembers.addComponent(membersSection);
    
    projMembers.setMargin(new MarginInfo(true, false, true, true));
    projMembers.setWidth("100%");
    projMembers.setSpacing(true);
    
    return projMembers; 
  }

  void updateContentDescription() {
    contact
        .setValue("<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
            + currentBean.getId()
            + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
            + currentBean.getId() + "</a>");
    String desc = currentBean.getDescription();
    if (!desc.isEmpty()) {
      descContent.setValue(desc);
    }

    //membersSection.removeAllComponents();
   // membersSection.addComponent(getMembersComponent());
    //membersSection.setMargin(new MarginInfo(false, false, false, true));
  }
  
  void updateContentMemberSection() {
    membersSection.removeAllComponents();
    membersSection.addComponent(getMembersComponent());
  }


  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {
    VerticalLayout statistics = new VerticalLayout();
    statistics.setCaption("Status");

    statContent = new HorizontalLayout();
    //statContent.setCaption("Statistics");
    //statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(""));

    // TODO
    // statContent.addComponent(new Label(String.format("%s sample(s),", )));
    // int numOfDatasets = datahandler.datasetMap.get(project.getId()).size();
    // statContent.addComponent(new Label(String.format("%s dataset(s).", numOfDatasets)));

    statContent.setMargin(new MarginInfo(true, false, false, true));
    statContent.setSpacing(true);

    /*
     * if (numOfDatasets > 0) {
     * 
     * String lastSample = "No samples available"; // if
     * (project.projectInformation.lastChangedSample != null) { // lastSample =
     * projectInformation.lastChangedSample.split("/")[2]; lastSample = "not implemented"; // TODO
     * // } statContent.addComponent(new Label(String.format("Last change %s",
     * String.format("occurred in sample %s (%s)", lastSample, //
     * projectInformation.lastChangedDataset.toString())))); "never")))); }
     */

    statistics.addComponent(statContent);
    statistics.setMargin(new MarginInfo(true, false, false, true));
    statistics.setSpacing(true);


    // status bar section

    status = new VerticalLayout();
    //status.setMargin(new MarginInfo(false, false, false, true));
    status.setSpacing(true);
    
    statistics.addComponent(status);
    return statistics;
  }

  /**
   * 
   */
  void updateContentStatistics() {
    statContent.removeAllComponents();
    statContent.addComponent(new Label(String.format("%s experiment(s),", currentBean
        .getExperiments().size())));
    statContent.setMargin(new MarginInfo(true, false, true, true));

    // TODO
    // statContent.addComponent(new Label(String.format("%s sample(s),", )));
    // int numOfDatasets = datahandler.datasetMap.get(project.getId()).size();
    // statContent.addComponent(new Label(String.format("%s dataset(s).", numOfDatasets)));

    status.removeAllComponents();
    VerticalLayout statusContent =
        this.createProjectStatusComponent(datahandler.computeProjectStatuses(currentBean));
    //statusContent.setCaption("Status");
    //statusContent.setIcon(FontAwesome.CLOCK_O);

    // TODO
    // statusContent.addComponent(new Label(projectInformation.statusMessage));
    statusContent.setSpacing(true);
    statusContent.setMargin(new MarginInfo(true, false, true, true));

    status.addComponent(statusContent);
  }



  VerticalLayout initTable() {
    this.table = this.buildFilterTable();
    this.tableClickChangeTreeView();
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

  void resetGraph() {
    graphSectionContent.removeAllComponents();
    //VerticalLayout graphSection = (VerticalLayout) graphSectionContent.getParent();
    //graphSection.getComponent(1).setVisible(true);
    //graphSection.getComponent(1).setEnabled(true);
 }

  /**
   * 
   * @return
   */
  VerticalLayout initGraph() {
    VerticalLayout graphSection = new VerticalLayout();
    graphSectionContent = new VerticalLayout();
    graphSection.setCaption( "Project Graph");

    //graphSectionContent.setCaption("Project Graph");
    //graphSectionContent.setIcon(FontAwesome.SHARE_SQUARE_O);

    graphSectionContent.setMargin(new MarginInfo(true, false, true, true));
    graphSection.setMargin(new MarginInfo(true, false, true, true));
    graphSection.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
    graphSectionContent.setWidth("100%");
    /*
    final Button loadGraph = new Button("[+]");
    loadGraph.setStyleName(ValoTheme.BUTTON_LINK);
    loadGraph.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        if (graphSectionContent.getComponentCount() == 0
            || !(graphSectionContent.getComponent(0) instanceof Image)) {
          ProgressBar progress = new ProgressBar();
          progress.setIndeterminate(true);
          Label info =
              new Label(
                  "Computing the project graph can take several seconds on big projects. Please be patient.");
          info.setStyleName(ValoTheme.LABEL_SUCCESS);
          graphSectionContent.addComponent(info);
          graphSectionContent.addComponent(progress);
          Worker worker = new Worker(getCurrent());
          worker.start();
          UI.getCurrent().setPollInterval(500);
          loadGraph.setEnabled(false);
        }


      }

      public void processed() {
        UI.getCurrent().setPollInterval(-1);
        loadGraph.setVisible(false);
      }

      class Worker extends Thread {
        private ProjectView projectView;

        public Worker(ProjectView current) {
          projectView = current;
        }

        @Override
        public void run() {
          projectView.updateContentGraph();
          synchronized (UI.getCurrent()) {
            processed();
          }

        }
      }
    });

  */
    graphSection.addComponent(graphSectionContent);
    //graphSection.addComponent(loadGraph);
    return graphSection;
  }
  
  public void processed() {
    UI.getCurrent().setPollInterval(-1);
  }

  class Worker extends Thread {
    private ProjectView projectView;

    public Worker(ProjectView current) {
      projectView = current;
    }

    @Override
    public void run() {
      projectView.updateContentGraph();
      synchronized (UI.getCurrent()) {
        processed();
      }

    }
  }
  
  public void loadGraph() {
    LOGGER.debug(String.valueOf(graphSectionContent.getComponentCount() == 0));
    if (graphSectionContent.getComponentCount() > 0)
      LOGGER.debug(String.valueOf(graphSectionContent.getComponent(0) instanceof Image));
    if (graphSectionContent.getComponentCount() == 0
        || !(graphSectionContent.getComponent(0) instanceof Image)) {
      ProgressBar progress = new ProgressBar();
      progress.setIndeterminate(true);
      Label info =
          new Label(
              "Computing the project graph can take several seconds on big projects. Please be patient.");
      info.setStyleName(ValoTheme.LABEL_SUCCESS);
      graphSectionContent.removeAllComponents();
      //graphSectionContent.addComponent(info);
      graphSectionContent.addComponent(progress);
      //graphSectionContent.setComponentAlignment(info, Alignment.MIDDLE_CENTER);
      graphSectionContent.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);
      Worker worker = new Worker(getCurrent());
      worker.start();
      UI.getCurrent().setPollInterval(500);
    }
  }

  public ProjectView getCurrent() {
    return this;
  }

  void updateContentGraph() {
    Resource resource = getGraphResource();
    
    if (resource != null) {
      graphSectionContent.removeAllComponents();
      Image graphImage = new Image("", resource);
  
      graphSectionContent.addComponent(graphImage);
      graphSectionContent.setComponentAlignment(graphImage, Alignment.MIDDLE_CENTER);
    } else {
      Label error = new Label("Project Graph can not be computed at that time for this project");
      error.setStyleName(ValoTheme.LABEL_FAILURE);
      graphSectionContent.removeAllComponents();
      graphSectionContent.addComponent(error);
      graphSectionContent.setComponentAlignment(error, Alignment.MIDDLE_CENTER);

      LOGGER.error(String.format("%s: %s",error.getValue(),currentBean.getId()));
    }
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

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectBean
   */
  public void setContainerDataSource(ProjectBean projectBean) {
    this.currentBean = projectBean;
    this.table.setContainerDataSource(projectBean.getExperiments());
    
    for (Iterator i = table.getItemIds().iterator(); i.hasNext();) {
    	// Get the current item identifier, which is an integer.
        ExperimentBean item = (ExperimentBean) i.next();
        
    	LOGGER.debug("Status " + item.getStatus());
    }
    table.setVisibleColumns(new Object[] {"code", "type", "registrationDate", "registrator",
        "status"});
    
    int rowNumber = this.table.size();
    
    if (rowNumber == 0) {
      this.table.setVisible(false);
    }
    else {
      this.table.setVisible(true);
      this.table.setPageLength(Math.min(rowNumber, 10));
    }
  }


  /**
   * returns Resource which represents the project graph of the current Bean. Can be set as the
   * resource of an {@link com.vaadin.ui.Image}.
   * 
   * @return
   */
  private Resource getGraphResource() {
    Resource resource = null;
    try {
      
      GraphGenerator graphFrame =
          new GraphGenerator(datahandler.getOpenBisClient().getSamplesOfProject(currentBean.getId()), datahandler.getOpenBisClient().getSampleTypes(), datahandler.getOpenBisClient(), currentBean.getId());
      resource = graphFrame.getRes();
    } catch (IOException e) {
      LOGGER.error("graph creation failed", e.getStackTrace());
    }
    return resource;
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, ExperimentView.navigateToLabel));
  }

  /**
   * initializes and builds a filtering table for this view
   * 
   * @return
   */
  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);


    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);


    filterTable.setColumnHeader("code", "Name");
    filterTable.setColumnHeader("type", "Type");
    filterTable.setColumnHeader("registrationDate", "Registration Date");
    filterTable.setColumnHeader("registrator", "Registered By");
    filterTable.setColumnHeader("status", "Status");

    return filterTable;
  }

  
  class MemberWorker extends Thread {

    @Override
    public void run() {
      Company company = null;
      long companyId = 1;
      try {
        String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
        company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
        companyId = company.getCompanyId();
        LOGGER.debug(String.format("Using webId %s and companyId %d to get Portal User", webId,
            companyId));
      } catch (PortalException | SystemException e) {
        LOGGER.error(
            "liferay error, could not retrieve companyId. Trying default companyId, which is "
                + companyId, e.getStackTrace());
      }
      Set<String> list = datahandler.removeQBiCStaffFromMemberSet(datahandler.getOpenBisClient().getSpaceMembers(currentBean.getId().split("/")[1]));
      members = new TreeMap<String, String>();
      memberLetters = new HashMap<String, String>();
      
      LOGGER.debug(list.toString());
      
      if (list != null) {
        memberString = new StringBuilder();
        for (String member : list) {
          User user = null;
          try {
            user = UserLocalServiceUtil.getUserByScreenName(companyId, member);
          } catch (PortalException | SystemException e) {
          }

          if (memberString.length() > 0) {
            //memberString.append(" , ");
          }

          if (user == null) {
            LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", member));
            //memberString.append(member);
            members.put(member, member);
            // membersLayout.addComponent(new Label(member));
          } else {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();

            String email = user.getEmailAddress();
           
            String userString = "<a href=\"mailto:" + email + "\" style=\"color: #0068AA; text-decoration: none\">" + lastName + ", " + firstName + "</a>";
            
            if(user.getLastName().length() > 0) {
              members.put(user.getLastName(), userString);
            }
            
            else {
              members.put(user.getFirstName(), userString);
            }
            
            //memberString.append("<a href=\"mailto:");
            //memberString.append(email);
            //memberString.append("\" style=\"color: #0068AA; text-decoration: none\">");
            //memberString.append(fullname);
            //memberString.append("</a>");
          }
        }
        synchronized (UI.getCurrent()) {
          processedMember();
        }
      }
    }
  }

  /**
   * 
   * @param list
   * @return
   */
  private Component getMembersComponent() {
    membersLayout = new HorizontalLayout();
    //membersLayout.setIcon(FontAwesome.USERS);
    //membersLayout.setCaption("Members");
    membersLayout.setWidth("100%");


    //final Button loadMembers = new Button("[+]");
    //membersLayout.addComponent(loadMembers);
    //loadMembers.setStyleName(ValoTheme.BUTTON_LINK);
    //loadMembers.addClickListener(new ClickListener() {

      //@Override
      //public void buttonClick(ClickEvent event) {
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        Label info =
            new Label(
                "Searching for members. Can take several seconds on big projects. Please be patient.");
        info.setStyleName(ValoTheme.LABEL_SUCCESS);
        //membersLayout.addComponent(info);
        membersLayout.addComponent(progress);
        MemberWorker worker = new MemberWorker();
        worker.start();
        UI.getCurrent().setPollInterval(500);
       // loadMembers.setEnabled(false);
        
        return membersLayout;
  }


      public void processedMember() {
        String memberString = "";
        Label label;
        
        if(members.size() < 1) {
          label = new Label("No Members found.");
        }
        else {
          for(Entry<String, String> entry: members.entrySet()) {
            String firstLetter = String.valueOf(entry.getKey().charAt(0));
            
            if(!memberLetters.containsKey(firstLetter)) {
              memberString += String.format("<font size='16'><b>%s</b></font><br>", firstLetter.toUpperCase());
              memberLetters.put(firstLetter, "");
              }
            
            memberString += String.format("%s<br>", entry.getValue());
          }
          label = new Label(memberString, ContentMode.HTML);
        }
        
        //if (memberString == null || memberString.length() == 0) {
        //  label = new Label("no members found.");
        //} else {
        //  label = new Label(memberString.toString(), ContentMode.HTML);
        //}
        
        membersLayout.removeAllComponents();
        membersLayout.addComponent(label);
        membersLayout.setSpacing(true);
        membersLayout.setMargin(new MarginInfo(false, false, true, true));


        UI.getCurrent().setPollInterval(-1);
        //loadMembers.setVisible(false);
      }

      /*
      class Worker extends Thread {

        @Override
        public void run() {
          Company company = null;
          long companyId = 1;
          try {
            String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
            company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
            companyId = company.getCompanyId();
            LOGGER.debug(String.format("Using webId %s and companyId %d to get Portal User", webId,
                companyId));
          } catch (PortalException | SystemException e) {
            LOGGER.error(
                "liferay error, could not retrieve companyId. Trying default companyId, which is "
                    + companyId, e.getStackTrace());
          }
          Set<String> list =
              datahandler.openBisClient.getSpaceMembers(currentBean.getId().split("/")[1]);
          if (list != null) {
            memberString = new StringBuilder();
            for (String member : list) {
              User user = null;
              try {
                user = UserLocalServiceUtil.getUserByScreenName(companyId, member);
              } catch (PortalException | SystemException e) {
              }

              if (memberString.length() > 0) {
                memberString.append(" , ");
              }

              if (user == null) {
                LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", member));
                memberString.append(member);
                // membersLayout.addComponent(new Label(member));
              } else {
                String fullname = user.getFullName();
                String email = user.getEmailAddress();
                // VaadinSession.getCurrent().getService();
                // ThemeDisplay themedisplay =
                // (ThemeDisplay)
                // VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
                // String url = user.getPortraitURL(themedisplay);
                // ExternalResource er = new ExternalResource(url);
                // com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
                // image.setHeight(80, Unit.PIXELS);
                // image.setWidth(65, Unit.PIXELS);
                // membersLayout.addComponent(image);
                // String labelString =
                // new String("<a href=\"mailto:" + email
                // + "\" style=\"color: #0068AA; text-decoration: none\">" + fullname + "</a>");
                // Label userLabel = new Label(labelString, ContentMode.HTML);
                // membersLayout.addComponent(userLabel);
                memberString.append("<a href=\"mailto:");
                memberString.append(email);
                memberString.append("\" style=\"color: #0068AA; text-decoration: none\">");
                memberString.append(fullname);
                memberString.append("</a>");
              }
            }
            synchronized (UI.getCurrent()) {
              processed();
            }
          }
        }
      }
    });
    */

  /**
   * 
   * @param statusValues
   * @return
   */
  public VerticalLayout createProjectStatusComponent(Map<String, Integer> statusValues) {
    VerticalLayout projectStatusContent = new VerticalLayout();

    Iterator<Entry<String, Integer>> it = statusValues.entrySet().iterator();
    int finishedExperiments = 0;

    while (it.hasNext()) {
      Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();

      if ((Integer) pairs.getValue() == 0) {
        Label statusLabel =
            new Label(pairs.getKey() + ": " + FontAwesome.TIMES.getHtml(), ContentMode.HTML);
        statusLabel.addStyleName("redicon");
        projectStatusContent.addComponent(statusLabel);
      }

      else {
        Label statusLabel =
            new Label(pairs.getKey() + ": " + FontAwesome.CHECK.getHtml(), ContentMode.HTML);
        statusLabel.addStyleName("greenicon");

        if (pairs.getKey().equals("Project Planned")) {
          projectStatusContent.addComponentAsFirst(statusLabel);
        } else {
          projectStatusContent.addComponent(statusLabel);

        }
        finishedExperiments += (Integer) pairs.getValue();
      }
    }
    //ProgressBar progressBar = new ProgressBar();
    //progressBar.setValue((float) finishedExperiments / statusValues.keySet().size());
    //projectStatusContent.addComponent(progressBar);

    return projectStatusContent;
  }

  /**
   * 
   */
  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    // TODO updateContent only if currentProject is not equal to newProject
    this.table.unselect(this.table.getValue());
    ProjectBean pbean = datahandler.getProject2(currentValue);
    // if the new project bean is different than reset the graph.
    if (currentBean != null && !pbean.getId().equals(currentBean.getId())) {
      LOGGER.debug("reseting graph");
      resetGraph();
    }
    this.setContainerDataSource(pbean);
    updateContent();
    
    projectview_tab.setSelectedTab(0);
  }

  public ProjectBean getCurrentBean() {
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
//     this.createBarcodesMenuItem.getParent().setEnabled(false);
    // this.downloadCompleteProjectMenuItem.getParent().setEnabled(false);
    this.toolbar.setEnabled(enabled);
  }


}
