/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
