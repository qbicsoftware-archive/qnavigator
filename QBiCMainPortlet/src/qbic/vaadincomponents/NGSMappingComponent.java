package qbic.vaadincomponents;

import guse.workflowrepresentation.GuseNode;
import guse.workflowrepresentation.GuseWorkflowRepresentation;
import guse.workflowrepresentation.InputPort;

import java.util.List;

import logging.Log4j2Logger;
import logging.Logger;

import org.json.JSONException;

import submitter.Workflow;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.beans.DatasetBean;

public class NGSMappingComponent extends CustomComponent {

  private static Logger LOGGER = new Log4j2Logger(NGSMappingComponent.class);

  private Button submit = new Button("Submit");
  private Button reset = new Button("Reset");

  private ParameterComponent parameterComponent = new ParameterComponent();
  private InputFilesComponent inputFileComponent = new InputFilesComponent();

  private WorkflowViewController controller;

  public NGSMappingComponent(WorkflowViewController controller) {
    this.controller = controller;
    reset.setDescription("Reset Parameters to default values.");
    submit
        .setDescription("Execute Workflow. With given input files, database/reference files and parameters.");

    HorizontalLayout buttonContent = new HorizontalLayout();
    buttonContent.addComponent(reset);
    buttonContent.addComponent(submit);

    VerticalLayout submissionContent = new VerticalLayout();
    submissionContent.setSpacing(true);
    submissionContent.addComponent(inputFileComponent);
    submissionContent.addComponent(parameterComponent);
    submissionContent.addComponent(buttonContent);
    setCompositionRoot(submissionContent);
  }

  public List<DatasetBean> getSelectedDatasets() {
    return inputFileComponent.getSelectedDatasets();
  }

  public Workflow getWorkflow() {
    Workflow tmp = parameterComponent.getWorkflow();
    boolean success = inputFileComponent.updateWorkflow(tmp, controller);
    return success ? tmp : null;
  }

  public void update(Workflow workflow, BeanItemContainer<DatasetBean> input) {
    this.inputFileComponent.buildLayout(workflow.getData().getData(), input);
    this.parameterComponent.buildLayout(workflow);
  }

  public void resetParameters() {
    parameterComponent.resetParameters();
  }

  public void addResetListener(ClickListener listener) {
    reset.addClickListener(listener);
  }

  public void addSubmissionListener(ClickListener listener) {
    submit.addClickListener(listener);
  }

  public void writeParametersToWorkflow() throws IllegalArgumentException, JSONException {
    if (parameterComponent.getWorkflow() instanceof GuseWorkflowRepresentation) {
      GuseWorkflowRepresentation w = (GuseWorkflowRepresentation) parameterComponent.getWorkflow();

      String header = "Group\tfile";

      StringBuilder tsv = new StringBuilder(header);

      for (DatasetBean b : inputFileComponent.getSelectedDatasets()) {
        String file = b.getFileName();
        String[] splittedID = b.getSampleIdentifier().split("/");
        String group = splittedID[splittedID.length - 1];
        tsv.append("\n" + group + "\t" + file);
      }
      tsv.append("\n");

      GuseNode node = w.getNode("Workflow");
      InputPort port = node.getPort("GROUPS");
      port.getParams().get("groups").setValue(tsv.toString());
    }

  }


}
