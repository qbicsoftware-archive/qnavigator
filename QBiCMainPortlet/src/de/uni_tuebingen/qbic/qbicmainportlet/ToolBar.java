package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.ui.HorizontalLayout;

public class ToolBar extends HorizontalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ToolBar(){
		this.setStyleName("toolbar");
		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);
	}
}
