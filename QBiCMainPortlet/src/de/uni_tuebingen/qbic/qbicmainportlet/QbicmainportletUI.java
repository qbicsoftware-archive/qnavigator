package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.ConfigurationManagerFactory;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

@SuppressWarnings("serial")
@Theme("qbicmainportlet")
@Widgetset("de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletWidgetset")
public class QbicmainportletUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = QbicmainportletUI.class)
	public static class Servlet extends VaadinServlet {
	}

	public class QBiCRenderContainer implements Observer {
		private IndexedContainer idx_cont_render = null;

		public QBiCRenderContainer() {
			super();
			this.setIndexContainer(new IndexedContainer());
		}

		public QBiCRenderContainer(IndexedContainer idxcont_tmp) {
			super();
			this.setIndexContainer(idxcont_tmp);
		}


		@Override
		public void update(Observable o, Object arg) {

			this.render(arg);
		}

		public IndexedContainer getIndexContainer() {
			return idx_cont_render;
		}

		public void setIndexContainer(IndexedContainer idx_cont_render) {
			this.idx_cont_render = idx_cont_render;
		}

		public void render(Object arg) {
			Vector<Item> children = (Vector<Item>) arg;


			if (children.isEmpty()) {
				System.out.println("no children to render!");
				return ;
			}

			idx_cont_render.removeAllItems();

			// arg is the root node of the opened (double-clicked) subtree

			for (Object c : children) {
				Item cobj = (Item) c;
				DummyMetaData work_obj = (DummyMetaData) cobj.getItemProperty("metadata").getValue();

				idx_cont_render.addContainerProperty("identifier", String.class, "N/A");
				idx_cont_render.addContainerProperty("description", String.class, "N/A");
				idx_cont_render.addContainerProperty("type", MetaDataType.class, MetaDataType.UNDEFINED);
				idx_cont_render.addContainerProperty("number of subitems", Integer.class, 0);
				idx_cont_render.addContainerProperty("creation date", Date.class, new Date());

				Object ic_id = idx_cont_render.addItem();
				idx_cont_render.getContainerProperty(ic_id, "identifier").setValue(work_obj.getIdentifier());
				idx_cont_render.getContainerProperty(ic_id, "description").setValue(work_obj.getDescription());
				idx_cont_render.getContainerProperty(ic_id, "type").setValue(work_obj.getType());
				idx_cont_render.getContainerProperty(ic_id, "number of subitems").setValue(work_obj.getNumOfChildren());
				idx_cont_render.getContainerProperty(ic_id, "creation date").setValue(work_obj.getCreationDate());
			}
		}

	};

	enum MetaDataType {UNDEFINED, QSPACE, QPROJECT, QSAMPLE, QEXPERIMENT};

	public class DummyMetaData {
		public DummyMetaData() {
			this.identifier = new String();
			this.description = new String();
			this.type = MetaDataType.UNDEFINED;
			this.num_of_children = new Integer(0);
			this.creation_date = new Date();

		}

		public String getIdentifier() {
			return identifier;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public MetaDataType getType() {
			return type;
		}
		public void setType(MetaDataType type) {
			this.type = type;
		}
		public Integer getNumOfChildren() {
			return num_of_children;
		}
		public void setNumOfChildren(Integer num_of_children) {
			this.num_of_children = num_of_children;
		}
		public Date getCreationDate() {
			return creation_date;
		}
		public void setCreationDate(Date creation_date) {
			this.creation_date = creation_date;
		}

		private String identifier;
		private String description;
		private MetaDataType type;
		private Integer num_of_children;
		private Date creation_date;
	}

	private  OpenBisClient openBisConnection;


	@Override
	protected void init(VaadinRequest request) {
		initConnection();

		UI.getCurrent().getSession().setAttribute("state", new State());
		UI.getCurrent().getSession().setAttribute("datahandler", new DataHandler(this.openBisConnection));
		
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(new Button("blakdfg"));
		this.setContent(layout);
		Map<String,ArrayList<String>> spaceToProj = new HashMap<String,ArrayList<String>>();
		spaceToProj.put("QBIC", new ArrayList<String>(Arrays.asList("HPTI","MUSP","KHEC")));
		Map<String,ArrayList<String>> projToExp = new HashMap<String,ArrayList<String>>();
		projToExp.put("HPTI", new ArrayList<String>(Arrays.asList("MA")));
		projToExp.put("MUSP", new ArrayList<String>(Arrays.asList("NMR","MTX")));
		Map<String,ArrayList<String>> expToSamp = new HashMap<String,ArrayList<String>>();
		expToSamp.put("MA", new ArrayList<String>(Arrays.asList("QHPTI001AB","QHPTI002DB","QHPTI003AC","QHPTI004AX","QHPTI005AC","QHPTI006AS","QHPTI007A4")));

		DummyDataReader datareaderDummy = null;
		try {
			datareaderDummy = new DummyDataReader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> spacesDummy  = datareaderDummy.getSpaces();

		Tree t = new Tree("QBiC Explorer");//TreeView.getInstance();//new Tree("Tree Explorer");
		//t.setImmediate(true);
		//t.setSelectable(true);
		TreeView qbic_tree = new TreeView(t);
		qbic_tree.registerClickListener();

		HierarchicalContainer tc = new HierarchicalContainer();
		t.setContainerDataSource(tc);

		tc.addContainerProperty("metadata", DummyMetaData.class, new DummyMetaData());

		tc.addContainerProperty("identifier", String.class, "N/A");
		tc.addContainerProperty("type", String.class, "N/A");
		
		for(String spaceKey : spacesDummy) {
			
			
			tc.addItem(spaceKey);
			
			tc.getContainerProperty(spaceKey, "identifier").setValue("IVAC_ALL");
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
				e.printStackTrace();
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
							tc.setChildrenAllowed(samp, false);
						}
					}
				}
			}
			// HALLO

		}



		IndexedContainer spaces = new IndexedContainer();
		QBiCRenderContainer qidx_container = new QBiCRenderContainer(spaces);
		//qbic_tree.addObserver(qidx_container);


		spaces.addContainerProperty("identifier", String.class, "N/A");
		spaces.addContainerProperty("description", String.class, "N/A");
		spaces.addContainerProperty("type", MetaDataType.class, MetaDataType.UNDEFINED);
		spaces.addContainerProperty("number of subitems", Integer.class, 0);
		spaces.addContainerProperty("creation date", Date.class, new Date());

		Object ic_id = spaces.addItem();
		spaces.getContainerProperty(ic_id, "identifier").setValue("QBIC_ROOT");
		spaces.getContainerProperty(ic_id, "description").setValue("Root node of TreeView");
		spaces.getContainerProperty(ic_id, "type").setValue(MetaDataType.UNDEFINED);


		try {
			spaces.getContainerProperty(ic_id, "number of subitems").setValue(spacesDummy.size());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			spaces.getContainerProperty(ic_id, "number of subitems").setValue(0);
			e.printStackTrace();
		}


		spaces.getContainerProperty(ic_id, "creation date").setValue(new Date(10,10,10));


		Navigator navigator = new Navigator(UI.getCurrent(),this);




		TreeView tv = new TreeView();
		tv.setContainerDataSource(tc);

		TreeView tv2 = new TreeView();
		tv2.setContainerDataSource(tc);

		TreeView tv3 = new TreeView();
		tv3.setContainerDataSource(tc);

		State state = (State) UI.getCurrent().getSession().getAttribute("state");

		state.addObserver(tv);
		state.addObserver(tv2);
		state.addObserver(tv3);

		LevelView spaceView = new LevelView(new ToolBar(ToolBar.View.Space), tv /*Tree.getInstance()*/, new SpaceView(new Table(), spaces));
		LevelView addspaceView = new LevelView(new ToolBar(ToolBar.View.Space), tv2/*Tree.getInstance()*/,new Button("I am doing nothing. But you will be able to add a space one day."));// new AddSpaceView(new Table(), spaces));
		LevelView datasetView = new LevelView(new ToolBar(ToolBar.View.Dataset),tv3, new DatasetView());

		navigator.addView("spaceView", spaceView);
		navigator.addView("addspaceView", addspaceView);
		navigator.addView("datasetView", datasetView);
		navigator.navigateTo("spaceView");
		
	}
	
	private  void initConnection() {
		ConfigurationManager manager = ConfigurationManagerFactory.getInstance();
		//System.out.println(manager.getDataSourceURL() + manager.getDataSourceUser() + manager.getDataSourcePassword());
		// TODO LiferayUtils ?!
		this.openBisConnection = new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(), manager.getDataSourceURL(), true); //LiferayAndVaadinUtils.getOpenBisClient();
	}

	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
	}
}