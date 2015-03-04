package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ProgressBar;

public class ProjectBean implements Serializable{
  
  private String id;
  private String code;
  private String description;
  private SpaceBean space;
  private BeanItemContainer<ExperimentBean> experiments;
  private ProgressBar progress;
  private Timestamp registrationDate;
  private String registrator;
  private String contact;
  //TODO userBean ?
  private List<String> members;
  
  public ProjectBean(String id, String code, String description, SpaceBean space,
      BeanItemContainer<ExperimentBean> experiments, ProgressBar progress, Timestamp registrationDate,
      String registrator, String contact, List<String> members) {
    super();
    this.id = id;
    this.code = code;
    this.description = description;
    this.space = space;
    this.experiments = experiments;
    this.progress = progress;
    this.registrationDate = registrationDate;
    this.registrator = registrator;
    this.contact = contact;
    this.members = members;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SpaceBean getSpace() {
    return space;
  }

  public void setSpace(SpaceBean space) {
    this.space = space;
  }

  public BeanItemContainer<ExperimentBean> getExperiments() {
    return experiments;
  }

  public void setExperiments(BeanItemContainer<ExperimentBean> experiments) {
    this.experiments = experiments;
  }

  public ProgressBar getProgress() {
    return progress;
  }

  public void setProgress(ProgressBar progress) {
    this.progress = progress;
  }

  public Timestamp getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(Timestamp registrationDate) {
    this.registrationDate = registrationDate;
  }

  public String getRegistrator() {
    return registrator;
  }

  public void setRegistrator(String registrator) {
    this.registrator = registrator;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  @Override
  public String toString() {
    return "ProjectBean [id=" + id + "]";
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
    ProjectBean other = (ProjectBean) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }  
}
