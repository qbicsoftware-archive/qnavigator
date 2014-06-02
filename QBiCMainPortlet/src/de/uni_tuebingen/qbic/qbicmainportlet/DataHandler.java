package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.util.IndexedContainer;

public class DataHandler {
	
	Map<String,IndexedContainer> datasets;
	OpenBisClient openBisClient;
	
	public DataHandler(){
		reset();
		initConnection();
	}

	public IndexedContainer getDatasets(String id) throws Exception {
		if(this.datasets.get(id) != null) {
			return this.datasets.get(id);
		}
		
		else {
			this.datasets = retrieveOpenBisData("Dataset");
			
			if(this.datasets.get(id) != null) {
				return this.datasets.get(id);
			}
			
			else {
		      	  throw new Exception("Sample ID not in database.");
			}
		}
	}


	public void reset() {
		 //this.spaces = new HashMap<String,IndexedContainer>();
		 //this.projects = new HashMap<String,IndexedContainer>();
		 //this.experiments = new HashMap<String,IndexedContainer>();
		 //this.samples = new HashMap<String,IndexedContainer>();
		 this.datasets = new HashMap<String,IndexedContainer>();
	}
	
	public void initConnection() {
		// TODO this.openBISClient = new OpenClient();
	}
	
	private Map<String, IndexedContainer> retrieveOpenBisData(String type) {
		// TODO Auto-generated method stub
		return null;
	}
}
	
