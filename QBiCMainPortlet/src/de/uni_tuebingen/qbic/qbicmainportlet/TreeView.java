package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
@SuppressWarnings("serial")
public class TreeView extends Panel implements Observer, ViewChangeListener {

  public Tree tree = new Tree();
  public Button backButton = new Button("Show all projects");

  public TreeView() {
    super();
    this.init();
  }

  public TreeView(Container c) {
    super();
    this.init();
    tree.setContainerDataSource(c);
  }

  private void init() {
    VerticalLayout vl = new VerticalLayout();
    vl.setMargin(false);
    vl.setSpacing(true);
    
    MenuBar menubar = new MenuBar();
    MenuItem pseudoItem = menubar.addItem("", null);
    pseudoItem.setIcon(new ThemeResource("qbic_logo.png"));
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
        removeFilter();
        tree.collapseItem(tree.getValue());
        tree.unselect(tree.getValue());
        UI.getCurrent().getNavigator().navigateTo("");
      }
      
    });
    //this.setCaption("Project Browser");
    
    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();
    this.setWidth(String.format("%spx", (browserWidth * 0.12)));
    this.setHeight(String.format("%spx", (browserHeight * 0.8)));
    
    // this.setWidth("250px");
    // this.setHeight("800px");
    this.addStyleName(ValoTheme.PANEL_BORDERLESS);
    this.setContent(vl);
    //this.addComponent(vl);
    //this.setMargin(false);
    
    this.registerClickListener();

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
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
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
        //State state = (State) UI.getCurrent().getSession().getAttribute("state");
        /*ArrayList<String> message = new ArrayList<String>();
        message.add("collapsed");
        message.add(event.getItemId().toString());
        state.notifyObservers(message);
        */
      }
    };

    ExpandListener e_listener = new ExpandListener() {

      @Override
      public void nodeExpand(ExpandEvent event) {
        //State state = (State) UI.getCurrent().getSession().getAttribute("state");
        expandNode(event.getItemId().toString());
        /*ArrayList<String> message = new ArrayList<String>();
        message.add("expanded");
        message.add(event.getItemId().toString());
        state.notifyObservers(message);
         */
      }

    };


    tree.addItemClickListener(ic_listener);
    tree.addCollapseListener(c_listener);
    tree.addExpandListener(e_listener);
  }

  public void setValue(Object itemId) {
    if (itemId == null || tree.getItem(itemId) == null) {
      return;
    }
    filterBasedOnSelection(itemId);
    tree.setValue(itemId);
    this.expandNode(itemId);
  }

  public void removeFilter() {
    ((HierarchicalContainer) tree.getContainerDataSource()).removeAllContainerFilters();
  }

  public void expandNode(Object itemId) {
    // this.expandItemsRecursively(itemId);
    int i = 0;
    while (!tree.isRoot(itemId) && i < 20) {
      tree.expandItem(itemId);
      itemId = tree.getParent(itemId);
      i++;
    }
    tree.expandItem(itemId);
  }

  private void filterBasedOnSelection(Object itemId) {
    if (this.getItemType(itemId).equals("project") && !(itemId.equals(tree.getValue()))) {

      SimpleStringFilter filter = new SimpleStringFilter("project", (String)itemId,true,false);
      // Add the new filter
      ((HierarchicalContainer) tree.getContainerDataSource()).addContainerFilter(filter);
    }
  }

  private String getItemType(Object itemId) {
    return tree.getItem(itemId).getItemProperty("type").getValue().toString();
  }
  private String getItemIdentifier(Object itemId){
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
    try{
      this.setValue(param);
    }catch(NullPointerException e){
      //nothing to do here. It just means that treeView does not need any update
    }
    
    return true;
  }

  @Override
  public void afterViewChange(ViewChangeEvent event) {
  //nothing to do here
  }

}
