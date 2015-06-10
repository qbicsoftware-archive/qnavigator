package model;

import java.io.Serializable;

public class ExperimentStatusBean implements Serializable {
 
  String identifier;
  String code; 
  String description;
  String download;
  String runWorkflow;
  Double status;
  
  public ExperimentStatusBean(String identifier, String code, String description, String download, String runWorkflow, Double status) {
    this.identifier = identifier;
    this.code = code;
    this.description = description;
    this.download = download;
    this.runWorkflow = runWorkflow;
    this.status = status;    
  }
  
  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
  
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
  
  public ExperimentStatusBean() {
  }
  
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDownload() {
    return download;
  }

  public void setDownload(String download) {
    this.download = download;
  }

  public String getRunWorkflow() {
    return runWorkflow;
  }

  public void setRunWorkflow(String runWorkflow) {
    this.runWorkflow = runWorkflow;
  }

  public Double getStatus() {
    return status;
  }

  public void setStatus(Double status) {
    this.status = status;
  } 
  
}
