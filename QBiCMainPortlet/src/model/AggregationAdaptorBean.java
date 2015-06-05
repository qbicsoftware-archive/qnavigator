package model;

/**
 * Helper class to parse datasets/folders/filestructure from quers aggregation service
 * @author Andreas Friedrich
 *
 */
public class AggregationAdaptorBean {

  private String ds;
  private String path;
  private String name;
  private long size;
  private String parent;
  private String lastmodified;

  public String getDs() {
    return ds;
  }

  public void setDs(String ds) {
    this.ds = ds;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getLastmodified() {
    return lastmodified;
  }

  public void setLastmodified(String lastmodified) {
    this.lastmodified = lastmodified;
  }
  
  @Override
  public String toString() {
    return ds+" "+path;
  }

  public AggregationAdaptorBean(String ds, String path, String name, long ss, String parent,
      String lastmodified) {
    this.ds = ds;
    this.path = path;
    this.name = name;
    this.size = ss;
    this.parent = parent;
    this.lastmodified = lastmodified;
  }
}
