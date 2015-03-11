  package de.uni_tuebingen.qbic.qbicmainportlet;

  import helpers.OpenBisFunctions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentBean;

  import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.TextField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Image;



  public class ChangePropertiesView extends Panel implements View{
      
      
      /**
       * 
       */
      private static final long serialVersionUID = 8672873911284888801L;
      static Logger LOGGER = new Log4j2Logger(ChangePropertiesView.class);
      static String navigateToLabel =  "changePropertiesView";
      private IndexedContainer datasets;
      private FormLayout form;
      private FieldGroup fieldGroup;
      VerticalLayout vert;
      String id;
      private DataHandler datahandler;
        
      public ChangePropertiesView(DataHandler datahandler, IndexedContainer datasource, String id) {
        this.datahandler = datahandler;
        vert = new VerticalLayout();
        this.id = id;
        this.datasets = datasource;
        this.setContent(vert);

      }


      public ChangePropertiesView(DataHandler datahandler) {
        // execute the above constructor with default settings, in order to have the same settings
        this(datahandler, new IndexedContainer(), "No Experiment selected");
      }
      
      public void setContainerDataSource(final ExperimentBean experimentBean, final String id){
        this.buildLayout();
        this.id = id;
        this.buildFormLayout(experimentBean);
        
        final Button saveButton = new Button("Commit Changes", new ClickListener() {
          @Override
          public void buttonClick(final ClickEvent event) {
            DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
            
            HashMap<String, Object> properties = new HashMap<String,Object>();
            Collection<Field<?>> registeredFields = fieldGroup.getFields();
            
            for(Field<?> field: registeredFields) {
              properties.put(field.getCaption(), field.getValue());
            }
            
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("identifier", experimentBean.getId());
            parameters.put("properties", properties);
     
            dh.openBisClient.triggerIngestionService("notify-user", parameters);
            Notification.show("Changes committed!", Type.TRAY_NOTIFICATION);
          }
        });
        
        VerticalLayout statistics = new VerticalLayout();
        statistics.addComponent(new Label(String.format("Experiment ID: %s",
            id)));
        statistics.addComponent(new Label(String.format("Experiment Type: %s",
            experimentBean.getId())));
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        
        buttonLayout.setHeight(null);
        buttonLayout.setWidth("100%");
        buttonLayout.setSpacing(false);
        buttonLayout.addComponent(saveButton);
        
        this.vert.addComponent(statistics);
        this.vert.addComponent(this.form);
        this.vert.addComponent(buttonLayout);
        this.form.setSizeFull();
        this.vert.setSizeFull();
        this.setSizeFull();
      }
      
      /**
       * Precondition: {DatasetView#table} has to be initialized. e.g. with {DatasetView#buildFilterTable} If it is not, strange behaviour has to be expected.
       * builds the Layout of this view. 
       */
      private void buildLayout(){
          //Layout
          this.vert.removeAllComponents();
          this.setSizeFull();
          this.setVisible(true);
         
          this.vert.setSpacing(true);
          this.vert.setMargin(true);
          this.vert.setSizeFull();
          }
      
  private void buildFormLayout(ExperimentBean experimentBean) {
    final FieldGroup fieldGroup = new FieldGroup();
    final FormLayout form2 = new FormLayout();
    
    for(String key: experimentBean.getProperties().keySet()){
      if(experimentBean.getControlledVocabularies().keySet().contains(key)) {
        ComboBox select = new ComboBox(key);
        fieldGroup.bind(select,key);
        form2.addComponent(select);
        
        // Add items with given item IDs
        for(String item: experimentBean.getControlledVocabularies().get(key)) {
          select.addItem(item);
        }
        select.setValue(experimentBean.getProperties().get(key));
      } else {
      TextField tf = new TextField(key);
      tf.setValue(experimentBean.getProperties().get(key));
      fieldGroup.bind(tf,key);
      form2.addComponent(tf);
      }
    }
    this.fieldGroup = fieldGroup;
    this.form = form2;    
  }


  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    System.out.println("currentValue: " + currentValue);
    System.out.println("navigateToLabel: " + navigateToLabel);
    try {
      //TODO fix datahandler method ?
      this.setContainerDataSource(datahandler.getExperiment(currentValue), currentValue);
    } catch (Exception e) {
      LOGGER.error("failed to load view with paramters: "+ currentValue, e.getStackTrace());
    }
    
  }
}
      
