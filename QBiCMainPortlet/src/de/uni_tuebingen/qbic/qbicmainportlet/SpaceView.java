package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class SpaceView extends Panel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2699910993266409192L;
	
	Table table;
	VerticalLayout vert;
	public SpaceView(Table table, IndexedContainer datasource) {
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
	
	
	public SpaceView(){
		//execute the above constructor with default settings, in order to have the same settings
		this(new Table(), new IndexedContainer());
	}
	
	public void setSizeFull(){
		this.table.setSizeFull();
		vert.setSizeFull();
		super.setSizeFull();
	}
	
	public void setContainerDataSource(IndexedContainer spaceViewIndexedContainer){
		this.table.setContainerDataSource(spaceViewIndexedContainer);
	}

}
