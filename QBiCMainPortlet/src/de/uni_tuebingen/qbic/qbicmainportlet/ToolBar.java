package de.uni_tuebingen.qbic.qbicmainportlet;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.server.ThemeResource;
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

	public enum View {Space, Project, Sample};
	
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
           default:
        	   throw new Exception("How did you do this? We show only spaces, projects and samples");
		}
		
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
		this.setButtonSize64(planning);
		
		//Integration
		PopupButton computer = new PopupButton("Integration");
		computer.setIcon(new ThemeResource("computer.png"));
		this.setButtonSize64(computer);
		DropDown integrationDropDown = new DropDown();
		integrationDropDown.addComponent("Do integrate","spaceView");
		integrationDropDown.addComponent("Do not integrate");
		computer.setContent(integrationDropDown);
		
		//Experiment
		PopupButton dna = new PopupButton("Experiment");
		dna.setIcon(new ThemeResource("dna.png"));
		this.setButtonSize64(dna);
		
		
		//Analysis
		PopupButton graph = new PopupButton("Analysis");
		graph.setIcon(new ThemeResource("graph.png"));
		this.setButtonSize64(graph);
		DropDown graphDropDown = new DropDown();
		graphDropDown.addComponent("Execute Workflow");
		graphDropDown.addComponent("What ever");
		graph.setContent(graphDropDown);
		
		this.addComponent(planning);
		this.addComponent(computer);
		this.addComponent(dna);
		this.addComponent(graph);
		
	}
	
	private void setButtonSize64(PopupButton button){
		button.setHeight("64px");
		button.setWidth("64px");
	}
	
	private Button createIconButton(String icon) {
		Button b = new Button();
		b.setIcon(new ThemeResource(icon));
		b.setStyleName(Reindeer.BUTTON_LINK);
		return b;
		}
	
	
}
