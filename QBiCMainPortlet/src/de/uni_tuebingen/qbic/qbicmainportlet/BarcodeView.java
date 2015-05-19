package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.BarcodeFunctions;
import helpers.BarcodesReadyRunnable;
import helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import logging.Logger;
import main.BarcodeCreator;
import main.OpenBisClient;
import model.ExperimentBarcodeSummaryBean;
import model.IBarcodeBean;
import model.NewModelBarcodeBean;
import model.NewSampleModelBean;
import model.SortBy;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class BarcodeView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 8921847321758727061L;

  static String navigateToLabel = "barcodeview";
  static Logger LOGGER = new Log4j2Logger(BarcodeView.class);
  FilterTable table;
  VerticalLayout projectview_content;

  private String id;
  private Button prepareButton;
  private ProgressBar bar;
  private Label info;
  private Button sheetDownloadButton;
  private Button pdfDownloadButton;
  private Button resetButton;
  private OptionGroup comparators;

  private Button export;

  BarcodeCreator creator;
  ArrayList<IBarcodeBean> barcodeBeans;

  private OpenBisClient openbis;



  public BarcodeView(OpenBisClient openbisClient, FilterTable table, IndexedContainer datasource,
      String id, String scripts, String paths) {
    this.openbis = openbisClient;
    projectview_content = new VerticalLayout();

    this.creator = new BarcodeCreator(scripts, paths);

    this.id = id;

    this.table = this.buildFilterTable();
    // this.table.setSizeFull();

    projectview_content.addComponent(this.table);
    projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    // this.setContent(projectview_content);
    this.addComponent(projectview_content);

    // this whole constructor seems unnecessary btw.
    // List<ExperimentBarcodeSummaryBean> beans = parseExperimentBeans(datasource);
    // BeanItemContainer<ExperimentBarcodeSummaryBean> beanContainer =
    // new BeanItemContainer<ExperimentBarcodeSummaryBean>(ExperimentBarcodeSummaryBean.class);
    // beanContainer.addAll(beans);
    // table.setContainerDataSource(beanContainer);

  }

  public BarcodeView(OpenBisClient openBisClient, String scripts, String path) {
    // execute the above constructor with default settings, in order to have the same settings
    this(openBisClient, new FilterTable(), new IndexedContainer(), "No project selected", scripts,
        path);
  }

  public void setSizeFull() {
    this.table.setSizeFull();
    projectview_content.setSizeFull();
    super.setSizeFull();
  }

  public String getNavigatorLabel() {
    return navigateToLabel;
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param list
   * @param id
   */
  public void setContainerDataSource(List<ExperimentBarcodeSummaryBean> summary, String id) {
    this.id = id;
    this.setStatistics();

    VerticalLayout buttonLayoutSection = new VerticalLayout();
    buttonLayoutSection.setMargin(true);
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    buttonLayoutSection.addComponent(buttonLayout);

    prepareButton = new Button("Prepare Barcodes");
    prepareButton.setEnabled(false);

    buttonLayout.addComponent(prepareButton);

    info = new Label();
    bar = new ProgressBar();
    buttonLayoutSection.addComponent(info);
    buttonLayoutSection.addComponent(bar);

    sheetDownloadButton = new Button("Download Sample Sheet");
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton = new Button("Download Tube Barcodes");
    pdfDownloadButton.setEnabled(false);
    resetButton = new Button("Reset Selection");
    resetButton.setEnabled(false);
    comparators = new OptionGroup("Sort Sheet by");
    comparators.addItems(SortBy.values());
    comparators.setValue(SortBy.ID);

    buttonLayoutSection.addComponent(comparators);
    buttonLayout.addComponent(sheetDownloadButton);
    buttonLayout.addComponent(pdfDownloadButton);
    buttonLayout.addComponent(resetButton);

    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.projectview_content.addComponent(buttonLayoutSection);

    BeanItemContainer<ExperimentBarcodeSummaryBean> beanContainer =
        new BeanItemContainer<ExperimentBarcodeSummaryBean>(ExperimentBarcodeSummaryBean.class);
    beanContainer.addAll(summary);
    table.setContainerDataSource(beanContainer);
    table.setSelectable(true);
    table.setMultiSelect(true);

    StreamResource sr = Utils.getTSVStream(Utils.containerToString(beanContainer), this.id);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);

    initControl();

    // this.updateCaption();
  }

  public Button getButtonTube() {
    return pdfDownloadButton;
  }

  public Button getButtonSheet() {
    return sheetDownloadButton;
  }

  private void initControl() {
    final BarcodeView instance = this; // needed for concurrency
    // TODO Auto-generated method stub
    /**
     * Button listeners
     */
    Button.ClickListener cl = new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Download Sample Sheet")) {
          creator.createAndDLSheet(barcodeBeans, (SortBy) comparators.getValue());
        }
        if (src.equals("Download Tube Barcodes")) {
          creator.zipAndDownloadBarcodes(barcodeBeans);
        }
        if (src.equals("Reset Selection")) {
          barcodeBeans = null;
          reset();
        }
        if (src.equals("Prepare Barcodes")) {
          creationPressed();
          barcodeBeans =
              getSamplesFromExperimentSummaries((Collection<ExperimentBarcodeSummaryBean>) table
                  .getValue());
          creator.findOrCreateBarcodesWithProgress(barcodeBeans, bar, info,
              new BarcodesReadyRunnable(instance, creator, barcodeBeans));
        }
      }
    };
    prepareButton.addClickListener(cl);
    resetButton.addClickListener(cl);
    pdfDownloadButton.addClickListener(cl);
    sheetDownloadButton.addClickListener(cl);

    /**
     * Experiment selection listener
     */

    ValueChangeListener expSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        Collection<ExperimentBarcodeSummaryBean> exps =
            (Collection<ExperimentBarcodeSummaryBean>) table.getValue();
        if (exps.size() > 0) {
          newExperimentSelected(true);
        } else {
          newExperimentSelected(false);
        }
      }

    };
    table.addValueChangeListener(expSelectListener);

  }

  protected ArrayList<IBarcodeBean> getSamplesFromExperimentSummaries(
      Collection<ExperimentBarcodeSummaryBean> experiments) {
    ArrayList<NewSampleModelBean> samples = new ArrayList<NewSampleModelBean>();
    for (ExperimentBarcodeSummaryBean b : experiments) {
      for (Sample s : openbis.getSamplesofExperiment(b.getExperimentID())) {
        String type = s.getSampleTypeCode();
        String bioType = "unknown";
        if (type.equals("Q_BIOLOGICAL_SAMPLE")) {
          bioType = s.getProperties().get("Q_PRIMARY_TISSUE");
        }
        if (type.equals("Q_TEST_SAMPLE")) {
          bioType = s.getProperties().get("Q_SAMPLE_TYPE");
        }
        if (type.equals("Q_NGS_MEASUREMENT")) {
          bioType = s.getProperties().get("Q_SEQUENCING_TYPE");
        }
        String secName = s.getProperties().get("Q_SECONDARY_NAME");
        
        if(secName == null) {
          samples.add(new NewSampleModelBean(s.getCode(), "",
              bioType));
        }
        else {
          samples.add(new NewSampleModelBean(s.getCode(), s.getProperties().get("Q_SECONDARY_NAME"),
              bioType));
         
        }
         }
    }
    return translateBeans(samples);
  }

  protected ArrayList<IBarcodeBean> translateBeans(Collection<NewSampleModelBean> samples) {
    List<Sample> samplePool = openbis.getSamplesOfProject(this.id);
    Map<String, Sample> sampleMap = new HashMap<String, Sample>();
    for (Sample s : samplePool) {
      sampleMap.put(s.getCode(), s);
    }
    Map<Sample, List<Sample>> parentMap = openbis.getParentMap(samplePool);
    ArrayList<IBarcodeBean> res = new ArrayList<IBarcodeBean>();
    for (NewSampleModelBean s : samples) {
      List<String> parents = new ArrayList<String>();
      for (Sample p : parentMap.get(sampleMap.get(s.getCode())))
        parents.add(p.getCode());
      res.add(new NewModelBarcodeBean(s.getCode(), s.getSecondary_Name(), s.getType(), parents));
    }
    return res;
  }

  public void enableExperiments(boolean enable) {
    table.setEnabled(enable);
  }

  public void creationPressed() {
    enableExperiments(false);
    prepareButton.setEnabled(false);
  }

  public void creationDone() {
    enableExperiments(true);
    sheetDownloadButton.setEnabled(true);
    pdfDownloadButton.setEnabled(true);
    resetButton.setEnabled(true);
  }

  public void reset() {
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton.setEnabled(false);
    resetButton.setEnabled(false);
  }

  public void newExperimentSelected(boolean enable) {
    prepareButton.setEnabled(enable);
    sheetDownloadButton.setEnabled(!enable);
    pdfDownloadButton.setEnabled(!enable);
    resetButton.setEnabled(!enable);
  }

  private void setStatistics() {
    // this.setWidth("100%");
    projectview_content.removeAllComponents();

    MenuBar menubar = new MenuBar();
    menubar.addStyleName("user-menu");
    menubar.setWidth(100.0f, Unit.PERCENTAGE);

    projectview_content.addComponent(menubar);

    // A top-level menu item that opens a submenu

    // set to true for the hack below
    menubar.setHtmlContentAllowed(true);


    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_higher.png"));

    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));
    manage.setEnabled(false);

    // Another top-level item
    MenuItem workflows = menubar.addItem("Run workflows", null, null);
    workflows.setIcon(new ThemeResource("dna_higher.png"));
    workflows.setEnabled(false);

    // Yet another top-level item
    MenuItem analysis = menubar.addItem("Analyze your data", null, null);
    analysis.setIcon(new ThemeResource("graph_higher.png"));
    analysis.setEnabled(false);


    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();

    projectview_content.setWidth("100%");
    this.setWidth(String.format("%spx", (browserWidth * 0.6)));
    // this.setHeight(String.format("%spx", (browserHeight * 0.8)));


    // table section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();

    tableSectionContent.setCaption("Select Experiments for Barcode Creation");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    projectview_content.addComponent(tableSection);

  }

  // TODO if you tested this code delete this
  // private void addentry(Integer itemId, HierarchicalContainer htc,
  // Map<String, SimpleEntry<String, Long>> entries, String fileName) {
  // fileName =
  // Paths.get(fileName, (String) htc.getItem(itemId).getItemProperty("File Name").getValue())
  // .toString();
  //
  // // System.out.println(fileName);
  // if (htc.hasChildren(itemId)) {
  //
  // for (Object childId : htc.getChildren(itemId)) {
  // addentry((Integer) childId, htc, entries, fileName);
  // }
  //
  // } else {
  // String datasetCode = (String) htc.getItem(itemId).getItemProperty("CODE").getValue();
  // Long datasetFileSize =
  // (Long) htc.getItem(itemId).getItemProperty("file_size_bytes").getValue();
  //
  // entries
  // .put(fileName, new AbstractMap.SimpleEntry<String, Long>(datasetCode, datasetFileSize));
  // }
  // }

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

    // filterTable.setCaption("Registered Experiments");
    filterTable.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);

    return filterTable;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
     System.out.println("currentValue: " + currentValue);
    // System.out.println("navigateToLabel: " + navigateToLabel);
    try {
      System.out.println(openbis.loggedin());
      this.setContainerDataSource(getSummaryBeans(openbis, currentValue), currentValue);
    } catch (Exception e) {
      LOGGER.error("setting container datasource from bean failed", e.getStackTrace());
    }
  }

  private List<ExperimentBarcodeSummaryBean> getSummaryBeans(OpenBisClient openbis, String project) {
    List<String> barcodeExperiments =
        new ArrayList<String>(Arrays.asList("Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION", "Q_NGS_MEASUREMENT"));
    List<ExperimentBarcodeSummaryBean> beans = new ArrayList<ExperimentBarcodeSummaryBean>();
    System.out.println(openbis.getExperimentsOfProjectByIdentifier(project));
    
    for (Experiment e : openbis.getExperimentsOfProjectByIdentifier(project)) {
      String type = e.getExperimentTypeCode();
      if (barcodeExperiments.contains(type)) {
        String expID = e.getIdentifier();
        List<Sample> samples = openbis.getSamplesofExperiment(e.getIdentifier());
        System.out.println(openbis.getSamplesofExperiment(e.getIdentifier()));
        int numOfSamples = samples.size();
        List<String> ids = new ArrayList<String>();
        for (Sample s : samples) {
          ids.add(s.getCode());
        }
        String bioType = "unknown";
        if (type.equals(barcodeExperiments.get(0))) {
          System.out.println(samples.get(0).getCode());
          System.out.println(samples.get(0).getProperties().get("Q_PRIMARY_TISSUE"));
          bioType = samples.get(0).getProperties().get("Q_PRIMARY_TISSUE");
        }
        if (type.equals(barcodeExperiments.get(1))) {
          System.out.println(samples.get(0).getCode());
          System.out.println(samples.get(0).getProperties().get("Q_SAMPLE_TYPE"));
          bioType = samples.get(0).getProperties().get("Q_SAMPLE_TYPE");
        }
        if (type.equals(barcodeExperiments.get(2))) {
          System.out.println(samples.get(0).getCode());
          bioType = e.getProperties().get("Q_SEQUENCING_TYPE");
        }
        beans.add(new ExperimentBarcodeSummaryBean(BarcodeFunctions.getBarcodeRange(ids), bioType,
            Integer.toString(numOfSamples), expID));
      }
    }
    return beans;
  }

  public SortBy getSorter() {
    return (SortBy) comparators.getValue();
  }
}
