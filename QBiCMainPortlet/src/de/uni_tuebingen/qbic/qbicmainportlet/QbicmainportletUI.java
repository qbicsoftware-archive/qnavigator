package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.portlet.PortletSession;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import logging.Log4j2Logger;
import main.OpenBisClient;
import model.DatasetBean;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;
import model.SpaceBean;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.ConfigurationManagerFactory;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

/*
 * in portal-ext.properties the following settings must be set, in order to work
 * com.liferay.portal.servlet.filters.etag.ETagFilter=false
 * com.liferay.portal.servlet.filters.header.HeaderFilter=false
 */
@SuppressWarnings("serial")
@Theme("qbicmainportlet")
public class QbicmainportletUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = QbicmainportletUI.class, widgetset = "de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletWidgetset")//,
     // widgetset = "com.example.workflowmockup.widgetset.WorkflowmockupWidgetset")
  public static class Servlet extends VaadinServlet {
  }
  
  
  private OpenBisClient openBisConnection;
  private VerticalLayout mainLayout;
  private ConfigurationManager manager;// = ConfigurationManagerFactory.getInstance();
  private logging.Logger LOGGER = new Log4j2Logger(QbicmainportletUI.class);
  private String version = "0.2.0";
  private String revision = "466";
  private String resUrl;

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {      
      buildNotLoggedinLayout();
    } else {
      manager = ConfigurationManagerFactory.getInstance();
      // logging who is connecting, when.
      LOGGER.info(String.format("QbicNavigator (%s.%s) used by: %s", version, revision,
          LiferayAndVaadinUtils.getUser().getScreenName()));
      // try to init connection to openbis and write some session attributes, that can be accessed
      // globally
      try {
        initConnection();
        initSessionAttributes();
      } catch (Exception e) {
        // probably the connection to openbis failed
        buildOpenbisConnectionErrorLayout(request);
        if (isInProductionMode())
          try {
            VaadinService.getCurrentResponse().sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT,
                "openbis could not be accessed.");
          } catch (IOException | IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
          }
        return;
      }
      // show progress bar and initialize the view

      this.resUrl =
          (String) getPortletSession().getAttribute("resURL", PortletSession.APPLICATION_SCOPE);
      initProgressBarAndThreading(request);
      // buildMainLayout();
    }
  }

  private boolean isInProductionMode() {
    return VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode();
  }

  /**
   * standard error layout, if connection to database failed.
   * 
   * @param request
   */
  private void buildOpenbisConnectionErrorLayout(final VaadinRequest request) {
    VerticalLayout vl = new VerticalLayout();
    this.setContent(vl);
    vl.addComponent(new Label(
        "An error occured, while trying to connect to the database. Please try again later, or contact your project manager."));
    Button button = new Button("retry");
    button.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        QbicmainportletUI.getCurrent().init(request);

      }
    });
    vl.addComponent(button);
  }

  /**
   * builds page if user is not logged in
   */
  private void buildNotLoggedinLayout() {
    // Mail to qbic
    ExternalResource resource = new ExternalResource("mailto:info@qbic.uni-tuebingen.de");
    Link mailToQbicLink = new Link("", resource);
    mailToQbicLink.setIcon(new ThemeResource("mail9.png"));


    ThemeDisplay themedisplay =
        (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);

    // redirect to liferay login page
    Link loginPortalLink = new Link("", new ExternalResource(themedisplay.getURLSignIn()));
    loginPortalLink.setIcon(new ThemeResource("lock12.png"));

    // left part of the page
    VerticalLayout signIn = new VerticalLayout();
    signIn.addComponent(new Label("<h3>Sign in to manage your projects and access your data:</h3>",
        ContentMode.HTML));
    signIn.addComponent(loginPortalLink);
    signIn.setStyleName("no-user-login");
    // right part of the page
    VerticalLayout contact = new VerticalLayout();
    contact.addComponent(new Label(
        "<h3>If you are interested in doing projects get in contact:</h3>", ContentMode.HTML));
    contact.addComponent(mailToQbicLink);
    contact.setStyleName("no-user-login");

    // build final layout, with some gaps between
    HorizontalLayout notSignedInLayout = new HorizontalLayout();
    Label expandingGap1 = new Label();
    expandingGap1.setWidth("100%");
    notSignedInLayout.addComponent(expandingGap1);
    notSignedInLayout.addComponent(signIn);

    notSignedInLayout.addComponent(contact);
    notSignedInLayout.setExpandRatio(expandingGap1, 0.16f);
    notSignedInLayout.setExpandRatio(signIn, 0.36f);

    notSignedInLayout.setExpandRatio(contact, 0.36f);

    notSignedInLayout.setWidth("100%");
    notSignedInLayout.setSpacing(true);
    setContent(notSignedInLayout);
  }

  private ProgressBar progress;
  private Label status;
  protected View currentView;


  /**
   * starts the querying of openbis and initializing the view
   * 
   * @param request
   */
  protected void initProgressBarAndThreading(VaadinRequest request) {
    final VerticalLayout layout = new VerticalLayout();

    this.setContent(layout);

    status = new Label("not running");
    status.addStyleName("h1");
    layout.addComponent(status);
    // Create the indicator, disabled until progress is started
    progress = new ProgressBar(new Float(0.0));
    progress.setEnabled(false);
    progress.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.6f, Unit.PIXELS);
    layout.addComponent(progress);

    final HierarchicalContainer tc = new HierarchicalContainer();
    final SpaceBean homeSpaceBean = null;
    status.setValue("Connecting to database.");
    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    DataHandler datahandler =
        (DataHandler) portletSession.getAttribute("datahandler", PortletSession.APPLICATION_SCOPE);
    final RunnableFillsContainer th =
        new RunnableFillsContainer(datahandler, tc, homeSpaceBean, LiferayAndVaadinUtils.getUser()
            .getScreenName(), request);
    final Thread thread = new Thread(th);
    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread th, Throwable ex) {
        LOGGER.error("exception thrown during initialization.", ex);
        status
            .setValue("An error occured, while trying to connect to the database. Please try again later, or contact your project manager.");
      }
    };
    thread.setUncaughtExceptionHandler(h);
    thread.start();

    // Enable polling and set frequency to 0.5 seconds
    UI.getCurrent().setPollInterval(500);

    // Disable the button until the work is done
    progress.setEnabled(true);


  }

  class RunnableFillsContainer implements Runnable {
    // Volatile because read in another thread in access()
    volatile double current = 0.0;
    private HierarchicalContainer tc;
    // private SpaceInformation homeViewInformation;
    private String userName;
    private SpaceBean homeSpaceBean;
    private DataHandler datahandler;
    private VaadinRequest request;

    public RunnableFillsContainer(DataHandler dh, HierarchicalContainer tc,
        SpaceBean homeSpaceBean, String user, VaadinRequest request) {
      this.tc = tc;
      this.homeSpaceBean = homeSpaceBean;
      this.userName = user;
      this.datahandler = dh;
      this.request = request;
    }

    @Override
    public void run() {
      long startTime = System.nanoTime();

      status.setValue("Connecting to database.");
      List<SpaceWithProjectsAndRoleAssignments> spaceList =
          datahandler.getSpacesWithProjectInformation();

      // Initialization of Tree Container
      tc.addContainerProperty("identifier", String.class, "N/A");
      tc.addContainerProperty("type", String.class, "N/A");
      tc.addContainerProperty("project", String.class, "N/A");
      tc.addContainerProperty("caption", String.class, "N/A");

      // Initialization of Home Information
      final BeanItemContainer<SpaceBean> spaceContainer =
          new BeanItemContainer<SpaceBean>(SpaceBean.class);

      int number_of_projects = 0;
      int i = 0;
      int all = spaceList.size();
      int number_of_experiments = 0;
      int number_of_samples = 0;
      int number_of_datasets = 0;
      String lastModifiedExperiment = "N/A";
      String lastModifiedSample = "N/A";
      Date lastModifiedDate = new Date(0, 0, 0);

      final SpaceBean homeSpaceBean =
          new SpaceBean("homeSpace", "", false, new BeanItemContainer<ProjectBean>(
              ProjectBean.class), new BeanItemContainer<ExperimentBean>(ExperimentBean.class),
              new BeanItemContainer<SampleBean>(SampleBean.class),
              new BeanItemContainer<DatasetBean>(DatasetBean.class), new ArrayList<String>(),
              new ProgressBar());

      BeanItemContainer<ProjectBean> projectContainer =
          new BeanItemContainer<ProjectBean>(ProjectBean.class);
      BeanItemContainer<ExperimentBean> allExperimentsContainer =
          new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
      BeanItemContainer<SampleBean> allSamplesContainer =
          new BeanItemContainer<SampleBean>(SampleBean.class);
      BeanItemContainer<DatasetBean> allDatasetsContainer =
          new BeanItemContainer<DatasetBean>(DatasetBean.class);

      List<String> project_identifiers_tmp = new ArrayList<String>();

      Boolean patientCreation = false;
      for (SpaceWithProjectsAndRoleAssignments s : spaceList) {
        if (s.getUsers().contains(userName)) {
          String spaceName = s.getCode();
          
          if(!patientCreation & spaceName.contains("IVAC")) {
            patientCreation = true;
          }

          // TODO does this work for everyone? should it? empty container would be the aim, probably
          if (spaceName.equals("QBIC_USER_SPACE")) {
            datahandler.fillPersonsContainer(spaceName);
          }
          List<Project> projects = s.getProjects();
          number_of_projects += projects.size();

          for (Project project : projects) {

            String projectIdentifier = project.getIdentifier();
            String projectCode = project.getCode();
            project_identifiers_tmp.add(projectIdentifier);

            // if (tc.containsId(project_name)) {
            // project_name = project.getIdentifier();
            // }

            // Project descriptions can be long; truncate the string to provide a brief preview
            String desc = project.getDescription();
            if (desc == null) {
              desc = "";
            } else if (desc.length() > 0) {
              desc = desc.substring(0, Math.min(desc.length(), 100));
              if (desc.length() == 100) {
                desc += "...";
              }
            }
            tc.addItem(projectIdentifier);
            tc.getContainerProperty(projectIdentifier, "type").setValue("project");
            tc.getContainerProperty(projectIdentifier, "identifier").setValue(projectIdentifier);
            tc.getContainerProperty(projectIdentifier, "project").setValue(projectIdentifier);
            tc.getContainerProperty(projectIdentifier, "caption").setValue(projectCode);

            List<Project> tmp_list = new ArrayList<Project>();
            tmp_list.add(project);
            List<Experiment> experiments =
                datahandler.openBisClient.listExperimentsOfProjects(tmp_list);

            // Add number of experiments for every project
            number_of_experiments += experiments.size();

            List<String> experiment_identifiers = new ArrayList<String>();

            ProjectBean newProjectBean =
                new ProjectBean(projectIdentifier, projectCode, desc, homeSpaceBean,
                    new BeanItemContainer<ExperimentBean>(ExperimentBean.class), new ProgressBar(),
                    new Date(), "", "", null, false);

            projectContainer.addBean(newProjectBean);

            for (Experiment experiment : experiments) {

              String experimentIdentifier = experiment.getIdentifier();
              experiment_identifiers.add(experimentIdentifier);
              String experimentCode = experiment.getCode();

              // if (tc.containsId(experiment_name)) {
              // experiment_name = experiment.getIdentifier();
              // }
              tc.addItem(experimentIdentifier);
              tc.setParent(experimentIdentifier, projectIdentifier);
              tc.getContainerProperty(experimentIdentifier, "type").setValue("experiment");
              tc.getContainerProperty(experimentIdentifier, "identifier").setValue(experimentCode);
              tc.getContainerProperty(experimentIdentifier, "project").setValue(projectIdentifier);
              tc.getContainerProperty(experimentIdentifier, "caption").setValue(
                  String.format("%s (%s)", datahandler.openBisClient.openBIScodeToString(experiment
                      .getExperimentTypeCode()), experimentCode));

              tc.setChildrenAllowed(experimentCode, false);

              // TODO empty constructor
              ExperimentBean newExperimentBean =
                  new ExperimentBean(experimentIdentifier, experimentCode,
                      experiment.getExperimentTypeCode(), new Image(), experiment
                          .getRegistrationDetails().getUserId(), new Timestamp(experiment
                          .getRegistrationDetails().getRegistrationDate().getTime()), null, null,
                      null, null, null, null);
              allExperimentsContainer.addBean(newExperimentBean);
            }

            if (experiment_identifiers.size() > 0) {
              List<DataSet> datasets =
                  datahandler.openBisClient.getFacade().listDataSetsForExperiments(
                      experiment_identifiers);
              newProjectBean.setContainsData(datasets.size() != 0);
            }
          }
        }

        // StringBuilder lce = new StringBuilder();
        // StringBuilder lcs = new StringBuilder();
        // dh.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
        // String tmplastModifiedExperiment = lce.toString();
        // String tmplastModifiedSample = lcs.toString();
        // if (!tmplastModifiedSample.equals("N/A")) {
        // lastModifiedExperiment = tmplastModifiedExperiment;
        // lastModifiedSample = tmplastModifiedSample;
        // }

        UI.getCurrent().access(
            new UpdateProgressbar(QbicmainportletUI.getCurrent().getProgressBar(),
                QbicmainportletUI.getCurrent().getStatusLabel(), (double) (i + 1) / (double) all));
        ++i;

      }

      // TODO 3 samples are missing in comparison to openBIS ???!
      List<Sample> samplesOfSpace = new ArrayList<Sample>();
      if (project_identifiers_tmp.size() > 0) {
        samplesOfSpace = datahandler.openBisClient.listSamplesForProjects(project_identifiers_tmp);
      } else {
        // samplesOfSpace = dh.openBisClient.getSamplesofSpace(spaceName);
      }

      System.out.println(samplesOfSpace.size());
      number_of_samples += samplesOfSpace.size();
      List<String> sample_identifiers_tmp = new ArrayList<String>();
      for (Sample sa : samplesOfSpace) {
        sample_identifiers_tmp.add(sa.getIdentifier());
        SampleBean newSampleBean = new SampleBean();
        newSampleBean.setId(sa.getIdentifier());
        newSampleBean.setCode(sa.getCode());
        allSamplesContainer.addBean(newSampleBean);
      }

      List<DataSet> datasets = new ArrayList<DataSet>();
      if (sample_identifiers_tmp.size() > 0) {
        datasets = datahandler.openBisClient.listDataSetsForSamples(sample_identifiers_tmp);
      }

      for (DataSet ds : datasets) {
        DatasetBean newDatasetBean = new DatasetBean();
        newDatasetBean.setCode(ds.getCode());
        allDatasetsContainer.addBean(newDatasetBean);
      }

      homeSpaceBean.setProjects(projectContainer);
      homeSpaceBean.setExperiments(allExperimentsContainer);
      homeSpaceBean.setSamples(allSamplesContainer);
      homeSpaceBean.setDatasets(allDatasetsContainer);
      
      final Boolean includePatientCreation = patientCreation;

      long endTime = System.nanoTime();
      LOGGER.info(String.format("Took %f s", ((endTime - startTime) / 1000000000.0)));
      LOGGER.info(String.format("User %s has %d projects", userName, homeSpaceBean.getProjects()
          .size()));
      try {
        Thread.currentThread();
        Thread.sleep(100); // Sleep for 100 milliseconds
      } catch (InterruptedException e) {
      }

      // Update the UI thread-safely
      UI.getCurrent().access(new Runnable() {
        @Override
        public void run() {
          // Restore the state to initial
          progress.setValue(new Float(0.0));
          progress.setEnabled(false);
          // Stop polling
          UI.getCurrent().setPollInterval(-1);

          // QbicmainportletUI.getCurrent().buildMainLayout(tc, homeViewInformation);
          System.out.println(spaceContainer.size());
          QbicmainportletUI.getCurrent().buildMainLayout(datahandler, tc, homeSpaceBean, includePatientCreation, request);
        }
      });
    }
  }

  public ProgressBar getProgressBar() {
    return progress;
  }

  public Label getStatusLabel() {
    return status;
  }

  public static QbicmainportletUI getCurrent() {
    return (QbicmainportletUI) UI.getCurrent();
  }

  class UpdateProgressbar implements Runnable {
    ProgressBar progress;
    Label label;
    double current;

    @Override
    public void run() {
      progress.setValue(new Float(current));
      if (current < 1.0)
        status.setValue(String.valueOf(((int) (current * 100))) + "% loaded.");
      else
        status.setValue("all done");
    }

    public UpdateProgressbar(ProgressBar progress, Label label, double current2) {
      this.progress = progress;
      this.label = label;
      this.current = current2;
    }
  }


  // public void buildMainLayout(HierarchicalContainer tc, SpaceInformation homeViewInformation) {
  public void buildMainLayout(DataHandler datahandler, HierarchicalContainer tc,
      SpaceBean homeSpaceBean, Boolean patientCreation, VaadinRequest request) {
    // HierarchicalContainer tc = new HierarchicalContainer();
    // System.out.println("Filling HierarchicalTreeContainer and preparing HomeView..");
    // long startTime = System.nanoTime();



    // DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    // User user = LiferayAndVaadinUtils.getUser();
    // SpaceInformation homeViewInformation = dh.initTreeAndHomeInfo(tc, user.getScreenName());

    // long endTime = System.nanoTime();
    // System.out.println("Took "+((endTime - startTime)/ 1000000000.0) + " s");

    // System.out.println("User " +user.getScreenName() + " has " +
    // homeViewInformation.numberOfProjects + " projects.");
    State state = (State) UI.getCurrent().getSession().getAttribute("state");
    // TODO getContainsData is always false!! Should be set correctly
    // if(projectBean.getContainsData()){
    // downloadProject.setEnabled(false);
    // }

    // LevelView spaceView = new LevelView(new SpaceView());
    // LevelView addspaceView =
    // new LevelView(
    // new Button(
    // "I am doing nothing. But you will be able to add workspaces some day in the \"early\" future."));//
    // new
    // AddSpaceView(new
    // Table(),
    // spaces));
    final HomeView homeView;

    // if (homeViewInformation.numberOfProjects > 0) {
    if (homeSpaceBean.getProjects().size() > 0) {
      // homeView = new HomeView(homeViewInformation, "Your Projects");
      homeView = new HomeView(datahandler, homeSpaceBean, "Your Projects", patientCreation);
    } else {
      homeView = new HomeView(datahandler);
    }
    LevelView maxQuantWorkflowView = new LevelView(new Button("maxQuantWorkflowView"));
    QcMlWorkflowView qcmlView = new QcMlWorkflowView();
    state.addObserver(qcmlView);
    LevelView qcMlWorkflowView = new LevelView(qcmlView);
    LevelView testRunWorkflowView = new LevelView(new Button("testRunWorkflowView"));
    LevelView searchView = new LevelView(new SearchForUsers());
    DatasetView datasetView = new DatasetView(datahandler);
    final SampleView sampleView = new SampleView(datahandler,state, resUrl);
    final ProjectView projectView = new ProjectView(datahandler, state, resUrl);
    BarcodeView barcodeView =
        new BarcodeView(datahandler.openBisClient, manager.getScriptsFolder(),
            manager.getPathVariable());
    final ExperimentView experimentView = new ExperimentView(datahandler,state, resUrl);
    ChangePropertiesView changepropertiesView = new ChangePropertiesView(datahandler);
    
    final AddPatientView addPatientView = new AddPatientView(datahandler, state, resUrl);
    final PatientView patientView = new PatientView(datahandler, state, resUrl);

    VerticalLayout navigatorContent = new VerticalLayout();

    Navigator navigator = new Navigator(UI.getCurrent(), navigatorContent);
    // navigator.addView("space", spaceView);
    // navigator.addView("addspaceView", addspaceView);
    navigator.addView(DatasetView.navigateToLabel, datasetView);
    navigator.addView(SampleView.navigateToLabel, sampleView);
    navigator.addView("", homeView);

    navigator.addView(ProjectView.navigateToLabel, projectView);
    navigator.addView(BarcodeView.navigateToLabel, barcodeView);
    navigator.addView(ExperimentView.navigateToLabel, experimentView);
    navigator.addView(ChangePropertiesView.navigateToLabel, changepropertiesView);
    
    navigator.addView(PatientView.navigateTolabel, patientView);
    navigator.addView(AddPatientView.navigateTolabel, addPatientView);
    
    navigator.addView("maxQuantWorkflow", maxQuantWorkflowView);
    navigator.addView("qcMlWorkflow", qcMlWorkflowView);
    navigator.addView("testRunWorkflow", testRunWorkflowView);
    navigator.addView("searchView", searchView);
    

    setNavigator(navigator);

    mainLayout = new VerticalLayout();
    mainLayout.setMargin(true);

    final TreeView tv = new TreeView(tc, state, navigator);
    state.addObserver(tv);
    navigator.addViewChangeListener(tv);
    HorizontalLayout treeViewAndLevelView = new HorizontalLayout();
    treeViewAndLevelView.addComponent(tv);

    treeViewAndLevelView.addComponent(navigatorContent);
    mainLayout.addComponent(treeViewAndLevelView);
    mainLayout.addComponent(new Label(String.format("version: %s", version)));
    if (!isInProductionMode()) {
      mainLayout.addComponent(new Label(String.format("revision: %s", revision)));
    }
    setContent(mainLayout);

    // "Responsive design"
    getPage().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
      @Override
      public void browserWindowResized(BrowserWindowResizeEvent event) {
        int height = event.getHeight();
        int width = event.getWidth();
        WebBrowser browser = event.getSource().getWebBrowser();
        tv.rebuildLayout(height, width, browser);
        if (currentView instanceof HomeView) {
          homeView.rebuildLayout(height, width, browser);
        } else if (currentView instanceof ProjectView) {
          projectView.updateView(height, width, browser);
        } else if(currentView instanceof ExperimentView){
          experimentView.updateView(height, width, browser);
        }
      }
    });
    int height = getPage().getBrowserWindowHeight();
    int width = getPage().getBrowserWindowWidth();
    WebBrowser browser = getPage().getWebBrowser();
    homeView.rebuildLayout(height, width, browser);
    tv.rebuildLayout(height, width, browser);

    navigator.addViewChangeListener(new ViewChangeListener() {

      @Override
      public boolean beforeViewChange(ViewChangeEvent event) {
        int height = getPage().getBrowserWindowHeight();
        int width = getPage().getBrowserWindowWidth();
        WebBrowser browser = getPage().getWebBrowser();
        
        currentView = event.getNewView();
        if(currentView instanceof HomeView){
          homeView.rebuildLayout(height, width, browser);
        }
        if (currentView instanceof ProjectView) {
          projectView.updateView(height, width, browser);
        }
        if (currentView instanceof ExperimentView) {
          experimentView.updateView(height, width, browser);
        }
        if (currentView instanceof SampleView) {
          sampleView.updateView(height, width, browser);
        }
        return true;
      }

      @Override
      public void afterViewChange(ViewChangeEvent event) {
        currentView = event.getNewView();
        Object currentBean = null;
        if (currentView instanceof ProjectView) {
          currentBean = projectView.getCurrentBean();
        }
        else if (currentView instanceof ExperimentView) {
          currentBean = experimentView.getCurrentBean();
        }
        else if(currentView instanceof SampleView){
          currentBean = sampleView.getCurrentBean();
        }
        else if(currentView instanceof DatasetView){
          currentBean = new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
        }
        try{
          PortletSession portletSession = QbicmainportletUI.getCurrent().getPortletSession();
          if( portletSession != null){
            portletSession.setAttribute("qbic_download", currentBean, PortletSession.APPLICATION_SCOPE);
          }
        }catch(NullPointerException e){
          //nothing to do. during initialization that might happen. Nothing to worry about
        }

        
      }

    });
    
    String requestParams = Page.getCurrent().getUriFragment();
    
    LOGGER.info("used urifragement: " + requestParams);
    if(requestParams != null){
      navigator.navigateTo(requestParams.startsWith("!")?requestParams.substring(1):requestParams);
    }
    else {
      navigator.navigateTo("");
    }
    
  }

  public PortletSession getPortletSession() {
    UI.getCurrent().getSession().getService();
    VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
    WrappedPortletSession wrappedPortletSession =
        (WrappedPortletSession) vaadinRequest.getWrappedSession();
    return wrappedPortletSession.getPortletSession();
  }

  private void initSessionAttributes() {
    if (this.openBisConnection == null) {
      this.initConnection();
    }
    UI.getCurrent().getSession().setAttribute("state", new State());
    DataHandler dataHandler = new DataHandler(this.openBisConnection);

    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    portletSession.setAttribute("datahandler", dataHandler, PortletSession.APPLICATION_SCOPE);

    portletSession.setAttribute("qbic_download",
        new HashMap<String, AbstractMap.SimpleEntry<String, Long>>(),
        PortletSession.APPLICATION_SCOPE);
  }

  private void initConnection() {
    this.openBisConnection =
        new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(),
            manager.getDataSourceUrl());
    this.openBisConnection.login();
    addDetachListener(new DetachListener() {

      @Override
      public void detach(DetachEvent event) {
        openBisConnection.logout();
      }
    });
  }

}
