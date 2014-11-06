package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
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
      System.out.println("QbicNavigator\nUser: " + LiferayAndVaadinUtils.getUser().getScreenName()
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
    ThemeDisplay themedisplay = (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
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

  private void fillHierarchicalTreeContainer(HierarchicalContainer tc) {
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    User user = LiferayAndVaadinUtils.getUser();
    dh.fillHierarchicalTreeContainer(tc, user.getScreenName());
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
    fillHierarchicalTreeContainer(tc);
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    User user = LiferayAndVaadinUtils.getUser();
    SpaceInformation homeViewInformation = dh.getHomeInformation(user.getScreenName());


    State state = (State) UI.getCurrent().getSession().getAttribute("state");

    LevelView spaceView =
        new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new SpaceView());
    LevelView addspaceView =
        new LevelView(
            new ToolBar(ToolBar.View.Space),
            createTreeView(tc, state),
            new Button(
                "I am doing nothing. But you will be able to add workspaces some day in the early future."));// new
                                                                                                             // AddSpaceView(new
                                                                                                             // Table(),
                                                                                                             // spaces));
    LevelView datasetView =
        new LevelView(new ToolBar(ToolBar.View.Dataset), createTreeView(tc, state),
            new DatasetView());
    // Reload so that MpPortletListener is activated. Stupid hack. there must be a better way to do
    // this
    JavaScript.getCurrent().execute("window.location.reload();");
    LevelView sampleView =
        new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new SampleView());
    LevelView homeView;

    if (homeViewInformation.numberOfProjects > 0) {
      homeView =
          new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new HomeView(
              homeViewInformation, "Your Projects"));
    } else {
      homeView =
          new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new HomeView());
    }
    LevelView projectView =
        new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new ProjectView());
    LevelView experimentView =
        new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state),
            new ExperimentView());


    VerticalLayout navigatorContent = new VerticalLayout();
    Navigator navigator = new Navigator(UI.getCurrent(), navigatorContent);
    navigator.addView("space", spaceView);
    navigator.addView("addspaceView", addspaceView);
    navigator.addView("datasetView", datasetView);
    navigator.addView("sample", sampleView);
    navigator.addView("", homeView);
    navigator.addView("project", projectView);
    navigator.addView("experiment", experimentView);

    setNavigator(navigator);



    mainLayout = new VerticalLayout();
    mainLayout.setMargin(true);
    mainLayout.addComponent(navigatorContent);
    setContent(mainLayout);

    navigator.navigateTo("");
  }

  private TreeView createTreeView(HierarchicalContainer tc, State st) {
    TreeView t = new TreeView();
    t.setContainerDataSource(tc);
    st.addObserver(t);
    return t;


  }

  private void initSessionAttributes() {
    if (this.openBisConnection == null) {
      this.initConnection();
    }
    UI.getCurrent().getSession().setAttribute("state", new State());
    UI.getCurrent().getSession()
        .setAttribute("datahandler", new DataHandler(this.openBisConnection));
  }

  private void initConnection() {
    ConfigurationManager manager = ConfigurationManagerFactory.getInstance();

    // System.out.println(manager.getDataSourceURL() + manager.getDataSourceUser() +
    // manager.getDataSourcePassword());
    // TODO LiferayUtils ?!
    this.openBisConnection =
        new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(),
            manager.getDataSourceURL(), true); // LiferayAndVaadinUtils.getOpenBisClient();
    addDetachListener(new DetachListener() {

      @Override
      public void detach(DetachEvent event) {
        openBisConnection.logout();
      }
    });
  }
    
}
