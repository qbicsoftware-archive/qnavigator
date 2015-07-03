package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.List;

import com.vaadin.ui.Component;

import main.OpenBisClient;
import model.ExperimentBarcodeSummaryBean;
import controllers.BarcodeController;

public class StandaloneBarcodeUI {

  WizardBarcodeView bw;
  BarcodeController bc;

  public StandaloneBarcodeUI(OpenBisClient openbis, String scripts, String path) {
    this.bw = new WizardBarcodeView();
    bc = new BarcodeController(bw, openbis, scripts, path);
    bc.init();
  }

  public void setExperiments() {}

  public Component getView() {
    return bw;
  }

  public void setSummaryBeans(String project) {
    bc.reactToProjectSelection(project);
  }

}
