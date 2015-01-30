package de.uni_tuebingen.qbic.qbicmainportlet;


import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;

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

  private boolean resUrlNotSet = true;
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
      resUrlNotSet = false;   
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
    Map<String, AbstractMap.SimpleEntry<String, Long>> entries =
        (Map<String, AbstractMap.SimpleEntry<String, Long>>) request.getPortletSession()
            .getAttribute("qbic_download", PortletSession.APPLICATION_SCOPE);
    if (entries == null || entries.isEmpty()) {
      response.setContentType("text/javascript");
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
          String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
      response.getWriter().append("Please select at least one dataset for download");
      // super.serveResource(request, response);
      return;
    }

    // request.getPortletSession().setAttribute("qbic_download_entries",null,PortletSession.APPLICATION_SCOPE);
    // System.out.println(entries);
    /*
     * if(dataHandler == null || entries == null || !(dataHandler instanceof DataHandler) ||
     * !(entries instanceof Map<?,?>)){ response.setContentType("text/plain");
     * response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
     * String.valueOf(HttpServletResponse.SC_NOT_FOUND)); response.getWriter().append(
     * "Oh Dear. Something went wrong.\nRetry later or contact your project manager.\n"+ "Time: " +
     * (new Date()).toString()); return; }
     */
    TarWriter tarWriter = new TarWriter();
    String filename = "all.tar";
    // Sets content type
    response.setContentType("application/x-tar");
    String contentDispositionValue = "attachement; filename=\"" + filename + "\"";
    response.setProperty("Content-Disposition", contentDispositionValue);
    try{
    if (entries != null) {
      long tarFileLength = tarWriter.computeTarLength2(entries);
      // response.setContentLength((int) tarFileLength);
      // For some reason setContentLength does not work
      response.setProperty("Content-Length", String.valueOf(tarFileLength));
      tarWriter.setOutputStream(response.getPortletOutputStream());

      Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
      Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
      while (it.hasNext()) {
        Entry<String, SimpleEntry<String, Long>> entry = it.next();

        String entryKey = entry.getKey().replaceFirst(entry.getValue().getKey() + "/", "");
        String[] splittedFilePath = entryKey.split("/");

        if ((splittedFilePath.length == 0) || (splittedFilePath == null)) {
          tarWriter.writeEntry(entry.getKey(),
              dataHandler.getDatasetStream(entry.getValue().getKey()), entry.getValue().getValue());
        } else {
          tarWriter.writeEntry(entry.getKey(), dataHandler.getDatasetStream(entry.getValue()
              .getKey(), entryKey), entry.getValue().getValue());
        }
      }

      // tarWriter.writeEntry(entries);
    }
    }catch(Exception e){
      //
      System.out.println("client aborted download.");
    }
    
    tarWriter.closeStream();
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
