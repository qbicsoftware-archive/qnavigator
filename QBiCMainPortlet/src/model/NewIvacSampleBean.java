package model;

import java.io.Serializable;

public class NewIvacSampleBean implements Serializable {

  String type;
  Integer amount;
  String tissue;
  String seqDevice;
  String secondaryName;
  Boolean dnaSeq;
  Boolean rnaSeq;
  Boolean deepSeq;

  public NewIvacSampleBean(String type, Integer amount, String tissue, Boolean dnaSeq,
      Boolean rnaSeq, Boolean deepSeq, String seqDevice, String secondaryName) {
    this.type = type;
    this.amount = amount;
    this.tissue = tissue;
    this.dnaSeq = dnaSeq;
    this.rnaSeq = rnaSeq;
    this.deepSeq = deepSeq;
    this.seqDevice = seqDevice;
    this.secondaryName = secondaryName;
  }

  public NewIvacSampleBean() {

  }
  
  public String getSecondaryName() {
    return secondaryName;
  }

  public void setSecondaryName(String secondaryName) {
    this.secondaryName = secondaryName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public String getTissue() {
    return tissue;
  }

  public void setTissue(String tissue) {
    this.tissue = tissue;
  }

  public Boolean getDnaSeq() {
    return dnaSeq;
  }

  public void setDnaSeq(Boolean dnaSeq) {
    this.dnaSeq = dnaSeq;
  }

  public Boolean getRnaSeq() {
    return rnaSeq;
  }

  public void setRnaSeq(Boolean rnaSeq) {
    this.rnaSeq = rnaSeq;
  }

  public Boolean getDeepSeq() {
    return deepSeq;
  }

  public void setDeepSeq(Boolean deepSeq) {
    this.deepSeq = deepSeq;
  }

  public String getSeqDevice() {
    return seqDevice;
  }

  public void setSeqDevice(String seqDevice) {
    this.seqDevice = seqDevice;
  }

}
