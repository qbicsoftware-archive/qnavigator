package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;



/**
 * dss client, a proxy to the generic openbis api
 * This client is based on DSS Client for openBIS written by Emanuel Schmid and the OpenbisConnector written by Bela Hullar
 * @author wojnar
 */
public class OpenBisClient {//implements Serializable {
	/**
	 * 
	 */
	//private static final long serialVersionUID = 3926210649301601498L;
	int timeout = 120; // 2 minutes
	int tolimit = 600;
	IOpenbisServiceFacade facade;
	IGeneralInformationService openbisInfoService;
	IQueryApiServer openbisDssService;
	String sessionToken;
	String userId;
	String password;
	String serverURL;
	IServiceForDataStoreServer testService1;
	boolean verbose;
	
	
	public OpenBisClient(String loginid, String password, String serverURL, boolean verbose)
	{
		this.userId = loginid;
		this.password = password;
		this.serverURL = serverURL;
		this.verbose = verbose;
		this.facade = null;
		this.login();
	}
	public boolean loggedin(){
		if (this.facade == null)
			return false;
		try{
			this.facade.checkSession();
		}
		catch(InvalidSessionException e){
			return false;
		}
		return true;
	}
	/**
	 * logs out of the OpenBIS server
	 */
	public void logout(){
		try{
			this.facade.logout();
		}
		catch (Exception e){
			//Nothing todo here
		} finally{
			this.facade = null;
		}
	}
	/**
	 *  logs in to the OpenBIS server with the system userid
     *  after calling this function, the user has to provide the password
	 */
	public void login(){
		if(this.loggedin())
			this.logout();
		
		int trialno = 3;
		int notrial = 0;
		int timeoutStep = this.timeout;
		while(true){
			try{
				facade = OpenbisServiceFacadeFactory.tryCreate(this.userId, this.password, this.serverURL, this.timeout*1000);
				ServiceFinder serviceFinder = new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
				ServiceFinder serviceFinder2 = new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);

				this.openbisInfoService = serviceFinder.createService(IGeneralInformationService.class, this.serverURL);
				this.openbisDssService = serviceFinder2.createService(IQueryApiServer.class, this.serverURL);
				this.sessionToken = this.openbisInfoService.tryToAuthenticateForAllServices(this.userId, this.password);
				break;
			}
			catch (Exception e){
				if(e.getMessage().contains("Read timed out")){
					if (this.timeout >= this.tolimit)
						throw new InvalidAuthenticationException("login failed");
					this.timeout += timeoutStep;
					
				}
				else{
					notrial++;
					if(notrial >= trialno) 
						throw new InvalidAuthenticationException("login failed");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(facade == null){
						return;//throw new UnexpectedException("OpenBis facade is not available");
					}
				}
			}
		}
	}
	
	
	/**
	 * Get a openBIS service facade when logged in to get functionality to 
	 * retrieve data for example.
	 * @return      a IOpenbisServiceFacade which provides various functions
	 */
	public IOpenbisServiceFacade getFacade(){
		if(!this.loggedin()){
			this.login();
		}
		return facade;
	}

	protected void finalize() throws Throwable {
		this.logout();
		super.finalize();
	}
	
	/**
	 * Function to get all properties assigned to a sample
	 * @param  sample  the sample object
	 * @return map with all properties assigned to the given sample (key=ID,value=fields)
	 */
	public Map<String,String> getProperties(Sample sample) {
		if(!this.loggedin())
			this.login();
		return sample.getProperties();
	}
	
	/**
	 * Function to retrieve all samples of a given experiment
	 * @param  exp  identifier of the openBIS experiment
	 * @return list with all samples of the given experiment
	 */
	public List<Sample> getSamplesofExp(String exp) {
		SearchCriteria sc = new SearchCriteria();
		SearchCriteria ec = new SearchCriteria();  
        ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, exp));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> foundSamples = this.openbisInfoService.searchForSamples(sessionToken, sc);
		return foundSamples;
		
	}
	
	/**
	 * Function to retrieve all samples of a given space
	 * @param  space  identifier of the openBIS space
	 * @return list with all samples of the given space
	 */
	public List<Sample> getSamplesofSpace(String space) {
		//this.openbisInfoService.listSpacesWithProjectsAndRoleAssignments(arg0, arg1)
        SearchCriteria sc = new SearchCriteria();        
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
        List<Sample> foundSamples = this.openbisInfoService.searchForSamples(sessionToken, sc);
		return foundSamples;
		
	}
	
	/**
	 * Function to retrieve a sample by it's identifier ... actually it seems to be the code not the identifier
	 * @param  id  identifier of the sample
	 * @return the sample with the given identifier
	 */
	public Sample getSampleByIdentifier(String id) {
	     SearchCriteria sc = new SearchCriteria();
	     sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, id));

	     List<Sample> foundSamples = this.openbisInfoService.searchForSamples(sessionToken, sc);
	     return foundSamples.get(0);
	}
	/**
	 * NOT IMPLEMENTED, DOES NOTHING
	 * @param id
	 */
	public void getSampleByCode(String id) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Function to get all samples of a specific project
	 * @param  project  identifier of the openBIS project
	 * @return list with all samples connected to the given project
	 */
	public List<Sample> getSamplesofProject(String project) {
		List<String> projects  = new ArrayList<String>();
		List<Project> foundProjects = facade.listProjects();
		List<Sample> foundSamples = new ArrayList<Sample>();
		
		
		for (Project proj : foundProjects) {
			if (project.equals(proj.getCode())){
				projects.add(proj.getIdentifier());
			}
		}
				
		if (projects.size() > 0) {
			List<Experiment> foundExp = facade.listExperimentsForProjects(projects);
			for (Experiment exp : foundExp) {
				SearchCriteria sc = new SearchCriteria();
				SearchCriteria ec = new SearchCriteria();
				ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, exp.getIdentifier()));
				sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
				foundSamples.addAll(this.openbisInfoService.searchForSamples(sessionToken, sc));
			}
		}
		
		
		// works in new sprint release version of the API, however does not return properties !!!!!
		
		//foundSamples = this.facade.listSamplesForProjects(projects);
		return foundSamples; 		
	}

	
	/**
	 * Function to retrieve a sample of a given space by its barcode 
	 * @param  space  identifier of the openBIS space
	 * @param  barcode barcode string
	 * @return sample of the given space with the given barcode
	 */
	public Sample getSamplebyBarcode(String space, String barcode) {
		List<Sample> samples= this.getSamplesofSpace(space);
		Sample found_sample = null;
		for (Sample samp: samples){
			if (samp.getProperties().get("QBIC_BARCODE").equals(barcode)) {
				found_sample = samp;
			}
		}
		return found_sample;
	}

	
	// TODO
	// Use listExperiments ?
	// java.util.List<IExperimentImmutable> listExperiments(java.lang.String projectIdentifier)
	// @param /space/EXP_CODE
	// in ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2
	/**
	 * returns a list of all Experiments connected to the project with the identifier from openBis
	 * @param projectIdentifier
	 * @return
	 */
	public List<Experiment> getExperimentsofProject(String projectIdentifier) {
		List<String> projects  = new ArrayList<String>();
		projects.add(projectIdentifier);
        List<Experiment> foundExps = facade.listExperimentsForProjects(projects);   
		return foundExps;	
	}
	
	/**
	 * Function to get all experiments for a given space and the information to which project 
	 * the corresponding experiment belongs to 
	 * @param  space  code of a openBIS space
	 * @return map with all projects (keys) and lists with all connected experiments (values)
	 */
	public Map<String, List<Experiment>> getProjectExperimentMapping(String space) {
		Map<String, List<Experiment>> mapping = new HashMap<String,List<Experiment>>();
		
		for(Experiment e: this.getExperimentsofSpace(space)) {
			String[] splitted = e.getIdentifier().split("/");
			String key = "/" + splitted[1] + "/" + splitted[2];
			List<Experiment> value = mapping.get(key);
			if (value != null) {
				mapping.get(key).add(e);
			} else {
				List<Experiment> exps = new ArrayList<Experiment>();
				exps.add(e);
				mapping.put(key,exps);
			}
		};
		
		return mapping;
	}
	
	/**
	 * Function to retrieve all experiments of a given space 
	 * @param  space  identifier of the openBIS space
	 * @return list with all experiments connected to this space
	 */
	public List<Experiment> getExperimentsofSpace(String space) {
		if (space.isEmpty()) {
			List<Experiment> foundExps = this.listExperiments();
			return foundExps;
		}
		else {
			SearchCriteria sc = new SearchCriteria();
			sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
			List<Experiment> foundExps = this.openbisInfoService.searchForExperiments(this.sessionToken, sc);
			return foundExps;
		}
	}
	
	/**
	 * Function to retrieve all experiments of a specific type 
	 * @param  type  identifier of the openBIS experiment type
	 * @return list with all experiments of this type
	 */
	public List<Experiment> getExperimentsofType(String type) {
		SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
        List<Experiment> foundExps = this.openbisInfoService.searchForExperiments(this.sessionToken, sc);
		return foundExps;
		
	}
	
	/**
	 * Function to retrieve all samples of a specific type 
	 * @param  type  identifier of the openBIS sample type
	 * @return list with all samples of this type
	 */
	public List<Sample> getSamplesofType(String type) {
		SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
        List<Sample> foundSamples = this.openbisInfoService.searchForSamples(this.sessionToken, sc);     
		return foundSamples;
		
	}
	
	/**
	 * Function to retrieve all projects of a given space from 
	 * openBIS.
	 * @param  space  the openBIS space (space object)
	 * @return  a list with all projects objects for that space
	 */
	public List<Project> getProjectsofSpace(String space) {
		List<Project> projects = new ArrayList<Project>();
		List<Project> foundProjects = facade.listProjects();
		
		for (Project proj : foundProjects) {
			if (space.equals(proj.getSpaceCode())){
				projects.add(proj);
			}
		}
		return projects;
	}
	
	
	/**
	 * Function to retrieve a project from openBIS by the ID of the 
	 * project.
	 * @param  proj  ID of the project which should be retrieved as string
	 * @return project associatied with the given id
	 */
	public Project getProjectbyID(String proj) {
		List<Project> projects = this.listProjects();
		Project project = null;
		for(Project p: projects){
			if(p.getIdentifier().equals(proj)){
				project = p;
			}
		}
		return project;
	}
	
	public Project getProjectByOpenBisCode(String projectCode){
		List<Project> projects = this.listProjects();
		Project project = null;
		for(Project p: projects){
			if(p.getCode().equals(projectCode)){
				project = p;
				break;
			}
		}
		return project;		
	}
	
	public Experiment getExperimentByOpenBisCode(String openbisCode) {
		List<Experiment> experiments =  this.listExperiments();
		Experiment experiment = null;
		for(Experiment e: experiments){
			if(e.getCode().equals(openbisCode)){
				experiment = e;
				break;
			}
		}
		return experiment;
	}
	
	/**
	 * Function to retrieve the project of an experiment from openBIS
	 * @param  exp  ID of the experiment as string
	 * @return  the found project
	 */
	public Project getProjectofExperiment(String exp) {
		List<Project> projects = this.facade.listProjects();
		String project = exp.split(("/"))[2];
		Project found_proj = null;
		
		for(Project proj: projects){
			if (project.equals(proj.getIdentifier().split("/")[2])) {
				found_proj = proj;
			}
		}
		return found_proj;	
	}
	
	/*
	public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> listDataSetsTest(String code) {
		List<Experiment> exps = this.getExperimentsofSpace(code);
		
		System.out.println(exps);
		
		List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> ds = new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>();
		for(Experiment exp: exps) {
			ds.addAll(this.getDataSetsOfExperiment(exp.getCode()));
		}
		
		return ds;
	}
*/

	/**
	 * Function to list all datasets of a specific sample
	 * @param  projCode identifier of the openBIS sample
	 * @return list with all datasets of the given sample
	 */
	public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfSample(String code) {
		
		List<String> codes = new ArrayList<String>();
		codes.add(code);
		
		return this.facade.listDataSetsForSamples(codes);
	}
	// TODO ANOTHER WAY TO GET THE CORRECT DATASET TYPE ?
	/*
	public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfSample(String code) {
		SearchCriteria ec = new SearchCriteria();
		ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
		SearchCriteria sc = new SearchCriteria();
		sc.addSubCriteria(SearchSubCriteria.createSampleCriteria(ec));
		return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
		}
	*/
	/**
	 * Function to list all datasets of a specific experiment
	 * @param  expPermId permId of the openBIS experiment (experiment.getPermId())
	 * @return list with all datasets of the given experiment
	 */
	public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfExperiment(String expPermId) {
		
		return this.facade.listDataSetsForExperiment(expPermId);
	}
	
	// TODO ANOTHER WAY TO GET THE CORRECT DATASET TYPE ?
	/**
	 * Returns all datasets of a given experiment. The new version should run smoother
	 * @param expCode openbis code of an openbis experiment
	 * @return list of dataset
	 * @deprecated 
	 */
	public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfExperimentOld(String expCode) {
		SearchCriteria ec = new SearchCriteria();
		ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, expCode));
		SearchCriteria sc = new SearchCriteria();
		sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
		return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
	}
	/**
	 * Function to list all datasets of a specific space
	 * @param  projCode identifier of the openBIS space
	 * @return list with all datasets of the given space
	 */
	public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfSpace(String space) {
		List<Sample> samples = getSamplesofSpace(space);
		ArrayList<String> ids = new ArrayList<String>(); 
		for (Iterator<Sample> iterator = samples.iterator(); iterator.hasNext();) {
			Sample s = (Sample) iterator.next();
			ids.add(s.getIdentifier());
		}
		if(ids.isEmpty()){
			return new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
		}
		return this.facade.listDataSetsForSamples(ids);
	}

	/**
	 * Function to list all datasets of a specific project
	 * @param  projCode identifier of the openBIS project
	 * @return list with all datasets of the given project
	 */
	public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProject(String projCode) {
		List<Sample> samps = getSamplesofProject(projCode);
		//System.out.println(samps.size());
		List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> res = new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
		for (Iterator<Sample> iterator = samps.iterator(); iterator.hasNext();) {
			Sample sample = (Sample) iterator.next();
			res.addAll(getDataSetsOfSample(sample.getIdentifier()));
		}
		return res;
	}
	
	// TODO ANOTHER WAY TO GET THE CORRECT DATASET TYPE ?
	/*
	public List<DataSet> getDataSetsOfProject(String projCode) {
		List<Sample> samps = getSamplesofProject(projCode);
		List<DataSet> res = new ArrayList<DataSet>();
		for (Iterator<Sample> iterator = samps.iterator(); iterator.hasNext();) {
			Sample sample = (Sample) iterator.next();
			res.addAll(getDataSetsOfSample(sample.getCode()));
		}
		return res;
	}
	*/
	/**
	 * Function to list all datasets of a specific type
	 * @param  type identifier of the openBIS type
	 * @return list with all datasets of the given type
	 */
	public List<DataSet> getDataSetsOfType(String type) {
		SearchCriteria sc = new SearchCriteria();
		sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
		return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
	}

	/**
	 * Function to list all attachments of a sample
	 * @param  project identifier of the openBIS sample
	 * @return list with all attachments connected to the given sample
	 */
	public List<Attachment> listAttachmentsForSample(String sample_id) {
		//System.out.println(this.openbisInfoService.listAttachmentsForSample(this.sessionToken, new SampleIdentifierId(sample_id), true));
		List<Attachment> attachments = new ArrayList<Attachment>();
		try {
			attachments = this.openbisInfoService.listAttachmentsForSample(this.sessionToken, new SampleIdentifierId(sample_id), true);
		}
		catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return attachments;

	}

	/**
	 * Function to list all attachments of a project
	 * @param  project identifier of the openBIS project
	 * @return list with all attachments connected to the given project
	 */
	public List<Attachment> listAttachmentsForProject(String project) {
		List<Project> foundProjects = facade.listProjects();
		String found_project = "";

		for (Project proj : foundProjects) {
			if (project.equals(proj.getCode())){
				found_project = proj.getIdentifier();
			}
		}

		return this.openbisInfoService.listAttachmentsForProject(this.sessionToken, new ProjectIdentifierId(found_project), true);

	}

	/**
	 * Function to add an attachment to a existing project in openBIS by
	 * calling the corresponding ingestion service of openBIS.
	 * @param  params  map with needed information for registration process
	 * @return nothing
	 */
	public void addAttachmentToProject(Map<String, Object> params) {
		System.out.println(this.openbisDssService.createReportFromAggregationService(this.sessionToken, "DSS1", "add-attachment", params));
	}

	
	/**
	 * Function to get all spaces which are registered in this openBIS instance
	 * @return list with the IDs of all available spaces
	 */
	public List<String> listSpaces() {
		List<SpaceWithProjectsAndRoleAssignments> spaces_roles = facade.getSpacesWithProjects();
		List<String> spaces = new ArrayList<String>();
		for(SpaceWithProjectsAndRoleAssignments sp: spaces_roles) {
			spaces.add(sp.getCode());
		}
		return spaces;
	}

	
	/**
	 * Function to get all projects which are registered in this openBIS instance
	 * @return list with all projects which are registered in this openBIS instance
	 */
	public List<Project> listProjects() {
		if(!this.loggedin()){
			this.login();
		}
		List<Project> projects = facade.listProjects();
		return projects;
	}

	
	/**
	 * Function to list all Experiments for a specific project which are registered in the openBIS
	 * instance.
	 * @param project the project for which the experiments should be listed
	 * @return list with all experiments registered in this openBIS instance
	 */	
	public List<Experiment> listExperimentsForProject(Project project) 
	{
		if(!this.loggedin()){
			this.login();
		}
		List<String> temp = new ArrayList<String>();
		temp.add(project.getIdentifier());
		List<Experiment> experiments = facade.listExperimentsForProjects(temp);
		return experiments;	
	}

	/**
	 * returns all users of a Space.
	 * @param code openbis code of the space
	 * @return set of strings
	 */
	public Set<String> getSpaceMembers(String code) {
		List<SpaceWithProjectsAndRoleAssignments> spaces = this.facade.getSpacesWithProjects();
		for(SpaceWithProjectsAndRoleAssignments space : spaces){
			if(space.getCode().equals(code)){
				return space.getUsers();
			}
		}
		return null;
	}

	/**
	 * Function to list all Experiments which are registered in the openBIS
	 * instance. 
	 * @return list with all experiments registered in this openBIS instance
	 */	
	public List<Experiment> listExperiments() 
	{
		List<String> spaces = this.listSpaces();
		List<Experiment> foundExps = new ArrayList<Experiment>();

		for(String s: spaces) {
			//this.getExperimentsofSpace(s)
			SearchCriteria sc = new SearchCriteria();
			sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, s));
			foundExps.addAll(this.openbisInfoService.searchForExperiments(this.sessionToken, sc));
		}

		return foundExps;
	}

	/**
	 * Function to retrieve all properties which have been assigned to a 
	 * specific entity type
	 * @param  type  entitiy type (experiment type, sample type,...)
	 * @return list of properties which are assigned to the entity type
	 */
	public List<PropertyType> listPropertiesForType(EntityType type)
	{
		List<PropertyType> property_types = new ArrayList<PropertyType>();
		List<PropertyTypeGroup> props = type.getPropertyTypeGroups();
		for (PropertyTypeGroup pg : props) {
			for (PropertyType prop_type : pg.getPropertyTypes())
			{
				property_types.add(prop_type);
			}
		}

		return property_types;
	}

	/**
	 * Function to list the vocabulary terms for a given property which has
	 * been added to openBIS. The property has to be a Controlled Vocabulary
	 * Property. 
	 * @param  prop  the property type
	 * @return      the vocabulary terms of the given property (vocab)
	 */
	public List<String> listVocabularyTermsForProperty(PropertyType prop) {
		List<String> terms = new ArrayList<String>();
		ControlledVocabularyPropertyType controlled_vocab = (ControlledVocabularyPropertyType) prop;
		for (VocabularyTerm term : controlled_vocab.getTerms()){
			terms.add(term.getLabel().toString());
		}

		return terms;
	}
	
	
	/**
	 * Function to add children samples to a sample (parent) using the corresponding ingestition service
	 * @param  params  map with needed information for registration process
	 * @param  name name of the service for the corresponding registration
	 * @return object name of the QueryTableModel which is returned by the aggregation service
	 */
	public String addParentChildConnection(Map<String, Object> params) {
		//System.out.println(params);
		return this.openbisDssService.createReportFromAggregationService(this.sessionToken, "DSS1", "create-parent-child", params).toString();
	}
	

	/**
	 * Function to trigger the registration of new openBIS instances like
	 * projects, experiments and samples. This function also  triggers the
	 * barcode generation for samples. 
	 * @param  params  map with needed information for registration process
	 * @param  name name of the service for the corresponding registration
	 * @param  number_of_samples_offset offset to generate correct barcodes 
	 * 		   (depending on number of samples) by accounting for delay of registration process
	 * @return object name of the QueryTableModel which is returned by the aggregation service
	 */
	public String addNewInstance(Map<String, Object> params, String service, int number_of_samples_offset) {
		@SuppressWarnings("unchecked")
		Map<String, String> properties = (Map<String, String>) params.get("properties");
		if ((params.get("properties") != null) && properties.get("QBIC_BARCODE") != null) {
			String barcode = generateBarcode("/" + params.get("space").toString() + "/" + params.get("project").toString(), number_of_samples_offset);
			properties.put("QBIC_BARCODE", barcode);
			params.put("code", barcode);
		}
		//System.out.println(params);
		return this.openbisDssService.createReportFromAggregationService(this.sessionToken, "DSS1", service, params).toString();
	}

	/**
	 * Function to create a QBiC barcode string for a sample based on the project
	 * ID. QBiC barcode format: Q + project_ID + sample number + X + checksum 
	 * @param  proj  ID of the project
	 * @return the QBiC barcode as string
	 */
	// TODO check if it works for all cases
	// TODO check for null ?
	public String generateBarcode(String proj, int number_of_samples_offset) {
		Project project = this.getProjectbyID(proj);
		//Project project = getProjectofExperiment(exp);
		int number_of_samples = getSamplesofProject(project.getCode()).size();
		//System.out.println(number_of_samples);
		
		String barcode = project.getCode() + String.format("%03d", (number_of_samples + 1)) + "S";
		//String barcode = project.getCode() + String.format("%03d", Math.max(1, number_of_samples + number_of_samples_offset)) + "S";
		barcode += checksum(barcode);
		return barcode;
	}
	
	/**
	 * Function map an integer value to a char
	 * @param  i the integer value which should be mapped
	 * @return the resulting char value
	 */
	public char mapToChar(int i) {
		i += 48;
		if (i > 57) {
			i += 7;
		}
		return (char) i;
	}

	/**
	 * Function to generate the checksum for the given barcode string
	 * @param  s  the barcode string
	 * @return the checksum for the given barcode
	 */
	public char checksum(String s) {
		int i = 1;
		int sum = 0;
		for (int idx = 0; idx <= s.length() - 1; idx++) {
			sum += (((int) s.charAt(idx)))*i;
			i += 1;
		}
		return mapToChar(sum % 34);
	}
	
	/**
	 * @throws MalformedURLException 
	 * Returns an download url for the openbis dataset with the given code and dataset_type.
	 * Throughs MalformedURLException if a url can not be created from the given parameters.
	 * NOTE: datastoreURL differs from serverURL only by the port -> quick hack used
	 * @param openbisCode
	 * @param openbisFilename
	 * @return
	 */
	
	public URL getDataStoreDownloadURL(String openbisCode, String openbisFilename) throws MalformedURLException{
	    String download_url = this.serverURL.substring(0,this.serverURL.length()-1);
	    download_url += "4";
	    download_url += "/datastore_server/";

	    download_url += openbisCode;
	    download_url += "/original/";
	    download_url += openbisFilename;
	    download_url += "?mode=simpleHtml&sessionID=";
	    download_url += this.getSessionToken();
	    return new URL(download_url);
		
	}
	
	public String getSessionToken(){
	    if(!this.openbisInfoService.isSessionActive(this.sessionToken)){
	        this.logout();
	        this.login();
	      }
	      return this.sessionToken;
	}	
	
}
