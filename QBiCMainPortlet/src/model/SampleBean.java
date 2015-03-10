package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import parser.Parser;
import properties.Qproperties;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;

public class SampleBean implements Comparable<Object> {

  private String id;
  private String code;
  private String type;
  // Map containing parents of the sample and the corresponding sample types
  //private Map<String, String> parents;
  private List<Sample> parents;
  private List<Sample> children;
  private BeanItemContainer<DatasetBean> datasets;
  private Date lastChangedDataset;
  private Map<String, String> properties;
  private Map<String, String> typeLabels;

  public SampleBean(String id, String code, String type, List<Sample> parents,
      BeanItemContainer<DatasetBean> datasets, Date lastChangedDataset,
      Map<String, String> properties, Map<String, String> typeLabels, List<Sample> children) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.parents = parents;
    this.datasets = datasets;
    this.lastChangedDataset = lastChangedDataset;
    this.properties = properties;
    this.typeLabels = typeLabels;
    this.children = children;
  }
  
  public SampleBean() {
    
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



  public List<Sample> getParents() {
    return parents;
  }



  public void setParents(List<Sample> parents) {
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
  
  public Map<String, String> getTypeLabels() {
    return typeLabels;
  }

  public void setTypeLabels(Map<String, String> typeLabels) {
    this.typeLabels = typeLabels;
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
  
  public String getParentsFormattedString() {
    String parentsHeader = "This sample has been derived from the following samples: ";
    String parentsBottom = "<ul>";

    if (this.getParents().isEmpty()) {
      return  parentsHeader += "None";

    } else {
        for(Sample sample: this.getParents()) {
        parentsBottom += "<li><b>" + sample.getCode() + "</b> (" + sample.getSampleTypeCode() + ") </li>";
      }
      parentsBottom += "</ul>";
      
      return parentsHeader + parentsBottom;
    }
  }
  
  public String generatePropertiesFormattedString() throws JAXBException {
    String propertiesBottom = "<ul> ";

    Iterator<Entry<String, String>> it = this.getProperties().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      if (pairs.getKey().equals("Q_PROPERTIES")) {
        continue;
        }
      else {
       propertiesBottom += "<li><b>" + (typeLabels.get(pairs.getKey()) + ":</b> " + pairs.getValue() + "</li>");
      }
    }
    propertiesBottom += "</ul>";

    return propertiesBottom;
  }
  
  public String generateXMLPropertiesFormattedString() throws JAXBException {

    String xmlPropertiesBottom = "<ul> ";

    Iterator<Entry<String, String>> it = this.getProperties().entrySet().iterator();
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
                  + (typeLabels.get(pairsProperties.getKey()) + ":</b> " + pairsProperties.getValue() + "</li>");
        }
        break;
      }
    }
    return xmlPropertiesBottom;
}

  public List<Sample> getChildren() {
    return children;
  }

  public void setChildren(List<Sample> children) {
    this.children = children;
  }
  
}
