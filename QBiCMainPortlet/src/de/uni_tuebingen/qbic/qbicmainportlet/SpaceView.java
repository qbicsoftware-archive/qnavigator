package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class SpaceView extends VerticalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2699910993266409192L;
	
	public SpaceView() {
		this.addComponent(new TextArea("Welcome your data..."));
		
		this.addComponent(new Table());
		
	}

}
