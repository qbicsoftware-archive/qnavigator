package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.remoting.RemoteAccessException;

import qbic.model.maxquant.MaxQuantModel;
import qbic.model.maxquant.MaxquantConverterFactory;
import qbic.model.maxquant.RawFilesBean;
import qbic.vaadincomponents.MaxQuantComponent;
import qbic.vaadincomponents.MicroarrayQCComponent;
import qbic.vaadincomponents.StandardWorkflowComponent;
import logging.Log4j2Logger;
import logging.Logger;
import submitter.SubmitFailedException;
import submitter.Workflow;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.Notification.Type;

import controllers.WorkflowViewController;
import de.uni_tuebingen.qbic.beans.DatasetBean;
import fasta.FastaBean;
import fasta.FastaDB;

public class WorkflowComponent extends CustomComponent {

  private static Logger LOGGER = new Log4j2Logger(WorkflowComponent.class);
  private static final String WORKFKLOW_GRID_DESCRIPTION =
      "If you want to execute a workflow, click on one of the rows in the table. Then select the parameters, input files database/reference files and click on submit.";
  private static final String SUBMISSION_CAPTION = "Submission";
  protected static final String SUBMISSION_FAILED_MESSAGE =
      "Workflow submission failed due to internal errors! Please try again later or contact your project manager.";

  private WorkflowViewController controller;
  private VerticalLayout viewContent = new VerticalLayout();
  private VerticalLayout workflows;
  private Grid availableWorkflows = new Grid();
  private VerticalLayout submission;

  // data
  BeanItemContainer<DatasetBean> datasetBeans;
  private String type;
  private String id;

  public WorkflowComponent(WorkflowViewController controller) {
    this.controller = controller;
    this.setCaption("Workflows");
    LOGGER.debug("new wf component");
    init();
  }

  private void init() {
    VerticalLayout workflowLayout = new VerticalLayout();

    viewContent.setWidth("100%");
    viewContent.setMargin(true);

    // select available workflows
    workflows = new VerticalLayout();
    VerticalLayout workflowsContent = new VerticalLayout();
    workflows.setMargin(new MarginInfo(false, true, true, false));

    workflowsContent.addComponent(availableWorkflows);
    availableWorkflows.setSizeFull();
    availableWorkflows.setDescription(WORKFKLOW_GRID_DESCRIPTION);
    // availableWorkflows.setWidth("100%");
    workflows.setVisible(false);

    workflows.setCaption("Available Workflows");
    workflows.setIcon(FontAwesome.EXCHANGE);
    workflows.addComponent(workflowsContent);
    workflows.setWidth(100.0f, Unit.PERCENTAGE);

    // submission
    submission = new VerticalLayout();
    submission.setMargin(new MarginInfo(false, true, true, false));


    submission.setCaption(SUBMISSION_CAPTION);
    submission.setIcon(FontAwesome.PLAY);
    submission.setWidth(100.0f, Unit.PERCENTAGE);
    submission.setVisible(false);

    // add sections to layout
    viewContent.addComponent(workflows);
    viewContent.addComponent(submission);

    workflowLayout.addComponent(viewContent);
    setCompositionRoot(workflowLayout);
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

  public void update(Map<String, String> map) {

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
        String experiment = map.get("experiment");

        BeanItemContainer<Workflow> suitableWorkflows =
            controller.suitableWorkflowsByExperimentType(experiment);
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
    this.submission.setCaption("");
    this.submission.removeAllComponents();
    if (!(suitableWorkflows.size() > 0)) {
      showNotification("No suitable workflows found. Pleace contact your project manager.");
    }

    availableWorkflows.setContainerDataSource(filtergpcontainer(suitableWorkflows));
    //availableWorkflows.setColumnOrder("name", "version", "fileTypes");
    availableWorkflows.setColumnOrder("name", "version");
    
    workflows.setVisible(true);
  }

  void showNotification(String message) {
    Notification notif = new Notification(message, Type.TRAY_NOTIFICATION);
    // Customize it
    notif.setDelayMsec(60000);
    notif.setPosition(Position.MIDDLE_CENTER);
    // Show it in the page
    notif.show(Page.getCurrent());

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
    gpcontainer.removeContainerProperty("fileTypes");
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
    this.submission.removeAllComponents();
    LOGGER.debug(workFlow.getName() + " chosen.");
    if (workFlow.getName().equals("MaxQuant")) {
      BeanItemContainer<RawFilesBean> rawFilesBeans =
          new BeanItemContainer<RawFilesBean>(RawFilesBean.class);
      BeanItemContainer<FastaBean> selectedfastas =
          new BeanItemContainer<FastaBean>(FastaBean.class);
      BeanItemContainer<FastaBean> fastas = new BeanItemContainer<FastaBean>(FastaBean.class);
      FastaDB db = new FastaDB();
      fastas.addAll(db.getAll());

      MaxQuantModel model =
          new MaxQuantModel(rawFilesBeans, projectDatasets, selectedfastas, fastas);
      VaadinSession.getCurrent().setConverterFactory(new MaxquantConverterFactory());
      MaxQuantComponent maxquantComponent = new MaxQuantComponent(model);
      maxquantComponent.setWorkflow(workFlow);
      maxquantComponent.addSubmissionListener(new MaxQuantSubmissionListener(maxquantComponent));

      this.submission.addComponent(maxquantComponent);
    } else if (workFlow.getName().contains("Microarray QC")) {
      MicroarrayQCComponent qcComp = new MicroarrayQCComponent(controller);
      qcComp.update(workFlow, projectDatasets);
      qcComp.addResetListener(new MicroarrayQCResetListener(qcComp));
      qcComp.addSubmissionListener(new MicroarrayQCSubmissionListener(qcComp));
      this.submission.addComponent(qcComp);
    } else {
      StandardWorkflowComponent standardComponent = new StandardWorkflowComponent(controller);
      standardComponent.update(workFlow, projectDatasets);
      standardComponent.addResetListener(new ResetListener(standardComponent));
      standardComponent.addSubmissionListener(new StandardSubmissionListener(standardComponent));
      this.submission.addComponent(standardComponent);
    }

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
          LOGGER.debug("selected wf");
          updateParameterView(selectedWorkflow, datasetBeans);
          submission.setVisible(true);

          // detailed Description should be visible
          availableWorkflows.setDetailsVisible(selectedWorkflow,
              !availableWorkflows.isDetailsVisible(selectedWorkflow));
        } else {
          LOGGER.warn("selected Workflow is null?");
          submission.setVisible(false);
        }

      }
    });
    availableWorkflows.setEditorEnabled(false);
  }

  private class ResetListener implements ClickListener {
    private static final long serialVersionUID = 6800369140265363672L;
    private StandardWorkflowComponent swc;

    public ResetListener(StandardWorkflowComponent standardWorkflowComponent) {
      swc = standardWorkflowComponent;
    }

    @Override
    public void buttonClick(ClickEvent event) {
      swc.resetParameters();
    }
  }

  public class MicroarrayQCResetListener implements ClickListener {
    private static final long serialVersionUID = -127474228749885664L;
    private MicroarrayQCComponent qcc;

    public MicroarrayQCResetListener(MicroarrayQCComponent wfComp) {
      qcc = wfComp;
    }

    @Override
    public void buttonClick(ClickEvent event) {
      qcc.resetParameters();
    }
  }

  /**
   * listens to clicks on submit button. Executes microarray qc workflow.
   * 
   * @author friedrich
   * 
   */
  private class MicroarrayQCSubmissionListener implements ClickListener {
    private static final long serialVersionUID = 24386950203184318L;
    private MicroarrayQCComponent comp;

    public MicroarrayQCSubmissionListener(MicroarrayQCComponent comp) {
      this.comp = comp;
    }

    @Override
    public void buttonClick(ClickEvent event) {
      try {
        List<DatasetBean> selectedDatasets = comp.getSelectedDatasets();
        comp.writeParametersToWorkflow();
        Workflow submittedWf = comp.getWorkflow();
        submit(submittedWf, new ArrayList<DatasetBean>(selectedDatasets));
      } catch (Exception e) {
        handleException(e);
      }
    }
  }

  /**
   * listenes to clicks on submit button. Executes standard workflow.
   * 
   * @author wojnar
   * 
   */
  private class StandardSubmissionListener implements ClickListener {
    private static final long serialVersionUID = 243869502031843198L;
    private StandardWorkflowComponent swc;

    public StandardSubmissionListener(StandardWorkflowComponent standardWorkflowComponent) {
      swc = standardWorkflowComponent;
    }

    @Override
    public void buttonClick(ClickEvent event) {
      try {
        List<DatasetBean> selectedDatasets = swc.getSelectedDatasets();
        Workflow submittedWf = swc.getWorkflow();
        submit(submittedWf, new ArrayList<DatasetBean>(selectedDatasets));
      } catch (Exception e) {
        handleException(e);
      }
    }
  }
  /**
   * listenes to clicks on submit button. Executes maxquant workflow.
   * 
   * @author wojnar
   * 
   */
  private class MaxQuantSubmissionListener implements ClickListener {
    private static final long serialVersionUID = 1888557742642278371L;
    private MaxQuantComponent maxQuantComponent;

    public MaxQuantSubmissionListener(MaxQuantComponent mqc) {
      maxQuantComponent = mqc;
    }

    @Override
    public void buttonClick(ClickEvent event) {

      try {
        maxQuantComponent.updateGroups();
        maxQuantComponent.writeParametersToWorkflow();
        Workflow submittedWf = maxQuantComponent.getWorkflow();
        Collection<DatasetBean> selectedDatasets = maxQuantComponent.getSelectedDatasets();
        submit(submittedWf, new ArrayList<DatasetBean>(selectedDatasets));
      } catch (Exception e) {
        handleException(e);
      }
    }

  }

  /**
   * submits workflow with given datasets
   * 
   * @throws SubmitFailedException
   * @throws IllegalArgumentException
   * @throws ConnectException
   */
  void submit(Workflow submittedWf, List<DatasetBean> selectedDatasets) throws ConnectException,
      IllegalArgumentException, SubmitFailedException {
    if (submittedWf == null || selectedDatasets.isEmpty()) {
      return;
    }
    // THIS IS THE IMPORTANT LINE IN THAT MESS
    String openbisId = controller.submitAndRegisterWf(type, id, submittedWf, selectedDatasets);
    showNotification("Workflow submitted and saved under " + openbisId);
  }

  /**
   * logs error and shows user that submission failed. Different exceptions yield different error
   * messages
   * 
   * @param e
   */
  public void handleException(Exception e) {
    if (e instanceof ConnectException || e instanceof IllegalArgumentException
        || e instanceof SubmitFailedException) {
      LOGGER.error("Submission failed, probably gUSE. " + e.getMessage(), e.getStackTrace());
      showNotification(SUBMISSION_FAILED_MESSAGE);
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
    } else if (e instanceof RemoteAccessException) {
      LOGGER.error("Submission failed, probably openbis. error message: " + e.getMessage(),
          e.getStackTrace());
      showNotification(SUBMISSION_FAILED_MESSAGE);
    } else {
      LOGGER.error("Internal error: " + e.getMessage(), e.getStackTrace());
      showNotification(SUBMISSION_FAILED_MESSAGE);
    }
  }
}
