package de.uni_tuebingen.qbic.qbicmainportlet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

/*
 * fuer die ID Sachen: precursor tolerance: 15 ppm: fragment tolerance 25 ppm. Mach mal human als
 * Datenbank.
 * 
 * Featurefinding default settings. ID MApper: 60 sec RT tolerance. Sonst im wesentlichen Default
 * settings.
 */
public class QcMlWorkflowView extends Panel implements Observer {
  /*
   * default values for QC workflow
   */
  final String dataType = "MZML";
  final String selectedFileDefault = "No file selected";
  final String precursorTolerancePpmDefault = "15";
  final String fragmentTolerancePpmDefault = "25";
  final String databaseDefault = "human";
  final String retentionTimeToleranceSecDefault = "60";

  final String sender = "DataSetView";

  /*
   * current values
   */
  String selectedFileCurrent = selectedFileDefault;
  String precursorTolerancePpmCurrent = precursorTolerancePpmDefault;
  String fragmentTolerancePpmCurrent = fragmentTolerancePpmDefault;
  String databaseCurrent = databaseDefault;
  String retentionTimeToleranceSecCurrent = retentionTimeToleranceSecDefault;

  String space = "";
  String project = "";
  String sample = "";
  String sampleType = "";
  String filePath = "";

  FormLayout fl = new FormLayout();

  public QcMlWorkflowView() {
    this.setContent(fl);
    submit.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("space", space);
        params.put("project", project);
        params.put("experiment", project + "E");
        params.put("sample", sample);

        params.put("experimentType", "MS_QCML");
        params.put("sampleType", "MS_QCML");
        params.put("userID", LiferayAndVaadinUtils.getUser().getScreenName());

        HashMap<String, Object> experimentProperties = new HashMap<String, Object>();
        HashMap<String, Object> sampleProperties = new HashMap<String, Object>();
        experimentProperties.put("Status", "STARTED");

        String workflowID = startWorkflow();

        experimentProperties.put("WorkflowID", workflowID);

        params.put("experimentProperties", experimentProperties);
        params.put("sampleProperties", sampleProperties);
        DataHandler datahandler =
            (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

        // merge with trunk datahandler has callIngestionService.
        // datahandler.ingest("DSS1", "registerUponWorkflow", params);
        updateDataHandler();
        Notification.show("Workflow started.");
      }

      private void updateDataHandler() {
        DataHandler datahandler =
            (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

      }

      private String startWorkflow() {
        /*
         * String host = "http://localhost:7777/wspgrade/RemoteServlet"; String pwd = "liferaytest";
         * String workflowRoot = "/home/wojnar/QBiC/workflows/guse_workflows/"; String
         * qcMlWorkflowDir = "qcMlWorkflow"; ExecuteQbicTestWorkflow eqtw = new
         * ExecuteQbicTestWorkflow("http://localhost:7777/wspgrade/RemoteServlet", "liferaytest",
         * Paths.get(workflowRoot,qcMlWorkflowDir).toFile()); HashMap<String, Object> parameters =
         * new HashMap<String, Object>(); parameters.put("REF", databaseCurrent);
         * parameters.put("input_files", selectedFileCurrent);
         * parameters.put("precursor_tolerance",precursorTolerancePpmCurrent);
         * parameters.put("fragment_tolerance", fragmentTolerancePpmCurrent );
         * parameters.put("retention_time_tolerance", retentionTimeToleranceSecCurrent);
         * eqtw.setParameters("Workflow", parameters); parameters.clear(); parameters.put("source",
         * filePath); parameters.put("target",
         * "/lustre_qbic/qbic/david/test_workflow_exe_space/qc_1/mzML/");
         * eqtw.setCommandLineArgs("DataStaging",parameters);
         * 
         * return eqtw.execute();
         */
        return null;
      }
    });
    resetParameters.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        String precursorTolerancePpmCurrent = precursorTolerancePpmDefault;
        String fragmentTolerancePpmCurrent = fragmentTolerancePpmDefault;
        String databaseCurrent = databaseDefault;
        String retentionTimeToleranceSecCurrent = retentionTimeToleranceSecDefault;
        rebuildLayout();
      }
    });
  }

  Button submit = new Button("submit");
  Button resetParameters = new Button("reset parameters");

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ArrayList<?>) {
      ArrayList<String> message = (ArrayList<String>) arg;
      // for(String s: message){
      // System.out.println(s);
      // }
      if (message.size() != 8 || !message.get(0).equals(sender) || !message.get(1).equals(dataType)) {
        return;
      }
      project = message.get(2);
      sample = message.get(3);
      sampleType = message.get(4);
      filePath = message.get(5);
      selectedFileCurrent = message.get(6);
      space = message.get(7);
      rebuildLayout();
    }
  }

  private void rebuildLayout() {
    fl.removeAllComponents();
    TextField file = new TextField("Executing QC Workflow on file");

    file.setValue(selectedFileCurrent);
    file.setReadOnly(true);
    fl.addComponent(file);

    TextField tf = new TextField("precursor tolerance (ppm)");
    tf.setConverter(Integer.class);
    tf.setValue(precursorTolerancePpmCurrent);
    fl.addComponent(tf);

    // Mark the first field as required
    // tf.setRequired(true);
    // tf.setRequiredError("The Field may not be empty.");

    TextField tf2 = new TextField("fragment tolerance (ppm)");
    tf2.setConverter(Integer.class);
    tf2.setValue(fragmentTolerancePpmCurrent);

    fl.addComponent(tf2);

    // Set the second field straing to error state with a message.
    // tf2.setComponentError(
    // new UserError("This is the error indicator of a Field."));

    TextField tf3 = new TextField("database");
    tf3.setValue(databaseCurrent);
    fl.addComponent(tf3);

    TextField tf4 = new TextField("retention time tolerance (sec)");
    tf4.setValue(retentionTimeToleranceSecCurrent);
    tf2.setConverter(Integer.class);
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.addComponent(submit);
    buttons.addComponent(resetParameters);
    fl.addComponent(buttons);
    // Make the FormLayout shrink to its contents
    fl.setSizeUndefined();

  }

}
