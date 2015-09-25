package qbic.vaadincomponents;

import guse.impl.GuseWorkflowFileSystem;

import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import logging.Logger;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import controllers.WorkflowViewController;

import submitter.Workflow;
import submitter.parameters.Parameter;
import submitter.parameters.StringParameter;

import de.uni_tuebingen.qbic.beans.DatasetBean;

public class MicroarrayQCComponent extends CustomComponent {

  private static Logger LOGGER = new Log4j2Logger(MicroarrayQCComponent.class);

  private Button submit = new Button("Submit");
  private Button reset = new Button("Reset");

  private ParameterComponent parameterComponent = new ParameterComponent();
  private InputFilesComponent inputFileComponent = new InputFilesComponent();

  private WorkflowViewController workflowViewController;

  public MicroarrayQCComponent(WorkflowViewController controller) {
    workflowViewController = controller;
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
    Map<String, Parameter> params = tmp.getParameters().getParams();
    StringParameter pheno =
        new StringParameter("pheno", "placeholder for pheno file created from openbis", true, true,
            null);
    pheno.setValue(workflowViewController.getExperimentalFactorsTSV());
    params.put("pheno", pheno);
    boolean success = inputFileComponent.updateWorkflow(tmp, workflowViewController);
    return success ? tmp : null;
  }

  public void update(Workflow workflow, BeanItemContainer<DatasetBean> input) {
    this.inputFileComponent.buildLayout(workflow.getData().getData(), input);
    this.parameterComponent.buildLayout(workflow);
    this.parameterComponent.setComboboxOptions("Microarray QC.1.f",
        workflowViewController.getExperimentalFactors());
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


}
