/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects. Copyright (C) "2016” Christopher
 * Mohr, David Wojnar, Andreas Friedrich
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.ProjectSummaryReadyRunnable;
import helpers.SummaryFetcher;
import helpers.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import logging.Log4j2Logger;
import model.ExperimentBean;
import model.ProjectBean;
import model.SpaceBean;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.google.gwt.aria.client.ProgressbarRole;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.themes.ValoTheme;

public class HomeView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private logging.Logger LOGGER = new Log4j2Logger(HomeView.class);

  private String caption;
  private FilterTable table;
  private Grid projectGrid;
  private VerticalLayout homeview_content;
  private VerticalLayout buttonLayoutSection = new VerticalLayout();
  private SpaceBean currentBean;
  // Boolean includePatientCreation = false;
  private ToolBar toolBar;
  private State state;
  private String resourceUrl;
  private String header;

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }


  private DataHandler datahandler;
  private SummaryFetcher summaryFetcher;

  private Button export = new Button("Export as TSV");

  private int numberOfProjects = 0;

  private String user;

  public HomeView(DataHandler datahandler, String caption, String user, State state, String resUrl,
      String tmpFolderPath) {
    homeview_content = new VerticalLayout();
    // this.table = buildFilterTable();
    this.projectGrid = new Grid();
    this.datahandler = datahandler;
    this.state = state;
    this.resourceUrl = resUrl;
    this.summaryFetcher = new SummaryFetcher(datahandler.getOpenBisClient(), tmpFolderPath);

    this.user = user;
    // tableClickChangeTreeView();
  }

  /**
   * execute the above constructor with default settings, in order to have the same settings
   */
  public HomeView(DataHandler datahandler) {
    this(datahandler, "You seem to have no registered projects. Please contact QBiC.", "",
        new State(), "", "");
  }

  public void setSizeFull() {
    homeview_content.setSizeFull();
    super.setSizeFull();
    // this.table.setSizeFull();
    this.projectGrid.setSizeFull();
    homeview_content.setSpacing(true);
    // homeview_content.setMargin(true);
  }

  /**
   * sets the ContainerDataSource of this view. Should usually contain project information. Caption
   * is caption.
   * 
   * @param homeViewInformation
   * @param caption
   */
  public void setContainerDataSource(SpaceBean spaceBean, String newCaption) {

    caption = newCaption;
    currentBean = spaceBean;
    numberOfProjects = currentBean.getProjects().size();
    projectGrid = new Grid();

    GeneratedPropertyContainer gpcProjects =
        new GeneratedPropertyContainer(spaceBean.getProjects());
    gpcProjects.removeContainerProperty("members");
    gpcProjects.removeContainerProperty("id");
    gpcProjects.removeContainerProperty("experiments");
    gpcProjects.removeContainerProperty("contact");
    gpcProjects.removeContainerProperty("contactPerson");
    gpcProjects.removeContainerProperty("containsData");
    gpcProjects.removeContainerProperty("description");
    gpcProjects.removeContainerProperty("progress");
    gpcProjects.removeContainerProperty("registrationDate");
    gpcProjects.removeContainerProperty("registrator");

    projectGrid.setContainerDataSource(gpcProjects);

    projectGrid.setHeightMode(HeightMode.ROW);
    projectGrid.setHeightByRows(20);

    projectGrid.getColumn("code").setHeaderCaption("Sub-Project");
    projectGrid.getColumn("secondaryName").setHeaderCaption("Short Name");
    projectGrid.getColumn("space").setHeaderCaption("Project");
    projectGrid.getColumn("principalInvestigator").setHeaderCaption("Investigator");
    projectGrid.setColumnOrder("code", "space", "secondaryName", "principalInvestigator");

    helpers.GridFunctions.addColumnFilters(projectGrid, gpcProjects);

    gpcProjects.addGeneratedProperty("Summary", new PropertyValueGenerator<String>() {
      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        return "show";
      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    projectGrid.getColumn("Summary").setRenderer(new ButtonRenderer(new RendererClickListener() {

      @Override
      public void click(RendererClickEvent event) {
        // Show loading window
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        VerticalLayout vl = new VerticalLayout(bar);
        vl.setSpacing(true);
        vl.setMargin(true);
        Window loadingWindow = new Window("Project summary loading...");
        loadingWindow.setContent(vl);
        loadingWindow.setWidth("210px");
        loadingWindow.center();
        loadingWindow.setModal(true);
        loadingWindow.setResizable(false);
        QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
        ui.addWindow(loadingWindow);

        // fetch summary and create docx in tmp folder
        
        ProjectBean proj = (ProjectBean) event.getItemId();
        summaryFetcher.fetchSummaryComponent(proj.getCode(),
            new ProjectSummaryReadyRunnable(summaryFetcher, loadingWindow, proj.getCode()));
      }
    }));

    projectGrid.addSelectionListener(new SelectionListener() {

      @Override
      public void select(SelectionEvent event) {
        Set<Object> selectedElements = event.getSelected();
        if (selectedElements == null) {
          return;
        }

        ProjectBean selectedProject = (ProjectBean) selectedElements.iterator().next();

        if (selectedProject == null) {
          return;
        }

        String entity = selectedProject.getId();
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(entity);
        message.add(ProjectView.navigateToLabel);
        state.notifyObservers(message);
      }
    });
  }

  private void setExportButton() {
    buttonLayoutSection.removeAllComponents();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.addComponent(buttonLayout);

    buttonLayout.addComponent(this.export);

    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getProjects()), this.caption);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
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
   * 
   * @return
   */
  ToolBar initToolBar() {
    // SearchBarView searchBarView = new SearchBarView(datahandler);
    SearchEngineView searchEngineView = new SearchEngineView(datahandler);

    toolBar = new ToolBar(resourceUrl, state, searchEngineView);
    toolBar.init();
    return toolBar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentToolBar() {
    toolBar.setDownload(false);
    toolBar.setWorkflow(false);
    toolBar.update("", "");
  }

  void buildLayout(int browserHeight, int browserWidth, WebBrowser browser) {
    this.setWidth("100%");

    this.setMargin(new MarginInfo(false, false, false, false));
    // clean up first
    homeview_content.removeAllComponents();
    homeview_content.setWidth("100%");

    updateView(browserWidth, browserWidth, browser);

    // view overall statistics
    VerticalLayout homeViewDescription = new VerticalLayout();

    Label statContent;
    if (numberOfProjects > 0) {
      statContent = new Label(String.format("You have %s Sub-Project(s)", numberOfProjects));
      setHeader(String.format("Total number of Sub-Projects: %s", numberOfProjects));
    } else {
      statContent = new Label(
          String.format("You have no projects so far. Please contact your project manager."));
      statContent.addStyleName(ValoTheme.LABEL_FAILURE);
      statContent.addStyleName(ValoTheme.LABEL_LARGE);
    }

    homeViewDescription.setWidth("100%");
    homeview_content.addComponent(homeViewDescription);


    // table
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSectionContent.setCaption("Sub-Projects");
    tableSectionContent.setIcon(FontAwesome.TABLE);
    // tableSectionContent.addComponent(this.table);
    tableSectionContent.addComponent(this.projectGrid);

    tableSection.setMargin(new MarginInfo(false, false, false, false));

    this.projectGrid.setWidth("100%");
    this.projectGrid.setSelectionMode(SelectionMode.SINGLE);

    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    homeview_content.addComponent(tableSection);
    this.addComponent(homeview_content);
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table
        .addValueChangeListener(new ViewTablesClickListener(table, ProjectView.navigateToLabel));
  }


  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    // filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(false);

    filterTable.setColumnReorderingAllowed(true);
    return filterTable;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    try {
      loadProjects();
      int height = event.getNavigator().getUI().getPage().getBrowserWindowHeight();
      int width = event.getNavigator().getUI().getPage().getBrowserWindowWidth();
      buildLayout(height, width, event.getNavigator().getUI().getPage().getWebBrowser());
    } catch (Exception e) {
      LOGGER.error(String.format("failed to load projects for user %s", user), e);
      homeview_content.removeAllComponents();
      Label error = new Label("Connection to database interrupted. Please try again later.");
      error.addStyleName(ValoTheme.LABEL_FAILURE);
      error.addStyleName(ValoTheme.LABEL_HUGE);
      homeview_content.addComponent(error);
      homeview_content.setComponentAlignment(error, Alignment.MIDDLE_CENTER);
      this.addComponent(homeview_content);
    }
  }

  /**
   * Enables or disables the component. The user can not interact disabled components, which are
   * shown with a style that indicates the status, usually shaded in light gray color. Components
   * are enabled by default.
   */
  public void setEnabled(boolean enabled) {
    this.export.setEnabled(enabled);
    this.table.setEnabled(enabled);
  }


  /**
   * refresh all openbis project for current user. Basically currentBean is overwritten
   */
  public void loadProjects() {
    // this.includePatientCreation = false;
    final SpaceBean homeSpaceBean =
        new SpaceBean("homeSpace", "", false, null, null, null, null, null, null);
    BeanItemContainer<ProjectBean> projectContainer =
        new BeanItemContainer<ProjectBean>(ProjectBean.class);

    LOGGER.info("Loading projects...");
    List<Project> projects = datahandler.getOpenBisClient().getOpenbisInfoService()
        .listProjectsOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), user);
    LOGGER.info("Loading projects...done.");

    for (Project project : projects) {
      String projectIdentifier = project.getIdentifier();
      String projectCode = project.getCode();
      String desc = project.getDescription();
      if (desc == null) {
        desc = "";
      } else if (desc.length() > 0) {
        desc = desc.substring(0, Math.min(desc.length(), 100));
        if (desc.length() == 100) {
          desc += "...";
        }
      }

      // TODO isn't this slow in this fashion? what about SELECT * and creating a map?
      String secondaryName = datahandler.getDatabaseManager().getProjectName(projectIdentifier);
      if (secondaryName.isEmpty() || secondaryName == null)
        secondaryName = "None";

      ProjectBean newProjectBean = new ProjectBean(projectIdentifier, projectCode, secondaryName,
          desc, project.getSpaceCode(), new BeanItemContainer<ExperimentBean>(ExperimentBean.class),
          new ProgressBar(), new Date(), "", "", null, false);

      // TODO isn't this slow in this fashion? what about SELECT * and creating a map?
      String pi = datahandler.getDatabaseManager().getInvestigatorForProject(projectIdentifier);

      if (pi.equals("")) {
        newProjectBean.setPrincipalInvestigator("No information provided.");
      } else {
        newProjectBean.setPrincipalInvestigator(pi);
      }

      projectContainer.addBean(newProjectBean);
    }

    homeSpaceBean.setProjects(projectContainer);
    if (homeSpaceBean.getProjects().size() > 0) {
      this.setContainerDataSource(homeSpaceBean, caption);
    }
  }
}
