package controllers;

import java.io.Serializable;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import submitter.SubmitFailedException;
import submitter.Submitter;
import submitter.Workflow;
import logging.Log4j2Logger;
import main.OpenBisClient;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;

import de.uni_tuebingen.qbic.beans.DatasetBean;
import de.uni_tuebingen.qbic.qbicmainportlet.DatasetView;
import de.uni_tuebingen.qbic.qbicmainportlet.ExperimentView;
import de.uni_tuebingen.qbic.qbicmainportlet.PatientView;
import de.uni_tuebingen.qbic.qbicmainportlet.ProjectView;
import de.uni_tuebingen.qbic.qbicmainportlet.SampleView;

public class WorkflowViewController {
  private OpenBisClient openbis;
  private Submitter submitter;

  private logging.Logger LOGGER = new Log4j2Logger(WorkflowViewController.class);
  private String user;

  public WorkflowViewController(Submitter submitter, OpenBisClient openbis, String user) {
    this.openbis = openbis;
    this.submitter = submitter;
    this.user = user;
  }

  /**
   * Returns a Container with the informations of de.uni_tuebingen.qbic.beans.DatasetBean.
   * 
   * @param datasets
   * @return
   */
  public Container fillTable(List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets) {
    HashMap<String, DataSet> dataMap =
        new HashMap<String, ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
    BeanItemContainer<DatasetBean> container =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    Map<String, Object> params = new HashMap<String, Object>();
    List<String> dsCodes = new ArrayList<String>();

    for (ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet ds : datasets) {
      dsCodes.add(ds.getCode());
      dataMap.put(ds.getCode(), ds);
    }
    params.put("codes", dsCodes);
    QueryTableModel res = openbis.getAggregationService("query-files", params);
    for (Serializable[] ss : res.getRows()) {
      String dsCode = (String) ss[0];
      // when tryGetInternalPathInDataStore is used here for project like qmari it takes over a
      // minute. without 0.02s
      String path =
      /* dataMap.get(dsCode).getDataSetDss().tryGetInternalPathInDataStore() + */(String) ss[1];
      // path = path.replace("/mnt/DSS1", "/mnt/nfs/qbic");
      DatasetBean bean =
          new DatasetBean((String) ss[2], dataMap.get(dsCode).getDataSetTypeCode(), dsCode, path,
              dataMap.get(dsCode).getSampleIdentifierOrNull());
      container.addBean(bean);
    }


    return container;
  }

  /**
   * Register a new Workflow experiment in openBIS. Should be done when starting the workflow.
   * Experiment name is automatically created from the project name and the number of existing
   * experiments in that project. Standard workflow experiment fields are also initialized.
   * 
   * @param space space code
   * @param project project code
   * @param typecode openbis type code of the workflow
   * @param wfName name of the workflow
   * @param wfVersion version of the workflow
   * @param userID the user that starts the workflow
   * @return Code of the newly registered experiment
   */
  public String registerWFExperiment(String space, String project, String typecode, String wfName,
      String wfVersion, String userID) {
    int last = 0;
    for (Experiment e : openbis.getExperimentsOfProjectByIdentifier("/" + space + "/" + project)) {
      String[] codeSplit = e.getCode().split("E");
      String number = codeSplit[codeSplit.length - 1];
      int num = 0;
      try {
        num = Integer.parseInt(number);
      } catch (NumberFormatException ex) {
      }
      last = Math.max(num, last);
    }
    String code = project + "E" + Integer.toString(last + 1);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", code);
    params.put("type", typecode);
    params.put("project", project);
    params.put("space", space);
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Q_WF_NAME", wfName);
    properties.put("Q_WF_VERSION", wfVersion);
    properties.put("Q_WF_EXECUTED_BY", userID);
    properties.put("Q_WF_STARTED_AT", getTime());
    properties.put("Q_WF_STATUS", "RUNNING");
    params.put("properties", properties);
    openbis.ingest("DSS1", "register-exp", params);
    return code;
  }

  private Object getTime() {
    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
    return ft.format(dNow);
  }

  public List<String> getConnectedSamples(List<DatasetBean> datasetBeans) {
    List<String> sampleIDs = new ArrayList<String>();

    for (DatasetBean bean : datasetBeans) {
      sampleIDs.add(bean.getSampleIdentifier());
    }

    return sampleIDs;
  }

  public String registerWFSample(String space, String project, String experiment, String typecode,
      List<String> parents) {
    int last = 0;
    for (Sample s : openbis.getSamplesofExperiment("/" + space + "/" + project + "/" + experiment)) {
      String[] codeSplit = s.getCode().split("R");
      String number = codeSplit[codeSplit.length - 1];
      int num = 0;
      try {
        num = Integer.parseInt(number);
      } catch (NumberFormatException ex) {
      }
      last = Math.max(num, last);
    }

    String code = experiment + "R" + Integer.toString(last + 1);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", code);
    params.put("type", typecode);

    params.put("sample_class", "");
    params.put("parents", parents);

    params.put("project", project);
    params.put("space", space);
    params.put("experiment", experiment);

    Map<String, Object> properties = new HashMap<String, Object>();
    // TODO fill properties
    params.put("properties", properties);

    openbis.ingest("DSS1", "register-samp", params);
    return code;
  }


  /**
   * Set the workflow ID for a workflow experiment. This must be the experiment whose code has been
   * given to the submitter to ensure correct registration of the results and log files.
   * 
   * @param space space code
   * @param project project code
   * @param experiment experiment code
   * @param wfID workflow ID created by the submitter for this workflow experiment
   */
  public void setWorkflowID(String space, String project, String experiment, String wfID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("identifier", "/" + space + "/" + project + "/" + experiment);
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Q_WF_ID", wfID);
    params.put("properties", properties);
    openbis.ingest("DSS1", "notify-user", params);
  }

  /**
   * returns all known workflows, that can be executed with the given filetype
   * 
   * @param fileType
   * @return
   */
  public BeanItemContainer<Workflow> suitableWorkflows(String fileType) {
    try {
      return submitter.getAvailableSuitableWorkflows(fileType);
    } catch (Exception e) {
      e.printStackTrace();
      return new BeanItemContainer<Workflow>(Workflow.class);
    }
  }

  /**
   * returns all known workflows, that can be executed with one of the given filetypes
   * 
   * @param fileType
   * @return
   */
  public BeanItemContainer<Workflow> suitableWorkflows(List<String> fileType) {
    try {
      return submitter.getAvailableSuitableWorkflows(fileType);
    } catch (Exception e) {
      e.printStackTrace();
      return new BeanItemContainer<Workflow>(Workflow.class);
    }
  }

  public BeanItemContainer<DatasetBean> getcontainer(String id) {
    List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets =
        openbis.getClientDatasetsOfProjectByIdentifierWithSearchCriteria(id);
    return (BeanItemContainer<DatasetBean>) fillTable(datasets);
  }

  public Submitter getSubmitter() {
    return submitter;
  }

  public String submitAndRegisterWf(String type, String id, Workflow workflow,
      List<DatasetBean> selectedDatasets) throws ConnectException, IllegalArgumentException,
      SubmitFailedException {
    SpaceAndProjectCodes spaceandproject = getSpaceAndProjects(type, id);
    
    String spaceCode = spaceandproject.space;
    String projectCode = spaceandproject.project;
    
    
    String experimentCode =
        registerWFExperiment(spaceCode, projectCode, workflow.getExperimentType(), workflow.getID(),
            workflow.getVersion(), user);


    List<String> parents = getConnectedSamples(selectedDatasets);
    String sampleType = workflow.getSampleType();

    String sampleCode = registerWFSample(spaceCode, projectCode, experimentCode, sampleType, parents);

    String openbisId =
        String.format("%s-%s-%s-%s", spaceCode, projectCode, experimentCode, sampleCode);

    LOGGER.info("User: " + user + " is submitting workflow " + workflow.getID() + " openbis id is:"
        + openbisId);
    String submit_id = submitter.submit(workflow, openbisId, user);
    LOGGER.info("Workflow has guse id: " + submit_id);

    setWorkflowID(spaceCode, projectCode, experimentCode, submit_id);
    return openbisId;
  }

  private SpaceAndProjectCodes getSpaceAndProjects(String type, String id) {
    String[] split = id.split("/");
    
    if(split.length == 0) return null;
    switch (type) {
      case PatientView.navigateToLabel:
      case ProjectView.navigateToLabel:
      case ExperimentView.navigateToLabel:
        return new SpaceAndProjectCodes(split[1],split[2]);
      case SampleView.navigateToLabel:
        String expId = openbis.getSampleByIdentifier(String.format("%s/%s", split[1],split[2])).getExperimentIdentifierOrNull();
        if(expId == null)
          return null;
        return getSpaceAndProjects(ExperimentView.navigateToLabel,expId);
      case DatasetView.navigateToLabel:
        throw new NotImplementedException("Dataset view is not ready for workflows!");
    default:
      LOGGER.debug(String.format("Problem with id %s, type %s", id, type));
      return null;     
    }
  }

  class SpaceAndProjectCodes{
    public String space;
    public String project;
    public SpaceAndProjectCodes(String spaceCode, String projectCode){
      this.space = spaceCode;
      this.project = projectCode;
    }
    
  }

}
