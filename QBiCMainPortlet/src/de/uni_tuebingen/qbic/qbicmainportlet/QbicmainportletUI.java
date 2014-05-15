package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@Theme("qbicmainportlet")
public class QbicmainportletUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = QbicmainportletUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout overallLayout = new VerticalLayout();
		overallLayout.setMargin(true);
		setContent(overallLayout);

		
		HorizontalSplitPanel treeComponentLayout = new HorizontalSplitPanel();
		
		Map<String,ArrayList<String>> spaceToProj = new HashMap<String,ArrayList<String>>();
		spaceToProj.put("QBIC", new ArrayList<String>(Arrays.asList("HPTI","MUSP","KHEC")));
		Map<String,ArrayList<String>> projToExp = new HashMap<String,ArrayList<String>>();
		projToExp.put("HPTI", new ArrayList<String>(Arrays.asList("MA")));
		projToExp.put("MUSP", new ArrayList<String>(Arrays.asList("NMR","MTX")));
		Map<String,ArrayList<String>> expToSamp = new HashMap<String,ArrayList<String>>();
		expToSamp.put("MA", new ArrayList<String>(Arrays.asList("QHPTI001AB","QHPTI002DB","QHPTI003AC","QHPTI004AX","QHPTI005AC","QHPTI006AS","QHPTI007A4")));
		Tree t = new Tree("Tree Explorer");
		HierarchicalContainer tc = new HierarchicalContainer();
		t.setContainerDataSource(tc);
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
		
		final VerticalLayout testTable = new VerticalLayout();
		
		Button button1 = new Button("tell me what to do!!");
		button1.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				testTable.addComponent(new Label("Thank you for clicking"));
			}
		});
		Button button2 = new Button("tasdfsadf!!");
		button2.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				testTable.addComponent(new Label("asdfasdf"));
			}
		});
		
		PopupButton popupButton = new PopupButton("Add");
		popupButton.setIcon(new ThemeResource(
		"../runo/icons/16/document-add.png"));

		GridLayout gl = new GridLayout(4, 3);
		gl.setWidth("150px");
		gl.setHeight("100px");
		gl.addComponent(createIconButton("../runo/icons/32/document.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-delete.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-pdf.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-web.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-doc.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-ppt.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-xsl.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-image.png"));
		gl.addComponent(createIconButton("../runo/icons/32/document-txt.png"));
		
		popupButton.setContent(gl);
		popupButton.setPopupVisible(true);
		
		ToolBar toolbar = new ToolBar();	
		//toolbar.addComponent(button1);
		//toolbar.addComponent(button2);
		//toolbar.addComponent(popupButton);
		
		t.setSizeFull();
		
		treeComponentLayout.addComponent(t);
		treeComponentLayout.addComponent(new SpaceView());
		treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
		treeComponentLayout.setSizeFull();
		treeComponentLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		
		overallLayout.setSizeFull();


		overallLayout.addComponent(toolbar);
		overallLayout.addComponent(treeComponentLayout);
		
		overallLayout.setExpandRatio(toolbar, 1);
		overallLayout.setExpandRatio(treeComponentLayout, 5);

	}
	
	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
		}

}