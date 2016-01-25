package model;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

public class SearchResultsProjectBean implements Comparable<Object>, Serializable {


  /**
   * 
   */
  private static final long serialVersionUID = -5213168232951534848L;
  private String projectID;
  private String description;
  private String queryString;



  public SearchResultsProjectBean(Project p, String query) {
    projectID = p.getIdentifier();
    description = p.getDescription();
    queryString = query;
  }


  public String getProjectID() {
    return projectID;
  }

  public void setProjectID(String projectID) {
    this.projectID = projectID;
  }

  public String getDescription() {
    return description;
  }



  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int compareTo(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }


}
