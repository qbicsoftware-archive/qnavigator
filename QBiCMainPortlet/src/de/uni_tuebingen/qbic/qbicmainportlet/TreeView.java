package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.data.Container;
import com.vaadin.ui.Tree;

public class TreeView extends Tree implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8164877353787099428L;
	
	
	public TreeView(Container c){
		this.setContainerDataSource(c);
	}
	
	
}
