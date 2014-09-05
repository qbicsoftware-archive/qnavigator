package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ExperimentView extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9156593640161721690L;
	Table table;
	VerticalLayout vert;

	private String id;
	public ExperimentView(Table table, IndexedContainer datasource, String id) {
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
	
	
	public ExperimentView(){
		//execute the above constructor with default settings, in order to have the same settings
		this(new Table(), new IndexedContainer(), "No project selected");
	}
	
	public void setSizeFull(){
		this.table.setSizeFull();
		vert.setSizeFull();
		super.setSizeFull();
	}
	
	/**
	 * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Experiment. The id is shown in the caption.
	 * @param projectInformation
	 * @param id
	 */
	public void setContainerDataSource(ExperimentInformation expInformation, String id){
		this.setStatistics(expInformation);
		this.table.setContainerDataSource(expInformation.samples);
		this.id = id;
		this.updateCaption();
	}

	private void setStatistics(ExperimentInformation expInformation) {
		vert.removeAllComponents();
		
		VerticalLayout statistics = new VerticalLayout();
		
		
		statistics.addComponent(new Label(String.format("Description: %s", expInformation.experimentType)));
		statistics.addComponent(new Label(String.format("Number of Samples: %s", expInformation.numberOfSamples)));
		statistics.addComponent(new Label(String.format("Number of Datasets: %s", expInformation.numberOfDatasets)));
		if(expInformation.numberOfDatasets > 0){
			
			String lastSample = "No Sample available";
			if(expInformation.lastChangedSample != null){
				lastSample = expInformation.lastChangedSample.split("/")[2];
			}
			statistics.addComponent(new Label(String.format("Last Change: %s", String.format("In Sample: %s. Date: %s",lastSample , expInformation.lastChangedDataset.toString()))));
		}
		vert.addComponent(statistics);
		vert.addComponent(this.table);
	}


	private void updateCaption() {
		this.setCaption(String.format("Statistics of Experiment: %s", id));
	}
	private void tableClickChangeTreeView(){
		table.setSelectable(true);
		table.setImmediate(true);
		this.table.addValueChangeListener(new ViewTablesClickListener(table, "Sample"));
	}

}
