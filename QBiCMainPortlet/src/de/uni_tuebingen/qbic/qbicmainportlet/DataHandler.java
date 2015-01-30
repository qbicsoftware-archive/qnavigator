package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import model.ExperimentType;

import parser.Parser;
import parser.PersonParser;
import persons.Qperson;
import properties.Qproperties;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.ProgressBar;

import de.uni_tuebingen.qbic.util.DashboardUtil;

class SpaceInformation {
  public int numberOfProjects;
  public int numberOfExperiments;
  public int numberOfSamples;
  public int numberOfDatasets;
  public String lastChangedExperiment;
  public String lastChangedSample;
  public Date lastChangedDataset;
  public IndexedContainer projects;
  public Set<String> members;

  public String toString() {
    return String.format(
        "#Projects: %s, #Exp, %s, #Samples %s, #Datasets %s, #containeritems %d, members: %s",
        numberOfProjects, numberOfExperiments, numberOfSamples, numberOfDatasets, projects.size(),
        members.toString());

  }
}


class ProjectInformation {
  public IndexedContainer experiments;
  public int numberOfExperiments;
  public int numberOfSamples;
  public int numberOfDatasets;
  public String lastChangedExperiment;
  public String lastChangedSample;
  public Date lastChangedDataset;
  public String description;
  public String statusMessage;
  public ProgressBar progressBar;
  public String contact;
  public Set<String> members;
}


class ExperimentInformation {

  public String experimentType;
  public int numberOfSamples;
  public int numberOfDatasets;
  public String lastChangedSample;
  public Date lastChangedDataset;
  public IndexedContainer samples;
  public Map<String, String> properties;
  public String propertiesFormattedString;
  public Map<String, List<String>> controlledVocabularies;
  public String identifier;
}


class SampleInformation {

  public String sampleType;
  public int numberOfDatasets;
  public Date lastChangedDataset;
  public HierarchicalContainer datasets;
  public Map<String, String> properties;
  public String propertiesFormattedString;
  // Map containing parents of the sample and the corresponding sample types
  public Map<String, String> parents;
  public String parentsFormattedString;
  public String xmlPropertiesFormattedString;
}


public class DataHandler {


  Map<String, SpaceInformation> spaces = new HashMap<String, SpaceInformation>();
  Map<String, ProjectInformation> projectInformations = new HashMap<String, ProjectInformation>();
  Map<String, ExperimentInformation> experimentInformations =
      new HashMap<String, ExperimentInformation>();
  Map<String, SampleInformation> sampleInformations = new HashMap<String, SampleInformation>();

  Map<String, IndexedContainer> space_to_projects = new HashMap<String, IndexedContainer>();

  Map<String, IndexedContainer> space_to_experiments = new HashMap<String, IndexedContainer>();
  Map<String, IndexedContainer> project_to_experiments = new HashMap<String, IndexedContainer>();

  Map<String, IndexedContainer> space_to_samples = new HashMap<String, IndexedContainer>();
  Map<String, IndexedContainer> project_to_samples = new HashMap<String, IndexedContainer>();
  Map<String, IndexedContainer> experiment_to_samples = new HashMap<String, IndexedContainer>();

  Map<String, HierarchicalContainer> space_to_datasets =
      new HashMap<String, HierarchicalContainer>();
  Map<String, HierarchicalContainer> project_to_datasets =
      new HashMap<String, HierarchicalContainer>();
  Map<String, HierarchicalContainer> experiment_to_datasets =
      new HashMap<String, HierarchicalContainer>();
  Map<String, HierarchicalContainer> sample_to_datasets =
      new HashMap<String, HierarchicalContainer>();

  List<SpaceWithProjectsAndRoleAssignments> space_list = null;
  // Map<String, IndexedContainer> connectedPersons = new HashMap<String, IndexedContainer>();
  IndexedContainer connectedPersons = new IndexedContainer();

  public List<SpaceWithProjectsAndRoleAssignments> getSpace_list() {
    if (space_list == null) {
      space_list = this.openBisClient.getFacade().getSpacesWithProjects();
    }
    return space_list;
  }

  OpenBisClient openBisClient;


  public DataHandler(OpenBisClient client) {
    reset();
    this.openBisClient = client;
    // initConnection();
  }

  /**
   * returns a SpaceInformation that contains statistics about all spaces, as if all of them would
   * be one big space.
   * 
   * @return
   */
  public SpaceInformation getHomeInformation(String userScreenName) {

    List<SpaceWithProjectsAndRoleAssignments> space_list = this.getSpace_list();
    SpaceInformation homeInformation = new SpaceInformation();
    IndexedContainer space_container = new IndexedContainer();
    space_container.addContainerProperty("Project", String.class, "");
    space_container.addContainerProperty("Description", String.class, "");
    // space_container.addContainerProperty("Number of Samples", String.class, "");
    space_container.addContainerProperty("Contains datasets", String.class, "");
    // space_container.addContainerProperty("Number of Experiments", String.class, "");
    int number_of_samples = 0;
    int number_of_projects = 0;
    int number_of_experiments = 0;
    int number_of_datasets = 0;
    String lastModifiedExperiment = "N/A";
    String lastModifiedSample = "N/A";
    Date lastModifiedDate = new Date(0, 0, 0);
    long endTime = 0;
    for (SpaceWithProjectsAndRoleAssignments space : space_list) {
      if (!space.getUsers().contains(userScreenName)) {
        continue;
      }
      String spaceIdentifier = space.getCode();

      long start = System.currentTimeMillis();
      List<Experiment> experiments_tmp = this.openBisClient.getExperimentsOfSpace(spaceIdentifier);

      List<Project> projects = space.getProjects();

      if (spaceIdentifier.equals("QBIC_USER_SPACE")) {
        fillPersonsContainer(spaceIdentifier);
      }
      number_of_experiments += experiments_tmp.size();
      // number_of_experiments +=
      // this.openBisClient.getExperimentsOfSpace(spaceIdentifier).size();//
      // this.openBisClient.openbisInfoService.listExperiments(this.openBisClient.getSessionToken(),
      // projects,
      // null);
      // List<Sample> samplesOfSpace = this.openBisClient.getSamplesofSpace(spaceIdentifier);//
      // this.openBisClient.facade.listSamplesForProjects(tmp_list_str);
      List<Sample> samplesOfSpace = new ArrayList<Sample>();
      List<String> project_identifiers_tmp = new ArrayList<String>();
      for (Project p : projects) {
        project_identifiers_tmp.add(p.getIdentifier());
      }

      if (project_identifiers_tmp.size() > 0) {
        samplesOfSpace = this.openBisClient.facade.listSamplesForProjects(project_identifiers_tmp);
      } else {
        samplesOfSpace = this.openBisClient.getSamplesofSpace(spaceIdentifier);
      }
      // samplesOfSpace = this.openBisClient.getSamplesofSpace(spaceIdentifier);
      number_of_samples += samplesOfSpace.size();
      List<String> sample_identifiers_tmp = new ArrayList<String>();
      for (Sample s : samplesOfSpace) {
        sample_identifiers_tmp.add(s.getIdentifier());
      }
      // List<DataSet> datasets =
      // this.openBisClient.getDataSetsOfSpaceByIdentifier(spaceIdentifier);//
      // this.openBisClient.facade.listDataSetsForExperiments(tmp_experiment_identifier_lis);
      List<DataSet> datasets = new ArrayList<DataSet>();
      if (sample_identifiers_tmp.size() > 0) {
        datasets = this.openBisClient.facade.listDataSetsForSamples(sample_identifiers_tmp);
      }
      long end = System.currentTimeMillis();
      System.out.println("Took " + (end - start) / 1000.0);

      number_of_datasets += datasets.size();
      StringBuilder lce = new StringBuilder();
      StringBuilder lcs = new StringBuilder();
      this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
      String tmplastModifiedExperiment = lce.toString();
      String tmplastModifiedSample = lcs.toString();
      if (!tmplastModifiedSample.equals("N/A")) {
        lastModifiedExperiment = tmplastModifiedExperiment;
        lastModifiedSample = tmplastModifiedSample;
      }

      number_of_projects += projects.size();// projects.size();
      for (Project p : projects) {
        Object new_s = space_container.addItem();
        space_container.getContainerProperty(new_s, "Project").setValue(p.getCode());

        // List<Experiment> experiments =
        // this.openBisClient.getExperimentsOfProjectByCode(p.getCode());
        List<String> project_identifier = new ArrayList<String>();
        project_identifier.add(p.getIdentifier());
        List<Experiment> experiments =
            this.openBisClient.facade.listExperimentsForProjects(project_identifier);
        List<String> experiment_identifiers = new ArrayList<String>();

        for (Experiment exp : experiments)
          experiment_identifiers.add(exp.getIdentifier());

        /*
         * for(Experiment exp :experiments) { experiment_identifiers.add(exp.getIdentifier()); if
         * (exp.getExperimentTypeCode().equals("Q_PROJECT_DETAILS") &&
         * exp.getProperties().get("Q_PERSONS") != null) {
         * 
         * List<Qperson> persons =
         * this.parseConnectedPeopleInformation(exp.getProperties().get("Q_PERSONS"));
         * 
         * for(Qperson person: persons) {
         * 
         * //if(connectedPersons.containsKey(person.getFirstname())) { //IndexedContainer
         * personContainer = connectedPersons.get(person.getFirstname()); Object newPerson =
         * this.connectedPersons.addItem();
         * this.connectedPersons.getContainerProperty(newPerson,"Title"
         * ).setValue(person.getTitle());
         * this.connectedPersons.getContainerProperty(newPerson,"First Name"
         * ).setValue(person.getFirstname());
         * this.connectedPersons.getContainerProperty(newPerson,"Last Name"
         * ).setValue(person.getLastname());
         * this.connectedPersons.getContainerProperty(newPerson,"Position"
         * ).setValue(person.getPosition());
         * this.connectedPersons.getContainerProperty(newPerson,"E-Mail"
         * ).setValue(person.getEmail());
         * this.connectedPersons.getContainerProperty(newPerson,"Phone"
         * ).setValue(person.getPhone().toString());
         * this.connectedPersons.getContainerProperty(newPerson,"Project").setValue(p.getCode()); }
         * break; } }
         */
        // Project descriptions can be long; truncate the string to provide a brief preview
        String desc = p.getDescription();

        if (desc != null && desc.length() > 0) {
          desc = desc.substring(0, Math.min(desc.length(), 100));

          if (desc.length() == 100) {
            desc += "...";
          }
        }



        space_container.getContainerProperty(new_s, "Description").setValue(desc);

        // True/False is nice for us but not for the customer
        // if (this.openBisClient.getDataSetsOfProjectByCode(p.getCode()).size() > 0) {
        // Makes it much faster
        if (experiment_identifiers.size() > 0
            && this.openBisClient.facade.listDataSetsForExperiments(experiment_identifiers).size() > 0) {
          space_container.getContainerProperty(new_s, "Contains datasets").setValue("yes");
        } else {
          space_container.getContainerProperty(new_s, "Contains datasets").setValue("no");
        }
      }
    }
    homeInformation.numberOfProjects = number_of_projects;
    homeInformation.numberOfExperiments = number_of_experiments;
    homeInformation.numberOfSamples = number_of_samples;
    homeInformation.numberOfDatasets = number_of_datasets;
    homeInformation.lastChangedDataset = lastModifiedDate;
    homeInformation.lastChangedSample = lastModifiedSample;
    homeInformation.lastChangedExperiment = lastModifiedExperiment;
    homeInformation.projects = space_container;

    return homeInformation;

  }


  // id in this case meaning the openBIS instance ?!
  public SpaceInformation getSpace(String identifier) throws Exception {

    List<SpaceWithProjectsAndRoleAssignments> space_list = null;
    SpaceInformation spaces = null;

    if (this.spaces.get(identifier) != null) {
      return this.spaces.get(identifier);
    }

    else if (this.spaces.get(identifier) == null) {
      space_list = this.getSpace_list();
      spaces = this.createSpaceContainer(space_list, identifier);

      this.spaces.put(identifier, spaces);
    }

    else {
      throw new Exception("Unknown Space: " + identifier + ". Method DataHandler::getSpace.");
    }

    return spaces;

  }


  public HierarchicalContainer getDatasets(String id, String type) throws Exception {

    List<DataSet> dataset_list = null;
    HierarchicalContainer datasets = null;

    // TODO change return type of dataset retrieval methods if possible
    if (type.equals("space")) {
      if (this.space_to_datasets.get(id) != null) {
        return this.space_to_datasets.get(id);
      }

      else {
        dataset_list = this.openBisClient.getDataSetsOfSpaceByIdentifier(id);
        datasets = this.createDatasetContainer(dataset_list, id);
        this.space_to_datasets.put(id, datasets);
      }
    } else if (type.equals("project")) {
      if (this.project_to_datasets.get(id) != null) {
        return this.project_to_datasets.get(id);
      }

      else {
        dataset_list = this.openBisClient.getDataSetsOfProjectByIdentifier(id);

        datasets = this.createDatasetContainer(dataset_list, id);
        this.project_to_datasets.put(id, datasets);
      }
    } else if (type.equals("experiment")) {
      if (this.experiment_to_datasets.get(id) != null) {
        return this.experiment_to_datasets.get(id);
      }

      else {
        Experiment tmp_exp = this.openBisClient.getExperimentByCode(id);
        dataset_list = this.openBisClient.getDataSetsOfExperiment(tmp_exp.getPermId());
        datasets = this.createDatasetContainer(dataset_list, id);
        this.experiment_to_datasets.put(id, datasets);
      }
    } else if (type.equals("sample")) {
      if (this.sample_to_datasets.get(id) != null) {
        return this.sample_to_datasets.get(id);
      }

      else {
        Sample sample = this.openBisClient.getSampleByIdentifier(id);

        dataset_list = this.openBisClient.getDataSetsOfSampleByIdentifier(sample.getIdentifier());

        datasets = this.createDatasetContainer(dataset_list, id);
        this.sample_to_datasets.put(id, datasets);
      }
    } else {
      throw new Exception("Unknown datatype: " + type);
    }

    return datasets;
  }

  public IndexedContainer getSamples(String id, String type) throws Exception {

    List<Sample> sample_list = null;
    IndexedContainer samples = null;

    // TODO change return type of dataset retrieval methods if possible
    if (type.equals("space")) {
      if (this.space_to_samples.get(id) != null) {
        return this.space_to_samples.get(id);
      }

      else {
        sample_list = this.openBisClient.getSamplesofSpace(id);
        samples = this.createSampleContainer(sample_list, id);
        this.space_to_samples.put(id, samples);
      }
    } else if (type.equals("project")) {
      if (this.project_to_samples.get(id) != null) {
        return this.project_to_samples.get(id);
      }

      else {
        sample_list = this.openBisClient.getSamplesOfProject(id);
        samples = this.createSampleContainer(sample_list, id);
        this.project_to_samples.put(id, samples);
      }
    } else if (type.equals("experiment")) {
      if (this.experiment_to_samples.get(id) != null) {
        return this.experiment_to_samples.get(id);
      }

      else {
        sample_list = this.openBisClient.getSamplesofExperiment(id);
        samples = this.createSampleContainer(sample_list, id);
        this.experiment_to_samples.put(id, samples);
      }
    } else {
      throw new Exception("Unknown datatype!");
    }

    return samples;
  }

  public IndexedContainer getExperiments(String id, String type) throws Exception {

    List<Experiment> experiment_list = null;
    IndexedContainer experiments = null;

    // TODO change return type of dataset retrieval methods if possible
    if (type.equals("space")) {
      if (this.space_to_experiments.get(id) != null) {
        return this.space_to_experiments.get(id);
      }

      else {
        experiment_list = this.openBisClient.getExperimentsOfSpace(id);
        experiments = this.createExperimentContainer(experiment_list, id);
        this.space_to_experiments.put(id, experiments);
      }
    } else if (type.equals("project")) {
      if (this.project_to_experiments.get(id) != null) {
        return this.project_to_experiments.get(id);
      }

      else {
        experiment_list = this.openBisClient.getExperimentsOfProjectByIdentifier(id);
        experiments = this.createExperimentContainer(experiment_list, id);
        this.project_to_experiments.put(id, experiments);
      }
    } else {
      System.out.println("Unknown");
      throw new Exception("Unknown datatype!");
    }

    return experiments;
  }

  public IndexedContainer getProjects(String id, String type) throws Exception {

    List<Project> project_list = null;
    IndexedContainer projects = null;

    // TODO change return type of dataset retrieval methods if possible
    if (type.equals("space")) {
      if (this.space_to_projects.get(id) != null) {
        return this.space_to_projects.get(id);
      }

      else {
        project_list = this.openBisClient.getProjectsOfSpace(id);
        projects = this.createProjectContainer(project_list, id);
        this.space_to_projects.put(id, projects);
      }
    } else {
      throw new Exception("Unknown datatype!");
    }

    return projects;
  }

  public ProjectInformation getProjectInformation(String id) {
    if (this.projectInformations.containsKey(id)) {
      return this.projectInformations.get(id);
    } else {
      ProjectInformation ret = new ProjectInformation();
      try {
        ret.experiments = this.getExperiments(id, "project");
        Project project = this.openBisClient.getProjectByIdentifier(id);
        ret.description = project.getDescription();
        ret.numberOfExperiments = ret.experiments.size();

        List<DataSet> datasets =
            this.openBisClient.getDataSetsOfProjectByIdentifier(project.getCode());

        ret.numberOfDatasets = datasets.size();

        StringBuilder lce = new StringBuilder();
        StringBuilder lcs = new StringBuilder();
        ret.lastChangedDataset = new Date(0, 0, 0);
        this.lastDatasetRegistered(datasets, ret.lastChangedDataset, lce, lcs);
        ret.lastChangedExperiment = lce.toString();
        ret.lastChangedSample = lcs.toString();
        List<Sample> samples = this.openBisClient.getSamplesOfProject(project.getCode());
        ret.numberOfSamples = samples.size();
        // TODO status message
        ret.statusMessage = "";

        ret.progressBar = new ProgressBar();
        ret.progressBar.setValue(this.openBisClient.computeProjectStatus(project));

        ret.contact = String.format("Some QBiC Stuff\nWith a phone number\nAnd an adress");
        // QBic Staff is removed from member set.
        ret.members =
            this.removeQBiCStaffFromMemberSet(this.getSpaceMembers(project.getSpaceCode()));


        this.projectInformations.put(id, ret);
      } catch (Exception e) {
        System.out.println("Exception in DataHandler.getProjectInformation.");
        ret = null;
        // e.printStackTrace();
      }



      return ret;
    }
  }

  /**
   * Returns all users of a Space.
   * 
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  private Set<String> getSpaceMembers(String spaceCode) {
    List<SpaceWithProjectsAndRoleAssignments> spaces = this.getSpace_list();
    for (SpaceWithProjectsAndRoleAssignments space : spaces) {
      if (space.getCode().equals(spaceCode)) {
        return space.getUsers();
      }
    }
    return null;
  }

  public ExperimentInformation getExperimentInformation(String id) {
    if (this.experimentInformations.containsKey(id)) {
      return this.experimentInformations.get(id);
    } else {
      ExperimentInformation ret = new ExperimentInformation();
      try {
        // TODO check for source of nullpointer exception !
        // seems like first the project id gets here
        Experiment exp = this.openBisClient.getExperimentByCode(id);
        // ret.identifier = exp.getIdentifier();
        ret.experimentType = this.openBisClient.openBIScodeToString(exp.getExperimentTypeCode());
        ret.samples = this.getSamples(id, "experiment");
        ret.numberOfSamples = ret.samples.size();
        List<DataSet> datasets = this.openBisClient.getDataSetsOfExperiment(exp.getPermId());
        ret.numberOfDatasets = datasets.size();
        StringBuilder lce = new StringBuilder();
        StringBuilder lcs = new StringBuilder();
        ret.lastChangedDataset = new Date(0, 0, 0);
        this.lastDatasetRegistered(datasets, ret.lastChangedDataset, lce, lcs);
        ret.lastChangedSample = lcs.toString();

        // TODO TEST
        // We want to get all properties for metadata changes, not only those with values

        Map<String, String> assignedProperties = exp.getProperties();
        List<PropertyType> completeProperties =
            openBisClient.listPropertiesForType(openBisClient.getExperimentTypeByString(exp
                .getExperimentTypeCode()));

        Map<String, String> properties = new HashMap<String, String>();
        Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();

        for (PropertyType p : completeProperties) {

          if (p.getDataType().toString().equals("CONTROLLEDVOCABULARY")) {
            controlledVocabularies
                .put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
          }

          if (assignedProperties.keySet().contains(p.getCode())) {
            properties.put(p.getCode(), assignedProperties.get(p.getCode()));
          } else {
            properties.put(p.getCode(), "");
          }
        }

        ret.properties = properties;
        ret.controlledVocabularies = controlledVocabularies;

        // Map<String,String> typeLabels =
        // this.openBisClient.getLabelsofProperties(this.openBisClient.getExperimentTypeByString(exp.getExperimentTypeCode()));

        String propertiesHeader = "Properties \n <ul>";
        String propertiesBottom = "";

        Iterator it = ret.properties.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          if (pairs.getValue().equals("")) {
            continue;
          } else if (pairs.getKey().equals("Q_PERSONS")) {
            continue;
          } else {
            // propertiesBottom += "<li><b>" + (typeLabels.get(pairs.getKey()) + ":</b> " +
            // pairs.getValue() + "</li>");
            propertiesBottom +=
                "<li><b>"
                    + (this.openBisClient.openBIScodeToString(pairs.getKey().toString()) + ":</b> "
                        + pairs.getValue() + "</li>");
          }
        }
        propertiesBottom += "</ul>";

        ret.propertiesFormattedString = propertiesHeader + propertiesBottom;

        this.experimentInformations.put(id, ret);
      } catch (Exception e) {
        e.printStackTrace();
        ret = null;
      }
      return ret;
    }
  }

  public SampleInformation getSampleInformation(String id) {
    if (this.sampleInformations.containsKey(id)) {
      return this.sampleInformations.get(id);
    } else {
      SampleInformation ret = new SampleInformation();
      Sample samp = this.openBisClient.getSampleByIdentifier(id);

      // watch out ! sample type is not the openBIS sample type anymore after this call.
      ret.sampleType = this.openBisClient.openBIScodeToString(samp.getSampleTypeCode());
      try {
        ret.datasets = this.getDatasets(id, "sample");

        ret.numberOfDatasets = ret.datasets.size();

        List<DataSet> datasets =
            this.openBisClient.getDataSetsOfSampleByIdentifier(samp.getIdentifier());
        ret.numberOfDatasets = datasets.size();

        ret.parents = new HashMap<String, String>();

        List<Sample> parents = this.openBisClient.facade.listSamplesOfSample(samp.getPermId());
        for (Sample s : parents) {
          ret.parents.put(s.getIdentifier(),
              this.openBisClient.openBIScodeToString(s.getSampleTypeCode()));
        }

        StringBuilder lce = new StringBuilder();
        StringBuilder lcs = new StringBuilder();
        ret.lastChangedDataset = new Date(0, 0, 0);

        this.lastDatasetRegistered(datasets, ret.lastChangedDataset, lce, lcs);

        ret.properties = samp.getProperties();

        Map<String, String> typeLabels =
            this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(samp
                .getSampleTypeCode()));



        // String propertiesHeader = "Properties \n <ul>";
        String propertiesBottom = "<ul> ";
        String xmlPropertiesBottom = "<ul> ";

        Iterator it = ret.properties.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          if (pairs.getKey().equals("Q_PROPERTIES")) {
            Parser xmlParser = new Parser();
            JAXBElement<Qproperties> xmlProperties =
                xmlParser.parseXMLString(pairs.getValue().toString());
            Map<String, String> xmlPropertiesMap = xmlParser.getMap(xmlProperties);

            Iterator itProperties = xmlPropertiesMap.entrySet().iterator();
            while (itProperties.hasNext()) {
              Map.Entry pairsProperties = (Map.Entry) itProperties.next();

              xmlPropertiesBottom +=
                  "<li><b>"
                      + (pairsProperties.getKey() + ":</b> " + pairsProperties.getValue() + "</li>");
            }
          } else {
            propertiesBottom +=
                "<li><b>"
                    + (typeLabels.get(pairs.getKey()) + ":</b> " + pairs.getValue() + "</li>");
          }
        }
        propertiesBottom += "</ul>";

        ret.propertiesFormattedString = propertiesBottom;
        ret.xmlPropertiesFormattedString = xmlPropertiesBottom;

        String parentsHeader = "Sample(s) derived from this sample: ";
        String parentsBottom = "<ul>";

        if (ret.parents.isEmpty()) {
          ret.parentsFormattedString = parentsHeader += "None";

        } else {
          Iterator parentsIt = ret.parents.entrySet().iterator();
          while (parentsIt.hasNext()) {
            Map.Entry pairs = (Map.Entry) parentsIt.next();
            parentsBottom += "<li><b>" + pairs.getKey() + "</b> (" + pairs.getValue() + ") </li>";
          }
          parentsBottom += "</ul>";
          ret.parentsFormattedString = parentsHeader + parentsBottom;
        }

        this.sampleInformations.put(id, ret);

      } catch (Exception e) {
        e.printStackTrace();
        ret = null;
      }
      return ret;
    }
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

  public void reset() {
    // this.spaces = new HashMap<String,IndexedContainer>();
    // this.projects = new HashMap<String,IndexedContainer>();
    // this.experiments = new HashMap<String,IndexedContainer>();
    // this.samples = new HashMap<String,IndexedContainer>();
    this.space_to_datasets = new HashMap<String, HierarchicalContainer>();
  }


  // public void initConnection() {
  // TODO this.openBISClient = new OpenClient();
  // }
  /**
   * returns an empyt Container if identifier is not a valid openbis space identifier. Else returns
   * some space informations.
   * 
   * @param spaces
   * @param identifier
   * @return
   */
  private SpaceInformation createSpaceContainer(List<SpaceWithProjectsAndRoleAssignments> spaces,
      String identifier) {
    SpaceWithProjectsAndRoleAssignments tmp_space = null;
    for (SpaceWithProjectsAndRoleAssignments space : spaces) {
      if (space.getCode().equals(identifier)) {
        tmp_space = space;
        break;
      }
    }
    if (tmp_space == null) {
      System.out.println(String.format(
          "space %s does not seem to exist! In DataHandler::createSpaceContainer", identifier));
      return null;
    }
    SpaceInformation spaceInformation = new SpaceInformation();
    IndexedContainer space_container = new IndexedContainer();

    space_container.addContainerProperty("Project", String.class, "");
    space_container.addContainerProperty("Description", String.class, "");
    space_container.addContainerProperty("Progress", ProgressBar.class, "");


    // List<Project> projects = this.openBisClient.getProjectsofSpace(id);
    int number_of_samples = 0;
    List<Project> projects = tmp_space.getProjects();
    int number_of_projects = projects.size();// projects.size();
    int number_of_experiments = 0;
    int number_of_datasets = 0;
    String lastModifiedExperiment = "N/A";
    String lastModifiedSample = "N/A";
    Date lastModifiedDate = new Date(0, 0, 0);

    number_of_experiments = this.openBisClient.getExperimentsOfSpace(identifier).size();// this.openBisClient.openbisInfoService.listExperiments(this.openBisClient.getSessionToken(),
                                                                                        // projects,
                                                                                        // null);
    List<Sample> samplesOfSpace = this.openBisClient.getSamplesofSpace(identifier);// this.openBisClient.facade.listSamplesForProjects(tmp_list_str);
    number_of_samples += samplesOfSpace.size();
    List<DataSet> datasets = this.openBisClient.getDataSetsOfSpaceByIdentifier(identifier); // this.openBisClient.facade.listDataSetsForExperiments(tmp_experiment_identifier_lis);
    number_of_datasets = datasets.size();

    StringBuilder lce = new StringBuilder();
    StringBuilder lcs = new StringBuilder();
    this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
    lastModifiedExperiment = lce.toString();
    lastModifiedSample = lcs.toString();

    spaceInformation.numberOfProjects = number_of_projects;
    spaceInformation.numberOfExperiments = number_of_experiments;
    spaceInformation.numberOfSamples = number_of_samples;
    spaceInformation.numberOfDatasets = number_of_datasets;
    spaceInformation.lastChangedDataset = lastModifiedDate;
    spaceInformation.lastChangedSample = lastModifiedSample;
    spaceInformation.lastChangedExperiment = lastModifiedExperiment;

    spaceInformation.members = removeQBiCStaffFromMemberSet(tmp_space.getUsers());

    for (Project p : projects) {
      Object new_s = space_container.addItem();
      space_container.getContainerProperty(new_s, "Project").setValue(p.getCode());
      space_container.getContainerProperty(new_s, "Description").setValue(p.getDescription());
      space_container.getContainerProperty(new_s, "Progress").setValue(
          new ProgressBar(this.openBisClient.computeProjectStatus(p)));
    }
    spaceInformation.projects = space_container;

    return spaceInformation;
  }

  /**
   * This method filters out qbic staff and other unnecessary space members TODO: this method might
   * be better of as not being part of the DataHandler...and not hardcoded
   * 
   * @param users a set of all space users or members
   * @return a new set which exculdes qbic staff and functional members
   */
  private Set<String> removeQBiCStaffFromMemberSet(Set<String> users) {
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

  @SuppressWarnings("unchecked")
  private IndexedContainer createProjectContainer(List<Project> projs, String id) {

    IndexedContainer project_container = new IndexedContainer();

    project_container.addContainerProperty("Description", String.class, null);
    project_container.addContainerProperty("Space", String.class, null);
    project_container.addContainerProperty("Registration Date", Timestamp.class, null);
    project_container.addContainerProperty("Registrator", String.class, null);
    project_container.addContainerProperty("Progress", ProgressBar.class, null);
    project_container.addContainerProperty("Progress", ProgressBar.class, null);

    for (Project p : projs) {
      Object new_p = project_container.addItem();

      String code = p.getCode();
      String desc = p.getDescription();

      String space = code.split("/")[1];

      Date date = p.getRegistrationDetails().getRegistrationDate();
      String registrator = p.getRegistrationDetails().getUserId();

      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);

      ProgressBar progressBar = new ProgressBar();
      progressBar.setValue(this.openBisClient.computeProjectStatus(p));

      project_container.getContainerProperty(new_p, "Space").setValue(space);
      project_container.getContainerProperty(new_p, "Description").setValue(desc);
      project_container.getContainerProperty(new_p, "Registration Date").setValue(ts);
      project_container.getContainerProperty(new_p, "Registerator").setValue(registrator);
      project_container.getContainerProperty(new_p, "Progress").setValue(progressBar);
    }

    return project_container;
  }

  @SuppressWarnings("unchecked")
  private IndexedContainer createExperimentContainer(List<Experiment> exps, String id) {

    IndexedContainer experiment_container = new IndexedContainer();

    experiment_container.addContainerProperty("Experiment", String.class, null);
    experiment_container.addContainerProperty("Experiment Type", String.class, null);
    experiment_container.addContainerProperty("Registration Date", Timestamp.class, null);
    experiment_container.addContainerProperty("Registrator", String.class, null);
    experiment_container.addContainerProperty("Status", Image.class, null);
    // experiment_container.addContainerProperty("Properties", Map.class, null);



    for (Experiment e : exps) {
      Object new_ds = experiment_container.addItem();

      String experimentIdentifier = e.getIdentifier();
      String type = this.openBisClient.openBIScodeToString(e.getExperimentTypeCode());
      String space = experimentIdentifier.split("/")[1];
      String project = experimentIdentifier.split("/")[2];

      Map<String, String> properties = e.getProperties();

      String status = "";

      if (properties.keySet().contains("Q_CURRENT_STATUS")) {
        status = properties.get("Q_CURRENT_STATUS");
      }

      Date date = e.getRegistrationDetails().getRegistrationDate();
      String registrator = e.getRegistrationDetails().getUserId();

      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      experiment_container.getContainerProperty(new_ds, "Experiment").setValue(e.getCode());
      experiment_container.getContainerProperty(new_ds, "Experiment Type").setValue(type);
      experiment_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      experiment_container.getContainerProperty(new_ds, "Registrator").setValue(registrator);

      Image statusColor = new Image(status, this.setExperimentStatusColor(status));
      statusColor.setWidth("15px");
      statusColor.setHeight("15px");
      experiment_container.getContainerProperty(new_ds, "Status").setValue(statusColor);
      // experiment_container.getContainerProperty(new_ds,
      // "Properties").setValue(e.getProperties());
    }

    return experiment_container;
  }

  @SuppressWarnings("unchecked")
  private IndexedContainer createSampleContainer(List<Sample> samples, String id) {

    IndexedContainer sample_container = new IndexedContainer();
    sample_container.addContainerProperty("Sample", String.class, null);
    sample_container.addContainerProperty("Description", String.class, null);
    sample_container.addContainerProperty("Sample Type", String.class, null);
    sample_container.addContainerProperty("Registration Date", Timestamp.class, null);
    // sample_container.addContainerProperty("Species", String.class, null);

    for (Sample s : samples) {
      Object new_ds = sample_container.addItem();

      String type = this.openBisClient.openBIScodeToString(s.getSampleTypeCode());

      Date date = s.getRegistrationDetails().getRegistrationDate();
      Map<String, String> properties = s.getProperties();
      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      sample_container.getContainerProperty(new_ds, "Sample").setValue(s.getCode());
      // sample_container.getContainerProperty(new_ds,
      // "Description").setValue(properties.get("SAMPLE_CLASS"));
      sample_container.getContainerProperty(new_ds, "Description").setValue(
          properties.get("Q_SECONDARY_NAME"));
      sample_container.getContainerProperty(new_ds, "Sample Type").setValue(type);
      sample_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      // sample_container.getContainerProperty(new_ds,
      // "Species").setValue(properties.get("SPECIES"));
    }

    return sample_container;
  }

  private HierarchicalContainer createDatasetContainer(List<DataSet> datasets, String id) {

    HierarchicalContainer dataset_container = new HierarchicalContainer();

    dataset_container.addContainerProperty("Select", CheckBox.class, null);
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

    for (DataSet d : datasets) {
      String identifier = d.getSampleIdentifierOrNull();
      Sample sampleObject = this.openBisClient.getSampleByIdentifier(identifier);
      String sample = sampleObject.getCode();
      String sampleType = this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode();
      Project projectObject =
          this.openBisClient.getProjectOfExperimentByIdentifier(sampleObject
              .getExperimentIdentifierOrNull());
      String project = projectObject.getCode();
      // String code = d.getSampleIdentifierOrNull();
      // String sample = code.split("/")[2];
      // String project = sample.substring(0, 5);
      Date date = d.getRegistrationDate();

      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);

      FileInfoDssDTO[] filelist = d.listFiles("original", true);

      // recursive test
      registerDatasetInTable(d, filelist, dataset_container, project, sample, ts, sampleType, null);

    }

    return dataset_container;
  }

  public void registerDatasetInTable(DataSet d, FileInfoDssDTO[] filelist,
      HierarchicalContainer dataset_container, String project, String sample, Timestamp ts,
      String sampleType, Object parent) {
    if (filelist[0].isDirectory()) {

      Object new_ds = dataset_container.addItem();

      String folderPath = filelist[0].getPathInDataSet();
      FileInfoDssDTO[] subList = d.listFiles(folderPath, false);

      long totalFileSize = 0L;
      dataset_container.setChildrenAllowed(new_ds, true);
      String download_link = filelist[0].getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      String file_name = download_link.split("/")[splitted_link.length - 1];
      //System.out.println(file_name);

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

  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {

    return this.openBisClient.getDataStoreDownloadURL(datasetCode, datasetName);
  }

  public InputStream getDatasetStream(String datasetCode) {

    IOpenbisServiceFacade facade = openBisClient.getFacade();
    DataSet dataSet = facade.getDataSet(datasetCode);
    FileInfoDssDTO[] filelist = dataSet.listFiles("original", false);
    return dataSet.getFile(filelist[0].getPathInDataSet());
  }

  public InputStream getDatasetStream(String datasetCode, String folder) {

    IOpenbisServiceFacade facade = openBisClient.getFacade();
    DataSet dataSet = facade.getDataSet(datasetCode);
    FileInfoDssDTO[] filelist = dataSet.listFiles("original/" + folder, false);
    return dataSet.getFile(filelist[0].getPathInDataSet());
  }

  /**
   * Function to fill tree container and collect statistical information of spaces. Should replace
   * the two functions and be somewhat faster. Still not pretty. Needs work
   * 
   * @param tc HierarchicalContainer for the Tree
   * @param userName Screenname of the Liferay User
   * @return SpaceInformation object
   */
  public SpaceInformation initTreeAndHomeInfo(HierarchicalContainer tc, String userName) {

    List<SpaceWithProjectsAndRoleAssignments> space_list = this.getSpace_list();

    // Initialization of Tree Container
    tc.addContainerProperty("identifier", String.class, "N/A");
    tc.addContainerProperty("type", String.class, "N/A");
    tc.addContainerProperty("project", String.class, "N/A");
    tc.addContainerProperty("caption", String.class, "N/A");


    // Initialization of Home Information
    SpaceInformation homeInformation = new SpaceInformation();
    IndexedContainer space_container = new IndexedContainer();
    space_container.addContainerProperty("Project", String.class, "");
    space_container.addContainerProperty("Description", String.class, "");
    space_container.addContainerProperty("Contains datasets", String.class, "");
    int number_of_projects = 0;
    int number_of_experiments = 0;
    int number_of_samples = 0;
    int number_of_datasets = 0;
    String lastModifiedExperiment = "N/A";
    String lastModifiedSample = "N/A";
    Date lastModifiedDate = new Date(0, 0, 0);
    for (SpaceWithProjectsAndRoleAssignments s : space_list) {
      if (s.getUsers().contains(userName)) {
        String space_name = s.getCode();

        // TODO does this work for everyone? should it? empty container would be the aim, probably
        if (space_name.equals("QBIC_USER_SPACE")) {
          fillPersonsContainer(space_name);
        }

        List<Project> projects = s.getProjects();
        number_of_projects += projects.size();
        List<String> project_identifiers_tmp = new ArrayList<String>();
        for (Project project : projects) {

          String project_name = project.getCode();
          if (tc.containsId(project_name)) {
            project_name = project.getIdentifier();
          }
          Object new_s = space_container.addItem();
          space_container.getContainerProperty(new_s, "Project").setValue(project_name);

          // Project descriptions can be long; truncate the string to provide a brief preview
          String desc = project.getDescription();

          if (desc != null && desc.length() > 0) {
            desc = desc.substring(0, Math.min(desc.length(), 100));
            if (desc.length() == 100) {
              desc += "...";
            }
          }
          space_container.getContainerProperty(new_s, "Description").setValue(desc);

          // System.out.println("|--Project: " + project_name);
          tc.addItem(project_name);

          tc.getContainerProperty(project_name, "type").setValue("project");
          tc.getContainerProperty(project_name, "identifier").setValue(project_name);
          tc.getContainerProperty(project_name, "project").setValue(project_name);
          tc.getContainerProperty(project_name, "caption").setValue(project_name);

          List<Project> tmp_list = new ArrayList<Project>();
          tmp_list.add(project);
          List<Experiment> experiments =
              this.openBisClient.openbisInfoService.listExperiments(
                  this.openBisClient.getSessionToken(), tmp_list, null);

          // Add number of experiments for every project
          number_of_experiments += experiments.size();

          List<String> experiment_identifiers = new ArrayList<String>();

          for (Experiment experiment : experiments) {
            experiment_identifiers.add(experiment.getIdentifier());
            String experiment_name = experiment.getCode();
            if (tc.containsId(experiment_name)) {
              experiment_name = experiment.getIdentifier();
            }
            // System.out.println(" |--Experiment: " + experiment_name);
            tc.addItem(experiment_name);
            tc.setParent(experiment_name, project_name);
            tc.getContainerProperty(experiment_name, "type").setValue("experiment");
            tc.getContainerProperty(experiment_name, "identifier").setValue(experiment_name);
            tc.getContainerProperty(experiment_name, "project").setValue(project_name);
            tc.getContainerProperty(experiment_name, "caption").setValue(
                String.format("%s (%s)",
                    this.openBisClient.openBIScodeToString(experiment.getExperimentTypeCode()),
                    experiment_name));

            tc.setChildrenAllowed(experiment_name, false);
          }
          if (experiment_identifiers.size() > 0
              && this.openBisClient.getFacade().listDataSetsForExperiments(experiment_identifiers)
                  .size() > 0) {
            space_container.getContainerProperty(new_s, "Contains datasets").setValue("yes");
          } else {
            space_container.getContainerProperty(new_s, "Contains datasets").setValue("no");
          }
        }
        List<Sample> samplesOfSpace = new ArrayList<Sample>();
        if (project_identifiers_tmp.size() > 0) {
          samplesOfSpace =
              this.openBisClient.getFacade().listSamplesForProjects(project_identifiers_tmp);
        } else {
          samplesOfSpace = this.openBisClient.getSamplesofSpace(space_name); // TODO code or
                                                                             // identifier
          // needed?
        }
        number_of_samples += samplesOfSpace.size();
        List<String> sample_identifiers_tmp = new ArrayList<String>();
        for (Sample sa : samplesOfSpace) {
          sample_identifiers_tmp.add(sa.getIdentifier());
        }
        List<DataSet> datasets = new ArrayList<DataSet>();
        if (sample_identifiers_tmp.size() > 0) {
          datasets = this.openBisClient.getFacade().listDataSetsForSamples(sample_identifiers_tmp);
        }
        number_of_datasets += datasets.size();
        StringBuilder lce = new StringBuilder();
        StringBuilder lcs = new StringBuilder();
        this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
        String tmplastModifiedExperiment = lce.toString();
        String tmplastModifiedSample = lcs.toString();
        if (!tmplastModifiedSample.equals("N/A")) {
          lastModifiedExperiment = tmplastModifiedExperiment;
          lastModifiedSample = tmplastModifiedSample;
        }
      }
    }
    homeInformation.numberOfProjects = number_of_projects;
    homeInformation.numberOfExperiments = number_of_experiments;
    homeInformation.numberOfSamples = number_of_samples;
    homeInformation.numberOfDatasets = number_of_datasets;
    homeInformation.lastChangedDataset = lastModifiedDate;
    homeInformation.lastChangedSample = lastModifiedSample;
    homeInformation.lastChangedExperiment = lastModifiedExperiment;
    homeInformation.projects = space_container;

    return homeInformation;
  }

  /**
   * This method fills the Hierarchical tree container for the user with the given screenName. It
   * contains the Openbis data model hierarchy including, projects, experiments and samples.
   * 
   * @param tc
   * @param screenName
   */
  @SuppressWarnings({"unchecked"})
  public void fillHierarchicalTreeContainer(HierarchicalContainer tc, String screenName) {
    tc.addContainerProperty("identifier", String.class, "N/A");
    tc.addContainerProperty("type", String.class, "N/A");
    tc.addContainerProperty("project", String.class, "N/A");

    List<SpaceWithProjectsAndRoleAssignments> space_list = this.getSpace_list();

    for (SpaceWithProjectsAndRoleAssignments s : space_list) {
      if (s.getUsers().contains(screenName)) {
        String space_name = s.getCode();

        // tc.addItem(space_name);
        // tc.setParent(space_name, null);
        // tc.getContainerProperty(space_name, "identifier").setValue(space_name);
        // tc.getContainerProperty(space_name, "type").setValue("space");

        List<Project> projects = s.getProjects();
        for (Project project : projects) {

          String project_name = project.getCode();
          if (tc.containsId(project_name)) {
            project_name = project.getIdentifier();
          }
          // System.out.println("|--Project: " + project_name);
          tc.addItem(project_name);
          tc.setParent(project_name, space_name);

          tc.getContainerProperty(project_name, "type").setValue("project");
          tc.getContainerProperty(project_name, "identifier").setValue(project_name);
          tc.getContainerProperty(project_name, "project").setValue(project_name);


          List<Project> tmp_list = new ArrayList<Project>();
          tmp_list.add(project);
          List<Experiment> experiments =
              this.openBisClient.openbisInfoService.listExperiments(
                  this.openBisClient.getSessionToken(), tmp_list, null);

          for (Experiment experiment : experiments) {
            String experiment_name = experiment.getCode();
            if (tc.containsId(experiment_name)) {
              experiment_name = experiment.getIdentifier();
            }
            // System.out.println("	|--Experiment: " + experiment_name);
            tc.addItem(experiment_name);
            tc.setParent(experiment_name, project_name);

            tc.getContainerProperty(experiment_name, "type").setValue("experiment");
            tc.getContainerProperty(experiment_name, "identifier").setValue(experiment_name);
            tc.getContainerProperty(experiment_name, "project").setValue(project_name);

            tc.setChildrenAllowed(experiment_name, false);
            /*
             * List<Sample> samples =
             * this.openBisClient.openbisInfoService.listSamplesForExperiment(
             * this.openBisClient.getSessionToken(), experiment.getIdentifier()); for (Sample sample
             * : samples) { // The commented code allows only biological samples to be shown in
             * TreeView. We show // all samples for now. if
             * (!sample.getSampleTypeCode().equals("BIOLOGICAL")){ continue; } String samp =
             * sample.getCode(); if (tc.containsId(samp)) { samp = sample.getIdentifier(); } //
             * System.out.println("		|--Sample: " + samp); tc.addItem(samp); tc.setParent(samp,
             * experiment_name);
             * 
             * tc.getContainerProperty(samp, "type").setValue("sample");
             * tc.getContainerProperty(samp, "identifier").setValue(samp);
             * tc.setChildrenAllowed(samp, false); }
             */
          }
        }
      }
    }
  }

  /**
   * Creates a Map of project statuses fulfilled, keyed by their meaning. For this, different steps
   * in the project flow are checked by looking at experiment types and data registered
   * 
   * @param project openBIS project
   * @return
   */
  public Map<String, Integer> computeProjectStatuses(String projectId) {

    Project p = this.openBisClient.getProjectByCode(projectId);
    Map<String, Integer> res = new HashMap<String, Integer>();
    IndexedContainer c = project_to_experiments.get(p.getIdentifier());
    // project was planned (otherwise it would hopefully not exist :) )
    res.put("Project Planned", 1);
    // design is pre-registered to the test sample level
    int prereg = 0;
    for (Object itemId : c.getItemIds()) {
      Item exp = c.getItem(itemId);
      String type = (String) exp.getItemProperty("Experiment Type").getValue();
      if (type.equals(this.openBisClient.openBIScodeToString(ExperimentType.Q_SAMPLE_PREPARATION
          .toString()))) {
        prereg = 1;
        break;
      }
    }
    res.put("Experimental Design registered", prereg);
    // data is uploaded
    if (project_to_datasets.get(p.getCode()) != null
        && project_to_datasets.get(p.getCode()).size() > 0)
      res.put("Data Registered", 1);
    else
      res.put("Data Registered", 0);
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

  public StreamResource getTSVStream(final String content, String id) {
    StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
      @Override
      public InputStream getStream() {
        try {
          InputStream is = new ByteArrayInputStream(content.getBytes());
          return is;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }, String.format("%s_table_contents.tsv", id));
    return resource;
  }

  // public String beanContainerToString(BeanItemContainer c) {
  // String header = "";
  // for (Object o : c.getContainerPropertyIds())
  // header += o.toString() + "\t";
  // for (c.get)
  // }

  public String containerToString(Container container) {
    String header = "";
    Collection<?> i = container.getItemIds();
    System.out.println(i);
    String rowString = "";

    Collection<?> propertyIDs = container.getContainerPropertyIds();

    for (Object o : propertyIDs) {
      header += o.toString() + "\t";
    }

    // for (int x = 1; x <= i.size(); x++) {
    for (Object id : i) {
      System.out.println(container.toString());
      Item it = container.getItem(id);

      for (Object o : propertyIDs) {
        // Could be extended to an exclusion list if we don't want to show further columns
        if (o.toString() == "dl_link") {
          continue;
        } else if (o.toString() == "Status") {
          Image image = (Image) it.getItemProperty(o).getValue();
          rowString += image.getCaption() + "\t";
        } else {
          Property prop = it.getItemProperty(o);
          rowString += prop.toString() + "\t";
        }
      }
      rowString += "\n";
    }
    return header + "\n" + rowString;
  }

  public void callIngestionService(String serviceName, Map<String, Object> parameters) {
    System.out.println(serviceName);
    System.out.println(parameters);
    this.openBisClient.triggerIngestionService("notify-user", parameters);
  }

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

  public List<Experiment> listExperimentsOfProjects(List<Project> tmp_list) {
    return this.openBisClient.openbisInfoService.listExperiments(
        this.openBisClient.getSessionToken(), tmp_list, null);

  }

  public String openBIScodeToString(String experimentTypeCode) {
    return this.openBisClient.openBIScodeToString(experimentTypeCode);
  }

  public List<DataSet> listDataSetsForExperiments(List<String> experimentIdentifiers) {
    return this.openBisClient.getFacade().listDataSetsForExperiments(experimentIdentifiers);
  }

  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    return this.openBisClient.getFacade().listSamplesForProjects(projectIdentifiers);
  }

  public List<Sample> getSamplesofSpace(String spaceName) {
    return this.openBisClient.getSamplesofSpace(spaceName);
  }

  public List<DataSet> listDataSetsForSamples(List<String> sampleIdentifier) {
    return this.openBisClient.getFacade().listDataSetsForSamples(sampleIdentifier);
  }

}
