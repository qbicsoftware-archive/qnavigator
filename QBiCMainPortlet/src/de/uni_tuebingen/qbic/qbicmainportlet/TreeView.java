package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TreeView extends Panel implements Observer {

  public Tree tree = new Tree();

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
    vl.setMargin(true);
    tree.setImmediate(true);
    tree.setSizeFull();
    vl.addComponent(tree);
    this.setCaption("Project Browser");
    this.setWidth("250px");
    this.setHeight("800px");
    this.setContent(vl);

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
        state.notifyObservers(message);
      }

    };

    CollapseListener c_listener = new CollapseListener() {

      @Override
      public void nodeCollapse(CollapseEvent event) {
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("collapsed");
        message.add(event.getItemId().toString());
        state.notifyObservers(message);
      }
    };

    ExpandListener e_listener = new ExpandListener() {

      @Override
      public void nodeExpand(ExpandEvent event) {
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("expanded");
        message.add(event.getItemId().toString());
        state.notifyObservers(message);

      }

    };


    tree.addItemClickListener(ic_listener);
    tree.addCollapseListener(c_listener);
    tree.addExpandListener(e_listener);
  }

  // public void buildLayout() {
  // this.setWidth("200px");
  // this.setHeight("800px");
  // tree.setSizeFull();
  // }

  public void setValue(Object itemId) {
    if (itemId == null) {
      return;
    }
    tree.setValue(itemId);
    this.expandNode(itemId);
  }

  public void expandNode(Object itemId) {
    // this.expandItemsRecursively(itemId);
    while (!tree.isRoot(itemId)) {
      tree.expandItem(itemId);
      itemId = tree.getParent(itemId);
    }
    tree.expandItem(itemId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void update(Observable o, Object arg) {

    if (((ArrayList<String>) arg).get(0).equals("expanded")) {
      this.expandNode(((ArrayList<String>) arg).get(1));
    } else if (((ArrayList<String>) arg).get(0).equals("collapsed")) {
      tree.collapseItem(((ArrayList<String>) arg).get(1));
    } else if (((ArrayList<String>) arg).get(0).equals("clicked")) {
      this.setValue(((ArrayList<String>) arg).get(1));
      UI.getCurrent().getNavigator()
          .navigateTo(tree.getItem(tree.getValue()).getItemProperty("type").getValue().toString());
    }
  }

}
