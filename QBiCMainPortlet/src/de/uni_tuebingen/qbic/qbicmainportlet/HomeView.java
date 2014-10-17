package de.uni_tuebingen.qbic.qbicmainportlet;


import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;

public class HomeView extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 377522772714840963L;
	VerticalLayout vert;

	
	public HomeView() {
		vert = new VerticalLayout();
		Label welcome_message = new Label("Welcome to the MainPortlet of Quantitative Biology Center (QBiC)");
		
		PopupDateField sample = new PopupDateField();
        sample.setValue(new Date());
        sample.setWidth(100.0f, Unit.PERCENTAGE);
        sample.setImmediate(true);
        sample.setTimeZone(TimeZone.getTimeZone("UTC"));
        sample.setLocale(Locale.US);
        sample.setResolution(Resolution.MINUTE);
        
		welcome_message.setContentMode(ContentMode.HTML);
		
		vert.addComponent(welcome_message);
		vert.addComponent(sample);
		vert.setComponentAlignment(welcome_message, Alignment.TOP_CENTER);
		
		vert.setComponentAlignment(sample, Alignment.BOTTOM_CENTER);

		
		this.setContent(vert);
		this.setSizeFull();
	}
	
	public void setSizeFull(){
		vert.setSizeFull();
		super.setSizeFull();
	}

}
