package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;


public class SpaceView extends Panel {

  /**
	 * 
	 */
  private static final long serialVersionUID = 2699910993266409192L;

  FilterTable table;
  VerticalLayout vert;

  private String id;

  public SpaceView(FilterTable table, SpaceInformation datasource, String id) {
    vert = new VerticalLayout();

    this.table = buildFilterTable();
    this.table.setSizeFull();

    vert.addComponent(this.table);
    vert.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(vert);

    this.setContainerDataSource(datasource, id);
    this.tableClickChangeTreeView();
  }


  public SpaceView() {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new SpaceInformation(), "No space selected");
  }

  public void setSizeFull() {
    vert.setSizeFull();
    super.setSizeFull();
    this.table.setSizeFull();
    vert.setSpacing(true);
    vert.setMargin(true);
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis Space.
   * The id is shown in the caption.
   * 
   * @param spaceViewIndexedContainer
   * @param id
   */
  public void setContainerDataSource(SpaceInformation spaceViewIndexedContainer, String id) {

    // this.table.setContainerDataSource(spaceViewIndexedContainer);
    this.id = id;
    this.updateCaption();
    this.setStatistics(spaceViewIndexedContainer);
    this.table.setContainerDataSource(spaceViewIndexedContainer.projects);
  }


  private void setStatistics(SpaceInformation spaceViewIndexedContainer) {
    vert.removeAllComponents();
    vert.addComponent(new Label(String.format("Number of Projects: %s",
        spaceViewIndexedContainer.numberOfProjects)));
    vert.addComponent(new Label(String.format("Number of Experiments: %s",
        spaceViewIndexedContainer.numberOfExperiments)));
    vert.addComponent(new Label(String.format("Number of Samples: %s",
        spaceViewIndexedContainer.numberOfSamples)));
    vert.addComponent(new Label(String.format("Number of Datasets: %s",
        spaceViewIndexedContainer.numberOfDatasets)));
    HorizontalLayout members = new HorizontalLayout();

    if (spaceViewIndexedContainer.members != null) {
      members.addComponent(new Label("Members:"));
      for (String member : spaceViewIndexedContainer.members) {

        try {
          // companyId. We have presumable just one portal id, which equals the companyId.
          User user = UserLocalServiceUtil.getUserByScreenName(1, member);
          VaadinSession.getCurrent().getService();
          ThemeDisplay themedisplay =
              (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
          String url = user.getPortraitURL(themedisplay);
          ExternalResource er = new ExternalResource(url);
          com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
          image.setHeight(80, Unit.PIXELS);
          image.setWidth(65, Unit.PIXELS);
          members.addComponent(image);

        } catch (com.liferay.portal.NoSuchUserException e) {
          members.addComponent(new Label(member));
        } catch (PortalException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (SystemException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      members.setSpacing(true);
      members.setMargin(true);
      vert.addComponent(members);
    }
    if (spaceViewIndexedContainer.numberOfDatasets > 0) {
      vert.addComponent(new Label(String.format("Last Change: %s", String.format(
          "In Sample: %s. Date: %s", spaceViewIndexedContainer.lastChangedSample.split("/")[2],
          spaceViewIndexedContainer.lastChangedDataset.toString()))));
    }
    vert.addComponent(this.table);
  }


  private void updateCaption() {
    this.setCaption(String.format("Statistics of Space %s", id));

  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, "Project"));
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

    filterTable.setCaption("Registered Projects");

    return filterTable;
  }

}
