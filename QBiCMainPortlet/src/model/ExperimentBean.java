package model;

import helpers.UglyToPrettyNameMapper;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Image;

public class ExperimentBean implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1927159369993633824L;
  private String id;
  private String code;
  private String type;
  private String prettyType;
  private String status;
  private String registrator;
  private Date registrationDate;
  private BeanItemContainer<SampleBean> samples;
  private String lastChangedSample;
  private Date lastChangedDataset;
  private Map<String, String> properties;
  private Map<String, List<String>> controlledVocabularies;
  private Map<String, String> typeLabels;
  private Boolean containsData;

  private UglyToPrettyNameMapper prettyNameMapper = new UglyToPrettyNameMapper();
  
  public Map<String, String> getTypeLabels() {
    return typeLabels;
  }



  public void setTypeLabels(Map<String, String> typeLabels) {
    this.typeLabels = typeLabels;
  }



  public ExperimentBean(String id, String code, String type, String status, String registrator,
      Date registrationDate, BeanItemContainer<SampleBean> samples, String lastChangedSample,
      Date lastChangedDataset, Map<String, String> properties,
      Map<String, List<String>> controlledVocabularies, Map<String, String> typeLabels) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.prettyType = prettyNameMapper.getPrettyName(type);
    this.type = this.prettyType;
    this.status = status;
    this.registrator = registrator;
    this.registrationDate = registrationDate;
    this.samples = samples;
    this.lastChangedSample = lastChangedSample;
    this.lastChangedDataset = lastChangedDataset;
    this.properties = properties;
    this.controlledVocabularies = controlledVocabularies;
    this.typeLabels = typeLabels;
  }



  public ExperimentBean() {
    // TODO Auto-generated constructor stub
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
    this.type = prettyNameMapper.getPrettyName(type);
   //this.type = type;
  }



  public String getStatus() {
    return status;
  }



  public void setStatus(String status) {
    this.status = status;
  }



  public String getRegistrator() {
    return registrator;
  }


  public void setRegistrator(String registrator) {
    this.registrator = registrator;
  }



  public Date getRegistrationDate() {
    return registrationDate;
  }



  public void setRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate;
  }



  public BeanItemContainer<SampleBean> getSamples() {
    return samples;
  }



  public void setSamples(BeanItemContainer<SampleBean> samples) {
    this.samples = samples;
  }



  public String getLastChangedSample() {
    return lastChangedSample;
  }



  public void setLastChangedSample(String lastChangedSample) {
    this.lastChangedSample = lastChangedSample;
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



  public Map<String, List<String>> getControlledVocabularies() {
    return controlledVocabularies;
  }



  public void setControlledVocabularies(Map<String, List<String>> controlledVocabularies) {
    this.controlledVocabularies = controlledVocabularies;
  }



  @Override
  public String toString() {
    return "ExperimentBean [id=" + id + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ExperimentBean other = (ExperimentBean) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  public String generatePropertiesFormattedString() {
    String propertiesHeader = "<ul>";
    String propertiesBottom = "";

    Iterator<Entry<String, String>> it = this.getProperties().entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String> pairs = it.next();
      if (pairs.getValue().equals("")) {
        continue;
      } else if (pairs.getKey().equals("Q_PERSONS")) {
        continue;
      } else {
        propertiesBottom +=
            "<li><b>" + (typeLabels.get(pairs.getKey()) + ":</b> " + pairs.getValue() + "</li>");
        // propertiesBottom +=
        // "<li><b>"
        // + (pairs.getKey().toString() + ":</b> "
        // + pairs.getValue() + "</li>");
      }
    }
    propertiesBottom += "</ul>";

    return propertiesHeader + propertiesBottom;
  }



  public Boolean getContainsData() {
    return containsData;
  }



  public void setContainsData(Boolean containsData) {
    this.containsData = containsData;
  }
}
