/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects. Copyright (C) "2016” Christopher
 * Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package qbic.vaadincomponents;

import helpers.TSVReadyRunnable;
import life.qbic.openbis.openbisclient.OpenBisClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import parser.XMLParser;
import properties.Property;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TSVDownloadComponent extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = 6140141377721355605L;

  private HorizontalLayout downloads;
  // private ProgressBar bar;

  private Button dlEntities;
  private Button dlExtracts;
  private Button dlPreps;

  private logging.Logger logger = new Log4j2Logger(TSVDownloadComponent.class);
  private List<FileDownloader> downloaders = new ArrayList<FileDownloader>();

  public TSVDownloadComponent() {
    // TODO progress bar not used atm because the labels in available UpdateProgressBar classes are
    // too specific
    // probably not needed anyway since ProjInformationComponent seems to show when everything is
    // complete
    // this.bar = new ProgressBar();
    // this.info = new Label();
    // info.setCaption("Preparing Spreadsheets");
    // addComponent(info);
    // addComponent(bar);

    downloads = new HorizontalLayout();
    downloads.setCaption("Spreadsheets");
    downloads.setSpacing(true);
    dlEntities = new Button("Sample Sources");
    dlExtracts = new Button("Sample Extracts");
    dlPreps = new Button("Sample Preparations");

    dlEntities.setStyleName(ValoTheme.BUTTON_LINK);
    dlEntities.setIcon(FontAwesome.DOWNLOAD);

    dlExtracts.setStyleName(ValoTheme.BUTTON_LINK);
    dlExtracts.setIcon(FontAwesome.DOWNLOAD);

    dlPreps.setStyleName(ValoTheme.BUTTON_LINK);
    dlPreps.setIcon(FontAwesome.DOWNLOAD);

    dlEntities.setEnabled(false);
    dlExtracts.setEnabled(false);
    dlPreps.setEnabled(false);
    downloads.addComponent(dlEntities);
    downloads.addComponent(dlExtracts);
    downloads.addComponent(dlPreps);
    addComponent(downloads);
  }

  public void disableSpreadSheets() {
    dlEntities.setEnabled(false);
    dlExtracts.setEnabled(false);
    dlPreps.setEnabled(false);
  }

  public void prepareSpreadsheets(final List<String> sampleTypes, String space,
      final String project, final OpenBisClient openbis) {
    final TSVDownloadComponent layout = this;

    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {

        Map<String, String> tables = new HashMap<String, String>();
        for (String type : sampleTypes) {
          tables.put(type, getTSVString(openbis.getProjectTSV(project, type)));
        }
        UI.getCurrent().access(new TSVReadyRunnable(layout, tables, project));
      }
    });
    t.start();
  }
  
  private static String getTSVString(List<String> table) {
    XMLParser p = new XMLParser();

    StringBuilder header = new StringBuilder(table.get(0).replace("\tAttributes", ""));
    StringBuilder tsv = new StringBuilder();
    table.remove(0);

    String xmlStart = "<?xml";
    // header
    List<String> factorLabels = new ArrayList<String>();
    for (String row : table) {
      String[] lineSplit = row.split("\t", -1);// doesn't remove trailing whitespaces
      String xml = "";
      for (String cell : lineSplit) {
        if (cell.startsWith(xmlStart))
          xml = cell;
      }
      List<Property> factors = new ArrayList<Property>();
      if (!xml.equals(xmlStart)) {
        try {
          factors = p.getAllPropertiesFromXML(xml);
        } catch (JAXBException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        for (Property f : factors) {
          String label = f.getLabel();
          if (!factorLabels.contains(label)) {
            factorLabels.add(label);
            header.append("\tCondition: " + label);
          }
        }
      }
    }

    // data
    for (String row : table) {
      String[] lineSplit = row.split("\t", -1);// doesn't remove trailing whitespaces
      String xml = "";
      for (String cell : lineSplit) {
        if (cell.startsWith(xmlStart))
          xml = cell;
      }
      row = row.replace("\t" + xml, "");
      StringBuilder line = new StringBuilder("\n" + row);
      List<Property> factors = new ArrayList<Property>();
      if (!xml.equals(xmlStart)) {
        try {
          factors = p.getAllPropertiesFromXML(xml);
        } catch (JAXBException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        Map<Integer, Property> order = new HashMap<Integer, Property>();
        for (Property f : factors) {
          String label = f.getLabel();
          order.put(factorLabels.indexOf(label), f);
        }
        for (int i = 0; i < factorLabels.size(); i++) {
          if (order.containsKey(i)) {
            Property f = order.get(i);
            line.append("\t" + f.getValue());
            if (f.hasUnit())
              line.append(f.getUnit());
          } else {
            line.append("\t");
          }
        }
      } else {
        for (int i = 0; i < factorLabels.size() - 1; i++) {
          line.append("\t");
        }
      }
      tsv.append(line);
    }
    return header.append(tsv).toString();
  }

  public void armButtons(List<StreamResource> streams) {
    armDownloadButton(dlEntities, streams.get(0), 1);
    armDownloadButton(dlExtracts, streams.get(1), 2);
    if (streams.size() > 2)
      armDownloadButton(dlPreps, streams.get(2), 3);
  }

  protected void armDownloadButton(Button b, StreamResource stream, int dlnum) {
    if (downloaders.size() < dlnum) {
      FileDownloader dl = new FileDownloader(stream);
      dl.extend(b);
      downloaders.add(dl);
    } else
      downloaders.get(dlnum - 1).setFileDownloadResource(stream);
    b.setEnabled(true);
  }

  public void enableDownloads(boolean b) {
    downloads.setEnabled(b);
  }

}
