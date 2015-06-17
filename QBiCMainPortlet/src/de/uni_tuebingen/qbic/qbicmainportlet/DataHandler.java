package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import main.OpenBisClient;
import model.AggregationAdaptorBean;
import model.DatasetBean;
import model.ExperimentBean;
import model.ExperimentStatusBean;
import model.ExperimentType;
import model.NewIvacSampleBean;
import model.ProjectBean;
import model.SampleBean;
import parser.PersonParser;
import persons.Qperson;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.ProgressBar;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import de.uni_tuebingen.qbic.util.DashboardUtil;


public class DataHandler implements Serializable {


  /**
   * 
   */
  private static final long serialVersionUID = -4814000017404997233L;
  private logging.Logger LOGGER = new Log4j2Logger(DataHandler.class);
  
  // Map<String, SpaceInformation> spaces = new HashMap<String, SpaceInformation>();
  // Map<String, ProjectInformation> projectInformations = new HashMap<String,
  // ProjectInformation>();
  // Map<String, ExperimentInformation> experimentInformations =
  // new HashMap<String, ExperimentInformation>();
  // Map<String, SampleInformation> sampleInformations = new HashMap<String, SampleInformation>();
  Map<String, ProjectBean> projectMap = new HashMap<String, ProjectBean>();
  Map<String, ExperimentBean> experimentMap = new HashMap<String, ExperimentBean>();
  Map<String, SampleBean> sampleMap = new HashMap<String, SampleBean>();
  Map<String, DatasetBean> datasetMap = new HashMap<String, DatasetBean>();

  Map<String, String> spaceToProjectPrefixMap = new HashMap<String, String>();

  // Map<String, IndexedContainer> space_to_projects = new HashMap<String, IndexedContainer>();
  //
  // Map<String, IndexedContainer> space_to_experiments = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> project_to_experiments = new HashMap<String, IndexedContainer>();
  //
  // Map<String, IndexedContainer> space_to_samples = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> project_to_samples = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> experiment_to_samples = new HashMap<String, IndexedContainer>();

  // Map<String, HierarchicalContainer> space_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> project_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> experiment_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> sample_to_datasets =
  // new HashMap<String, HierarchicalContainer>();

  List<SpaceWithProjectsAndRoleAssignments> space_list = null;
  // Map<String, IndexedContainer> connectedPersons = new HashMap<String, IndexedContainer>();
  IndexedContainer connectedPersons = new IndexedContainer();

  public List<SpaceWithProjectsAndRoleAssignments> getSpacesWithProjectInformation() {
    if (space_list == null) {
      space_list = this.openBisClient.getFacade().getSpacesWithProjects();
    }
    return space_list;
  }

  OpenBisClient openBisClient;

  private Map<String, Project> dtoProjects = new HashMap<String, Project>();
  private Map<String, Experiment> dtoExperiments = new HashMap<String, Experiment>();

  public DataHandler(OpenBisClient client) {
    // reset(); //TODO useless?
    this.openBisClient = client;
  }


  // // id in this case meaning the openBIS instance ?!
  // public SpaceInformation getSpace(String identifier) throws Exception {
  //
  // List<SpaceWithProjectsAndRoleAssignments> space_list = null;
  // SpaceInformation spaces = null;
  //
  // if (this.spaces.get(identifier) != null) {
  // return this.spaces.get(identifier);
  // }
  //
  // else if (this.spaces.get(identifier) == null) {
  // space_list = this.getSpacesWithProjectInformation();
  // spaces = this.createSpaceContainer(space_list, identifier);
  //
  // this.spaces.put(identifier, spaces);
  // }
  //
  // else {
  // throw new Exception("Unknown Space: " + identifier + ". Method DataHandler::getSpace.");
  // }
  //
  // return spaces;
  //
  // }


  /**
   * 
   * @param dsCodes List of dataset codes
   * @return A list of DatasetBeans denoting the roots of the folder structure of each dataset.
   *         Subfolders and files can be reached by calling the getChildren() function on each Bean.
   */
  public List<DatasetBean> queryDatasetsForFolderStructure(
      List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> datasets) {
    Map<String, Object> params = new HashMap<String, Object>();
    List<String> dsCodes = new ArrayList<String>();
    Map<String, String> types = new HashMap<String, String>();

    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet ds : datasets) {
      dsCodes.add(ds.getCode());
      types.put(ds.getCode(), ds.getDataSetTypeCode());
    }

    params.put("codes", dsCodes);
    QueryTableModel res = openBisClient.getAggregationService("query-files", params);
  
    List<List<AggregationAdaptorBean>> beans = new ArrayList<List<AggregationAdaptorBean>>();
    String curDS = (String) res.getRows().get(0)[0];
    List<AggregationAdaptorBean> filesInDataset = new ArrayList<AggregationAdaptorBean>();
    for (Serializable[] ss : res.getRows()) {
      String ds = (String) ss[0];
      AggregationAdaptorBean b =
          new AggregationAdaptorBean(ds, (String) ss[1], (String) ss[2], (Long) ss[3],
              (String) ss[4], (String) ss[5]);
      if (!curDS.equals(ds)) {
        curDS = ds;
        beans.add(filesInDataset);
        filesInDataset = new ArrayList<AggregationAdaptorBean>();
      }
      filesInDataset.add(b);
    }
    beans.add(filesInDataset);
    List<DatasetBean> roots = new ArrayList<DatasetBean>();
  
    for (List<AggregationAdaptorBean> dataset : beans) {
      List<DatasetBean> lastLevel = new ArrayList<DatasetBean>();
      List<DatasetBean> curLevel = new ArrayList<DatasetBean>();
      String curFolder = dataset.get(0).getParent();
      for (AggregationAdaptorBean b : dataset) {
        DatasetBean newBean = new DatasetBean();
        newBean.setFileName(b.getName());
        newBean.setDssPath(b.getPath());
        newBean.setFileSize(b.getSize());
        newBean.setType(types.get(b.getDs()));
        

        // 2015-05-31 23:47:10 +0200
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
          date = formatter.parse(b.getLastmodified().split("\\+")[0]);
          System.out.println(date);
          System.out.println(formatter.format(date));

        } catch (ParseException e) {
          e.printStackTrace();
        }

        newBean.setCode(b.getDs());
        newBean.setRegistrationDate(date);
        if (!b.getParent().equals(curFolder)) {
          lastLevel = curLevel;
          curLevel = new ArrayList<DatasetBean>();
        }
        newBean.setChildren(lastLevel);
        curLevel.add(newBean);
      }
      roots.add(curLevel.get(curLevel.size() - 1));
    }
    return roots;

  }

  /**
   * Method to get Bean from either openbis identifier or openbis object. Checks if corresponding
   * bean is already stored in datahandler map.
   * 
   * @param
   * @return
   */
  public ProjectBean getProject(Object proj) {
    Project project;
    ProjectBean newProjectBean;
    // System.out.println(proj);
    // System.out.println(this.projectMap);

    if (proj instanceof Project) {
      project = (Project) proj;
      newProjectBean = this.createProjectBean(project);
      this.projectMap.put(newProjectBean.getId(), newProjectBean);
    } else {
      if (this.projectMap.get((String) proj) != null) {

        newProjectBean = this.projectMap.get(proj);
      } else {
        project = this.openBisClient.getProjectByIdentifier((String) proj);
        newProjectBean = this.createProjectBean(project);
        this.projectMap.put(newProjectBean.getId(), newProjectBean);
      }
    }
    return newProjectBean;
  }

  /**
   * Method to get Bean from either openbis identifier or openbis object. Checks if corresponding
   * bean is already stored in datahandler map.
   * 
   * @param
   * @return
   */
  public ProjectBean getProject2(String projectIdentifier) {
    List<Experiment> experiments = this.openBisClient.getExperimentsForProject2(projectIdentifier);
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> datasets =
        this.openBisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria(projectIdentifier);
    float projectStatus = this.openBisClient.computeProjectStatus(experiments);

    Project project = getOpenbisDtoProject(projectIdentifier);
    if (project == null) {
      project = openBisClient.getProjectByIdentifier(projectIdentifier);
      addOpenbisDtoProject(project);
    }
    ProjectBean newProjectBean = new ProjectBean();

    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(projectStatus);

    Date registrationDate = project.getRegistrationDetails().getRegistrationDate();

    newProjectBean.setId(project.getIdentifier());
    newProjectBean.setCode(project.getCode());
    String desc = project.getDescription();
    if (desc == null)
      desc = "";
    newProjectBean.setDescription(desc);
    newProjectBean.setRegistrationDate(registrationDate);
    newProjectBean.setProgress(progressBar);
    newProjectBean.setRegistrator(project.getRegistrationDetails().getUserId());
    newProjectBean.setContact(project.getRegistrationDetails().getUserEmail());

    BeanItemContainer<ExperimentBean> experimentBeans =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);

    for (Experiment experiment : experiments) {
      ExperimentBean newExperimentBean = new ExperimentBean();
      String status = "";

      Map<String, String> assignedProperties = experiment.getProperties();

      if (assignedProperties.keySet().contains("Q_CURRENT_STATUS")) {
        status = assignedProperties.get("Q_CURRENT_STATUS");
      }

      Image statusColor = new Image(status, this.setExperimentStatusColor(status));
      statusColor.setWidth("15px");
      statusColor.setHeight("15px");
      statusColor.setCaption(status);

      newExperimentBean.setId(experiment.getIdentifier());
      newExperimentBean.setCode(experiment.getCode());
      newExperimentBean.setType(experiment.getExperimentTypeCode());
      newExperimentBean.setStatus(statusColor);
      newExperimentBean.setRegistrator(experiment.getRegistrationDetails().getUserId());
      newExperimentBean.setRegistrationDate(experiment.getRegistrationDetails()
          .getRegistrationDate());
      experimentBeans.addBean(newExperimentBean);
    }

    newProjectBean.setContainsData(datasets.size() != 0);
    // for(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataset: datasets){
    // TODO use the datasets information!
    // }

    newProjectBean.setExperiments(experimentBeans);
    newProjectBean.setMembers(new HashSet<String>());
    return newProjectBean;
  }
  
  public ProjectBean getProjectIvac(String projectIdentifier){
    List<Experiment> experiments = this.openBisClient.getExperimentsForProject2(projectIdentifier);
    float projectStatus = this.openBisClient.computeProjectStatus(experiments);

    Project project = getOpenbisDtoProject(projectIdentifier);
    if (project == null) {
      project = openBisClient.getProjectByIdentifier(projectIdentifier);
      addOpenbisDtoProject(project);
    }
    ProjectBean newProjectBean = new ProjectBean();

    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(projectStatus);

    Date registrationDate = project.getRegistrationDetails().getRegistrationDate();

    newProjectBean.setId(project.getIdentifier());
    newProjectBean.setCode(project.getCode());
    String desc = project.getDescription();
    if (desc == null)
      desc = "";
    newProjectBean.setDescription(desc);
    newProjectBean.setRegistrationDate(registrationDate);
    newProjectBean.setProgress(progressBar);
    newProjectBean.setRegistrator(project.getRegistrationDetails().getUserId());
    newProjectBean.setContact(project.getRegistrationDetails().getUserEmail());

    BeanItemContainer<ExperimentBean> experimentBeans =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);

    for (Experiment experiment : experiments) {
      ExperimentBean newExperimentBean = new ExperimentBean();

      Map<String, String> assignedProperties = experiment.getProperties();


      newExperimentBean.setId(experiment.getIdentifier());
      newExperimentBean.setCode(experiment.getCode());
      newExperimentBean.setType(experiment.getExperimentTypeCode());
      newExperimentBean.setProperties(assignedProperties);
      newExperimentBean.setRegistrator(experiment.getRegistrationDetails().getUserId());
      newExperimentBean.setRegistrationDate(experiment.getRegistrationDetails()
          .getRegistrationDate());
      experimentBeans.addBean(newExperimentBean);
    }

    newProjectBean.setContainsData(false);


    newProjectBean.setExperiments(experimentBeans);
    newProjectBean.setMembers(new HashSet<String>());
    return newProjectBean;    
/*
    Project project = getOpenbisDtoProject(projectIdentifier);
    if (project == null) {
      project = openBisClient.getProjectByIdentifier(projectIdentifier);
      addOpenbisDtoProject(project);
    }
    ProjectBean newProjectBean = new ProjectBean();

    ProgressBar progressBar = new ProgressBar();
    newProjectBean.setProgress(progressBar);
    
    Date registrationDate = project.getRegistrationDetails().getRegistrationDate();

    newProjectBean.setId(project.getIdentifier());
    newProjectBean.setCode(project.getCode());
    String desc = project.getDescription();
    if (desc == null)
      desc = "";
    newProjectBean.setDescription(desc);
    newProjectBean.setRegistrationDate(registrationDate);
    
    newProjectBean.setRegistrator(project.getRegistrationDetails().getUserId());
    newProjectBean.setContact(project.getRegistrationDetails().getUserEmail());

    BeanItemContainer<ExperimentBean> experimentBeans =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    
    List<Experiment> experiments = this.openBisClient.getExperimentsForProject2(projectIdentifier);
    for (Experiment experiment : experiments) {
      ExperimentBean newExperimentBean = new ExperimentBean();


      newExperimentBean.setId(experiment.getIdentifier());
      newExperimentBean.setCode(experiment.getCode());
      newExperimentBean.setType(experiment.getExperimentTypeCode());

      newExperimentBean.setRegistrator(experiment.getRegistrationDetails().getUserId());
      newExperimentBean.setRegistrationDate(experiment.getRegistrationDetails()
          .getRegistrationDate());
      
      
      // Get all properties for metadata changing
      List<PropertyType> completeProperties =
          this.openBisClient.listPropertiesForType(this.openBisClient
              .getExperimentTypeByString(experiment.getExperimentTypeCode()));

      Map<String, String> properties = new HashMap<String, String>();
      Map<String, String> assignedProperties = experiment.getProperties();
      Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();

      for (PropertyType p : completeProperties) {

        // TODO no hardcoding

        if (p instanceof ControlledVocabularyPropertyType) {
          controlledVocabularies.put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
        }

        if (assignedProperties.keySet().contains(p.getCode())) {
          properties.put(p.getCode(), assignedProperties.get(p.getCode()));
        } else {
          properties.put(p.getCode(), "");
        }
      }
      
      newExperimentBean.setProperties(properties);
      newExperimentBean.setControlledVocabularies(controlledVocabularies);
      /*List<Sample> samples = openBisClient.getSamplesofExperiment(experiment.getIdentifier());
      BeanItemContainer<SampleBean> sampleBeans = new BeanItemContainer<SampleBean>(SampleBean.class);
      for(Sample sample: samples){
        SampleBean newSampleBean = new SampleBean();

        Map<String, String> sampleProperties = sample.getProperties();

        newSampleBean.setId(sample.getIdentifier());
        newSampleBean.setCode(sample.getCode());
        newSampleBean.setType(sample.getSampleTypeCode());
        newSampleBean.setProperties(sampleProperties);
        sampleBeans.addBean(newSampleBean);
      }
      newExperimentBean.setSamples(sampleBeans);/
      
      experimentBeans.addBean(newExperimentBean);
    }

    newProjectBean.setContainsData(false);
    // for(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataset: datasets){
    // TODO use the datasets information!
    // }

    newProjectBean.setExperiments(experimentBeans);
    newProjectBean.setMembers(new HashSet<String>());
    return newProjectBean;*/
  }


  public Project getOpenbisDtoProject(String projectIdentifier) {
    if (this.dtoProjects.containsKey(projectIdentifier)) {
      return this.dtoProjects.get(projectIdentifier);
    }
    return null;

  }

  
  public void addOpenbisDtoProject(Project project) {
    if(project != null && !dtoProjects.containsKey(project.getIdentifier())){
      this.dtoProjects.put(project.getIdentifier(), project);
    }
  }



  /**
   * Method to get Bean from either openbis identifier or openbis object. Checks if corresponding
   * bean is already stored in datahandler map.
   * 
   * @param
   * @return
   */
  public ExperimentBean getExperiment(Object exp) {
    Experiment experiment;
    ExperimentBean newExperimentBean;

    if (exp instanceof Experiment) {
      experiment = (Experiment) exp;
      newExperimentBean = this.createExperimentBean(experiment);
      this.experimentMap.put(newExperimentBean.getId(), newExperimentBean);
    }

    else {
      if (this.experimentMap.get((String) exp) != null) {
        newExperimentBean = this.experimentMap.get(exp);
      } else {
        experiment = this.openBisClient.getExperimentById((String) exp);
        newExperimentBean = this.createExperimentBean(experiment);
        this.experimentMap.put(newExperimentBean.getId(), newExperimentBean);
      }
    }


    return newExperimentBean;
  }


  public ExperimentBean getExperiment2(String expIdentifiers) {

    ExperimentBean ebean = new ExperimentBean();

    String status = "";
    List<Experiment> experiments = openBisClient.getExperimentById2(expIdentifiers);
    Experiment experiment = null;
    for (Experiment tmpexp : experiments) {
      if (tmpexp.getIdentifier().equals(expIdentifiers)) {
        experiment = tmpexp;
        break;
      }
    }
    if (experiment == null)
      throw new IllegalArgumentException(String.format("experiment Identifier %s does not exist",
          expIdentifiers));
    // Get all properties for metadata changing
    List<PropertyType> completeProperties =
        this.openBisClient.listPropertiesForType(this.openBisClient
            .getExperimentTypeByString(experiment.getExperimentTypeCode()));

    Map<String, String> assignedProperties = experiment.getProperties();
    Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();
    Map<String, String> properties = new HashMap<String, String>();

    if (assignedProperties.keySet().contains("Q_CURRENT_STATUS")) {
      status = assignedProperties.get("Q_CURRENT_STATUS");
    }

    for (PropertyType p : completeProperties) {

      // TODO no hardcoding

      if (p instanceof ControlledVocabularyPropertyType) {
        controlledVocabularies.put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
      }

      if (assignedProperties.keySet().contains(p.getCode())) {
        properties.put(p.getCode(), assignedProperties.get(p.getCode()));
      } else {
        properties.put(p.getCode(), "");
      }
    }

    Map<String, String> typeLabels =
        this.openBisClient.getLabelsofProperties(this.openBisClient
            .getExperimentTypeByString(experiment.getExperimentTypeCode()));

    Image statusColor = new Image(status, this.setExperimentStatusColor(status));
    statusColor.setWidth("15px");
    statusColor.setHeight("15px");

    ebean.setId(experiment.getIdentifier());
    ebean.setCode(experiment.getCode());
    ebean.setType(experiment.getExperimentTypeCode());
    ebean.setStatus(statusColor);
    ebean.setRegistrator(experiment.getRegistrationDetails().getUserId());
    ebean.setRegistrationDate(experiment.getRegistrationDetails().getRegistrationDate());
    ebean.setProperties(properties);
    ebean.setControlledVocabularies(controlledVocabularies);
    ebean.setTypeLabels(typeLabels);

    // TODO do we want to have that ? (last Changed)
    ebean.setLastChangedSample(null);
    ebean.setContainsData(this.openBisClient.getDataSetsOfExperimentByCodeWithSearchCriteria(
        experiment.getCode()).size() > 0);

    List<Sample> samples = this.openBisClient.getSamplesofExperiment(expIdentifiers);
    // Create sample Beans (or fetch them) for samples of experiment
    BeanItemContainer<SampleBean> sampleBeans = new BeanItemContainer<SampleBean>(SampleBean.class);
    for (Sample sample : samples) {
      SampleBean sbean = new SampleBean();
      sbean.setId(sample.getIdentifier());
      sbean.setCode(sample.getCode());
      sbean.setType(sample.getSampleTypeCode());
      /*
       * Map<String, String> sampleTypeLabels =
       * this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(sample
       * .getSampleTypeCode())); sbean.setTypeLabels(sampleTypeLabels);
       */

      sampleBeans.addBean(sbean);
    }
    ebean.setSamples(sampleBeans);

    return ebean;
  }


  public SampleBean getSample2(String sampleIdentifiers) {
    Sample sample = this.openBisClient.getSampleByIdentifier(sampleIdentifiers);
    SampleBean sbean = createSampleBean(sample);
    return sbean;
  }

  /**
   * Method to get Bean from either openbis identifier or openbis object. Checks if corresponding
   * bean is already stored in datahandler map.
   * 
   * @param
   * @return
   */
  public SampleBean getSample(Object samp) {
    Sample sample;
    SampleBean newSampleBean;

    if (samp instanceof Sample) {
      sample = (Sample) samp;
      newSampleBean = this.createSampleBean(sample);
      this.sampleMap.put(newSampleBean.getId(), newSampleBean);
    }

    else {
      if (this.sampleMap.get((String) samp) != null) {
        newSampleBean = this.sampleMap.get(samp);
      } else {
        sample = this.openBisClient.getSampleByIdentifier((String) samp);
        newSampleBean = this.createSampleBean(sample);
        this.sampleMap.put(newSampleBean.getId(), newSampleBean);
      }
    }

    return newSampleBean;
  }

  /**
   * Method to get Bean from either openbis identifier or openbis object. Checks if corresponding
   * bean is already stored in datahandler map.
   * 
   * @param
   * @return
   */
  public DatasetBean getDataset(Object ds) {
    DataSet dataset;
    DatasetBean newDatasetBean;

    if (ds instanceof DataSet) {
      dataset = (DataSet) ds;
      newDatasetBean = this.createDatasetBean(dataset);
    }

    else {
      if (this.datasetMap.get((String) ds) != null) {
        newDatasetBean = this.datasetMap.get(ds);
      } else {
        dataset = this.openBisClient.getFacade().getDataSet((String) ds);
        newDatasetBean = this.createDatasetBean(dataset);
      }
    }
    this.datasetMap.put(newDatasetBean.getCode(), newDatasetBean);
    return newDatasetBean;
  }


  /**
   * Returns all users of a Space.
   * 
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  private Set<String> getSpaceMembers(String spaceCode) {
    List<SpaceWithProjectsAndRoleAssignments> spaces = this.getSpacesWithProjectInformation();
    for (SpaceWithProjectsAndRoleAssignments space : spaces) {
      if (space.getCode().equals(spaceCode)) {
        return space.getUsers();
      }
    }
    return null;
  }

  /**
   * checks which of the datasets in the given list is the oldest and writes that into the last tree
   * parameters Note: lastModifiedDate, lastModifiedExperiment, lastModifiedSample will be modified.
   * if lastModifiedSample, lastModifiedExperiment have value N/A datasets have no registration
   * dates Params should not be null
   * 
   * @param datasets List of datasets that will be compared
   * @param lastModifiedDate will contain the last modified date
   * @param lastModifiedExperiment will contain experiment identifier, which contains last
   *        registered dataset
   * @param lastModifiedSample will contain last sample identifier, which contains last registered
   *        dataset, or null if dataset does not belong to a sample.
   */
  public void lastDatasetRegistered(List<DataSet> datasets, Date lastModifiedDate,
      StringBuilder lastModifiedExperiment, StringBuilder lastModifiedSample) {
    String exp = "N/A";
    String samp = "N/A";
    for (DataSet dataset : datasets) {
      Date date = dataset.getRegistrationDate();

      if (date.after(lastModifiedDate)) {
        samp = dataset.getSampleIdentifierOrNull();
        if (samp == null) {
          samp = "N/A";
        }
        exp = dataset.getExperimentIdentifier();
        lastModifiedDate.setTime(date.getTime());
        break;
      }
    }
    lastModifiedExperiment.append(exp);
    lastModifiedSample.append(samp);
  }

  // public void reset() {
  // // this.spaces = new HashMap<String,IndexedContainer>();
  // // this.projects = new HashMap<String,IndexedContainer>();
  // // this.experiments = new HashMap<String,IndexedContainer>();
  // // this.samples = new HashMap<String,IndexedContainer>();
  // this.space_to_datasets = new HashMap<String, HierarchicalContainer>();
  // }


  /**
   * This method filters out qbic staff and other unnecessary space members TODO: this method might
   * be better of as not being part of the DataHandler...and not hardcoded
   * 
   * @param users a set of all space users or members
   * @return a new set which exculdes qbic staff and functional members
   */
  private Set<String> removeQBiCStaffFromMemberSet(Set<String> users) {
    // TODO there is probably a method to get users of the QBIC group out of openBIS
    Set<String> ret = new LinkedHashSet<String>(users);
    ret.remove("iiswo01"); // QBiC Staff
    ret.remove("iisfr01"); // QBiC Staff
    ret.remove("kxmsn01"); // QBiC Staff
    ret.remove("zxmbf02"); // QBiC Staff
    ret.remove("qeana10"); // functional user
    ret.remove("etlserver"); // OpenBIS user
    ret.remove("admin"); // OpenBIS user
    ret.remove("QBIC"); // OpenBIS user
    ret.remove("sauron");
    // ret.remove("babysauron");
    return ret;
  }


  /**
   * Method create ProjectBean for project object
   * 
   * @param Project project
   * @return ProjectBean for corresponding project
   */

  ProjectBean createProjectBean(Project project) {

    ProjectBean newProjectBean = new ProjectBean();

    List<Experiment> experiments =
        this.openBisClient.getExperimentsOfProjectByIdentifier(project.getIdentifier());

    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(this.openBisClient.computeProjectStatus(project));

    Date registrationDate = project.getRegistrationDetails().getRegistrationDate();

    newProjectBean.setId(project.getIdentifier());
    newProjectBean.setCode(project.getCode());
    String desc = project.getDescription();
    if (desc == null)
      desc = "";
    newProjectBean.setDescription(desc);
    newProjectBean.setRegistrationDate(registrationDate);
    newProjectBean.setProgress(progressBar);
    newProjectBean.setRegistrator(project.getRegistrationDetails().getUserId());
    newProjectBean.setContact(project.getRegistrationDetails().getUserEmail());

    BeanItemContainer<ExperimentBean> experimentBeans =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);

    List<String> experiment_identifiers = new ArrayList<String>();

    for (Experiment experiment : experiments) {
      experimentBeans.addBean(this.getExperiment(experiment));
      experiment_identifiers.add(experiment.getIdentifier());
    }
    List<DataSet> datasets =
        (experiment_identifiers.size() > 0) ? openBisClient.getFacade().listDataSetsForExperiments(
            experiment_identifiers) : new ArrayList<DataSet>();
    newProjectBean.setContainsData(datasets.size() != 0);

    newProjectBean.setExperiments(experimentBeans);
    newProjectBean.setMembers(this.openBisClient.getSpaceMembers(project.getSpaceCode()));

    return newProjectBean;
  }


  /**
   * Method to create ExperimentBean for experiment object
   * 
   * @param Experiment experiment
   * @return ExperimentBean for corresponding experiment
   */
  ExperimentBean createExperimentBean(Experiment experiment) {

    ExperimentBean newExperimentBean = new ExperimentBean();
    List<Sample> samples = this.openBisClient.getSamplesofExperiment(experiment.getIdentifier());

    String status = "";

    // Get all properties for metadata changing
    List<PropertyType> completeProperties =
        this.openBisClient.listPropertiesForType(this.openBisClient
            .getExperimentTypeByString(experiment.getExperimentTypeCode()));

    Map<String, String> assignedProperties = experiment.getProperties();
    Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();
    Map<String, String> properties = new HashMap<String, String>();

    if (assignedProperties.keySet().contains("Q_CURRENT_STATUS")) {
      status = assignedProperties.get("Q_CURRENT_STATUS");
    }

    for (PropertyType p : completeProperties) {

      // TODO no hardcoding

      if (p instanceof ControlledVocabularyPropertyType) {
        controlledVocabularies.put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
      }

      if (assignedProperties.keySet().contains(p.getCode())) {
        properties.put(p.getCode(), assignedProperties.get(p.getCode()));
      } else {
        properties.put(p.getCode(), "");
      }
    }

    Map<String, String> typeLabels =
        this.openBisClient.getLabelsofProperties(this.openBisClient
            .getExperimentTypeByString(experiment.getExperimentTypeCode()));

    Image statusColor = new Image(status, this.setExperimentStatusColor(status));
    statusColor.setWidth("15px");
    statusColor.setHeight("15px");

    newExperimentBean.setId(experiment.getIdentifier());
    newExperimentBean.setCode(experiment.getCode());
    newExperimentBean.setType(experiment.getExperimentTypeCode());
    newExperimentBean.setStatus(statusColor);
    newExperimentBean.setRegistrator(experiment.getRegistrationDetails().getUserId());
    newExperimentBean
        .setRegistrationDate(experiment.getRegistrationDetails().getRegistrationDate());
    newExperimentBean.setProperties(properties);
    newExperimentBean.setControlledVocabularies(controlledVocabularies);
    newExperimentBean.setTypeLabels(typeLabels);

    // TODO do we want to have that ? (last Changed)
    newExperimentBean.setLastChangedSample(null);
    newExperimentBean.setContainsData(false);

    // Create sample Beans (or fetch them) for samples of experiment
    BeanItemContainer<SampleBean> sampleBeans = new BeanItemContainer<SampleBean>(SampleBean.class);
    for (Sample sample : samples) {
      SampleBean sbean = this.getSample(sample);
      if (sbean.getDatasets().size() > 0) {
        newExperimentBean.setContainsData(true);
      }
      sampleBeans.addBean(sbean);
    }
    newExperimentBean.setSamples(sampleBeans);

    return newExperimentBean;
  }


  /**
   * Method to create SampleBean for sample object
   * 
   * @param Sample sample
   * @return SampleBean for corresponding object
   */
  private SampleBean createSampleBean(Sample sample) {

    SampleBean newSampleBean = new SampleBean();

    Map<String, String> properties = sample.getProperties();

    newSampleBean.setId(sample.getIdentifier());
    newSampleBean.setCode(sample.getCode());
    newSampleBean.setType(sample.getSampleTypeCode());
    newSampleBean.setProperties(properties);
    newSampleBean.setParents(this.openBisClient.getParents(sample.getCode()));
    newSampleBean.setChildren(this.openBisClient.getFacade()
        .listSamplesOfSample(sample.getPermId()));

    BeanItemContainer<DatasetBean> datasetBeans =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    List<DataSet> datasets =
        this.openBisClient.getDataSetsOfSampleByIdentifier(sample.getIdentifier());

    Date lastModifiedDate = new Date();

    for (DataSet dataset : datasets) {
      DatasetBean datasetBean = this.getDataset(dataset);
      datasetBean.setSample(newSampleBean);
      datasetBeans.addBean(datasetBean);
      Date date = dataset.getRegistrationDate();
      if (date.after(lastModifiedDate)) {
        lastModifiedDate.setTime(date.getTime());
        break;
      }
    }

    newSampleBean.setDatasets(datasetBeans);
    newSampleBean.setLastChangedDataset(lastModifiedDate);

    Map<String, String> typeLabels =
        this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(sample
            .getSampleTypeCode()));
    newSampleBean.setTypeLabels(typeLabels);

    return newSampleBean;
  }


  /**
   * Method to create DatasetBean for dataset object
   * 
   * @param Dataset dataset
   * @return DatasetBean for corresponding object
   */
  private DatasetBean createDatasetBean(DataSet dataset) {

    DatasetBean newDatasetBean = new DatasetBean();
    FileInfoDssDTO[] filelist = dataset.listFiles("original", true);
    String download_link = filelist[0].getPathInDataSet();
    String[] splitted_link = download_link.split("/");
    String fileName = splitted_link[splitted_link.length - 1];
    newDatasetBean.setCode(dataset.getCode());
    newDatasetBean.setName(fileName);
    StringBuilder dssPath =
        new StringBuilder(dataset.getDataSetDss().tryGetInternalPathInDataStore());
    dssPath.append("/");
    dssPath.append(filelist[0].getPathInDataSet());
    newDatasetBean.setDssPath(dssPath.toString());
    newDatasetBean.setType(dataset.getDataSetTypeCode());
    newDatasetBean.setFileSize(filelist[0].getFileSize());
    // TODO
    // newDatasetBean.setRegistrator(registrator);
    newDatasetBean.setRegistrationDate(dataset.getRegistrationDate());

    newDatasetBean.setParent(null);
    newDatasetBean.setRoot(newDatasetBean);

    newDatasetBean.setSelected(false);


    if (filelist[0].isDirectory()) {
      newDatasetBean.setDirectory(filelist[0].isDirectory());
      String folderPath = filelist[0].getPathInDataSet();
      FileInfoDssDTO[] subList = dataset.listFiles(folderPath, false);
      datasetBeanChildren(newDatasetBean, subList, dataset);
    }

    // TODO
    // this.fileSize = fileSize;
    // this.humanReadableFileSize = humanReadableFileSize;
    // this.dssPath = dssPath;
    return newDatasetBean;
  }

  public void datasetBeanChildren(DatasetBean datasetBean, FileInfoDssDTO[] fileList, DataSet d) {
    ArrayList<DatasetBean> beans = new ArrayList<DatasetBean>();
    for (FileInfoDssDTO dto : fileList) {
      DatasetBean newBean = new DatasetBean();
      newBean.setCode(datasetBean.getCode());
      StringBuilder dssPath = new StringBuilder(datasetBean.getDssPath());
      dssPath.append("/");
      dssPath.append(dto.getPathInDataSet());
      newBean.setDssPath(dssPath.toString());
      newBean.setExperiment(datasetBean.getExperiment());
      String download_link = dto.getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      newBean.setFileName(splitted_link[splitted_link.length - 1]);
      newBean.setFileSize(dto.getFileSize());
      newBean.setFileType(d.getDataSetTypeCode());
      newBean
          .setHumanReadableFileSize(DashboardUtil.humanReadableByteCount(dto.getFileSize(), true));
      newBean.setParent(datasetBean);
      newBean.setProject(datasetBean.getProject());
      newBean.setRegistrationDate(datasetBean.getRegistrationDate());
      newBean.setRegistrator(datasetBean.getRegistrator());
      newBean.setRoot(datasetBean.getRoot());
      newBean.setSample(datasetBean.getSample());
      newBean.setSelected(datasetBean.getIsSelected().getValue());
      newBean.setDirectory(false);
      if (dto.isDirectory()) {
        newBean.setDirectory(true);
        String folderPath = dto.getPathInDataSet();
        FileInfoDssDTO[] subList = d.listFiles(folderPath, false);
        datasetBeanChildren(newBean, subList, d);
      }
      beans.add(newBean);

    }
    datasetBean.setChildren(beans);
  }

  /*
   * @SuppressWarnings("unchecked") private BeanItemContainer<ProjectBean>
   * createProjectContainer(List<Project> projs, String spaceID) throws Exception {
   * 
   * BeanItemContainer<ProjectBean> res = new BeanItemContainer<ProjectBean>(ProjectBean.class);
   * 
   * // project_container.addContainerProperty("Description", String.class, null); //
   * project_container.addContainerProperty("Space", String.class, null); //
   * project_container.addContainerProperty("Registration Date", Timestamp.class, null); //
   * project_container.addContainerProperty("Registrator", String.class, null); //
   * project_container.addContainerProperty("Progress", ProgressBar.class, null); SpaceBean space =
   * new SpaceBean(); space.setId(spaceID); // TODO do we need more space information at this point?
   * for (Project p : projs) { ProgressBar progressBar = new ProgressBar();
   * progressBar.setValue(this.openBisClient.computeProjectStatus(p)); Date date =
   * p.getRegistrationDetails().getRegistrationDate(); SimpleDateFormat sd = new
   * SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String dateString = sd.format(date); Timestamp ts =
   * Timestamp.valueOf(dateString); ProjectBean b = new ProjectBean(p.getIdentifier(), p.getCode(),
   * p.getDescription(), space, getExperiments(p.getIdentifier(), "project"), progressBar, ts, p
   * .getRegistrationDetails().getUserId(), p.getRegistrationDetails().getUserEmail(),
   * (List<String>) getSpaceMembers(spaceID), datasetMap.get(p.getIdentifier()) != null);
   * res.addBean(b); // Object new_p = project_container.addItem(); // // String code = p.getCode();
   * // String desc = p.getDescription(); // // String space = code.split("/")[1]; // // String
   * registrator = p.getRegistrationDetails().getUserId(); //
   * 
   * //
   * 
   * // // project_container.getContainerProperty(new_p, "Space").setValue(space); //
   * project_container.getContainerProperty(new_p, "Description").setValue(desc); //
   * project_container.getContainerProperty(new_p, "Registration Date").setValue(ts); //
   * project_container.getContainerProperty(new_p, "Registerator").setValue(registrator); //
   * project_container.getContainerProperty(new_p, "Progress").setValue(progressBar); }
   * 
   * return res; }
   */
  /*
   * @SuppressWarnings("unchecked") private BeanItemContainer<ExperimentBean>
   * createExperimentContainer(List<Experiment> exps, String projID) {
   * 
   * BeanItemContainer<ExperimentBean> res = new
   * BeanItemContainer<ExperimentBean>(ExperimentBean.class);
   * 
   * // project_container.addContainerProperty("Description", String.class, null); //
   * project_container.addContainerProperty("Space", String.class, null); //
   * project_container.addContainerProperty("Registration Date", Timestamp.class, null); //
   * project_container.addContainerProperty("Registrator", String.class, null); //
   * project_container.addContainerProperty("Progress", ProgressBar.class, null);
   * 
   * 
   * 
   * 
   * 
   * 
   * IndexedContainer experiment_container = new IndexedContainer();
   * 
   * experiment_container.addContainerProperty("Experiment", String.class, null);
   * experiment_container.addContainerProperty("Experiment Type", String.class, null);
   * experiment_container.addContainerProperty("Registration Date", Timestamp.class, null);
   * experiment_container.addContainerProperty("Registrator", String.class, null);
   * experiment_container.addContainerProperty("Status", Image.class, null); //
   * experiment_container.addContainerProperty("Properties", Map.class, null); ProjectBean space =
   * new ProjectBean(); space.setId(spaceID); // TODO do we need more space information at this
   * point? for (Experiment e : exps) { ProgressBar progressBar = new ProgressBar();
   * progressBar.setValue(this.openBisClient.computeProjectStatus(p)); Date date =
   * p.getRegistrationDetails().getRegistrationDate(); SimpleDateFormat sd = new
   * SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String dateString = sd.format(date); Timestamp ts =
   * Timestamp.valueOf(dateString); ExperimentBean e = new ExperimentBean(id, code, type, status,
   * registrator, project, registrationDate, samples, lastChangedSample, lastChangedDataset,
   * properties, controlledVocabularies) ProjectBean b = new ProjectBean(p.getIdentifier(),
   * p.getCode(), p.getDescription(), space, getExperiments(p.getIdentifier(), "project"),
   * progressBar, ts, p .getRegistrationDetails().getUserId(),
   * p.getRegistrationDetails().getUserEmail(), (List<String>) getSpaceMembers(spaceID),
   * datasetMap.get(p.getIdentifier()) != null); res.addBean(b);
   * 
   * 
   * 
   * 
   * for (Experiment e : exps) { Object new_ds = experiment_container.addItem();
   * 
   * String type = this.openBisClient.openBIScodeToString(e.getExperimentTypeCode());
   * 
   * Map<String, String> properties = e.getProperties();
   * 
   * String status = "";
   * 
   * if (properties.keySet().contains("Q_CURRENT_STATUS")) { status =
   * properties.get("Q_CURRENT_STATUS"); }
   * 
   * Date date = e.getRegistrationDetails().getRegistrationDate(); String registrator =
   * e.getRegistrationDetails().getUserId();
   * 
   * SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String dateString =
   * sd.format(date); Timestamp ts = Timestamp.valueOf(dateString);
   * experiment_container.getContainerProperty(new_ds, "Experiment").setValue(e.getCode());
   * experiment_container.getContainerProperty(new_ds, "Experiment Type").setValue(type);
   * experiment_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
   * experiment_container.getContainerProperty(new_ds, "Registrator").setValue(registrator);
   * 
   * Image statusColor = new Image(status, this.setExperimentStatusColor(status));
   * statusColor.setWidth("15px"); statusColor.setHeight("15px");
   * experiment_container.getContainerProperty(new_ds, "Status").setValue(statusColor); //
   * experiment_container.getContainerProperty(new_ds, // "Properties").setValue(e.getProperties());
   * }
   * 
   * return experiment_container; }
   */

  /*
   * beans.add(newBean);
   * 
   * @SuppressWarnings("unchecked") private BeanItemContainer<SampleBean>
   * createSampleContainer(List<Sample> samples, String id) {
   * 
   * IndexedContainer sample_container = new IndexedContainer();
   * sample_container.addContainerProperty("Sample", String.class, null);
   * sample_container.addContainerProperty("Description", String.class, null);
   * sample_container.addContainerProperty("Sample Type", String.class, null);
   * sample_container.addContainerProperty("Registration Date", Timestamp.class, null); //
   * sample_container.addContainerProperty("Species", String.class, null);
   * 
   * for (Sample s : samples) { Object new_ds = sample_container.addItem();
   * 
   * String type = this.openBisClient.openBIScodeToString(s.getSampleTypeCode());
   * 
   * Date date = s.getRegistrationDetails().getRegistrationDate(); Map<String, String> properties =
   * s.getProperties(); SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String
   * dateString = sd.format(date); Timestamp ts = Timestamp.valueOf(dateString);
   * sample_container.getContainerProperty(new_ds, "Sample").setValue(s.getCode()); //
   * sample_container.getContainerProperty(new_ds, //
   * "Description").setValue(properties.get("SAMPLE_CLASS"));
   * sample_container.getContainerProperty(new_ds, "Description").setValue(
   * properties.get("Q_SECONDARY_NAME")); sample_container.getContainerProperty(new_ds,
   * "Sample Type").setValue(type); sample_container.getContainerProperty(new_ds,
   * "Registration Date").setValue(ts); // sample_container.getContainerProperty(new_ds, //
   * "Species").setValue(properties.get("SPECIES")); }
   * 
   * return sample_container; }
   */
  /*
   * private BeanItemContainer<DatasetBean> createDatasetContainer(List<DataSet> datasets, String
   * id) {
   * 
   * HierarchicalContainer dataset_container = new HierarchicalContainer();
   * 
   * dataset_container.addContainerProperty("Select", CheckBox.class, null);
   * dataset_container.addContainerProperty("Project", String.class, null);
   * dataset_container.addContainerProperty("Sample", String.class, null);
   * dataset_container.addContainerProperty("Sample Type", String.class, null);
   * dataset_container.addContainerProperty("File Name", String.class, null);
   * dataset_container.addContainerProperty("File Type", String.class, null);
   * dataset_container.addContainerProperty("Dataset Type", String.class, null);
   * dataset_container.addContainerProperty("Registration Date", Timestamp.class, null);
   * dataset_container.addContainerProperty("Validated", Boolean.class, null);
   * dataset_container.addContainerProperty("File Size", String.class, null);
   * dataset_container.addContainerProperty("file_size_bytes", Long.class, null);
   * dataset_container.addContainerProperty("dl_link", String.class, null);
   * dataset_container.addContainerProperty("CODE", String.class, null);
   * 
   * for (DataSet d : datasets) { String identifier = d.getSampleIdentifierOrNull(); Sample
   * sampleObject = this.openBisClient.getSampleByIdentifier(identifier); String sample =
   * sampleObject.getCode(); String sampleType =
   * this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode(); Project projectObject =
   * this.openBisClient.getProjectOfExperimentByIdentifier(sampleObject
   * .getExperimentIdentifierOrNull()); String project = projectObject.getCode(); // String code =
   * d.getSampleIdentifierOrNull(); // String sample = code.split("/")[2]; // String project =
   * sample.substring(0, 5); Date date = d.getRegistrationDate();
   * 
   * SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String dateString =
   * sd.format(date); Timestamp ts = Timestamp.valueOf(dateString);
   * 
   * FileInfoDssDTO[] filelist = d.listFiles("original", true);
   * 
   * // recursive test registerDatasetInTable(d, filelist, dataset_container, project, sample, ts,
   * sampleType, null);
   * 
   * }
   * 
   * return dataset_container; }
   */

  public void registerDatasetInTable(DataSet d, FileInfoDssDTO[] filelist,
      HierarchicalContainer dataset_container, String project, String sample, Timestamp ts,
      String sampleType, Object parent) {
    if (filelist[0].isDirectory()) {

      Object new_ds = dataset_container.addItem();

      String folderPath = filelist[0].getPathInDataSet();
      FileInfoDssDTO[] subList = d.listFiles(folderPath, false);

      dataset_container.setChildrenAllowed(new_ds, true);
      String download_link = filelist[0].getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      String file_name = download_link.split("/")[splitted_link.length - 1];
      // System.out.println(file_name);

      dataset_container.getContainerProperty(new_ds, "Select").setValue(new CheckBox());

      dataset_container.getContainerProperty(new_ds, "Project").setValue(project);
      dataset_container.getContainerProperty(new_ds, "Sample").setValue(sample);
      dataset_container.getContainerProperty(new_ds, "Sample Type").setValue(
          this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode());
      dataset_container.getContainerProperty(new_ds, "File Name").setValue(file_name);
      dataset_container.getContainerProperty(new_ds, "File Type").setValue("Folder");
      dataset_container.getContainerProperty(new_ds, "Dataset Type").setValue("-");
      dataset_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_ds, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_ds, "dl_link").setValue(
          d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
      dataset_container.getContainerProperty(new_ds, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_ds, "file_size_bytes").setValue(
          filelist[0].getFileSize());

      // System.out.println("Now it should be a folder: " + filelist[0].getPathInDataSet());

      if (parent != null) {
        dataset_container.setParent(new_ds, parent);
      }

      for (FileInfoDssDTO file : subList) {
        FileInfoDssDTO[] childList = {file};
        registerDatasetInTable(d, childList, dataset_container, project, sample, ts, sampleType,
            new_ds);
      }

    } else {
      // System.out.println("Now it should be a file: " + filelist[0].getPathInDataSet());

      Object new_file = dataset_container.addItem();
      dataset_container.setChildrenAllowed(new_file, false);
      String download_link = filelist[0].getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      String file_name = download_link.split("/")[splitted_link.length - 1];
      // String file_name = download_link.split("/")[1];
      String fileSize = DashboardUtil.humanReadableByteCount(filelist[0].getFileSize(), true);

      dataset_container.getContainerProperty(new_file, "Select").setValue(new CheckBox());
      dataset_container.getContainerProperty(new_file, "Project").setValue(project);
      dataset_container.getContainerProperty(new_file, "Sample").setValue(sample);
      dataset_container.getContainerProperty(new_file, "Sample Type").setValue(sampleType);
      dataset_container.getContainerProperty(new_file, "File Name").setValue(file_name);
      dataset_container.getContainerProperty(new_file, "File Type")
          .setValue(d.getDataSetTypeCode());
      dataset_container.getContainerProperty(new_file, "Dataset Type").setValue(
          d.getDataSetTypeCode());
      dataset_container.getContainerProperty(new_file, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_file, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_file, "File Size").setValue(fileSize);
      dataset_container.getContainerProperty(new_file, "dl_link").setValue(
          d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
      dataset_container.getContainerProperty(new_file, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_file, "file_size_bytes").setValue(
          filelist[0].getFileSize());
      if (parent != null) {
        dataset_container.setParent(new_file, parent);
      }
    }
  }

  /**
   * Function to fill tree container and collect statistical information of spaces. Should replace
   * the two functions and be somewhat faster. Still not pretty. Needs work
   * 
   * @param tc HierarchicalContainer for the Tree
   * @param userName Screenname of the Liferay User
   * @return SpaceInformation object
   */
  // public SpaceInformation initTreeAndHomeInfo(HierarchicalContainer tc, String userName) {
  //
  // List<SpaceWithProjectsAndRoleAssignments> space_list = this.getSpacesWithProjectInformation();
  //
  // // Initialization of Tree Container
  // tc.addContainerProperty("identifier", String.class, "N/A");
  // tc.addContainerProperty("type", String.class, "N/A");
  // tc.addContainerProperty("project", String.class, "N/A");
  // tc.addContainerProperty("caption", String.class, "N/A");
  //
  //
  // // Initialization of Home Information
  // SpaceInformation homeInformation = new SpaceInformation();
  // IndexedContainer space_container = new IndexedContainer();
  // space_container.addContainerProperty("Project", String.class, "");
  // space_container.addContainerProperty("Description", String.class, "");
  // space_container.addContainerProperty("Contains datasets", String.class, "");
  // int number_of_projects = 0;
  // int number_of_experiments = 0;
  // int number_of_samples = 0;
  // int number_of_datasets = 0;
  // String lastModifiedExperiment = "N/A";
  // String lastModifiedSample = "N/A";
  // Date lastModifiedDate = new Date(0, 0, 0);
  // for (SpaceWithProjectsAndRoleAssignments s : space_list) {
  // if (s.getUsers().contains(userName)) {
  // String space_name = s.getCode();
  //
  // // TODO does this work for everyone? should it? empty container would be the aim, probably
  // if (space_name.equals("QBIC_USER_SPACE")) {
  // fillPersonsContainer(space_name);
  // }
  //
  // List<Project> projects = s.getProjects();
  // number_of_projects += projects.size();
  // List<String> project_identifiers_tmp = new ArrayList<String>();
  // for (Project project : projects) {
  //
  // String project_name = project.getCode();
  // if (tc.containsId(project_name)) {
  // project_name = project.getIdentifier();
  // }
  // Object new_s = space_container.addItem();
  // space_container.getContainerProperty(new_s, "Project").setValue(project_name);
  //
  // // Project descriptions can be long; truncate the string to provide a brief preview
  // String desc = project.getDescription();
  //
  // if (desc != null && desc.length() > 0) {
  // desc = desc.substring(0, Math.min(desc.length(), 100));
  // if (desc.length() == 100) {
  // desc += "...";
  // }
  // }
  // space_container.getContainerProperty(new_s, "Description").setValue(desc);
  //
  // // System.out.println("|--Project: " + project_name);
  // tc.addItem(project_name);
  //
  // tc.getContainerProperty(project_name, "type").setValue("project");
  // tc.getContainerProperty(project_name, "identifier").setValue(project_name);
  // tc.getContainerProperty(project_name, "project").setValue(project_name);
  // tc.getContainerProperty(project_name, "caption").setValue(project_name);
  //
  // List<Project> tmp_list = new ArrayList<Project>();
  // tmp_list.add(project);
  // List<Experiment> experiments =
  // this.openBisClient.getOpenbisInfoService().listExperiments(
  // this.openBisClient.getSessionToken(), tmp_list, null);
  //
  // // Add number of experiments for every project
  // number_of_experiments += experiments.size();
  //
  // List<String> experiment_identifiers = new ArrayList<String>();
  //
  // for (Experiment experiment : experiments) {
  // experiment_identifiers.add(experiment.getIdentifier());
  // String experiment_name = experiment.getCode();
  // if (tc.containsId(experiment_name)) {
  // experiment_name = experiment.getIdentifier();
  // }
  // // System.out.println(" |--Experiment: " + experiment_name);
  // tc.addItem(experiment_name);
  // tc.setParent(experiment_name, project_name);
  // tc.getContainerProperty(experiment_name, "type").setValue("experiment");
  // tc.getContainerProperty(experiment_name, "identifier").setValue(experiment_name);
  // tc.getContainerProperty(experiment_name, "project").setValue(project_name);
  // tc.getContainerProperty(experiment_name, "caption").setValue(
  // String.format("%s (%s)",
  // this.openBisClient.openBIScodeToString(experiment.getExperimentTypeCode()),
  // experiment_name));
  //
  // tc.setChildrenAllowed(experiment_name, false);
  // }
  // if (experiment_identifiers.size() > 0
  // && this.openBisClient.getFacade().listDataSetsForExperiments(experiment_identifiers)
  // .size() > 0) {
  // space_container.getContainerProperty(new_s, "Contains datasets").setValue("yes");
  // } else {
  // space_container.getContainerProperty(new_s, "Contains datasets").setValue("no");
  // }
  // }
  // List<Sample> samplesOfSpace = new ArrayList<Sample>();
  // if (project_identifiers_tmp.size() > 0) {
  // samplesOfSpace =
  // this.openBisClient.getFacade().listSamplesForProjects(project_identifiers_tmp);
  // } else {
  // samplesOfSpace = this.openBisClient.getSamplesofSpace(space_name); // TODO code or
  // // identifier
  // // needed?
  // }
  // number_of_samples += samplesOfSpace.size();
  // List<String> sample_identifiers_tmp = new ArrayList<String>();
  // for (Sample sa : samplesOfSpace) {
  // sample_identifiers_tmp.add(sa.getIdentifier());
  // }
  // List<DataSet> datasets = new ArrayList<DataSet>();
  // if (sample_identifiers_tmp.size() > 0) {
  // datasets = this.openBisClient.getFacade().listDataSetsForSamples(sample_identifiers_tmp);
  // }
  // number_of_datasets += datasets.size();
  // StringBuilder lce = new StringBuilder();
  // StringBuilder lcs = new StringBuilder();
  // this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
  // String tmplastModifiedExperiment = lce.toString();
  // String tmplastModifiedSample = lcs.toString();
  // if (!tmplastModifiedSample.equals("N/A")) {
  // lastModifiedExperiment = tmplastModifiedExperiment;
  // lastModifiedSample = tmplastModifiedSample;
  // }
  // }
  // }
  // homeInformation.numberOfProjects = number_of_projects;
  // homeInformation.numberOfExperiments = number_of_experiments;
  // homeInformation.numberOfSamples = number_of_samples;
  // homeInformation.numberOfDatasets = number_of_datasets;
  // homeInformation.lastChangedDataset = lastModifiedDate;
  // homeInformation.lastChangedSample = lastModifiedSample;
  // homeInformation.lastChangedExperiment = lastModifiedExperiment;
  // homeInformation.projects = space_container;
  //
  // return homeInformation;
  // }

  /**
   * Creates a Map of project statuses fulfilled, keyed by their meaning. For this, different steps
   * in the project flow are checked by looking at experiment types and data registered
   * 
   * @param project openBIS project
   * @return
   */
  public Map<String, Integer> computeProjectStatuses(ProjectBean projectBean) {

    // Project p = this.openBisClient.getProjectByCode(projectId);
    Map<String, Integer> res = new HashMap<String, Integer>();
    BeanItemContainer<ExperimentBean> cont = projectBean.getExperiments();

    // project was planned (otherwise it would hopefully not exist :) )
    res.put("Project Planned", 1);
    // design is pre-registered to the test sample level
    int prereg = 0;
    for (ExperimentBean bean : cont.getItemIds()) {
      String type = bean.getType();
      if (type.equals(this.openBisClient.openBIScodeToString(ExperimentType.Q_SAMPLE_PREPARATION
          .toString()))) {
        prereg = 1;
        break;
      }
    }
    res.put("Experimental Design registered", prereg);
    // data is uploaded
    // TODO fix that
    // if (datasetMap.get(p.getIdentifier()) != null)
    // res.put("Data Registered", 1);
    // else
    int dataregistered = projectBean.getContainsData() ? 1 : 0;
    res.put("Data Registered", dataregistered);
    return res;
  }

  public BeanItemContainer<ExperimentStatusBean> computeIvacPatientStatus(ProjectBean projectBean) {

    BeanItemContainer<ExperimentStatusBean> res =
        new BeanItemContainer<ExperimentStatusBean>(ExperimentStatusBean.class);
    BeanItemContainer<ExperimentBean> cont = projectBean.getExperiments();

    // TODO set download link and workflow triggering
    // TODO add immune monitoring, report generation, vaccine design

    ExperimentStatusBean barcode = new ExperimentStatusBean();
    barcode.setDescription("Barcode Generation");
    barcode.setStatus(1.0);

    ExperimentStatusBean ngsCall = new ExperimentStatusBean();
    ngsCall.setDescription("Variant Calling");
    ngsCall.setStatus(0.0);

    ExperimentStatusBean hlaType = new ExperimentStatusBean();
    hlaType.setDescription("HLA Typing");
    hlaType.setStatus(0.0);

    ExperimentStatusBean epitopePred = new ExperimentStatusBean();
    epitopePred.setDescription("Epitope Prediction");
    epitopePred.setStatus(0.0);

    for (ExperimentBean bean : cont.getItemIds()) {
      String type = bean.getType();
      Double experimentStatus = bean.getProperties().get("Q_CURRENT_STATUS") ==null?0.0:helpers.OpenBisFunctions.statusToDoubleValue(bean.getProperties()
          .get("Q_CURRENT_STATUS").toString());
      if (type.equalsIgnoreCase(ExperimentType.Q_NGS_MEASUREMENT.name())) {

        ExperimentStatusBean ngsMeasure = new ExperimentStatusBean();
        ngsMeasure.setDescription("NGS Sequencing");
        ngsMeasure.setStatus(0.0);
        ngsMeasure.setStatus(experimentStatus);
        ngsMeasure.setCode(bean.getCode());
        ngsMeasure.setIdentifier(bean.getId());

        res.addBean(ngsMeasure);
      }
      if (type.equalsIgnoreCase(ExperimentType.Q_NGS_VARIANT_CALLING.name())) {
        LOGGER.debug(bean.getCode());
        LOGGER.debug(bean.getId());
        LOGGER.debug(String.valueOf(experimentStatus));
        ngsCall.setStatus(experimentStatus);
        ngsCall.setCode(bean.getCode());
        ngsCall.setIdentifier(bean.getId());
      }
      if (type.equalsIgnoreCase(ExperimentType.Q_NGS_HLATYPING.name())) {
        hlaType.setStatus(experimentStatus);
        hlaType.setCode(bean.getCode());
        hlaType.setIdentifier(bean.getId());
      }
      if (type.equalsIgnoreCase(ExperimentType.Q_WF_NGS_EPITOPE_PREDICTION.name())) {
        epitopePred.setStatus(experimentStatus);
        epitopePred.setCode(bean.getCode());
        epitopePred.setIdentifier(bean.getId());
      }
    }

    res.addBean(barcode);
    res.addBean(ngsCall);
    res.addBean(hlaType);
    res.addBean(epitopePred);

    return res;
  }

  public ThemeResource setExperimentStatusColor(String status) {
    ThemeResource resource = null;
    if (status.equals("FINISHED")) {
      resource = new ThemeResource("green_light.png");
    } else if (status.equals("DELAYED")) {
      resource = new ThemeResource("yellow_light.png");
    } else if (status.equals("STARTED")) {
      resource = new ThemeResource("grey_light.png");
    } else if (status.equals("FAILED")) {
      resource = new ThemeResource("red_light.png");
    } else {
      resource = new ThemeResource("red_light.png");
    }

    // image.setWidth("15px");
    // image.setHeight("15px");\
    return resource;
  }

  // public String beanContainerToString(BeanItemContainer c) {
  // String header = "";
  // for (Object o : c.getContainerPropertyIds())
  // header += o.toString() + "\t";
  // for (c.get)
  // }


  public List<Qperson> parseConnectedPeopleInformation(String xmlString) {
    PersonParser xmlParser = new PersonParser();
    List<Qperson> xmlPersons = null;
    try {
      xmlPersons = xmlParser.getPersonsFromXML(xmlString);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return xmlPersons;
  }

  public void fillPersonsContainer(String spaceIdentifier) {
    List<Sample> samplesOfSpace = new ArrayList<Sample>();
    samplesOfSpace = this.openBisClient.getSamplesofSpace(spaceIdentifier);

    if (this.connectedPersons.size() == 0) {
      for (PropertyType p : this.openBisClient.listPropertiesForType(this.openBisClient
          .getSampleTypeByString(("Q_USER")))) {
        this.connectedPersons.addContainerProperty(p.getLabel(), String.class, null);
      }
      this.connectedPersons.addContainerProperty("Project", String.class, null);
    }

    for (Sample s : samplesOfSpace) {
      List<Sample> parents = this.openBisClient.getParents(s.getCode());
      Map<String, String> labelMap =
          this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(s
              .getSampleTypeCode()));

      for (Sample parent : parents) {
        Object newPerson = this.connectedPersons.addItem();
        Iterator it = s.getProperties().entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          this.connectedPersons.getContainerProperty(newPerson, labelMap.get(pairs.getKey()))
              .setValue(pairs.getValue());
        }
        this.connectedPersons.getContainerProperty(newPerson, "Project").setValue(
            this.openBisClient
                .getProjectOfExperimentByIdentifier(parent.getExperimentIdentifierOrNull())
                .getCode().toString());

      }
    }
  }

  public void registerNewPatients(int numberPatients, List<String> secondaryNames,
      BeanItemContainer<NewIvacSampleBean> samplesToRegister, String space, String description,
      Map<String, List<String>> hlaTyping) {

    // get prefix code for projects for corresponding space
    String projectPrefix = model.spaceToProjectPrefixMap.myMap.get(space);

    // extract to function for that
    List<Integer> projectCodes = new ArrayList<Integer>();
    for (Project p : openBisClient.getProjectsOfSpace(space)) {
      // String maxValue = Collections.max(p.getCode());
      String maxValue = p.getCode().replaceAll("\\D+", "");
      int codeAsNumber = Integer.parseInt(maxValue);
      projectCodes.add(codeAsNumber);
    }

    int numberOfProject;

    if (projectCodes.size() == 0) {
      numberOfProject = 0;
    } else {
      numberOfProject = Collections.max(projectCodes);
    }

    for (int i = 0; i < numberPatients; i++) {
      Map<String, Object> projectMap = new HashMap<String, Object>();
      Map<String, Object> firstLevel = new HashMap<String, Object>();

      numberOfProject += 1;
      int numberOfRegisteredExperiments = 1;
      int numberOfRegisteredSamples = 1;

      // register new patient (project)
      String newProjectCode = projectPrefix + Utils.createCountString(numberOfProject, 3);

      projectMap.put("code", newProjectCode);
      projectMap.put("space", space);
      projectMap.put("desc", description + " [" + secondaryNames.get(i) + "]");
      projectMap.put("user", LiferayAndVaadinUtils.getUser().getScreenName());

      // call of ingestion service to register project
      this.openBisClient.triggerIngestionService("register-proj", projectMap);
      helpers.Utils.printMapContent(projectMap);

      String newProjectDetailsCode =
          projectPrefix + Utils.createCountString(numberOfProject, 3) + "E_INFO";
      String newProjectDetailsID = "/" + space + "/" + newProjectCode + "/" + newProjectDetailsCode;

      String newExperimentalDesignCode =
          projectPrefix + Utils.createCountString(numberOfProject, 3) + "E"
              + numberOfRegisteredExperiments;
      String newExperimentalDesignID =
          "/" + space + "/" + newProjectCode + "/" + newExperimentalDesignCode;
      numberOfRegisteredExperiments += 1;

      String newBiologicalEntitiyCode =
          newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "H";
      String newBiologicalEntitiyID =
          "/" + space + "/" + newBiologicalEntitiyCode
              + helpers.BarcodeFunctions.checksum(newBiologicalEntitiyCode);
      numberOfRegisteredSamples += 1;

      // register first level of new patient
      firstLevel.put("lvl", "1");
      firstLevel.put("projectDetails", newProjectDetailsID);
      firstLevel.put("experimentalDesign", newExperimentalDesignID);
      firstLevel.put("secondaryName", secondaryNames.get(i));
      firstLevel.put("biologicalEntity", newBiologicalEntitiyID);
      firstLevel.put("user", LiferayAndVaadinUtils.getUser().getScreenName());

      this.openBisClient.triggerIngestionService("register-ivac-lvl", firstLevel);
      System.out.println("Level 1: ");

      helpers.Utils.printMapContent(firstLevel);

      Map<String, Object> fithLevel = new HashMap<String, Object>();

      List<String> newHLATypingIDs = new ArrayList<String>();
      List<String> newHLATypingSampleIDs = new ArrayList<String>();
      List<String> hlaClasses = new ArrayList<String>();
      List<String> typings = new ArrayList<String>();
      List<String> typingMethods = new ArrayList<String>();

      // TODO choose parent sample for hlaTyping
      String parentHLA = "";


      for (Iterator iter = samplesToRegister.getItemIds().iterator(); iter.hasNext();) {

        NewIvacSampleBean sampleBean = (NewIvacSampleBean) iter.next();

        for (int ii = 1; ii <= sampleBean.getAmount(); ii++) {
          Map<String, Object> secondLevel = new HashMap<String, Object>();
          Map<String, Object> thirdLevel = new HashMap<String, Object>();
          Map<String, Object> fourthLevel = new HashMap<String, Object>();

          List<String> newSamplePreparationIDs = new ArrayList<String>();
          List<String> newTestSampleIDs = new ArrayList<String>();
          List<String> testTypes = new ArrayList<String>();

          List<String> newNGSMeasurementIDs = new ArrayList<String>();
          List<String> newNGSRunIDs = new ArrayList<String>();
          List<Boolean> additionalInfo = new ArrayList<Boolean>();
          List<String> parents = new ArrayList<String>();

          List<String> newSampleExtractionIDs = new ArrayList<String>();
          List<String> newBiologicalSampleIDs = new ArrayList<String>();
          List<String> primaryTissues = new ArrayList<String>();
          List<String> detailedTissue = new ArrayList<String>();
          List<String> sequencerDevice = new ArrayList<String>();

          String newSampleExtractionCode = newProjectCode + "E" + numberOfRegisteredExperiments;
          newSampleExtractionIDs.add("/" + space + "/" + newProjectCode + "/"
              + newSampleExtractionCode);
          numberOfRegisteredExperiments += 1;

          String newBiologicalSampleCode =
              newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "B";
          String newBiologicalSampleID =
              "/" + space + "/" + newBiologicalSampleCode
                  + helpers.BarcodeFunctions.checksum(newBiologicalSampleCode);

          parentHLA = newBiologicalSampleID;

          newBiologicalSampleIDs.add(newBiologicalSampleID);
          System.out.println(numberOfRegisteredSamples);
          numberOfRegisteredSamples += 1;

          primaryTissues.add(sampleBean.getTissue());
          detailedTissue.add(sampleBean.getType());

          // register second level of new patient
          secondLevel.put("lvl", "2");
          secondLevel.put("sampleExtraction", newSampleExtractionIDs);
          secondLevel.put("biologicalSamples", newBiologicalSampleIDs);
          secondLevel.put("secondaryNames", secondaryNames.get(i));
          secondLevel.put("parent", newBiologicalEntitiyID);
          secondLevel.put("primaryTissue", primaryTissues);
          secondLevel.put("detailedTissue", detailedTissue);
          secondLevel.put("user", LiferayAndVaadinUtils.getUser().getScreenName());

          this.openBisClient.triggerIngestionService("register-ivac-lvl", secondLevel);
          System.out.println("Level 2: ");
          helpers.Utils.printMapContent(secondLevel);

          if (sampleBean.getDnaSeq()) {
            String newSamplePreparationCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newSamplePreparationID =
                "/" + space + "/" + newProjectCode + "/" + newSamplePreparationCode;
            newSamplePreparationIDs.add(newSamplePreparationID);
            numberOfRegisteredExperiments += 1;

            String newTestSampleCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "B";
            String newTestSampleID =
                "/" + space + "/" + newTestSampleCode
                    + helpers.BarcodeFunctions.checksum(newTestSampleCode);
            newTestSampleIDs.add(newTestSampleID);
            numberOfRegisteredSamples += 1;
            testTypes.add("DNA");

            String newNGSMeasurementCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newNGSMeasurementID =
                "/" + space + "/" + newProjectCode + "/" + newNGSMeasurementCode;
            newNGSMeasurementIDs.add(newNGSMeasurementID);
            numberOfRegisteredExperiments += 1;

            String newNGSRunCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "R";
            String newNGSRunID =
                "/" + space + "/" + newNGSRunCode
                    + helpers.BarcodeFunctions.checksum(newNGSRunCode);
            newNGSRunIDs.add(newNGSRunID);
            numberOfRegisteredSamples += 1;

            additionalInfo.add(false);
            sequencerDevice.add(sampleBean.getSeqDevice());
            parents.add(newTestSampleID);

          }

          if (sampleBean.getRnaSeq()) {
            String newSamplePreparationCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newSamplePreparationID =
                "/" + space + "/" + newProjectCode + "/" + newSamplePreparationCode;
            newSamplePreparationIDs.add(newSamplePreparationID);
            numberOfRegisteredExperiments += 1;

            String newTestSampleCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "B";
            String newTestSampleID =
                "/" + space + "/" + newTestSampleCode
                    + helpers.BarcodeFunctions.checksum(newTestSampleCode);
            newTestSampleIDs.add(newTestSampleID);
            numberOfRegisteredSamples += 1;
            testTypes.add("RNA");

            String newNGSMeasurementCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newNGSMeasurementID =
                "/" + space + "/" + newProjectCode + "/" + newNGSMeasurementCode;
            newNGSMeasurementIDs.add(newNGSMeasurementID);
            numberOfRegisteredExperiments += 1;

            String newNGSRunCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "R";
            String newNGSRunID =
                "/" + space + "/" + newNGSRunCode
                    + helpers.BarcodeFunctions.checksum(newNGSRunCode);
            newNGSRunIDs.add(newNGSRunID);
            numberOfRegisteredSamples += 1;

            additionalInfo.add(false);
            sequencerDevice.add(sampleBean.getSeqDevice());
            parents.add(newTestSampleID);
          }

          if (sampleBean.getDeepSeq()) {
            String newSamplePreparationCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newSamplePreparationID =
                "/" + space + "/" + newProjectCode + "/" + newSamplePreparationCode;
            newSamplePreparationIDs.add(newSamplePreparationID);
            numberOfRegisteredExperiments += 1;

            String newTestSampleCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "B";
            String newTestSampleID =
                "/" + space + "/" + newTestSampleCode
                    + helpers.BarcodeFunctions.checksum(newTestSampleCode);
            newTestSampleIDs.add(newTestSampleID);
            numberOfRegisteredSamples += 1;
            testTypes.add("DNA");

            String newNGSMeasurementCode = newProjectCode + "E" + numberOfRegisteredExperiments;
            String newNGSMeasurementID =
                "/" + space + "/" + newProjectCode + "/" + newNGSMeasurementCode;
            newNGSMeasurementIDs.add(newNGSMeasurementID);
            numberOfRegisteredExperiments += 1;

            String newNGSRunCode =
                newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "R";
            String newNGSRunID =
                "/" + space + "/" + newNGSRunCode
                    + helpers.BarcodeFunctions.checksum(newNGSRunCode);
            newNGSRunIDs.add(newNGSRunID);
            numberOfRegisteredSamples += 1;

            additionalInfo.add(true);
            sequencerDevice.add(sampleBean.getSeqDevice());
            parents.add(newTestSampleID);
          }

          // register third and fourth level of new patient
          thirdLevel.put("lvl", "3");
          thirdLevel.put("parent", newBiologicalSampleID);
          thirdLevel.put("experiments", newSamplePreparationIDs);
          thirdLevel.put("samples", newTestSampleIDs);
          thirdLevel.put("types", testTypes);
          thirdLevel.put("user", LiferayAndVaadinUtils.getUser().getScreenName());

          fourthLevel.put("lvl", "4");
          fourthLevel.put("experiments", newNGSMeasurementIDs);
          fourthLevel.put("samples", newNGSRunIDs);
          fourthLevel.put("parents", parents);
          fourthLevel.put("types", testTypes);
          fourthLevel.put("info", additionalInfo);
          fourthLevel.put("device", sequencerDevice);
          fourthLevel.put("user", LiferayAndVaadinUtils.getUser().getScreenName());

          // TODO additional level for HLA typing

          // call of ingestion services for differeny levels
          System.out.println("Level 3: ");
          helpers.Utils.printMapContent(thirdLevel);
          System.out.println("Level 4: ");
          helpers.Utils.printMapContent(fourthLevel);
          this.openBisClient.triggerIngestionService("register-ivac-lvl", thirdLevel);
          this.openBisClient.triggerIngestionService("register-ivac-lvl", fourthLevel);
        }
      }

      for (Map.Entry<String, List<String>> entry : hlaTyping.entrySet()) {

        String newHLATyping = newProjectCode + "E" + numberOfRegisteredExperiments;

        newHLATypingIDs.add("/" + space + "/" + newProjectCode + "/" + newHLATyping);

        numberOfRegisteredExperiments += 1;

        String newHLATypingSampleCode =
            newProjectCode + Utils.createCountString(numberOfRegisteredSamples, 3) + "H";

        String newHLATypingSampleID =
            "/" + space + "/" + newHLATypingSampleCode
                + helpers.BarcodeFunctions.checksum(newHLATypingSampleCode);

        newHLATypingSampleIDs.add(newHLATypingSampleID);
        System.out.println(numberOfRegisteredSamples);
        numberOfRegisteredSamples += 1;

        hlaClasses.add(entry.getKey());
        typings.add(entry.getValue().get(0));
        typingMethods.add(entry.getValue().get(1));
      }

      fithLevel.put("lvl", "5");
      fithLevel.put("experiments", newHLATypingIDs);
      fithLevel.put("samples", newHLATypingSampleIDs);
      fithLevel.put("typings", typings);
      fithLevel.put("classes", hlaClasses);
      fithLevel.put("methods", typingMethods);
      fithLevel.put("parent", parentHLA);

      this.openBisClient.triggerIngestionService("register-ivac-lvl", fithLevel);

    }
  }
}
