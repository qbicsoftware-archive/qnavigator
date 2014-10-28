package de.uni_tuebingen.qbic.qbicmainportlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class DatasetView extends VerticalLayout {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8672873911284888801L;

	private Label general_information;
	private final FilterTable table;
	private IndexedContainer datasets;
	private ButtonLink download;
	private final String DOWNLOAD_BUTTON_CAPTION = "Download";
	private final String VISUALIZE_BUTTON_CAPTION = "Visualize";
	
	private final String[] FILTER_TABLE_COLUMNS = new String[] { "Project", "Sample", "Sample Type",
            "File Name", "File Type", "Dataset Type", "Registration Date" , "Validated", "File Size"};
	
	public DatasetView() {
		this.table = buildFilterTable();
		this.buildLayout();
	}
	
	public DatasetView(IndexedContainer dataset){
		this.datasets = dataset;
		this.table = buildFilterTable();
		this.buildLayout();
		this.setContainerDataSource(this.datasets);
		
	}
	
	
	public void setContainerDataSource(IndexedContainer newDataSource){
		this.datasets = (IndexedContainer) newDataSource;
		this.table.setContainerDataSource(this.datasets);
		
		this.table.setColumnCollapsed("state", true);

		this.table.setVisibleColumns((Object[]) FILTER_TABLE_COLUMNS);
		
		this.setSizeFull();
		this.table.setSizeFull();
	}
	
	public void setInfo(String name, String entity) {		
		this.general_information.setValue(String.format("Name: %s\nEntity Type: %s\n",name));
	}
	/**
	 * Precondition: {DatasetView#table} has to be initialized. e.g. with {DatasetView#buildFilterTable} If it is not, strange behaviour has to be expected.
	 * builds the Layout of this view. 
	 */
	private void buildLayout(){
		//Layout
		this.setSizeFull();
		this.setVisible(true);
		HorizontalLayout buttonLayout = new HorizontalLayout();
	    buttonLayout.setHeight(null);
	    buttonLayout.setWidth("100%");
	    buttonLayout.setSpacing(false);
	    
		this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));
		//this.download.setStyleName(Reindeer.BUTTON_SMALL);
		final Button visualize = new Button(VISUALIZE_BUTTON_CAPTION);
		visualize.setEnabled(false);
		buttonLayout.addComponent(this.download);
		buttonLayout.addComponent(visualize);
		
		
		
		
		
		MpPortletListener mppl  = new MpPortletListener(this.download, this.table);
		this.table.addValueChangeListener(mppl);
		if (VaadinSession.getCurrent() instanceof VaadinPortletSession) {
			VaadinPortletSession portletsession =
					(VaadinPortletSession) VaadinSession.getCurrent();
			// Add a custom listener to handle action and
			// render requests.
			portletsession.addPortletListener(mppl);
		}
		
		this.table.addValueChangeListener(new ValueChangeListener(){
			@Override
			public void valueChange(ValueChangeEvent event) {
				//Nothing selected or more than one selected.
				Set<Object> selectedValues = (Set<Object>)event.getProperty().getValue();
				if(selectedValues == null || selectedValues.size() == 0 || selectedValues.size() > 1){
					visualize.setEnabled(false);
					return;
				}
				//if one selected check whether its dataset type is either fastqc or qcml.
				//For now we only visulize these two file types.
				Iterator<Object> iterator = selectedValues.iterator();
				Object next = iterator.next();
				String datasetType = (String)table.getItem(next).getItemProperty("Dataset Type").getValue();
				//TODO: No hardcoding!!
				if(datasetType.equals("FASTQC") || datasetType.equals("QCML")){
					visualize.setEnabled(true);
				}else{
					visualize.setEnabled(false);
				}
			}
		});
		//Assumes that table Value Change listner is enabling or disabling the button if preconditions are not fullfilled
		visualize.addClickListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				Set<Object> selectedValues = (Set<Object>) table.getValue();
				Iterator<Object> iterator = selectedValues.iterator();
				Object next = iterator.next();
				String datasetCode = (String)table.getItem(next).getItemProperty("CODE").getValue();
				String datasetFileName = (String)table.getItem(next).getItemProperty("File Name").getValue();
				DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
				URL url;
				try {
					url = dh.getUrlForDataset(datasetCode, datasetFileName);
					Window subWindow = new Window("QC of Sample: "+ (String)table.getItem(next).getItemProperty("Sample").getValue());
			        VerticalLayout subContent = new VerticalLayout();
			        subContent.setMargin(true);
			        subWindow.setContent(subContent);
			        QbicmainportletUI ui = (QbicmainportletUI)UI.getCurrent();
			        // Put some components in it
			        BrowserFrame frame = new BrowserFrame("", new ExternalResource(url));
			        frame.setSizeFull();
			        subContent.addComponent(frame);
			        
			        // Center it in the browser window
			        subWindow.center();
			        subWindow.setModal(true);
			        subWindow.setSizeFull();
			        
			        frame.setHeight((int)(ui.getPage().getBrowserWindowHeight()*0.8),Unit.PIXELS);
			        // Open it in the UI
			        ui.addWindow(subWindow);
				} catch (MalformedURLException e) {
					System.out.println("MalformedURLException");
					Notification.show("Given dataset has no file attached to it!! Please Contact your project manager. Or check whether it already has some data", Notification.Type.ERROR_MESSAGE);
				}
			}
		});
		
		this.general_information = new Label("Name: \nEntity Type: \nOwner: \n", Label.CONTENT_PREFORMATTED);
		this.general_information.setCaption("General Information: ");
		
		this.addComponent(this.general_information);
		
		this.addComponent(this.table);
		this.table.setSizeFull();
		this.addComponent(buttonLayout);
	}
	
	private FilterTable buildFilterTable() {
        FilterTable filterTable = new FilterTable();
        filterTable.setSizeFull();

        filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
        filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

        filterTable.setFilterBarVisible(true);

        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);

        filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

        filterTable.setColumnCollapsingAllowed(true);

        filterTable.setColumnReorderingAllowed(true);

        filterTable.setContainerDataSource(this.datasets);

        /*
        filterTable.setItemDescriptionGenerator(new ColumnDescriptionGenerator() {

        
        @Override
            public String generateDescription(Component source, Object itemId,
                    Object propertyId) {
                return propertyId.toString() + " of the corresponding dataset.";
            }
        });
		*/
        return filterTable;
    }
}
