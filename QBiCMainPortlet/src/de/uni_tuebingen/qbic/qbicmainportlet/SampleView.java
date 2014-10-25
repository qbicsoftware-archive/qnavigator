package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.CustomTable.RowHeaderMode;

public class SampleView extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;
  FilterTable table;
  VerticalLayout vert;
  private IndexedContainer datasets;
  private ButtonLink download;
  private final String DOWNLOAD_BUTTON_CAPTION = "Download marked files";
  private final String[] SAMPLEVIEW_TABLE_COLUMNS = new String[] {"File Name", "File Type",
      "Dataset Type", "Registration Date", "Validated", "File Size"};

  private String id;

  public SampleView(FilterTable table, IndexedContainer datasource, String id) {
    this.vert = new VerticalLayout();
    this.id = id;

    this.table = buildFilterTable();
    this.table.setSelectable(true);
    this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.vert.setSpacing(true);

    this.table.setContainerDataSource(datasource);

    this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));

    MpPortletListener mppl = new MpPortletListener(this.download, this.table);
    this.table.addValueChangeListener(mppl);
    if (VaadinSession.getCurrent() instanceof VaadinPortletSession) {
      VaadinPortletSession portletsession = (VaadinPortletSession) VaadinSession.getCurrent();

      // Add a custom listener to handle action and
      // render requests.
      portletsession.addPortletListener(mppl);

    }
    // this.tableClickChangeTreeView();
  }

  public SampleView() {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new IndexedContainer(), "No Sample has been selected!");
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Sample. The id is shown in the caption.
   * 
   * @param sampleInformation
   * @param id
   */
  public void setContainerDataSource(SampleInformation sampInformation, String id) {
    this.setStatistics(sampInformation);

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    // this.download.setStyleName(Reindeer.BUTTON_SMALL);
    buttonLayout.addComponent(this.download);

    this.vert.addComponent(buttonLayout);

    this.table.setContainerDataSource(sampInformation.datasets);
    this.table.setVisibleColumns((Object[]) SAMPLEVIEW_TABLE_COLUMNS);
    this.id = id;
    this.updateCaption();
  }

  private void setStatistics(SampleInformation sampInformation) {
    this.vert.removeAllComponents();

    VerticalLayout statistics = new VerticalLayout();

    statistics
        .addComponent(new Label(String.format("Sample Type: %s", sampInformation.sampleType)));
    statistics.addComponent(new Label(String.format("Number of Datasets: %s",
        sampInformation.numberOfDatasets)));

    statistics.addComponent(new Label(sampInformation.propertiesFormattedString, ContentMode.HTML));

    this.vert.setSpacing(true);
    this.vert.setMargin(true);
    this.vert.addComponent(statistics);

    Label parents = new Label(sampInformation.parentsFormattedString, ContentMode.HTML);

    statistics.addComponent(parents);

    if (sampInformation.numberOfDatasets > 0) {

      String lastDataset = "No Datasets available!";
      if (sampInformation.lastChangedDataset != null) {
        lastDataset = sampInformation.lastChangedDataset.toString();
        statistics.addComponent(new Label(String.format(
            "Last Change: %s",
            String.format("Dataset added on %s",
                lastDataset))));
      } else {
        statistics.addComponent(new Label(lastDataset));
      }
    }

    this.vert.addComponent(this.table);
  }

  public void setSizeFull() {
    this.vert.setSizeFull();
    this.table.setSizeFull();
    // super.setSizeFull();
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Space.
   * The id is shown in the caption.
   * 
   * @param spaceViewIndexedContainer
   * @param id
   */
  public void setContainerDataSource(IndexedContainer spaceViewIndexedContainer, String id) {
    this.datasets = (IndexedContainer) spaceViewIndexedContainer;
    this.table.setContainerDataSource(this.datasets);

    this.table.setColumnCollapsed("state", true);

    // this.table.setVisibleColumns((Object[]) SAMPLEVIEW_TABLE_COLUMNS);
    this.id = id;

    this.updateCaption();
    this.setSizeFull();
  }

  private void updateCaption() {
    this.setCaption(String.format("Statistics of Sample: %s", id));
  }

  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);
    filterTable.setMultiSelect(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    filterTable.setContainerDataSource(this.datasets);

    filterTable.setCaption("Registered Datasets");

    return filterTable;
  }
}
