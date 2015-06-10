package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.util.ArrayList;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentBean;

import org.tepi.filtertable.FilterTable;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

public class ExperimentView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = -9156593640161721690L;
  static Logger LOGGER = new Log4j2Logger(ExperimentView.class);
  static String navigateToLabel = "experiment";
  FilterTable table;
  VerticalLayout expview_content;

  private Button export;
  private DataHandler datahandler;
  private State state;
  private String resourceUrl;
  private VerticalLayout buttonLayoutSection;
  private FileDownloader fileDownloader;
  private ExperimentBean currentBean;
  private MenuBar menubar;
  private MenuItem downloadCompleteProjectMenuItem;
  private MenuItem datasetOverviewMenuItem;
  private MenuItem createBarcodesMenuItem;
  private Label generalInfoLabel;
  private Label statContentLabel;
  private Label propertiesContentLabel;


  public ExperimentView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }


  public ExperimentView(DataHandler datahandler, State state) {
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

    expview_content = new VerticalLayout();
    expview_content.addComponent(initMenuBar());
    expview_content.addComponent(initDescription());
    expview_content.addComponent(initStatistics());
    expview_content.addComponent(initTable());
    expview_content.addComponent(initButtonLayout());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    expview_content.setWidth("100%");
    this.addComponent(expview_content);
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

    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayout.addComponent(this.export);
    return buttonLayout;
  }

  void updateContentButtonLayout() {
    if (fileDownloader != null)
      this.export.removeExtension(fileDownloader);
    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getSamples()), currentBean.getId());
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
    MenuItem downloadExperiment = menubar.addItem("Download your data", null, null);
    downloadExperiment.setIcon(new ThemeResource("computer_higher.png"));
    downloadExperiment.addSeparator();
    downloadExperiment.setEnabled(false);
    this.downloadCompleteProjectMenuItem =
        downloadExperiment
            .addItem(
                "<a href=\""
                    + resourceUrl
                    + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete experiment</a>",
                null);

    // Open DatasetView
    this.datasetOverviewMenuItem = downloadExperiment.addItem("Dataset Overview", null);
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

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentMenuBar() {

    downloadCompleteProjectMenuItem.getParent().setEnabled(currentBean.getContainsData());
    downloadCompleteProjectMenuItem
        .setText("<a href=\""
            + resourceUrl
            + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete experiment</a>");

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
    VerticalLayout generalInfo = new VerticalLayout();
    VerticalLayout generalInfoContent = new VerticalLayout();
    generalInfoContent.setCaption("General Information");
    generalInfoContent.setIcon(FontAwesome.INFO);
    generalInfoLabel = new Label("");

    generalInfoContent.addComponent(generalInfoLabel);
    generalInfoContent.setMargin(true);
    generalInfo.setMargin(true);

    generalInfo.addComponent(generalInfoContent);



    return generalInfo;
  }

  void updateContentDescription() {
    generalInfoLabel.setValue(String.format("Kind:\t %s", currentBean.getType()));

  }

  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {
    VerticalLayout statistics = new VerticalLayout();

    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);


    // int numberOfDatasets = dh.datasetMap.get(experimentBean.getId()).size();
    statContentLabel = new Label("");

    statContent.addComponent(statContentLabel);
    // statContent.addComponent(new Label(String.format("%s dataset(s).",numberOfDatasets )));
    statContent.setMargin(true);
    statContent.setSpacing(true);

    /*
     * if (numberOfDatasets > 0) {
     * 
     * String lastSample = "No samples available"; if (experimentBean.getLastChangedSample() !=
     * null) { lastSample = experimentBean.getLastChangedSample();// .split("/")[2]; }
     * statContent.addComponent(new Label(String.format( "Last change %s",
     * String.format("occurred in sample %s (%s)", lastSample,
     * experimentBean.getLastChangedDataset().toString())))); }
     */


    statistics.addComponent(statContent);
    statistics.setMargin(true);

    // Properties of experiment
    VerticalLayout properties = new VerticalLayout();
    VerticalLayout propertiesContent = new VerticalLayout();
    propertiesContent.setCaption("Properties");
    propertiesContent.setIcon(FontAwesome.LIST_UL);
    propertiesContentLabel = new Label("", ContentMode.HTML);
    propertiesContent.addComponent(propertiesContentLabel);
    properties.addComponent(propertiesContent);
    properties.setMargin(true);
    statistics.addComponent(properties);

    return statistics;
  }

  /**
   * 
   */
  void updateContentStatistics() {
    statContentLabel.setValue(String.format("%s sample(s),", currentBean.getSamples().size()));
    propertiesContentLabel.setValue(currentBean.generatePropertiesFormattedString());
  }

  VerticalLayout initTable() {
    this.table = this.buildFilterTable();
    this.tableClickChangeTreeView();
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    tableSectionContent.setCaption("Registered Samples");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);

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

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Experiment. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param id
   */
  public void setContainerDataSource(ExperimentBean experimentBean) {
    this.currentBean = experimentBean;
    LOGGER.debug(String.valueOf(experimentBean.getSamples().size()));
    this.table.setContainerDataSource(experimentBean.getSamples());
    this.table.setVisibleColumns(new Object[] {"code", "type"});
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Sample"));
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

    filterTable.setColumnHeader("code", "Name");
    filterTable.setColumnHeader("type", "Type");

    return filterTable;
  }


  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    LOGGER.debug(currentValue);
    // TODO updateContent only if currentExperiment is not equal to newExperiment
    this.table.unselect(this.table.getValue());
    this.setContainerDataSource(datahandler.getExperiment2(currentValue));

    updateContent();
  }


  public ExperimentBean getCurrentBean() {
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
