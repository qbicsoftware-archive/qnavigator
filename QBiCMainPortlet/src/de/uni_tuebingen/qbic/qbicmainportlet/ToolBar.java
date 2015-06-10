package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class ToolBar extends HorizontalLayout {

  /**
	 * 
	 */
  private static final long serialVersionUID = -3673630619072584036L;

  public enum View {
    Space, Project, Sample, Dataset
  };

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
    switch (view) {
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
        throw new Exception(
            "How did you do this? We show only spaces, projects and samples and new feature: datasets");
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

  class DropDown extends VerticalLayout {

    /**
		 * 
		 */
    private static final long serialVersionUID = 7837447101767097268L;

    public DropDown() {
      this.setSizeFull();
      this.setStyleName("toolbar");
    }

    public void addComponent(String componentcaption, final String view) {
      Button button = new Button(componentcaption, new Button.ClickListener() {

        @Override
        public void buttonClick(ClickEvent event) {
          System.out.println(UI.getCurrent().getPage().getLocation().getPath());
          System.out.println(UI.getCurrent().getPage().getLocation().getScheme());
          System.out.println(UI.getCurrent().getPage().getLocation().getFragment());
          System.out.println(UI.getCurrent().getPage().getLocation().getAuthority());
          System.out.println(UI.getCurrent().getPage().getLocation().getQuery());
          String fragment = UI.getCurrent().getPage().getLocation().getFragment();
          fragment = fragment.substring(1);
          String[] typeAndId = fragment.split("/");
          System.out.println(fragment);
          String navigateTo = view;
          if (typeAndId != null && typeAndId.length == 2) {
            navigateTo += "/type=" + typeAndId[0] + "&id=" + typeAndId[1];
          }
          System.out.println(navigateTo);
          getUI().getNavigator().navigateTo(navigateTo);
        }
      });
      button.setSizeFull();
      // button.setStyleName("toolbar");
      this.addComponent(button);
    }

    public void addComponent(String componentcaption) {
      Button button = new Button(componentcaption);
      button.setSizeFull();
      // button.setStyleName("toolbar");
      this.addComponent(button);
    }


  }

  class DropDownStateObserver extends DropDown implements Observer {
    PopupButton graph;

    public DropDownStateObserver(PopupButton graph) {
      this.graph = graph;
    }

    @Override
    public void update(Observable o, Object arg) {
      ArrayList<String> message = (ArrayList<String>) arg;
      this.removeAllComponents();
      if (message.get(1).equals("MZML")) {
        graph.setEnabled(true);
        this.addComponent("MaxQuant", "maxQuantWorkflow");
        this.addComponent("qcML run", "qcMlWorkflow");
        this.addComponent("Test run", "testRunWorkflow");
        graph.setDescription("Execute Workflows for selected DataSet(s)");
        return;
      }
      graph.setEnabled(false);
      graph.setDescription("No workflows available for current DataSet selection.");
    }
  }

  // Be aware that some style information are in the scss or css files!
  private void createSpaceButtonSet() {

    // planning
    PopupButton planning = new PopupButton("Planning");
    planning.setIcon(new ThemeResource("barcode.png"));

    DropDown planningDropDown = new DropDown();

    planningDropDown.addComponent("Add Space", "addspaceView");
    planningDropDown.addComponent("Add User", "spaceView");
    planningDropDown.addComponent("Download", "addspaceView");

    planning.setContent(planningDropDown);
    planning.setPopupVisible(false);
    this.setButtonSize(planning, 128);

    // Integration
    PopupButton computer = new PopupButton("Integration");
    computer.setIcon(new ThemeResource("computer.png"));
    this.setButtonSize(computer, 128);
    DropDown integrationDropDown = new DropDown();
    // Dataset View
    integrationDropDown.addComponent("View Datasets", "datasetView");
    integrationDropDown.addComponent("Search for users", "searchView");
    // integrationDropDown.addComponent("Change Status", "changePropertiesView");

    computer.setContent(integrationDropDown);
    computer
        .setDescription("To see all dataset of a project or an experemint: Select a project or an experiment in the view below, click this button and select 'View Datasets'.");
    // Experiment
    PopupButton dna = new PopupButton("Experiment");
    dna.setIcon(new ThemeResource("dna.png"));
    this.setButtonSize(dna, 128);


    // Analysis
    PopupButton graph = new PopupButton("Analysis");
    graph.setIcon(new ThemeResource("graph.png"));
    this.setButtonSize(graph, 128);
    DropDownStateObserver graphDropDown = new DropDownStateObserver(graph);
    State state = (State) UI.getCurrent().getSession().getAttribute("state");
    state.addObserver(graphDropDown);
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

  private void setButtonSize(PopupButton button, Integer button_size_px) {
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
