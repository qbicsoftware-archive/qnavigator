package de.uni_tuebingen.qbic.qbicmainportlet;

import static com.google.common.truth.Truth.ASSERT;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.DatasetBean;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;
import model.SpaceBean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ProgressBar;

public class TestCustomVaadinPortlet {
  static ProjectBean projectBean;
  static DatasetBean datasetBean1;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    SpaceBean spaceBean =
        new SpaceBean("space-id", "i am a space", true, null, null, null, null, null, null);

    BeanItemContainer<DatasetBean> datasets1 =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    BeanItemContainer<DatasetBean> datasets2 =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    BeanItemContainer<DatasetBean> datasets3 =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    BeanItemContainer<DatasetBean> datasets4 =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);

    List<DatasetBean> children1 = new ArrayList<DatasetBean>();
    datasetBean1 = new DatasetBean();
    DatasetBean datasetBean01 =
        new DatasetBean(new CheckBox(), null, null, null, "dataset01-code", "dataset01.txt", "txt",
            5, "5 B", "/dss/openbis/oscureCode1/dataset01.txt", new Date(), "me", false,
            datasetBean1, datasetBean1, null);
    DatasetBean datasetBean02 =
        new DatasetBean(new CheckBox(), null, null, null, "dataset02-code", "dataset02.html",
            "html", 10, "10 B", "/dss/openbis/oscureCode1/dataset02.html", new Date(), "me", false,
            datasetBean1, datasetBean1, null);
    children1.add(datasetBean02);
    children1.add(datasetBean01);
    datasetBean1.setChildren(children1);
    datasetBean1.setCode("dataset1-code");
    datasetBean1.setDirectory(true);
    datasetBean1.setDssPath("/dss/openbis/oscureCode1");
    datasetBean1.setExperiment(null);
    datasetBean1.setFileName("oscureCode1");
    datasetBean1.setFileSize(-1);
    datasetBean1.setFileType("folder");
    datasetBean1.setHumanReadableFileSize("-1");
    datasetBean1.setParent(null);
    datasetBean1.setProject(null);
    datasetBean1.setRegistrationDate(new Date());
    datasetBean1.setRegistrator("me");
    datasetBean1.setRoot(null);
    datasetBean1.setSample(null);
    datasetBean1.setSelected(false);

    datasets1.addBean(datasetBean1);


    BeanItemContainer<SampleBean> samples1 = new BeanItemContainer<SampleBean>(SampleBean.class);
    BeanItemContainer<SampleBean> samples2 = new BeanItemContainer<SampleBean>(SampleBean.class);
    SampleBean sample1 =
        new SampleBean("sample1-id", "sample1-code", "sample1-type", null, datasets1, new Date(),
            null, null, null);
    SampleBean sample2 =
        new SampleBean("sample2-id", "sample2-code", "sample2-type", null, datasets2, new Date(),
            null, null, null);
    SampleBean sample3 =
        new SampleBean("sample3-id", "sample3-code", "sample3-type", null, datasets3, new Date(),
            null, null, null);
    SampleBean sample4 =
        new SampleBean("sample4-id", "sample4-code", "sample4-type", null, datasets4, new Date(),
            null, null, null);

    samples1.addBean(sample1);
    samples1.addBean(sample2);
    samples1.addBean(sample3);
    samples2.addBean(sample4);



    ExperimentBean expbean1 =
        new ExperimentBean("expbean1-id", "expbean1-code", "expbean1-type", "", "Lister",
            new Date(), samples1, "sample1-id", new Date(), new HashMap<String, String>(),
            new HashMap<String, List<String>>(), new HashMap<String, String>());
    ExperimentBean expbean2 =
        new ExperimentBean("expbean2-id", "expbean2-code", "expbean2-type", "", "Lister",
            new Date(), samples2, "sample4-id", new Date(), new HashMap<String, String>(),
            new HashMap<String, List<String>>(), new HashMap<String, String>());
    BeanItemContainer<ExperimentBean> expbeans =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    expbeans.addBean(expbean1);
    expbeans.addBean(expbean2);
    Set<String> members = new HashSet<String>();

    projectBean =
        new ProjectBean("project-id", "project-code", "secName", "project-description",
            spaceBean.getId(), expbeans, new ProgressBar(0.25f), new Date(), "Davinci",
            "Donatello", members, true, "projManager");
    datasetBean1.setProject(projectBean);
    datasetBean1.setExperiment(expbean1);
    datasetBean1.setSample(sample1);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {


  }

  @After
  public void tearDown() throws Exception {}



  @Test
  public void testAddEntry() {
    CustomVaadinPortlet vaadinPortlet = new CustomVaadinPortlet();
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
        new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
    vaadinPortlet.addEntry(datasetBean1, entries);
    ASSERT.that(entries.size()).isEqualTo(2);
    ASSERT.that(entries.containsKey("dataset1-code/oscureCode1/dataset01.txt")).comparesEqualTo(
        true);
    ASSERT.that(entries.containsKey("dataset1-code/oscureCode1/dataset02.html")).comparesEqualTo(
        true);
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset01.txt").getKey()).isEqualTo(
        "dataset01-code");
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset02.html").getKey()).isEqualTo(
        "dataset02-code");
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset01.txt").getValue()).isEqualTo(5);
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset02.html").getValue()).isEqualTo(10);
  }

  @Test
  public void testConvertBeanToEntries() {
    CustomVaadinPortlet vaadinPortlet = new CustomVaadinPortlet();
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
        vaadinPortlet.convertBeanToEntries(projectBean);
    ASSERT.that(entries.size()).isEqualTo(2);
    ASSERT.that(entries.size()).isEqualTo(2);
    ASSERT.that(entries.containsKey("dataset1-code/oscureCode1/dataset01.txt")).comparesEqualTo(
        true);
    ASSERT.that(entries.containsKey("dataset1-code/oscureCode1/dataset02.html")).comparesEqualTo(
        true);
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset01.txt").getKey()).isEqualTo(
        "dataset01-code");
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset02.html").getKey()).isEqualTo(
        "dataset02-code");
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset01.txt").getValue()).isEqualTo(5);
    ASSERT.that(entries.get("dataset1-code/oscureCode1/dataset02.html").getValue()).isEqualTo(10);
  }

}
