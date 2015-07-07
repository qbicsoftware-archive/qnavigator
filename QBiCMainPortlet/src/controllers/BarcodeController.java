package controllers;

import helpers.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.BarcodeCreator;
import main.OpenBisClient;
import model.ExperimentBarcodeSummaryBean;
import model.IBarcodeBean;
import model.NewModelBarcodeBean;
import model.NewSampleModelBean;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Extension;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

import de.uni_tuebingen.qbic.qbicmainportlet.WizardBarcodeView;

/**
 * Controls preparation and creation of barcode files
 * 
 * @author Andreas Friedrich
 * 
 */

public class BarcodeController {

  private WizardBarcodeView view;
  private OpenBisClient openbis;
  private BarcodeCreator creator;

  List<IBarcodeBean> barcodeBeans;
  String hi;

  private List<String> barcodeExperiments = new ArrayList<String>(Arrays.asList(
      "Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION", "Q_NGS_MEASUREMENT"));

  /**
   * @param bw BarcodeView instance
   * @param openbis OpenBisClient API
   * @param barcodeScripts Path to different barcode creation scripts
   * @param pathVar Path variable so python scripts can work when called from the JVM
   */
  public BarcodeController(WizardBarcodeView bw, OpenBisClient openbis, String barcodeScripts,
      String pathVar) {
    view = bw;
    this.openbis = openbis;
    creator = new BarcodeCreator(barcodeScripts, pathVar);
  }

  /**
   * Initializes all listeners
   */
  @SuppressWarnings("serial")
  public void init() {
    /**
     * Button listeners
     */
    Button.ClickListener cl = new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Prepare Barcodes")) {
          view.creationPressed();
          Iterator<Extension> it = view.getButtonSheet().getExtensions().iterator();
          if (it.hasNext())
            view.getButtonSheet().removeExtension(it.next());
          it = view.getButtonTube().getExtensions().iterator();
          if (it.hasNext())
            view.getButtonTube().removeExtension(it.next());
          barcodeBeans = getSamplesFromExperimentSummaries(view.getExperiments());
          Collection<String> options = (Collection<String>) view.getPrepOptionGroup().getValue();
          if (options.size() == 2) {
            creator.findOrCreateBarcodesWithProgress(barcodeBeans, view.getProgressBar(),
                view.getProgressInfo(), new BarcodesReadyRunnable(view, creator, barcodeBeans));
          } else if (options.contains("Sample Tube Barcodes")) {
            creator.findOrCreateTubeBarcodesWithProgress(barcodeBeans, view.getProgressBar(),
                view.getProgressInfo(), new TubeBarcodesReadyRunnable(view, creator, barcodeBeans));
          } else if (options.contains("Sample Sheet Barcodes")) {
            creator
                .findOrCreateSheetBarcodesWithProgress(barcodeBeans, view.getProgressBar(), view
                    .getProgressInfo(), new SheetBarcodesReadyRunnable(view, creator, barcodeBeans));
          }
        }
      }
    };
    for (Button b : view.getButtons())
      b.addClickListener(cl);

    /**
     * Space selection listener
     */
    ValueChangeListener spaceSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        view.resetProjects();
        String space = view.getSpaceCode();
        if (space != null) {
          List<String> projects = new ArrayList<String>();
          for (Project p : openbis.getProjectsOfSpace(space)) {
            projects.add(p.getCode());
          }
          view.setProjectCodes(projects);
        }
      }

    };
    ComboBox space = view.getSpaceBox();
    if (space != null)
      space.addValueChangeListener(spaceSelectListener);

    /**
     * Project selection listener
     */

    ValueChangeListener projectSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        view.resetExperiments();
        String project = view.getProjectCode();
        if (project != null) {
          reactToProjectSelection(project);
        }
      }

    };
    ComboBox project = view.getProjectBox();
    if (project != null)
      project.addValueChangeListener(projectSelectListener);

    /**
     * Experiment selection listener
     */

    ValueChangeListener expSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        barcodeBeans = null;
        view.reset();
        view.enablePrep(expSelected() && optionSelected());
        if (expSelected() && tubesSelected())
          view.enablePreview(getUsefulSampleFromExperiment());
      }
    };
    view.getExperimentTable().addValueChangeListener(expSelectListener);

    ValueChangeListener optionListener = new ValueChangeListener() {
      @Override
      public void valueChange(ValueChangeEvent event) {
        if (optionSelected()) {
          view.enablePrep(expSelected());
          if (tubesSelected() && expSelected())
            view.enablePreview(getUsefulSampleFromExperiment());
          else
            view.disablePreview();
        } else
          view.disablePreview();


      }
    };
    view.getPrepOptionGroup().addValueChangeListener(optionListener);
  }

  public void reactToProjectSelection(String project) {
    List<ExperimentBarcodeSummaryBean> beans = new ArrayList<ExperimentBarcodeSummaryBean>();
    for (Experiment e : openbis.getExperimentsOfProjectByCode(project)) {
      String type = e.getExperimentTypeCode();
      List<Sample> samples = openbis.getSamplesofExperiment(e.getIdentifier());
      if (barcodeExperiments.contains(type) && samples.size() > 0) {
        String expID = e.getIdentifier();
        List<String> ids = new ArrayList<String>();
        for (Sample s : samples) {
          if (BarcodeFunctions.isQbicBarcode(s.getCode()))
            ids.add(s.getCode());
        }
        int numOfSamples = ids.size();
        String bioType = null;
        int i = 0;
        if (type.equals(barcodeExperiments.get(0))) {
          while (bioType == null) {
            bioType = samples.get(i).getProperties().get("Q_PRIMARY_TISSUE");
            i++;
          }
        }
        if (type.equals(barcodeExperiments.get(1))) {
          while (bioType == null) {
            bioType = samples.get(i).getProperties().get("Q_SAMPLE_TYPE");
            i++;
          }
        }
        if (type.equals(barcodeExperiments.get(2))) {
          bioType = e.getProperties().get("Q_SEQUENCING_TYPE");
        }
        beans.add(new ExperimentBarcodeSummaryBean(bioType, Integer.toString(numOfSamples), expID));
      }
    }
    view.setExperiments(beans);
  }

  private Sample getUsefulSampleFromExperiment() {
    List<Sample> samples =
        openbis.getSamplesofExperiment(view.getExperiments().iterator().next().fetchExperimentID());
    int i = 0;
    String code = "";
    while (!BarcodeFunctions.isQbicBarcode(code)) {
      code = samples.get(i).getCode();
      i++;
    }
    return samples.get(i);
  }

  private boolean tubesSelected() {
    return ((Collection<String>) view.getPrepOptionGroup().getValue())
        .contains("Sample Tube Barcodes");
  }

  private boolean expSelected() {
    return view.getExperiments().size() > 0;
  }

  private boolean optionSelected() {
    return ((Collection<String>) view.getPrepOptionGroup().getValue()).size() > 0;
  }

  protected List<IBarcodeBean> getSamplesFromExperimentSummaries(
      Collection<ExperimentBarcodeSummaryBean> experiments) {
    List<IBarcodeBean> samples = new ArrayList<IBarcodeBean>();
    List<String> types =
        new ArrayList<String>(Arrays.asList("Q_BIOLOGICAL_SAMPLE", "Q_TEST_SAMPLE"));
    List<Sample> openbisSamples = new ArrayList<Sample>();
    for (ExperimentBarcodeSummaryBean b : experiments) {
      openbisSamples.addAll(openbis.getSamplesofExperiment(b.fetchExperimentID()));
    }
    Map<Sample, List<String>> parentMap = getParentMap(openbisSamples);
    for (Sample s : openbisSamples) {
      String type = s.getSampleTypeCode();
      String bioType = "unknown";
      if (type.equals(types.get(0))) {
        bioType = s.getProperties().get("Q_PRIMARY_TISSUE");
      }
      if (type.equals(types.get(1))) {
        bioType = s.getProperties().get("Q_SAMPLE_TYPE");
      }
      if (types.contains(type))
        samples.add(new NewModelBarcodeBean(s.getCode(), view.getCodedString(s), view.getInfo1(s),
            view.getInfo2(s), bioType, parentMap.get(s)));
    }
    return samples;
  }

  protected Map<Sample, List<String>> getParentMap(List<Sample> samples) {
    List<String> codes = new ArrayList<String>();
    for (Sample s : samples) {
      codes.add(s.getCode());
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("codes", codes);
    QueryTableModel resTable = openbis.getAggregationService("get-parentmap", params);
    Map<String, List<String>> parentMap = new HashMap<String, List<String>>();
    String curCode = (String) resTable.getRows().get(0)[0];
    List<String> parents = new ArrayList<String>();
    for (Serializable[] ss : resTable.getRows()) {
      String code = (String) ss[0];
      String parent = (String) ss[1];
      if (code.equals(curCode))
        parents.add(parent);
      else {
        parentMap.put(curCode, parents);
        parents = new ArrayList<String>();
        curCode = code;
      }
    }
    Map<Sample, List<String>> res = new HashMap<Sample, List<String>>();
    for (Sample s : samples) {
      List<String> prnts = parentMap.get(s.getCode());
      if (prnts == null)
        prnts = new ArrayList<String>();
      res.put(s, prnts);
    }
    return res;
  }
}