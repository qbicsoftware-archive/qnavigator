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
	public SampleView(Table table, IndexedContainer datasource) {
		vert = new VerticalLayout();
		this.setCaption("Available Projects");
		
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
		this(new Table(), new IndexedContainer());
	}
	
	public void setSizeFull(){
		vert.setSizeFull();
		this.table.setSizeFull();
		super.setSizeFull();
	}
	public void setContainerDataSource(IndexedContainer sampleViewIndexedContainer){
		this.table.setContainerDataSource(sampleViewIndexedContainer);
	}
}
