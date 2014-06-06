package de.uni_tuebingen.qbic.qbicmainportlet;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

import com.vaadin.data.util.IndexedContainer;

public class DataHandler {
	
	IndexedContainer datasets;
	OpenBisClient openBisClient;
	
	public DataHandler(){
		reset();
		initConnection();
	}

	
	public IndexedContainer getDatasets(String id, String type) throws Exception {
		/*if(this.datasets.get(id) != null) {
			return this.datasets.get(id);
		}
		
		else {
		*/
		List<DataSet> dataset_list = null;
		
		// TODO change return type of dataset retrieval methods if possible
		if(type.equals("space")) {
			dataset_list = this.openBisClient.getDataSetsOfSpace(id);
		}
		/*
		else if(type.equals("project")) {
			dataset = this.openBisClient.getDataSetsOfProject(id);
		}
		else if(type.equals("experiment")) {
			dataset = this.openBisClient.getDataSetsOfExperiment(id);
		}
		else if(type.equals("sample")) {
			dataset = this.openBisClient.getDataSetsOfSample(id);
		}*/
		else {
			throw new Exception("Unknown datatype!");
		}
		
		return this.createDatasetContainer(dataset_list);
		
		//this.datasets = this.toContainer(List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataset, type);
		
			/*if(this.datasets.get(id) != null) {
				return this.datasets.get(id);
			}
			
			else {
		      	  throw new Exception("Sample ID not in database.");
			}
			*/
		}

	//}


	public void reset() {
		 //this.spaces = new HashMap<String,IndexedContainer>();
		 //this.projects = new HashMap<String,IndexedContainer>();
		 //this.experiments = new HashMap<String,IndexedContainer>();
		 //this.samples = new HashMap<String,IndexedContainer>();
		 //this.datasets = new HashMap<String,IndexedContainer>();
	}
	
	public void initConnection() {
		// TODO this.openBISClient = new OpenClient();
	}
	
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createDatasetContainer(List<DataSet> datasets) {
		IndexedContainer ds = new IndexedContainer();
		
		ds.addContainerProperty("Patient", String.class, null);
		ds.addContainerProperty("Sample", String.class, null);
		ds.addContainerProperty("Sample Type", String.class, null);
		ds.addContainerProperty("File Name", String.class, null);
		ds.addContainerProperty("File Type", String.class, null);
		ds.addContainerProperty("Dataset Type", String.class, null);
		ds.addContainerProperty("Registration Date", Timestamp.class, null);
		ds.addContainerProperty("Validated", Boolean.class, null);
		ds.addContainerProperty("File Size", Integer.class, null);
		ds.addContainerProperty("dl_link", String.class, null);
		
		for(DataSet d: datasets) {
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
            
            ds.getContainerProperty(new_ds, "Patient").setValue(patient);
            ds.getContainerProperty(new_ds, "Sample").setValue(sample);
            ds.getContainerProperty(new_ds, "Sample Type").setValue(this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode());
            ds.getContainerProperty(new_ds, "File Name").setValue(file_name);
            ds.getContainerProperty(new_ds, "File Type").setValue(d.getDataSetTypeCode());
            ds.getContainerProperty(new_ds, "Dataset Type").setValue(d.getDataSetTypeCode());
            ds.getContainerProperty(new_ds, "Registration Date").setValue(ts);
            ds.getContainerProperty(new_ds, "Validated").setValue(true);
            ds.getContainerProperty(new_ds, "File Size").setValue( (int) filelist[0].getFileSize());
            ds.getContainerProperty(new_ds, "dl_link").setValue(d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
		}
		return ds;
	}
}
	
