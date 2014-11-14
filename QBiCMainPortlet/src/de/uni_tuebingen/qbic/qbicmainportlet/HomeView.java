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
    
    this.setHeight("600px");
    
    // view overall statistics
    homeview_content.addComponent(new Label(String.format("Number of Projects: %s",
        generalOpenBISInformation.numberOfProjects)));
    homeview_content.addComponent(new Label(String.format("Number of Experiments: %s",
        generalOpenBISInformation.numberOfExperiments)));
    homeview_content.addComponent(new Label(String.format("Number of Samples: %s",
        generalOpenBISInformation.numberOfSamples)));
    homeview_content.addComponent(new Label(String.format("Number of Datasets: %s",
        generalOpenBISInformation.numberOfDatasets)));
    
    Label vertical_spacer1 = new Label("");
    vertical_spacer1.setHeight("0.5em");
    homeview_content.addComponent(vertical_spacer1);
    
    if (generalOpenBISInformation.numberOfDatasets > 0) {
      String lastSample = "No Sample available";
      if (generalOpenBISInformation.lastChangedSample != null) {
        lastSample = generalOpenBISInformation.lastChangedSample.split("/")[2];
      }
      homeview_content.addComponent(new Label(String.format("Last Change: %s", String.format(
          "In Sample: %s. Date: %s", generalOpenBISInformation.lastChangedSample.split("/")[2],
          generalOpenBISInformation.lastChangedDataset.toString()))));
    }
    
    this.table.setSelectable(true);
    this.table.setWidth("100%");
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

    filterTable.setCaption("Registered Projects");

    return filterTable;
  }

}
