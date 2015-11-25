package qbic.vaadincomponents;

import java.util.List;

import submitter.Workflow;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.beans.DatasetBean;

public class StandardWorkflowComponent extends CustomComponent{
  private static final long serialVersionUID = 3857796285188122864L;
  
  private Button submit = new Button("Submit");
  private Button reset = new Button("Reset");

  private ParameterComponent parameterComponent = new ParameterComponent();
  private InputFilesComponent inputFileComponent = new InputFilesComponent();

  private WorkflowViewController workflowViewController;
  
  public StandardWorkflowComponent(WorkflowViewController c){
    workflowViewController = c;
    reset.setDescription("Reset Parameters to default values.");
    submit.setDescription("Execute Workflow. With given input files, database/reference files and parameters.");
    
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
  
  /**
   * add a listener that decides what to do if the reset button was clicked
   * @param listener
   */
  public void addResetListener(ClickListener listener){
    reset.addClickListener(listener);
  }
  
  /**
   * add a listener that decides what to do if the submit button was clicked
   * @param listener
   */
  public void addSubmissionListener(ClickListener listener){
    submit.addClickListener(listener);
  }
  
  /**
   * update the Component to show the input files and the parameters for the given workflow
   * @param workflow
   * @param input
   */
  public void update(Workflow workflow, BeanItemContainer<DatasetBean> input){
    this.inputFileComponent.buildLayout(workflow.getData().getData(), input);
    this.parameterComponent.buildLayout(workflow);
  }
  
  /**
   * resets parameters to default values. Keep in mind that inputs, database, fasta will not be reset.
   */
  public void resetParameters(){
    parameterComponent.resetParameters();
  }
  
  /**
   * return only the datasets that where selected.
   * @return
   */
  public List<DatasetBean> getSelectedDatasets(){
    return inputFileComponent.getSelectedDatasets();
  }

  /**
   * if the workflow can not be updated for one or another reason, null is returned.
   * e.g. not all mandatory parameters are set
   * @return
   */
  public Workflow getWorkflow() {
    Workflow tmp =  parameterComponent.getWorkflow();
    boolean success = inputFileComponent.updateWorkflow(tmp, workflowViewController);
    return success?tmp:null;
  }
  
}



