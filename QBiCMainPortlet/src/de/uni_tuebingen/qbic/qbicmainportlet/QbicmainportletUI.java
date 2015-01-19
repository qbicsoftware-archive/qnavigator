package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.server.VaadinPortletSession.PortletListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
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
@Widgetset("de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletWidgetset")

public class QbicmainportletUI extends UI {

  private OpenBisClient openBisConnection;
  private VerticalLayout mainLayout;

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {
      buildNoUserLogin();    
    } else {
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      System.out.println("QbicNavigator\nUser logged in: " + LiferayAndVaadinUtils.getUser().getScreenName()
          + " at " + dateFormat.format(new Date()) + " UTC.");
      initConnection();
      initSessionAttributes();
      buildLayout(); 
    }
  }

  private void buildNoUserLogin() {
    final GridLayout grid = new GridLayout(3, 3);
    HorizontalLayout layout = new HorizontalLayout();
    ExternalResource resource = new ExternalResource("mailto:info@qbic.uni-tuebingen.de");
    Link mailToQbicLink = new Link("", resource);
    mailToQbicLink.setIcon(new ThemeResource("mail9.png"));
    mainLayout = new VerticalLayout();
    mainLayout.setMargin(false);
    ThemeDisplay themedisplay =
        (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
    Link loginPortalLink = new Link("", new ExternalResource(themedisplay.getURLSignIn()));
    loginPortalLink.setIcon(new ThemeResource("lock12.png"));

    VerticalLayout signIn = new VerticalLayout();



    signIn.addComponent(new Label("<h3>Sign in to manage your projects and access your data:</h3>",
        ContentMode.HTML));
    signIn.addComponent(loginPortalLink);
    signIn.setStyleName("no-user-login");
    VerticalLayout contact = new VerticalLayout();
    contact.addComponent(new Label(
        "<h3>If you are interested in doing projects get in contact:</h3>", ContentMode.HTML));
    contact.addComponent(mailToQbicLink);
    contact.setStyleName("no-user-login");

    HorizontalLayout test = new HorizontalLayout();
    Label expandingGap1 = new Label();
    expandingGap1.setWidth("100%");
    test.addComponent(expandingGap1);
    test.addComponent(signIn);
    // Label expandingGap2 = new Label();
    // test.addComponent(expandingGap2);
    test.addComponent(contact);
    test.setExpandRatio(expandingGap1, 0.16f);
    test.setExpandRatio(signIn, 0.36f);

    // test.setExpandRatio(expandingGap2, 0.12f);
    test.setExpandRatio(contact, 0.36f);

    test.setWidth("100%");
    test.setSpacing(true);
    setContent(test);
  }

  private void fillHierarchicalTreeContainerWithDummyData(HierarchicalContainer tc) {
    DummyDataReader datareaderDummy = null;
    try {
      datareaderDummy = new DummyDataReader();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    ArrayList<String> spacesDummy = datareaderDummy.getSpaces();

    tc.addContainerProperty("identifier", String.class, "N/A");
    tc.addContainerProperty("type", String.class, "N/A");

    for (String spaceKey : spacesDummy) {


      tc.addItem(spaceKey);
      tc.setParent(spaceKey, null);
      tc.getContainerProperty(spaceKey, "identifier").setValue(spaceKey);
      tc.getContainerProperty(spaceKey, "type").setValue("space");

      ArrayList<String> projects = datareaderDummy.getProjects(spaceKey);


      if (projects != null) {
        for (String proj : projects) {
          tc.addItem(proj);
          tc.setParent(proj, spaceKey);

          ArrayList<String> samples = datareaderDummy.getSamples(proj);
          tc.getContainerProperty(proj, "type").setValue("project");

          if (samples != null) {
            for (String samp : samples) {

              tc.addItem(samp);
              tc.setParent(samp, proj);

              tc.getContainerProperty(samp, "type").setValue("sample");
              tc.setChildrenAllowed(samp, false);
            }
          }
        }
      }
    }
  }

  private void buildLayout() {
    HierarchicalContainer tc = new HierarchicalContainer();
    System.out.println("Filling HierarchicalTreeContainer and preparing HomeView..");
    long startTime = System.nanoTime();
    
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    User user = LiferayAndVaadinUtils.getUser();
    SpaceInformation homeViewInformation = dh.initTreeAndHomeInfo(tc, user.getScreenName());
    //dh.fillHierarchicalTreeContainer(tc,user.getScreenName());
    //SpaceInformation homeViewInformation = dh.getHomeInformation(user.getScreenName());
    
    long endTime = System.nanoTime();
    System.out.println("Took "+((endTime - startTime)/ 1000000000.0) + " s");
    
    System.out.println("User " +user.getScreenName() + " has " + homeViewInformation.numberOfProjects + " projects.");
    State state = (State) UI.getCurrent().getSession().getAttribute("state");

    LevelView spaceView =
        new LevelView(new SpaceView());
    LevelView addspaceView =
        new LevelView(
            new Button(
                "I am doing nothing. But you will be able to add workspaces some day in the \"early\" future."));// new
                                                                                                             // AddSpaceView(new
                                                                                                             // Table(),
                                                                                                             // spaces));

    LevelView homeView;

    if (homeViewInformation.numberOfProjects > 0) {
      homeView =
          new LevelView( new HomeView(
              homeViewInformation, "Your Projects"));
    } else {
      homeView =
          new LevelView( new HomeView());
    }
    LevelView maxQuantWorkflowView =
        new LevelView( new Button("maxQuantWorkflowView"));
    QcMlWorkflowView qcmlView = new QcMlWorkflowView();
    state.addObserver(qcmlView);
    LevelView qcMlWorkflowView =
        new LevelView(qcmlView);
    LevelView testRunWorkflowView =
        new LevelView( new Button(
            "testRunWorkflowView"));
    LevelView searchView = new LevelView(new SearchForUsers());
    // Reload so that MpPortletListener is activated. Stupid hack. there must be a better way to do
    // this
    JavaScript.getCurrent().execute("window.location.reload();");

    VerticalLayout navigatorContent = new VerticalLayout();
    Navigator navigator = new Navigator(UI.getCurrent(), navigatorContent);
    navigator.addView("space", spaceView);
    navigator.addView("addspaceView", addspaceView);
    navigator.addView("datasetView", new DatasetView());
    navigator.addView(SampleView.navigateToLabel, new SampleView());
    navigator.addView("", homeView);
    //navigator.addView("project", projectView);
    
    navigator.addView(ProjectView.navigateToLabel,new ProjectView());
    navigator.addView(ExperimentView.navigateToLabel, new ExperimentView());
    navigator.addView(ChangePropertiesView.navigateToLabel, new ChangePropertiesView());
    navigator.addView("maxQuantWorkflow", maxQuantWorkflowView);
    navigator.addView("qcMlWorkflow", qcMlWorkflowView);
    navigator.addView("testRunWorkflow", testRunWorkflowView);
    navigator.addView("searchView", searchView);

  

    setNavigator(navigator);


    mainLayout = new VerticalLayout();
    mainLayout.setMargin(true);
    
    ToolBar toolbar = new ToolBar(ToolBar.View.Space);
    HorizontalLayout headerLayout = new HorizontalLayout();
    headerLayout.setWidth("100%");
    headerLayout.addComponent(toolbar);
    headerLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_CENTER);
    mainLayout.addComponent(headerLayout);
    
    
    
    TreeView tv = createTreeView(tc, state);
    tv.setHeight("600px");
    navigator.addViewChangeListener(tv);
    HorizontalLayout treeViewAndLevelView = new HorizontalLayout();
    treeViewAndLevelView.addComponent(tv);
    
    treeViewAndLevelView.addComponent(navigatorContent);
    treeViewAndLevelView.setExpandRatio(tv, 1);
    treeViewAndLevelView.setExpandRatio(navigatorContent, 4);
    mainLayout.addComponent(treeViewAndLevelView);
    
    setContent(mainLayout);

    navigator.navigateTo("");
  }

  private TreeView createTreeView(HierarchicalContainer tc, State st) {
    TreeView t = new TreeView();
    t.tree.setContainerDataSource(tc);
    st.addObserver(t);
    return t;


  }
  
  public PortletSession getPortletSession(){
    VaadinRequest vaadinRequest= UI.getCurrent().getSession().getService().getCurrentRequest();
    WrappedPortletSession wrappedPortletSession = (WrappedPortletSession)vaadinRequest.getWrappedSession();
    return wrappedPortletSession.getPortletSession();
  }
  
  
  private void initSessionAttributes() {
    if (this.openBisConnection == null) {
      this.initConnection();
    }
    UI.getCurrent().getSession().setAttribute("state", new State());
    DataHandler dataHandler = new DataHandler(this.openBisConnection);
    UI.getCurrent().getSession()
        .setAttribute("datahandler", dataHandler);
    UI.getCurrent().getSession().setAttribute("qbic_download", new HashMap<String, AbstractMap.SimpleEntry<String, Long>>());
    
    
    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    portletSession.setAttribute("datahandler", dataHandler, PortletSession.APPLICATION_SCOPE);
    
    portletSession.setAttribute("qbic_download", new HashMap<String, AbstractMap.SimpleEntry<String, Long>>(),
        PortletSession.APPLICATION_SCOPE);
    
  }

  private void initConnection() {
    ConfigurationManager manager = ConfigurationManagerFactory.getInstance();

    // System.out.println(manager.getDataSourceURL() + manager.getDataSourceUser() +
    // manager.getDataSourcePassword());
    // TODO LiferayUtils ?!
    this.openBisConnection =
        new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(),
            manager.getDataSourceUrl(), true); // LiferayAndVaadinUtils.getOpenBisClient();
    addDetachListener(new DetachListener() {

      @Override
      public void detach(DetachEvent event) {
        openBisConnection.logout();
      }
    });
  }

}
