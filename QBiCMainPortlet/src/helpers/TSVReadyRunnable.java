package helpers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import parser.XMLParser;
import properties.Factor;
import qbic.vaadincomponents.TSVDownloadComponent;

import com.vaadin.server.StreamResource;

public class TSVReadyRunnable implements Runnable {

  private TSVDownloadComponent layout;
  Map<String, List<String>> tables;
  private String project;
  private logging.Logger logger = new Log4j2Logger(TSVReadyRunnable.class);

  public TSVReadyRunnable(TSVDownloadComponent layout, Map<String, List<String>> tables, String project) {
    this.layout = layout;
    this.tables = tables;
    this.project = project;
  }

    @Override
    public void run() {
      List<StreamResource> streams = new ArrayList<StreamResource>();
      streams.add(
          getTSVStream(getTSVString(tables.get("Q_BIOLOGICAL_ENTITY")), project + "_sample_sources"));
      streams.add(getTSVStream(getTSVString(tables.get("Q_BIOLOGICAL_SAMPLE")),
          project + "_sample_extracts"));
      if (tables.containsKey("Q_TEST_SAMPLE"))
        streams.add(getTSVStream(getTSVString(tables.get("Q_TEST_SAMPLE")),
            project + "_sample_preparations"));
      logger.debug("done, arming buttons");
      layout.armButtons(streams);
    }

    public StreamResource getTSVStream(final String content, String name) {
      StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
        @Override
        public InputStream getStream() {
          try {
            InputStream is = new ByteArrayInputStream(content.getBytes());
            return is;
          } catch (Exception e) {
            e.printStackTrace();
            return null;
          }
        }
      }, String.format("%s.tsv", name));
      return resource;
    }

    private static String getTSVString(List<String> table) {
      XMLParser p = new XMLParser();

      StringBuilder header = new StringBuilder(table.get(0).replace("\tAttributes", ""));
      StringBuilder tsv = new StringBuilder();
      table.remove(0);

      List<String> factorLabels = new ArrayList<String>();
      for (String row : table) {
        String[] lineSplit = row.split("\t", -1);// doesn't remove trailing whitespaces
        String xml = lineSplit[lineSplit.length - 1];
        List<Factor> factors = new ArrayList<Factor>();
        if (!xml.isEmpty()) {
          try {
            factors = p.getFactorsFromXML(xml);
          } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          for (Factor f : factors) {
            String label = f.getLabel();
            if (!factorLabels.contains(label)) {
              factorLabels.add(label);
              header.append("\tCondition: " + label);
            }
          }
        }
      }

      for (String row : table) {
        String[] lineSplit = row.split("\t", -1);// doesn't remove trailing whitespaces
        String xml = lineSplit[lineSplit.length - 1];
        if (!xml.isEmpty())
          row = row.replace("\t" + xml, "");
        StringBuilder line = new StringBuilder("\n" + row);
        List<Factor> factors = new ArrayList<Factor>();
        if (!xml.isEmpty()) {
          try {
            factors = p.getFactorsFromXML(xml);
          } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          Map<Integer, Factor> order = new HashMap<Integer, Factor>();
          for (Factor f : factors) {
            String label = f.getLabel();
            order.put(factorLabels.indexOf(label), f);
          }
          for (int i = 0; i < factorLabels.size(); i++) {
            if (order.containsKey(i)) {
              Factor f = order.get(i);
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
}
