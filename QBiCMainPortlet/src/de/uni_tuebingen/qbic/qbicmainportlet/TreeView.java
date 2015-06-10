package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import logging.Log4j2Logger;
import main.OpenBisClient;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
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

  private logging.Logger LOGGER = new Log4j2Logger(TreeView.class);

  private Tree tree = new Tree();
  private Button backButton = new Button("Show all projects");
  private State state;
  private Navigator navigator;
  private OpenBisClient openbisClient;

  public TreeView(State state, Navigator navigator) {
    super();
    this.state = state;
    this.navigator = navigator;
    this.init();
  }

  public TreeView(Container c, State state, Navigator navigator) {
    this(state, navigator);
    tree.setContainerDataSource(c);
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
    //MenuItem pseudoItem = menubar.addItem("", null);
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
        expandNode(event.getItemId().toString());
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

  public void setValue(Object itemId) {
    LOGGER.debug("enterying setValue");
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
      this.expandNode(itemId);
      addFilterBasedOnSelection(itemId);
      tree.setValue(itemId);
    }
  }



  public void expandNode(Object itemId) {
    // this.expandItemsRecursively(itemId);
    addExperiments(itemId);
    int i = 0;
    while (!tree.isRoot(itemId) && i < 20) {
      tree.expandItem(itemId);
      itemId = tree.getParent(itemId);
      i++;
    }
    tree.expandItem(itemId);
  }

  String projectId(String openbisId) {
    String[] split = openbisId.split("/");
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
        /*
         * samplesplit:
         * 
         * space project experiment sample
         */
      case 5:
        return String.format("/%s/%s", split[1], split[2]);
        // what happens with datasets?
      default:
        LOGGER.debug(String.format("datasets of %s?", openbisId));
        return null;
    }
  }

  public void addExperiments(Object itemId) {
    if (itemId instanceof String && tree.getContainerDataSource() instanceof HierarchicalContainer) {
      
      String openbisId = (String) itemId;
      String projectId = projectId(openbisId);
      
      HierarchicalContainer container = (HierarchicalContainer) tree.getContainerDataSource();
      LOGGER.debug(String.format("adding experiments for project %s",projectId));
      List<Experiment> experiments = openbisClient.getExperimentsForProject2(projectId);
      LOGGER.debug(String.format("# of experiments %d",experiments.size()));
      setExperiments(container, projectId, experiments);
    }
  }

  void setExperiments(HierarchicalContainer container, String openbisId,
      List<Experiment> experiments) {
    for (Experiment experiment : experiments) {

      String experimentIdentifier = experiment.getIdentifier();
      String experimentCode = experiment.getCode();
      LOGGER.debug(String.format("add experiment %s if it does not exist", experimentIdentifier));
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
  
  private void addFilterBasedOnSelection(Object itemId) {
    LOGGER.debug((String)itemId);
    if ("project".equals(this.getItemType(itemId)) && !(itemId.equals(tree.getValue()))) {
      this.backButton.setVisible(true);
      SimpleStringFilter filter = new SimpleStringFilter("project", (String) itemId, true, false);
      // Add the new filter
      ((HierarchicalContainer) tree.getContainerDataSource()).addContainerFilter(filter);
    }
    else if ("experiment".equals(this.getItemType(itemId)) && !(itemId.equals(tree.getValue()))) {
      this.backButton.setVisible(true);
      String projectId = projectId((String) itemId);
      LOGGER.debug("project is " + projectId);
      SimpleStringFilter filter = new SimpleStringFilter("project", projectId, true, false);
      // Add the new filter
      ((HierarchicalContainer) tree.getContainerDataSource()).addContainerFilter(filter);
    }
    
  }

  private String getItemType(Object itemId) {
    Item item = tree.getItem(itemId);
    LOGGER.debug(String.format("Item with itemId %s exists %s", (String)itemId, String.valueOf(item != null)));
    return item == null?null: item.getItemProperty("type").getValue().toString();
  }

  private String getItemIdentifier(Object itemId) {
    return tree.getItem(itemId).getItemProperty("identifier").getValue().toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void update(Observable o, Object arg) {
    if (((ArrayList<String>) arg).get(0).equals("clicked")) {
      this.setValue(((ArrayList<String>) arg).get(1));
    }
  }

  @Override
  public boolean beforeViewChange(ViewChangeEvent event) {
    String param = event.getParameters();
    try {
      this.setValue(param);
    } catch (NullPointerException e) {
      // nothing to do here. It just means that treeView does not need any update
    }

    return true;
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

}
