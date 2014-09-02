package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


public class SpaceView extends Panel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2699910993266409192L;
	
	Table table;
	VerticalLayout vert;

	private String id;
	public SpaceView(Table table, SpaceInformation datasource, String id) {
		vert = new VerticalLayout();
		
		this.table = table;
		this.table.setSelectable(true);
		this.setSizeFull();
		
		vert.addComponent(this.table);
		vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
		this.setContent(vert);
		
		this.setContainerDataSource(datasource, id);
		this.tableClickChangeTreeView();
	}
	
	
	public SpaceView(){
		//execute the above constructor with default settings, in order to have the same settings
		this(new Table(), new SpaceInformation(), "No space selected");
	}
	
	public void setSizeFull(){
		vert.setSizeFull();
		super.setSizeFull();
		this.table.setSizeFull();
		vert.setSpacing(true);
		vert.setMargin(true);
	}
	
	/**
	 * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Space. The id is shown in the caption.
	 * @param spaceViewIndexedContainer
	 * @param id
	 */
	public void setContainerDataSource(SpaceInformation spaceViewIndexedContainer, String id){
		
		//this.table.setContainerDataSource(spaceViewIndexedContainer);
		this.id = id;
		this.updateCaption();
		this.setStatistics(spaceViewIndexedContainer);
		this.table.setContainerDataSource(spaceViewIndexedContainer.projects);
	}


	private void setStatistics(SpaceInformation spaceViewIndexedContainer) {
		vert.removeAllComponents();
		vert.addComponent(new Label(String.format("Number of Projects: %s", spaceViewIndexedContainer.numberOfProjects)));
		vert.addComponent(new Label(String.format("Number of Experiments: %s", spaceViewIndexedContainer.numberOfExperiments)));
		vert.addComponent(new Label(String.format("Number of Samples: %s", spaceViewIndexedContainer.numberOfSamples)));
		vert.addComponent(new Label(String.format("Number of Datasets: %s", spaceViewIndexedContainer.numberOfDatasets)));
		if(spaceViewIndexedContainer.members != null){
			vert.addComponent(new Label(String.format("Members: %s", spaceViewIndexedContainer.members.toString())));
		}
		if(spaceViewIndexedContainer.numberOfDatasets > 0){
			vert.addComponent(new Label(String.format("Last Change: %s", String.format("In Experiment %s, Sample: %s. Date: %s", spaceViewIndexedContainer.lastChangedExperiment,spaceViewIndexedContainer.lastChangedSample.split("/")[2], spaceViewIndexedContainer.lastChangedDataset.toString()))));
		}
		vert.addComponent(this.table);
	}


	private void updateCaption() {
		this.setCaption(String.format("Statistics of Space %s", id));
		
	}
	
	private void tableClickChangeTreeView(){
		table.setSelectable(true);
		table.setImmediate(true);
		this.table.addValueChangeListener(new Property.ValueChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1520058540377656100L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object property  = event.getProperty().getValue();
				if(property == null){
					return;
				}
				String project = (String) table.getItem(property).getItemProperty("Project").getValue();
				State state = (State)UI.getCurrent().getSession().getAttribute("state");
				ArrayList<String> message = new ArrayList<String>();
				message.add("clicked");
				message.add(project);
				state.notifyObservers(message);
				
			}
		});
	}

}
