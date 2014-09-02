package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
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
	 * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Space. The id is shown in the caption.
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
		vert.addComponent(new Label(String.format("Description: %s", projectInformation.description)));
		vert.addComponent(new Label(String.format("Number of Experiments: %s", projectInformation.numberOfExperiments)));
		vert.addComponent(new Label(String.format("Number of Samples: %s", projectInformation.numberOfSamples)));
		vert.addComponent(new Label(String.format("Number of Datasets: %s", projectInformation.numberOfDatasets)));
		if(projectInformation.numberOfDatasets > 0){
			
			String lastSample = "No Sample available";
			if(projectInformation.lastChangedSample != null){
				lastSample = projectInformation.lastChangedSample.split("/")[2];
			}
			vert.addComponent(new Label(String.format("Last Change: %s", String.format("In Experiment %s, Sample: %s. Date: %s", projectInformation.lastChangedExperiment,lastSample , projectInformation.lastChangedDataset.toString()))));
		}
		vert.addComponent(this.table);
	}


	private void updateCaption() {
		this.setCaption(String.format("Statistics of Project: %s", id));
	}
	private void tableClickChangeTreeView(){
		table.setSelectable(true);
		table.setImmediate(true);
		this.table.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object property  = event.getProperty().getValue();
				if(property == null){
					return;
				}
				String experiment = (String) table.getItem(property).getItemProperty("Experiment").getValue();
				State state = (State)UI.getCurrent().getSession().getAttribute("state");
				ArrayList<String> message = new ArrayList<String>();
				message.add("clicked");
				message.add(experiment);
				state.notifyObservers(message);
				
			}
		});
	}

	
	
	
}
