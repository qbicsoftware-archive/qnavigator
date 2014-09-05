package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends Panel {

	Table table;
	VerticalLayout vert;

	private String id;
	public ProjectView(Table table, IndexedContainer datasource, String id) {
		vert = new VerticalLayout();
		this.id = id;
		
		this.table = table;
		this.table.setSelectable(true);
		this.table.setSizeFull();
		
		vert.addComponent(this.table);
		vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
		this.setContent(vert);
				
		this.table.setContainerDataSource(datasource);
		this.tableClickChangeTreeView();
	}
	
	
	public ProjectView(){
		//execute the above constructor with default settings, in order to have the same settings
		this(new Table(), new IndexedContainer(), "No project selected");
	}
	
	public void setSizeFull(){
		this.table.setSizeFull();
		vert.setSizeFull();
		super.setSizeFull();
	}
	
	/**
	 * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Project. The id is shown in the caption.
	 * @param projectInformation
	 * @param id
	 */
	public void setContainerDataSource(ProjectInformation projectInformation, String id){
		this.setStatistics(projectInformation);
		this.table.setContainerDataSource(projectInformation.experiments);
		this.id = id;
		this.updateCaption();
	}

	private void setStatistics(ProjectInformation projectInformation) {
		vert.removeAllComponents();
		
		VerticalLayout contact = new VerticalLayout();
		contact.addComponent(new Label("QBiC contact:"));
		contact.addComponent(new Label(projectInformation.contact));
		
		VerticalLayout statistics = new VerticalLayout();
		
		
		statistics.addComponent(new Label(String.format("Description: %s", projectInformation.description)));
		statistics.addComponent(new Label(String.format("Number of Experiments: %s", projectInformation.numberOfExperiments)));
		statistics.addComponent(new Label(String.format("Number of Samples: %s", projectInformation.numberOfSamples)));
		statistics.addComponent(new Label(String.format("Number of Datasets: %s", projectInformation.numberOfDatasets)));
		HorizontalLayout temp = new HorizontalLayout();
		temp.addComponent(new Label(String.format("Status: %s", projectInformation.statusMessage)));
		temp.addComponent(projectInformation.progressBar);
		//temp.setSizeFull();
		temp.setSpacing(true);
		statistics.addComponent(temp);
		if(projectInformation.numberOfDatasets > 0){
			
			String lastSample = "No Sample available";
			if(projectInformation.lastChangedSample != null){
				lastSample = projectInformation.lastChangedSample.split("/")[2];
			}
			statistics.addComponent(new Label(String.format("Last Change: %s", String.format("In Sample: %s. Date: %s",lastSample , projectInformation.lastChangedDataset.toString()))));
		}
		HorizontalLayout head = new HorizontalLayout();
		head.addComponent(statistics);
		head.addComponent(contact);
		head.setMargin(true);
		head.setSpacing(true);
		vert.addComponent(head);
		vert.addComponent(this.table);
	}


	private void updateCaption() {
		this.setCaption(String.format("Statistics of Project: %s", id));
	}
	private void tableClickChangeTreeView(){
		table.setSelectable(true);
		table.setImmediate(true);
		this.table.addValueChangeListener(new ViewTablesClickListener(table, "Experiment"));
	}
}
