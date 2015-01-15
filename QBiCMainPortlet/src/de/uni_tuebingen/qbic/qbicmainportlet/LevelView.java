package de.uni_tuebingen.qbic.qbicmainportlet;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
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
  // TextArea statusMonitor = new TextArea("Status monitor",
  // "Here we could put some job status or general information.");
  Component mainComponent;
  String navigateToLabel;

  public LevelView() {
  }
  
  public LevelView(Component component){
    this(component,"");
  }
  
  
  public LevelView( Component component, String navigateToLabel) {
    this.navigateToLabel = navigateToLabel;
    this.mainComponent = component;
    this.buildLayout();
  }

  public void buildLayout() {
    // clean up
    this.removeAllComponents();

    // set maximum width according to browser info
    // this.compositeLayout.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth(),
    // Unit.PIXELS);
    // header layout first

    this.setWidth("100%");

    // mainLayout
    VerticalLayout mainLayout = new VerticalLayout();
    mainLayout.addComponent(this.mainComponent);
    mainLayout.setMargin(true);
    this.addComponent(mainLayout);



    // this.treeComponentLayout.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
    // this.treeComponentLayout.setExpandRatio(this.treeView, 0.15f);
    // this.treeComponentLayout.setExpandRatio(this.mainComponent, 0.75f);
    // this.mainComponent.setSizeFull();
    // this.mainComponent.setHeight("800px");
    // this.mainComponent.setWidth("800px");
    // this.mainComponent.setSizeUndefined();
    // this.mainComponent.setWidth("90%");
    // this.mainComponent.setHeight("100%");

    // this.treeComponentLayout.setSizeFull();
    this.setStyleName(Reindeer.SPLITPANEL_SMALL);
    this.setMargin(true);

    // this.setMargin(true);
    // this.setSpacing(true);
    // //this.setSizeFull();

    // this.headerLayout.addComponent(this.statusMonitor);

    // this.headerLayout.setMargin(true);
    // this.headerLayout.setExpandRatio(this.statusMonitor, 0.2f);
    // this.headerLayout.setExpandRatio(this.toolbar, 0.8f);
    // this.statusMonitor.setSizeFull();
    // this.toolbar.setSizeFull();
    // this.headerLayout.setSizeFull();

    // Label space1 = new
    // Label("<div style=\"font-size:xx-small; border-color:blue;border-style:dotted hidden dashed hidden;\">&nbsp;</div>",
    // Label.CONTENT_XHTML);
    // space1.setHeight("1em");
    // Label space2 = new Label("<hr width=\"100%\">", Label.CONTENT_XHTML);
    // this.addComponent(space1);
    // this.addComponent(space2);

    // this.setExpandRatio(this.headerLayout, 1);
    // this.setExpandRatio(this.treeComponentLayout, 3);
  }

  @Override
  public void enter(ViewChangeEvent event) {

    String currentValue = event.getParameters();
    System.out.println("currentValue: " + currentValue);
    System.out.println("navigateToLabel: " + navigateToLabel);
    // System.out.println("type: " +
    // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue());
    // System.out.println("ID: " +
    // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("identifier").getValue());
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

    if (this.mainComponent instanceof DatasetView) {
      DatasetView ds = (DatasetView) this.mainComponent;
      try {
        ds.setContainerDataSource(dh.getDatasets((String) currentValue, navigateToLabel));
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is DatasetView");
         e.printStackTrace();
      }
      // ds.setInfo(name, type);

    } /*else if (this.mainComponent instanceof SampleView) {
      SampleView sv = (SampleView) this.mainComponent;
      try {
        sv.setContainerDataSource(dh.getSampleInformation(currentValue), currentValue);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is SampleView");
        // e.printStackTrace();
      }
    } */else if (this.mainComponent instanceof SpaceView) {
      SpaceView sv = (SpaceView) this.mainComponent;
      try {

        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        sv.setContainerDataSource(dh.getSpace(currentValue), currentValue);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is SpaceView");
        // e.printStackTrace();
      }
    } /*else if (this.mainComponent instanceof ProjectView) {
      ProjectView pv = (ProjectView) this.mainComponent;
      try {

        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();

        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        Project project = dh.openBisClient.getProjectByCode(currentValue);
        String projectIdentifier = project.getIdentifier();

        pv.setContainerDataSource(dh.getProjectInformation(projectIdentifier), currentValue);
      } catch (Exception e) {
        System.out.println("Exception in LevelView.enter. mainComponent is ProjectView");
        // e.printStackTrace();
      }
    } *//*else if (this.mainComponent instanceof ExperimentView) {
      ExperimentView ev = (ExperimentView) this.mainComponent;
      try {
        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        ev.setContainerDataSource(dh.getExperimentInformation(currentValue), currentValue);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }*//* else if (this.mainComponent instanceof ChangePropertiesView) {
      ChangePropertiesView cpv = (ChangePropertiesView) this.mainComponent;
      try {
        // String type =
        // this.treeView.getContainerDataSource().getItem(currentValue).getItemProperty("type").getValue().toString();
        cpv.setContainerDataSource(dh.getExperimentInformation(currentValue), currentValue);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }*/
    // UI.getCurrent().scrollIntoView(mainComponent);
  }
}
