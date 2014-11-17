package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import java.util.ArrayList;
import java.util.Set;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends Panel {

  FilterTable table;
  VerticalLayout vert;

  private String id;
  private Button export;

  public ProjectView(FilterTable table, IndexedContainer datasource, String id) {
    vert = new VerticalLayout();
    this.id = id;

    this.table = this.buildFilterTable();
    this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.table.setContainerDataSource(datasource);
    this.tableClickChangeTreeView();
  }


  public ProjectView() {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new IndexedContainer(), "No project selected");
  }

  public void setSizeFull() {
    this.table.setSizeFull();
    vert.setSizeFull();
    super.setSizeFull();
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param id
   */
  public void setContainerDataSource(ProjectInformation projectInformation, String id) {
    this.setStatistics(projectInformation);

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);

    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.vert.addComponent(buttonLayout);

    this.table.setContainerDataSource(projectInformation.experiments);
    this.id = id;

    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    StreamResource sr =
        dh.getTSVStream(dh.containerToString(projectInformation.experiments), this.id);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);

    this.updateCaption();
  }


  private void setStatistics(ProjectInformation projectInformation) {
    vert.removeAllComponents();

    Label vertical_spacer_small = new Label();
    Label vertical_spacer_big1 = new Label();
    Label vertical_spacer_big2 = new Label();


    vertical_spacer_small.setHeight("0.5em");
    vertical_spacer_big1.setHeight("1.5em");
    vertical_spacer_big2.setHeight("1.5em");


    // VerticalLayout contact = new VerticalLayout();
    // contact.addComponent(new Label("QBiC contact:"));
    // contact.addComponent(new Label(projectInformation.contact));

    // TODO email address according to project ?
    Label contact =
        new Label(
            "<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
                + this.id + "\" style=\"color: #0068AA; text-decoration: none\">Support</a>",
            ContentMode.HTML);
    contact.setIcon(FontAwesome.ENVELOPE);


    VerticalLayout statistics = new VerticalLayout();


    Label description = new Label(projectInformation.description);
    // description.setWidth("600px"); should be dictated by the container
    // Label des = new Label("Description: ");
    description.setIcon(FontAwesome.COMMENT);
    description.setCaption("Description");

    HorizontalLayout projDescription = new HorizontalLayout();
    // projDescription.addComponent(des);
    projDescription.addComponent(description);

    statistics.addComponent(projDescription);
    statistics.addComponent(vertical_spacer_big1);


    Label numberExperiments =
        new Label(String.format("Total Experiments: %s", projectInformation.numberOfExperiments));
    numberExperiments.setIcon(FontAwesome.BAR_CHART_O);
    numberExperiments.setCaption("Statistics");
    statistics.addComponent(numberExperiments);
    statistics.addComponent(new Label(String.format("Total Samples: %s",
        projectInformation.numberOfSamples)));
    statistics.addComponent(new Label(String.format("Total Datasets: %s",
        projectInformation.numberOfDatasets)));

    HorizontalLayout temp = new HorizontalLayout();

    temp.addComponent(new Label(String.format("Status: %s", projectInformation.statusMessage)));
    temp.addComponent(projectInformation.progressBar);
    temp.setSpacing(true);

    statistics.addComponent(temp);
    if (projectInformation.numberOfDatasets > 0) {

      String lastSample = "No Sample available";
      if (projectInformation.lastChangedSample != null) {
        lastSample = projectInformation.lastChangedSample.split("/")[2];
      }
      statistics.addComponent(new Label(String.format(
          "Last Change %s",
          String.format("in Sample: %s (%s)", lastSample,
              projectInformation.lastChangedDataset.toString()))));
    }
    HorizontalLayout head = new HorizontalLayout();
    head.addComponent(statistics);
    head.addComponent(contact);
    head.setMargin(true);
    head.setSpacing(true);

    statistics.setSpacing(true);
    statistics.addComponent(vertical_spacer_big2);
    vert.setMargin(true);
    vert.setSpacing(true);
    vert.addComponent(head);
    // Label membersLabel = new Label(getMembersString(projectInformation.members));
    Label membersLabel = new Label("");
    membersLabel.setIcon(FontAwesome.USERS);
    membersLabel.setCaption("Members");

    statistics.addComponent(membersLabel);
    statistics.addComponent(getMembersComponent(projectInformation.members));
    vert.addComponent(this.table);
    this.table.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);
  }

  private void updateCaption() {
    this.setCaption(String.format("Viewing Project %s", id));
  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Experiment"));
  }

  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);


    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    filterTable.setCaption("Registered Experiments");

    return filterTable;
  }

  private String getMembersString(Set<String> members) {
    String concat = new String("");
    if (members != null) {
      Object[] tmp = members.toArray();
      concat = (String) tmp[0];

      if (tmp.length > 1) {
        for (int i = 1; i < tmp.length; ++i) {
          concat = concat + ", " + tmp[i];
        }
      }
    }

    return concat;
  }


  private Component getMembersComponent(Set<String> members) {
    HorizontalLayout membersLayout = new HorizontalLayout();
    if (members != null) {
      // membersLayout.addComponent(new Label("Members:"));
      for (String member : members) {

        // Cool idea, but let's do this when we have more portrait pictures in Liferay

        try {
          // companyId. We have presumable just one portal id, which equals the companyId.
          User user = UserLocalServiceUtil.getUserByScreenName(1, member);
          String fullname = user.getFullName();
          String email = user.getEmailAddress();


          // VaadinSession.getCurrent().getService();
          // ThemeDisplay themedisplay =
          // (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
          // String url = user.getPortraitURL(themedisplay);
          // ExternalResource er = new ExternalResource(url);
          // com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
          // image.setHeight(80, Unit.PIXELS);
          // image.setWidth(65, Unit.PIXELS);
          // membersLayout.addComponent(image);
          String labelString = new String("<a href=\"mailto:" + email + "\">" + fullname + "</a>");
          Label userLabel = new Label(labelString, ContentMode.HTML);
          membersLayout.addComponent(userLabel);

        } catch (com.liferay.portal.NoSuchUserException e) {
          membersLayout.addComponent(new Label(member));
        } catch (PortalException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (SystemException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }


        // membersLayout.addComponent(new Label(member));
      }
      membersLayout.setSpacing(true);
      // membersLayout.setMargin(true);
    }
    return membersLayout;
  }
}
