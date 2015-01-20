package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.Set;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectView extends Panel implements View {

  static String navigateToLabel = "project";
  
  FilterTable table;
  VerticalLayout projectview_content;

  private String id;
  private Button export;

  public ProjectView(FilterTable table, IndexedContainer datasource, String id) {
    projectview_content = new VerticalLayout();
        
    this.id = id;

    this.table = this.buildFilterTable();
    //this.table.setSizeFull();

    projectview_content.addComponent(this.table);
    projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    this.setContent(projectview_content);

    this.table.setContainerDataSource(datasource);
    this.tableClickChangeTreeView();
   
  }


  public ProjectView() {
    // execute the above constructor with default settings, in order to have the same settings
    this(new FilterTable(), new IndexedContainer(), "No project selected");
  }

  public void setSizeFull() {
    this.table.setSizeFull();
    projectview_content.setSizeFull();
    super.setSizeFull();
  }
  public String getNavigatorLabel(){
    return navigateToLabel;
  }
  
  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Project. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param id
   */
  public void setContainerDataSource(ProjectInformation projectInformation, String id) {
    this.id = id;
    this.setStatistics(projectInformation);

    
    VerticalLayout buttonLayoutSection = new VerticalLayout();
    buttonLayoutSection.setMargin(true);
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayout.setSpacing(true);
    
    buttonLayoutSection.addComponent(buttonLayout);

    this.export = new Button("Export as TSV");
    buttonLayout.addComponent(this.export);

    this.projectview_content.addComponent(buttonLayoutSection);

    this.table.setContainerDataSource(projectInformation.experiments);
    
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    StreamResource sr =
        dh.getTSVStream(dh.containerToString(projectInformation.experiments), this.id);
    FileDownloader fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);

    this.updateCaption();
  }


  private void setStatistics(ProjectInformation projectInformation) {
    //this.setWidth("100%");
    projectview_content.removeAllComponents();
    
    //TODO Toolbar in there or not?
    //ToolBar toolbar = new ToolBar(ToolBar.View.Space);
    //projectview_content.addComponent(toolbar);
    
    MenuBar menubar = new MenuBar();
    menubar.addStyleName("qbicmainportlet");
    menubar.setWidth(100.0f, Unit.PERCENTAGE);
    
    projectview_content.addComponent(menubar);
    
    // A top-level menu item that opens a submenu
    MenuItem drinks = menubar.addItem("Download your Data", null, null);
    drinks.setIcon(new ThemeResource("computer_test.png"));
    
    // Submenu item with a sub-submenu
    MenuItem hots = drinks.addItem("test1", null, null);
    
    hots.addItem("Tea",
        null, null);
    hots.addItem("Coffee",
        null, null);

    MenuItem manage = menubar.addItem("Manage your Data", null, null);
    manage.setIcon(new ThemeResource("barcode_test.png"));
   
    // Another submenu item with a sub-submenu
    MenuItem colds = manage.addItem("test2", null, null);

    colds.addItem("Milk",      null, null);
    colds.addItem("Weissbier", null, null);

    // Another top-level item
    MenuItem snacks = menubar.addItem("Run workflows", null, null);
    snacks.setIcon(new ThemeResource("graph_test.png"));

    snacks.addItem("Weisswurst", null, null);
    snacks.addItem("Bratwurst",  null, null);
    snacks.addItem("Currywurst", null, null);
            
    // Yet another top-level item
    MenuItem servs = menubar.addItem("Analyze your data", null, null);
    servs.setIcon(new ThemeResource("dna_test.png"));
    servs.addItem("Car Service", null, null);
    
    
    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();

    projectview_content.setWidth("100%");
    this.setWidth(String.format("%spx", (browserWidth * 0.6)));
    this.setHeight(String.format("%spx", (browserHeight * 0.8)));

    
    // Project description
    VerticalLayout projDescription = new VerticalLayout();
    VerticalLayout projDescriptionContent = new VerticalLayout();
    
    Label descContent = new Label("none");
    if (!("".equals(projectInformation.description))) {
      descContent = new Label(projectInformation.description);
    }

    Label contact =
        new Label(
            "<a href=\"mailto:info@qbic.uni-tuebingen.de?subject=Question%20concerning%20project%20"
                + this.id
                + "\" style=\"color: #0068AA; text-decoration: none\">Send question regarding project "
                + this.id + "</a>", ContentMode.HTML);
    // contact.setIcon(FontAwesome.ENVELOPE);
    
    projDescription.setWidth("100%");
    projDescriptionContent.setWidth("100%");
    descContent.setWidth("100%");
    contact.setWidth("100%");
    
    projDescriptionContent.addComponent(descContent);
    projDescriptionContent.addComponent(contact);
    projDescriptionContent.setMargin(true);
    projDescriptionContent.setCaption("Description");
    projDescriptionContent.setIcon(FontAwesome.FILE_TEXT_O);

    projDescription.addComponent(projDescriptionContent);
    projDescription.setMargin(true);
    projectview_content.addComponent(projDescription);

    // statistics.addComponent(projDescription);
    // statistics.addComponent(vertical_spacer_big1);


    // VerticalLayout contact = new VerticalLayout();
    // contact.addComponent(new Label("QBiC contact:"));
    // contact.addComponent(new Label(projectInformation.contact));

    // TODO email address according to project ?

    // members section

    VerticalLayout members_section = new VerticalLayout();
    Component membersContent = getMembersComponent(projectInformation.members);

    membersContent.setIcon(FontAwesome.USERS);
    membersContent.setCaption("Members");
    members_section.addComponent(membersContent);
    members_section.setMargin(true);

    projectview_content.addComponent(members_section);



    // statistics section
    VerticalLayout statistics = new VerticalLayout();

    HorizontalLayout statContent = new HorizontalLayout();
    statContent.setCaption("Statistics");
    statContent.setIcon(FontAwesome.BAR_CHART_O);
    statContent.addComponent(new Label(String.format("%s experiment(s),",
        projectInformation.numberOfExperiments)));

    statContent.addComponent(new Label(String.format("%s sample(s),",
        projectInformation.numberOfSamples)));

    statContent.addComponent(new Label(String.format("%s dataset(s).",
        projectInformation.numberOfDatasets)));

    statContent.setMargin(true);
    statContent.setSpacing(true);

    if (projectInformation.numberOfDatasets > 0) {

      String lastSample = "No samples available";
      if (projectInformation.lastChangedSample != null) {
        lastSample = projectInformation.lastChangedSample.split("/")[2];
      }
      statContent.addComponent(new Label(String.format(
          "Last change %s",
          String.format("occurred in sample %s (%s)", lastSample,
              projectInformation.lastChangedDataset.toString()))));
    }


    statistics.addComponent(statContent);
    statistics.setMargin(true);
    projectview_content.addComponent(statistics);


    // status bar section

    VerticalLayout status = new VerticalLayout();
    HorizontalLayout statusContent = new HorizontalLayout();
    statusContent.setCaption("Status");
    statusContent.setIcon(FontAwesome.CLOCK_O);
    
    statusContent.addComponent(projectInformation.progressBar);
    statusContent.addComponent(new Label(projectInformation.statusMessage));
    statusContent.setSpacing(true);
    statusContent.setMargin(true);
    
    status.addComponent(statusContent);
    status.setMargin(true);

    projectview_content.addComponent(status);



    // table section
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    
    tableSectionContent.setCaption("Registered Experiments");
    tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);
    
    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");
    
    tableSection.addComponent(tableSectionContent);
    projectview_content.addComponent(tableSection);


    // HorizontalLayout head = new HorizontalLayout();
    // head.addComponent(statistics);
    // head.addComponent(contact);
    // head.setMargin(true);
    // head.setSpacing(true);

    // statistics.setSpacing(true);

    //
    // statistics.addComponent(vertical_spacer_big2);
    // projectview_content.setMargin(true);
    // projectview_content.setSpacing(true);
    // projectview_content.addComponent(head);
    // Label membersLabel = new Label(getMembersString(projectInformation.members));


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
    //filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);


    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    //filterTable.setCaption("Registered Experiments");
    filterTable.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);

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
          String labelString =
              new String("<a href=\"mailto:" + email
                  + "\" style=\"color: #0068AA; text-decoration: none\">" + fullname + "</a>");
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
      membersLayout.setMargin(true);
    }
    return membersLayout;
  }


  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    System.out.println("currentValue: " + currentValue);
    System.out.println("navigateToLabel: " + navigateToLabel);
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    try {
      Project project = dh.openBisClient.getProjectByCode(currentValue);
      String projectIdentifier = project.getIdentifier();

      this.setContainerDataSource(dh.getProjectInformation(projectIdentifier), currentValue);
    } catch (Exception e) {
      System.out.println("Exception in LevelView.enter. mainComponent is ProjectView");
      // e.printStackTrace();
    }
    
    
    
    
  }
}
