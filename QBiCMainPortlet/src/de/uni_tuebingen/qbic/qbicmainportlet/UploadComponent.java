package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;
import main.OpenBisClient;
import main.UploadsPanel;
import model.AttachmentConfig;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class UploadComponent extends CustomComponent {

  private VerticalLayout mainView;

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

  public void updateUI(ConfigurationManager manager, String projectCode, String space, OpenBisClient openBisClient) {
    AttachmentConfig attachConfig =
        new AttachmentConfig(Integer.parseInt(manager.getAttachmentMaxSize()),
            manager.getAttachmentURI(), manager.getAttachmentUser(), manager.getAttachmenPassword());

    mainView =
        new UploadsPanel(manager.getTmpFolder(), space, projectCode, new ArrayList<String>(Arrays.asList(
            "Project Planning", "Results")), LiferayAndVaadinUtils.getUser().getScreenName(),
            attachConfig, openBisClient);
    
    mainView.setWidth(100.0f, Unit.PERCENTAGE);
    mainView.setMargin(new MarginInfo(true, false, true, true));
    mainView.setSpacing(true);

    this.setCompositionRoot(mainView);

  }
}
