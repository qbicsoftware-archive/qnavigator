package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logging.Log4j2Logger;
import logging.Logger;
import model.DatasetBean;
import model.ExperimentBean;
import model.ExperimentStatusBean;
import model.ProjectBean;
import model.SampleBean;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
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
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;

public class PatientView extends VerticalLayout implements View {

  static String navigateTolabel = "ivacproject";

  private ProjectBean currentBean;

  VerticalLayout patientViewContent;

  private String resourceUrl;
  private DataHandler datahandler;
  private State state;

  private Label contact;

  private Label descContent;

  private Label patientInformation;

  private VerticalLayout status;

  private Button registerPatients;

  private Grid experiments;

  private VerticalLayout buttonLayoutSection;
  private HorizontalLayout graphSectionContent;


  private MenuBar menubar;

  private VerticalLayout membersSection;

  private Label hlaTypeLabel;

  private static Logger LOGGER = new Log4j2Logger(ProjectView.class);



  public PatientView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }

  public PatientView(DataHandler datahandler, State state) {
    this.datahandler = datahandler;
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
    patientViewContent = new VerticalLayout();
    patientViewContent.addComponent(initMenuBar());
    patientViewContent.addComponent(initProjectStatus());
    patientViewContent.addComponent(initDescription());
    patientViewContent.addComponent(initHLALayout());
    patientViewContent.addComponent(initGraph());

    patientViewContent.setWidth("100%");
    this.addComponent(patientViewContent);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
    // updateContentMenuBar();
    updateHLALayout();
    updateContentDescription();
    updateProjectStatus();
    updateContentGraph();
  }

  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();

    // String desc = currentBean.getDescription();
    // if (!desc.isEmpty()) {
    // descContent.setValue(desc);
    // }
    descContent = new Label("");
    // contact.setValue("<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
    // + currentBean.getId()
    // + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
    // + currentBean.getId() + "</a>");
    contact = new Label("", ContentMode.HTML);

    patientInformation = new Label("No patient information provided.", ContentMode.HTML);

    projDescriptionContent.addComponent(patientInformation);
    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);
    projDescriptionContent.setMargin(true);
    projDescriptionContent.setCaption("General Information");
    projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);

    descContent.setStyleName("patientview");
    contact.setStyleName("patientview");
    patientInformation.setStyleName("patientview");

    membersSection = new VerticalLayout();
    Component membersContent = new VerticalLayout();

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    membersSection.addComponent(membersContent);
    // membersSection.setMargin(true);
    projDescription.addComponent(membersSection);
    membersSection.setWidth("100%");

    projDescription.setMargin(new MarginInfo(false, true, false, true));
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
    for (Iterator i = currentBean.getExperiments().getItemIds().iterator(); i.hasNext();) {
      // Get the current item identifier, which is an integer.
      ExperimentBean expBean = (ExperimentBean) i.next();
      System.out.println(expBean.getType());

      if (expBean.getType().equalsIgnoreCase(model.ExperimentType.Q_EXPERIMENTAL_DESIGN.name())) {
        for (Iterator ii = expBean.getSamples().getItemIds().iterator(); ii.hasNext();) {
          SampleBean sampBean = (SampleBean) ii.next();
          System.out.println(sampBean.getType());
          if (sampBean.getType().equals("Q_BIOLOGICAL_ENTITY")) {
            if (sampBean.getProperties().get("Q_ADDITIONAL_INFO") != null) {
              available = true;
              String[] splitted = sampBean.getProperties().get("Q_ADDITIONAL_INFO").split(";");
              for (String s : splitted) {
                String[] splitted2 = s.split(":");
                patientInfo += String.format("<p><u>%s</u>: %s </p> ", splitted2[0], splitted2[1]);

              }
            }
          }
        }
      }
    }

    System.out.println(patientInfo);
    if (available) {
      patientInformation.setValue(patientInfo);
    }
    // TODO use space information to check whether members really have to be recalculated.
    // For users chances are high, that they click on a project from the same space -> no
    // recalculation needed!
    Component membersContent = getMembersComponent(currentBean.getMembers());

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    membersContent.setWidth("100%");
    membersSection.removeAllComponents();
    membersSection.addComponent(membersContent);
    // membersSection.setMargin(true);
  }

  /**
   * initializes the hla type layout
   * 
   * @return
   */
  VerticalLayout initHLALayout() {
    VerticalLayout hlaTyping = new VerticalLayout();
    VerticalLayout hlaTypingContent = new VerticalLayout();

    // String desc = currentBean.getDescription();
    // if (!desc.isEmpty()) {
    // descContent.setValue(desc);
    // }
    hlaTypeLabel = new Label("Not available.", ContentMode.HTML);
    hlaTypeLabel.setStyleName("patientview");

    hlaTypingContent.setMargin(true);
    hlaTypingContent.setCaption("HLA Typing");
    hlaTypingContent.setIcon(FontAwesome.BARCODE);
    hlaTypingContent.addComponent(hlaTypeLabel);

    hlaTyping.addComponent(hlaTypingContent);

    hlaTyping.setMargin(new MarginInfo(false, true, false, true));
    hlaTyping.setWidth("100%");
    return hlaTyping;
  }

  void updateHLALayout() {

    String labelContent = "<head> <title></title> </head> <body> ";

    Boolean available = false;

    for (Iterator i = currentBean.getExperiments().getItemIds().iterator(); i.hasNext();) {
      // Get the current item identifier, which is an integer.
      ExperimentBean expBean = (ExperimentBean) i.next();

      if (expBean.getType().equalsIgnoreCase(model.ExperimentType.Q_NGS_HLATYPING.name())) {
        for (Iterator ii = expBean.getSamples().getItemIds().iterator(); ii.hasNext();) {
          SampleBean sampBean = (SampleBean) ii.next();
          if (sampBean.getType().equalsIgnoreCase(model.ExperimentType.Q_NGS_HLATYPING.name())) {
            available = true;
            String classString = sampBean.getProperties().get("Q_HLA_CLASS");
            String[] splitted = classString.split("_");
            String lastOne = splitted[splitted.length - 1];
            String addInformation = "";

            if (!(sampBean.getProperties().get("Q_ADDITIONAL_INFO") == null)) {
              addInformation = sampBean.getProperties().get("Q_ADDITIONAL_INFO");
            }

            labelContent +=
                String.format("MHC Class %s " + "<p><u>Patient</u>: %s </p> " + "<p>%s </p> ",
                    lastOne, sampBean.getProperties().get("Q_HLA_TYPING"), addInformation);

          }
        }
      }
    }
    labelContent += "</body>";
    if (available) {
      hlaTypeLabel.setValue(labelContent);
    }
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
    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
    downloadProject.setIcon(new ThemeResource("computer_higher.png"));
    downloadProject.addSeparator();
    /*
     * this.downloadCompleteProjectMenuItem = downloadProject .addItem( "<a href=\"" + resourceUrl +
     * "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project</a>"
     * , null);
     */
    // Open DatasetView
    // this.datasetOverviewMenuItem = downloadProject.addItem("Dataset Overview", null);
    downloadProject.addItem("Dataset Overview", null);
    
    MenuItem manage = menubar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));

    // Another submenu item with a sub-submenu
    // this.createBarcodesMenuItem = manage.addItem("Create Barcodes", null, null);
    // Another top-level item
    manage.addItem("Create Barcodes", null, null);
    return menubar;
  }

  /**
   * 
   * @param list
   * @return
   */
  private Component getMembersComponent(Set<String> list) {
    HorizontalLayout membersLayout = new HorizontalLayout();
    if (list != null) {
      // membersLayout.addComponent(new Label("Members:"));
      for (String member : list) {

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
          LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", member));
          membersLayout.addComponent(new Label(member));
        } catch (PortalException | SystemException e) {
          LOGGER.error(
              "reading out openbis members and matching their names to liferay users failed",
              e.getStackTrace());
        }

      }
      membersLayout.setSpacing(true);
      membersLayout.setMargin(true);
    }
    return membersLayout;
  }

  void updateProjectStatus() {

    BeanItemContainer<ExperimentStatusBean> experimentBeans =
        datahandler.computIvacPatientStatus(currentBean);

    int finishedExperiments = 0;
    status.removeAllComponents();
    status.setWidth(100.0f, Unit.PERCENTAGE);


    // Generate button caption column
    final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(experimentBeans);
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
    experiments.setHeaderVisible(false);
    experiments.setHeightMode(HeightMode.ROW);
    experiments.setHeightByRows(gpc.size());
    experiments.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.35f, Unit.PIXELS);

    experiments.getColumn("status").setRenderer(new ProgressBarRenderer());
    experiments.setColumnOrder("started", "code", "description","status","download", "runWorkflow");
    
    experiments.getColumn("download").setRenderer(new ButtonRenderer(new RendererClickListener() {
      @Override
      public void click(RendererClickEvent event) {        
        ExperimentStatusBean esb = (ExperimentStatusBean) event.getItemId();
          
          if(esb.getDescription().equals("Barcode Generation")) {
            new Notification("Download of Barcodes not available.",
                "<br/>Please create barcodes by clicking 'Run'.",
                Type.WARNING_MESSAGE, true)
                .show(Page.getCurrent());
          }
          else {
          ArrayList<String> message = new ArrayList<String>();
          message.add("clicked");
          StringBuilder sb = new StringBuilder("type=");
          sb.append("experiment");
          sb.append("&");
          sb.append("id=");
          //sb.append(currentBean.getId());
          sb.append(esb.getIdentifier());
          message.add(sb.toString());
          message.add(DatasetView.navigateToLabel);
          System.out.println(message);
          state.notifyObservers(message);
          }
        }
      
  }));
    
    experiments.getColumn("runWorkflow").setRenderer(new ButtonRenderer(new RendererClickListener() {
      @Override
      public void click(RendererClickEvent event) {
        ExperimentStatusBean esb = (ExperimentStatusBean) event.getItemId();
        
        //TODO idea get description of item to navigate to the correct workflow ?!
        if(esb.getDescription().equals("Barcode Generation")) {
          ArrayList<String> message = new ArrayList<String>();
          message.add("clicked");
          message.add(currentBean.getId());
          message.add(BarcodeView.navigateToLabel);
          state.notifyObservers(message);
        }
        else {
          new Notification("Workflow for this experiment not yet available.",
              "<br/>Please get in contact with your project manager.",
              Type.WARNING_MESSAGE, true)
              .show(Page.getCurrent());
        }
      }
  }));
  
    experiments.getColumn("started").setRenderer(new HtmlRenderer());

    ProgressBar progressBar = new ProgressBar();
    progressBar.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.4f, Unit.PIXELS);
    progressBar.setStyleName("patientprogress");

    status.addComponent(progressBar);
    status.addComponent(experiments);
    status.setComponentAlignment(experiments, Alignment.MIDDLE_CENTER);


    /**
     * Defined Experiments for iVac - Barcodes available -> done with project creation (done) -
     * Sequencing done (Status Q_NGS_MEASUREMENT) - Variants annotated (Status
     * Q_NGS_VARIANT_CALLING) - HLA Typing done (STATUS Q_NGS_WF_HLA_TYPING) - Epitope Prediction
     * done (STATUS Q_WF_NGS_EPITOPE_PREDICTION)
     */


    for (Iterator i = experimentBeans.getItemIds().iterator(); i.hasNext();) {
      ExperimentStatusBean statusBean = (ExperimentStatusBean) i.next();

            
      //HorizontalLayout experimentStatusRow = new HorizontalLayout();
      //experimentStatusRow.setSpacing(true);
      
      finishedExperiments += statusBean.getStatus();

      statusBean.setDownload("Download");
      statusBean.setRunWorkflow("Run");

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


    progressBar.setValue((float) finishedExperiments / experimentBeans.size());
  }


  /**
   * 
   * @param statusValues
   * @return
   */
  public VerticalLayout initProjectStatus() {
    status = new VerticalLayout();
    status.setMargin(true);
    status.setSpacing(true);
    status.setCaption("Project Status");
    status.setIcon(FontAwesome.CHECK_SQUARE);

    VerticalLayout projectStatus = new VerticalLayout();
    projectStatus.setMargin(new MarginInfo(true, false, false, true));
    projectStatus.setSpacing(true);

    experiments = new Grid();
    experiments.setReadOnly(true);
    experiments.setWidth("100%");
    status.addComponent(experiments);

    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(0f);
    status.addComponent(progressBar);

    projectStatus.addComponent(status);

    return projectStatus;
  }

  /**
   * 
   * @return
   */
  VerticalLayout initGraph() {
    VerticalLayout graphSection = new VerticalLayout();
    graphSectionContent = new HorizontalLayout();

    graphSectionContent.setCaption("Project Graph");
    graphSectionContent.setIcon(FontAwesome.SHARE_SQUARE_O);

    graphSectionContent.setMargin(true);
    graphSection.setMargin(new MarginInfo(false, true, false, true));
    graphSection.setWidth("100%");
    graphSectionContent.setWidth("100%");
    graphSection.addComponent(graphSectionContent);
    return graphSection;
  }

  void updateContentGraph() {
    Resource resource = getGraphResource();
    if (resource != null) {
      graphSectionContent.removeAllComponents();
      Image graphImage = new Image("", resource);
      graphSectionContent.addComponent(graphImage);
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
      GraphGenerator graphFrame = new GraphGenerator(currentBean, datahandler.openBisClient);
      resource = graphFrame.getRes();
    } catch (IOException e) {
      LOGGER.error("graph creation failed", e.getStackTrace());
    }
    return resource;
  }

  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    this.setContainerDataSource(datahandler.getProject(currentValue));
    updateContent();
  }

  public ProjectBean getCurrentBean() {
    return currentBean;
  }

}
