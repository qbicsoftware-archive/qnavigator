package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class SampleView extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;
  FilterTable table;
  VerticalLayout vert;
  private IndexedContainer datasets;
  private ButtonLink download;
  private Button export;
  private final String DOWNLOAD_BUTTON_CAPTION = "Download";
  private final String[] SAMPLEVIEW_TABLE_COLUMNS = new String[] {"File Name", "File Type",
      "Dataset Type", "Registration Date", "Validated", "File Size"};

  private String id;

  public SampleView(FilterTable table, IndexedContainer datasource, String id) {
    this.vert = new VerticalLayout();
    this.id = id;

    this.table = buildFilterTable();
    this.table.setSelectable(true);
    //this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    //this.vert.setSpacing(true);

    this.table.setContainerDataSource(datasource);

    this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));
    this.download.setEnabled(false);
    MpPortletListener mppl = new MpPortletListener(this.download, this.table);
    this.table.addValueChangeListener(mppl);
    if (VaadinSession.getCurrent() instanceof VaadinPortletSession) {
      VaadinPortletSession portletsession = (VaadinPortletSession) VaadinSession.getCurrent();

      // Add a custom listener to handle action and
      // render requests.
     // portletsession.addPortletListener(mppl);

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
    
    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.vert.addComponent(buttonLayout);

    this.table.setContainerDataSource(sampInformation.datasets);
    this.table.setVisibleColumns((Object[]) SAMPLEVIEW_TABLE_COLUMNS);
    this.id = id;
    
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    StreamResource sr = dh.getTSVStream(dh.containerToString(sampInformation.datasets), this.id);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
    
    this.updateCaption();
  }

  private void setStatistics(SampleInformation sampInformation) {
    this.vert.removeAllComponents();
    
    // Description of sample   
    VerticalLayout sampleDescription = new VerticalLayout();
    VerticalLayout sampleDescriptionContent = new VerticalLayout();
    sampleDescriptionContent.setMargin(true);
    sampleDescriptionContent.setCaption("Description");
    sampleDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);
    sampleDescriptionContent.addComponent(new Label(String.format("%s.", sampInformation.sampleType)));
    sampleDescriptionContent.addComponent(new Label(sampInformation.parentsFormattedString, ContentMode.HTML));
    sampleDescription.addComponent(sampleDescriptionContent);
    sampleDescription.setMargin(true);
    this.vert.addComponent(sampleDescription);
    
    
    // Statistics of sample
    VerticalLayout statistics = new VerticalLayout();
    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(String.format("%s dataset(s).",
        sampInformation.numberOfDatasets)));
    statContent.setMargin(true);
    statContent.setSpacing(true);   
    if (sampInformation.numberOfDatasets > 0) {

      String lastDataset = "No Datasets available!";
      if (sampInformation.lastChangedDataset != null) {
        lastDataset = sampInformation.lastChangedDataset.toString();
        statContent.addComponent(new Label(String.format(
            "Last Change: %s",
            String.format("Dataset added on %s",
                lastDataset))));
      } else {
        statContent.addComponent(new Label(lastDataset));
      }
    }  
    statistics.addComponent(statContent);
    statistics.setMargin(true);
    this.vert.addComponent(statistics);
    
    
    // Properties of sample
    VerticalLayout properties = new VerticalLayout();
    VerticalLayout propertiesContent = new VerticalLayout();
    propertiesContent.setCaption("Properties");
    propertiesContent.setIcon(FontAwesome.LIST_UL);
    propertiesContent.addComponent(new Label(sampInformation.propertiesFormattedString, ContentMode.HTML));  
    properties.addComponent(propertiesContent);
    properties.setMargin(true);
    this.vert.addComponent(properties);

    // Experimental factors of sample
    VerticalLayout experimentalFactors = new VerticalLayout();
    VerticalLayout experimentalFactorsContent = new VerticalLayout();
    experimentalFactorsContent.setCaption("Experimental Factors");
    experimentalFactorsContent.setIcon(FontAwesome.TH);
    experimentalFactorsContent.addComponent(new Label(sampInformation.xmlPropertiesFormattedString, ContentMode.HTML));  
    experimentalFactors.addComponent(experimentalFactorsContent);
    experimentalFactors.setMargin(true);
    this.vert.addComponent(experimentalFactors);
    
    /*VerticalLayout statistics = new VerticalLayout();

    statistics
        .addComponent(new Label(String.format("Kind: %s", sampInformation.sampleType)));
    statistics.addComponent(new Label(String.format("# Datasets: %s",
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
    */
    // Table (containing datasets) section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    
    tableSectionContent.setCaption("Registered Datasets");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);
    
    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    
    tableSection.addComponent(tableSectionContent);
    this.vert.addComponent(tableSection);
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
  /*public void setContainerDataSource(IndexedContainer spaceViewIndexedContainer, String id) {
    this.datasets = (IndexedContainer) spaceViewIndexedContainer;
    this.table.setContainerDataSource(this.datasets);

    this.table.setColumnCollapsed("state", true);

    // this.table.setVisibleColumns((Object[]) SAMPLEVIEW_TABLE_COLUMNS);
    this.id = id;

    this.updateCaption();
    this.setSizeFull();
  }
*/
  private void updateCaption() {
    this.setCaption(String.format("Viewing Sample %s", id));
  }

  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    //filterTable.setSizeFull();

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

    //filterTable.setCaption("Registered Datasets");

    return filterTable;
  } 
}