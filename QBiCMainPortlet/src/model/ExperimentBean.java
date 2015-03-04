package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Image;

public class ExperimentBean implements Serializable {

  private String id;
  private String code;
  private String type;
  private Image status;
  private String registrator;
  private Timestamp registrationDate;
  private BeanItemContainer<SampleBean> samples;
  private String lastChangedSample;
  private Date lastChangedDataset;
  private Map<String, String> properties;
  private Map<String, List<String>> controlledVocabularies;

  public ExperimentBean(String id, String code, String type, Image status, String registrator,
      Timestamp registrationDate, BeanItemContainer<SampleBean> samples, String lastChangedSample,
      Date lastChangedDataset, Map<String, String> properties,
      Map<String, List<String>> controlledVocabularies) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.status = status;
    this.registrator = registrator;
    this.registrationDate = registrationDate;
    this.samples = samples;
    this.lastChangedSample = lastChangedSample;
    this.lastChangedDataset = lastChangedDataset;
    this.properties = properties;
    this.controlledVocabularies = controlledVocabularies;
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



  public Image getStatus() {
    return status;
  }



  public void setStatus(Image status) {
    this.status = status;
  }



  public String getRegistrator() {
    return registrator;
  }



  public void setRegistrator(String registrator) {
    this.registrator = registrator;
  }



  public Timestamp getRegistrationDate() {
    return registrationDate;
  }



  public void setRegistrationDate(Timestamp registrationDate) {
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
}
