package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.BarcodesReadyRunnable;
import helpers.Functions;

import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletSession;

import main.BarcodeCreator;
import model.ExperimentBarcodeSummaryBean;
import model.IBarcodeBean;
import model.NewModelBarcodeBean;
import model.NewSampleModelBean;
import model.SortBy;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class BarcodeView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 8921847321758727061L;

  static String navigateToLabel = "barcodeView";

  FilterTable table;
  VerticalLayout projectview_content;

  private String scripts;
  private String paths;

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
  OpenBisClient openbis;

  public BarcodeView(FilterTable table, IndexedContainer datasource, String id, String scripts,
      String paths) {
    projectview_content = new VerticalLayout();

    this.scripts = scripts;
    this.paths = paths;
    this.creator = new BarcodeCreator(scripts, paths);
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    openbis = dh.openBisClient;

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

    this.tableClickChangeTreeView();
  }

  public BarcodeView(String scripts, String path) {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new IndexedContainer(), "No project selected", scripts, path);
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
  public void setContainerDataSource(ProjectInformation projectInformation,
      List<ExperimentBarcodeSummaryBean> summary, String id) {
    this.id = id;
    this.setStatistics(projectInformation);

    VerticalLayout buttonLayoutSection = new VerticalLayout();
    buttonLayoutSection.setMargin(true);
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    buttonLayoutSection.addComponent(buttonLayout);

    prepareButton = new Button("Prepare Barcodes");
    prepareButton.setEnabled(false);

    addComponent(prepareButton);

    info = new Label();
    bar = new ProgressBar();
    addComponent(info);
    addComponent(bar);

    sheetDownloadButton = new Button("Download Sample Sheet");
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton = new Button("Download Tube Barcodes");
    pdfDownloadButton.setEnabled(false);
    resetButton = new Button("Reset Selection");
    resetButton.setEnabled(false);
    comparators = new OptionGroup("Sort Sheet by");
    comparators.addItems(SortBy.values());
    comparators.setValue(SortBy.DESCRIPTION);
    addComponent(comparators);
    addComponent(sheetDownloadButton);
    addComponent(pdfDownloadButton);
    addComponent(resetButton);

    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.projectview_content.addComponent(buttonLayoutSection);

    BeanItemContainer<ExperimentBarcodeSummaryBean> beanContainer =
        new BeanItemContainer<ExperimentBarcodeSummaryBean>(ExperimentBarcodeSummaryBean.class);
    beanContainer.addAll(summary);
    table.setContainerDataSource(beanContainer);
    table.setSelectable(true);
    table.setMultiSelect(true);

    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    StreamResource sr = dh.getTSVStream(dh.containerToString(beanContainer), this.id);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);

    initControl();

    // this.updateCaption();
  }

  private void initControl() {
    final BarcodeView instance = this; //needed for concurrency
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
              new BarcodesReadyRunnable(instance));
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
      for (Sample s : openbis.getSamplesofExperiment(b.getExperiment())) {
        String type = s.getSampleTypeCode();
        String bioType = "unknown";
        if (type.equals("Q_BIOLOGICAL_SAMPLE")) {
          bioType = s.getProperties().get("Q_PRIMARY_TISSUE");
        }
        if (type.equals("Q_TEST_SAMPLE")) {
          bioType = s.getProperties().get("Q_SAMPLE_TYPE");
        }
        samples.add(new NewSampleModelBean(s.getCode(), s.getProperties().get("Q_SECONDARY_NAME"),
            bioType));
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

  private void setStatistics(ProjectInformation projectInformation) {
    // this.setWidth("100%");
    projectview_content.removeAllComponents();

    MenuBar menubar = new MenuBar();
    menubar.addStyleName("qbicmainportlet");
    menubar.setWidth(100.0f, Unit.PERCENTAGE);

    projectview_content.addComponent(menubar);

    // A top-level menu item that opens a submenu

    // set to true for the hack below
    menubar.setHtmlContentAllowed(true);


    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_test2.png"));

    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    DataHandler datahandler =
        (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    HierarchicalContainer datasetContainer = null;
    try {
      datasetContainer = datahandler.getDatasets(this.id, "project");
    } catch (Exception e) {
      e.printStackTrace();
      datasetContainer = null;
    }
    if (datasetContainer == null || datasetContainer.getItemIds().isEmpty()) {
      downloadProject.setEnabled(false);
    } else {
      Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
          new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
      for (Object itemId : datasetContainer.getItemIds()) {
        // if((datasetContainer.getChildren(itemId) != null) &&
        // !datasetContainer.getChildren(itemId).isEmpty()) {
        if (datasetContainer.getParent(itemId) == null) {
          addentry((Integer) itemId, datasetContainer, entries,
              (String) datasetContainer.getItem(itemId).getItemProperty("CODE").getValue());
        }
      }
      portletSession.setAttribute("qbic_download", entries, PortletSession.APPLICATION_SCOPE);
      downloadProject
          .addItem(
              "<a href=\""
                  + (String) portletSession
                      .getAttribute("resURL", PortletSession.APPLICATION_SCOPE)
                  + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project.</a>",
              null);
    }


    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_test2.png"));

    // Another top-level item
    MenuItem snacks = menubar.addItem("Run workflows", null, null);
    snacks.setIcon(new ThemeResource("dna_test2.png"));

    // Yet another top-level item
    MenuItem servs = menubar.addItem("Analyze your data", null, null);
    servs.setIcon(new ThemeResource("graph_test2.png"));


    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();

    projectview_content.setWidth("100%");
    this.setWidth(String.format("%spx", (browserWidth * 0.6)));
    this.setHeight(String.format("%spx", (browserHeight * 0.8)));


    // Project description
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

    Label descContent = new Label("none");
    if (!("".equals(projectInformation.description))) {
      descContent = new Label(projectInformation.description);
    }

    Label contact =
        new Label(
            "<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
                + this.id
                + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
                + this.id + "</a>", ContentMode.HTML);

    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);
    projDescriptionContent.setMargin(true);
    projDescriptionContent.setCaption("Description");
    projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);
    projDescription.setMargin(true);
    projDescription.setWidth("100%");
    projectview_content.addComponent(projDescription);

    // members section

    VerticalLayout members_section = new VerticalLayout();
    Component membersContent = getMembersComponent(projectInformation.members);

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    members_section.addComponent(membersContent);
    members_section.setMargin(true);

    projectview_content.addComponent(members_section);



    // statistics section
    VerticalLayout statistics = new VerticalLayout();

    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(String.format("%s experiment(s),",
        projectInformation.numberOfExperiments)));

    statContent.addComponent(new Label(String.format("%s sample(s),",
        projectInformation.numberOfSamples)));

    statContent.addComponent(new Label(String.format("%s dataset(s).",
        projectInformation.numberOfDatasets)));

    statContent.setMargin(true);
    statContent.setSpacing(true);

    if (projectInformation.numberOfDatasets > 0) {

      String lastSample = "No samples available";
      if (projectInformation.lastChangedSample != null) {
        lastSample = projectInformation.lastChangedSample.split("/")[2];
      }
      statContent.addComponent(new Label(String.format("Last change %s", String.format(
          "occurred in sample %s (%s)", lastSample,
          projectInformation.lastChangedDataset.toString()))));
    }


    statistics.addComponent(statContent);
    statistics.setMargin(true);
    projectview_content.addComponent(statistics);


    // status bar section

    VerticalLayout status = new VerticalLayout();
    HorizontalLayout statusContent = new HorizontalLayout();
    statusContent.setCaption("Status");
    statusContent.setIcon(FontAwesome.CLOCK_O);

    statusContent.addComponent(projectInformation.progressBar);
    statusContent.addComponent(new Label(projectInformation.statusMessage));
    statusContent.setSpacing(true);
    statusContent.setMargin(true);

    status.addComponent(statusContent);
    status.setMargin(true);

    projectview_content.addComponent(status);



    // table section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();

    tableSectionContent.setCaption("Registered Experiments");
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

  private void addentry(Integer itemId, HierarchicalContainer htc,
      Map<String, SimpleEntry<String, Long>> entries, String fileName) {
    fileName =
        Paths.get(fileName, (String) htc.getItem(itemId).getItemProperty("File Name").getValue())
            .toString();

    System.out.println(fileName);
    if (htc.hasChildren(itemId)) {

      for (Object childId : htc.getChildren(itemId)) {
        addentry((Integer) childId, htc, entries, fileName);
      }

    } else {
      String datasetCode = (String) htc.getItem(itemId).getItemProperty("CODE").getValue();
      Long datasetFileSize =
          (Long) htc.getItem(itemId).getItemProperty("file_size_bytes").getValue();

      entries
          .put(fileName, new AbstractMap.SimpleEntry<String, Long>(datasetCode, datasetFileSize));
    }
  }


  private void updateCaption() {
    this.setCaption(String.format("Viewing Project %s", id));
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Experiment"));
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

    // filterTable.setCaption("Registered Experiments");
    filterTable.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);

    return filterTable;
  }

  private String getMembersString(Set<String> members) {
    String concat = new String("");
    if (members != null) {
      Object[] tmp = members.toArray();
      concat = (String) tmp[0];

      if (tmp.length > 1) {
        for (int i = 1; i < tmp.length; ++i) {
          concat = concat + ", " + tmp[i];
        }
      }
    }

    return concat;
  }


  private Component getMembersComponent(Set<String> members) {
    HorizontalLayout membersLayout = new HorizontalLayout();
    if (members != null) {
      // membersLayout.addComponent(new Label("Members:"));
      for (String member : members) {

        // Cool idea, but let's do this when we have more portrait pictures in Liferay

        try {
          // companyId. We have presumable just one portal id, which equals the companyId.
          User user = UserLocalServiceUtil.getUserByScreenName(1, member);
          String fullname = user.getFullName();
          String email = user.getEmailAddress();


          // VaadinSession.getCurrent().getService();
          // ThemeDisplay themedisplay =
          // (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
          // String url = user.getPortraitURL(themedisplay);
          // ExternalResource er = new ExternalResource(url);
          // com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
          // image.setHeight(80, Unit.PIXELS);
          // image.setWidth(65, Unit.PIXELS);
          // membersLayout.addComponent(image);
          String labelString =
              new String("<a href=\"mailto:" + email
                  + "\" style=\"color: #0068AA; text-decoration: none\">" + fullname + "</a>");
          Label userLabel = new Label(labelString, ContentMode.HTML);
          membersLayout.addComponent(userLabel);

        } catch (com.liferay.portal.NoSuchUserException e) {
          membersLayout.addComponent(new Label(member));
        } catch (PortalException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (SystemException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }


        // membersLayout.addComponent(new Label(member));
      }
      membersLayout.setSpacing(true);
      membersLayout.setMargin(true);
    }
    return membersLayout;
  }


  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    System.out.println("currentValue: " + currentValue);
    System.out.println("navigateToLabel: " + navigateToLabel);
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    try {
      Project project = dh.openBisClient.getProjectByCode(currentValue);
      String projectIdentifier = project.getIdentifier();

      this.setContainerDataSource(dh.getProjectInformation(projectIdentifier),
          getSummaryBeans(dh.openBisClient, currentValue), currentValue);
    } catch (Exception e) {
      System.out.println("Exception in ProjectView.enter");
      e.printStackTrace();
    }
  }

  private List<ExperimentBarcodeSummaryBean> getSummaryBeans(OpenBisClient openbis, String project) {
    List<String> barcodeExperiments =
        new ArrayList<String>(Arrays.asList("Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION"));
    List<ExperimentBarcodeSummaryBean> beans = new ArrayList<ExperimentBarcodeSummaryBean>();
    for (Experiment e : openbis.getExperimentsOfProjectByCode(project)) {
      String type = e.getExperimentTypeCode();
      if (barcodeExperiments.contains(type)) {
        String expCode = e.getCode();
        List<Sample> samples = openbis.getSamplesofExperiment(e.getCode());
        int numOfSamples = samples.size();
        List<String> ids = new ArrayList<String>();
        for (Sample s : samples) {
          ids.add(s.getCode());
        }
        String bioType = "unknown";
        if (type.equals(barcodeExperiments.get(0))) {
          bioType = samples.get(0).getProperties().get("Q_PRIMARY_TISSUE");
        }
        if (type.equals(barcodeExperiments.get(1))) {
          bioType = samples.get(0).getProperties().get("Q_SAMPLE_TYPE");
        }
        beans.add(new ExperimentBarcodeSummaryBean(Functions.getBarcodeRange(ids), bioType, Integer
            .toString(numOfSamples), expCode));
      }
    }
    return beans;
  }
}
