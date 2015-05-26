package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logging.Log4j2Logger;
import logging.Logger;
import model.ProjectBean;

import org.tepi.filtertable.FilterTable;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends VerticalLayout implements View {

  static String navigateToLabel = "project";

  FilterTable table;
  VerticalLayout projectview_content;
  VerticalLayout buttonLayoutSection;


  private ProjectBean currentBean;

  private Button export;

  private DataHandler datahandler;

  private State state;

  private FileDownloader fileDownloader;

  private String resourceUrl;

  private MenuItem downloadCompleteProjectMenuItem;

  private MenuItem datasetOverviewMenuItem;

  private MenuItem createBarcodesMenuItem;

  private MenuBar menubar;

  private Label contact;

  private Label descContent;

  private VerticalLayout status;

  private HorizontalLayout statContent;

  private HorizontalLayout graphSectionContent;

  private VerticalLayout membersSection;

  private static Logger LOGGER = new Log4j2Logger(ProjectView.class);


  public ProjectView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }

  public ProjectView(DataHandler datahandler, State state) {
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
    setWidth((browserWidth * 0.6f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() {
    projectview_content = new VerticalLayout();
    projectview_content.addComponent(initMenuBar());
    projectview_content.addComponent(initDescription());
    projectview_content.addComponent(initStatistics());
    projectview_content.addComponent(initTable());
    projectview_content.addComponent(initButtonLayout());
    projectview_content.addComponent(initGraph());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    projectview_content.setWidth("100%");
    this.addComponent(projectview_content);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
    updateContentMenuBar();
    updateContentDescription();
    updateContentStatistics();
    updateContentTable();
    updateContentButtonLayout();
    updateContentGraph();
  }


  /**
   * 
   * @return
   */
  HorizontalLayout initButtonLayout() {
    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");

    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayout.addComponent(this.export);
    return buttonLayout;
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
  MenuBar initMenuBar() {
    menubar = new MenuBar();
    menubar.setWidth(100.0f, Unit.PERCENTAGE);
    menubar.addStyleName("user-menu");

    // set to true for the hack below
    menubar.setHtmlContentAllowed(true);
    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setEnabled(false); 
    
    downloadProject.setIcon(new ThemeResource("computer_higher.png"));
    downloadProject.addSeparator();
    this.downloadCompleteProjectMenuItem =
        downloadProject
            .addItem(
                "<a href=\""
                    + resourceUrl
                    + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project</a>",
                null);
    
    // Open DatasetView
    this.datasetOverviewMenuItem = downloadProject.addItem("Dataset Overview", null);
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));

    // Another submenu item with a sub-submenu
    this.createBarcodesMenuItem = manage.addItem("Create Barcodes", null, null);

    /*MenuItem workflows = menubar.addItem("Run workflows", null, null);
    workflows.setIcon(new ThemeResource("dna_higher.png"));
    workflows.setEnabled(false);

    MenuItem analyze = menubar.addItem("Analyze your data", null, null);
    analyze.setIcon(new ThemeResource("graph_higher.png"));
    analyze.setEnabled(false);*/
    return menubar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentMenuBar() {
    LOGGER.debug("downloadCompleteProjectMenuItem.getParent() is " + downloadCompleteProjectMenuItem.getParent());
    LOGGER.debug("currentBean is " + currentBean);
    LOGGER.debug("currentBean.getContainsData() is " + currentBean.getContainsData());
    Boolean containsData = currentBean.getContainsData();
    MenuItem downloadProject = this.downloadCompleteProjectMenuItem.getParent();
    LOGGER.debug("setting downloadProject " + downloadProject + " enabled: " + containsData );
    downloadProject.setEnabled(containsData);
    
    downloadCompleteProjectMenuItem
        .setText("<a href=\""
            + resourceUrl
            + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project</a>");

    datasetOverviewMenuItem.setCommand(new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem) {
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        StringBuilder sb = new StringBuilder("type=");
        sb.append(navigateToLabel);
        sb.append("&");
        sb.append("id=");
        sb.append(currentBean.getId());
        message.add(sb.toString());
        message.add(DatasetView.navigateToLabel);
        state.notifyObservers(message);
      }
    });
    createBarcodesMenuItem.setCommand(new MenuBar.Command() {
      public void menuSelected(MenuItem selectedItem) {
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(currentBean.getId());
        message.add(BarcodeView.navigateToLabel);
        state.notifyObservers(message);
      }
    });

  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

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
    projDescriptionContent.setMargin(true);
    projDescriptionContent.setCaption("Description");
    projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);
    membersSection = new VerticalLayout();
    Component membersContent = new VerticalLayout();

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    membersSection.addComponent(membersContent);
    membersSection.setMargin(true);
    projDescription.addComponent(membersSection);
    membersSection.setWidth("100%");

    projDescription.setMargin(true);
    projDescription.setWidth("100%");
    return projDescription;
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
    // TODO use space information to check whether members really have to be recalculated.
    // For users chances are high, that they click on a project from the same space -> no
    // recalculation needed!
    Component membersContent = getMembersComponent(currentBean.getMembers());

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    membersContent.setWidth("100%");
    membersSection.removeAllComponents();
    membersSection.addComponent(membersContent);
    membersSection.setMargin(true);
  }


  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {
    VerticalLayout statistics = new VerticalLayout();

    statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(""));

    // TODO
    // statContent.addComponent(new Label(String.format("%s sample(s),", )));
    // int numOfDatasets = datahandler.datasetMap.get(project.getId()).size();
    // statContent.addComponent(new Label(String.format("%s dataset(s).", numOfDatasets)));

    statContent.setMargin(true);
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
    statistics.setMargin(true);


    // status bar section

    status = new VerticalLayout();
    status.setMargin(true);
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

    // TODO
    // statContent.addComponent(new Label(String.format("%s sample(s),", )));
    // int numOfDatasets = datahandler.datasetMap.get(project.getId()).size();
    // statContent.addComponent(new Label(String.format("%s dataset(s).", numOfDatasets)));

    status.removeAllComponents();
    VerticalLayout statusContent =
        this.createProjectStatusComponent(datahandler.computeProjectStatuses(currentBean));
    statusContent.setCaption("Status");
    statusContent.setIcon(FontAwesome.CLOCK_O);

    // TODO
    // statusContent.addComponent(new Label(projectInformation.statusMessage));
    statusContent.setSpacing(true);
    statusContent.setMargin(true);

    status.addComponent(statusContent);
  }



  VerticalLayout initTable() {
    this.table = this.buildFilterTable();
    this.tableClickChangeTreeView();
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSectionContent.setCaption("Registered Experiments");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);

    return tableSection;
  }


  void updateContentTable() {
    // Nothing to do here at the moment
    // table is already set in setdataresource
  }


  /**
   * 
   * @return
   */
  VerticalLayout initGraph() {
    VerticalLayout graphSection = new VerticalLayout();
    graphSectionContent = new HorizontalLayout();

    graphSectionContent.setCaption("Project Graph");
    graphSectionContent.setIcon(FontAwesome.SHARE_SQUARE_O);

    graphSectionContent.setMargin(true);
    graphSection.setMargin(true);
    graphSection.setWidth("100%");
    graphSectionContent.setWidth("100%");
    graphSection.addComponent(graphSectionContent);
    return graphSection;
  }

  void updateContentGraph() {
    Resource resource = getGraphResource();
    if (resource != null) {
      graphSectionContent.removeAllComponents();
      Image graphImage = new Image("", resource);
      graphSectionContent.addComponent(graphImage);
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
    table.setVisibleColumns(new Object[] {"code", "type", "registrationDate", "registrator",
        "status"});
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
      GraphGenerator graphFrame = new GraphGenerator(currentBean, datahandler.openBisClient);
      resource = graphFrame.getRes();
    } catch (IOException e) {
      LOGGER.error("graph creation failed", e.getStackTrace());
    }
    return resource;
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Experiment"));
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

  /**
   * 
   * @param list
   * @return
   */
  private Component getMembersComponent(Set<String> list) {
    HorizontalLayout membersLayout = new HorizontalLayout();
    if (list != null) {
      // membersLayout.addComponent(new Label("Members:"));
      for (String member : list) {

        // Cool idea, but let's do this when we have more portrait pictures in Liferay

        try {
          // companyId. We have presumable just one portal id, which equals the companyId.
          User user = UserLocalServiceUtil.getUserByScreenName(1, member);
          String fullname = user.getFullName();
          String email = user.getEmailAddress();


          // VaadinSession.getCurrent().getService();
          // ThemeDisplay themedisplay =
          // (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
          // String url = user.getPortraitURL(themedisplay);
          // ExternalResource er = new ExternalResource(url);
          // com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
          // image.setHeight(80, Unit.PIXELS);
          // image.setWidth(65, Unit.PIXELS);
          // membersLayout.addComponent(image);
          String labelString =
              new String("<a href=\"mailto:" + email
                  + "\" style=\"color: #0068AA; text-decoration: none\">" + fullname + "</a>");
          Label userLabel = new Label(labelString, ContentMode.HTML);
          membersLayout.addComponent(userLabel);

        } catch (com.liferay.portal.NoSuchUserException e) {
          LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", member));
          membersLayout.addComponent(new Label(member));
        } catch (PortalException | SystemException e) {
          LOGGER.error(
              "reading out openbis members and matching their names to liferay users failed",
              e.getStackTrace());
        }

      }
      membersLayout.setSpacing(true);
      membersLayout.setMargin(true);
    }
    return membersLayout;
  }

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
    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue((float) finishedExperiments / statusValues.keySet().size());
    projectStatusContent.addComponent(progressBar);

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
    this.setContainerDataSource(datahandler.getProject(currentValue));
    
    updateContent();
  }

  public ProjectBean getCurrentBean() {
    return currentBean;
  }

}
