package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.UI;

/**
 * This Class communicates clicks on the tables in the View classes to the state object, which in turn notifys the TreeViews and a change in the Navigator.
 *  e.g. In SpaceView the user clicks on a project in the table --> notify state --> notify TreeView --> TreeView notifies Navigator to navigate to clicked project.
 *  Important: If the viewTable and the type are not set, valueChange will through Errors or behave unexpectedly.
 * @author wojnar
 *
 */
public class ViewTablesClickListener implements Property.ValueChangeListener {
	
	
	public ViewTablesClickListener(FilterTable table, String type){
		this.viewTable = table;
		this.type = type;
	}
	public ViewTablesClickListener(){
		this.viewTable = null;
		this.type = null;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -2654127722351401378L;
	
	/**
	 * 
	 */
	private FilterTable viewTable;
	

	
	/**
	 * returns the table that the listener is listening to.
	 * @return
	 */
	public FilterTable getViewTable() {
		return viewTable;
	}
	/**
	 * sets the table that this listener is listening to.
	 * @param viewTable
	 */
	public void setViewTable(FilterTable viewTable) {
		this.viewTable = viewTable;
	}
	/**
	 * Get the type of View that the table belongs to. e.g. Space, Project, Experiment.
	 * @return
	 */
	public String getType() {
		return type;
	}
	/**
	 * Sets the type of View that the table belongs to. e.g. Space, Project, Experiment.
	 * @return
	 */
	public void setType(String type) {
		this.type = type;
	}

	private String type;
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		// TODO Auto-generated method stub
		Object property  = event.getProperty().getValue();
		if(property == null){
			return;
		}
		String experiment = (String) this.viewTable.getItem(property).getItemProperty(this.type).getValue();
		State state = (State)UI.getCurrent().getSession().getAttribute("state");
		ArrayList<String> message = new ArrayList<String>();
		message.add("clicked");
		message.add(experiment);
		state.notifyObservers(message);		
	}
	/**
	 * checks whether the table and the type are set.
	 * @return
	 */
	public boolean isInitialized(){
		return viewTable == null || type == null; 
	}


}
