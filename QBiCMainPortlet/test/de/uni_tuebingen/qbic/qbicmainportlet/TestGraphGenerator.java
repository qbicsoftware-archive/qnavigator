package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import main.OpenBisClient;
import model.ProjectBean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;

import com.vaadin.server.Resource;

public class TestGraphGenerator {
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static Properties config;

  @Rule
  // public ContiPerfRule i = new ContiPerfRule();
  private OpenBisClient openbisClient;
  private DataHandler datahandler;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    config = new Properties();
    config
        .load(new FileReader(
            "/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    // datahandler = new DataHandler(openbisClient);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  // @PerfTest(invocations =4, threads = 4)
  /*
   * samples: 4 max: 46803 average: 46795.75 median: 46796
   */
  public void testTimeForGeneration2_QMARI() {

    GraphGenerator graphFrame;
    try {
      List<Sample> samples = openbisClient.getSamplesOfProject("/ABI_SYSBIO/QMARI");
      Map<String, SampleType> types = openbisClient.getSampleTypes();
      graphFrame = new GraphGenerator(samples, types, openbisClient, "/ABI_SYSBIO/QMARI");
      Resource resource = graphFrame.getRes();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * samples: 4 max: 632303 average: 632285.75 median: 632288
   */
  @Test
  // //@PerfTest(invocations =4, threads = 4)
  public void testTimeForGeneration1_QMARI() {

    GraphGenerator graphFrame;
    try {
      ProjectBean pbean = datahandler.getProject("/ABI_SYSBIO/QMARI");
      graphFrame = new GraphGenerator(pbean, openbisClient);
      Resource resource = graphFrame.getRes();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * time = 0; project = "/TEST28/QTEST"; samples = 352; ds = 29; for (int i = 0; i < reruns; i++) {
   * long start = System.nanoTime(); GraphGenerator graphFrame; try { graphFrame = new
   * GraphGenerator(project, openbisClient); Resource resource = graphFrame.getRes(); } catch
   * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
   * 
   * long stop = System.nanoTime(); time += stop - start; } printRunTimeInfo("GraphGenerator", time,
   * reruns, project, samples, ds);
   * 
   * // QJFPH 160 150 time = 0; project = "QJFPH"; samples = 160; ds = 150; for (int i = 0; i <
   * reruns; i++) { long start = System.nanoTime(); GraphGenerator graphFrame; try { graphFrame =
   * new GraphGenerator(project, openbisClient); Resource resource = graphFrame.getRes(); } catch
   * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
   * 
   * long stop = System.nanoTime(); time += stop - start; } printRunTimeInfo("GraphGenerator", time,
   * reruns, project, samples, ds); }
   * 
   * public void printRunTimeInfo(String funcname, long time, int reruns, String project, int
   * samples, int ds) { System.out .println(String .format(
   * "%s took %f s for %s reloads for project %s with ~ %d samples and %d datasets Makes %f s on average. "
   * , funcname, time / 1000000000.0, reruns, project, samples, ds, time / 1000000000.0 / reruns));
   * }
   */
}
