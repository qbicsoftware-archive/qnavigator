package de.uni_tuebingen.qbic.qbicmainportlet;


import org.tepi.filtertable.FilterTable;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.CustomTable.RowHeaderMode;

public class HomeView extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;

  private String caption;
  FilterTable table;
  VerticalLayout vert;

  public HomeView(SpaceInformation datasource, String caption){
    vert = new VerticalLayout();
    this.table = buildFilterTable();
    vert.addComponent(new Label("Huhu"));
    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.setContainerDataSource(datasource, caption);
    this.tableClickChangeTreeView();
  }
  
  public HomeView(FilterTable table, SpaceInformation datasource, String caption) {
    vert = new VerticalLayout();
    this.table = table;
    this.table.setSelectable(true);
    this.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.setContainerDataSource(datasource, caption);
    this.tableClickChangeTreeView();
  }


  public HomeView(){
    //execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new SpaceInformation(), "You seem to have no registered projects. Please contact QBiC.");
  }

  public void setSizeFull(){
    vert.setSizeFull();
    super.setSizeFull();
    this.table.setSizeFull();
    vert.setSpacing(true);
    vert.setMargin(true);
  }

  /**
   * sets the ContainerDataSource of this view. Should usually contains project information. Caption is caption.
   * @param homeViewInformation
   * @param caption
   */
  public void setContainerDataSource(SpaceInformation homeViewInformation, String caption){

    //this.table.setContainerDataSource(spaceViewIndexedContainer);
    this.caption = caption;
    this.updateCaption();
    this.setStatistics(homeViewInformation);
    this.table.setContainerDataSource(homeViewInformation.projects);
  }


  private void setStatistics(SpaceInformation generalOpenBISInformation) {
    vert.removeAllComponents();
    vert.addComponent(new Label(String.format("Number of Projects: %s", generalOpenBISInformation.numberOfProjects)));
    vert.addComponent(new Label(String.format("Number of Experiments: %s", generalOpenBISInformation.numberOfExperiments)));
    vert.addComponent(new Label(String.format("Number of Samples: %s", generalOpenBISInformation.numberOfSamples)));
    vert.addComponent(new Label(String.format("Number of Datasets: %s", generalOpenBISInformation.numberOfDatasets)));

    if(generalOpenBISInformation.numberOfDatasets > 0){
      String lastSample = "No Sample available";
      if(generalOpenBISInformation.lastChangedSample != null){
          lastSample = generalOpenBISInformation.lastChangedSample.split("/")[2];
      }
      vert.addComponent(new Label(String.format("Last Change: %s", String.format("In Sample: %s. Date: %s",generalOpenBISInformation.lastChangedSample.split("/")[2], generalOpenBISInformation.lastChangedDataset.toString()))));
    }
    vert.addComponent(this.table);
  }


  private void updateCaption() {
    this.setCaption(caption);

  }

  private void tableClickChangeTreeView(){
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Project"));
  }

  
  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    filterTable.setSizeFull();

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
