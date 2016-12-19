package helpers;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletUI;

public class ProjectSummaryReadyRunnable implements Runnable {
  private SummaryFetcher fetcher;
  private Window loadingWindow;
  private String project;

  public ProjectSummaryReadyRunnable(SummaryFetcher fetcher, Window window, String projectCode) {
    this.fetcher = fetcher;
    this.loadingWindow = window;
    this.project = projectCode;
  }

  @Override
  public void run() {
    // loading finished, remove loading window if user didn't close it already
    if (loadingWindow.getParent() != null)
      loadingWindow.close();

    // show results in new window
    Window subWindow = new Window(" Summary for project " + project);
    subWindow.setContent(fetcher.getWindowContent());
    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setIcon(FontAwesome.LIST);
    subWindow.setHeight("75%");
    subWindow.setResizable(false);

    QbicmainportletUI ui = (QbicmainportletUI) UI.getCurrent();
    ui.addWindow(subWindow);
  }
}
