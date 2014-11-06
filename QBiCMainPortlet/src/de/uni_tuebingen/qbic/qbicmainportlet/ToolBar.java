package de.uni_tuebingen.qbic.qbicmainportlet;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class ToolBar extends HorizontalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3673630619072584036L;

	public enum View {Space, Project, Sample, Dataset};
	
	public ToolBar(View view) {
		try {
			createPopupButtons(view);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setMargin(true);
		setSpacing(true);

		setStyleName("toolbar");

		setWidth("100%");
	}

	
	
	private void createPopupButtons(View view) throws Exception {
		switch(view){
			case Space:
				this.createSpaceButtonSet();
                break;
			case Project:
				this.createProjectButtonSet();
                break;
			case Sample:
				this.createSampleButtonSet();
                break;
			case Dataset:
				this.createDatasetButtonSet();
                break;     
           default:
        	   throw new Exception("How did you do this? We show only spaces, projects and samples and new feature: datasets");
		}
		
	}

	private void createDatasetButtonSet() {
		createSpaceButtonSet();
		
	}



	private void createSampleButtonSet() {
		// TODO Auto-generated method stub
		
	}

	private void createProjectButtonSet() {
		// TODO Auto-generated method stub
		
	}
	
	class DropDown extends VerticalLayout{

		/**
		 * 
		 */
		private static final long serialVersionUID = 7837447101767097268L;
		
		public DropDown(){
			this.setSizeFull();
			this.setStyleName("toolbar");
		}
		
		public void addComponent(String componentcaption,final String view){
			Button button = new Button(componentcaption , new Button.ClickListener() {
				 
				@Override
				public void buttonClick(ClickEvent event) {
					getUI().getNavigator().navigateTo(view);
				}
			});
			button.setSizeFull();
			//button.setStyleName("toolbar");
			this.addComponent(button);
		}
		
		public void addComponent(String componentcaption){
			Button button = new Button(componentcaption);
			button.setSizeFull();
			//button.setStyleName("toolbar");
			this.addComponent(button);
		}
		
		
	}
	
	//Be aware that some style information are in the scss or css files!
	private void createSpaceButtonSet() {
		
		//planning
		PopupButton planning = new PopupButton("Planning");
		planning.setIcon(new ThemeResource("barcode.png"));
		
		DropDown planningDropDown = new DropDown();
		
		planningDropDown.addComponent("Add Space","addspaceView");
		planningDropDown.addComponent("Add User","spaceView");

		planningDropDown.addComponent("Download","addspaceView");

		planning.setContent(planningDropDown);
		planning.setPopupVisible(false);
		this.setButtonSize(planning, 128);
		
		//Integration
		PopupButton computer = new PopupButton("Integration");
		computer.setIcon(new ThemeResource("computer.png"));
		this.setButtonSize(computer, 128);
		DropDown integrationDropDown = new DropDown();
		// Dataset View
		integrationDropDown.addComponent("View Datasets","datasetView");
		computer.setContent(integrationDropDown);
		computer.setDescription("To see all dataset of a project or an experemint: Select a project or an experiment in the view below, click this button and select 'View Datasets'.");
		//Experiment
		PopupButton dna = new PopupButton("Experiment");
		dna.setIcon(new ThemeResource("dna.png"));
		this.setButtonSize(dna, 128);
		
		
		//Analysis
		PopupButton graph = new PopupButton("Analysis");
		graph.setIcon(new ThemeResource("graph.png"));
		this.setButtonSize(graph, 128);
		DropDown graphDropDown = new DropDown();
		graphDropDown.addComponent("Execute Workflow");
		graph.setContent(graphDropDown);
		
		this.addComponent(planning);
		this.addComponent(computer);
		this.addComponent(dna);
		this.addComponent(graph);
		
		this.setComponentAlignment(planning, Alignment.TOP_CENTER);
		this.setComponentAlignment(computer, Alignment.TOP_CENTER);
		this.setComponentAlignment(dna, Alignment.TOP_CENTER);
		this.setComponentAlignment(graph, Alignment.TOP_CENTER);
		
		planning.setEnabled(false);
		dna.setEnabled(false);
		graph.setEnabled(false);
		
	}
	
	private void setButtonSize(PopupButton button, Integer button_size_px){
		button.setHeight(button_size_px.toString() + "px");
		button.setWidth(button_size_px.toString() + "px");
	}
	
	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
		}
	
	
}
