package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;
import model.SampleBean;

import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.FilterTreeTable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class SampleView extends VerticalLayout implements View{

  /**
   * 
   */
  private static final long serialVersionUID = 377522772714840963L;
  static String navigateToLabel = "sample";
  FilterTreeTable table;
  VerticalLayout vert;
  private HierarchicalContainer datasets;
  private ButtonLink download;
  private Button export;
  private final String DOWNLOAD_BUTTON_CAPTION = "Download";
  private final String[] SAMPLEVIEW_TABLE_COLUMNS = new String[] {"File Name", "File Type",
      "Dataset Type", "Registration Date", "Validated", "File Size"};

  private String id;

  public SampleView(FilterTable table, HierarchicalContainer datasource, String id) {
    this.vert = new VerticalLayout();
    this.id = id;

    this.table = buildFilterTable();
    this.table.setSelectable(true);
    //this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    //this.setContent(vert);
    this.addComponent(vert);
    
    //this.vert.setSpacing(true);

    this.table.setContainerDataSource(datasource);

    this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));
    this.download.setEnabled(false);
    //MpPortletListener mppl = new MpPortletListener(this.download, this.table);
    //this.table.addValueChangeListener(mppl);
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
    this(new FilterTable(), new HierarchicalContainer(), "No Sample has been selected!");
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Sample. The id is shown in the caption.
   * 
   * @param sampleInformation
   * @param id
   */
  public void setContainerDataSource(SampleBean sampleBean, String id) {
    this.setStatistics(sampleBean);

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    // this.download.setStyleName(Reindeer.BUTTON_SMALL);
    buttonLayout.addComponent(this.download);
    
    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.vert.addComponent(buttonLayout);

    this.table.setContainerDataSource(sampleBean.getDatasets());
    this.table.setVisibleColumns((Object[]) SAMPLEVIEW_TABLE_COLUMNS);
    this.id = id;
    
    //TODO FIX THAT
    //DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    //StreamResource sr = Utils.getTSVStream(Utils.containerToString(sampInformation.datasets), this.id);
    //FileDownloader fileDownloader = new FileDownloader(sr);
    //fileDownloader.extend(this.export);
    
   // this.updateCaption();
  }

  private void setStatistics(SampleBean sampleBean) {
    this.vert.removeAllComponents();
    
    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();

    vert.setWidth("100%");
    this.setWidth(String.format("%spx", (browserWidth * 0.6)));
    this.setHeight(String.format("%spx", (browserHeight * 0.8)));
    
    MenuBar menubar = new MenuBar();
    // A top-level menu item that opens a submenu
    
    //set to true for the hack below
    menubar.setHtmlContentAllowed(true);
    this.vert.addComponent(menubar);

    menubar.addStyleName("qbicmainportlet");
    menubar.setWidth(100.0f, Unit.PERCENTAGE);    
    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_test2.png"));
    downloadProject.setEnabled(false);
    
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_test2.png"));
    manage.setEnabled(false);

    // Another top-level item
    MenuItem workflows = menubar.addItem("Run workflows", null, null);
    workflows.setIcon(new ThemeResource("dna_test2.png"));
    workflows.setEnabled(false);
            
    // Yet another top-level item
    MenuItem analyze = menubar.addItem("Analyze your data", null, null);
    analyze.setIcon(new ThemeResource("graph_test2.png"));
    analyze.setEnabled(false);
    
    // Description of sample   
    VerticalLayout sampleDescription = new VerticalLayout();
    VerticalLayout sampleDescriptionContent = new VerticalLayout();
    sampleDescriptionContent.setMargin(true);
    sampleDescriptionContent.setCaption("Description");
    sampleDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);
    sampleDescriptionContent.addComponent(new Label(String.format("%s.", sampleBean.getType())));
    
    //TODO 
    //sampleDescriptionContent.addComponent(new Label(sampleBean.generateParentsFormattedString(), ContentMode.HTML));
    sampleDescription.addComponent(sampleDescriptionContent);
    sampleDescription.setMargin(true);
    this.vert.addComponent(sampleDescription);
    
    
    // Statistics of sample
    int numberOfDatasets = sampleBean.getDatasets().size();
    VerticalLayout statistics = new VerticalLayout();
    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(String.format("%s dataset(s).",
        numberOfDatasets)));
    statContent.setMargin(true);
    statContent.setSpacing(true);   
    if (numberOfDatasets > 0) {

      String lastDataset = "No Datasets available!";
      if (sampleBean.getLastChangedDataset() != null) {
        lastDataset = sampleBean.getLastChangedDataset().toString();
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
    
    //TODO 
    //propertiesContent.addComponent(new Label(sampleBean.generatePropertiesFormattedString(), ContentMode.HTML));  
    properties.addComponent(propertiesContent);
    properties.setMargin(true);
    this.vert.addComponent(properties);

    // Experimental factors of sample
    VerticalLayout experimentalFactors = new VerticalLayout();
    VerticalLayout experimentalFactorsContent = new VerticalLayout();
    experimentalFactorsContent.setCaption("Experimental Factors");
    experimentalFactorsContent.setIcon(FontAwesome.TH);
    
    //TODO 
    //experimentalFactorsContent.addComponent(new Label(sampInformation.xmlPropertiesFormattedString, ContentMode.HTML));  
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
    
    tableSectionContent.setWidth("100%");
    tableSection.setWidth("100%");
    this.table.setWidth("100%");
    
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

  private FilterTreeTable buildFilterTable() {
    FilterTreeTable filterTable = new FilterTreeTable();
    
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

    if(this.datasets != null) {
      filterTable.setContainerDataSource(this.datasets);
    }
    //filterTable.setCaption("Registered Datasets");

    return filterTable;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    //System.out.println("currentValue: " + currentValue);
    //System.out.println("navigateToLabel: " + navigateToLabel);
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    try {
      this.setContainerDataSource(dh.getSampleInformation(currentValue), currentValue);
    } catch (Exception e) {
      System.out.println("Exception in SampleView.enter");
      // e.printStackTrace();
    }
    
  } 
}