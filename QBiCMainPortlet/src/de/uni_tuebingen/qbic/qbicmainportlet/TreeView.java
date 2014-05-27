package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.Collection;
import java.util.Observable;
import java.util.Vector;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Tree;


public class TreeView extends Observable {
	private Tree qbic_navtree = null;

	public TreeView() {
		super();
		this.qbic_navtree = new Tree("Default title");
	}

	public TreeView(Tree init_tree) {
		super();
		this.qbic_navtree = init_tree;
	}

	public void registerClickListener() {
		ItemClickListener ic_listener = new ItemClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -607183115859033829L;

			@Override
			public void itemClick(ItemClickEvent event) {
				if(event.isDoubleClick()){
					qbic_navtree.expandItem(event.getItemId());
					
					//notifyObservers(qbic_navtree.getContainerProperty(event.getItemId(), "metadata").getValue());

					Collection<?> child_ids = qbic_navtree.getChildren(event.getItemId());
					
					// item ids are not enough; reference actual items
					Vector<Item> child_nodes = new Vector<Item>();
					if (child_ids != null) {
						for (Object c : child_ids) {
							// System.out.println("child " + c.toString());
							child_nodes.add(qbic_navtree.getItem(c));
						}
						setChanged();	
					}

					notifyObservers(child_nodes);
				}

			}
		};

		qbic_navtree.addItemClickListener(ic_listener);
	}

	public TreeView(Container c){
		this.qbic_navtree.setContainerDataSource(c);
	}
}
