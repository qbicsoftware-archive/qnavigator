package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import model.SampleBean;

import org.tepi.filtertable.FilterTreeTable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

public class SampleView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;
  
  private logging.Logger LOGGER = new Log4j2Logger(SampleView.class);
  
  static String navigateToLabel = "sample";
  FilterTreeTable table;
  VerticalLayout vert;
  private HierarchicalContainer datasets;
  private Button export;

  private DataHandler datahandler;
  private String resourceUrl;
  private State state;
  private VerticalLayout sampview_content;
  private VerticalLayout buttonLayoutSection;
  private FileDownloader fileDownloader;
  private SampleBean currentBean;
  private MenuBar menubar;
  private MenuItem downloadCompleteProjectMenuItem;
  private MenuItem datasetOverviewMenuItem;
  private MenuItem createBarcodesMenuItem;
  private Label sampleTypeLabel;
  private Label sampleParentLabel;
  private Label numberOfDatasetsLabel;
  private Label lastChangedDatasetLabel;
  private Label propertiesLabel;
  private Label experimentalFactorLabel;


  public SampleView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }


  public SampleView(DataHandler datahandler, State state) {
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
    sampview_content = new VerticalLayout();
    sampview_content.addComponent(initMenuBar());
    sampview_content.addComponent(initDescription());
    sampview_content.addComponent(initStatistics());
    sampview_content.addComponent(initTable());
    sampview_content.addComponent(initButtonLayout());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    sampview_content.setWidth("100%");
    this.addComponent(sampview_content);
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
    buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.addComponent(this.export);
    return buttonLayout;
  }

  void updateContentButtonLayout() {
    if (fileDownloader != null)
      this.export.removeExtension(fileDownloader);
    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getDatasets()), currentBean.getId());
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
    MenuItem downloadSample = menubar.addItem("Download your data", null, null);
    downloadSample.setIcon(new ThemeResource("computer_higher.png"));
    downloadSample.addSeparator();
    downloadSample.setEnabled(false);
    this.downloadCompleteProjectMenuItem =
        downloadSample
            .addItem(
                "<a href=\""
                    + resourceUrl
                    + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete sample</a>",
                null);

    // Open DatasetView
    this.datasetOverviewMenuItem = downloadSample.addItem("Dataset Overview", null);
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));

    this.createBarcodesMenuItem = manage.addItem("Create Barcodes", null, null);

    /*
     * MenuItem workflows = menubar.addItem("Run workflows", null, null); workflows.setIcon(new
     * ThemeResource("dna_higher.png")); workflows.setEnabled(false);
     * 
     * MenuItem analyze = menubar.addItem("Analyze your data", null, null); analyze.setIcon(new
     * ThemeResource("graph_higher.png")); analyze.setEnabled(false);
     */

    return menubar;
  }

  boolean containsDatasets() {
    return currentBean.getDatasets() != null && currentBean.getDatasets().size() > 0;
  }


  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentMenuBar() {
    downloadCompleteProjectMenuItem.getParent().setEnabled(containsDatasets());
    downloadCompleteProjectMenuItem
        .setText("<a href=\""
            + resourceUrl
            + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete sample</a>");

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
    VerticalLayout sampleDescription = new VerticalLayout();
    VerticalLayout sampleDescriptionContent = new VerticalLayout();
    //sampleDescriptionContent.setMargin(true);
    sampleDescriptionContent.setCaption("Description");
    sampleDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);
    sampleTypeLabel = new Label("");
    sampleDescriptionContent.addComponent(sampleTypeLabel);
    sampleParentLabel = new Label("", ContentMode.HTML);
    sampleDescriptionContent.addComponent(sampleParentLabel);
    sampleDescriptionContent.setMargin(new MarginInfo(false, false, false, true));
    sampleDescription.addComponent(sampleDescriptionContent);
    sampleDescription.setMargin(new MarginInfo(false, false, false, true));
    return sampleDescription;
  }

  void updateContentDescription() {
    sampleTypeLabel.setValue(String.format("%s.", currentBean.getType()));
    sampleParentLabel.setValue(currentBean.getParentsFormattedString());
  }

  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {

    // Statistics of sample
    VerticalLayout statistics = new VerticalLayout();
    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    numberOfDatasetsLabel = new Label("");
    statContent.addComponent(numberOfDatasetsLabel);
    lastChangedDatasetLabel = new Label("");
    statContent.addComponent(lastChangedDatasetLabel);
    statContent.setMargin(new MarginInfo(false, false, false, true));
    //statContent.setMargin(true);
    //statContent.setSpacing(true);

    statistics.addComponent(statContent);
    //statistics.setMargin(true);


    // Properties of sample
    VerticalLayout properties = new VerticalLayout();
    VerticalLayout propertiesContent = new VerticalLayout();
    propertiesContent.setCaption("Properties");
    propertiesContent.setIcon(FontAwesome.LIST_UL);
    propertiesLabel = new Label("", ContentMode.HTML);

    propertiesContent.addComponent(propertiesLabel);
    propertiesContent.setMargin(new MarginInfo(false, false, false, true));

    properties.addComponent(propertiesContent);
    //properties.setMargin(true);
    statistics.addComponent(properties);

    // Experimental factors of sample
    VerticalLayout experimentalFactors = new VerticalLayout();
    VerticalLayout experimentalFactorsContent = new VerticalLayout();
    experimentalFactorsContent.setCaption("Experimental Factors");
    experimentalFactorsContent.setIcon(FontAwesome.TH);
    experimentalFactorLabel = new Label("", ContentMode.HTML);
    experimentalFactorsContent.addComponent(experimentalFactorLabel);
    experimentalFactorsContent.setMargin(new MarginInfo(false, false, false, true));

    experimentalFactors.addComponent(experimentalFactorsContent);
    statistics.addComponent(experimentalFactors);
    statistics.setSpacing(true);
    statistics.setMargin(new MarginInfo(false, false, false, true));
    return statistics;
  }


  /**
   * 
   */
  void updateContentStatistics() {
    int numberOfDatasets = currentBean.getDatasets().size();
    numberOfDatasetsLabel.setValue(String.format("%s dataset(s).", numberOfDatasets));
    if (numberOfDatasets > 0) {

      String lastDataset = "No Datasets available!";
      if (currentBean.getLastChangedDataset() != null) {
        lastDataset = currentBean.getLastChangedDataset().toString();
        lastChangedDatasetLabel.setValue(String.format("Last Change: %s",
            String.format("Dataset added on %s", lastDataset)));
      } else {
        lastChangedDatasetLabel.setValue(lastDataset);
      }
    }

    try {
      propertiesLabel.setValue(currentBean.generatePropertiesFormattedString());
      experimentalFactorLabel.setValue(currentBean.generateXMLPropertiesFormattedString());
    } catch (JAXBException e) {
      LOGGER.error(String.format("failed to parse experimental factors for sample %s", currentBean.getId()),e);
    }
  }


  VerticalLayout initTable() {
    this.table = this.buildFilterTable();

    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    tableSectionContent.setCaption("Registered Datasets");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(new MarginInfo(false, false, false, true));
    //tableSection.setMargin(true);
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    tableSection.setMargin(new MarginInfo(false, false, false, true));

    return tableSection;
  }


  void updateContentTable() {
    // Nothing to do here at the moment
    // table is already set in setdataresource
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

  public void setContainerDataSource(SampleBean sampleBean) {
    this.currentBean = sampleBean;

    this.table.setContainerDataSource(sampleBean.getDatasets());
    this.table.setVisibleColumns(new Object[] {"name", "type", "registrationDate", "fileSize"});
  }

  private FilterTreeTable buildFilterTable() {
    FilterTreeTable filterTable = new FilterTreeTable();

    // filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);
    filterTable.setMultiSelect(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    if (this.datasets != null) {
      filterTable.setContainerDataSource(this.datasets);
    }
    filterTable.setColumnHeader("name", "Name");
    filterTable.setColumnHeader("type", "Type");
    filterTable.setColumnHeader("registrationDate", "Registration Date");
    filterTable.setColumnHeader("fileSize", "Size");

    return filterTable;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    // TODO updateContent only if currentExperiment is not equal to newExperiment
    this.table.unselect(this.table.getValue());
    this.setContainerDataSource(datahandler.getSample(currentValue));

    updateContent();

  }

  public SampleBean getCurrentBean() {
    return currentBean;
  }


  /**
   * Enables or disables the component. The user can not interact disabled components, which are
   * shown with a style that indicates the status, usually shaded in light gray color. Components
   * are enabled by default.
   */
  public void setEnabled(boolean enabled) {
    this.export.setEnabled(enabled);
    this.table.setEnabled(enabled);
    // this.createBarcodesMenuItem.getParent().setEnabled(false);
    // this.downloadCompleteProjectMenuItem.getParent().setEnabled(false);
    this.menubar.setEnabled(enabled);
  }

}
