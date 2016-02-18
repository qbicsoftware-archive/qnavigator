package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import logging.Log4j2Logger;
import logging.Logger;
import model.ProjectBean;
import properties.Factor;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class ChangeProjectMetadataComponent extends CustomComponent {
  /**
   * TODO generic component for experiments and samples
   */
  private static final long serialVersionUID = -5318223225284123020L;

  private static Logger LOGGER = new Log4j2Logger(ChangeProjectMetadataComponent.class);

  private DataHandler datahandler;
  private String resourceUrl;
  private State state;
  private VerticalLayout properties;

  private FormLayout form;
  private FieldGroup fieldGroup;
  VerticalLayout vert;

  private String currentDescription;

  public ChangeProjectMetadataComponent(DataHandler dh, State state, String resourceurl) {
    this.datahandler = dh;
    this.resourceUrl = resourceurl;
    this.state = state;

    // this.setCaption("Metadata");

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

  public void updateUI(final ProjectBean projectBean) {
    properties.removeAllComponents();
    Button saveButton = new Button("Submit Changes");
    saveButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);

    currentDescription = projectBean.getDescription();

    saveButton.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(final ClickEvent event) {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        Collection<Field<?>> registeredFields = fieldGroup.getFields();

        List<Factor> factors = new ArrayList<Factor>();

        for (Field<?> field : registeredFields) {
          parameters.put("description", field.getValue());
        }

        parameters.put("identifier", projectBean.getId());
        parameters.put("user", LiferayAndVaadinUtils.getUser().getScreenName());
        datahandler.getOpenBisClient().triggerIngestionService("update-project-metadata",
            parameters);
        Utils.Notification(
            "Project details changed succesfully",
            String.format("Details of project %s have been commited successfully.",
                projectBean.getId()), "success");
      }
    });

    buildFormLayout();
    properties
        .addComponent(new Label(
            String
                .format(
                    "This view shows project details and can be used to change the corresponding values. \nIdentifier: %s",
                    projectBean.getId()), Label.CONTENT_PREFORMATTED));

    properties.addComponent(this.form);
    properties.addComponent(saveButton);
  }


  private void buildFormLayout() {
    final FieldGroup fieldGroup = new FieldGroup();
    final FormLayout form2 = new FormLayout();

    TextArea tf = new TextArea("Description");
    fieldGroup.bind(tf, "Description");
    tf.setCaption("Description");
    tf.setDescription("Description of this project.");
    tf.setValue(currentDescription);
    form2.addComponent(tf);

    this.fieldGroup = fieldGroup;
    this.form = form2;
  }

}
