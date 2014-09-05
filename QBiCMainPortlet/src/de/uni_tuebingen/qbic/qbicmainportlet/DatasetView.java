package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class DatasetView extends VerticalLayout {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8672873911284888801L;

	private Label general_information;
	private FilterTable table;
	private IndexedContainer datasets;
	private ButtonLink download;
	private final String DOWNLOAD_BUTTON_CAPTION = "Download marked files";
	private final String[] FILTER_TABLE_COLUMNS = new String[] { "Project", "Sample", "Sample Type",
            "File Name", "File Type", "Dataset Type", "Registration Date" , "Validated", "File Size"};
	
	public DatasetView() {
		this.buildLayout();
	}
	
	public DatasetView(IndexedContainer dataset){
		this.datasets = dataset;
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
	
	private void buildLayout(){
		//Layout
		this.setSizeFull();
		this.setVisible(true);
		HorizontalLayout buttonLayout = new HorizontalLayout();
	    buttonLayout.setHeight(null);
	    buttonLayout.setWidth("100%");
	    buttonLayout.setSpacing(true);
		
		this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));
		//this.download.setStyleName(Reindeer.BUTTON_SMALL);
		buttonLayout.addComponent(this.download);
		
		
		this.table = buildFilterTable();
		MpPortletListener mppl  = new MpPortletListener(this.download, this.table);
		this.table.addValueChangeListener(mppl);
	       if (VaadinSession.getCurrent() instanceof VaadinPortletSession) {
	            VaadinPortletSession portletsession =
	                    (VaadinPortletSession) VaadinSession.getCurrent();

	            // Add a custom listener to handle action and
	            // render requests.
	            portletsession.addPortletListener(mppl);
	      
			}

		
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
