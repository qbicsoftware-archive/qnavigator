package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
//import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;

import de.uni_tuebingen.qbic.util.DashboardUtil;

public class DataHandler {
	
	
	Map<String,IndexedContainer> spaces = new HashMap<String,IndexedContainer>();
	
	Map<String,IndexedContainer> space_to_projects = new HashMap<String,IndexedContainer>();

	Map<String,IndexedContainer> space_to_experiments = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> project_to_experiments = new HashMap<String,IndexedContainer>();
	
	Map<String,IndexedContainer> space_to_samples = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> project_to_samples = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> experiment_to_samples = new HashMap<String,IndexedContainer>();
	
	Map<String,IndexedContainer> space_to_datasets = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> project_to_datasets = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> experiment_to_datasets = new HashMap<String,IndexedContainer>();
	Map<String,IndexedContainer> sample_to_datasets = new HashMap<String,IndexedContainer>();
	
	
	
	OpenBisClient openBisClient;
	
	public DataHandler(OpenBisClient client){
		reset();
		
		this.openBisClient = client;
		//initConnection();
	}
	
	// id in this case meaning the openBIS instance ?!
	public IndexedContainer getSpaces(String id) throws Exception {
		
		List<SpaceWithProjectsAndRoleAssignments> space_list = null;
		IndexedContainer spaces = null;
		
		if(this.spaces.get(id) != null) {
			return this.spaces.get(id);
		}

		else if(this.spaces.get(id) == null){
			space_list = this.openBisClient.facade.getSpacesWithProjects();
			spaces = this.createSpaceContainer(space_list, id);
			this.spaces.put(id, spaces);
		}
		
		else {
			throw new Exception("Unknown Space: " + id);
		}
		
		return spaces;
		
	}

	
	public IndexedContainer getDatasets(String id, String type) throws Exception {

		List<DataSet> dataset_list = null;
		IndexedContainer datasets = null;

		// TODO change return type of dataset retrieval methods if possible
		if(type.equals("space")) {
			if(this.space_to_datasets.get(id) != null) {
				return this.space_to_datasets.get(id);
			}

			else {
				dataset_list = this.openBisClient.getDataSetsOfSpace(id);
				datasets = this.createDatasetContainer(dataset_list, id);
				this.space_to_datasets.put(id, datasets);
			}
		}
		else if(type.equals("project")) {
			if(this.project_to_datasets.get(id) != null) {
				return this.project_to_datasets.get(id);
			}

			else {
				dataset_list = this.openBisClient.getDataSetsOfProject(id);
				datasets = this.createDatasetContainer(dataset_list, id);
				this.project_to_datasets.put(id, datasets);
			}
		}
		else if(type.equals("experiment")) {
			if(this.experiment_to_datasets.get(id) != null) {
				return this.experiment_to_datasets.get(id);
			}

			else {
				dataset_list = this.openBisClient.getDataSetsOfExperiment(id);
				datasets = this.createDatasetContainer(dataset_list, id);
				this.experiment_to_datasets.put(id, datasets);
			}
		}
		else if(type.equals("sample")) {
			if(this.sample_to_datasets.get(id) != null) {
				return this.sample_to_datasets.get(id);
			}

			else {
				Sample sample = this.openBisClient.getSampleByIdentifier(id);
				
				dataset_list = this.openBisClient.getDataSetsOfSample(sample.getIdentifier());
				
				datasets = this.createDatasetContainer(dataset_list, id);
				this.sample_to_datasets.put(id, datasets);
			}
		}
		else {
			throw new Exception("Unknown datatype: " + type);
		}

		return datasets;
	}
	
	public IndexedContainer getSamples(String id, String type) throws Exception {

		List<Sample> sample_list = null;
		IndexedContainer samples = null;

		// TODO change return type of dataset retrieval methods if possible
		if(type.equals("space")) {
			if(this.space_to_samples.get(id) != null) {
				return this.space_to_samples.get(id);
			}

			else {
				sample_list = this.openBisClient.getSamplesofSpace(id);
				samples = this.createSampleContainer(sample_list, id);
				this.space_to_samples.put(id, samples);
			}
		}
		else if(type.equals("project")) {
			if(this.project_to_samples.get(id) != null) {
				return this.project_to_samples.get(id);
			}

			else {
				sample_list = this.openBisClient.getSamplesofProject(id);
				samples = this.createSampleContainer(sample_list, id);
				this.project_to_samples.put(id, samples);
			}
		}
		else if(type.equals("experiment")) {
			if(this.experiment_to_samples.get(id) != null) {
				return this.experiment_to_samples.get(id);
			}

			else {
				sample_list = this.openBisClient.getSamplesofExp(id);
				samples = this.createSampleContainer(sample_list, id);
				this.experiment_to_samples.put(id, samples);
			}
		}
		else {
			throw new Exception("Unknown datatype!");
		}

		return samples;
	}
	
	public IndexedContainer getExperiments(String id, String type) throws Exception {

		List<Experiment> experiment_list = null;
		IndexedContainer experiments = null;

		// TODO change return type of dataset retrieval methods if possible
		if(type.equals("space")) {
			if(this.space_to_experiments.get(id) != null) {
				return this.space_to_experiments.get(id);
			}

			else {
				experiment_list = this.openBisClient.getExperimentsofSpace(id);
				experiments = this.createExperimentContainer(experiment_list, id);
				this.space_to_experiments.put(id, experiments);
			}
		}
		else if(type.equals("project")) {
			if(this.project_to_experiments.get(id) != null) {
				return this.project_to_experiments.get(id);
			}

			else {
				experiment_list = this.openBisClient.getExperimentsofProject(id);
				experiments = this.createExperimentContainer(experiment_list, id);
				this.project_to_samples.put(id, experiments);
			}
		}
		else {
			throw new Exception("Unknown datatype!");
		}

		return experiments;
	}

	public IndexedContainer getProjects(String id, String type) throws Exception {

		List<Project> project_list = null;
		IndexedContainer projects = null;

		// TODO change return type of dataset retrieval methods if possible
		if(type.equals("space")) {
			if(this.space_to_projects.get(id) != null) {
				return this.space_to_projects.get(id);
			}

			else {
				project_list = this.openBisClient.getProjectsofSpace(id);
				projects = this.createProjectContainer(project_list, id);
				this.space_to_experiments.put(id, projects);
			}
		}
		else {
			throw new Exception("Unknown datatype!");
		}

		return projects;
	}




	public void reset() {
		 //this.spaces = new HashMap<String,IndexedContainer>();
		 //this.projects = new HashMap<String,IndexedContainer>();
		 //this.experiments = new HashMap<String,IndexedContainer>();
		 //this.samples = new HashMap<String,IndexedContainer>();
		 this.space_to_datasets = new HashMap<String,IndexedContainer>();
	}
	
	
	//public void initConnection() {
		// TODO this.openBISClient = new OpenClient();
	//}
	/**
	 * returns an empyt Container if id is not a valid space id
	 * @param spaces
	 * @param id
	 * @return
	 */
	private IndexedContainer createSpaceContainer(List<SpaceWithProjectsAndRoleAssignments> spaces, String id) {
		
		IndexedContainer space_container = new IndexedContainer();
		
		space_container.addContainerProperty("Number of Projects", Integer.class, 0);
		space_container.addContainerProperty("Number of Samples", Integer.class, 0);
		space_container.addContainerProperty("Number of Datasets", Integer.class, 0);
		space_container.addContainerProperty("Last Dataset registered", String.class, "No dataset registered");
		
		List<Project> projects = this.openBisClient.getProjectsofSpace(id);
		int number_of_samples = 0;
		int number_of_projects = projects.size();
		int number_of_datasets = 0;
		String lastModifiedDataset = "N/A"; 
		String lastModifiedExperiment = "N/A";
		String lastModifiedSample = "N/A";
		Date lastModifiedDate = new Date(0,0,0);
		
		//List<String> tmp_list_str = new ArrayList<String>();
		//for(Project project : projects){
		//	tmp_list_str.add(project.getIdentifier());
		//}
		
		
		
		//List<Experiment> experiments = this.openBisClient.getExperimentsofSpace(id);//this.openBisClient.openbisInfoService.listExperiments(this.openBisClient.getSessionToken(), projects, null);
		List<Sample> samplesOfSpace = this.openBisClient.getSamplesofSpace(id);//this.openBisClient.facade.listSamplesForProjects(tmp_list_str);
		number_of_samples += samplesOfSpace.size();
		//ArrayList<String> tmp_experiment_identifier_lis = new ArrayList<String>();
		//for(Experiment experiment: experiments){
		//	tmp_experiment_identifier_lis.add(experiment.getIdentifier());
		//}
		List<DataSet> datasets = this.openBisClient.getDataSetsOfSpace(id); //this.openBisClient.facade.listDataSetsForExperiments(tmp_experiment_identifier_lis);
		number_of_datasets = datasets.size();
		
		
		for(DataSet dataset: datasets){
			Date date = dataset.getRegistrationDate();
			if(date.after(lastModifiedDate)){
				lastModifiedDataset = dataset.getCode();
				lastModifiedSample = dataset.getSampleIdentifierOrNull();
				if(lastModifiedSample == null){
					lastModifiedSample = "N/A";
				}
				lastModifiedExperiment = dataset.getExperimentIdentifier();
			}
		}
		
		
		
		
		Object new_s = space_container.addItem();
		space_container.getContainerProperty(new_s, "Number of Projects").setValue(number_of_projects);
		space_container.getContainerProperty(new_s, "Number of Samples").setValue(number_of_samples);
		space_container.getContainerProperty(new_s, "Number of Datasets").setValue(number_of_datasets);

		space_container.getContainerProperty(new_s, "Last Dataset registered").setValue(String.format("DatSet %s in Sample %s on Experiment %s Date: %s", lastModifiedDataset, lastModifiedSample, lastModifiedExperiment, lastModifiedDate.toString()));
		return space_container;
	}
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createProjectContainer(List<Project> projs, String id) {
		
		IndexedContainer project_container = new IndexedContainer();
		
		project_container.addContainerProperty("Space", String.class, null);
		project_container.addContainerProperty("Description", String.class, null);
		project_container.addContainerProperty("Registration Date", Timestamp.class, null);
		project_container.addContainerProperty("Registerator", String.class, null);
		
		project_container.addContainerProperty("Space", String.class, null);
		
		for(Project p: projs) {
			Object new_p = project_container.addItem();
			
			String code = p.getCode();
			String desc = p.getDescription();
			
			String space = code.split("/")[1];
			
			Date date = p.getRegistrationDetails().getRegistrationDate();
			String registrator = p.getRegistrationDetails().getUserId();
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = sd.format(date);
            Timestamp ts = Timestamp.valueOf(dateString);
            
            project_container.getContainerProperty(new_p, "Space").setValue(space);
            project_container.getContainerProperty(new_p, "Description").setValue(desc);
            project_container.getContainerProperty(new_p, "Registration Date").setValue(ts);
            project_container.getContainerProperty(new_p, "Registerator").setValue(registrator);
		}
		
		return project_container;
	}
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createExperimentContainer(List<Experiment> exps, String id) {
		
		IndexedContainer experiment_container = new IndexedContainer();
		
		experiment_container.addContainerProperty("Space", String.class, null);
		experiment_container.addContainerProperty("Project", String.class, null);
		experiment_container.addContainerProperty("Experiment Type", String.class, null);
		experiment_container.addContainerProperty("Registration Date", Timestamp.class, null);
		experiment_container.addContainerProperty("Registerator", String.class, null);
		experiment_container.addContainerProperty("Properties", Map.class, null);


		
		for(Experiment e: exps) {
			Object new_ds = experiment_container.addItem();
			
			String experimentIdentifier = e.getIdentifier();
			String type = e.getExperimentTypeCode();
			String space = experimentIdentifier.split("/")[1];
			String project = experimentIdentifier.split("/")[2];
						
			Date date = e.getRegistrationDetails().getRegistrationDate();
			String registrator = e.getRegistrationDetails().getUserId();
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = sd.format(date);
            Timestamp ts = Timestamp.valueOf(dateString);
            
            experiment_container.getContainerProperty(new_ds, "Space").setValue(space);
            experiment_container.getContainerProperty(new_ds, "Project").setValue(project);
            experiment_container.getContainerProperty(new_ds, "Experiment Type").setValue(type);
            experiment_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
            experiment_container.getContainerProperty(new_ds, "Registerator").setValue(registrator);
            experiment_container.getContainerProperty(new_ds, "Properties").setValue(e.getProperties());
		}
            
		return experiment_container;
	}
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createSampleContainer(List<Sample> samples, String id) {
		
		IndexedContainer sample_container = new IndexedContainer();
		
		sample_container.addContainerProperty("Space", String.class, null);
		sample_container.addContainerProperty("Project", String.class, null);
		sample_container.addContainerProperty("Experiment", String.class, null);
		sample_container.addContainerProperty("Sample Type", String.class, null);
		sample_container.addContainerProperty("Registration Date", Timestamp.class, null);
		sample_container.addContainerProperty("Registerator", String.class, null);
		sample_container.addContainerProperty("Properties", HashMap.class, null);


		
		for(Sample s: samples) {
			Object new_ds = sample_container.addItem();
			
			String code = s.getCode();
			String type = s.getSampleTypeCode();
			
			String space = code.split("/")[1];
			String project = code.split("/")[2];
			String experiment = code.split("/")[3];
						
			Date date = s.getRegistrationDetails().getRegistrationDate();
			String registrator = s.getRegistrationDetails().getUserId();
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = sd.format(date);
            Timestamp ts = Timestamp.valueOf(dateString);
            
            sample_container.getContainerProperty(new_ds, "Space").setValue(space);
            sample_container.getContainerProperty(new_ds, "Project").setValue(project);
            sample_container.getContainerProperty(new_ds, "Experiment").setValue(experiment);
            sample_container.getContainerProperty(new_ds, "Sample Type").setValue(type);
            sample_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
            sample_container.getContainerProperty(new_ds, "Registerator").setValue(registrator);
            sample_container.getContainerProperty(new_ds, "Properties").setValue(s.getProperties());
		}
            
		return sample_container;
	}
	
	@SuppressWarnings("unchecked")
	private IndexedContainer createDatasetContainer(List<DataSet> datasets, String id) {
		
		IndexedContainer dataset_container = new IndexedContainer();
		
		dataset_container.addContainerProperty("Project", String.class, null);
		dataset_container.addContainerProperty("Sample", String.class, null);
		dataset_container.addContainerProperty("Sample Type", String.class, null);
		dataset_container.addContainerProperty("File Name", String.class, null);
		dataset_container.addContainerProperty("File Type", String.class, null);
		dataset_container.addContainerProperty("Dataset Type", String.class, null);
		dataset_container.addContainerProperty("Registration Date", Timestamp.class, null);
		dataset_container.addContainerProperty("Validated", Boolean.class, null);
		dataset_container.addContainerProperty("File Size", String.class, null);
		dataset_container.addContainerProperty("file_size_bytes", Long.class, null);
		dataset_container.addContainerProperty("dl_link", String.class, null);
		dataset_container.addContainerProperty("CODE", String.class, null);
		for(DataSet d: datasets) {
			
			Object new_ds = dataset_container.addItem();
			String code = d.getSampleIdentifierOrNull();
			String sample = code.split("/")[2];
			String project = sample.substring(0, 5);
			Date date = d.getRegistrationDate();
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = sd.format(date);
            Timestamp ts = Timestamp.valueOf(dateString);
            
            FileInfoDssDTO[] filelist = d.listFiles("original", false);
            
            String download_link = filelist[0].getPathInDataSet();
            String file_name = download_link.split("/")[1];
            
            String fileSize = DashboardUtil.humanReadableByteCount(filelist[0].getFileSize(), true);
            dataset_container.getContainerProperty(new_ds, "Project").setValue(project);
            dataset_container.getContainerProperty(new_ds, "Sample").setValue(sample);
            dataset_container.getContainerProperty(new_ds, "Sample Type").setValue(this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode());
            dataset_container.getContainerProperty(new_ds, "File Name").setValue(file_name);
            dataset_container.getContainerProperty(new_ds, "File Type").setValue(d.getDataSetTypeCode());
            dataset_container.getContainerProperty(new_ds, "Dataset Type").setValue(d.getDataSetTypeCode());
            dataset_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
            dataset_container.getContainerProperty(new_ds, "Validated").setValue(true);
            dataset_container.getContainerProperty(new_ds, "File Size").setValue( fileSize );
            dataset_container.getContainerProperty(new_ds, "dl_link").setValue(d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
            dataset_container.getContainerProperty(new_ds, "CODE").setValue(d.getCode());
            dataset_container.getContainerProperty(new_ds, "file_size_bytes").setValue(filelist[0].getFileSize());
		}
		System.out.println("datasets size: " + datasets.size());
		return dataset_container;
	}


	public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {
		
		return this.openBisClient.getDataStoreDownloadURL(datasetCode, datasetName);
	}
	
	public InputStream getDatasetStream(String datasetCode){
		
		IOpenbisServiceFacade facade = openBisClient.getFacade();
		DataSet dataSet  = facade.getDataSet(datasetCode);
		FileInfoDssDTO[] filelist = dataSet.listFiles("original", false);
		return dataSet.getFile(filelist[0].getPathInDataSet());
	}

	public void fillHierarchicalTreeContainer(HierarchicalContainer tc, String screenName) {
		tc.addContainerProperty("metadata", DummyMetaData.class, new DummyMetaData());
		tc.addContainerProperty("identifier", String.class, "N/A");
		tc.addContainerProperty("type", String.class, "N/A");
		
		List<SpaceWithProjectsAndRoleAssignments> space_list = this.openBisClient.facade.getSpacesWithProjects();
		
		for(SpaceWithProjectsAndRoleAssignments s : space_list) {
			if(s.getUsers().contains(screenName)){
				String space_name  = s.getCode();

				tc.addItem(space_name);
				tc.setParent(space_name, null);
				tc.getContainerProperty(space_name, "identifier").setValue(space_name);
				tc.getContainerProperty(space_name, "type").setValue("space");
				
				DummyMetaData dmd = new DummyMetaData();
				dmd.setIdentifier(space_name);
				dmd.setType(MetaDataType.QSPACE);
				dmd.setDescription("This is space " + space_name);
				dmd.setCreationDate(new Date(2020,02,10));
				List<Project> projects = s.getProjects();
				for(Project project: projects){
					String project_name = project.getCode();
					tc.addItem(project_name);
					tc.setParent(project_name, space_name);
					DummyMetaData dmd1 = new DummyMetaData();
					dmd1.setIdentifier(project_name);
					dmd1.setType(MetaDataType.QPROJECT);
					dmd1.setDescription(project.getDescription());
					EntityRegistrationDetails erd = project.getRegistrationDetails();
					if(erd == null){
						dmd1.setCreationDate(new Date(2020,1,1));
					}else{
						dmd1.setCreationDate(erd.getRegistrationDate());
					}
					tc.getContainerProperty(project_name, "metadata").setValue(dmd1);
					tc.getContainerProperty(project_name, "type").setValue("project");
					tc.getContainerProperty(project_name, "identifier").setValue(project_name);
					List<Project> tmp_list = new ArrayList<Project>();
					tmp_list.add(project);
					int number_of_samples = 0;
					List<Experiment> experiments = this.openBisClient.openbisInfoService.listExperiments(this.openBisClient.getSessionToken(), tmp_list, null);
					
					for(Experiment experiment : experiments){
						List<Sample> samples = this.openBisClient.openbisInfoService.listSamplesForExperiment(this.openBisClient.getSessionToken(), experiment.getIdentifier());
						for(Sample sample : samples){
							number_of_samples++;
							String samp = sample.getCode();
							tc.addItem(samp);
							tc.setParent(samp, project_name);

							DummyMetaData dmd3 = new DummyMetaData();
							dmd3.setIdentifier(samp);
							dmd3.setType(MetaDataType.QSAMPLE);
							dmd3.setDescription(sample.getIdentifier());
							dmd3.setNumOfChildren(-1);
							dmd3.setCreationDate(sample.getRegistrationDetails().getRegistrationDate());

							tc.getContainerProperty(samp, "metadata").setValue(dmd3);
							tc.getContainerProperty(samp, "type").setValue("sample");
							tc.getContainerProperty(samp, "identifier").setValue(samp);
							tc.setChildrenAllowed(samp, false);
						}
					}				
					dmd1.setNumOfChildren(number_of_samples);
				}
			}
		}
	}
}
	
