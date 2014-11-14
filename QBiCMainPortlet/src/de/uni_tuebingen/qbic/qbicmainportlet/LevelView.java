package de.uni_tuebingen.qbic.qbicmainportlet;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Implements the vaadin View interface. LevelViews show their main component in the same manner,
 * whether it is a button a table or any other vaadin component.
 * 
 *
 */
public class LevelView extends VerticalLayout implements View {
  /**
	 * 
	 */
  private static final long serialVersionUID = 4753771416181038820L;

  // Label statusMonitor = new Label("Here we could put some job status or general information.");
  TextArea statusMonitor = new TextArea("Status monitor",
      "Here we could put some job status or general information.");
  ToolBar toolbar;
  TreeView treeView;
  Component mainComponent;
  HorizontalLayout headerLayout = new HorizontalLayout();
  HorizontalLayout treeComponentLayout = new HorizontalLayout();
  String currentValue;

  public LevelView() {
    currentValue = "_____empyt_____";
  }

  public LevelView(ToolBar toolbar, TreeView treeView, Component component) {
    this.toolbar = toolbar;
    this.treeView = treeView;
    this.mainComponent = component;
    currentValue = "_____empyt_____";
    this.buildLayout();
  }

  public void buildLayout() {

    this.treeComponentLayout.removeAllComponents();
    this.removeAllComponents();
    this.treeComponentLayout.addComponent(this.treeView);
    this.treeComponentLayout.addComponent(this.mainComponent);
    //this.treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
    this.treeComponentLayout.setExpandRatio(this.treeView, 0.15f);
    this.treeComponentLayout.setExpandRatio(this.mainComponent, 0.75f);
    // this.mainComponent.setSizeFull();
    this.mainComponent.setHeight("800px");

    //this.treeComponentLayout.setSizeFull();
    this.treeComponentLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
    this.treeComponentLayout.setMargin(true);
    this.setMargin(true);
    this.setSpacing(true);
    //this.setSizeFull();

    // this.headerLayout.addComponent(this.statusMonitor);
    this.headerLayout.addComponent(this.toolbar);
    this.headerLayout.setMargin(true);
    // this.headerLayout.setExpandRatio(this.statusMonitor, 0.2f);
    this.headerLayout.setExpandRatio(this.toolbar, 0.8f);
    //this.statusMonitor.setSizeFull();
    this.toolbar.setSizeFull();
    this.headerLayout.setSizeFull();
    this.addComponent(this.headerLayout);
    // Label space1 = new
    // Label("<div style=\"font-size:xx-small; border-color:blue;border-style:dotted hidden dashed hidden;\">&nbsp;</div>",
    // Label.CONTENT_XHTML);
    // space1.setHeight("1em");
    Label space2 = new Label("<hr width=\"100%\">", Label.CONTENT_XHTML);
    // this.addComponent(space1);
    this.addComponent(space2);
    this.addComponent(this.treeComponentLayout);

    this.setExpandRatio(this.headerLayout, 1);
    this.setExpandRatio(this.treeComponentLayout, 3);
  }

  @Override
  public void enter(ViewChangeEvent event) {
    Object currentValue = this.treeView.tree.getValue();

    if (currentValue == null || currentValue.equals(this.currentValue)) {
      return;
    }

    // System.out.println(currentValue);
    // System.out.println("type: " +
    // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue());
    // System.out.println("ID: " +
    // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("identifier").getValue());
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

    if (this.mainComponent instanceof DatasetView) {
      String name =
          this.treeView.tree.getContainerDataSource().getItem(currentValue)
              .getItemProperty("identifier").getValue().toString();
      String type =
          this.treeView.tree.getContainerDataSource().getItem(currentValue).getItemProperty("type")
              .getValue().toString();
      // System.out.println("Name: " +name + " type " + type + " value " + this.treeView.getValue()
      // );
      DatasetView ds = (DatasetView) this.mainComponent;
      try {
        ds.setContainerDataSource(dh.getDatasets((String) currentValue, type));
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is DatasetView");
        // e.printStackTrace();
      }
      ds.setInfo(name, type);

    } else if (this.mainComponent instanceof SampleView) {
      SampleView sv = (SampleView) this.mainComponent;
      try {
        String type =
            this.treeView.tree.getContainerDataSource().getItem(currentValue)
                .getItemProperty("type").getValue().toString();
        String name =
            this.treeView.tree.getContainerDataSource().getItem(currentValue)
                .getItemProperty("identifier").getValue().toString();
        sv.setContainerDataSource(dh.getSampleInformation(name), name);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is SampleView");
        // e.printStackTrace();
      }
    } else if (this.mainComponent instanceof SpaceView) {
      SpaceView sv = (SpaceView) this.mainComponent;
      try {

        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        String name =
            this.treeView.tree.getContainerDataSource().getItem(currentValue)
                .getItemProperty("identifier").getValue().toString();
        sv.setContainerDataSource(dh.getSpace(name), name);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is SpaceView");
        // e.printStackTrace();
      }
    } else if (this.mainComponent instanceof ProjectView) {
      ProjectView pv = (ProjectView) this.mainComponent;
      try {

        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        String name =
            this.treeView.tree.getContainerDataSource().getItem(currentValue)
                .getItemProperty("identifier").getValue().toString();
        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        Project project = dh.openBisClient.getProjectByCode(name);
        String projectIdentifier = project.getIdentifier();
        pv.setContainerDataSource(dh.getProjectInformation(projectIdentifier), name);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is ProjectView");
        // e.printStackTrace();
      }
    } else if (this.mainComponent instanceof ExperimentView) {
      ExperimentView ev = (ExperimentView) this.mainComponent;
      try {
        String name =
            this.treeView.tree.getContainerDataSource().getItem(currentValue)
                .getItemProperty("identifier").getValue().toString();
        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        ev.setContainerDataSource(dh.getExperimentInformation(name), name);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if(this.mainComponent instanceof ChangePropertiesView){
	        ChangePropertiesView cpv = (ChangePropertiesView) this.mainComponent;
            try{
              String name = this.treeView.tree.getContainerDataSource().getItem(currentValue).getItemProperty("identifier").getValue().toString();
              //String type = this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
              cpv.setContainerDataSource(dh.getExperimentInformation(name), name);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
		//UI.getCurrent().scrollIntoView(mainComponent);
	}
}
