package model;

import java.io.Serializable;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

public class NewIvacSampleBean implements Serializable{
  
  String type;
  Integer amount;
  String tissue;
  String seqDevice;
  Boolean dnaSeq;
  Boolean rnaSeq;
  Boolean deepSeq;
  
  public NewIvacSampleBean(String type, Integer amount, String tissue, Boolean dnaSeq, Boolean rnaSeq, Boolean deepSeq, String seqDevice) {
    this.type = type;
    this.amount = amount;
    this.tissue = tissue;
    this.dnaSeq = dnaSeq;
    this.rnaSeq = rnaSeq;
    this.deepSeq = deepSeq;
    this.seqDevice = seqDevice;
  }
  
  public NewIvacSampleBean() {
    
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
  
