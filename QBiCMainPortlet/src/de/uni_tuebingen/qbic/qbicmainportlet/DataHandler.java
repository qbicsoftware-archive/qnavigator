package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;

import com.vaadin.data.util.IndexedContainer;

public class DataHandler {
	
	IndexedContainer datasets;
	OpenBisClient openBisClient;
	
	public DataHandler(){
		reset();
		initConnection();
	}

	/*
	public IndexedContainer getDatasets(String id, String type) throws Exception {
		if(this.datasets.get(id) != null) {
			return this.datasets.get(id);
		}
		
		else {
		
		List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataset = null;
		
		if(type.equals("space")) {
			// TODO change return type of method
			//dataset = this.openBisClient.getDataSetsOfSpace(id);
		}
		else if(type.equals("project")) {
			dataset = this.openBisClient.getDataSetsOfProject(id);
		}
		else if(type.equals("experiment")) {
			dataset = this.openBisClient.getDataSetsOfExperiment(id);
		}
		else if(type.equals("sample")) {
			dataset = this.openBisClient.getDataSetsOfSample(id);
		}
		else {
			throw new Exception("Unknown datatype!");
		}
		
		//this.datasets = this.toContainer(List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataset, type);
		
			if(this.datasets.get(id) != null) {
				return this.datasets.get(id);
			}
			
			else {
		      	  throw new Exception("Sample ID not in database.");
			}
		}
		
	}
*/

	private IndexedContainer toContainer(List<DataSet> dataset, String type) {
		// TODO Auto-generated method stub
		return null;
	}

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
	
	private Map<String, IndexedContainer> retrieveOpenBisData(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private IndexedContainer toContainer(String type) {
		IndexedContainer ix = new IndexedContainer();
		
		if(type.equals("dataset")){
			
		}
		
		return ix;
	}
}
	
