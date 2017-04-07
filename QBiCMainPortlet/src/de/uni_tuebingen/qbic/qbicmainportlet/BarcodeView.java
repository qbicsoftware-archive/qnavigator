///*******************************************************************************
// * QBiC Project qNavigator enables users to manage their projects.
// * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *******************************************************************************/
//package de.uni_tuebingen.qbic.qbicmainportlet;
//
//import java.util.List;
//
//import logging.Log4j2Logger;
//import logging.Logger;
//import main.OpenBisClient;
//import model.ExperimentBarcodeSummaryBean;
//
//import org.tepi.filtertable.FilterTable;
//
//import com.vaadin.data.util.IndexedContainer;
//import com.vaadin.navigator.View;
//import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
//import com.vaadin.server.ThemeResource;
//import com.vaadin.ui.MenuBar;
//import com.vaadin.ui.MenuBar.MenuItem;
//import com.vaadin.ui.UI;
//import com.vaadin.ui.VerticalLayout;
//
//public class BarcodeView extends VerticalLayout implements View {
//
//  /**
//   * 
//   */
//  private static final long serialVersionUID = 8921847321758727061L;
//
//  public final static String navigateToLabel = "barcodeview";
//  static Logger LOGGER = new Log4j2Logger(BarcodeView.class);
//  // FilterTable table;
//  // VerticalLayout projectview_content;
//  StandaloneBarcodeUI wizardWrapper;
//  OpenBisClient openbis;
//  private String id;
//
//  // ArrayList<IBarcodeBean> barcodeBeans;
//
//  public BarcodeView(OpenBisClient openbisClient, FilterTable table, IndexedContainer datasource,
//      String id, String scripts, String paths) {
//    this.openbis = openbisClient;
//    wizardWrapper = new StandaloneBarcodeUI(openbisClient, scripts, paths);
//    // projectview_content = new VerticalLayout();
//
//    this.id = id;
//
//    // projectview_content.addComponent(this.table);
//    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
//    // this.addComponent(projectview_content);
//
//    this.addComponent(wizardWrapper.getView());
//  }
//
//  public BarcodeView(OpenBisClient openBisClient, String scripts, String path) {
//    // execute the above constructor with default settings, in order to have the same settings
//    this(openBisClient, new FilterTable(), new IndexedContainer(), "No project selected", scripts,
//        path);
//  }
//
//  public void setSizeFull() {
//    // this.table.setSizeFull();
//    // projectview_content.setSizeFull();
//    wizardWrapper.getView().setSizeFull();
//    super.setSizeFull();
//  }
//
//  public String getNavigatorLabel() {
//    return navigateToLabel;
//  }
//
//  /**
//   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
//   * Project. The id is shown in the caption.
//   * 
//   * @param projectInformation
//   * @param list
//   * @param id
//   */
//  public void setContainerDataSource(List<ExperimentBarcodeSummaryBean> summary, String id) {
//    this.id = id;
//    this.setStatistics();
//  }
//
//  private void setStatistics() {
//    // this.setWidth("100%");
//    // projectview_content.
//    removeAllComponents();
//
//    MenuBar menubar = new MenuBar();
//    menubar.addStyleName("user-menu");
//    menubar.setWidth(100.0f, Unit.PERCENTAGE);
//
//    // projectview_content.
//    addComponent(menubar);
//
//    // A top-level menu item that opens a submenu
//
//    // set to true for the hack below
//    menubar.setHtmlContentAllowed(true);
//
//
//    MenuItem downloadProject = menubar.addItem("Download your data", null, null);
//    downloadProject.setIcon(new ThemeResource("computer_higher.png"));
//    downloadProject.setEnabled(false);
//
//    MenuItem manage = menubar.addItem("Manage your data", null, null);
//    manage.setIcon(new ThemeResource("barcode_higher.png"));
//    manage.setEnabled(false);
//
//
//    int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
//    int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();
//
//    // projectview_content.setWidth("100%");
//    this.setWidth(String.format("%spx", (browserWidth * 0.6)));
//    // this.setHeight(String.format("%spx", (browserHeight * 0.8)));
//
//
//    // table section
//    // VerticalLayout tableSection = new VerticalLayout();
//    // HorizontalLayout tableSectionContent = new HorizontalLayout();
//    //
//    // tableSectionContent.setCaption("Select Experiments for Barcode Creation");
//    // tableSectionContent.setIcon(FontAwesome.FLASK);
//    // // tableSectionContent.addComponent(this.table);
//    //
//    // tableSectionContent.setMargin(true);
//    // tableSection.setMargin(true);
//    // // this.table.setWidth("100%");
//    // tableSection.setWidth("100%");
//    // tableSectionContent.setWidth("100%");
//    //
//    // tableSection.addComponent(tableSectionContent);
//    // projectview_content.addComponent(tableSection);
//
//  }
//
//  // private FilterTable buildFilterTable() {
//  // FilterTable filterTable = new FilterTable();
//  // // filterTable.setSizeFull();
//  //
//  // filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
//  // filterTable.setFilterGenerator(new DatasetViewFilterGenerator());
//  //
//  // filterTable.setFilterBarVisible(true);
//  //
//  //
//  // filterTable.setSelectable(true);
//  // filterTable.setImmediate(true);
//  //
//  // filterTable.setRowHeaderMode(RowHeaderMode.INDEX);
//  //
//  // filterTable.setColumnCollapsingAllowed(true);
//  //
//  // filterTable.setColumnReorderingAllowed(true);
//  //
//  // // filterTable.setCaption("Registered Experiments");
//  // filterTable.setColumnAlignment("Status", com.vaadin.ui.CustomTable.Align.CENTER);
//  //
//  // return filterTable;
//  // }
//
//  @Override
//  public void enter(ViewChangeEvent event) {
//    String currentValue = event.getParameters();
//    System.out.println("currentValue: " + currentValue);
//    // System.out.println("navigateToLabel: " + navigateToLabel);
//    try {
//      System.out.println(openbis.loggedin());
//      LOGGER.debug("code "+ currentValue.split("/")[2]);
//      wizardWrapper.setSummaryBeans(currentValue.split("/")[2]);
//    } catch (Exception e) {
//      LOGGER.error("setting container datasource from bean failed " + e.getMessage(), e.getStackTrace());
//    }
//  }
//}
