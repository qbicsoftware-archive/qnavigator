package de.uni_tuebingen.qbic.qbicmainportlet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;

import elemental.dom.Node;
/*
 * für die ID Sachen: precursor tolerance: 15 ppm: fragment tolerance 25 
ppm. Mach mal human als Datenbank.

Featurefinding default settings. ID MApper: 60 sec RT tolerance. Sonst 
im wesentlichen Default settings.
 */
public class QcMlWorkflowView extends Panel  implements Observer{
  FormLayout fl = new FormLayout();
  public QcMlWorkflowView(){
    this.setContent(fl);
    submit.addClickListener(new ClickListener() {
      
      @Override
      public void buttonClick(ClickEvent event) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        
        // workflow test instances
        String space = "WORKFLOW_TEST";
        String project = "QWFTE";
        String sample = "QWFTE001HS";
        
        params.put("space", space);
        params.put("project", project);
        params.put("experiment", project + "E");
        params.put("sample", sample);
        
        params.put("experimentType", "MS_QCML");
        params.put("sampleType", "MS_QCML");
        
        params.put("userID", "admin");
        
        HashMap<String, Object> experimentProperties = new HashMap<String, Object>();
        HashMap<String, Object> sampleProperties = new HashMap<String, Object>();
        experimentProperties.put("Status", "STARTED");
        
        params.put("experimentProperties", experimentProperties);
        params.put("sampleProperties", sampleProperties);
        DataHandler datahandler = (DataHandler)UI.getCurrent().getSession().getAttribute("datahandler");
        
       // merge with trunk datahandler has callIngestionService.
       // datahandler.ingest("DSS1", "registerUponWorkflow", params);
      }
    });
  }
  Button submit = new Button("submit");
  @Override
  public void update(Observable o, Object arg) {
    fl.removeAllComponents();
    if(arg instanceof ArrayList<?>){
      ArrayList<String> message = (ArrayList<String>) arg;
      for(String s: message){
        System.out.println(s);
      }
      
      
      if(!message.get(0).equals("DataSetView")){
        return;
      }
      TextField file = new TextField("Executing QC Workflow on file");
      String selectedFile = "No file selected";
        if(message.get(1).equals("MZML")){
          selectedFile = message.get(2);
      }
      
      file.setValue(selectedFile);
      file.setEnabled(false);
      fl.addComponent(file);
      
      TextField tf = new TextField("precursor tolerance (ppm)");
      tf.setConverter(Integer.class);
      tf.setValue("15");
      fl.addComponent(tf);
      
      // Mark the first field as required
      //tf.setRequired(true);
      //tf.setRequiredError("The Field may not be empty.");

      TextField tf2 = new TextField("fragment tolerance (ppm)");
      tf2.setConverter(Integer.class);
      tf2.setValue("25");
      
      fl.addComponent(tf2);

      // Set the second field straing to error state with a message.
      tf2.setComponentError(
          new UserError("This is the error indicator of a Field."));
      
      TextField tf3 = new TextField("database");
      tf3.setValue("human");
      fl.addComponent(tf3);
      
      TextField tf4 = new TextField("retention time tolerance (sec)");
      tf4.setValue("60");
      tf2.setConverter(Integer.class);
      
      fl.addComponent(submit);
      // Make the FormLayout shrink to its contents 
      fl.setSizeUndefined();
    }

    
    
  }
  
}
