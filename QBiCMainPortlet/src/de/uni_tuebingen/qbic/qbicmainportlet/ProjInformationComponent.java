package de.uni_tuebingen.qbic.qbicmainportlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import javax.portlet.PortletSession;

import logging.Log4j2Logger;
import logging.Logger;
import model.BiologicalEntitySampleBean;
import model.BiologicalSampleBean;
import model.DatasetBean;
import model.ProjectBean;
import model.SampleBean;
import model.TestSampleBean;

import org.apache.catalina.util.Base64;
import org.tepi.filtertable.FilterTreeTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.Notification.Type;

import de.uni_tuebingen.qbic.util.DashboardUtil;

public class ProjInformationComponent extends CustomComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 8672873911284888801L;

  private VerticalLayout mainLayout;
  private static Logger LOGGER = new Log4j2Logger(DatasetView.class);
  private FilterTreeTable datasetTable;
  private HierarchicalContainer datasets;
  private BeanItemContainer<TestSampleBean> samples;
  VerticalLayout vert;
  private final String DOWNLOAD_BUTTON_CAPTION = "Download";
  private final String VISUALIZE_BUTTON_CAPTION = "Visualize";
  public final static String navigateToLabel = "datasetview";
  Label descriptionLabel = new Label("");
  private DataHandler datahandler;
  private State state;
  private String resourceUrl;
  private final ButtonLink download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(
      ""));

  private final String[] FILTER_TABLE_COLUMNS = new String[] {"Select", "Description", "File Name",
      "Registration Date"};

  private int numberOfDatasets;

  private Label descContent;

  private Label contact;

  private Label patientInformation;

  private ProjectBean projectBean;

  public ProjInformationComponent(DataHandler dh, State state, String resourceurl) {
    this.datahandler = dh;
    this.resourceUrl = resourceurl;
    this.state = state;

    this.setCaption("");

    this.initUI();
  }

  private void initUI() {
    vert = new VerticalLayout();
    datasetTable = buildFilterTable();
    descContent = new Label("");
    contact = new Label("", ContentMode.HTML);
    patientInformation = new Label("No patient information provided.", ContentMode.HTML);
    mainLayout = new VerticalLayout(vert);

    this.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
    this.setCompositionRoot(mainLayout);
  }

  public void updateUI(ProjectBean currentBean, String projectType) {

    if (currentBean.getId() == null)
      return;
    try {

      projectBean = currentBean;

      contact
          .setValue("<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
              + currentBean.getId()
              + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
              + currentBean.getId() + "</a>");
      String desc = currentBean.getDescription();
      if (!desc.isEmpty()) {
        descContent.setValue(desc);
      }

      HierarchicalContainer datasetContainer = new HierarchicalContainer();
      datasetContainer.addContainerProperty("Select", CheckBox.class, null);
      datasetContainer.addContainerProperty("Project", String.class, null);
      datasetContainer.addContainerProperty("Description", String.class, null);
      datasetContainer.addContainerProperty("Sample", String.class, null);
      // datasetContainer.addContainerProperty("Sample Type", String.class, null);
      datasetContainer.addContainerProperty("File Name", String.class, null);
      datasetContainer.addContainerProperty("File Type", String.class, null);
      datasetContainer.addContainerProperty("Dataset Type", String.class, null);
      datasetContainer.addContainerProperty("Registration Date", Timestamp.class, null);
      datasetContainer.addContainerProperty("Validated", Boolean.class, null);
      datasetContainer.addContainerProperty("File Size", String.class, null);
      datasetContainer.addContainerProperty("file_size_bytes", Long.class, null);
      datasetContainer.addContainerProperty("dl_link", String.class, null);
      datasetContainer.addContainerProperty("CODE", String.class, null);

      // HierarchicalContainer sampleContainer = new HierarchicalContainer()

      List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> retrievedDatasetsAll = null;
      List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> retrievedDatasets =
          new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>();
      // List<Sample> retrievedSamples = new ArrayList<Sample>();
      Map<String, ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>> datasetFilter =
          new HashMap<String, ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>>();


      String projectIdentifier = currentBean.getId();
      retrievedDatasetsAll =
          datahandler.getOpenBisClient().getDataSetsOfProjectByIdentifierWithSearchCriteria(
              projectIdentifier);

      for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet ds : retrievedDatasetsAll) {

        ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> values =
            datasetFilter.get(ds.getSampleIdentifierOrNull());

        if (values == null) {
          values = new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>();
          datasetFilter.put(ds.getSampleIdentifierOrNull(), values);
        }
        values.add(ds);
      }

      List<Sample> allSamples =
          datahandler.getOpenBisClient().getSamplesOfProject(projectIdentifier);

      for (Sample sample : allSamples) {
        if (sample.getSampleTypeCode().equals("Q_ATTACHMENT_SAMPLE")) {

          ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> foundDataset =
              datasetFilter.get(sample.getIdentifier());

          if (foundDataset != null) {
            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet ds : foundDataset) {
              if (ds.getProperties().get("Q_ATTACHMENT_TYPE").equals("INFORMATION")) {
                retrievedDatasets.add(ds);
              }
            }
          }
        }
      }

      this.datasetTable.setCaption("Project Data");
      // descriptionLabel = new Label(String.format("This project contains %s result datasets.",
      // numberOfDatasets), Label.CONTENT_PREFORMATTED);

      numberOfDatasets = retrievedDatasets.size();

      Boolean dataAvailable = true;

      if (numberOfDatasets == 0) {
        dataAvailable = false;
        // new Notification("No datasets available.",
        // "<br/>Please contact the project manager.", Type.WARNING_MESSAGE, true).show(Page
        // .getCurrent());
      } else {

        Map<String, String> samples = new HashMap<String, String>();

        // project same for all datasets
        String projectCode = retrievedDatasets.get(0).getExperimentIdentifier().split("/")[2];
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataset : retrievedDatasets) {
          samples.put(dataset.getCode(), dataset.getSampleIdentifierOrNull().split("/")[2]);
        }

        List<DatasetBean> dsBeans = datahandler.queryDatasetsForFolderStructure(retrievedDatasets);

        for (DatasetBean d : dsBeans) {
          Date date = d.getRegistrationDate();
          SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
          String dateString = sd.format(date);
          Timestamp ts = Timestamp.valueOf(dateString);
          String sampleID = samples.get(d.getCode());

          registerDatasetInTable(d, datasetContainer, projectCode, sampleID, ts, null);
        }
      }

      this.setContainerDataSource(datasetContainer, dataAvailable, projectType);

    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error(String.format("getting dataset failed for dataset %s %s", currentBean.getId()),
          e.getStackTrace());
    }
  }

  public void setContainerDataSource(HierarchicalContainer newDataSource, Boolean dataAvailable,
      String projectType) {
    datasets = (HierarchicalContainer) newDataSource;
    datasetTable.setContainerDataSource(this.datasets);

    datasetTable.setVisibleColumns((Object[]) FILTER_TABLE_COLUMNS);

    datasetTable.setSizeFull();
    this.buildLayout(dataAvailable, projectType);
  }

  public HierarchicalContainer getContainerDataSource() {
    return this.datasets;
  }

  /**
   * Precondition: {DatasetView#table} has to be initialized. e.g. with
   * {DatasetView#buildFilterTable} If it is not, strange behaviour has to be expected. builds the
   * Layout of this view.
   */
  private void buildLayout(Boolean dataAvailable, String projectType) {
    this.vert.removeAllComponents();
    this.vert.setWidth("100%");

    // Table (containing datasets) section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();

    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

    tableSectionContent.setMargin(new MarginInfo(false, false, false, false));
    projDescriptionContent.setMargin(new MarginInfo(false, false, false, false));

    projDescription.setCaption("");

    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);

    if (projectType.equals("patient")) {
      String patientInfo = "";
      Boolean available = false;

      SearchCriteria sampleSc = new SearchCriteria();
      sampleSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
          "Q_BIOLOGICAL_ENTITY"));
      SearchCriteria projectSc = new SearchCriteria();
      projectSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
          projectBean.getCode()));
      sampleSc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(projectSc));

      SearchCriteria experimentSc = new SearchCriteria();
      experimentSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
          model.ExperimentType.Q_EXPERIMENTAL_DESIGN.name()));
      sampleSc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentSc));
      List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> samples =
          datahandler.getOpenBisClient().getFacade().searchForSamples(sampleSc);
      for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample : samples) {
        if (sample.getProperties().get("Q_ADDITIONAL_INFO") != null) {
          available = true;
          String[] splitted = sample.getProperties().get("Q_ADDITIONAL_INFO").split(";");
          for (String s : splitted) {
            String[] splitted2 = s.split(":");
            patientInfo += String.format("<p><u>%s</u>: %s </p> ", splitted2[0], splitted2[1]);
          }
        }
      }
      if (available) {
        patientInformation.setValue(patientInfo);
      } else {
        patientInformation.setValue("No patient information provided.");
      }
      projDescriptionContent.addComponent(patientInformation);
    }

    projDescription.addComponent(projDescriptionContent);

    projDescriptionContent.setSpacing(true);
    projDescription.setMargin(new MarginInfo(false, false, true, true));
    projDescription.setWidth("100%");
    projDescription.setSpacing(true);

    descriptionLabel.setWidth("100%");
    // tableSection.addComponent(descriptionLabel);
    tableSectionContent.addComponent(this.datasetTable);

    tableSection.setMargin(new MarginInfo(true, false, false, true));
    tableSection.setSpacing(true);

    tableSection.addComponent(tableSectionContent);

    this.vert.addComponent(projDescription);

    datasetTable.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    // this.table.setSizeFull();

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setMargin(new MarginInfo(false, false, true, false));
    buttonLayout.setHeight(null);
    // buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    this.download.setEnabled(false);
    final Button visualize = new Button(VISUALIZE_BUTTON_CAPTION);
    visualize.setEnabled(false);
    buttonLayout.addComponent(this.download);
    buttonLayout.addComponent(visualize);
    buttonLayout.setSpacing(true);

    Button checkAll = new Button("Select all datasets");
    checkAll.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        for (Object itemId : datasetTable.getItemIds()) {
          ((CheckBox) datasetTable.getItem(itemId).getItemProperty("Select").getValue())
              .setValue(true);
        }
      }
    });

    Button uncheckAll = new Button("Unselect all datasets");
    uncheckAll.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        for (Object itemId : datasetTable.getItemIds()) {
          ((CheckBox) datasetTable.getItem(itemId).getItemProperty("Select").getValue())
              .setValue(false);
        }
      }
    });

    buttonLayout.addComponent(checkAll);
    buttonLayout.addComponent(uncheckAll);
    /**
     * prepare download.
     */
    download.setResource(new ExternalResource("javascript:"));
    download.setEnabled(false);


    for (final Object itemId : this.datasetTable.getItemIds()) {
      setCheckedBox(itemId, (String) this.datasetTable.getItem(itemId).getItemProperty("CODE")
          .getValue());
    }



    /*
     * Update the visualize button. It is only enabled, if the files can be visualized.
     */
    this.datasetTable.addValueChangeListener(new ValueChangeListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -4875903343717437913L;


      /**
       * check for what selection can be visualized. If so, enable the button. TODO change to
       * checked.
       */
      @Override
      public void valueChange(ValueChangeEvent event) {
        // Nothing selected or more than one selected.
        Set<Object> selectedValues = (Set<Object>) event.getProperty().getValue();
        if (selectedValues == null || selectedValues.size() == 0 || selectedValues.size() > 1) {
          visualize.setEnabled(false);
          return;
        }
        // if one selected check whether its dataset type is either fastqc or qcml.
        // For now we only visulize these two file types.
        Iterator<Object> iterator = selectedValues.iterator();
        Object next = iterator.next();
        String datasetType =
            (String) datasetTable.getItem(next).getItemProperty("Dataset Type").getValue();
        String fileName =
            (String) datasetTable.getItem(next).getItemProperty("File Name").getValue();
        // TODO: No hardcoding!!
        // if (datasetType.equals("FASTQC") || datasetType.equals("QCML") ||
        // datasetType.equals("BAM")
        // || datasetType.equals("VCF")) {
        if (datasetType.equals("Q_WF_MS_QUALITYCONTROL_RESULTS")
            && (fileName.endsWith(".html") || fileName.endsWith(".qcML"))) {
          visualize.setEnabled(true);
        } else if (datasetType.equals("Q_WF_MS_QUALITYCONTROL_LOGS")
            && (fileName.endsWith(".err") || fileName.endsWith(".out"))) {
          visualize.setEnabled(true);
        } else if (datasetType.equals("Q_WF_MA_QUALITYCONTROL_RESULTS")
            && (fileName.endsWith(".html"))) {
          visualize.setEnabled(true);
        } else {
          visualize.setEnabled(false);
        }
      }
    });

    /*
     * Send message that in datasetview the following was selected. WorkflowViews get those messages
     * and save them, if it is valid information for them.
     */
    this.datasetTable.addValueChangeListener(new ValueChangeListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -3554627008191389648L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // Nothing selected or more than one selected.
        Set<Object> selectedValues = (Set<Object>) event.getProperty().getValue();
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("DataSetView");
        if (selectedValues != null && selectedValues.size() == 1) {
          Iterator<Object> iterator = selectedValues.iterator();
          Object next = iterator.next();
          String datasetType =
              (String) datasetTable.getItem(next).getItemProperty("Dataset Type").getValue();
          message.add(datasetType);
          String project =
              (String) datasetTable.getItem(next).getItemProperty("Project").getValue();

          String space = datahandler.getOpenBisClient().getProjectByCode(project).getSpaceCode();// .getIdentifier().split("/")[1];
          message.add(project);
          message.add((String) datasetTable.getItem(next).getItemProperty("Sample").getValue());
          // message.add((String) table.getItem(next).getItemProperty("Sample Type").getValue());
          message.add((String) datasetTable.getItem(next).getItemProperty("dl_link").getValue());
          message.add((String) datasetTable.getItem(next).getItemProperty("File Name").getValue());
          message.add(space);
          // state.notifyObservers(message);
        } else {
          message.add("null");
        }// TODO
         // state.notifyObservers(message);

      }
    });


    // TODO get the GV to work here. Together with reverse proxy
    // Assumes that table Value Change listner is enabling or disabling the button if preconditions
    // are not fullfilled
    visualize.addClickListener(new ClickListener() {
      /**
       * 
       */
      private static final long serialVersionUID = 9015273307461506369L;

      @Override
      public void buttonClick(ClickEvent event) {
        Set<Object> selectedValues = (Set<Object>) datasetTable.getValue();
        Iterator<Object> iterator = selectedValues.iterator();
        Object next = iterator.next();
        String datasetCode = (String) datasetTable.getItem(next).getItemProperty("CODE").getValue();
        String datasetFileName =
            (String) datasetTable.getItem(next).getItemProperty("File Name").getValue();
        URL url;
        try {
          Object parent = datasetTable.getParent(next);
          if (parent != null) {
            String parentDatasetFileName =
                (String) datasetTable.getItem(parent).getItemProperty("File Name").getValue();
            url =
                datahandler.getOpenBisClient().getUrlForDataset(datasetCode,
                    parentDatasetFileName + "/" + datasetFileName);
          } else {
            url = datahandler.getOpenBisClient().getUrlForDataset(datasetCode, datasetFileName);
          }

          Window subWindow =
              new Window("QC of Sample: "
                  + (String) datasetTable.getItem(next).getItemProperty("Sample").getValue());
          VerticalLayout subContent = new VerticalLayout();
          subContent.setMargin(true);
          subWindow.setContent(subContent);
          QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
          // Put some components in it
          Resource res = null;
          String datasetType =
              (String) datasetTable.getItem(next).getItemProperty("Dataset Type").getValue();
          final RequestHandler rh = new ProxyForGenomeViewerRestApi();
          boolean rhAttached = false;
          if (datasetType.equals("Q_WF_MS_QUALITYCONTROL_RESULTS")
              && datasetFileName.endsWith(".qcML")) {
            QcMlOpenbisSource re = new QcMlOpenbisSource(url);
            StreamResource streamres = new StreamResource(re, datasetFileName);
            streamres.setMIMEType("application/xml");
            res = streamres;
          } else if (datasetType.equals("Q_WF_MS_QUALITYCONTROL_RESULTS")
              && datasetFileName.endsWith(".html")) {
            QcMlOpenbisSource re = new QcMlOpenbisSource(url);
            StreamResource streamres = new StreamResource(re, datasetFileName);
            streamres.setMIMEType("text/html");
            res = streamres;
          } else if (datasetType.equals("Q_WF_MS_QUALITYCONTROL_LOGS")
              && (datasetFileName.endsWith(".err") || datasetFileName.endsWith(".out"))) {
            QcMlOpenbisSource re = new QcMlOpenbisSource(url);
            StreamResource streamres = new StreamResource(re, datasetFileName);
            streamres.setMIMEType("text/plain");
            res = streamres;
          } else if (datasetType.equals("Q_WF_MA_QUALITYCONTROL_RESULTS")
              && datasetFileName.endsWith(".html")) {
            QcMlOpenbisSource re = new QcMlOpenbisSource(url);
            StreamResource streamres = new StreamResource(re, datasetFileName);
            streamres.setMIMEType("text/html");
            res = streamres;
          } else if (datasetType.equals("FASTQC")) {
            res = new ExternalResource(url);
          } else if (datasetType.equals("BAM") || datasetType.equals("VCF")) {
            String filePath =
                (String) datasetTable.getItem(next).getItemProperty("dl_link").getValue();
            filePath = String.format("/store%s", filePath.split("store")[1]);
            String fileId =
                (String) datasetTable.getItem(next).getItemProperty("File Name").getValue();
            // fileId = "control.1kg.panel.samples.vcf.gz";
            // UI.getCurrent().getSession().addRequestHandler(rh);
            rhAttached = true;
            ThemeDisplay themedisplay =
                (ThemeDisplay) VaadinService.getCurrentRequest()
                    .getAttribute(WebKeys.THEME_DISPLAY);
            String hostTmp = "http://localhost:7778/vizrest/rest";// "http://localhost:8080/web/guest/mainportlet?p_p_id=QbicmainportletApplicationPortlet_WAR_QBiCMainPortlet_INSTANCE_5pPd5JQ8uGOt&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_count=1";
            // hostTmp +=
            // "&qbicsession=" + UI.getCurrent().getSession().getAttribute("gv-restapi-session")
            // + "&someblabla=";
            // String hostTmp = themedisplay.getURLPortal() +
            // UI.getCurrent().getPage().getLocation().getPath() + "?qbicsession=" +
            // UI.getCurrent().getSession().getAttribute("gv-restapi-session") + "&someblabla=" ;
            LOGGER.debug(hostTmp);
            String host = Base64.encode(hostTmp.getBytes());
            LOGGER.debug(host);
            String title = (String) datasetTable.getItem(next).getItemProperty("Sample").getValue();
            res =
                new ExternalResource(
                    String
                        .format(
                            "http://localhost:7778/genomeviewer/?host=%s&title=%s&fileid=%s&featuretype=alignments&filepath=%s&removeZeroGenotypes=false",
                            host, title, fileId, filePath));
          }
          LOGGER.debug("Is resource null?: " + String.valueOf(res == null));
          BrowserFrame frame = new BrowserFrame("", res);
          if (rhAttached) {
            frame.addDetachListener(new DetachListener() {

              /**
               * 
               */
              private static final long serialVersionUID = 1534523447730906543L;

              @Override
              public void detach(DetachEvent event) {
                UI.getCurrent().getSession().removeRequestHandler(rh);
              }

            });
          }

          frame.setSizeFull();
          subContent.addComponent(frame);

          // Center it in the browser window
          subWindow.center();
          subWindow.setModal(true);
          subWindow.setSizeFull();

          frame.setHeight((int) (ui.getPage().getBrowserWindowHeight() * 0.8), Unit.PIXELS);
          // Open it in the UI
          ui.addWindow(subWindow);
        } catch (MalformedURLException e) {
          LOGGER.error(String.format(
              "Visualization failed because of malformedURL for dataset: %s", datasetCode));
          Notification
              .show(
                  "Given dataset has no file attached to it!! Please Contact your project manager. Or check whether it already has some data",
                  Notification.Type.ERROR_MESSAGE);
        }
      }
    });

    // this.vert.addComponent(buttonLayout);
    if (dataAvailable) {
      this.vert.addComponent(tableSection);
      tableSection.addComponent(buttonLayout);
      projDescription.setMargin(new MarginInfo(false, false, false, true));
    }
  }


  private void setCheckedBox(Object itemId, String parentFolder) {
    CheckBox itemCheckBox =
        (CheckBox) this.datasetTable.getItem(itemId).getItemProperty("Select").getValue();
    itemCheckBox.addValueChangeListener(new TableCheckBoxValueChangeListener(itemId, parentFolder));

    if (datasetTable.hasChildren(itemId)) {
      for (Object childId : datasetTable.getChildren(itemId)) {
        String newParentFolder =
            Paths.get(parentFolder,
                (String) this.datasetTable.getItem(itemId).getItemProperty("File Name").getValue())
                .toString();
        setCheckedBox(childId, newParentFolder);
      }
    }

  }

  private FilterTreeTable buildFilterTable() {
    FilterTreeTable filterTable = new FilterTreeTable();
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

    if (this.datasets != null) {
      filterTable.setContainerDataSource(this.datasets);
    }

    return filterTable;
  }

  @SuppressWarnings("unchecked")
  public void registerDatasetInTable(DatasetBean d, HierarchicalContainer dataset_container,
      String project, String sample, Timestamp ts, Object parent) {
    if (d.hasChildren()) {

      Object new_ds = dataset_container.addItem();

      List<DatasetBean> subList = d.getChildren();


      dataset_container.setChildrenAllowed(new_ds, true);

      dataset_container.getContainerProperty(new_ds, "Select").setValue(new CheckBox());

      dataset_container.getContainerProperty(new_ds, "Project").setValue(project);
      dataset_container.getContainerProperty(new_ds, "Sample").setValue(sample);
      String secName = d.getProperties().get("Q_SECONDARY_NAME");
      // TODO add User here
      if (secName != null) {
        dataset_container.getContainerProperty(new_ds, "Description").setValue(
            d.getProperties().get("Q_SECONDARY_NAME"));
      }
      // dataset_container.getContainerProperty(new_ds, "Sample Type").setValue(
      // d.getSample().getType());
      dataset_container.getContainerProperty(new_ds, "File Name").setValue(d.getName());
      dataset_container.getContainerProperty(new_ds, "File Type").setValue("Folder");
      dataset_container.getContainerProperty(new_ds, "Dataset Type").setValue(d.getType());
      dataset_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_ds, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_ds, "dl_link").setValue(d.getDssPath());
      dataset_container.getContainerProperty(new_ds, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_ds, "file_size_bytes").setValue(d.getFileSize());

      if (parent != null) {
        dataset_container.setParent(new_ds, parent);
      }

      for (DatasetBean file : subList) {
        registerDatasetInTable(file, dataset_container, project, sample, ts, new_ds);
      }

    } else {
      // System.out.println("Now it should be a file: " + filelist[0].getPathInDataSet());

      Object new_file = dataset_container.addItem();
      dataset_container.setChildrenAllowed(new_file, false);

      dataset_container.getContainerProperty(new_file, "Select").setValue(new CheckBox());
      dataset_container.getContainerProperty(new_file, "Project").setValue(project);
      dataset_container.getContainerProperty(new_file, "Sample").setValue(sample);
      String secName = d.getProperties().get("Q_SECONDARY_NAME");
      // TODO add User here too
      if (secName != null) {
        dataset_container.getContainerProperty(new_file, "Description").setValue(
            d.getProperties().get("Q_SECONDARY_NAME"));
      }
      // dataset_container.getContainerProperty(new_file, "Sample Type").setValue(sampleType);
      dataset_container.getContainerProperty(new_file, "File Name").setValue(d.getFileName());
      dataset_container.getContainerProperty(new_file, "File Type").setValue(d.getFileType());
      dataset_container.getContainerProperty(new_file, "Dataset Type").setValue(d.getType());
      dataset_container.getContainerProperty(new_file, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_file, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_file, "File Size").setValue(
          DashboardUtil.humanReadableByteCount(d.getFileSize(), true));
      dataset_container.getContainerProperty(new_file, "dl_link").setValue(d.getDssPath());
      dataset_container.getContainerProperty(new_file, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_file, "file_size_bytes").setValue(d.getFileSize());
      if (parent != null) {
        dataset_container.setParent(new_file, parent);
      }
    }
  }



  private class TableCheckBoxValueChangeListener implements ValueChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = -7177199525909283879L;
    private Object itemId;
    private String itemFolderName;

    public TableCheckBoxValueChangeListener(final Object itemId, String itemFolderName) {
      this.itemFolderName = itemFolderName;
      this.itemId = itemId;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {

      PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
      Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
          (Map<String, AbstractMap.SimpleEntry<String, Long>>) portletSession.getAttribute(
              "qbic_download", PortletSession.APPLICATION_SCOPE);

      boolean itemSelected = (Boolean) event.getProperty().getValue();
      /*
       * String fileName = ""; Object parentId = table.getParent(itemId); //In order to prevent
       * infinity loop int folderDepth = 0; while(parentId != null && folderDepth < 100){ fileName =
       * Paths.get((String) table.getItem(parentId).getItemProperty("File Name").getValue(),
       * fileName).toString(); parentId = table.getParent(parentId); folderDepth++; }
       */

      valueChange(itemId, itemSelected, entries, itemFolderName);
      portletSession.setAttribute("qbic_download", entries, PortletSession.APPLICATION_SCOPE);

      if (entries == null || entries.isEmpty()) {
        download.setResource(new ExternalResource("javascript:"));
        download.setEnabled(false);
      } else {
        String resourceUrl =
            (String) portletSession.getAttribute("resURL", PortletSession.APPLICATION_SCOPE);
        download.setResource(new ExternalResource(resourceUrl));
        download.setEnabled(true);
      }

    }

    /**
     * updates entries (puts and removes) for selected table item and all its children. Means
     * Checkbox is updated. And in case download button is clicked all checked items will be
     * downloaded.
     * 
     * @param itemId Container id
     * @param itemSelected checkbox value of the item
     * @param entries all checked items
     * @param fileName fileName of current item
     */
    private void valueChange(Object itemId, boolean itemSelected,
        Map<String, SimpleEntry<String, Long>> entries, String fileName) {

      ((CheckBox) datasetTable.getItem(itemId).getItemProperty("Select").getValue())
          .setValue(itemSelected);
      fileName =
          Paths.get(fileName,
              (String) datasetTable.getItem(itemId).getItemProperty("File Name").getValue())
              .toString();

      // System.out.println(fileName);
      if (datasetTable.hasChildren(itemId)) {
        for (Object childId : datasetTable.getChildren(itemId)) {
          valueChange(childId, itemSelected, entries, fileName);
        }
      } else if (itemSelected) {
        String datasetCode =
            (String) datasetTable.getItem(itemId).getItemProperty("CODE").getValue();
        Long datasetFileSize =
            (Long) datasetTable.getItem(itemId).getItemProperty("file_size_bytes").getValue();
        entries.put(fileName, new AbstractMap.SimpleEntry<String, Long>(datasetCode,
            datasetFileSize));
      } else {
        entries.remove(fileName);
      }
    }
  }

  //TODO seems this isn't used. can we delete it?
  /**
   * The input should have the following form: type=openbis_type&id=openbis_id e.g.
   * type=sample&id=/ABI_SYSBIO/QMARI117AV It is specifically designed to be used in the case of
   * datasetView. In other cases there is no guarantee that it will work correctly. returns a map
   * with two entries: "type": "openbistype" "id" : "openbisId"
   * 
   * @param parameters
   * @return
   */
  public static Map<String, String> getMap(String parameters) {
    if (parameters == null || parameters.equals(""))
      return null;
    String[] params = parameters.split("&");
    // TODO check for length == 2 needed ?
    // if (params == null || params.length != 2)
    if (params == null || params.length > 3)
      return null;
    HashMap<String, String> map = new HashMap<String, String>();
    for (String p : params) {
      String[] kv = p.split("=");
      if (kv.length != 2)
        return null;
      map.put(kv[0], kv[1]);
    }
    return map;
  }

}
