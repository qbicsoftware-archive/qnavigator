package de.uni_tuebingen.qbic.qbicmainportlet;

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
import com.vaadin.ui.themes.Reindeer;

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
	};


	@Override
	protected void init(VaadinRequest request) {

		Map<String,ArrayList<String>> spaceToProj = new HashMap<String,ArrayList<String>>();
		spaceToProj.put("QBIC", new ArrayList<String>(Arrays.asList("HPTI","MUSP","KHEC")));
		Map<String,ArrayList<String>> projToExp = new HashMap<String,ArrayList<String>>();
		projToExp.put("HPTI", new ArrayList<String>(Arrays.asList("MA")));
		projToExp.put("MUSP", new ArrayList<String>(Arrays.asList("NMR","MTX")));
		Map<String,ArrayList<String>> expToSamp = new HashMap<String,ArrayList<String>>();
		expToSamp.put("MA", new ArrayList<String>(Arrays.asList("QHPTI001AB","QHPTI002DB","QHPTI003AC","QHPTI004AX","QHPTI005AC","QHPTI006AS","QHPTI007A4")));


		Tree t = new Tree("QBiC Explorer");//TreeView.getInstance();//new Tree("Tree Explorer");
		TreeView qbic_tree = new TreeView(t);
		qbic_tree.registerClickListener();

		HierarchicalContainer tc = new HierarchicalContainer();
		t.setContainerDataSource(tc);

		tc.addContainerProperty("metadata", DummyMetaData.class, new DummyMetaData());

		for(String spaceKey : spaceToProj.keySet()) {
			tc.addItem(spaceKey);
			DummyMetaData dmd = new DummyMetaData();
			dmd.setIdentifier(spaceKey);
			dmd.setType(MetaDataType.QSPACE);
			dmd.setDescription("This is space " + spaceKey);
			dmd.setNumOfChildren(-1);
			dmd.setCreationDate(new Date(2014,02,10));

			tc.getContainerProperty(spaceKey, "metadata").setValue(dmd);

			for(String proj : spaceToProj.get(spaceKey)) {
				tc.addItem(proj);
				tc.setParent(proj, spaceKey);
				DummyMetaData dmd1 = new DummyMetaData();
				dmd1.setIdentifier(proj);
				dmd1.setType(MetaDataType.QPROJECT);
				dmd1.setDescription("This is project " + proj);
				dmd1.setNumOfChildren(-1);
				dmd1.setCreationDate(new Date(2014,02,10));

				tc.getContainerProperty(proj, "metadata").setValue(dmd1);


				if(projToExp.get(proj)!=null) {
					for(String exp : projToExp.get(proj)) {
						tc.addItem(exp);
						tc.setParent(exp, proj);

						DummyMetaData dmd2 = new DummyMetaData();
						dmd2.setIdentifier(exp);
						dmd2.setType(MetaDataType.QEXPERIMENT);
						dmd2.setDescription("This is experiment " + exp);
						dmd2.setNumOfChildren(-1);
						dmd2.setCreationDate(new Date(2014,02,10));

						tc.getContainerProperty(exp, "metadata").setValue(dmd2);


						if(expToSamp.get(exp)!=null) {
							for(String samp : expToSamp.get(exp)) {
								tc.addItem(samp);
								tc.setParent(samp, exp);

								DummyMetaData dmd3 = new DummyMetaData();
								dmd3.setIdentifier(samp);
								dmd3.setType(MetaDataType.QSAMPLE);
								dmd3.setDescription("This is sample " + samp);
								dmd3.setNumOfChildren(-1);
								dmd3.setCreationDate(new Date(2014,02,10));

								tc.getContainerProperty(samp, "metadata").setValue(dmd3);

							}
						}
					}
				}
			}
		}


		IndexedContainer spaces = new IndexedContainer();
		QBiCRenderContainer qidx_container = new QBiCRenderContainer(spaces);
		qbic_tree.addObserver(qidx_container);

		spaces.addContainerProperty("identifier", String.class, "N/A");
		spaces.addContainerProperty("description", String.class, "N/A");
		spaces.addContainerProperty("type", MetaDataType.class, MetaDataType.UNDEFINED);
		spaces.addContainerProperty("number of subitems", Integer.class, 0);
		spaces.addContainerProperty("creation date", Date.class, new Date());

		Object ic_id = spaces.addItem();
		spaces.getContainerProperty(ic_id, "identifier").setValue("TEST");
		spaces.getContainerProperty(ic_id, "description").setValue("TEST HALT");
		spaces.getContainerProperty(ic_id, "type").setValue(MetaDataType.QSAMPLE);
		spaces.getContainerProperty(ic_id, "number of subitems").setValue(0);
		spaces.getContainerProperty(ic_id, "creation date").setValue(new Date(10,10,10));

		
		Navigator navigator = new Navigator(UI.getCurrent(),this);
		Tree t2 = new Tree();
		t2.setContainerDataSource(tc);
		LevelView spaceView = new LevelView(new ToolBar(ToolBar.View.Space), t/*Tree.getInstance()*/, new SpaceView(new Table(), spaces));
		LevelView addspaceView = new LevelView(new ToolBar(ToolBar.View.Space), t2/*Tree.getInstance()*/,new Button("I am doing nothing. But you will be able to add a space one day."));// new AddSpaceView(new Table(), spaces));
		navigator.addView("spaceView", spaceView);
		navigator.addView("addspaceView", addspaceView);
		navigator.navigateTo("spaceView");
		
	}

	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
	}
}