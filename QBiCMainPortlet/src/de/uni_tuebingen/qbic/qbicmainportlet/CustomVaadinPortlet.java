package de.uni_tuebingen.qbic.qbicmainportlet;


import helpers.OpenBisFunctions;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletResponse;

import logging.Log4j2Logger;
import logging.Logger;
import model.DatasetBean;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;

import main.OpenBisClient;

/**
 * 
 * copied from:
 * https://github.com/jamesfalkner/vaadin-liferay-beacon-demo/blob/master/src/main/java/
 * com/liferay/mavenizedbeacons/CustomVaadinPortlet.java This custom Vaadin portlet allows for
 * serving Vaadin resources like theme or widgetset from its web context (instead of from ROOT).
 * Usually it doesn't need any changes.
 * 
 */
public class CustomVaadinPortlet extends VaadinPortlet {
  private static final long serialVersionUID = -13615405654173335L;

  private class CustomVaadinPortletService extends VaadinPortletService {
    /**
     *
     */
    private static final long serialVersionUID = -6282242585931296999L;



    public CustomVaadinPortletService(final VaadinPortlet portlet,
        final DeploymentConfiguration config) throws ServiceException {
      super(portlet, config);
    }


    /**
     * This method is used to determine the uri for Vaadin resources like theme or widgetset. It's
     * overriden to point to this web application context, instead of ROOT context
     */
    @Override
    public String getStaticFileLocation(final VaadinRequest request) {
      return super.getStaticFileLocation(request);
      // self contained approach:
      // return request.getContextPath();
    }
  }

  private static Logger LOGGER = new Log4j2Logger(CustomVaadinPortletService.class);
  public static final String RESOURCE_ID = "mainPortletResourceId";
  public static final String RESOURCE_ATTRIBUTE = "resURL";

  @Override
  protected void doDispatch(javax.portlet.RenderRequest request,
      javax.portlet.RenderResponse response) throws javax.portlet.PortletException,
      java.io.IOException {
    if (request.getPortletSession().getAttribute(RESOURCE_ATTRIBUTE,
        PortletSession.APPLICATION_SCOPE) == null) {
      ResourceURL resURL = response.createResourceURL();
      // get Resource ID ?
      resURL.setResourceID(RESOURCE_ID);
      request.getPortletSession().setAttribute(RESOURCE_ATTRIBUTE, resURL.toString(),
          PortletSession.APPLICATION_SCOPE);
    }
    super.doDispatch(request, response);
  }

  @Override
  public void serveResource(javax.portlet.ResourceRequest request,
      javax.portlet.ResourceResponse response) throws PortletException, IOException {
    // System.out.println(request.getResourceID());
    // System.out.println(RESOURCE_ID);
    if (request.getResourceID().equals("openbisUnreachable")) {
      response.setContentType("text/plain");
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
          String.valueOf(HttpServletResponse.SC_GATEWAY_TIMEOUT));
      response.getWriter().append(
          "Internal Error.\nRetry later or contact your project manager.\n" + "Time: "
              + (new Date()).toString());
    } else if (request.getResourceID().equals(RESOURCE_ID)) {
      serveDownloadResource(request, response);
    } else {
      super.serveResource(request, response);
    }
  }

  public void serveDownloadResource(javax.portlet.ResourceRequest request,
      javax.portlet.ResourceResponse response) throws PortletException, IOException {
    DataHandler dataHandler =
        (DataHandler) request.getPortletSession().getAttribute("datahandler",
            PortletSession.APPLICATION_SCOPE);
    Object bean =
        (Object) request.getPortletSession().getAttribute("qbic_download",
            PortletSession.APPLICATION_SCOPE);
    if (bean instanceof ProjectBean) {
      serveProject((ProjectBean) bean, new TarWriter(), response, dataHandler.openBisClient);
    } else if (bean instanceof ExperimentBean) {
      serveExperiment((ExperimentBean) bean, new TarWriter(), response, dataHandler.openBisClient);
    } else if (bean instanceof SampleBean) {
      serveSample((SampleBean) bean, new TarWriter(), response, dataHandler.openBisClient);
    } else {
      response.setContentType("text/javascript");
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
          String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
      response.getWriter().append("Please select at least one dataset for download");
      return;
    }
  }

  /**
   * 
   * Note: the provided stream will be closed.
   * 
   * @param bean bean containing datasets.
   * @param writer writes
   * @param response writer writes to its outputstream
   * @param openbisClient
   */
  private void serveProject(ProjectBean bean, TarWriter writer, ResourceResponse response,
      OpenBisClient openbisClient) {
    String filename = bean.getCode() + ".tar";

    response.setContentType(writer.getContentType());
    StringBuilder sb = new StringBuilder("attachement; filename=\"");
    sb.append(filename);
    sb.append("\"");
    response.setProperty("Content-Disposition", sb.toString());
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries = convertBeanToEntries(bean);

    long tarFileLength = writer.computeTarLength2(entries);
    // response.setContentLength((int) tarFileLength);
    // For some reason setContentLength does not work
    response.setProperty("Content-Length", String.valueOf(tarFileLength));
    try {
      writer.setOutputStream(response.getPortletOutputStream());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
    while (it.hasNext()) {
      Entry<String, SimpleEntry<String, Long>> entry = it.next();
      String entryKey = entry.getKey().replaceFirst(entry.getValue().getKey() + "/", "");
      String[] splittedFilePath = entryKey.split("/");

      if ((splittedFilePath.length == 0) || (splittedFilePath == null)) {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(),
            openbisClient.getDatasetStream(entry.getValue().getKey()), entry.getValue().getValue());
      } else {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(), openbisClient.getDatasetStream(
            entry.getValue().getKey(), entryKey), entry.getValue().getValue());
      }
    }
    writer.closeStream();
  }

  /**
   * 
   * Note: the provided stream will be closed.
   * 
   * @param bean bean containing datasets.
   * @param writer writes
   * @param response writer writes to its outputstream
   * @param openbisClient
   */
  private void serveExperiment(ExperimentBean bean, TarWriter writer, ResourceResponse response,
      OpenBisClient openbisClient) {
    String filename = bean.getCode() + ".tar";

    response.setContentType(writer.getContentType());
    StringBuilder sb = new StringBuilder("attachement; filename=\"");
    sb.append(filename);
    sb.append("\"");
    response.setProperty("Content-Disposition", sb.toString());
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries = convertBeanToEntries(bean);

    long tarFileLength = writer.computeTarLength2(entries);
    // response.setContentLength((int) tarFileLength);
    // For some reason setContentLength does not work
    response.setProperty("Content-Length", String.valueOf(tarFileLength));
    try {
      writer.setOutputStream(response.getPortletOutputStream());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
    while (it.hasNext()) {
      Entry<String, SimpleEntry<String, Long>> entry = it.next();
      String entryKey = entry.getKey().replaceFirst(entry.getValue().getKey() + "/", "");
      String[] splittedFilePath = entryKey.split("/");

      if ((splittedFilePath.length == 0) || (splittedFilePath == null)) {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(),
            openbisClient.getDatasetStream(entry.getValue().getKey()), entry.getValue().getValue());
      } else {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(), openbisClient.getDatasetStream(
            entry.getValue().getKey(), entryKey), entry.getValue().getValue());
      }
    }
    writer.closeStream();
  }



  /**
   * 
   * Note: the provided stream will be closed.
   * 
   * @param bean bean containing datasets.
   * @param writer writes
   * @param response writer writes to its outputstream
   * @param openbisClient
   */
  private void serveSample(SampleBean bean, TarWriter writer, ResourceResponse response,
      OpenBisClient openbisClient) {
    String filename = bean.getCode() + ".tar";

    response.setContentType(writer.getContentType());
    StringBuilder sb = new StringBuilder("attachement; filename=\"");
    sb.append(filename);
    sb.append("\"");
    response.setProperty("Content-Disposition", sb.toString());
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries = convertBeanToEntries(bean);

    long tarFileLength = writer.computeTarLength2(entries);
    // response.setContentLength((int) tarFileLength);
    // For some reason setContentLength does not work
    response.setProperty("Content-Length", String.valueOf(tarFileLength));
    try {
      writer.setOutputStream(response.getPortletOutputStream());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
    while (it.hasNext()) {
      Entry<String, SimpleEntry<String, Long>> entry = it.next();
      String entryKey = entry.getKey().replaceFirst(entry.getValue().getKey() + "/", "");
      String[] splittedFilePath = entryKey.split("/");

      if ((splittedFilePath.length == 0) || (splittedFilePath == null)) {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(),
            openbisClient.getDatasetStream(entry.getValue().getKey()), entry.getValue().getValue());
      } else {
        writer.writeEntry(bean.getCode() + "/" + entry.getKey(), openbisClient.getDatasetStream(
            entry.getValue().getKey(), entryKey), entry.getValue().getValue());
      }
    }
    writer.closeStream();
  }



  /**
   * if it is one of the openbis beans, then it will be converted into an entry. Used to prepare a
   * bean for download via a writer, e.g. a {@link TarWriter}
   * 
   * @param bean
   * @return
   */
  Map<String, SimpleEntry<String, Long>> convertBeanToEntries(Object bean) {
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
        new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
    if (bean instanceof ProjectBean) {
      ProjectBean projectBean = (ProjectBean) bean;
      for (ExperimentBean eb : projectBean.getExperiments().getItemIds()) {
        for (SampleBean sb : eb.getSamples().getItemIds()) {
          for (DatasetBean db : sb.getDatasets().getItemIds()) {
            addEntry(db, entries);
          }
        }
      }
    } else if (bean instanceof ExperimentBean) {
      ExperimentBean experimentBean = (ExperimentBean) bean;
      for (SampleBean sb : experimentBean.getSamples().getItemIds()) {
        for (DatasetBean db : sb.getDatasets().getItemIds()) {
          addEntry(db, entries);
        }
      }
    }

    else if (bean instanceof SampleBean) {
      SampleBean sampleBean = (SampleBean) bean;
      for (DatasetBean db : sampleBean.getDatasets().getItemIds()) {
        addEntry(db, entries);
      }
    }

    return entries;
  }

  /**
   * Given datasetbean (and its children) is included into the entry, which can be used for download
   * 
   * @param db
   * @param entries
   * @return
   */
  Map<String, AbstractMap.SimpleEntry<String, Long>> addEntry(DatasetBean db,
      Map<String, AbstractMap.SimpleEntry<String, Long>> entries) {
    StringBuilder sb = new StringBuilder(db.getCode());
    sb.append("/");
    sb.append(db.getName());
    if (db.isDirectory()) {
      for (DatasetBean child : db.getChildren()) {
        addChildrensEntry(child, entries, sb.toString());
      }
    } else {
      entries.put(sb.toString(),
          new AbstractMap.SimpleEntry<String, Long>(db.getCode(), db.getFileSize()));
    }
    return entries;
  }

  /**
   * Helper function of addEntry. Adds name of parent db to children.
   * 
   * @param db
   * @param entries
   * @param name
   * @return
   */
  private Map<String, AbstractMap.SimpleEntry<String, Long>> addChildrensEntry(DatasetBean db,
      Map<String, SimpleEntry<String, Long>> entries, String name) {
    StringBuilder sb = new StringBuilder(name);
    sb.append("/");
    sb.append(db.getName());
    if (db.isDirectory()) {
      for (DatasetBean child : db.getChildren()) {
        addChildrensEntry(child, entries, sb.toString());
      }
    } else {
      entries.put(sb.toString(),
          new AbstractMap.SimpleEntry<String, Long>(db.getCode(), db.getFileSize()));
    }
    return entries;

  }

  @Override
  protected VaadinPortletService createPortletService(
      final DeploymentConfiguration deploymentConfiguration) throws ServiceException {
    final CustomVaadinPortletService customVaadinPortletService =
        new CustomVaadinPortletService(this, deploymentConfiguration);
    customVaadinPortletService.init();
    return customVaadinPortletService;
  }
}
