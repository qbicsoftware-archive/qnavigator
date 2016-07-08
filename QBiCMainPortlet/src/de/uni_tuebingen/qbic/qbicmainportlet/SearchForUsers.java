/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import org.tepi.filtertable.FilterTable;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class SearchForUsers extends Panel implements ClickListener {

  private VerticalLayout vert;
  private FilterTable table;
  private Button button;

  public SearchForUsers() {
    this.vert = new VerticalLayout();


    this.table = buildFilterTable();
    this.table.setSelectable(true);
    // this.table.setSizeFull();

    this.button = new Button("Show Users");
    this.button.addClickListener(this);
    this.button.setIcon(FontAwesome.SEARCH);
    this.button.setDescription("Click button to show user information");


    VerticalLayout searchSection = new VerticalLayout();
    HorizontalLayout searchContent = new HorizontalLayout();
    searchSection.setMargin(true);
    searchContent.setMargin(true);

    searchContent.addComponent(this.button);
    searchSection.addComponent(searchContent);

    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    tableSectionContent.setIcon(FontAwesome.USERS);
    tableSectionContent.setCaption("Connected Users");
    tableSectionContent.addComponent(this.table);
    tableSectionContent.setMargin(true);
    tableSection.setMargin(true);
    tableSection.addComponent(tableSectionContent);

    this.vert.addComponent(searchSection);
    this.vert.addComponent(tableSection);
    vert.setSpacing(true);
    this.setContent(vert);
  }

  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    // filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);
    filterTable.setMultiSelect(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    // filterTable.setContainerDataSource(this.datasets);

    // filterTable.setCaption("Registered Datasets");

    filterTable.setVisible(false);
    return filterTable;
  }

  @Override
  public void buttonClick(ClickEvent event) {
    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

    /*
     * if (this.searchField.getValue().equals("")) { IndexedContainer personContainer = new
     * IndexedContainer(); personContainer.addContainerProperty("Title", String.class, null);
     * personContainer.addContainerProperty("First Name", String.class, null);
     * personContainer.addContainerProperty("Last Name", String.class, null);
     * personContainer.addContainerProperty("Position", String.class, null);
     * personContainer.addContainerProperty("E-Mail", String.class, null);
     * personContainer.addContainerProperty("Phone", Integer.class, null);
     * personContainer.addContainerProperty("Project", String.class, null);
     * 
     * 
     * Iterator it = dh.connectedPersons.entrySet().iterator(); while (it.hasNext()) { Map.Entry
     * pairs = (Map.Entry) it.next();
     * 
     * IndexedContainer ic = (IndexedContainer) pairs.getValue(); for (Object itemID :
     * ic.getItemIds()) { personContainer.addItem(ic.getItem(itemID)); } }
     * 
     * this.table.setContainerDataSource(personContainer); } else {
     * this.table.setContainerDataSource(dh.connectedPersons.get(this.searchField.getValue())); }
     */
    this.table.setContainerDataSource(dh.connectedPersons);
    this.table.setVisible(true);
  }
}
