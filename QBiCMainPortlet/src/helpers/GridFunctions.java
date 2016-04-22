package helpers;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.TextField;

public class GridFunctions {
  // Set up a filter for all columns

  public static void addColumnFilters(Grid grid, final GeneratedPropertyContainer gpcBio) {
    HeaderRow filterRow = grid.appendHeaderRow();

    for (final Object pid : grid.getContainerDataSource().getContainerPropertyIds()) {
      HeaderCell cell = filterRow.getCell(pid);

      // Have an input field to use for filter
      final TextField filterField = new TextField();

      // filterField.setColumns(8);

      // Update filter When the filter input is changed
      filterField.addTextChangeListener(new TextChangeListener() {

        /**
         * 
         */
        private static final long serialVersionUID = 7670817216478146116L;
        Filter currentFilter = null;

        @Override
        public void textChange(TextChangeEvent event) {
          if (currentFilter != null) {
            gpcBio.removeContainerFilter(currentFilter);
            currentFilter = null;
          }

          if (!event.getText().isEmpty()) {
            currentFilter = new SimpleStringFilter(pid, event.getText(), true, false);
            gpcBio.addContainerFilter(currentFilter);
          }
          // (Re)create the filter if necessary
          // if (!filterField.getValue().equals("")) {
          // currentFilter = new SimpleStringFilter(pid, filterField.getValue(), true, false);
          // gpcBio.addContainerFilter(currentFilter);
          // }

        }
      });
      cell.setComponent(filterField);
    }
  }
}
