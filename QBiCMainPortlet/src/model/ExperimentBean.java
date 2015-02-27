package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.vaadin.ui.Image;

public class ExperimentBean implements Serializable{
  
  private String id;
  private String code;
  private String type;
  private String description;
  private String parents;
  private String species;
  private Image status;
  private String registrator;
  private Timestamp registrationDate;
  private List<String> samples;
  
  public ExperimentBean(String id, String code, String type, String description, String parents,
      String species, Image status, String registrator, Timestamp registrationDate,
      List<String> samples) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.description = description;
    this.parents = parents;
    this.species = species;
    this.status = status;
    this.registrator = registrator;
    this.registrationDate = registrationDate;
    this.samples = samples;
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
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getParents() {
    return parents;
  }
  public void setParents(String parents) {
    this.parents = parents;
  }
  public String getSpecies() {
    return species;
  }
  public void setSpecies(String species) {
    this.species = species;
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
  public List<String> getSamples() {
    return samples;
  }
  public void setSamples(List<String> samples) {
    this.samples = samples;
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
