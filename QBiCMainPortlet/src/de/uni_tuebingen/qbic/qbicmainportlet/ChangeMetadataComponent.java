package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class ChangeMetadataComponent extends CustomComponent {
	private DataHandler datahandler;
	private String resourceUrl;
	private State state;
	private VerticalLayout properties;

	private FormLayout form;
	private FieldGroup fieldGroup;
	VerticalLayout vert;
	String id;

	public ChangeMetadataComponent(DataHandler dh, State state, String resourceurl) {
		this.datahandler = dh;
		this.resourceUrl = resourceurl;
		this.state = state;

		this.setCaption("Metadata");

		this.initUI();
	}

	private void initUI() {
		properties = new VerticalLayout();
		properties.setWidth(100.0f, Unit.PERCENTAGE);
		properties.setMargin(new MarginInfo(true, false, true, true));
		properties.setSpacing(true);
		this.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
		this.setCompositionRoot(properties);
	}

	public void updateUI(final SampleBean currentBean) {
		properties.removeAllComponents();

		final Button saveButton = new Button("Commit Changes", new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				Collection<Field<?>> registeredFields = fieldGroup.getFields();

				for (Field<?> field : registeredFields) {
					properties.put(field.getCaption(), field.getValue());
				}

				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("user", LiferayAndVaadinUtils.getUser().getScreenName());
				parameters.put("identifier", currentBean.getId());
				parameters.put("properties", properties);

				datahandler.getOpenBisClient().triggerIngestionService("update-single-sample-metadata", parameters);
				Notification.show("Changes committed!", Type.TRAY_NOTIFICATION);
			}
		});
		buildFormLayout(currentBean);
		properties.addComponent(new Label(String.format("This view shows the metadata connected to this sample and can be used to change metadata values."), Label.CONTENT_PREFORMATTED));
		properties.addComponent(this.form);
		properties.addComponent(saveButton);
	}
	
	private Map<String, List<String>> getControlledVocabularies(SampleBean currentBean) {
		List<PropertyType> completeProperties =
		        datahandler.getOpenBisClient().listPropertiesForType(datahandler.getOpenBisClient().getSampleTypeByString(currentBean.getType()));
	
	Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();

    for (PropertyType p : completeProperties) {
      if (p instanceof ControlledVocabularyPropertyType) {
        controlledVocabularies.put(p.getCode(), datahandler.getOpenBisClient().listVocabularyTermsForProperty(p));
      }
    }
    return controlledVocabularies;
	}


	private Map<String, String> getProperties(SampleBean currentBean) {
		List<PropertyType> completeProperties =
		        datahandler.getOpenBisClient().listPropertiesForType(datahandler.getOpenBisClient().getSampleTypeByString(currentBean.getType()));
	
    Map<String, String> properties = new HashMap<String, String>();
    // Change that call
    Map<String, String> assignedProperties = currentBean.getProperties();

    for (PropertyType p : completeProperties) {
      if (p.getDataType().toString().equals("XML") ) {
    	continue;  
      }
      else if (assignedProperties.keySet().contains(p.getCode())) {
          properties.put(p.getCode(), assignedProperties.get(p.getCode()));
        } 
      else {
        properties.put(p.getCode(), "");
      }
    }
    return properties;
    
	}

	private void buildFormLayout(SampleBean sample) {
		final FieldGroup fieldGroup = new FieldGroup();
		final FormLayout form2 = new FormLayout();
		
		Map<String, List<String>> controlledVocabularies = getControlledVocabularies(sample);
		Map<String, String> properties = getProperties(sample);

		for (String key : properties.keySet()) {
			if (controlledVocabularies.keySet().contains(key)) {
				ComboBox select = new ComboBox(key);
				fieldGroup.bind(select, key);
				form2.addComponent(select);

				// Add items with given item IDs
				for (String item : controlledVocabularies.get(key)) {
					select.addItem(item);
				}
				select.setValue(properties.get(key));
			} else {
				TextField tf = new TextField(key);
				fieldGroup.bind(tf, key);
				tf.setValue(properties.get(key));
				form2.addComponent(tf);
			}
		}
		this.fieldGroup = fieldGroup;
		this.form = form2;
	}


	/**
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

  public void setContainerDataSource(final ExperimentBean experimentBean, final String id) {
    this.buildLayout();
    this.id = id;
    this.buildFormLayout(experimentBean);

    final Button saveButton = new Button("Commit Changes", new ClickListener() {
      @Override
      public void buttonClick(final ClickEvent event) {
        DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

        HashMap<String, Object> properties = new HashMap<String, Object>();
        Collection<Field<?>> registeredFields = fieldGroup.getFields();

        for (Field<?> field : registeredFields) {
          properties.put(field.getCaption(), field.getValue());
        }

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", experimentBean.getId());
        parameters.put("properties", properties);

        dh.getOpenBisClient().triggerIngestionService("notify-user", parameters);
        Notification.show("Changes committed!", Type.TRAY_NOTIFICATION);
      }
    });

    VerticalLayout statistics = new VerticalLayout();
    statistics.addComponent(new Label(String.format("Experiment ID: %s", id)));
    statistics
        .addComponent(new Label(String.format("Experiment Type: %s", experimentBean.getId())));

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


  private void buildLayout() {
    // Layout
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

    for (String key : experimentBean.getProperties().keySet()) {
      if (experimentBean.getControlledVocabularies().keySet().contains(key)) {
        ComboBox select = new ComboBox(key);
        fieldGroup.bind(select, key);
        form2.addComponent(select);

        // Add items with given item IDs
        for (String item : experimentBean.getControlledVocabularies().get(key)) {
          select.addItem(item);
        }
        select.setValue(experimentBean.getProperties().get(key));
      } else {
        TextField tf = new TextField(key);
        tf.setValue(experimentBean.getProperties().get(key));
        fieldGroup.bind(tf, key);
        form2.addComponent(tf);
      }
    }
    this.fieldGroup = fieldGroup;
    this.form = form2;
  }
	 */
}
