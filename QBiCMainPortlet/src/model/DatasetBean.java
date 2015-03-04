package model;

import java.io.Serializable;
import java.sql.Timestamp;

import com.vaadin.ui.CheckBox;

public class DatasetBean implements Serializable{
  
  
  /**
   * 
   */
  private static final long serialVersionUID = 4275310001607043674L;

  //is the bean selected in the table?
  private boolean isSelected;
  
  //all information in its linked parents.
  private ProjectBean project;
  private SampleBean sample;
  private ExperimentBean experiment;
  
  //openbis code
  private String code;
  //file or directory name on dss 
  private String name;
  //type of this dataset
  private String type;
  
  //TODO to bytes method
  //size of this dataset on dss.
  private long fileSize;
  //same as {@link fileSize} but human readable format
  private String humanReadableFileSize;
  
  
  //path to the actual file or directory of this dataset on the dss.
  private String dssPath;
  //date of the registration of this dataset (not necessary when data arrives)
  private Timestamp registrationDate;
  //name of the registrator
  //TODO class user or liferay user?
  private String registrator;
  
  //If it is a directory, the file structure has to taken with care.
  private boolean isDirectory;
  
  
  
  public DatasetBean(boolean isSelected, ProjectBean project, SampleBean sample,
      ExperimentBean experiment, String code, String name, String type, long fileSize,
      String humanReadableFileSize, String dssPath, Timestamp registrationDate, String registrator,
      boolean isDirectory) {
    super();
    this.isSelected = isSelected;
    this.project = project;
    this.sample = sample;
    this.experiment = experiment;
    this.code = code;
    this.name = name;
    this.type = type;
    this.fileSize = fileSize;
    this.humanReadableFileSize = humanReadableFileSize;
    this.dssPath = dssPath;
    this.registrationDate = registrationDate;
    this.registrator = registrator;
    this.isDirectory = isDirectory;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getHumanReadableFileSize() {
    return humanReadableFileSize;
  }

  public void setHumanReadableFileSize(String humanReadableFileSize) {
    this.humanReadableFileSize = humanReadableFileSize;
  }

  public String getDssPath() {
    return dssPath;
  }

  public void setDssPath(String dssPath) {
    this.dssPath = dssPath;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public ProjectBean getProject() {
    return project;
  }
  public void setProject(ProjectBean project) {
    this.project = project;
  }
  public ExperimentBean getExperiment() {
    return experiment;
  }
  public void setExperiment(ExperimentBean experiment) {
    this.experiment = experiment;
  }
  public SampleBean getSample() {
    return sample;
  }
  public void setSample(SampleBean sample) {
    this.sample = sample;
  }
  public String getFileName() {
    return name;
  }
  public void setFileName(String fileName) {
    this.name = fileName;
  }
  public String getFileType() {
    return type;
  }
  public void setFileType(String fileType) {
    this.type = fileType;
  }
  public long getFileSize() {
    return fileSize;
  }
  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }
  public String getFileDownloadLink() {
    return dssPath;
  }
  public void setFileDownloadLink(String fileDownloadLink) {
    this.dssPath = fileDownloadLink;
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

  @Override
  public String toString() {
    return "DatasetBean [code=" + code + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
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
    DatasetBean other = (DatasetBean) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    return true;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
  }
  
}
