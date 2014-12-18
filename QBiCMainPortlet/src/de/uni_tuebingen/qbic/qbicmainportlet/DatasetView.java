package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletSession;
import javax.portlet.ResourceURL;

import org.apache.catalina.util.Base64;
import org.tepi.filtertable.FilterTreeTable;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("deprecation")
public class DatasetView extends Panel {


  /**
   * 
   */
  private static final long serialVersionUID = 8672873911284888801L;

  private Label general_information;
  // private final FilterTable table;
  private final FilterTreeTable table;
  private HierarchicalContainer datasets;
  VerticalLayout vert;
  private final String DOWNLOAD_BUTTON_CAPTION = "Download";
  private final String VISUALIZE_BUTTON_CAPTION = "Visualize";

  private final ButtonLink download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(
      ""));

  private final String[] FILTER_TABLE_COLUMNS = new String[] {"Select", "Project", "Sample",
      "Sample Type", "File Name", "File Type", "Dataset Type", "Registration Date", "Validated",
      "File Size"};

  public DatasetView() {
    this.vert = new VerticalLayout();
    this.table = buildFilterTable();
    // this.buildLayout();
    this.setContent(vert);
    // initMppListener();
  }

  public DatasetView(HierarchicalContainer dataset) {
    this.vert = new VerticalLayout();
    this.datasets = dataset;
    this.table = buildFilterTable();
    this.buildLayout();
    this.setContainerDataSource(this.datasets);
    this.setContent(vert);
  }


  public void setContainerDataSource(HierarchicalContainer newDataSource) {
    this.datasets = (HierarchicalContainer) newDataSource;
    this.buildLayout();
    this.table.setContainerDataSource(this.datasets);

    this.table.setColumnCollapsed("state", true);

    this.table.setVisibleColumns((Object[]) FILTER_TABLE_COLUMNS);

    this.table.setSizeFull();
  }


  // public void setInfo(String name, String entity) {
  // this.general_information.setValue(String.format("Name: %s\nEntity Type: %s\n",name));
  // }
  /*
   * private void initMppListener(){ 
   * MpPortletListener mppl = new MpPortletListener(this.download,this.table); 
   * this.table.addValueChangeListener(mppl); 
   * if (VaadinSession.getCurrent() instanceof VaadinPortletSession) { 
   * VaadinPortletSession portletsession = (VaadinPortletSession) VaadinSession.getCurrent(); // Add a custom listener to handle action and // render requests.
   * portletsession.addPortletListener(mppl); } 
   * UI.getCurrent().getSession().setAttribute("mppl", mppl); }
   */
  /**
   * Precondition: {DatasetView#table} has to be initialized. e.g. with
   * {DatasetView#buildFilterTable} If it is not, strange behaviour has to be expected. builds the
   * Layout of this view.
   */
  private void buildLayout() {
    // Layout
    // this.setSizeFull();
    // this.setVisible(true);
    this.vert.removeAllComponents();

    VerticalLayout statistics = new VerticalLayout();
    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(String.format("%s dataset(s).", this.datasets.size())));
    statContent.setMargin(true);
    statContent.setSpacing(true);
    statistics.addComponent(statContent);
    statistics.setMargin(true);
    this.vert.addComponent(statistics);


    // Table (containing datasets) section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();

    tableSectionContent.setCaption("Registered Datasets");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);

    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);

    tableSection.addComponent(tableSectionContent);
    this.vert.addComponent(tableSection);

    this.table.setSizeFull();

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(false);

    // this.download = new ButtonLink(DOWNLOAD_BUTTON_CAPTION, new ExternalResource(""));
    this.download.setEnabled(false);
    // this.download.setStyleName(Reindeer.BUTTON_SMALL);
    final Button visualize = new Button(VISUALIZE_BUTTON_CAPTION);
    visualize.setEnabled(false);
    buttonLayout.addComponent(this.download);
    buttonLayout.addComponent(visualize);


    /**
     * prepare download.
     */
    this.table.addValueChangeListener(new ValueChangeListener() {
      @Override
      public void valueChange(ValueChangeEvent event) {
        Set<Object> currentSelectedTableIndices = (Set<Object>) event.getProperty().getValue();
        System.out.println(currentSelectedTableIndices);
        Object next;
        if (currentSelectedTableIndices != null) {

          if (currentSelectedTableIndices.size() == 1) {
            DataHandler dataHanlder =
                (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
            Iterator<Object> iterator = currentSelectedTableIndices.iterator();
            next = iterator.next();
            String datasetCode = (String) table.getItem(next).getItemProperty("CODE").getValue();
            String datasetType =
                (String) table.getItem(next).getItemProperty("File Name").getValue();
            try {
              download.setResource(new ExternalResource(dataHanlder.getUrlForDataset(datasetCode,
                  datasetType)));
              download.setEnabled(true);
            } catch (MalformedURLException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            currentSelectedTableIndices = null;
          } else if (currentSelectedTableIndices.size() > 1) {
            DataHandler dataHandler =
                (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
            PortletSession portletSession =
                ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
            String resourceUrl =
                (String) portletSession.getAttribute("resURL",
                    PortletSession.APPLICATION_SCOPE);
            
            // TODO use table to create map and save it in portletSession!!    
            Map<String, AbstractMap.SimpleEntry<InputStream, Long>> selected_datasets = new HashMap<String, AbstractMap.SimpleEntry<InputStream, Long>>();
            Iterator<Object> itemIterator = currentSelectedTableIndices.iterator();
            
            while (itemIterator.hasNext()){
              next = itemIterator.next();
             
              //TODO change this to some flag or something like that
              if(table.getItem(next).getItemProperty("File Type").getValue().equals("Folder")){
                Collection<?> filesInFolder = table.getChildren(next);
                
                String folderName =
                    (String) table.getItem(next).getItemProperty("File Name").getValue();
                
                for(Object itemID: filesInFolder) {
                  String datasetChildCode = (String) table.getItem(itemID).getItemProperty("CODE").getValue();
                  String datasetChildName =
                      (String) table.getItem(itemID).getItemProperty("File Name").getValue();
                  InputStream datasetChildStream =
                      dataHandler.getDatasetStream(datasetChildCode, folderName);
                  Long datasetChildSize =
                      (Long) table.getItem(itemID).getItemProperty("file_size_bytes").getValue();
                  
                  selected_datasets.put(String.format("%s/%s", folderName, datasetChildName) , new AbstractMap.SimpleEntry<InputStream, Long>(datasetChildStream, datasetChildSize));                  
                }
              }
                else {
                  String datasetCode = (String) table.getItem(next).getItemProperty("CODE").getValue();
                  String datasetFileName =
                      (String) table.getItem(next).getItemProperty("File Name").getValue();
                  InputStream datasetFileStream =
                      dataHandler.getDatasetStream(datasetCode);
                  Long datasetFileSize =
                      (Long) table.getItem(next).getItemProperty("file_size_bytes").getValue();
                  
                  selected_datasets.put(datasetFileName, new AbstractMap.SimpleEntry<InputStream, Long>(datasetFileStream, datasetFileSize));
                }
            
              
              //TODO fix file size in datahandler

            }
            
            System.out.println(selected_datasets);
            // set Map with marked datasets as session attribute
            portletSession.setAttribute("qbic_download", selected_datasets, PortletSession.APPLICATION_SCOPE);
            
            download.setResource(new ExternalResource(resourceUrl));
            download.setEnabled(true);// UI.getCurrent().getPage().getWebBrowser().isChrome());
          } else {
            // nothing selected. Probably will never occur, because then the set is probably null
            currentSelectedTableIndices = null;

            download.setEnabled(false);
          }
        } else {
          download.setEnabled(false);
        }

      }

    });
    /*
     * Update the visualize button. It is only enabled, if the files can be visualized.
     */
    this.table.addValueChangeListener(new ValueChangeListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -4875903343717437913L;


      /**
       * check for what selection can be visualized. If so, enable the button.
       */
      @Override
      public void valueChange(ValueChangeEvent event) {
        // Nothing selected or more than one selected.
        Set<Object> selectedValues = (Set<Object>) event.getProperty().getValue();
        if (selectedValues == null || selectedValues.size() == 0 || selectedValues.size() > 1) {
          visualize.setEnabled(false);
          return;
        }
        // if one selected check whether its dataset type is either fastqc or qcml.
        // For now we only visulize these two file types.
        Iterator<Object> iterator = selectedValues.iterator();
        Object next = iterator.next();
        String datasetType =
            (String) table.getItem(next).getItemProperty("Dataset Type").getValue();
        // TODO: No hardcoding!!
        if (datasetType.equals("FASTQC") || datasetType.equals("QCML") || datasetType.equals("BAM")
            || datasetType.equals("VCF")) {
          visualize.setEnabled(true);
        } else {
          visualize.setEnabled(false);
        }
      }
    });

    /*
     * Send message that in datasetview the following was selected. WorkflowViews get those messages
     * and save them, if it is valid information for them.
     */
    this.table.addValueChangeListener(new ValueChangeListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -3554627008191389648L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // Nothing selected or more than one selected.
        Set<Object> selectedValues = (Set<Object>) event.getProperty().getValue();
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("DataSetView");
        if (selectedValues != null && selectedValues.size() == 1) {
          Iterator<Object> iterator = selectedValues.iterator();
          Object next = iterator.next();
          String datasetType =
              (String) table.getItem(next).getItemProperty("Dataset Type").getValue();
          message.add(datasetType);
          String project = (String) table.getItem(next).getItemProperty("Project").getValue();
          DataHandler datahandler =
              (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
          String space = datahandler.openBisClient.getProjectByCode(project).getSpaceCode();// .getIdentifier().split("/")[1];
          message.add(project);
          message.add((String) table.getItem(next).getItemProperty("Sample").getValue());
          message.add((String) table.getItem(next).getItemProperty("Sample Type").getValue());
          message.add((String) table.getItem(next).getItemProperty("dl_link").getValue());
          message.add((String) table.getItem(next).getItemProperty("File Name").getValue());
          message.add(space);
          state.notifyObservers(message);
        } else {
          message.add("null");
        }
        state.notifyObservers(message);

      }
    });

    // Assumes that table Value Change listner is enabling or disabling the button if preconditions
    // are not fullfilled
    visualize.addClickListener(new ClickListener() {
      /**
       * 
       */
      private static final long serialVersionUID = 9015273307461506369L;

      @Override
      public void buttonClick(ClickEvent event) {
        Set<Object> selectedValues = (Set<Object>) table.getValue();
        Iterator<Object> iterator = selectedValues.iterator();
        Object next = iterator.next();
        String datasetCode = (String) table.getItem(next).getItemProperty("CODE").getValue();
        String datasetFileName =
            (String) table.getItem(next).getItemProperty("File Name").getValue();
        DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
        URL url;
        try {
          url = dh.getUrlForDataset(datasetCode, datasetFileName);
          Window subWindow =
              new Window("QC of Sample: "
                  + (String) table.getItem(next).getItemProperty("Sample").getValue());
          VerticalLayout subContent = new VerticalLayout();
          subContent.setMargin(true);
          subWindow.setContent(subContent);
          QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
          // Put some components in it
          Resource res = null;
          String datasetType =
              (String) table.getItem(next).getItemProperty("Dataset Type").getValue();
          final RequestHandler rh = new ProxyForGenomeViewerRestApi();
          boolean rhAttached = false;
          if (datasetType.equals("QCML")) {
            QcMlOpenbisSource re = new QcMlOpenbisSource(url);
            StreamResource streamres = new StreamResource(re, "test-file");
            streamres.setMIMEType("application/xml");
            res = streamres;
          } else if (datasetType.equals("FASTQC")) {
            res = new ExternalResource(url);
          } else if (datasetType.equals("BAM") || datasetType.equals("VCF")) {
            String filePath = (String) table.getItem(next).getItemProperty("dl_link").getValue();
            filePath = String.format("/store%s", filePath.split("store")[1]);
            String fileId = (String) table.getItem(next).getItemProperty("File Name").getValue();
            fileId = "control.1kg.panel.samples.vcf.gz";
            // UI.getCurrent().getSession().addRequestHandler(rh);
            rhAttached = true;
            ThemeDisplay themedisplay =
                (ThemeDisplay) VaadinService.getCurrentRequest()
                    .getAttribute(WebKeys.THEME_DISPLAY);
            String hostTmp =
                "http://localhost:8080/web/guest/mainportlet?p_p_id=QbicmainportletApplicationPortlet_WAR_QBiCMainPortlet_INSTANCE_5pPd5JQ8uGOt&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_count=1";
            hostTmp +=
                "&qbicsession=" + UI.getCurrent().getSession().getAttribute("gv-restapi-session")
                    + "&someblabla=";
            // String hostTmp = themedisplay.getURLPortal() +
            // UI.getCurrent().getPage().getLocation().getPath() + "?qbicsession=" +
            // UI.getCurrent().getSession().getAttribute("gv-restapi-session") + "&someblabla=" ;
            System.out.println(hostTmp);
            String host = Base64.encode(hostTmp.getBytes());
            System.out.println(host);
            String title = (String) table.getItem(next).getItemProperty("Sample").getValue();
            res =
                new ExternalResource(
                    String
                        .format(
                            "http://localhost:7777/genomeviewer/?host=%s&title=%s&fileid=%s&featuretype=alignments&filepath=%s&removeZeroGenotypes=false",
                            host, title, fileId, filePath));
          }
          System.out.println(res.toString());
          BrowserFrame frame = new BrowserFrame("", res);
          if (rhAttached) {
            frame.addDetachListener(new DetachListener() {

              /**
               * 
               */
              private static final long serialVersionUID = 1534523447730906543L;

              @Override
              public void detach(DetachEvent event) {
                UI.getCurrent().getSession().removeRequestHandler(rh);
              }

            });
          }

          frame.setSizeFull();
          subContent.addComponent(frame);

          // Center it in the browser window
          subWindow.center();
          subWindow.setModal(true);
          subWindow.setSizeFull();

          frame.setHeight((int) (ui.getPage().getBrowserWindowHeight() * 0.8), Unit.PIXELS);
          // Open it in the UI
          ui.addWindow(subWindow);
        } catch (MalformedURLException e) {
          System.out.println("MalformedURLException");
          Notification
              .show(
                  "Given dataset has no file attached to it!! Please Contact your project manager. Or check whether it already has some data",
                  Notification.Type.ERROR_MESSAGE);
        }
      }
    });

    // this.general_information = new Label("Name: \nEntity Type: \nOwner: \n",
    // Label.CONTENT_PREFORMATTED);
    // this.general_information.setCaption("General Information: ");

    // this.addComponent(this.general_information);
    this.vert.addComponent(buttonLayout);

  }


  // private FilterTable buildFilterTable() {
  private FilterTreeTable buildFilterTable() {
    // FilterTable filterTable = new FilterTable();
    FilterTreeTable filterTable = new FilterTreeTable();
    filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);
    filterTable.setMultiSelect(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    if (this.datasets != null) {
      filterTable.setContainerDataSource(this.datasets);
    }

    /*
     * filterTable.setItemDescriptionGenerator(new ColumnDescriptionGenerator() {
     * 
     * 
     * @Override public String generateDescription(Component source, Object itemId, Object
     * propertyId) { return propertyId.toString() + " of the corresponding dataset."; } });
     */
    return filterTable;
  }
}
