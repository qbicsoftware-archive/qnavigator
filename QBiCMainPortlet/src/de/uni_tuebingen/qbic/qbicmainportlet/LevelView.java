package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class LevelView extends VerticalLayout{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4753771416181038820L;
	ToolBar toolbar;
	Tree treeView;
	Component mainComponent;
	HorizontalLayout treeComponentLayout = new HorizontalLayout();
	public LevelView(){
		
	}
	
	public LevelView(ToolBar toolbar, Tree treeView, Component component){
		this.toolbar = toolbar;
		this.treeView = treeView;
		this.mainComponent = component;
		
		this.buildLayout();

	}

	public void buildLayout(){
		this.treeComponentLayout.removeAllComponents();
		this.removeAllComponents();
		
		this.treeView.setSizeFull();
		this.treeComponentLayout.addComponent(this.treeView);
		this.treeComponentLayout.addComponent(this.mainComponent);
		//this.treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
		this.treeComponentLayout.setExpandRatio(this.treeView, 0.2f);
		this.treeComponentLayout.setExpandRatio(this.mainComponent, 0.8f);
		this.mainComponent.setSizeFull();
		this.treeComponentLayout.setSizeFull();
		this.treeComponentLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		
		this.setSizeFull();


		this.addComponent(this.toolbar);
		//Label space1 = new Label("<div style=\"font-size:xx-small; border-color:blue;border-style:dotted hidden dashed hidden;\">&nbsp;</div>", Label.CONTENT_XHTML);
		//space1.setHeight("1em");
		Label space2 = new Label("<hr width=\"100%\">",Label.CONTENT_XHTML);
		//this.addComponent(space1);
		this.addComponent(space2);
		this.addComponent(this.treeComponentLayout);
		
		this.setExpandRatio(this.toolbar, 1);
		this.setExpandRatio(this.treeComponentLayout, 5);
	}
}
