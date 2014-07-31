package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;


public class TreeView extends Tree implements Observer {

	public TreeView() {
		super();
		this.setImmediate(true);
		this.registerClickListener();
	}
	
	public TreeView(Container c){
		super();
		this.setImmediate(true);
		this.registerClickListener();
		this.setContainerDataSource(c);
	}
	
	public Tree getTree() {
		return this;
	}

	public void registerClickListener() {
		ItemClickListener ic_listener = new ItemClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -607183115859033829L;

			@Override
			public void itemClick(ItemClickEvent event) {
				State state = (State)UI.getCurrent().getSession().getAttribute("state");
				ArrayList<String> message = new ArrayList<String>();
				message.add("expanded");
				message.add(event.getItemId().toString());
				state.notifyObservers(message);
			}

		/*	public void valueChange(ValueChangeEvent event) {
					State state = (State)UI.getCurrent().getSession().getAttribute("state");
					ArrayList<String> message = new ArrayList<String>();
					message.add("expanded");
					message.add(event.getItemId().toString());
					state.notifyObservers(message);
										
					//notifyObservers(qbic_navtree.getContainerProperty(event.getItemId(), "metadata").getValue());

					//Collection<?> child_ids = qbic_navtree.getChildren(event.getItemId());
					
					// item ids are not enough; reference actual items
					/*Vector<Item> child_nodes = new Vector<Item>();
					if (child_ids != null) {
						for (Object c : child_ids) {
							// System.out.println("child " + c.toString());
							child_nodes.add(qbic_navtree.getItem(c));
						}
						setChanged();	
					}

					notifyObservers(child_nodes);
			}*/
			



		};
		
		CollapseListener c_listener = new CollapseListener() {

			@Override
			public void nodeCollapse(CollapseEvent event) {
				State state = (State)UI.getCurrent().getSession().getAttribute("state");
				ArrayList<String> message = new ArrayList<String>();
				message.add("collapsed");
				message.add(event.getItemId().toString());
				state.notifyObservers(message);
			}
		};
		
		ExpandListener e_listener = new ExpandListener() {

			@Override
			public void nodeExpand(ExpandEvent event) {
				State state = (State)UI.getCurrent().getSession().getAttribute("state");
				ArrayList<String> message = new ArrayList<String>();
				message.add("expanded");
				message.add(event.getItemId().toString());
				state.notifyObservers(message);
				
			}
			
		};
		

		this.addItemClickListener(ic_listener);
		this.addCollapseListener(c_listener);
		this.addExpandListener(e_listener);
	}
	
	public void buildLayout() {
		this.setSizeFull();
	}
	
	public void setValue(Object itemId) {
		if(itemId == null) {
			return;
		}
		super.setValue(itemId);
		//this.expandItemsRecursively(itemId);
		while(!this.isRoot(itemId)){
			this.expandItem(itemId);
			itemId = this.getParent(itemId);
		}
		this.expandItem(itemId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {

		if(((ArrayList<String>) arg).get(0).equals("expanded")) {
			this.setValue(((ArrayList<String>) arg).get(1));
			UI.getCurrent().getNavigator().navigateTo(this.getItem(this.getValue()).getItemProperty("type").getValue().toString());
		}
		else if(((ArrayList<String>) arg).get(0).equals("collapsed")) {
			this.collapseItem(((ArrayList<String>) arg).get(1));
		}
	}
	
}
