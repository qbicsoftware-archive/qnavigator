package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
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
	HorizontalSplitPanel treeComponentLayout = new HorizontalSplitPanel();
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
		this.treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
		this.treeComponentLayout.setSizeFull();
		this.treeComponentLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		
		this.setSizeFull();


		this.addComponent(this.toolbar);
		this.addComponent(this.treeComponentLayout);
		
		this.setExpandRatio(this.toolbar, 1);
		this.setExpandRatio(this.treeComponentLayout, 5);
	}
}
