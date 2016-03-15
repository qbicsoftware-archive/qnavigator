package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import logging.Logger;
import model.BiologicalEntitySampleBean;
import model.BiologicalSampleBean;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;


public class BiologicalSamplesComponent extends CustomComponent {

  private enum sampleTypes {
    Q_BIOLOGICAL_ENTITY, Q_BIOLOGICAL_SAMPLE
  };

  private enum propertyTypes {
    Q_ADDIIONAL_INFO, Q_EXTERNALDB_ID, Q_SECONDARY_NAME, Q_NCBI_ORGANISM
  };

  private static final long serialVersionUID = 8672873911284888801L;

  private VerticalLayout mainLayout;
  private static Logger LOGGER = new Log4j2Logger(BiologicalSamplesComponent.class);
  private Grid sampleBioGrid;
  private Grid sampleEntityGrid;

  private ChangeSampleMetadataComponent changeMetadata;

  VerticalLayout vert;

  private DataHandler datahandler;
  private State state;
  private String resourceUrl;

  private int numberOfBioSamples;

  private int numberOfEntitySamples;

  private BeanItemContainer<BiologicalSampleBean> samplesBio;

  private BeanItemContainer<BiologicalEntitySampleBean> samplesEntity;

  private String currentID;

  /**
   * 
   * @param dh
   * @param state
   * @param resourceurl
   * @param caption
   */
  public BiologicalSamplesComponent(DataHandler dh, State state, String resourceurl, String caption) {
    this.datahandler = dh;
    this.resourceUrl = resourceurl;
    this.state = state;
    changeMetadata = new ChangeSampleMetadataComponent(dh, state, resourceurl);

    this.setCaption(caption);

    this.initUI();
  }

  /**
   * 
   */
  private void initUI() {
    vert = new VerticalLayout();
    sampleBioGrid = new Grid();
    sampleEntityGrid = new Grid();

    sampleEntityGrid.addSelectionListener(new SelectionListener() {

      @Override
      public void select(SelectionEvent event) {
        BeanItem<BiologicalEntitySampleBean> selectedBean =
            samplesEntity.getItem(sampleEntityGrid.getSelectedRow());

        if (selectedBean == null) {
          TextField filterField =
              (TextField) sampleBioGrid.getHeaderRow(1).getCell("biologicalEntity").getComponent();
          filterField.setValue("");
        } else {
          TextField filterField =
              (TextField) sampleBioGrid.getHeaderRow(1).getCell("biologicalEntity").getComponent();
          filterField.setValue(selectedBean.getBean().getCode());
          // samplesBio.addContainerFilter("biologicalEntity",
          // selectedBean.getBean().getSecondaryName(), false, false);
        }
      }

    });

    mainLayout = new VerticalLayout(vert);

    this.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
    this.setCompositionRoot(mainLayout);
  }

  /**
   * 
   * @param id
   */
  public void updateUI(String id) {

    currentID = id;
    sampleBioGrid = new Grid();
    sampleEntityGrid = new Grid();

    sampleEntityGrid.addSelectionListener(new SelectionListener() {

      @Override
      public void select(SelectionEvent event) {
        BeanItem<BiologicalEntitySampleBean> selectedBean =
            samplesEntity.getItem(sampleEntityGrid.getSelectedRow());

        if (selectedBean == null) {
          TextField filterField =
              (TextField) sampleBioGrid.getHeaderRow(1).getCell("biologicalEntity").getComponent();
          filterField.setValue("");
        } else {
          TextField filterField =
              (TextField) sampleBioGrid.getHeaderRow(1).getCell("biologicalEntity").getComponent();
          filterField.setValue(selectedBean.getBean().getCode());
          // samplesBio.addContainerFilter("biologicalEntity",
          // selectedBean.getBean().getSecondaryName(), false, false);
        }
      }

    });

    if (id == null)
      return;

    BeanItemContainer<BiologicalSampleBean> samplesBioContainer =
        new BeanItemContainer<BiologicalSampleBean>(BiologicalSampleBean.class);
    BeanItemContainer<BiologicalEntitySampleBean> samplesEntityContainer =
        new BeanItemContainer<BiologicalEntitySampleBean>(BiologicalEntitySampleBean.class);

    List<Sample> allSamples = datahandler.getOpenBisClient().getSamplesOfProjectBySearchService(id);
    List<VocabularyTerm> terms = null;
    Map<String, String> termsMap = new HashMap<String, String>();

    for (Sample sample : allSamples) {

      if (sample.getSampleTypeCode().equals(sampleTypes.Q_BIOLOGICAL_ENTITY.toString())) {

        Map<String, String> sampleProperties = sample.getProperties();

        BiologicalEntitySampleBean newEntityBean = new BiologicalEntitySampleBean();
        newEntityBean.setCode(sample.getCode());
        newEntityBean.setId(sample.getIdentifier());
        newEntityBean.setType(sample.getSampleTypeCode());
        newEntityBean.setAdditionalInfo(sampleProperties.get("Q_ADDITIONAL_INFO"));
        newEntityBean.setExternalDB(sampleProperties.get("Q_EXTERNALDB_ID"));
        newEntityBean.setSecondaryName(sampleProperties.get("Q_SECONDARY_NAME"));

        String organismID = sampleProperties.get("Q_NCBI_ORGANISM");
        newEntityBean.setOrganism(organismID);

        if (terms != null) {
          if (termsMap.containsKey(organismID)) {
            newEntityBean.setOrganismName(termsMap.get(organismID));
          } else {
            for (VocabularyTerm term : terms) {
              if (term.getCode().equals(organismID)) {
                newEntityBean.setOrganismName(term.getLabel());
                break;
              }
            }
          }
        } else {
          for (Vocabulary vocab : datahandler.getOpenBisClient().getFacade().listVocabularies()) {
            if (vocab.getCode().equals("Q_NCBI_TAXONOMY")) {
              terms = vocab.getTerms();
              for (VocabularyTerm term : vocab.getTerms()) {
                if (term.getCode().equals(organismID)) {
                  newEntityBean.setOrganismName(term.getLabel());
                  termsMap.put(organismID, term.getLabel());
                  break;
                }
              }
              break;
            }
          }
        }

        newEntityBean.setProperties(sampleProperties);
        newEntityBean.setGender(sampleProperties.get("Q_GENDER"));
        samplesEntityContainer.addBean(newEntityBean);

        for (Sample child : datahandler.getOpenBisClient().getChildrenSamples(sample)) {

          if (child.getSampleTypeCode().equals(sampleTypes.Q_BIOLOGICAL_SAMPLE.toString())) {
            Sample realChild =
                datahandler.getOpenBisClient().getSampleByIdentifier(child.getIdentifier());

            Map<String, String> sampleBioProperties = realChild.getProperties();

            BiologicalSampleBean newBean = new BiologicalSampleBean();
            newBean.setCode(realChild.getCode());
            newBean.setId(realChild.getIdentifier());
            newBean.setType(realChild.getSampleTypeCode());
            newBean.setPrimaryTissue(sampleBioProperties.get("Q_PRIMARY_TISSUE"));
            newBean.setTissueDetailed(sampleBioProperties.get("Q_TISSUE_DETAILED"));
            newBean.setBiologicalEntity(sample.getCode());

            newBean.setAdditionalInfo(sampleBioProperties.get("Q_ADDITIONAL_INFO"));
            newBean.setExternalDB(sampleBioProperties.get("Q_EXTERNALDB_ID"));
            newBean.setSecondaryName(sampleBioProperties.get("Q_SECONDARY_NAME"));
            newBean.setProperties(sampleBioProperties);

            samplesBioContainer.addBean(newBean);
          }
        }
      }
    }


    numberOfBioSamples = samplesBioContainer.size();
    numberOfEntitySamples = samplesEntityContainer.size();

    samplesBio = samplesBioContainer;
    samplesEntity = samplesEntityContainer;

    sampleEntityGrid.removeAllColumns();

    final GeneratedPropertyContainer gpcEntity = new GeneratedPropertyContainer(samplesEntity);
    gpcEntity.removeContainerProperty("id");
    gpcEntity.removeContainerProperty("type");
    gpcEntity.removeContainerProperty("organismName");
    gpcEntity.removeContainerProperty("organism");

    sampleEntityGrid.setContainerDataSource(gpcEntity);
    sampleEntityGrid.setColumnReorderingAllowed(true);

    gpcEntity.addGeneratedProperty("Organism", new PropertyValueGenerator<String>() {

      @Override
      public Class<String> getType() {
        return String.class;
      }

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        String ncbi =
            String
                .format(
                    "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Undef&name=%s&lvl=0&srchmode=1&keep=1&unlock' target='_blank'>%s</a>",
                    item.getItemProperty("organism").getValue(),
                    item.getItemProperty("organismName").getValue());
        String link = String.format("<a href='%s", ncbi);

        return link;
      }
    });

    sampleEntityGrid.getColumn("Organism").setRenderer(new HtmlRenderer());

    final GeneratedPropertyContainer gpcBio = new GeneratedPropertyContainer(samplesBio);
    gpcBio.removeContainerProperty("id");
    gpcBio.removeContainerProperty("type");

    sampleBioGrid.setContainerDataSource(gpcBio);
    sampleBioGrid.setColumnReorderingAllowed(true);
    sampleBioGrid.setColumnOrder("secondaryName", "code");

    gpcEntity.addGeneratedProperty("edit", new PropertyValueGenerator<String>() {
      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        return "Edit";
      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    gpcBio.addGeneratedProperty("edit", new PropertyValueGenerator<String>() {
      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        return "Edit";
      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    sampleEntityGrid.addItemClickListener(new ItemClickListener() {

      @Override
      public void itemClick(ItemClickEvent event) {

        BeanItem selected = (BeanItem) samplesEntity.getItem(event.getItemId());
        BiologicalEntitySampleBean selectedExp = (BiologicalEntitySampleBean) selected.getBean();

        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(selectedExp.getId());
        message.add("sample");
        state.notifyObservers(message);
      }
    });

    sampleEntityGrid.getColumn("edit").setRenderer(new ButtonRenderer(new RendererClickListener() {

      @Override
      public void click(RendererClickEvent event) {
        BeanItem selected = (BeanItem) samplesEntity.getItem(event.getItemId());
        BiologicalEntitySampleBean selectedSample = (BiologicalEntitySampleBean) selected.getBean();

        LOGGER.debug("clicked");
        Window subWindow = new Window("Edit Metadata");

        changeMetadata.updateUI(selectedSample.getId(), selectedSample.getType());
        VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subContent.addComponent(changeMetadata);
        subWindow.setContent(subContent);
        // Center it in the browser window
        subWindow.center();
        subWindow.setModal(true);
        subWindow.setIcon(FontAwesome.PENCIL);
        subWindow.setHeight("75%");

        subWindow.addCloseListener(new CloseListener() {
          /**
           * 
           */
          private static final long serialVersionUID = -1329152609834711109L;

          @Override
          public void windowClose(CloseEvent e) {
            LOGGER.debug("OMG window is closes");
            updateUI(currentID);
          }
        });

        QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
        ui.addWindow(subWindow);
      }
    }));
    sampleEntityGrid.getColumn("edit").setWidth(70);
    sampleEntityGrid.getColumn("edit").setHeaderCaption("");
    sampleEntityGrid.setColumnOrder("edit", "secondaryName", "Organism", "properties", "code",
        "additionalInfo", "gender", "externalDB");

    sampleBioGrid.addItemClickListener(new ItemClickListener() {

      @Override
      public void itemClick(ItemClickEvent event) {

        BeanItem selected = (BeanItem) samplesBio.getItem(event.getItemId());
        BiologicalSampleBean selectedExp = (BiologicalSampleBean) selected.getBean();

        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(selectedExp.getId());
        message.add("sample");
        state.notifyObservers(message);
      }
    });

    sampleBioGrid.getColumn("edit").setRenderer(new ButtonRenderer(new RendererClickListener() {

      @Override
      public void click(RendererClickEvent event) {
        BeanItem selected = (BeanItem) samplesBio.getItem(event.getItemId());
        BiologicalSampleBean selectedSample = (BiologicalSampleBean) selected.getBean();

        LOGGER.debug("clicked");
        Window subWindow = new Window();

        LOGGER.debug(selectedSample.getId());
        LOGGER.debug(selectedSample.getType());
        changeMetadata.updateUI(selectedSample.getId(), selectedSample.getType());
        VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subContent.addComponent(changeMetadata);
        subWindow.setContent(subContent);
        // Center it in the browser window
        subWindow.center();
        subWindow.setModal(true);
        subWindow.setResizable(false);

        subWindow.addCloseListener(new CloseListener() {
          /**
           * 
           */
          private static final long serialVersionUID = -1329152609834711109L;

          @Override
          public void windowClose(CloseEvent e) {
            updateUI(currentID);
          }
        });

        QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
        ui.addWindow(subWindow);
      }
    }));

    sampleBioGrid.getColumn("edit").setWidth(70);
    sampleBioGrid.getColumn("edit").setHeaderCaption("");
    sampleBioGrid.setColumnOrder("edit", "secondaryName", "primaryTissue", "properties",
        "tissueDetailed", "code", "additionalInfo", "biologicalEntity", "externalDB");

    sampleBioGrid.getColumn("biologicalEntity").setHeaderCaption("Source");

    helpers.GridFunctions.addColumnFilters(sampleBioGrid, gpcBio);
    helpers.GridFunctions.addColumnFilters(sampleEntityGrid, gpcEntity);
    this.buildLayout();
  }

  /**
   * Precondition: {DatasetView#table} has to be initialized. e.g. with
   * {DatasetView#buildFilterTable} If it is not, strange behaviour has to be expected. builds the
   * Layout of this view.
   */
  private void buildLayout() {
    this.vert.removeAllComponents();
    this.vert.setWidth("100%");

    // Table (containing datasets) section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    HorizontalLayout sampletableSectionContent = new HorizontalLayout();

    tableSectionContent.setMargin(new MarginInfo(true, false, true, false));
    sampletableSectionContent.setMargin(new MarginInfo(true, false, false, false));

    // tableSectionContent.setCaption("Datasets");
    // tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSection
        .addComponent(new Label(
            String
                .format(
                    "This view shows the sample sources (e.g., human, mouse) to be studied and the corresponding extracted samples. With sample sources, information specific to the subject (e.g., age or BMI in the case of patient data) can be stored. The extracted sample is a sample which has been extracted from the corresponding sample source. This is the raw sample material that can be later prepared for specific analytical methods such as MS or NGS. "
                        + "\n\n There are %s extracted  samples coming from %s distinct sample sources in this study.",
                    numberOfBioSamples, numberOfEntitySamples), Label.CONTENT_PREFORMATTED));

    tableSectionContent.addComponent(sampleBioGrid);
    sampletableSectionContent.addComponent(sampleEntityGrid);

    sampleEntityGrid.setCaption("Sample Sources");
    sampleBioGrid.setCaption("Extracted Samples");

    tableSection.setMargin(new MarginInfo(true, false, false, true));
    tableSection.setSpacing(true);

    tableSection.addComponent(sampletableSectionContent);
    tableSection.addComponent(tableSectionContent);
    this.vert.addComponent(tableSection);

    sampleBioGrid.setWidth("100%");
    sampleEntityGrid.setWidth("100%");

    tableSection.setWidth("100%");
    sampletableSectionContent.setWidth("100%");
    tableSectionContent.setWidth("100%");

    // this.table.setSizeFull();

    HorizontalLayout buttonLayout = new HorizontalLayout();
    // buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.setHeight(null);
    // buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

  }
}
