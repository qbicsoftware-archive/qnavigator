package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import main.OpenBisClient;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.server.Resource;

public class TestGraphGenerator {
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testTimeForGeneration() {
    Properties config = new Properties();
    try {
      config.load(new FileReader("/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
    }catch(IOException e){
      e.printStackTrace();
    }
    OpenBisClient openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL), false);
    
    long time = 0;
    int reruns = 5;
    String project = "QKSFF";
    int samples = 80;
    int ds = 40;

    for(int i = 0; i < reruns; i++){
      long start = System.nanoTime();
      GraphGenerator graphFrame;
      try {
        graphFrame = new GraphGenerator(project, openbisClient);
        Resource resource = graphFrame.getRes();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      long stop = System.nanoTime();
      time += stop - start;
    }
    printRunTimeInfo("GraphGenerator", time, reruns, project, samples, ds);

    time = 0;
    project = "/TEST28/QTEST";
    samples = 352;
    ds = 29;
    for(int i = 0; i < reruns; i++){
      long start = System.nanoTime();
      GraphGenerator graphFrame;
      try {
        graphFrame = new GraphGenerator(project, openbisClient);
        Resource resource = graphFrame.getRes();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      long stop = System.nanoTime();
      time += stop - start;
    }
    printRunTimeInfo("GraphGenerator", time, reruns, project, samples, ds);    
    
    //QJFPH 160 150
    time = 0;
    project = "QJFPH";
    samples = 160;
    ds = 150;
    for(int i = 0; i < reruns; i++){
      long start = System.nanoTime();
      GraphGenerator graphFrame;
      try {
        graphFrame = new GraphGenerator(project, openbisClient);
        Resource resource = graphFrame.getRes();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      long stop = System.nanoTime();
      time += stop - start;
    }
    printRunTimeInfo("GraphGenerator", time, reruns, project, samples, ds);
  }
  public void printRunTimeInfo(String funcname, long time, int reruns, String project, int samples, int ds){
    System.out.println(String.format("%s took %f s for %s reloads for project %s with ~ %d samples and %d datasets Makes %f s on average. ",funcname, time /1000000000.0, reruns, project,samples,ds, time / 1000000000.0 / reruns));
  }

}
