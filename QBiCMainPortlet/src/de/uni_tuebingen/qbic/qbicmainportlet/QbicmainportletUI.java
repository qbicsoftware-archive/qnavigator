package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
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

	@Override
	protected void init(VaadinRequest request) {
		
		Map<String,ArrayList<String>> spaceToProj = new HashMap<String,ArrayList<String>>();
		spaceToProj.put("QBIC", new ArrayList<String>(Arrays.asList("HPTI","MUSP","KHEC")));
		Map<String,ArrayList<String>> projToExp = new HashMap<String,ArrayList<String>>();
		projToExp.put("HPTI", new ArrayList<String>(Arrays.asList("MA")));
		projToExp.put("MUSP", new ArrayList<String>(Arrays.asList("NMR","MTX")));
		Map<String,ArrayList<String>> expToSamp = new HashMap<String,ArrayList<String>>();
		expToSamp.put("MA", new ArrayList<String>(Arrays.asList("QHPTI001AB","QHPTI002DB","QHPTI003AC","QHPTI004AX","QHPTI005AC","QHPTI006AS","QHPTI007A4")));
		HierarchicalContainer tc = new HierarchicalContainer();
		for(String spaceKey : spaceToProj.keySet()) {
			tc.addItem(spaceKey);
			for(String proj : spaceToProj.get(spaceKey)) {
				tc.addItem(proj);
				tc.setParent(proj, spaceKey);
				if(projToExp.get(proj)!=null) {
					for(String exp : projToExp.get(proj)) {
						tc.addItem(exp);
						tc.setParent(exp, proj);
						if(expToSamp.get(exp)!=null) {
							for(String samp : expToSamp.get(exp)) {
								tc.addItem(samp);
								tc.setParent(samp, exp);
							}
						}
					}
				}
			}
		}
		
		IndexedContainer spaces = new IndexedContainer();
		spaces.addContainerProperty("name", String.class, "");
		spaces.addContainerProperty("projects", String.class, "");
		spaces.addContainerProperty("date", String.class, "");

        Object ic_id = spaces.addItemAt(0);
        spaces.getContainerProperty(ic_id, "name").setValue("TEST1");
        spaces.getContainerProperty(ic_id, "projects").setValue("5");
        spaces.getContainerProperty(ic_id, "date").setValue("01.01.2014");
		
        Object ic_id1 = spaces.addItemAt(0);
        spaces.getContainerProperty(ic_id1, "name").setValue("TEST2");
        spaces.getContainerProperty(ic_id1, "projects").setValue("3");
        spaces.getContainerProperty(ic_id1, "date").setValue("01.01.2014");
        
        Object ic_id2 = spaces.addItemAt(0);
        spaces.getContainerProperty(ic_id2, "name").setValue("TEST3");
        spaces.getContainerProperty(ic_id2, "projects").setValue("9");
        spaces.getContainerProperty(ic_id2, "date").setValue("01.01.2014");
		
        Navigator navigator = new Navigator(UI.getCurrent(), this);
		TreeView t = new TreeView(tc);
		t.addItemClickListener(new ItemClickListener(){

			@Override
			public void itemClick(ItemClickEvent event) {
				if(event.isDoubleClick()){
					System.out.println(event.getItemId());
				}
				
			}
			
		});
        LevelView spaceView = new LevelView(new ToolBar(ToolBar.View.Space), t/*Tree.getInstance()*/, new SpaceView(new Table(), spaces));
        TreeView t2 = new TreeView(tc);

        LevelView addSpaceView = new LevelView(new ToolBar(ToolBar.View.Space),t2/*Tree.getInstance()*/, new AddSpaceView(new Table(), spaces));
        
        navigator.addView("spaceView", spaceView);
        navigator.addView("addspaceView", addSpaceView);
        navigator.navigateTo("spaceView");
	}
	
	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
	}
}