/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects. Copyright (C) "2016”
 * Christopher Mohr, David Wojnar, Andreas Friedrich
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class ToolBar extends HorizontalLayout {



  /**
   * 
   */
  private static final long serialVersionUID = -3673630619072584036L;

  // private SearchBarView searchBarView;
  private SearchEngineView searchEngineView;
  private MenuItem downloadwhole;
  private MenuItem datasetoverview;
  private MenuItem manage;
  private MenuItem createBarcodes;
  private MenuItem workflows;
  private String resourceUrl;
  private State state;

  private MenuItem download;

  public ToolBar() {
    resourceUrl = "javascript";
  }

  // public ToolBar(String resourceUrl, State state, SearchBarView searchBarView) {
  // this.searchBarView = searchBarView;
  // this.resourceUrl = resourceUrl;
  // this.state = state;
  // }

  public ToolBar(String resourceUrl, State state, SearchEngineView searchEngineView) {
    this.searchEngineView = searchEngineView;
    this.resourceUrl = resourceUrl;
    this.state = state;
  }


  void setCompleteDownloadResourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }

  public void setState(State state) {
    this.state = state;
  }

  void setDownload(boolean enabled) {
    download.setEnabled(enabled);
    downloadwhole.setEnabled(enabled);
    datasetoverview.setEnabled(enabled);
  }

  void visibleDownload(boolean visible) {
    download.setVisible(visible);
    downloadwhole.setVisible(visible);
    datasetoverview.setVisible(visible);
  }

  void setBarcode(boolean enabled) {
    manage.setEnabled(enabled);
    createBarcodes.setEnabled(enabled);
  }

  void visibleBarcode(boolean visible) {
    manage.setVisible(visible);
    createBarcodes.setVisible(visible);
  }

  void visibleWorkflow(boolean visible) {
    workflows.setVisible(visible);
  }

  void setWorkflow(boolean enabled) {
    workflows.setEnabled(enabled);
  }


  void init() {
    // setWidth(100.0f, Unit.PERCENTAGE);
    // addStyleName("user-menu");
    // setWidth((UI.getCurrent().getPage().getBrowserWindowWidth() * 0.6f), Unit.PIXELS);

    MenuBar menuBar = new MenuBar();
    menuBar.addStyleName("user-menu");

    menuBar.setHtmlContentAllowed(true);
    download = menuBar.addItem("Download your data", null, null);
    download.setEnabled(false);

    download.setIcon(new ThemeResource("computer_higher.png"));
    download.addSeparator();
    this.downloadwhole =
        download
            .addItem(
                "<a href=\""
                    + resourceUrl
                    + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download everything belonging to this entity.</a>",
                null);
    this.downloadwhole.setEnabled(false);
    // Open DatasetView
    this.datasetoverview = download.addItem("Dataset Overview", null);
    this.datasetoverview.setEnabled(false);
    manage = menuBar.addItem("Manage your data", null, null);
    manage.setIcon(new ThemeResource("barcode_higher.png"));
    manage.setEnabled(false);
    // Another submenu item with a sub-submenu
    this.createBarcodes = manage.addItem("Create Barcodes", null, null);
    createBarcodes.setEnabled(false);

    workflows = menuBar.addItem("Run workflows", null, null);
    workflows.setIcon(new ThemeResource("dna_higher.png"));
    workflows.setEnabled(false);

    addComponent(menuBar);
    // menuBar.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth() * 0.3f), Unit.PIXELS);
    // searchEngineView.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth() * 0.2f),
    // Unit.PIXELS);
    // searchEngineView.setWidth("100%");
    addComponent(searchEngineView);
    setComponentAlignment(searchEngineView, Alignment.TOP_RIGHT);
    // setExpandRatio(menuBar, 0.7f);
    // setExpandRatio(searchEngineView, 0.3f);
  }

  void update(final String type, final String id) {

    downloadwhole
        .setText("<a href=\""
            + resourceUrl
            + "\" target=\"_blank\" style=\"text-decoration: none ; color:#2c2f34\">Download complete project</a>");

    if (state != null) {
      datasetoverview.setCommand(new MenuBar.Command() {

        @Override
        public void menuSelected(MenuItem selectedItem) {
          ArrayList<String> message = new ArrayList<String>();
          message.add("clicked");
          StringBuilder sb = new StringBuilder("type=");
          sb.append(type);
          sb.append("&");
          sb.append("id=");
          sb.append(id);
          message.add(sb.toString());
          message.add(DatasetView.navigateToLabel);
          state.notifyObservers(message);
        }
      });
      createBarcodes.setCommand(new MenuBar.Command() {
        public void menuSelected(MenuItem selectedItem) {
          ArrayList<String> message = new ArrayList<String>();
          message.add("clicked");
          message.add(id);
          message.add(BarcodeView.navigateToLabel);
          state.notifyObservers(message);
        }
      });

      if (workflows.isEnabled() && workflows.isVisible()) {
        workflows.setCommand(new MenuBar.Command() {
          public void menuSelected(MenuItem selectedItem) {
            ArrayList<String> message = new ArrayList<String>();
            message.add("clicked");
            StringBuilder sb = new StringBuilder("type=");
            sb.append(type);
            sb.append("&");
            sb.append("id=");
            sb.append(id);
            message.add(sb.toString());
            message.add(WorkflowView.navigateToLabel);
            state.notifyObservers(message);
          }
        });
      }

    }

  }

}
