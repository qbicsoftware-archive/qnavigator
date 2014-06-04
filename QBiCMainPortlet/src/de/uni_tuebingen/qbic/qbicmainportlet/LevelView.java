package de.uni_tuebingen.qbic.qbicmainportlet;

import javax.swing.GroupLayout.Alignment;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class LevelView extends VerticalLayout implements View{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4753771416181038820L;
	
	//Label statusMonitor = new Label("Here we could put some job status or general information.");
	TextArea statusMonitor = new TextArea("Status monitor", "Here we could put some job status or general information.");
	ToolBar toolbar;
	TreeView treeView;
	Component mainComponent;
	HorizontalLayout headerLayout = new HorizontalLayout();
	HorizontalLayout treeComponentLayout = new HorizontalLayout();
	
	public LevelView(){
		
	}
	
	public LevelView(ToolBar toolbar, TreeView treeView, Component component){
		this.toolbar = toolbar;
		this.treeView = treeView;
		this.mainComponent = component;
		
		this.buildLayout();

	}

	public void buildLayout(){
		this.treeComponentLayout.removeAllComponents();
		this.removeAllComponents();
		this.treeComponentLayout.addComponent(this.treeView);
		this.treeComponentLayout.addComponent(this.mainComponent);
		//this.treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
		this.treeComponentLayout.setExpandRatio(this.treeView, 0.2f);
		this.treeComponentLayout.setExpandRatio(this.mainComponent, 0.8f);
		this.mainComponent.setSizeFull();
		this.treeComponentLayout.setSizeFull();
		this.treeComponentLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		this.treeComponentLayout.setMargin(true);
		
		this.setSizeFull();

		this.headerLayout.addComponent(this.statusMonitor);
		this.headerLayout.addComponent(this.toolbar);
		this.headerLayout.setMargin(true);
		this.headerLayout.setExpandRatio(this.statusMonitor, 0.2f);
		this.headerLayout.setExpandRatio(this.toolbar, 0.8f);
		this.statusMonitor.setSizeFull();
		this.toolbar.setSizeFull();
		this.headerLayout.setSizeFull();
		this.addComponent(this.headerLayout);
		//Label space1 = new Label("<div style=\"font-size:xx-small; border-color:blue;border-style:dotted hidden dashed hidden;\">&nbsp;</div>", Label.CONTENT_XHTML);
		//space1.setHeight("1em");
		Label space2 = new Label("<hr width=\"100%\">",Label.CONTENT_XHTML);
		//this.addComponent(space1);
		this.addComponent(space2);
		this.addComponent(this.treeComponentLayout);
		
		this.setExpandRatio(this.headerLayout, 1);
		this.setExpandRatio(this.treeComponentLayout, 3);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		
		if(this.mainComponent instanceof DatasetView)
		{
			Object currentValue = this.treeView.getValue();
			Object type = this.treeView.getContainerProperty(currentValue, "type");
		//	((DatasetView) this.mainComponent).setContainerDataSource(DataHandler.getDatasets(currentValue, type));
			
		}
		
		//System.out.println("Entering enter");
		//this.treeView.setValue(UI.getCurrent().getSession().getAttribute("value"));
		// TODO Auto-generated method stub
		
	}
}
