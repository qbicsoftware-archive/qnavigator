package de.uni_tuebingen.qbic.qbicmainportlet;

import static com.google.common.truth.Truth.ASSERT;
import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.AbstractMap.SimpleEntry;

import main.OpenBisClient;
import model.DatasetBean;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;
import model.SpaceBean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.databene.contiperf.report.ReportModule;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Image;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;



public class TestQbicmainportletUI {
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static Properties config;
  QbicmainportletUI ui = new QbicmainportletUI();
  @Rule
  public ContiPerfRule i = new ContiPerfRule();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    config = new Properties();
    config
        .load(new FileReader(
            "/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  private DataHandler datahandler;
  private OpenBisClient openbisClient;

  @Before
  public void setUp() throws Exception {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    datahandler = new DataHandler(openbisClient);
  }

  @After
  public void tearDown() throws Exception {}

  //@Test
  //@PerfTest(invocations = 100, threads = 1)
  public void prepareHomeSpaceBean() {
    HierarchicalContainer tc = new HierarchicalContainer();
    SpaceBean homeSpaceBean = null;
    String user = "iiswo01";

    RunnableFillsContainer rfc =
        ui.new RunnableFillsContainer(datahandler, tc, homeSpaceBean, user, null);
    rfc.prepareHomeSpaceBean(openbisClient.getFacade().getSpacesWithProjects(), false);
  }

 // @Test
  //@PerfTest(invocations = 100, threads = 1)
  public void setContainers() {
    HierarchicalContainer tc = new HierarchicalContainer();
    // Initialization of Tree Container
    tc.addContainerProperty("identifier", String.class, "N/A");
    tc.addContainerProperty("type", String.class, "N/A");
    tc.addContainerProperty("project", String.class, "N/A");
    tc.addContainerProperty("caption", String.class, "N/A");
    
    String userName = "iiswo01";
    QbicmainportletUI ui = new QbicmainportletUI();
    final SpaceBean homeSpaceBean =
        new SpaceBean("homeSpace", "", false, null, null, null, null, null, null);
    RunnableFillsContainer rfc =
        ui.new RunnableFillsContainer(datahandler, tc, homeSpaceBean, userName, null);



    BeanItemContainer<ProjectBean> projectContainer =
        new BeanItemContainer<ProjectBean>(ProjectBean.class);
    BeanItemContainer<ExperimentBean> allExperimentsContainer =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    BeanItemContainer<SampleBean> allSamplesContainer =
        new BeanItemContainer<SampleBean>(SampleBean.class);
    BeanItemContainer<DatasetBean> allDatasetsContainer =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);

    List<String> project_identifiers_tmp = new ArrayList<String>();

    Boolean patientCreation = false;
    List<SpaceWithProjectsAndRoleAssignments> spaceList =
        datahandler.openBisClient.getFacade().getSpacesWithProjects();
    for (SpaceWithProjectsAndRoleAssignments s : spaceList) {
      if (s.getUsers().contains(userName)) {
        rfc.setContainers(s, patientCreation, project_identifiers_tmp, projectContainer,
            allExperimentsContainer);
      }
    }
  }
  
  //@Test
  //@PerfTest(invocations = 25, threads = 1) //77 ms
  public void listProjectsOnBehalfOfUser_projects(){
    List<Project> projects = openbisClient.getOpenbisInfoService().listProjectsOnBehalfOfUser(openbisClient.getSessionToken(), "iiswo01");
    for(Project p : projects){
      p.getIdentifier();
    }
  }
  
  //@Test
  //@PerfTest(invocations = 25, threads = 1) //568 ms 
  public void listProjectsOnBehalfOfUser_spaces() {
    List<SpaceWithProjectsAndRoleAssignments> spaceList =
        openbisClient.getFacade().getSpacesWithProjects();
    for (SpaceWithProjectsAndRoleAssignments s : spaceList) {
      if (s.getUsers().contains("iiswo01")) {
       for(Project p: s.getProjects()){
         p.getIdentifier();
       }
      }
    }
  }
  @Test
  @PerfTest(invocations = 25, threads = 1)
  public void prepareHomeSpaceBean_new() {
    HierarchicalContainer tc = new HierarchicalContainer();
    SpaceBean homeSpaceBean = null;
    String user = "iiswo01";
    fail();
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 1)
  public void prepHomeView(){
    String user = "iiswo01";
    List<Project> projects = openbisClient.getOpenbisInfoService().listProjectsOnBehalfOfUser(openbisClient.getSessionToken(), user);
    for(Project p : projects){
      p.getIdentifier();
    }
  }
  
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void datahandler_createProject(){
    List<Project> projects = datahandler.openBisClient.listProjects();
    for(Project p: projects){
      ProjectBean pbean =datahandler.createProjectBean(p);
      System.out.println(pbean.getId());
    }
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void datahandler_createExperimentBean(){
    List<Project> projects = datahandler.openBisClient.listProjects();
    for(Project p: projects){
      for(Experiment exp: openbisClient.getExperimentsForProject(p)){
        ExperimentBean ebean = datahandler.createExperimentBean(exp);
        System.out.println(ebean.getId());
      }
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 1)
  public void datahandler_openBisClient_getProjectByIdentifier(){
    Project project = datahandler.openBisClient.getProjectByIdentifier("/ABI_SYSBIO/QMARI");
    project.getId();
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void datahandler_getProject2(){
    ProjectBean pbean = datahandler.getProject2("/ABI_SYSBIO/QMARI");
    System.out.println(pbean.getExperiments().size());
    
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void datahandler_getProject(){
    ProjectBean pbean = datahandler.getProject("/ABI_SYSBIO/QMARI");
    pbean.getExperiments();
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void customVaadinPortlet_convertDatasetsToEntries(){
    CustomVaadinPortlet cvp = new CustomVaadinPortlet();
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/ABI_SYSBIO/QMARI");
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries = cvp.convertDatasetsToEntries(datasets);
    
    System.out.println(entries.size());
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void customVaadinPortlet_convertDatasetsToEntries2(){
    CustomVaadinPortlet cvp = new CustomVaadinPortlet();
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/MFT_FRICK_MICROBIOSTIMUL/QJFDC");
    System.out.println("read datasets");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("starting..");
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries = cvp.convertDatasetsToEntries(datasets);
    
    System.out.println(entries.size());
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void customVaadinPortlet_addDatasetFiles(){
    CustomVaadinPortlet cvp = new CustomVaadinPortlet();
    DataSet dataset = datahandler.openBisClient.getFacade().getDataSet("20150224174804803-6961");
    Map<String, SimpleEntry<String, Long>> entries = new HashMap<String, SimpleEntry<String, Long>>();
    FileInfoDssDTO[] filelist = dataset.listFiles("original", true);
    String folderPath = filelist[0].getPathInDataSet();
    cvp.addDatasetFiles(dataset.listFiles(folderPath, false),dataset,entries);
  }
  
  @Test
  public void customVaadinPortlet_addDatasetFiles_(){
    CustomVaadinPortlet cvp = new CustomVaadinPortlet();
    DataSet dataset = datahandler.openBisClient.getFacade().getDataSet("20150224174804803-6961");
    Map<String, SimpleEntry<String, Long>> entries = new HashMap<String, SimpleEntry<String, Long>>();
    FileInfoDssDTO[] filelist = dataset.listFiles("original", true);
    String folderPath = filelist[0].getPathInDataSet();
    cvp.addDatasetFiles(dataset.listFiles(folderPath, false),dataset,entries);
    ASSERT.that(entries.size()).isEqualTo(24);
   }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void listFilesOfDatasets_QJFDC(){
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/MFT_FRICK_MICROBIOSTIMUL/QJFDC");
    for(DataSet dataset: datasets){
      FileInfoDssDTO[] filelist = dataset.listFiles("original", true);
      System.out.println(filelist.length);
      filelist[0].getPathInDataSet();
      filelist[0].getFileSize();
    }
  }
  @Test
  @PerfTest(invocations = 4, threads = 1)
  public void listFilesOfDatasets_QMARI(){
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/ABI_SYSBIO/QMARI");
    for(int i = 0; i< datasets.size()/4;i++){
      DataSet dataset = datasets.get(i);
      FileInfoDssDTO[] filelist = dataset.listFiles("original", true);
      //System.out.println(dataset.getCode());
      filelist[0].getPathInDataSet();
      filelist[0].getFileSize();
    }
  }

  @Test
  @PerfTest(invocations = 25, threads = 1)
  public void listFilesOfDatasetsWithAggregtionService_QMARI(){
    List<String> codes = new ArrayList<String>();
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/ABI_SYSBIO/QMARI");
    for(DataSet dataset: datasets) {
      codes.add(dataset.getCode());
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("codes", codes);
    QueryTableModel res = openbisClient.getAggregationService("query-files", params);
    for (Serializable[] ss : res.getRows()) {
      System.out.println("next");
      for(Object x : ss)
        System.out.println(x.toString());
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 1)
  public void listFilesOfDatasetsWithAggregtionService_wrapped_QMARI(){
    List<String> codes = new ArrayList<String>();
    List<DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/ABI_SYSBIO/QMARI");
    for(DataSet dataset: datasets) {
      codes.add(dataset.getCode());
    }
    Map<String, List<String>> params = new HashMap<String, List<String>>();
    params.put("codes", codes);
    QueryTableModel res = openbisClient.queryFileInformation(params);
    for (Serializable[] ss : res.getRows()) {
      System.out.println("next");
      for(Object x : ss)
        System.out.println(x.toString());
    }
  } 
  
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void split(){
    String openbisId = "/ABI_SYSBIO/QMARI";
    String [] split = openbisId.split("/");
    for(String s: split){
      System.out.println(s);
    }
    System.out.println(split.length);
     openbisId = "/ABI_SYSBIO/QMARI/QMARIE3";
    split = openbisId.split("/");
    for(String s: split){
      System.out.println(s);
    }
    System.out.println(split.length);
  }
 
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void instanceofString(){
    String openbisId = null;
    assert !(openbisId instanceof String);
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1)
  public void datahandler_getExperiment2(){
   ExperimentBean ebean =  datahandler.getExperiment2("/ABI_SYSBIO/QMARI/QMARIE3");
   System.out.println(ebean.toString());
   assert ebean != null;
   assert ebean.getSamples().size() > 0;
  }
  
  
}
