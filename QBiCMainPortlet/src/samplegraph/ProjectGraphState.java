package samplegraph;

import java.util.List;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class ProjectGraphState extends JavaScriptComponentState {

  private List<SampleSummary> project;

  public List<SampleSummary> getProject() {
    return project;
  }

  public void setProject(List<SampleSummary> project) {
    this.project = project;
  }
}
