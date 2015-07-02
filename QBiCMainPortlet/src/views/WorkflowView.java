package views;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import logging.Log4j2Logger;
import logging.Logger;

import org.springframework.remoting.RemoteAccessException;

import submitter.SubmitFailedException;
import submitter.Workflow;
import qbic.vaadincomponents.InputFilesComponent;
import qbic.vaadincomponents.ParameterComponent;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.beans.DatasetBean;
import de.uni_tuebingen.qbic.qbicmainportlet.DatasetView;

public class WorkflowView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = -1461508641666415578L;
  private static Logger LOGGER = new Log4j2Logger(WorkflowView.class);
  public final static String navigateToLabel = "workflow";

  // Controller
  private WorkflowViewController controller;

  // View
  private VerticalLayout viewContent = new VerticalLayout();
  private Grid availableWorkflows = new Grid();

  private Button submitWorkflow = new Button("Submit");
  private Button resetParameters = new Button("Reset Parameters");

  private ParameterComponent parameterComponent = new ParameterComponent();
  private InputFilesComponent inputFileComponent = new InputFilesComponent();

  private VerticalLayout submission;

  private VerticalLayout workflows;

  // data
  BeanItemContainer<DatasetBean> datasetBeans;
  private String type;
  private String id;



  public WorkflowView(WorkflowViewController controller) {
    this.controller = controller;
    init();
  }

  private void init() {

    viewContent.setWidth("100%");
    viewContent.setMargin(true);

    // select available workflows
    workflows = new VerticalLayout();
    VerticalLayout workflowsContent = new VerticalLayout();
    workflows.setMargin(new MarginInfo(false, true, true, true));

    workflowsContent.addComponent(availableWorkflows);
    // availableWorkflows.setWidth("100%");
    workflows.setVisible(false);

    workflows.setCaption("Available Workflows");
    workflows.setIcon(FontAwesome.EXCHANGE);
    workflows.addComponent(workflowsContent);
    workflows.setWidth(100.0f, Unit.PERCENTAGE);

    // submission
    submission = new VerticalLayout();
    VerticalLayout submissionContent = new VerticalLayout();
    HorizontalLayout buttonContent = new HorizontalLayout();
    submission.setMargin(new MarginInfo(false, true, true, true));

    availableWorkflows.setSizeFull();
    submissionContent.setSpacing(true);
    submissionContent.addComponent(inputFileComponent);
    submissionContent.addComponent(parameterComponent);
    submissionContent.addComponent(buttonContent);

    buttonContent.addComponent(resetParameters);
    buttonContent.addComponent(submitWorkflow);

    submission.setCaption("Submission");
    submission.setIcon(FontAwesome.PLAY);
    submission.addComponent(submissionContent);
    submission.setWidth(100.0f, Unit.PERCENTAGE);
    submission.setVisible(false);

    // add sections to layout
    viewContent.addComponent(workflows);
    viewContent.addComponent(submission);

    this.addComponent(viewContent);
    addComponentListeners();
  }

  /**
   * updates view, if height, width or the browser changes.
   * 
   * @param browserHeight
   * @param browserWidth
   * @param browser
   */
  public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
    setWidth((browserWidth * 0.6f), Unit.PIXELS);
  }



  @Override
  public void enter(ViewChangeEvent event) {
    Map<String, String> map = DatasetView.getMap(event.getParameters());
    if (map == null)
      return;
    // TODO In background thread?
    type = map.get("type");
    id = map.get("id");
    datasetBeans = controller.getcontainer(map.get("id"));
    List<String> datasetTypesInProject = new ArrayList<String>();

    for (Iterator<DatasetBean> i = datasetBeans.getItemIds().iterator(); i.hasNext();) {
      DatasetBean dsBean = (DatasetBean) i.next();
      datasetTypesInProject.add(dsBean.getFileType());
    }
    updateWorkflowSelection(datasetTypesInProject);
  }


  protected void updateWorkflowSelection(DatasetBean dataset) {
    updateSelection(controller.suitableWorkflows(dataset.getFileType()));
  }

  protected void updateWorkflowSelection(List<String> datasetTypes) {
    updateSelection(controller.suitableWorkflows(datasetTypes));
  }

  /**
   * updates availableWorkflows to contain only workflows according to dataset selection.
   * 
   * @param suitableWorkflows
   */
  void updateSelection(BeanItemContainer<Workflow> suitableWorkflows) {
    if (!(suitableWorkflows.size() > 0)) {
      Notification notif =
          new Notification("No suitable workflows found. Pleace contact your project manager.",
              Type.TRAY_NOTIFICATION);

      // Customize it
      notif.setDelayMsec(60000);
      notif.setPosition(Position.MIDDLE_CENTER);

      // Show it in the page
      notif.show(Page.getCurrent());
    }

    availableWorkflows.setContainerDataSource(filtergpcontainer(suitableWorkflows));
    availableWorkflows.setColumnOrder("name", "description", "version", "fileTypes");
    workflows.setVisible(true);
  }

  /**
   * filter grid columns
   * 
   * @param suitableWorkflows
   * @return
   */
  GeneratedPropertyContainer filtergpcontainer(BeanItemContainer<Workflow> suitableWorkflows) {
    // ONLY SHOW SPECIFIC COLUMNS IN GRID
    GeneratedPropertyContainer gpcontainer = new GeneratedPropertyContainer(suitableWorkflows);

    gpcontainer.removeContainerProperty("ID");
    gpcontainer.removeContainerProperty("data");
    gpcontainer.removeContainerProperty("datasetType");
    gpcontainer.removeContainerProperty("nodes");
    gpcontainer.removeContainerProperty("experimentType");
    gpcontainer.removeContainerProperty("parameterToNodesMapping");
    gpcontainer.removeContainerProperty("parameters");
    gpcontainer.removeContainerProperty("sampleType");
    return gpcontainer;
  }

  private void updateParameterView(Workflow workFlow, BeanItemContainer<DatasetBean> projectDatasets) {
    this.inputFileComponent.buildLayout(workFlow.getData().getData().entrySet(), projectDatasets);
    this.parameterComponent.buildLayout(workFlow);
  }



  private void addComponentListeners() {

    /*
     * this.availableDatasets.addSelectionListener(new SelectionListener() { private static final
     * long serialVersionUID = 4033655694590766143L;
     * 
     * @Override public void select(SelectionEvent event) { Object selectedDataset =
     * availableDatasets.getSelectedRow(); submission.setVisible(false);
     * workflows.setVisible(false); //Object selectedDataset =
     * datasetContainer.getItem(availableDatasets.getSelectedRow()); if (selectedDataset == null)
     * return; if (selectedDataset instanceof DatasetBean) { DatasetBean dataset = (DatasetBean)
     * selectedDataset; updateWorkflowSelection(dataset); } else {
     * Notification.show("Selected item is not a valid dataset. Please contact your project manager."
     * , Type.ERROR_MESSAGE); } } } );
     */
    this.availableWorkflows.addSelectionListener(new SelectionListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2628561841420694483L;

      @Override
      public void select(SelectionEvent event) {

        // TODO get path of datasetBean and set it as input ?!
        Workflow selectedWorkflow = (Workflow) availableWorkflows.getSelectedRow();

        if (selectedWorkflow != null) {
          updateParameterView(selectedWorkflow, datasetBeans);

          resetParameters.setVisible(true);
          submission.setVisible(true);
        } else {
          LOGGER.debug("selected Workflow is null?");
        }
      }
    });

    this.resetParameters.addClickListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        // TODO reset InputList
        parameterComponent.resetParameters();
      }
    });

    this.submitWorkflow.addClickListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        List<DatasetBean> selectedDatasets = inputFileComponent.getSelectedDatasets();
        Workflow submittedWf = parameterComponent.getWorkflow();
        if (submittedWf == null || selectedDatasets.isEmpty() || !inputFileComponent.updateWorkflow(submittedWf,controller)) {
          return;
        }
        try {
          // THIS IS THE IMPORTANT LINE IN THAT MESS
          String openbisId =
              controller.submitAndRegisterWf(type, id, submittedWf, selectedDatasets);

          Notification.show("Workflow submitted and saved under" + openbisId, Type.WARNING_MESSAGE);
        } catch (ConnectException | IllegalArgumentException | SubmitFailedException e) {
          LOGGER.error("Submission failed, probably gUSE. " + e.getMessage(), e.getStackTrace());
          Notification
              .show(
                  "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
                  Type.WARNING_MESSAGE);
          try {
            VaadinService
                .getCurrentResponse()
                .sendError(
                    HttpServletResponse.SC_GATEWAY_TIMEOUT,
                    "An error occured, while trying to connect to the database. Please try again later, or contact your project manager.");
          } catch (IOException | IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
          }
        } catch (RemoteAccessException e) {
          LOGGER.error("Submission failed, probably openbis.", e.getStackTrace());
          Notification
              .show(
                  "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
                  Type.TRAY_NOTIFICATION);
        } catch (Exception e) {
          LOGGER.error("Internal error: " + e.getMessage(), e.getStackTrace());
          Notification
              .show(
                  "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
                  Type.TRAY_NOTIFICATION);
        }
      }
    });

  }
}
