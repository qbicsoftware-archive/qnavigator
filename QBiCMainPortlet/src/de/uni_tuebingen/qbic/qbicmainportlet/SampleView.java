package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class SampleView extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 377522772714840963L;
	Table table;
	VerticalLayout vert;

	private String id;
	public SampleView(Table table, IndexedContainer datasource, String id) {
		vert = new VerticalLayout();
		this.id = id;
		this.updateCaption();
		
		this.table = table;
		this.table.setSelectable(true);
		this.table.setSizeFull();
		
		vert.addComponent(this.table);
		vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
		this.setContent(vert);
				
		this.table.setContainerDataSource(datasource);
	}
	
	
	public SampleView(){
		//execute the above constructor with default settings, in order to have the same settings
		this(new Table(), new IndexedContainer(), "No sample selected");
	}
	
	public void setSizeFull(){
		this.table.setSizeFull();
		vert.setSizeFull();
		super.setSizeFull();
	}
	
	/**
	 * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Space. The id is shown in the caption.
	 * @param spaceViewIndexedContainer
	 * @param id
	 */
	public void setContainerDataSource(IndexedContainer spaceViewIndexedContainer, String id){
		this.table.setContainerDataSource(spaceViewIndexedContainer);
		this.id = id;
		this.updateCaption();
	}


	private void updateCaption() {
		this.setCaption(String.format("Statistics of Sample: %s", id));
		
	}
}
