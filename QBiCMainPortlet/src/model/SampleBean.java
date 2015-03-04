package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;

public class SampleBean implements Comparable<Object> {

  private String id;
  private String code;
  private String type;
  // Map containing parents of the sample and the corresponding sample types
  private Map<String, String> parents;
  private BeanItemContainer<DatasetBean> datasets;
  private Date lastChangedDataset;
  private Map<String, String> properties;
  private String xmlPropertiesFormattedString;

  public SampleBean(String id, String code, String type, Map<String, String> parents,
      BeanItemContainer<DatasetBean> datasets, Date lastChangedDataset,
      Map<String, String> properties, String xmlPropertiesFormattedString) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.parents = parents;
    this.datasets = datasets;
    this.lastChangedDataset = lastChangedDataset;
    this.properties = properties;
    this.xmlPropertiesFormattedString = xmlPropertiesFormattedString;
  }



  public String getId() {
    return id;
  }



  public void setId(String id) {
    this.id = id;
  }



  public String getCode() {
    return code;
  }



  public void setCode(String code) {
    this.code = code;
  }



  public String getType() {
    return type;
  }



  public void setType(String type) {
    this.type = type;
  }



  public Map<String, String> getParents() {
    return parents;
  }



  public void setParents(Map<String, String> parents) {
    this.parents = parents;
  }



  public BeanItemContainer<DatasetBean> getDatasets() {
    return datasets;
  }



  public void setDatasets(BeanItemContainer<DatasetBean> datasets) {
    this.datasets = datasets;
  }



  public Date getLastChangedDataset() {
    return lastChangedDataset;
  }



  public void setLastChangedDataset(Date lastChangedDataset) {
    this.lastChangedDataset = lastChangedDataset;
  }



  public Map<String, String> getProperties() {
    return properties;
  }



  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }



  public String getXmlPropertiesFormattedString() {
    return xmlPropertiesFormattedString;
  }



  public void setXmlPropertiesFormattedString(String xmlPropertiesFormattedString) {
    this.xmlPropertiesFormattedString = xmlPropertiesFormattedString;
  }



  @Override
  public int compareTo(Object o) {
    return id.compareTo(((SampleBean) o).getId());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SampleBean) {
      SampleBean b = (SampleBean) o;
      return id.equals(b.getId());
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
