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

public class DatasetView extends VerticalLayout {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8672873911284888801L;

	private FilterTable table;
	private IndexedContainer datasets;
	private Button download;
	private final String DOWNLOAD_BUTTON_CAPTION = "Download marked files";
	private final String[] FILTER_TABLE_COLUMNS = new String[] { "Patient", "Sample", "Sample Type",
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
	}
	
	private void buildLayout(){
		//Layout
		this.setSizeFull();
		this.setVisible(true);
		HorizontalLayout buttonLayout = new HorizontalLayout();
	    buttonLayout.setHeight(null);
	    buttonLayout.setWidth("100%");
	    buttonLayout.setSpacing(true);
		
		this.download = new Button(DOWNLOAD_BUTTON_CAPTION);
		this.download.setStyleName(Reindeer.BUTTON_SMALL);
		buttonLayout.addComponent(this.download);
		
		
		table = buildFilterTable();
		
		this.addComponent(this.table);
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

        filterTable.setColumnCollapsed("state", true);

        filterTable.setVisibleColumns((Object[]) FILTER_TABLE_COLUMNS);

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
