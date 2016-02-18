package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.ui.Component;

import main.OpenBisClient;
import controllers.BarcodeController;

public class StandaloneBarcodeUI {

  WizardBarcodeView bw;
  BarcodeController bc;

  public StandaloneBarcodeUI(OpenBisClient openbis, String scripts, String path) {
//    old
//    this.bw = new WizardBarcodeViewOld();
    bc = new BarcodeController(openbis, scripts, path);
//    bc.init();
    
    //new
    this.bw = new WizardBarcodeView();
    bw.initControl(bc);
  }

  public void setExperiments() {}

  public Component getView() {
    return bw;
  }

  public void setSummaryBeans(String project) {
    bc.reactToProjectSelection(project);
  }

}
