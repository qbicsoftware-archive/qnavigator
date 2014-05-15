package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

public class ToolBar extends HorizontalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3673630619072584036L;

	private Button overview = addButton("Project Overview", "test1");
	private Button newContact = addButton("Add Patient", "test1");
	private Button newExperiment = addButton("Add Experiment", "test1");
	private Button newSample = addButton("Add Sample", "test1");
	private Button immuno_monitor = addButton("Immune Monitoring","test1");
	private Button project_graph = addButton("Project Diagram", "test1");
	//private Button search = addSearchButton();
	private Button connection = addButton("Sample Connection", "test1");
	private Button datasetview = addButton("View/Download Datasets", "test1");
	//private Button help = addButton("Help",Ivac_portletUI.HELPVIEW);

	public ToolBar() {
		addComponent(overview);
		addComponent(project_graph);
		addComponent(newContact);
		addComponent(newExperiment);
		addComponent(newSample);
		//addComponent(immuno_monitor);
		addComponent(connection);
		addComponent(datasetview);
		//addComponent(search);
		//addComponent(help);

		overview.setIcon(new ThemeResource("../runo/icons/32/globe.png"));
		newContact.setIcon(new ThemeResource("../runo/icons/32/users.png"));
		newExperiment.setIcon(new ThemeResource("../runo/icons/32/document-add.png"));
		newSample.setIcon(new ThemeResource("../runo/icons/32/document-add.png"));
		//search.setIcon(new ThemeResource("../runo/icons/32/folder-add.png"));
		immuno_monitor.setIcon(new ThemeResource("../runo/icons/32/document-edit.png"));
		//help.setIcon(new ThemeResource("../runo/icons/32/help.png"));
		project_graph.setIcon(new ThemeResource("../runo/icons/32/reload.png"));
		connection.setIcon(new ThemeResource("../runo/icons/32/ok.png"));
		datasetview.setIcon(new ThemeResource("../runo/icons/32/document-web.png"));

		setMargin(true);
		setSpacing(true);

		setStyleName("toolbar");

		setWidth("100%");
	}

	private Button addButton(String caption, final String view) {
		@SuppressWarnings("serial")
		Button button = new Button(caption);
				
		/*
		  , new Button.ClickListener() {
		 
			@Override
			public void buttonClick(ClickEvent event) {
				getUI().getNavigator().navigateTo(view);
			}
		});
		*/
		return button;

	}
}
