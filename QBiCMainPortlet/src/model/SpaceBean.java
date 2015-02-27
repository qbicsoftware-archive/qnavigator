package model;

import java.io.Serializable;

import com.vaadin.data.Container;

public class SpaceBean implements Serializable{

  private String id;
  private String description;
  private String containsData;
  //TODO beans
  private Container projects;
  private Container experiments;
  private Container samples;
  private Container datasets;
  
  public SpaceBean(String id, String description, String containsData, Container projects,
      Container experiments, Container samples, Container datasets) {
    super();
    this.id = id;
    this.description = description;
    this.containsData = containsData;
    this.projects = projects;
    this.experiments = experiments;
    this.samples = samples;
    this.datasets = datasets;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContainsData() {
    return containsData;
  }

  public void setContainsData(String containsData) {
    this.containsData = containsData;
  }

  public Container getProjects() {
    return projects;
  }

  public void setProjects(Container projects) {
    this.projects = projects;
  }

  public Container getExperiments() {
    return experiments;
  }

  public void setExperiments(Container experiments) {
    this.experiments = experiments;
  }

  public Container getSamples() {
    return samples;
  }

  public void setSamples(Container samples) {
    this.samples = samples;
  }

  public Container getDatasets() {
    return datasets;
  }

  public void setDatasets(Container datasets) {
    this.datasets = datasets;
  }

  @Override
  public String toString() {
    return "SpaceBean [id=" + id + "]";
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
    SpaceBean other = (SpaceBean) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
 
}
