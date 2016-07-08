/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import views.WorkflowView;
import logging.Log4j2Logger;
import main.OpenBisClient;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TreeView extends Panel implements Observer, ViewChangeListener {

  enum containerProperty {identifier, type, project, caption};
  private logging.Logger LOGGER = new Log4j2Logger(TreeView.class);
  private final String backButtonCaption = "Show all projects";
  private Tree tree = new Tree();
  private Button backButton = new Button(backButtonCaption);
  private State state;
  private Navigator navigator;
  private OpenBisClient openbisClient;

  private String user = "";

  public TreeView(State state, Navigator navigator, String user) {
    super();
    this.user = user;
    this.state = state;
    this.navigator = navigator;
    this.init();
    this.initTreeContainer();
  }

  public void setOpenbisClient(OpenBisClient openbisClient) {
    this.openbisClient = openbisClient;
  }

  public void setContainerDatasource(Container c) {
    tree.setContainerDataSource(c);
  }

  private void init() {
    VerticalLayout vl = new VerticalLayout();
    vl.setMargin(false);
    vl.setSpacing(true);

    MenuBar menubar = new MenuBar();
    // MenuItem pseudoItem = menubar.addItem("", null);
    // pseudoItem.setIcon(new ThemeResource("qbic_logo.png"));
    menubar.setWidth("100%");
    menubar.addStyleName("user-menu");
    vl.addComponent(menubar);

    tree.setImmediate(true);

    // test to not show code of shown experiments but experiment type (+ name)
    tree.setItemCaptionPropertyId("caption");
    tree.setItemCaptionMode(ItemCaptionMode.PROPERTY);

    vl.addComponent(tree);
    tree.setHeight("100%");

    vl.addComponent(backButton);
    backButton.setWidth("100%");
    backButton.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        unselect();
        navigator.navigateTo("");
      }

    });
    // this.setCaption("Project Browser");


    // this.setWidth("250px");
    // this.setHeight("800px");
    this.addStyleName(ValoTheme.PANEL_BORDERLESS);
    this.setContent(vl);
    // this.addComponent(vl);
    // this.setMargin(false);

    this.registerClickListener();

  }

  public void rebuildLayout(int height, int width, WebBrowser browser) {
    this.setWidth((width * 0.12f), Unit.PIXELS);
    this.setHeight((height * 0.8f), Unit.PIXELS);
  }

  void unselect() {
    removeAllFiltersBasedOnSelection();
    if (tree.getValue() != null) {
      tree.collapseItem(tree.getValue());
      tree.unselect(tree.getValue());
    }
  }

  public Tree getTree() {
    return tree;
  }

  public void registerClickListener() {
    ItemClickListener ic_listener = new ItemClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -607183115859033829L;

      @Override
      public void itemClick(ItemClickEvent event) {
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(event.getItemId().toString());
        message.add(getItemType(event.getItemId().toString()));
        state.notifyObservers(message);
      }

    };

    CollapseListener c_listener = new CollapseListener() {

      @Override
      public void nodeCollapse(CollapseEvent event) {
        tree.collapseItem(event.getItemId().toString());
        // State state = (State) UI.getCurrent().getSession().getAttribute("state");
        /*
         * ArrayList<String> message = new ArrayList<String>(); message.add("collapsed");
         * message.add(event.getItemId().toString()); state.notifyObservers(message);
         */
      }
    };

    ExpandListener e_listener = new ExpandListener() {

      @Override
      public void nodeExpand(ExpandEvent event) {
        // State state = (State) UI.getCurrent().getSession().getAttribute("state");
        String type = (String)getContainerDataSource().getItem(event.getItemId()).getItemProperty("type").getValue();
        String projectId = projectId(type, event.getItemId().toString());
        expandNode(type,event.getItemId().toString(),projectId);
        /*
         * ArrayList<String> message = new ArrayList<String>(); message.add("expanded");
         * message.add(event.getItemId().toString()); state.notifyObservers(message);
         */
      }

    };


    tree.addItemClickListener(ic_listener);
    tree.addCollapseListener(c_listener);
    tree.addExpandListener(e_listener);
  }

  void setValue(String type, Object itemId) {
    // can not do anything with a non existant item
    if (itemId == null) {
      return;
    }
    if (openbisClient == null) {
      LOGGER.warn("openbis client is not set. Can not retrieve additional information for "
          + itemId.toString());
      return;
    }
    if (itemId == "") {
      unselect();
    } else {
      LOGGER.debug("itemId: "+ (String)itemId + " type: " + type);
      removeAllFiltersBasedOnSelection();
      String projectId = projectId(type, (String)itemId);
      this.expandNode(type,itemId, projectId);
      setFilterBasedOnSelection(type,projectId);
      tree.setValue(itemId);
    }
  }



  public void expandNode(String type, Object itemId, String projectId) {
    // this.expandItemsRecursively(itemId);
    addExperiments(type,projectId);
    Object id = itemId;
    if(type == SampleView.navigateToLabel){
      id = openbisClient.getSampleByIdentifier((String)itemId).getExperimentIdentifierOrNull();
      if(id == null) id = itemId;
    }
    int i = 0;
    while (!tree.isRoot(id) && i < 20) {
      tree.expandItem(id);
      id = tree.getParent(id);
      i++;
    }
    tree.expandItem(id);
  }

  /**
   * this fall back method is used, when projectId can not be retrieved in the usual way
   * @param openbisId
   * @return
   */
  String projectIdFallback(String openbisId,String[] split) {
    switch (split.length) {
    /*
     * projectsplit:
     * 
     * space project
     */
      case 3:
        return openbisId;
        /*
         * experimentsplit:
         * 
         * space project experiment
         */
        
      case 4:
        return String.format("/%s/%s", split[1], split[2]);
        /*
         * samplesplit:
         * 
         * space sample
         */
      case 5:
        //TODO adapt. that does not work correctly
        return String.format("/%s/%s", split[1], split[2]);
        
        // what happens with datasets?
      default:
        LOGGER.debug(String.format("datasets of %s?", openbisId));
        return null;
    }
  }
  
  public String projectId(String type, String openbisId){
    String[] split = openbisId.split("/");
    
    if(split.length == 0) return null;
    switch (type) {
      case PatientView.navigateToLabel:
      case ProjectView.navigateToLabel:
      case BarcodeView.navigateToLabel:
        return openbisId;
      case ExperimentView.navigateToLabel:
        return projectIdFallback(openbisId, split);
      case SampleView.navigateToLabel:
        String expId = openbisClient.getSampleByIdentifier(String.format("%s/%s", split[1],split[2])).getExperimentIdentifierOrNull();
        if(expId == null)
          return null;
        return projectId(ExperimentView.navigateToLabel,expId);
      case DatasetView.navigateToLabel:
      case WorkflowView.navigateToLabel:
        Map<String, String> map = DatasetView.getMap(openbisId);
        if(map == null) return null;
        return projectId(map.get("type"),map.get("id"));
    default:
      LOGGER.debug(String.format("Problem with id %s", openbisId));
      return null;     
    }
  }
  
  
  public void addExperiments(String type, String projectId) {
    if (tree.getContainerDataSource() instanceof HierarchicalContainer) {

      List<Experiment> experiments = openbisClient.getExperimentsForProject2(projectId);
      setExperiments((HierarchicalContainer)tree.getContainerDataSource(), projectId, experiments);
    }
  }
  /**
   * helper function for addExperiments
   * @param container
   * @param openbisId
   * @param experiments
   */
  void setExperiments(HierarchicalContainer container, String openbisId,
      List<Experiment> experiments) {
    for (Experiment experiment : experiments) {

      String experimentIdentifier = experiment.getIdentifier();
      String experimentCode = experiment.getCode();
      // why bother?
      if (container.containsId(experimentIdentifier))
        continue;
      container.addItem(experimentIdentifier);
      container.setParent(experimentIdentifier, openbisId);
      container.getContainerProperty(experimentIdentifier, "type").setValue("experiment");
      container.getContainerProperty(experimentIdentifier, "identifier").setValue(experimentCode);
      container.getContainerProperty(experimentIdentifier, "project").setValue(openbisId);
      container.getContainerProperty(experimentIdentifier, "caption")
          .setValue(
              String.format("%s (%s)",
                  openbisClient.openBIScodeToString(experiment.getExperimentTypeCode()),
                  experimentCode));
      container.setChildrenAllowed(experimentCode, false);
      this.markAsDirty();
    }
  }

  public void removeAllFiltersBasedOnSelection() {
    ((HierarchicalContainer) tree.getContainerDataSource()).removeAllContainerFilters();
    this.backButton.setVisible(false);
  }

  private void setFilterBasedOnSelection(String type, String projectId) {
      this.backButton.setVisible(true);
      SimpleStringFilter filter = new SimpleStringFilter("project", projectId, true, false);
      // Add the new filter
      ((HierarchicalContainer) tree.getContainerDataSource()).addContainerFilter(filter);
  }

  private String getItemType(Object itemId) {
    Item item = tree.getItem(itemId);
    return item == null ? null : item.getItemProperty("type").getValue().toString();
  }

  private String getItemIdentifier(Object itemId) {
    return tree.getItem(itemId).getItemProperty("identifier").getValue().toString();
  }

  /**
   * Observes what State is saying. Here it just looks whether any kind of 'click' in e.g. ProjectView has happened 
   */
  @SuppressWarnings("unchecked")
  @Override
  public void update(Observable o, Object arg) {
    if (((ArrayList<String>) arg).get(0).equals("clicked")) {
      this.setValue(((ArrayList<String>) arg).get(2),((ArrayList<String>) arg).get(1));
    }
  }

  @Override
  public boolean beforeViewChange(ViewChangeEvent event) {
    String param = event.getParameters();
    try {
      if (param == "") {
        try {
          loadProjects();
          backButton.setVisible(false);
        } catch (Exception e) {
          LOGGER.error(String.format("failed to load projects for user %s", user), e);
          backButton.setVisible(false);
        }

      } else {
        LOGGER.debug(param);
        String type = getType(event.getNewView());
        this.setValue(type,param);
      }
    } catch (NullPointerException e) {
      // nothing to do here. It just means that treeView does not need any update
    }

    return true;
  }

  String getType(View newView) {
    if(newView instanceof ProjectView){
      return ProjectView.navigateToLabel;
    }else if(newView instanceof PatientView){
      return PatientView.navigateToLabel;
    }
else if(newView instanceof ExperimentView){
  return ExperimentView.navigateToLabel;
    }
else if(newView instanceof SampleView){
  return SampleView.navigateToLabel;
}
else if(newView instanceof DatasetView){
  return DatasetView.navigateToLabel;
}
else if(newView instanceof BarcodeView){
  return BarcodeView.navigateToLabel;
}
else{
  return "";
}
  }

  @Override
  public void afterViewChange(ViewChangeEvent event) {
    // nothing to do here
  }

  /**
   * Enables or disables the component. The user can not interact disabled components, which are
   * shown with a style that indicates the status, usually shaded in light gray color. Components
   * are enabled by default.
   */
  public void setEnabled(boolean enabled) {
    this.tree.setEnabled(enabled);
    this.backButton.setEnabled(enabled);
  }

  /**
   * retrieve projects from openbis and 
   */
  public void loadProjects() {
    // Initialization of Tree Container
    HierarchicalContainer tc = initTreeContainer();
    List<Project> projects =
        openbisClient.getOpenbisInfoService().listProjectsOnBehalfOfUser(
            openbisClient.getSessionToken(), user);
    for (Project project : projects) {

      String projectIdentifier = project.getIdentifier();
      String projectCode = project.getCode();

      tc.addItem(projectIdentifier);
      tc.getContainerProperty(projectIdentifier, "type").setValue("project");
      tc.getContainerProperty(projectIdentifier, "identifier").setValue(projectIdentifier);
      tc.getContainerProperty(projectIdentifier, "project").setValue(projectIdentifier);
      tc.getContainerProperty(projectIdentifier, "caption").setValue(projectCode);
    }

  }

  HierarchicalContainer getContainerDataSource() {
    return (HierarchicalContainer) tree.getContainerDataSource();
  }
  
  HierarchicalContainer initTreeContainer(){
    HierarchicalContainer tc = new HierarchicalContainer();
    
    tc.addContainerProperty("identifier", String.class, "N/A");
    tc.addContainerProperty("type", String.class, "N/A");
    tc.addContainerProperty("project", String.class, "N/A");
    tc.addContainerProperty("caption", String.class, "N/A");
    tree.setContainerDataSource(tc);
    return getContainerDataSource();
  }

}
