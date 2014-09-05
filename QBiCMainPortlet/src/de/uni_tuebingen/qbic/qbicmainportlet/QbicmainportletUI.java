package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.annotation.WebServlet;

import com.liferay.portal.model.User;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
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

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = QbicmainportletUI.class)
	public static class Servlet extends VaadinServlet {
	}

	private  OpenBisClient openBisConnection;
	private VerticalLayout mainLayout;

	@Override
	protected void init(VaadinRequest request) {
		
		if(LiferayAndVaadinUtils.getUser() == null){
			buildNoUserLogin();
		}
		else{
			initConnection();
			initSessionAttributes();
			buildLayout();
		}
	}
	
	private void buildNoUserLogin() {
		mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);	
		mainLayout.addComponent(new Label("You have to 'Sign in', in order to see your data."));
        setContent(mainLayout);
		
	}

	private void fillHierarchicalTreeContainer(HierarchicalContainer tc){
		DataHandler dh = (DataHandler)UI.getCurrent().getSession().getAttribute("datahandler");
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
		ArrayList<String> spacesDummy  = datareaderDummy.getSpaces();


		tc.addContainerProperty("metadata", DummyMetaData.class, new DummyMetaData());

		tc.addContainerProperty("identifier", String.class, "N/A");
		tc.addContainerProperty("type", String.class, "N/A");
		
		for(String spaceKey : spacesDummy) {
			
			
			tc.addItem(spaceKey);
			tc.setParent(spaceKey, null);
			tc.getContainerProperty(spaceKey, "identifier").setValue(spaceKey);
			tc.getContainerProperty(spaceKey, "type").setValue("space");

			DummyMetaData dmd = new DummyMetaData();
			dmd.setIdentifier(spaceKey);
			dmd.setType(MetaDataType.QSPACE);
			dmd.setDescription("This is space " + spaceKey);
			dmd.setCreationDate(new Date(2014,02,10));

			ArrayList<String> projects = datareaderDummy.getProjects(spaceKey);

			try {
				dmd.setNumOfChildren(projects.size());
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				dmd.setNumOfChildren(0);
				//e.printStackTrace();
			}

			tc.getContainerProperty(spaceKey, "metadata").setValue(dmd);

			if(projects != null){
				for(String proj : projects) {
					tc.addItem(proj);
					tc.setParent(proj, spaceKey);
					DummyMetaData dmd1 = new DummyMetaData();
					dmd1.setIdentifier(proj);
					dmd1.setType(MetaDataType.QPROJECT);
					dmd1.setDescription("This is project " + proj);
					dmd1.setCreationDate(new Date(2014,02,11));

					ArrayList<String> samples  = datareaderDummy.getSamples(proj);
					try {
						dmd1.setNumOfChildren(samples.size());
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
						dmd.setNumOfChildren(0);
						e.printStackTrace();
					}
					tc.getContainerProperty(proj, "metadata").setValue(dmd1);
					tc.getContainerProperty(proj, "type").setValue("project");

					if(samples !=null) {
						for(String samp :samples) {
							
							tc.addItem(samp);
							tc.setParent(samp, proj);

							DummyMetaData dmd3 = new DummyMetaData();
							dmd3.setIdentifier(samp);
							dmd3.setType(MetaDataType.QSAMPLE);
							dmd3.setDescription("This is sample " + samp);
							dmd3.setNumOfChildren(-1);
							dmd3.setCreationDate(new Date(2014,02,12));

							tc.getContainerProperty(samp, "metadata").setValue(dmd3);
							tc.getContainerProperty(samp, "type").setValue("sample");
							tc.setChildrenAllowed(samp, false);
						}
					}
				}
			}
			// HALLO

		}		
	}

	private void buildLayout() {
		HierarchicalContainer tc = new HierarchicalContainer();
		fillHierarchicalTreeContainer(tc);
		
		State state = (State) UI.getCurrent().getSession().getAttribute("state");
		
		LevelView spaceView = new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc,state) , new SpaceView());
		LevelView addspaceView = new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc,state),new Button("I am doing nothing. But you will be able to add a space one day."));// new AddSpaceView(new Table(), spaces));
		LevelView datasetView = new LevelView(new ToolBar(ToolBar.View.Dataset),createTreeView(tc,state), new DatasetView());
		LevelView sampleView = new LevelView(new ToolBar(ToolBar.View.Space),createTreeView(tc,state) ,new SampleView());
		LevelView homeView =new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc,state), new Label("Welcome, your data"));
		LevelView projectView =new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc,state), new ProjectView());
		LevelView experimentView = new LevelView(new ToolBar(ToolBar.View.Space), createTreeView(tc, state), new ExperimentView());
		
		
        VerticalLayout navigatorContent = new VerticalLayout();
		Navigator navigator = new Navigator(UI.getCurrent(),navigatorContent);
		navigator.addView("space", spaceView);
		navigator.addView("addspaceView", addspaceView);
		navigator.addView("datasetView", datasetView);
		navigator.addView("sample",sampleView);
		navigator.addView("", homeView);
		navigator.addView("project", projectView);
		navigator.addView("experiment", experimentView);
		
		navigator.navigateTo("");
		setNavigator(navigator);
		//Reload so that MpPortletListener is activated. Stupid hack. there must be a better way to do this
		JavaScript.getCurrent().execute("window.location.reload();");
        
		mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);	
		mainLayout.addComponent(navigatorContent);
        setContent(mainLayout);
		
	}

	private TreeView createTreeView(HierarchicalContainer tc, State st){
		TreeView t = new TreeView();
		t.setContainerDataSource(tc);
		st.addObserver(t);
		return t;
		
		
	}
	
	private void initSessionAttributes() {
		if(this.openBisConnection == null){
			this.initConnection();
		}
		UI.getCurrent().getSession().setAttribute("state", new State());
		UI.getCurrent().getSession().setAttribute("datahandler", new DataHandler(this.openBisConnection));
	}

	private  void initConnection() {
		ConfigurationManager manager = ConfigurationManagerFactory.getInstance();
		//System.out.println(manager.getDataSourceURL() + manager.getDataSourceUser() + manager.getDataSourcePassword());
		// TODO LiferayUtils ?!
		this.openBisConnection = new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(), manager.getDataSourceURL(), true); //LiferayAndVaadinUtils.getOpenBisClient();
	}

}