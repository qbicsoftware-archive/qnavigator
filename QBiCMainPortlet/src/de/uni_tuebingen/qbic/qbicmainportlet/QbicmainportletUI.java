package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

import javax.portlet.PortletSession;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import submitter.Submitter;
import submitter.WorkflowSubmitterFactory;
import submitter.WorkflowSubmitterFactory.Type;
import views.WorkflowView;
import logging.Log4j2Logger;
import main.OpenBisClient;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import controllers.MultiscaleController;
import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.ConfigurationManagerFactory;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

@SuppressWarnings("serial")
@Theme("qbicmainportlet")
public class QbicmainportletUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = true, ui = QbicmainportletUI.class,
      widgetset = "de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletWidgetset")
  public static class Servlet extends VaadinServlet {

  }


  private OpenBisClient openBisConnection;
  private DataHandler datahandler;
  private VerticalLayout mainLayout;
  private ConfigurationManager manager;
  private logging.Logger LOGGER = new Log4j2Logger(QbicmainportletUI.class);
  private String version = "0.5.1";
  private String revision = "567";
  private String resUrl;
  protected View currentView;

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {
      buildNotLoggedinLayout();
    } else {
      manager = ConfigurationManagerFactory.getInstance();
      // log who is connecting, when.
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
        // write an error message if failed to load openbis and is in production
        errorMessageIfIsProduction();
        return;
      }

      this.resUrl =
          (String) getPortletSession().getAttribute("resURL", PortletSession.APPLICATION_SCOPE);
      initProgressBarAndThreading(request);
    }
  }

  void errorMessageIfIsProduction() {
    if (isInProductionMode())
      try {
        VaadinService.getCurrentResponse().sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT,
            "openbis could not be accessed.");
      } catch (IOException | IllegalArgumentException e1) {
        VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
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

  /**
   * starts the querying of openbis and initializing the view
   * 
   * @param request
   */
  protected void initProgressBarAndThreading(VaadinRequest request) {
    final VerticalLayout layout = new VerticalLayout();

    this.setContent(layout);

    final Label status = new Label("Connecting to database.");
    status.addStyleName(ValoTheme.LABEL_HUGE);
    status.addStyleName(ValoTheme.LABEL_LIGHT);
    layout.addComponent(status);
    layout.setComponentAlignment(status, Alignment.MIDDLE_RIGHT);
    try {
      buildMainLayout(datahandler, request, LiferayAndVaadinUtils.getUser().getScreenName());
    } catch (Exception e) {
      LOGGER.error("exception thrown during initialization.", e);
      status
          .setValue("An error occured, while trying to connect to the database. Please try again later, or contact your project manager.");
    }
  }

  public static QbicmainportletUI getCurrent() {
    return (QbicmainportletUI) UI.getCurrent();
  }

  public void buildMainLayout(DataHandler datahandler, VaadinRequest request, String user) {

    State state = (State) UI.getCurrent().getSession().getAttribute("state");

    MultiscaleController multiscaleController = new MultiscaleController(datahandler.getOpenBisClient(), user);

    final HomeView homeView = new HomeView(datahandler, "Your Projects", user, state, resUrl);
    DatasetView datasetView = new DatasetView(datahandler,state, resUrl);
    final SampleView sampleView = new SampleView(datahandler, state, resUrl, multiscaleController);
    final ProjectView projectView = new ProjectView(datahandler, state, resUrl);
    BarcodeView barcodeView =
        new BarcodeView(datahandler.getOpenBisClient(), manager.getBarcodeScriptsFolder(),
            manager.getBarcodePathVariable());
    final ExperimentView experimentView = new ExperimentView(datahandler, state, resUrl);
    ChangePropertiesView changepropertiesView = new ChangePropertiesView(datahandler);

    final AddPatientView addPatientView = new AddPatientView(datahandler, state, resUrl);
    final PatientView patientView = new PatientView(datahandler, state, resUrl);
    
        
    Submitter submitter = null;
    try {
      submitter = WorkflowSubmitterFactory.getSubmitter(Type.guseSubmitter, manager);
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    LOGGER.debug("SUBMITTER " + submitter);
    WorkflowViewController controller = new WorkflowViewController(submitter, datahandler.getOpenBisClient(), user);
    final WorkflowView workflowView = new WorkflowView(controller);
    
    VerticalLayout navigatorContent = new VerticalLayout();

    final Navigator navigator = new Navigator(UI.getCurrent(), navigatorContent);

    navigator.addView(DatasetView.navigateToLabel, datasetView);
    navigator.addView(SampleView.navigateToLabel, sampleView);
    navigator.addView("", homeView);

    navigator.addView(ProjectView.navigateToLabel, projectView);
    navigator.addView(BarcodeView.navigateToLabel, barcodeView);
    navigator.addView(ExperimentView.navigateToLabel, experimentView);
    navigator.addView(ChangePropertiesView.navigateToLabel, changepropertiesView);

    navigator.addView(PatientView.navigateToLabel, patientView);
    navigator.addView(AddPatientView.navigateToLabel, addPatientView);
    
    navigator.addView(WorkflowView.navigateToLabel, workflowView);

    setNavigator(navigator);

    mainLayout = new VerticalLayout();
    mainLayout.setMargin(true);

    //final TreeView tv = new TreeView(state, navigator, user);
    //tv.setOpenbisClient(this.openBisConnection);
    //tv.loadProjects();
    //state.addObserver(tv);
    //navigator.addViewChangeListener(tv);
    HorizontalLayout treeViewAndLevelView = new HorizontalLayout();
    HorizontalLayout headerView = new HorizontalLayout();
    
    //treeViewAndLevelView.addComponent(tv);
    
    VerticalLayout versionLayout = new VerticalLayout();

    Button homeButton = new Button("Home");
    homeButton.setIcon(FontAwesome.HOME);
    //homeButton.addStyleName(ValoTheme.BUTTON_QUIET);
    homeButton.setStyleName(ValoTheme.BUTTON_LARGE);
    
    homeButton.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        navigator.navigateTo("");
      }

    });
    
    SearchBarView searchBarView = new SearchBarView(datahandler);

    headerView.setWidth("100%");
    headerView.addComponent(homeButton);
    headerView.addComponent(searchBarView);    
    headerView.setComponentAlignment(searchBarView, Alignment.TOP_RIGHT);
    headerView.setComponentAlignment(homeButton, Alignment.TOP_LEFT);

    treeViewAndLevelView.addComponent(navigatorContent);
    mainLayout.addComponent(headerView);
    mainLayout.addComponent(treeViewAndLevelView);
    versionLayout.addComponent(new Label(String.format("version: %s", version)));
    if (!isInProductionMode()) {
      versionLayout.addComponent(new Label(String.format("revision: %s", revision)));
    }
    versionLayout.setMargin(new MarginInfo(true, false, false, false));
    mainLayout.addComponent(versionLayout);
    setContent(mainLayout);

    // "Responsive design"
    getPage().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
      @Override
      public void browserWindowResized(BrowserWindowResizeEvent event) {
        int height = event.getHeight();
        int width = event.getWidth();
        WebBrowser browser = event.getSource().getWebBrowser();
        //tv.rebuildLayout(height, width, browser);
        if (currentView instanceof HomeView) {
          homeView.updateView(height, width, browser);
        } else if (currentView instanceof ProjectView) {
          projectView.updateView(height, width, browser);
        } else if (currentView instanceof ExperimentView) {
          experimentView.updateView(height, width, browser);
        } else if (currentView instanceof WorkflowView){
          workflowView.updateView(height, width, browser);
        }
        else if (currentView instanceof PatientView) {
          patientView.updateView(height, width, browser);
        } else if (currentView instanceof AddPatientView) {
          addPatientView.updateView(height, width, browser);
        }
      }
    });

    navigator.addViewChangeListener(new ViewChangeListener() {

      @Override
      public boolean beforeViewChange(ViewChangeEvent event) {
        int height = getPage().getBrowserWindowHeight();
        int width = getPage().getBrowserWindowWidth();
        WebBrowser browser = getPage().getWebBrowser();
        // View oldView = event.getOldView();
        // this.setEnabled(oldView, false);

        currentView = event.getNewView();
        if (currentView instanceof HomeView) {
          homeView.updateView(height, width, browser);
        }
        if (currentView instanceof ProjectView) {
          projectView.updateView(height, width, browser);
        }
        if (currentView instanceof ExperimentView) {
          experimentView.updateView(height, width, browser);
        }
        if (currentView instanceof SampleView) {
          sampleView.updateView(height, width, browser);
        }else if (currentView instanceof WorkflowView){
          workflowView.updateView(height, width, browser);
        }
      else if (currentView instanceof PatientView) {
        patientView.updateView(height, width, browser);
      } else if (currentView instanceof AddPatientView) {
        addPatientView.updateView(height, width, browser);
      }
        return true;
      }

      private void setEnabled(View view, boolean enabled) {
        //tv.setEnabled(enabled);
        if (view instanceof HomeView) {
          homeView.setEnabled(enabled);
        }
        if (view instanceof ProjectView) {
          projectView.setEnabled(enabled);
        }
        if (view instanceof ExperimentView) {
          experimentView.setEnabled(enabled);
        }
        if (view instanceof SampleView) {
          sampleView.setEnabled(enabled);
        }
      }

      @Override
      public void afterViewChange(ViewChangeEvent event) {
        currentView = event.getNewView();
        // this.setEnabled(currentView, true);
        Object currentBean = null;
        if (currentView instanceof ProjectView) {
          //TODO refactoring
          currentBean = new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
          //currentBean = projectView.getCurrentBean();
        } else if (currentView instanceof ExperimentView) {
          currentBean = experimentView.getCurrentBean();
        } else if (currentView instanceof SampleView) {
          currentBean = sampleView.getCurrentBean();
        } else if (currentView instanceof DatasetView) {
          currentBean = new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
        }
        try {
          PortletSession portletSession = QbicmainportletUI.getCurrent().getPortletSession();
          if (portletSession != null) {
            portletSession.setAttribute("qbic_download", currentBean,
                PortletSession.APPLICATION_SCOPE);
          }
        } catch (NullPointerException e) {
          // nothing to do. during initialization that might happen. Nothing to worry about
        }


      }

    });

    // go to correct page
    String requestParams = Page.getCurrent().getUriFragment();

    LOGGER.debug("used urifragement: " + requestParams);
    if (requestParams != null) {
      navigator.navigateTo(requestParams.startsWith("!") ? requestParams.substring(1)
          : requestParams);
    } else {
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

    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    portletSession.setAttribute("openbisClient", this.openBisConnection,
        PortletSession.APPLICATION_SCOPE);

    portletSession.setAttribute("qbic_download",
        new HashMap<String, AbstractMap.SimpleEntry<String, Long>>(),
        PortletSession.APPLICATION_SCOPE);
  }

  private void initConnection() {
    this.openBisConnection =
        new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(),
            manager.getDataSourceUrl());
    this.openBisConnection.login();
    this.datahandler = new DataHandler(openBisConnection);
  }

}
