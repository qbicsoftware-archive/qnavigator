package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import logging.Logger;
import main.UploadsPanel;
import model.AttachmentConfig;
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

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class UploadComponent extends CustomComponent {


	private DataHandler datahandler;
	private String resourceUrl;
	private State state;
	private VerticalLayout mainView;

	private FormLayout form;
	private FieldGroup fieldGroup;
	VerticalLayout vert;
	String id;

	public UploadComponent() {
		this.setCaption("Upload Files");
		this.initUI();
	}

	private void initUI() {
		mainView = new VerticalLayout();
		
		mainView.setWidth(100.0f, Unit.PERCENTAGE);
		mainView.setMargin(new MarginInfo(true, false, true, true));
		mainView.setSpacing(true);

		this.setWidth(Page.getCurrent().getBrowserWindowWidth() * 0.8f, Unit.PIXELS);
	}

	public void updateUI(ConfigurationManager manager, String projectCode) {		
		AttachmentConfig attachConfig =
				new AttachmentConfig(Integer.parseInt(manager.getAttachmentMaxSize()),
						manager.getAttachmentURI(), manager.getAttachmentUser(),
						manager.getAttachmenPassword());
		
		mainView = new UploadsPanel(manager.getTmpFolder(), projectCode,
				new ArrayList<String>(Arrays.asList("Project Planning",
						"Results")), LiferayAndVaadinUtils.getUser()
						.getScreenName(), attachConfig);
		
		mainView.setWidth(100.0f, Unit.PERCENTAGE);
		mainView.setMargin(new MarginInfo(true, false, true, true));
		mainView.setSpacing(true);

		this.setCompositionRoot(mainView);

	}
}