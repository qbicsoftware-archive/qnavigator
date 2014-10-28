package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends Panel {

  FilterTable table;
  VerticalLayout vert;

  private String id;

  public ProjectView(FilterTable table, IndexedContainer datasource, String id) {
    vert = new VerticalLayout();
    this.id = id;

    this.table = this.buildFilterTable();
    this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.table.setContainerDataSource(datasource);
    this.tableClickChangeTreeView();
  }


  public ProjectView() {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new IndexedContainer(), "No project selected");
  }

  public void setSizeFull() {
    this.table.setSizeFull();
    vert.setSizeFull();
    super.setSizeFull();
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param id
   */
  public void setContainerDataSource(ProjectInformation projectInformation, String id) {
    this.setStatistics(projectInformation);
    this.table.setContainerDataSource(projectInformation.experiments);
    this.id = id;
    this.updateCaption();
  }

  @SuppressWarnings("deprecation")
  private void setStatistics(ProjectInformation projectInformation) {
    vert.removeAllComponents();

    VerticalLayout contact = new VerticalLayout();
    contact.addComponent(new Label("QBiC contact:"));
    contact.addComponent(new Label(projectInformation.contact));

    VerticalLayout statistics = new VerticalLayout();


    Label description = new Label(projectInformation.description);
    description.setWidth("400px");
    Label des = new Label("Description: ");
    HorizontalLayout projDescription = new HorizontalLayout();
    projDescription.addComponent(des);
    projDescription.addComponent(description);
    statistics.addComponent(projDescription);
    statistics.addComponent(new Label(String.format("Number of Experiments: %s",
        projectInformation.numberOfExperiments)));
    statistics.addComponent(new Label(String.format("Number of Samples: %s",
        projectInformation.numberOfSamples)));
    statistics.addComponent(new Label(String.format("Number of Datasets: %s",
        projectInformation.numberOfDatasets)));
    HorizontalLayout temp = new HorizontalLayout();
    temp.addComponent(new Label(String.format("Status: %s", projectInformation.statusMessage)));
    temp.addComponent(projectInformation.progressBar);
    // temp.setSizeFull();
    temp.setSpacing(true);
    statistics.addComponent(temp);
    if (projectInformation.numberOfDatasets > 0) {

      String lastSample = "No Sample available";
      if (projectInformation.lastChangedSample != null) {
        lastSample = projectInformation.lastChangedSample.split("/")[2];
      }
      statistics
          .addComponent(new Label(String.format("Last Change: %s", String.format(
              "In Sample: %s. Date: %s", lastSample,
              projectInformation.lastChangedDataset.toString()))));
    }
    HorizontalLayout head = new HorizontalLayout();
    head.addComponent(statistics);
    head.addComponent(contact);
    head.setMargin(true);
    head.setSpacing(true);
    vert.addComponent(head);
    vert.addComponent(this.table);
    this.table.setColumnAlignment("Status", FilterTable.ALIGN_CENTER);
  }


  private void updateCaption() {
    this.setCaption(String.format("Statistics of Project: %s", id));
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Experiment"));
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

    filterTable.setCaption("Registered Experiments");

    return filterTable;
  }
}
