package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentStatusBean;
import model.ProjectBean;

import org.tepi.filtertable.FilterTable;

import views.WorkflowView;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.themes.ValoTheme;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.main.ConfigurationManager;

public class PatientView extends VerticalLayout implements View {

  /**
	 * 
	 */
  private static final long serialVersionUID = 2177082328716962295L;

  public final static String navigateToLabel = "ivacproject";

  private ProjectBean currentBean;

  FilterTable registeredExperiments;

  VerticalLayout patientViewContent;

  private String resourceUrl;
  private DataHandler datahandler;
  private State state;

  private Label contact;

  private Label descContent;

  private Button export;

  private Label patientInformation;

  private VerticalLayout status;

  private Grid experiments;

  private VerticalLayout buttonLayoutSection;
  private VerticalLayout graphSectionContent;

  private TreeMap<String, String> members;
  private HashMap<String, String> memberLetters;

  private ToolBar toolbar;

  private VerticalLayout membersSection;

  private Label hlaTypeLabel;

  private TabSheet patientViewTab;

  private HorizontalLayout membersLayout;

  private StringBuilder memberString;

  private String headerLabel;

  public String getHeaderLabel() {
    return headerLabel;
  }

  public void setHeaderLabel(String headerLabel) {
    this.headerLabel = headerLabel;
  }

  private DatasetComponent datasetComponent;

  private BiologicalSamplesComponent biologicalSamplesComponent;

  private LevelComponent measuredSamplesComponent;

  private LevelComponent resultsComponent;

  private PatientStatusComponent statusComponent;

  private WorkflowViewController wfController;

  private WorkflowComponent workflowComponent;

  private ConfigurationManager manager;

  private UploadComponent uploadComponent;

  private ProjInformationComponent projectInformation;

  private static Logger LOGGER = new Log4j2Logger(PatientView.class);



  public PatientView(DataHandler datahandler, State state, String resourceurl,
      WorkflowViewController wfController, ConfigurationManager manager) {
    this(datahandler, state, wfController);
    this.resourceUrl = resourceurl;
    this.manager = manager;
  }

  public PatientView(DataHandler datahandler, State state, WorkflowViewController wfController) {
    this.datahandler = datahandler;
    this.wfController = wfController;
    this.state = state;
    resourceUrl = "javascript;";
    initView();
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectBean
   */
  public void setContainerDataSource(ProjectBean projectBean) {
    this.currentBean = projectBean;

    registeredExperiments.setContainerDataSource(projectBean.getExperiments());
    registeredExperiments.setVisibleColumns(new Object[] {"code", "type", "registrationDate",
        "registrator", "status"});

    int rowNumber = projectBean.getExperiments().size();

    if (rowNumber == 0) {
      registeredExperiments.setVisible(false);
    } else {
      registeredExperiments.setVisible(true);
      registeredExperiments.setPageLength(Math.min(rowNumber, 10));
    }
  }


  /**
   * updates view, if height, width or the browser changes.
   * 
   * @param browserHeight
   * @param browserWidth
   * @param browser
   */
  public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
    setWidth((browserWidth * 0.85f), Unit.PIXELS);
    setHeight((browserHeight * 2.0f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() {
    patientViewContent = new VerticalLayout();
    patientViewContent.setMargin(new MarginInfo(true, true, false, false));
    // patientViewContent.setMargin(true);

    headerLabel = "";

    patientViewTab = new TabSheet();
    patientViewTab.setHeight("100%");
    patientViewTab.setWidth("100%");


    datasetComponent = new DatasetComponent(datahandler, state, resourceUrl);
    biologicalSamplesComponent =
        new BiologicalSamplesComponent(datahandler, state, resourceUrl, "Biological Samples");
    measuredSamplesComponent =
        new LevelComponent(datahandler, state, resourceUrl, "Measured Samples");
    resultsComponent = new LevelComponent(datahandler, state, resourceUrl, "Results");
    statusComponent = new PatientStatusComponent(datahandler, state, resourceUrl);
    workflowComponent = new WorkflowComponent(wfController);
    uploadComponent = new UploadComponent();
    projectInformation = new ProjInformationComponent(datahandler, state, resourceUrl);

    patientViewTab.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
    patientViewTab.addStyleName(ValoTheme.TABSHEET_FRAMED);
    patientViewTab.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

    // patientViewTab.addTab(initDescription()).setIcon(FontAwesome.INFO_CIRCLE);
    patientViewTab.addTab(projectInformation).setIcon(FontAwesome.INFO_CIRCLE);
    patientViewTab.addTab(statusComponent).setIcon(FontAwesome.CHECK_CIRCLE);
    patientViewTab.addTab(initGraph()).setIcon(FontAwesome.SITEMAP);
    patientViewTab.addTab(initMemberSection()).setIcon(FontAwesome.USERS);
    patientViewTab.addTab(initHLALayout()).setIcon(FontAwesome.BARCODE);
    patientViewTab.addTab(initTable()).setIcon(FontAwesome.FLASK);
    patientViewTab.addTab(datasetComponent).setIcon(FontAwesome.DATABASE);
    patientViewTab.addTab(biologicalSamplesComponent).setIcon(FontAwesome.TINT);
    patientViewTab.addTab(measuredSamplesComponent).setIcon(FontAwesome.SIGNAL);
    patientViewTab.addTab(resultsComponent).setIcon(FontAwesome.TH_LARGE);
    patientViewTab.addTab(workflowComponent).setIcon(FontAwesome.COGS);
    patientViewTab.addTab(uploadComponent).setIcon(FontAwesome.UPLOAD);


    patientViewTab.setImmediate(true);


    patientViewTab.addSelectedTabChangeListener(new SelectedTabChangeListener() {

      @Override
      public void selectedTabChange(SelectedTabChangeEvent event) {
        if (event.getTabSheet().getSelectedTab().getCaption().equals("Project Graph")) {
          loadGraph();
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Datasets")) {
          datasetComponent.updateUI("project", getCurrentBean().getId());
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Measured Samples")) {
          measuredSamplesComponent.updateUI("project", getCurrentBean().getId(), "measured");
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Biological Samples")) {
          biologicalSamplesComponent.updateUI(getCurrentBean().getId());
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Results")) {
          resultsComponent.updateUI("project", getCurrentBean().getId(), "results");
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Status")) {
          statusComponent.updateUI(getCurrentBean());
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Workflows")) {
          Map<String, String> args = new HashMap<String, String>();
          args.put("id", getCurrentBean().getId());
          args.put("type", "project");
          workflowComponent.update(args);
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("Upload Files")) {
          //(get space from currentBean)
          uploadComponent.updateUI(manager, getCurrentBean().getCode(), currentBean.getId().split("/")[1], datahandler
              .getOpenBisClient());
        } else if (event.getTabSheet().getSelectedTab().getCaption().equals("")) {
          projectInformation.updateUI(getCurrentBean(), "patient");
        }
      }
    });

    patientViewContent.addComponent(patientViewTab);
    this.addComponent(patientViewContent);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
    setHeaderLabel("Patient " + getCurrentBean().getCode());

    // updateContentToolBar();
    long startTime = System.nanoTime();
    updateHLALayout();
    long endTime = System.nanoTime();
    LOGGER.info(String.format("updateHLALayout took %f s", ((endTime - startTime) / 1000000000.0)));

    startTime = System.nanoTime();
    // updateContentDescription();
    endTime = System.nanoTime();
    LOGGER.info(String.format("updateContentDescription took %f s",
        ((endTime - startTime) / 1000000000.0)));

    startTime = System.nanoTime();
    // updateProjectStatus();
    statusComponent.updateUI(this.currentBean);
    endTime = System.nanoTime();
    LOGGER.info(String.format("updateProjectStatus took %f s",
        ((endTime - startTime) / 1000000000.0)));
    updateContentMemberSection();

    projectInformation.updateUI(getCurrentBean(), "patient");
  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

    projDescription.setCaption("");
    projDescription.setIcon(FontAwesome.FILE_TEXT_O);

    descContent = new Label("");
    contact = new Label("", ContentMode.HTML);

    patientInformation = new Label("No patient information provided.", ContentMode.HTML);

    projDescriptionContent.addComponent(patientInformation);
    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);
    projDescriptionContent.setMargin(new MarginInfo(true, false, true, true));
    projDescriptionContent.setSpacing(true);

    projDescription.addComponent(projDescriptionContent);

    descContent.setStyleName("patientview");
    contact.setStyleName("patientview");
    patientInformation.setStyleName("patientview");

    projDescription.setMargin(new MarginInfo(true, false, true, true));
    projDescription.setSpacing(true);
    projDescription.setWidth("100%");

    return projDescription;
  }

  void updateContentDescription() {
    contact
        .setValue("<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
            + currentBean.getId()
            + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding patient "
            + currentBean.getCode() + "</a>");
    String desc = currentBean.getDescription();
    if (!desc.isEmpty()) {
      descContent.setValue(desc);
    }
    String patientInfo = "";
    Boolean available = false;

    SearchCriteria sampleSc = new SearchCriteria();
    sampleSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
        "Q_BIOLOGICAL_ENTITY"));
    SearchCriteria projectSc = new SearchCriteria();
    projectSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
        currentBean.getCode()));
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


    /*
     * for (Iterator<ExperimentBean> i = currentBean.getExperiments().getItemIds().iterator(); i
     * .hasNext();) { // Get the current item identifier, which is an integer. ExperimentBean
     * expBean = i.next();
     * 
     * if (expBean.getType().equalsIgnoreCase(model.ExperimentType.Q_EXPERIMENTAL_DESIGN.name())) {
     * for (Iterator<SampleBean> ii = expBean.getSamples().getItemIds().iterator(); ii.hasNext();) {
     * SampleBean sampBean = ii.next(); if (sampBean.getType().equals("Q_BIOLOGICAL_ENTITY")) { if
     * (sampBean.getProperties().get("Q_ADDITIONAL_INFO") != null) { available = true; String[]
     * splitted = sampBean.getProperties().get("Q_ADDITIONAL_INFO").split(";"); for (String s :
     * splitted) { String[] splitted2 = s.split(":"); patientInfo +=
     * String.format("<p><u>%s</u>: %s </p> ", splitted2[0], splitted2[1]);
     * 
     * } } } } } }
     */
    if (available) {
      patientInformation.setValue(patientInfo);
    } else {
      patientInformation.setValue("No patient information provided.");
    }

    // membersSection.removeAllComponents();
    // membersSection.addComponent(getMembersComponent());
  }


  VerticalLayout initMemberSection() {
    VerticalLayout projMembers = new VerticalLayout();
    projMembers.setCaption("Members");

    membersSection = new VerticalLayout();
    Component membersContent = new VerticalLayout();

    // membersContent.setIcon(FontAwesome.USERS);
    // membersContent.setCaption("Members");
    membersSection.addComponent(membersContent);
    // membersSection.setMargin(new MarginInfo(false, false, false, true));
    membersSection.setWidth("100%");
    membersSection.setSpacing(true);

    membersSection.setMargin(new MarginInfo(true, false, true, true));
    projMembers.addComponent(membersSection);

    projMembers.setMargin(new MarginInfo(true, false, true, true));
    projMembers.setWidth("100%");
    projMembers.setSpacing(true);

    return projMembers;
  }

  void updateContentMemberSection() {
    membersSection.removeAllComponents();

    Component memberComponent = getMembersComponent();

    membersSection
        .addComponent(new Label(
            "The following people are members of this project. If you would like to contact them, click on their name.",
            Label.CONTENT_PREFORMATTED));

    membersSection.addComponent(memberComponent);
    membersSection.setComponentAlignment(memberComponent, Alignment.MIDDLE_CENTER);
  }

  /**
   * initializes the hla type layout
   * 
   * @return
   */
  VerticalLayout initHLALayout() {
    VerticalLayout hlaTyping = new VerticalLayout();
    VerticalLayout hlaTypingContent = new VerticalLayout();

    hlaTyping.setCaption("HLA Typing");

    // String desc = currentBean.getDescription();
    // if (!desc.isEmpty()) {
    // descContent.setValue(desc);
    // }
    hlaTypeLabel = new Label("Not available.", ContentMode.HTML);
    hlaTypeLabel.setStyleName("patientview");

    hlaTypingContent.setMargin(new MarginInfo(true, false, true, true));
    hlaTypingContent.setSpacing(true);
    // hlaTypingContent.setCaption("HLA Typing");
    // hlaTypingContent.setIcon(FontAwesome.BARCODE);
    hlaTypingContent.addComponent(hlaTypeLabel);

    hlaTyping.addComponent(hlaTypingContent);

    hlaTyping.setMargin(new MarginInfo(true, false, true, true));
    hlaTyping.setSpacing(true);
    hlaTyping.setWidth("100%");
    return hlaTyping;
  }

  void updateHLALayout() {

    String labelContent = "<head> <title></title> </head> <body> ";

    Boolean available = false;

    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
        model.ExperimentType.Q_NGS_HLATYPING.name()));
    SearchCriteria projectSc = new SearchCriteria();
    projectSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
        currentBean.getCode()));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(projectSc));

    SearchCriteria experimentSc = new SearchCriteria();
    experimentSc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
        model.ExperimentType.Q_NGS_HLATYPING.name()));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentSc));


    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> samples =
        datahandler.getOpenBisClient().getFacade().searchForSamples(sc);

    SearchCriteria sc2 = new SearchCriteria();
    sc2.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
        model.ExperimentType.Q_WF_NGS_HLATYPING.name()));
    SearchCriteria projectSc2 = new SearchCriteria();
    projectSc2.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
        currentBean.getCode()));
    sc2.addSubCriteria(SearchSubCriteria.createExperimentCriteria(projectSc2));

    SearchCriteria experimentSc2 = new SearchCriteria();
    experimentSc2.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
        model.ExperimentType.Q_WF_NGS_HLATYPING.name()));
    sc2.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentSc2));

    List<Experiment> wfExperiments =
        datahandler.getOpenBisClient().getFacade().searchForExperiments(sc2);

    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> wfSamples =
        new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample>();

    for (Experiment exp : wfExperiments) {
      if (exp.getCode().contains(currentBean.getCode())) {
        wfSamples
            .addAll(datahandler.getOpenBisClient().getSamplesofExperiment(exp.getIdentifier()));
      }
    }

    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample : samples) {
      available = true;
      String classString = sample.getProperties().get("Q_HLA_CLASS");
      String[] splitted = classString.split("_");
      String lastOne = splitted[splitted.length - 1];
      String addInformation = "";

      if (!(sample.getProperties().get("Q_ADDITIONAL_INFO") == null)) {
        addInformation = sample.getProperties().get("Q_ADDITIONAL_INFO");
      }

      if (!(sample.getProperties().get("Q_ADDITIONAL_INFO") == null)) {
        addInformation = sample.getProperties().get("Q_ADDITIONAL_INFO");
      }

      labelContent +=
          String.format("MHC Class %s " + "<p><u>Patient</u>: %s </p> " + "<p>%s </p> ", lastOne,
              sample.getProperties().get("Q_HLA_TYPING"), addInformation);
    }

    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample : wfSamples) {
      available = true;
      labelContent +=
          String.format("<u>Computational Typing (OptiType)</u>" + "<p> %s </p> ", sample
              .getProperties().get("Q_HLA_TYPING"));
    }

    labelContent += "</body>";
    if (available) {
      hlaTypeLabel.setValue(labelContent);
    }

    else {
      hlaTypeLabel.setValue("Not available.");
    }
  }

  VerticalLayout initTable() {
    registeredExperiments = this.buildFilterTable();
    this.tableClickChangeTreeView();
    VerticalLayout tableSection = new VerticalLayout();
    VerticalLayout tableSectionContent = new VerticalLayout();

    tableSection.setCaption("Experiments");

    // tableSectionContent.setCaption("Registered Experiments");
    // tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(registeredExperiments);

    tableSectionContent.setMargin(new MarginInfo(true, false, false, true));
    tableSection.setMargin(new MarginInfo(true, false, false, true));
    registeredExperiments.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);

    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.addComponent(this.export);
    buttonLayout.setMargin(new MarginInfo(false, false, true, false));
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, true, true));

    tableSection.addComponent(buttonLayoutSection);

    return tableSection;
  }

  /**
   * 
   * @return
   */
  ToolBar initToolBar() {
    SearchBarView searchBarView = new SearchBarView(datahandler);
    toolbar = new ToolBar(resourceUrl, state, searchBarView);
    toolbar.init();
    toolbar.visibleBarcode(false);
    toolbar.visibleWorkflow(true);
    toolbar.setSizeFull();
    return toolbar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentToolBar() {

    Boolean containsData = currentBean.getContainsData();
    toolbar.setDownload(containsData);
    toolbar.setWorkflow(containsData);
    toolbar.update("project", currentBean.getId());
  }


  class MemberWorker extends Thread {

    @Override
    public void run() {
      Company company = null;
      long companyId = 1;
      try {
        String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
        company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
        companyId = company.getCompanyId();
        LOGGER.debug(String.format("Using webId %s and companyId %d to get Portal User", webId,
            companyId));
      } catch (PortalException | SystemException e) {
        LOGGER.error(
            "liferay error, could not retrieve companyId. Trying default companyId, which is "
                + companyId, e.getStackTrace());
      }
      Set<String> list =
          datahandler.removeQBiCStaffFromMemberSet(datahandler.getOpenBisClient().getSpaceMembers(
              currentBean.getId().split("/")[1]));
      members = new TreeMap<String, String>();
      memberLetters = new HashMap<String, String>();

      LOGGER.debug(list.toString());

      if (list != null) {
        memberString = new StringBuilder();
        for (String member : list) {
          User user = null;
          try {
            user = UserLocalServiceUtil.getUserByScreenName(companyId, member);
          } catch (PortalException | SystemException e) {
          }

          if (memberString.length() > 0) {
            // memberString.append(" , ");
          }

          if (user == null) {
            LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", member));
            // memberString.append(member);
            members.put(member, member);
            // membersLayout.addComponent(new Label(member));
          } else {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();

            String email = user.getEmailAddress();

            String userString =
                "<a href=\"mailto:" + email + "\" style=\"color: #0068AA; text-decoration: none\">"
                    + lastName + ", " + firstName + "</a>";
            if (user.getLastName().length() > 0) {
              members.put(user.getLastName(), userString);
            }

            else {
              members.put(user.getFirstName(), userString);
            }

            // memberString.append("<a href=\"mailto:");
            // memberString.append(email);
            // memberString.append("\" style=\"color: #0068AA; text-decoration: none\">");
            // memberString.append(fullname);
            // memberString.append("</a>");
          }
        }
        synchronized (UI.getCurrent()) {
          processedMember();
        }
      }
    }
  }

  /**
   * 
   * @param list
   * @return
   */
  private Component getMembersComponent() {
    membersLayout = new HorizontalLayout();
    // membersLayout.setIcon(FontAwesome.USERS);
    // membersLayout.setCaption("Members");
    membersLayout.setWidth("100%");


    // final Button loadMembers = new Button("[+]");
    // membersLayout.addComponent(loadMembers);
    // loadMembers.setStyleName(ValoTheme.BUTTON_LINK);
    // loadMembers.addClickListener(new ClickListener() {

    // @Override
    // public void buttonClick(ClickEvent event) {
    ProgressBar progress = new ProgressBar();
    progress.setIndeterminate(true);
    Label info =
        new Label(
            "Searching for members. Can take several seconds on big projects. Please be patient.");
    info.setStyleName(ValoTheme.LABEL_SUCCESS);
    // membersLayout.addComponent(info);
    membersLayout.addComponent(progress);
    MemberWorker worker = new MemberWorker();
    worker.start();
    UI.getCurrent().setPollInterval(500);
    // loadMembers.setEnabled(false);

    return membersLayout;
  }


  public void processedMember() {
    String memberString = "";
    Label label;

    if (members.size() < 1) {
      label = new Label("No Members found.");
    } else {
      for (Entry<String, String> entry : members.entrySet()) {
        String firstLetter = String.valueOf(entry.getKey().charAt(0));

        if (!memberLetters.containsKey(firstLetter)) {
          memberString +=
              String.format("<font size='16'><b>%s</b></font><br>", firstLetter.toUpperCase());
          memberLetters.put(firstLetter, "");
        }

        memberString += String.format("%s<br>", entry.getValue());
      }
      label = new Label(memberString, ContentMode.HTML);
    }

    // if (memberString == null || memberString.length() == 0) {
    // label = new Label("no members found.");
    // } else {
    // label = new Label(memberString.toString(), ContentMode.HTML);
    // }

    membersLayout.removeAllComponents();
    membersLayout.addComponent(label);
    membersLayout.setSpacing(true);
    membersLayout.setMargin(new MarginInfo(false, false, true, true));


    UI.getCurrent().setPollInterval(-1);
    // loadMembers.setVisible(false);
  }


  // OLD
  void updateProjectStatus() {

    BeanItemContainer<ExperimentStatusBean> experimentstatusBeans =
        datahandler.computeIvacPatientStatus(currentBean);

    int finishedExperiments = 0;
    status.removeAllComponents();
    status.setWidth(100.0f, Unit.PERCENTAGE);


    // Generate button caption column
    final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(experimentstatusBeans);
    gpc.addGeneratedProperty("started", new PropertyValueGenerator<String>() {

      @Override
      public Class<String> getType() {
        return String.class;
      }

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        String status = null;

        if ((double) item.getItemProperty("status").getValue() > 0.0) {
          status =
              "<span class=\"v-icon\" style=\"font-family: " + FontAwesome.CHECK.getFontFamily()
                  + ";color:" + "#2dd085" + "\">&#x"
                  + Integer.toHexString(FontAwesome.CHECK.getCodepoint()) + ";</span>";
        } else {
          status =
              "<span class=\"v-icon\" style=\"font-family: " + FontAwesome.TIMES.getFontFamily()
                  + ";color:" + "#f54993" + "\">&#x"
                  + Integer.toHexString(FontAwesome.TIMES.getCodepoint()) + ";</span>";
        }

        return status.toString();
      }
    });
    gpc.removeContainerProperty("identifier");

    experiments.setContainerDataSource(gpc);
    // experiments.setHeaderVisible(false);
    experiments.setHeightMode(HeightMode.ROW);
    experiments.setHeightByRows(gpc.size());
    experiments.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.6f, Unit.PIXELS);



    experiments.getColumn("status").setRenderer(new ProgressBarRenderer());
    experiments.setColumnOrder("started", "code", "description", "status", "download",
        "runWorkflow");

    ButtonRenderer downloadRenderer = new ButtonRenderer(new RendererClickListener() {
      @Override
      public void click(RendererClickEvent event) {
        ExperimentStatusBean esb = (ExperimentStatusBean) event.getItemId();

        if (esb.getDescription().equals("Barcode Generation")) {
          new Notification("Download of Barcodes not available.",
              "<br/>Please create barcodes by clicking 'Run'.", Type.WARNING_MESSAGE, true)
              .show(Page.getCurrent());
        } else if (esb.getIdentifier() == null || esb.getIdentifier().isEmpty()) {
          new Notification("No data available for download.",
              "<br/>Please do the analysis by clicking 'Run' first.", Type.WARNING_MESSAGE, true)
              .show(Page.getCurrent());
        } else {
          ArrayList<String> message = new ArrayList<String>();
          message.add("clicked");
          StringBuilder sb = new StringBuilder("type=");
          sb.append("experiment");
          sb.append("&");
          sb.append("id=");
          // sb.append(currentBean.getId());
          sb.append(esb.getIdentifier());
          message.add(sb.toString());
          message.add(DatasetView.navigateToLabel);
          state.notifyObservers(message);
        }

      }

    });


    experiments.getColumn("download").setRenderer(downloadRenderer);

    experiments.getColumn("runWorkflow").setRenderer(
        new ButtonRenderer(new RendererClickListener() {
          @Override
          public void click(RendererClickEvent event) {
            ExperimentStatusBean esb = (ExperimentStatusBean) event.getItemId();

            // TODO idea get description of item to navigate to the correct workflow ?!
            if (esb.getDescription().equals("Barcode Generation")) {
              ArrayList<String> message = new ArrayList<String>();
              message.add("clicked");
              message.add(currentBean.getId());
              message.add(BarcodeView.navigateToLabel);
              state.notifyObservers(message);
            } else {
              ArrayList<String> message = new ArrayList<String>();
              message.add("clicked");
              StringBuilder sb = new StringBuilder("type=");
              sb.append("workflowExperimentType");
              sb.append("&");
              sb.append("id=");
              sb.append("Q_WF_MS_PEPTIDEID");
              sb.append("&");
              sb.append("project=");
              sb.append(currentBean.getId());
              message.add(sb.toString());
              message.add(WorkflowView.navigateToLabel);
              state.notifyObservers(message);
            }
          }
        }));

    experiments.getColumn("started").setRenderer(new HtmlRenderer());

    ProgressBar progressBar = new ProgressBar();
    progressBar.setCaption("Overall Progress");
    progressBar.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.6f, Unit.PIXELS);
    progressBar.setStyleName("patientprogress");

    status.addComponent(progressBar);
    status.addComponent(experiments);
    status.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
    status.setComponentAlignment(experiments, Alignment.MIDDLE_CENTER);


    /**
     * Defined Experiments for iVac - Barcodes available -> done with project creation (done) -
     * Sequencing done (Status Q_NGS_MEASUREMENT) - Variants annotated (Status
     * Q_NGS_VARIANT_CALLING) - HLA Typing done (STATUS Q_NGS_WF_HLA_TYPING) - Epitope Prediction
     * done (STATUS Q_WF_NGS_EPITOPE_PREDICTION)
     */


    for (Iterator i = experimentstatusBeans.getItemIds().iterator(); i.hasNext();) {
      ExperimentStatusBean statusBean = (ExperimentStatusBean) i.next();


      // HorizontalLayout experimentStatusRow = new HorizontalLayout();
      // experimentStatusRow.setSpacing(true);

      finishedExperiments += statusBean.getStatus();

      // statusBean.setDownload("Download");
      statusBean.setWorkflow("Run");

      /*
       * if ((Integer) pairs.getValue() == 0) { Label statusLabel = new Label(pairs.getKey() + ": "
       * + FontAwesome.TIMES.getHtml(), ContentMode.HTML); statusLabel.addStyleName("redicon");
       * experimentStatusRow.addComponent(statusLabel);
       * statusContent.addComponent(experimentStatusRow); }
       * 
       * else {
       * 
       * Label statusLabel = new Label(pairs.getKey() + ": " + FontAwesome.CHECK.getHtml(),
       * ContentMode.HTML); statusLabel.addStyleName("greenicon");
       * experimentStatusRow.addComponent(statusLabel);
       * statusContent.addComponent(experimentStatusRow);
       * 
       * finishedExperiments += (Integer) pairs.getValue(); }
       * experimentStatusRow.addComponent(runWorkflow);
       * 
       * }
       */
    }


    progressBar.setValue((float) finishedExperiments / experimentstatusBeans.size());
  }

  /**
   * 
   * @param statusValues
   * @return
   */
  public VerticalLayout initProjectStatus() {
    status = new VerticalLayout();
    status.setWidth(100.0f, Unit.PERCENTAGE);

    status.setMargin(new MarginInfo(true, false, true, true));
    status.setSpacing(true);
    // status.setCaption("Project Status");
    // status.setIcon(FontAwesome.CHECK_SQUARE);
    status.setSizeFull();

    VerticalLayout projectStatus = new VerticalLayout();
    projectStatus.setCaption("Status");
    projectStatus.setMargin(new MarginInfo(true, false, true, true));
    projectStatus.setSpacing(true);

    experiments = new Grid();
    experiments.setReadOnly(true);
    experiments.setWidth(100.0f, Unit.PERCENTAGE);
    status.addComponent(experiments);

    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(0f);
    status.addComponent(progressBar);

    projectStatus.addComponent(status);

    return projectStatus;
  }

  void resetGraph() {
    graphSectionContent.removeAllComponents();
    // VerticalLayout graphSection = (VerticalLayout) graphSectionContent.getParent();
    // graphSection.getComponent(1).setVisible(true);
    // graphSection.getComponent(1).setEnabled(true);
  }

  /**
   * 
   * @return
   */
  VerticalLayout initGraph() {
    VerticalLayout graphSection = new VerticalLayout();
    graphSectionContent = new VerticalLayout();

    graphSection.setCaption("Project Graph");

    graphSectionContent.setMargin(new MarginInfo(true, false, true, true));
    graphSection.setMargin(new MarginInfo(true, false, true, true));
    graphSection.setWidth("100%");
    graphSectionContent.setWidth("100%");

    graphSection.addComponent(graphSectionContent);
    return graphSection;
  }

  public void processed() {
    UI.getCurrent().setPollInterval(-1);
  }

  class Worker extends Thread {
    private PatientView patientView;

    public Worker(PatientView current) {
      patientView = current;
    }

    @Override
    public void run() {
      patientView.updateContentGraph();
      synchronized (UI.getCurrent()) {
        processed();
      }

    }
  }

  public void loadGraph() {
    LOGGER.debug(String.valueOf(graphSectionContent.getComponentCount() == 0));
    if (graphSectionContent.getComponentCount() > 0)
      LOGGER.debug(String.valueOf(graphSectionContent.getComponent(0) instanceof Image));
    if (graphSectionContent.getComponentCount() == 0
        || !(graphSectionContent.getComponent(0) instanceof Image)) {
      ProgressBar progress = new ProgressBar();
      progress.setIndeterminate(true);
      Label info =
          new Label(
              "Computing the project graph can take several seconds on big projects. Please be patient.");
      info.setStyleName(ValoTheme.LABEL_SUCCESS);
      graphSectionContent.removeAllComponents();
      // graphSectionContent.addComponent(info);
      graphSectionContent.addComponent(progress);
      // graphSectionContent.setComponentAlignment(info, Alignment.MIDDLE_CENTER);
      graphSectionContent.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);

      Worker worker = new Worker(getCurrent());
      worker.start();
      UI.getCurrent().setPollInterval(500);
    }
  }

  private void tableClickChangeTreeView() {
    registeredExperiments.setSelectable(true);
    registeredExperiments.setImmediate(true);
    registeredExperiments.addValueChangeListener(new ViewTablesClickListener(registeredExperiments,
        ExperimentView.navigateToLabel));
  }

  /**
   * initializes and builds a filtering table for this view
   * 
   * @return
   */
  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();

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
    filterTable.setColumnHeader("registrationDate", "Registration Date");
    filterTable.setColumnHeader("registrator", "Registered By");
    filterTable.setColumnHeader("status", "Status");

    return filterTable;
  }

  public PatientView getCurrent() {
    return this;
  }

  void updateContentGraph() {
    Resource resource = getGraphResource();

    if (resource != null) {
      graphSectionContent.removeAllComponents();
      Image graphImage = new Image("", resource);

      graphSectionContent.addComponent(graphImage);
      graphSectionContent.setComponentAlignment(graphImage, Alignment.MIDDLE_CENTER);

    } else {
      Label error = new Label("Project Graph can not be computed at that time for this project");
      error.setStyleName(ValoTheme.LABEL_FAILURE);
      graphSectionContent.removeAllComponents();
      graphSectionContent.addComponent(error);
      graphSectionContent.setComponentAlignment(error, Alignment.MIDDLE_CENTER);

      LOGGER.error(String.format("%s: %s", error.getValue(), currentBean.getId()));
    }
  }

  /**
   * returns Resource which represents the project graph of the current Bean. Can be set as the
   * resource of an {@link com.vaadin.ui.Image}.
   * 
   * @return
   */
  private Resource getGraphResource() {
    Resource resource = null;
    try {
      GraphGenerator graphFrame =
          new GraphGenerator(datahandler.getOpenBisClient()
              .getSamplesOfProject(currentBean.getId()), datahandler.getOpenBisClient()
              .getSampleTypes(), datahandler.getOpenBisClient(), currentBean.getId());
      resource = graphFrame.getRes();
    } catch (IOException e) {
      LOGGER.error("graph creation failed", e.getStackTrace());
    }
    return resource;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    long startTime = System.nanoTime();
    ProjectBean pbean = datahandler.getProjectIvac(currentValue);
    long endTime = System.nanoTime();
    registeredExperiments.unselect(registeredExperiments.getValue());
    LOGGER.info(String.format("getProject took %f s", ((endTime - startTime) / 1000000000.0)));
    // if the new project bean is different than reset the graph.
    LOGGER.debug(String.valueOf(currentBean == null));

    // if (currentBean != null)
    // LOGGER.debug(String.valueOf(pbean.getId().equals(currentBean.getId())));
    if (currentBean != null && !pbean.getId().equals(currentBean.getId())) {
      LOGGER.debug("reseting graph");
      resetGraph();
    }

    startTime = System.nanoTime();
    this.setContainerDataSource(pbean);
    endTime = System.nanoTime();
    LOGGER.info(String.format("setContainerDataSource took %f s",
        ((endTime - startTime) / 1000000000.0)));

    updateContent();

    patientViewTab.setSelectedTab(0);

  }

  public ProjectBean getCurrentBean() {
    return currentBean;
  }

}
