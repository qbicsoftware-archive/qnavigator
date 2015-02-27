package model;

import java.io.Serializable;
import java.sql.Timestamp;

import com.vaadin.ui.CheckBox;

public class DatasetBean implements Serializable{
  
  //TODO beans?
  private String code;
  private String project;
  private String experiment;
  private String sample;
  private String fileName;
  private String fileType;
  //TODO to bytes method
  private String fileSize;
  private String fileDownloadLink;
  private Timestamp registrationDate;
  private String registrator;
  
  public DatasetBean(String code, String project, String experiment, String sample,
      String fileName, String fileType, String fileSize, String fileDownloadLink,
      Timestamp registrationDate, String registrator) {
    super();
    this.code = code;
    this.project = project;
    this.experiment = experiment;
    this.sample = sample;
    this.fileName = fileName;
    this.fileType = fileType;
    this.fileSize = fileSize;
    this.fileDownloadLink = fileDownloadLink;
    this.registrationDate = registrationDate;
    this.registrator = registrator;
  }
  
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public String getProject() {
    return project;
  }
  public void setProject(String project) {
    this.project = project;
  }
  public String getExperiment() {
    return experiment;
  }
  public void setExperiment(String experiment) {
    this.experiment = experiment;
  }
  public String getSample() {
    return sample;
  }
  public void setSample(String sample) {
    this.sample = sample;
  }
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public String getFileType() {
    return fileType;
  }
  public void setFileType(String fileType) {
    this.fileType = fileType;
  }
  public String getFileSize() {
    return fileSize;
  }
  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }
  public String getFileDownloadLink() {
    return fileDownloadLink;
  }
  public void setFileDownloadLink(String fileDownloadLink) {
    this.fileDownloadLink = fileDownloadLink;
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
  
}
