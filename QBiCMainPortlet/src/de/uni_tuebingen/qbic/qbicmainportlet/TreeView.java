package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class TreeView extends Tree implements Observer {

	public TreeView() {
		super();
		this.init();
		
	}
	private void init(){
		this.setImmediate(true);
		this.registerClickListener();
		this.setCaption("Projects");
	}
	
	public TreeView(Container c){
		super();
		this.init();
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
				message.add("clicked");
				message.add(event.getItemId().toString());
				state.notifyObservers(message);
			}

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
		this.expandNode(itemId);
	}
	
	public void expandNode(Object itemId){
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
			this.expandNode(((ArrayList<String>) arg).get(1));
		}
		else if(((ArrayList<String>) arg).get(0).equals("collapsed")) {
			this.collapseItem(((ArrayList<String>) arg).get(1));
		}
		else if(((ArrayList<String>) arg).get(0).equals("clicked")) {
			this.setValue(((ArrayList<String>) arg).get(1));
			UI.getCurrent().getNavigator().navigateTo(this.getItem(this.getValue()).getItemProperty("type").getValue().toString());
		}
	}
	
}
