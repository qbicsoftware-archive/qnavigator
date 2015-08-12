package de.uni_tuebingen.qbic.qbicmainportlet;


import helpers.Utils;

import java.util.Date;
import java.util.List;

import logging.Log4j2Logger;
import model.ExperimentBean;
import model.ProjectBean;
import model.SpaceBean;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class HomeView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private logging.Logger LOGGER = new Log4j2Logger(HomeView.class);

  String caption;
  FilterTable table;
  VerticalLayout homeview_content;
  VerticalLayout buttonLayoutSection = new VerticalLayout();
  SpaceBean currentBean;
  //Boolean includePatientCreation = false;
  ToolBar toolBar;
  State state;
  String resourceUrl;
  String header;

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }


  DataHandler datahandler;

  private Button export = new Button("Export as TSV");

  private int numberOfProjects = 0;

  private String user;

  public HomeView(DataHandler datahandler, String caption, String user, State state, String resUrl) {
    homeview_content = new VerticalLayout();
    this.table = buildFilterTable();
    this.datahandler = datahandler;
    this.state = state;
    this.resourceUrl = resUrl;

    this.user = user;
    tableClickChangeTreeView();
  }

  /**
   * execute the above constructor with default settings, in order to have the same settings
   */
  public HomeView(DataHandler datahandler) {
    this(datahandler, "You seem to have no registered projects. Please contact QBiC.", "", new State(), "");
  }

  public void setSizeFull() {
    homeview_content.setSizeFull();
    super.setSizeFull();
    this.table.setSizeFull();
    homeview_content.setSpacing(true);
    //homeview_content.setMargin(true);
  }

  /**
   * sets the ContainerDataSource of this view. Should usually contains project information. Caption
   * is caption.
   * 
   * @param homeViewInformation
   * @param caption
   */
  public void setContainerDataSource(SpaceBean spaceBean, String caption) {

    this.caption = caption;
    this.currentBean = spaceBean;
    this.numberOfProjects = currentBean.getProjects().size();

    setExportButton();

    this.table.setContainerDataSource(spaceBean.getProjects());
    this.table.setVisibleColumns(new Object[] {"code", "description"});
    this.table.setColumnHeader("code", "Name");
    this.table.setColumnHeader("description", "Description");
    this.table.setColumnExpandRatio("Name", 1);
    this.table.setColumnExpandRatio("Description", 3);

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
    SearchBarView searchBarView = new SearchBarView(datahandler);
    toolBar = new ToolBar(resourceUrl, state, searchBarView);
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
    this.setMargin(false);
    // clean up first
    homeview_content.removeAllComponents();
    homeview_content.setWidth("100%");

    // menubar
    //homeview_content.addComponent(initToolBar());

    updateView(browserWidth, browserWidth, browser);

    // view overall statistics
    //VerticalLayout statistics = new VerticalLayout();
    VerticalLayout homeViewDescription = new VerticalLayout();
    
    // patientButton
    /*
    if (includePatientCreation) {
      Button addPatient = new Button("Add Patient");
      addPatient.setIcon(FontAwesome.PLUS);
      addPatient.setStyleName("addpatient");

      addPatient.addClickListener(new ClickListener() {
        @Override
        public void buttonClick(ClickEvent event) {
          UI.getCurrent().getNavigator().navigateTo(String.format(AddPatientView.navigateToLabel));
        }
      });
      statistics.addComponent(addPatient);
      statistics.setComponentAlignment(addPatient, Alignment.TOP_RIGHT);
    }
    */
    // statistics
    //statistics.setCaption("Statistics");
    //statistics.setIcon(FontAwesome.FILE_TEXT_O);
    Label statContent;
    if (numberOfProjects > 0) {
      statContent = new Label(String.format("You have %s project(s)", numberOfProjects));
      setHeader(String.format("Total number of projects: %s", numberOfProjects));
    } else {
      statContent =
          new Label(
              String.format("You have no projects so far. Please contact your project manager."));
      statContent.addStyleName(ValoTheme.LABEL_FAILURE);
      statContent.addStyleName(ValoTheme.LABEL_LARGE);
    }
    //statistics.addComponent(statContent);

    //statistics.setMargin(new MarginInfo(false,false,false,true));
    //statistics.setWidth(100.0f, Unit.PERCENTAGE);

    //homeViewDescription.addComponent(statistics);
    //MhomeViewDescription.setMargin(true);
    homeViewDescription.setWidth("100%");
    homeview_content.addComponent(homeViewDescription);


    // table
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSectionContent.setCaption("Projects");
    tableSectionContent.setIcon(FontAwesome.TABLE);
    tableSectionContent.addComponent(this.table);

    //tableSectionContent.setMargin(true);
    //tableSection.setMargin(new MarginInfo(true, false, true, true));

    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    homeview_content.addComponent(tableSection);
    this.addComponent(homeview_content);
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, ProjectView.navigateToLabel));
  }


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
    //this.includePatientCreation = false;
    final SpaceBean homeSpaceBean =
        new SpaceBean("homeSpace", "", false, null, null, null, null, null, null);
    BeanItemContainer<ProjectBean> projectContainer =
        new BeanItemContainer<ProjectBean>(ProjectBean.class);

    List<Project> projects =
        datahandler.getOpenBisClient().getOpenbisInfoService().listProjectsOnBehalfOfUser(
            datahandler.getOpenBisClient().getSessionToken(), user);
    for (Project project : projects) {
     // if (project.getSpaceCode().contains("IVAC")) {
     //   this.includePatientCreation = true;
     // }
      datahandler.addOpenbisDtoProject(project);
      String projectIdentifier = project.getIdentifier();
      String projectCode = project.getCode();

      // Project descriptions can be long; truncate the string to provide a brief preview
      String desc = project.getDescription();
      if (desc == null) {
        desc = "";
      } else if (desc.length() > 0) {
        desc = desc.substring(0, Math.min(desc.length(), 100));
        if (desc.length() == 100) {
          desc += "...";
        }
      }
      ProjectBean newProjectBean =
          new ProjectBean(projectIdentifier, projectCode, desc, homeSpaceBean,
              new BeanItemContainer<ExperimentBean>(ExperimentBean.class), new ProgressBar(),
              new Date(), "", "", null, false);
      projectContainer.addBean(newProjectBean);
    }
    homeSpaceBean.setProjects(projectContainer);
    if (homeSpaceBean.getProjects().size() > 0) {
      this.setContainerDataSource(homeSpaceBean, caption);
    }
  }

}
