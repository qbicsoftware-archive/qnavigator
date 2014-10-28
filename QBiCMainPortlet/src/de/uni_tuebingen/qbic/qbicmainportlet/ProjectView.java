package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;
import java.util.Set;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends Panel {

  FilterTable table;
  VerticalLayout vert;

  private String id;

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
    this.table.setContainerDataSource(projectInformation.experiments);
    this.id = id;
    this.updateCaption();
  }
  
  
  private void setStatistics(ProjectInformation projectInformation) {
    vert.removeAllComponents();

    VerticalLayout contact = new VerticalLayout();
    contact.addComponent(new Label("QBiC contact:"));
    contact.addComponent(new Label(projectInformation.contact));

    VerticalLayout statistics = new VerticalLayout();


    Label description = new Label(projectInformation.description);
    description.setWidth("400px");
    Label des = new Label("Description: ");
    HorizontalLayout projDescription = new HorizontalLayout();
    projDescription.addComponent(des);
    projDescription.addComponent(description);
    statistics.addComponent(projDescription);
    statistics.addComponent(new Label(String.format("Number of Experiments: %s",
        projectInformation.numberOfExperiments)));
    statistics.addComponent(new Label(String.format("Number of Samples: %s",
        projectInformation.numberOfSamples)));
    statistics.addComponent(new Label(String.format("Number of Datasets: %s",
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
      statistics
          .addComponent(new Label(String.format("Last Change: %s", String.format(
              "In Sample: %s. Date: %s", lastSample,
              projectInformation.lastChangedDataset.toString()))));
    }
    HorizontalLayout head = new HorizontalLayout();
    head.addComponent(statistics);
    head.addComponent(contact);
    head.setMargin(true);
    head.setSpacing(true);
    vert.addComponent(head);
    vert.addComponent(getMemebersComponent(projectInformation.members));
    vert.addComponent(this.table);
    this.table.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);
  }
  private void updateCaption() {
    this.setCaption(String.format("Statistics of Project: %s", id));
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

	private Component getMemebersComponent(Set<String> members) {
	  HorizontalLayout membersLayout = new HorizontalLayout();  
	  if(members != null){
	      membersLayout.addComponent(new Label("Members:"));
	      for(String member : members){

	        try {
	          //companyId. We have presumable just one portal id, which equals the companyId.
	          User user = UserLocalServiceUtil.getUserByScreenName(1, member);
	          VaadinSession.getCurrent().getService();
	          ThemeDisplay themedisplay = (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
	          String url = user.getPortraitURL(themedisplay);
	          ExternalResource er = new ExternalResource(url);
	          com.vaadin.ui.Image image  = new com.vaadin.ui.Image(user.getFullName(),er);
	          image.setHeight(80, Unit.PIXELS);
	          image.setWidth(65, Unit.PIXELS);
	          membersLayout.addComponent(image);

	        } catch(com.liferay.portal.NoSuchUserException e){
	          membersLayout.addComponent(new Label(member));
	        }
	        catch (PortalException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        } catch (SystemException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	      }
	      membersLayout.setSpacing(true);
	      membersLayout.setMargin(true);
	  }
	  return membersLayout;
  }
}
