package de.uni_tuebingen.qbic.qbicmainportlet;


import helpers.Utils;

import java.util.Iterator;

import model.ProjectBean;
import model.SpaceBean;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.Align;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class HomeView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private String caption;
  FilterTable table;
  VerticalLayout homeview_content;
  VerticalLayout buttonLayoutSection = new VerticalLayout();


  DataHandler dh;

  private Button export;
  
  public HomeView(SpaceBean datasource, String caption) {
    homeview_content = new VerticalLayout();
    this.table = buildFilterTable();

    this.setContainerDataSource(datasource, caption);
    dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    
    this.tableClickChangeTreeView();

    this.buildLayout(datasource);
  }

  public HomeView(FilterTable table, SpaceBean datasource, String caption) {
    homeview_content = new VerticalLayout();
    this.table = table;

    this.setContainerDataSource(datasource, caption);
    dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    
    this.tableClickChangeTreeView();

    this.buildLayout(datasource);

  }

  /**
   * execute the above constructor with default settings, in order to have the same settings
   */
  public HomeView() {
    //this(new FilterTable(), new SpaceInformation(),
    this(new FilterTable(), new SpaceBean(),"You seem to have no registered projects. Please contact QBiC.");
  }

  public void setSizeFull() {
    homeview_content.setSizeFull();
    super.setSizeFull();
    this.table.setSizeFull();
    homeview_content.setSpacing(true);
    homeview_content.setMargin(true);
  }

  /**
   * sets the ContainerDataSource of this view. Should usually contains project information. Caption
   * is caption.
   * 
   * @param homeViewInformation
   * @param caption
   */
  //public void setContainerDataSource(SpaceInformation homeViewInformation, String caption) {
  public void setContainerDataSource(SpaceBean spaceBean, String caption) {
  
    this.caption = caption;
    //this.updateCaption();
    // this.setStatistics(homeViewInformation);

    //this.table.setContainerDataSource(homeViewInformation.projects);
    //TODO iterate over spaceBeanContainer and get projects ?
    
    buttonLayoutSection.removeAllComponents();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.addComponent(buttonLayout);

    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);
    
    StreamResource sr = Utils.getTSVStream(Utils.containerToString(spaceBean.getProjects()), this.caption);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
      
    this.table.setContainerDataSource(spaceBean.getProjects());
    this.table.setVisibleColumns(new Object[]{"code", "description", "containsData"});
    this.table.setColumnHeader("code", "Name");
    this.table.setColumnHeader("description", "Description");
    this.table.setColumnHeader("containsData", "Contains Datasets");
  }


  //private void buildLayout(SpaceInformation generalOpenBISInformation) {
  private void buildLayout(SpaceBean spaceBean) {
  // clean up first
    homeview_content.removeAllComponents();
    this.setMargin(false);
    
    //homeview_content.setMargin(true);
    //homeview_content.setWidth("100%");

    //this.setHeight("600px");

    // System.out.println(UI.getCurrent().getPage().getBrowserWindowHeight());
    // System.out.println(UI.getCurrent().getPage().getBrowserWindowWidth());

    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();

    homeview_content.setWidth("100%");
    //this.setWidth(String.format("%spx", (browserWidth * 0.6)));
    //this.setHeight(String.format("%spx", (browserHeight * 0.8)));
    this.setWidth((browserWidth * 0.6f), Unit.PIXELS);
    //this.setHeight((browserHeight * 0.8f), Unit.PIXELS);
    MenuBar menubar = new MenuBar();
    menubar.addStyleName("user-menu");
    // A top-level menu item that opens a submenu
    
    //set to true for the hack below
    menubar.setHtmlContentAllowed(true);
    homeview_content.addComponent(menubar);

    //menubar.addStyleName("qbicmainportlet");
    //menubar.setWidth(100.0f, Unit.PERCENTAGE);
    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_test2.png"));
    downloadProject.setEnabled(false);
    
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_test2.png"));
    manage.setEnabled(false);
   
    // Another submenu item with a sub-submenu
    MenuItem colds = manage.addItem("test2", null, null);

    // Another top-level item
    MenuItem snacks = menubar.addItem("Run workflows", null, null);
    snacks.setIcon(new ThemeResource("dna_test2.png"));
    snacks.setEnabled(false);
            
    // Yet another top-level item
    MenuItem servs = menubar.addItem("Analyze your data", null, null);
    servs.setIcon(new ThemeResource("graph_test2.png"));
    servs.addItem("Car Service", null, null);
    servs.setEnabled(false);

    // view overall statistics
    VerticalLayout statistics = new VerticalLayout();
    VerticalLayout homeViewDescription = new VerticalLayout();
    statistics.setCaption("Statistics");
    statistics.setIcon(FontAwesome.FILE_TEXT_O);

    Label statContent = new Label(String.format("You have %s project(s), %s experiment(s), %s sample(s), and %s dataset(s).", 
        spaceBean.getProjects().size(), spaceBean.getExperiments().size(), spaceBean.getSamples().size(), spaceBean.getDatasets().size()));
        //generalOpenBISInformation.numberOfProjects, generalOpenBISInformation.numberOfExperiments, generalOpenBISInformation.numberOfSamples,
    //generalOpenBISInformation.numberOfDatasets) );
    statistics.addComponent(statContent);
    statistics.setMargin(true);
    statistics.setWidth(100.0f, Unit.PERCENTAGE);
    homeViewDescription.addComponent(statistics);
    homeViewDescription.setMargin(true);
    homeViewDescription.setWidth("100%");
    
    homeview_content.addComponent(homeViewDescription);

    //TODO ?
    /*
    if (generalOpenBISInformation.numberOfDatasets > 0) {
      String lastSample = "No samples available";
      if (generalOpenBISInformation.lastChangedSample != null) {
        lastSample = generalOpenBISInformation.lastChangedSample.split("/")[2];
      }
      statistics.addComponent(new Label(String.format("Last change %s", String.format(
          "occurred in sample %s (%s)", lastSample,
          generalOpenBISInformation.lastChangedDataset.toString()))));
    }
    */

    // table section
    this.table.setSelectable(true);

    /* using an absolute width here; otherwise it leads broken homeView visualization in Chrome
    int browser_window_width = UI.getCurrent().getPage().getBrowserWindowWidth();
    int table_width;

    if (browser_window_width > 1440) {
      table_width = (int) Math.floor(0.45 * browser_window_width);
    } else {
      table_width = (int) Math.floor(0.7 * browser_window_width);
    }
    */
    //HorizontalLayout button_bar = new HorizontalLayout();

    //this.export = new Button("Export as TSV");
    //button_bar.addComponent(this.export);
    //button_bar.setMargin(true);
    //homeview_content.addComponent(button_bar);
    
    //TODO FIX ITTTTT
    //FileDownloader fileDownloader = new FileDownloader(sr);
    //fileDownloader.extend(this.export);
    
    //this.table.setWidth(table_width, Unit.PIXELS);    
    this.table.setColumnExpandRatio("Name", 1);
    this.table.setColumnExpandRatio("Description", 3);
    this.table.setColumnExpandRatio("Contains datasets", 1);
    this.table.setColumnAlignment("Contains datasets", Align.CENTER);
    
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSectionContent.setCaption("Registered Projects");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);
    //tableSectionContent.addComponent(button_bar);

    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    homeview_content.addComponent(tableSection);
    //this.setContent(homeview_content);
    this.addComponent(homeview_content);
  }


  private void updateCaption() {
    this.setCaption(caption);

  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Project"));
  }


  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    // filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);
    
    // filterTable.setCaption("Registered Projects");

    return filterTable;
  }
  
  @Override
  public void enter(ViewChangeEvent event) {

  }
}
