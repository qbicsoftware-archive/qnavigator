package de.uni_tuebingen.qbic.qbicmainportlet;


import org.tepi.filtertable.FilterTable;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.CustomTable.RowHeaderMode;

public class HomeView extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private String caption;
  FilterTable table;
  VerticalLayout homeview_content;

  DataHandler dh;
  StreamResource sr;

  private Button export;

  public HomeView(SpaceInformation datasource, String caption) {
    homeview_content = new VerticalLayout();
    this.table = buildFilterTable();

    this.setContainerDataSource(datasource, caption);
    dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    sr = dh.getTSVStream(dh.containerToString(datasource.projects), this.caption);
    this.tableClickChangeTreeView();

    this.buildLayout(datasource);
  }

  public HomeView(FilterTable table, SpaceInformation datasource, String caption) {
    homeview_content = new VerticalLayout();
    this.table = table;

    this.setContainerDataSource(datasource, caption);
    dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    sr = dh.getTSVStream(dh.containerToString(datasource.projects), this.caption);
    this.tableClickChangeTreeView();

    this.buildLayout(datasource);

  }


  /**
   * execute the above constructor with default settings, in order to have the same settings
   */
  public HomeView() {
    this(new FilterTable(), new SpaceInformation(),
        "You seem to have no registered projects. Please contact QBiC.");
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
  public void setContainerDataSource(SpaceInformation homeViewInformation, String caption) {

    this.caption = caption;
    this.updateCaption();
    // this.setStatistics(homeViewInformation);

    this.table.setContainerDataSource(homeViewInformation.projects);



  }


  private void buildLayout(SpaceInformation generalOpenBISInformation) {
    // clean up first
    homeview_content.removeAllComponents();

    homeview_content.setMargin(true);
    homeview_content.setWidth("100%");

    this.setHeight("600px");

    //System.out.println(UI.getCurrent().getPage().getBrowserWindowHeight());
    //System.out.println(UI.getCurrent().getPage().getBrowserWindowWidth());


    // view overall statistics
    VerticalLayout statistics = new VerticalLayout();

    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");

    statContent.addComponent(new Label(String.format("You have %s project(s),",
        generalOpenBISInformation.numberOfProjects)));
    statContent.addComponent(new Label(String.format("%s experiment(s),",
        generalOpenBISInformation.numberOfExperiments)));
    statContent.addComponent(new Label(String.format("%s sample(s),",
        generalOpenBISInformation.numberOfSamples)));
    statContent.addComponent(new Label(String.format("and %s dataset(s).",
        generalOpenBISInformation.numberOfDatasets)));
    statContent.setMargin(true);
    statContent.setSpacing(true);

    statistics.addComponent(statContent);
    statistics.setMargin(true);
    homeview_content.addComponent(statistics);


    if (generalOpenBISInformation.numberOfDatasets > 0) {
      String lastSample = "No samples available";
      if (generalOpenBISInformation.lastChangedSample != null) {
        lastSample = generalOpenBISInformation.lastChangedSample.split("/")[2];
      }
      statContent.addComponent(new Label(String.format("Last change %s", String.format(
          "occurred in sample %s (%s)", lastSample,
          generalOpenBISInformation.lastChangedDataset.toString()))));
    }


    this.table.setSelectable(true);


    int browser_window_width = UI.getCurrent().getPage().getBrowserWindowWidth();
    int table_width;

    if (browser_window_width > 1440) {
      table_width = (int) Math.floor(0.5 * browser_window_width);
    }
    else {
      table_width = (int) Math.floor(0.7 * browser_window_width);
    }
    
    // using an absolute width here; otherwise it leads broken homeView visualization
    this.table.setWidth(table_width, Unit.PIXELS);
    
    this.table.setCaption("Registered Projects");
    this.table.setIcon(FontAwesome.FLASK);
    
    
    homeview_content.addComponent(this.table);
    // homeview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);

    HorizontalLayout button_bar = new HorizontalLayout();

    this.export = new Button("Export as TSV");
    button_bar.addComponent(this.export);
    homeview_content.addComponent(button_bar);


    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);

    this.setContent(homeview_content);
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

    //filterTable.setCaption("Registered Projects");

    return filterTable;
  }

}
