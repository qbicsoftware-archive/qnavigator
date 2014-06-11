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

import de.uni_tuebingen.qbic.util.DashboardUtil;

public class DataHandler {
	
	Map<String,IndexedContainer> datasets = new HashMap<String,IndexedContainer>();
	OpenBisClient openBisClient;
	
	public DataHandler(OpenBisClient client){
		reset();
		
		this.openBisClient = client;
		//initConnection();
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
			this.datasets.put(id, new IndexedContainer());
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
		
		return this.createDatasetContainer(dataset_list, id);
		
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
		 this.datasets = new HashMap<String,IndexedContainer>();
	}
	
	
	//public void initConnection() {
		// TODO this.openBISClient = new OpenClient();
	//}
	
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createDatasetContainer(List<DataSet> datasets, String id) {
		
	
		this.datasets.get(id).addContainerProperty("Patient", String.class, null);
		this.datasets.get(id).addContainerProperty("Sample", String.class, null);
		this.datasets.get(id).addContainerProperty("Sample Type", String.class, null);
		this.datasets.get(id).addContainerProperty("File Name", String.class, null);
		this.datasets.get(id).addContainerProperty("File Type", String.class, null);
		this.datasets.get(id).addContainerProperty("Dataset Type", String.class, null);
		this.datasets.get(id).addContainerProperty("Registration Date", Timestamp.class, null);
		this.datasets.get(id).addContainerProperty("Validated", Boolean.class, null);
		this.datasets.get(id).addContainerProperty("File Size", String.class, null);
		this.datasets.get(id).addContainerProperty("dl_link", String.class, null);
		
		for(DataSet d: datasets) {
			Object new_ds = this.datasets.get(id).addItem();
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
            
            String fileSize = DashboardUtil.humanReadableByteCount(filelist[0].getFileSize(), true);
            
            this.datasets.get(id).getContainerProperty(new_ds, "Patient").setValue(patient);
            this.datasets.get(id).getContainerProperty(new_ds, "Sample").setValue(sample);
            this.datasets.get(id).getContainerProperty(new_ds, "Sample Type").setValue(this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode());
            this.datasets.get(id).getContainerProperty(new_ds, "File Name").setValue(file_name);
            this.datasets.get(id).getContainerProperty(new_ds, "File Type").setValue(d.getDataSetTypeCode());
            this.datasets.get(id).getContainerProperty(new_ds, "Dataset Type").setValue(d.getDataSetTypeCode());
            this.datasets.get(id).getContainerProperty(new_ds, "Registration Date").setValue(ts);
            this.datasets.get(id).getContainerProperty(new_ds, "Validated").setValue(true);
            this.datasets.get(id).getContainerProperty(new_ds, "File Size").setValue( fileSize );
            this.datasets.get(id).getContainerProperty(new_ds, "dl_link").setValue(d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
		}
		return this.datasets.get(id);
	}
}
	
