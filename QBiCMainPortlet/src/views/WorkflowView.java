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

import qbic.vaadincomponents.InputFilesComponent;
import qbic.vaadincomponents.ParameterComponent;
import submitter.SubmitFailedException;
import submitter.Workflow;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
  private static final String WORKFKLOW_GRID_DESCRIPTION =
      "If you want to execute a workflow, click on one of the rows in the table. Then select the parameters, input files database/reference files and click on submit.";
  private static final String SUBMISSION_CAPTION = "Submission";

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
    availableWorkflows.setDescription(WORKFKLOW_GRID_DESCRIPTION);
    submissionContent.setSpacing(true);
    submissionContent.addComponent(inputFileComponent);
    submissionContent.addComponent(parameterComponent);
    submissionContent.addComponent(buttonContent);

    buttonContent.addComponent(resetParameters);
    buttonContent.addComponent(submitWorkflow);

    submission.setCaption(SUBMISSION_CAPTION);
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

    switch (type) {
      case "project":
        datasetBeans = controller.getcontainer(type, id);
        List<String> datasetTypesInProject = new ArrayList<String>();

        for (Iterator<DatasetBean> i = datasetBeans.getItemIds().iterator(); i.hasNext();) {
          DatasetBean dsBean = (DatasetBean) i.next();
          datasetTypesInProject.add(dsBean.getFileType());
        }
        updateWorkflowSelection(datasetTypesInProject);
        break;

      case "experiment":
        break;

      case "sample":
        break;

      case "workflowExperimentType":
        String projectID = map.get("project");

        BeanItemContainer<Workflow> suitableWorkflows =
            controller.suitableWorkflowsByExperimentType(id);
        BeanItemContainer<DatasetBean> suitableDatasets =
            new BeanItemContainer<DatasetBean>(DatasetBean.class);

        List<String> workflowDatasetTypes = new ArrayList<String>();
        for (Iterator i = suitableWorkflows.getItemIds().iterator(); i.hasNext();) {
          Workflow workflowBean = (Workflow) i.next();

          workflowDatasetTypes.addAll(workflowBean.getFileTypes());
        }

        for (Iterator i = controller.getcontainer("project", id).getItemIds().iterator(); i
            .hasNext();) {
          DatasetBean datasetBean = (DatasetBean) i.next();

          if (workflowDatasetTypes.contains(datasetBean.getFileType())) {
            suitableDatasets.addBean(datasetBean);
          }
        }

        datasetBeans = suitableDatasets;
        updateSelection(suitableWorkflows);
        break;

      default:
        updateSelection(new BeanItemContainer<Workflow>(Workflow.class));
        break;
    }
  }


  protected void updateWorkflowSelection(DatasetBean dataset) {
    updateSelection(controller.suitableWorkflows(dataset.getFileType()));
  }

  protected void updateWorkflowSelection(List<String> datasetTypes) {
    updateSelection(controller.suitableWorkflows(datasetTypes));
  }

  protected void updateWorkflowSelection(String experimentType) {
    updateSelection(controller.suitableWorkflowsByExperimentType(experimentType));
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
    availableWorkflows.setColumnOrder("name", "version", "fileTypes");
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
    gpcontainer.removeContainerProperty("description");
    return gpcontainer;
  }

  private void updateParameterView(Workflow workFlow, BeanItemContainer<DatasetBean> projectDatasets) {
    this.submission.setCaption(SUBMISSION_CAPTION + ": " + workFlow.getName());
    this.inputFileComponent.buildLayout(workFlow.getData().getData().entrySet(), projectDatasets);
    this.parameterComponent.buildLayout(workFlow);
  }



  private void addComponentListeners() {

    availableWorkflows.setDetailsGenerator(new DetailsGenerator() {
      private static final long serialVersionUID = 6123522348935657638L;

      @Override
      public Component getDetails(RowReference rowReference) {
        FormLayout main = new FormLayout();
        Workflow w = (Workflow) rowReference.getItemId();
        Label description = new Label(w.getDescription(), ContentMode.HTML);
        description.setCaption("Description");
        main.addComponent(description);
        return main;
      }
    });

    availableWorkflows.addItemClickListener(new ItemClickListener() {
      private static final long serialVersionUID = 3786125825391677177L;

      @Override
      public void itemClick(ItemClickEvent event) {
        // TODO get path of datasetBean and set it as input ?!
        Workflow selectedWorkflow = (Workflow) event.getItemId();
        if (selectedWorkflow != null) {
          updateParameterView(selectedWorkflow, datasetBeans);
          resetParameters.setVisible(true);
          submission.setVisible(true);
          availableWorkflows.setDetailsVisible(selectedWorkflow,
              !availableWorkflows.isDetailsVisible(selectedWorkflow));
        } else {
          LOGGER.warn("selected Workflow is null?");
        }

      }
    });
    availableWorkflows.setEditorEnabled(false);



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
        if (submittedWf == null || selectedDatasets.isEmpty()
            || !inputFileComponent.updateWorkflow(submittedWf, controller)) {
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
          LOGGER.error("Submission failed, probably openbis. error message: " + e.getMessage(),
              e.getStackTrace());
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
