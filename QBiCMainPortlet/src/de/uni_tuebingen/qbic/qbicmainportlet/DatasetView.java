package de.uni_tuebingen.qbic.qbicmainportlet;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class DatasetView extends VerticalLayout implements View, ValueChangeListener, ClickListener {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8672873911284888801L;
	//private Toolbar toolbar;
	//private Ivac_portletUI app;
	private FilterTable table;
	private IndexedContainer datasets;
	private ComboBox project;
	private Button download;
	
	public DatasetView() {
		
	}
	
	/*public DatasetView(Ivac_portletUI app) {
		this.setSizeFull();
		this.app = app; 
		this.setVisible(true);
		toolbar = new Toolbar();
		this.addComponent(toolbar);
		
		
		Panel panel = new Panel();
		VerticalLayout vert = new VerticalLayout();
		vert.setSpacing(true);
		
		HorizontalLayout horz = new HorizontalLayout();
		horz.setSpacing(true);
		
		this.setMargin(new MarginInfo(false, true, false, true));
		
		this.setSpacing(true);
				
		this.datasets = new IndexedContainer();
		this.datasets.addContainerProperty("Patient", String.class, null);
		this.datasets.addContainerProperty("Sample", String.class, null);
		this.datasets.addContainerProperty("Sample Type", String.class, null);
		this.datasets.addContainerProperty("File Name", String.class, null);
		this.datasets.addContainerProperty("File Type", String.class, null);
		this.datasets.addContainerProperty("Dataset Type", String.class, null);
		this.datasets.addContainerProperty("Registration Date", Timestamp.class, null);
		this.datasets.addContainerProperty("Validated", Boolean.class, null);
		this.datasets.addContainerProperty("File Size", Integer.class, null);
		this.datasets.addContainerProperty("dl_link", String.class, null);
		
		this.project = this.createProjectCombobox(this.app.getSpaces());
		horz.addComponent(this.project);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
	    buttonLayout.setHeight(null);
	    buttonLayout.setWidth("100%");
	    buttonLayout.setSpacing(true);
		
		this.download = new Button("Download marked files");
		this.download.setStyleName(Reindeer.BUTTON_SMALL);
		this.download.addClickListener(this);
		buttonLayout.addComponent(this.download);
		
        vert.addComponent(horz);
		
		table = buildFilterTable();
		vert.addComponent(table);
		vert.addComponent(buttonLayout);
		vert.setSizeFull();
		vert.setExpandRatio(table, 1.0f);
		panel.setContent(vert);
		panel.setStyleName(Reindeer.PANEL_LIGHT);
		panel.setSizeFull();
		this.addComponent(panel);
		setExpandRatio(panel, 1.0f);
	}
	*/
	
	private ComboBox createProjectCombobox(List<String> container) {
		//Creates a new combobox using an existing container
		ComboBox select_spaces = new ComboBox("Select Project.",container);
		select_spaces.setInputPrompt("No project selected");

		// Sets the combobox to show a certain property as the item caption
		//select_spaces.setItemCaptionPropertyId(ExampleUtil.iso3166_PROPERTY_NAME);
		//select_spaces.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		// Set a reasonable width
		select_spaces.setWidth(350.0f, Unit.PIXELS);

		// Set the appropriate filtering mode for this example
		select_spaces.setFilteringMode(FilteringMode.CONTAINS);
		select_spaces.setImmediate(true);

		// Disallow null selections
		select_spaces.setNullSelectionAllowed(false);

		select_spaces.addValueChangeListener((ValueChangeListener) this);
		//this.spaces = select_spaces;
		return select_spaces;	
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

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

        filterTable.setColumnCollapsed("state", true);

        filterTable.setVisibleColumns((Object[]) new String[] { "Patient", "Sample", "Sample Type",
                "File Name", "File Type", "Dataset Type", "Registration Date" , "Validated", "File Size"});
        
        filterTable.addValueChangeListener(this);

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
	
	

	private void addDataSets(List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> list) {
				
		for(ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet d: list) {
			Object new_ds = this.datasets.addItem();
			String code = d.getSampleIdentifierOrNull();
			String sample = code.split("/")[2];
			String patient = sample.substring(0, 5);
			Date date = d.getRegistrationDate();
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = sd.format(date);
            Timestamp ts = Timestamp.valueOf(dateString);
            
            
            FileInfoDssDTO[] filelist = d.listFiles("original", false);
            
            String download_link = filelist[0].getPathInDataSet();
            String file_name = download_link.split("/")[1];
            
            //System.out.println(d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());


            //long filesize = filelist[0].getFileSize() / (1024L * 1024L);
            
            this.datasets.getContainerProperty(new_ds, "Patient").setValue(patient);
            this.datasets.getContainerProperty(new_ds, "Sample").setValue(sample);
            //this.datasets.getContainerProperty(new_ds, "Sample Type").setValue(this.app.getOpenClient().getSampleByIdentifier(sample).getSampleTypeCode());
            this.datasets.getContainerProperty(new_ds, "File Name").setValue(file_name);
            this.datasets.getContainerProperty(new_ds, "File Type").setValue(d.getDataSetTypeCode());
            this.datasets.getContainerProperty(new_ds, "Dataset Type").setValue(d.getDataSetTypeCode());
            this.datasets.getContainerProperty(new_ds, "Registration Date").setValue(ts);
            this.datasets.getContainerProperty(new_ds, "Validated").setValue(true);
            this.datasets.getContainerProperty(new_ds, "File Size").setValue( (int) filelist[0].getFileSize());
            this.datasets.getContainerProperty(new_ds, "dl_link").setValue(d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
		}
		
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property<?> property = event.getProperty();
		
		if(property == this.project) {
			this.datasets.removeAllItems();
			//this.addDataSets(this.app.getOpenClient().getDataSetsOfSpace(this.project.getValue().toString()));
		}
		
		else if(property == this.table) {
			System.out.println(table.getValue());
		} 
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		
		if (event.getButton() == this.download)
		{
			Collection selected_datasets = (Collection) this.table.getValue();
			
			for(Object i: selected_datasets) {
				System.out.println(this.datasets.getItem(i).getItemProperty("dl_link").getValue());
			}
		}
	}
}
