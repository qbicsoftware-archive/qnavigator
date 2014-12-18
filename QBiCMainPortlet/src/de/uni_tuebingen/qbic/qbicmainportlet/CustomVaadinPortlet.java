package de.uni_tuebingen.qbic.qbicmainportlet;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.ConfigurationManagerFactory;

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
      //self contained approach:
      //return request.getContextPath();
    }
  }
  
  private boolean resUrlNotSet = true;
  public static final String RESOURCE_ID = "mainPortletResourceId";
  public static final String RESOURCE_ATTRIBUTE = "resURL";
  @Override
  protected void doDispatch(javax.portlet.RenderRequest request,
      javax.portlet.RenderResponse response)
throws javax.portlet.PortletException,
      java.io.IOException{
    if(resUrlNotSet){
      ResourceURL resURL = response.createResourceURL();
      // get Resource ID ?
      resURL.setResourceID(RESOURCE_ID);
      request.getPortletSession().setAttribute(RESOURCE_ATTRIBUTE,resURL,PortletSession.APPLICATION_SCOPE);
      resUrlNotSet = false;
    }
    super.doDispatch(request, response);
  }
  @Override
  public void  serveResource(javax.portlet.ResourceRequest request, javax.portlet.ResourceResponse response) throws PortletException, IOException{
    System.out.println(request.getResourceID());
    System.out.println(RESOURCE_ID);
    if(request.getResourceID().equals("openbisUnreachable")){
      response.setContentType("text/plain");
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_GATEWAY_TIMEOUT));
      response.getWriter().append(
          "Internal Error.\nRetry later or contact your project manager.\n"+
          "Time: " + (new Date()).toString());
    }
    else if(request.getResourceID().equals(RESOURCE_ID)){
      serveDownloadResource(request, response);
    }else{
      super.serveResource(request, response);
    }
  }
  
  public void serveDownloadResource(javax.portlet.ResourceRequest request, javax.portlet.ResourceResponse response) throws PortletException, IOException{
    DataHandler dataHandler = (DataHandler)request.getPortletSession().getAttribute("datahandler",PortletSession.APPLICATION_SCOPE);
    Map<String, AbstractMap.SimpleEntry<InputStream, Long>> entries = (Map<String, AbstractMap.SimpleEntry<InputStream, Long>>)request.getPortletSession().getAttribute("qbic_download",PortletSession.APPLICATION_SCOPE);
    request.getPortletSession().setAttribute("qbic_download_entries",null,PortletSession.APPLICATION_SCOPE);
    System.out.println(entries);
    /*
    if(dataHandler == null || entries == null || !(dataHandler instanceof DataHandler) || !(entries instanceof Map<?,?>)){
      response.setContentType("text/plain");
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_NOT_FOUND));
      response.getWriter().append(
          "Oh Dear. Something went wrong.\nRetry later or contact your project manager.\n"+
          "Time: " + (new Date()).toString());
      return;
    }
    */
    OutputStream out = null;
    TarWriter tarWriter = new TarWriter();
    String filename = "all.tar";
      // Sets content type
     response.setContentType("application/x-tar");
     String contentDispositionValue = "attachement; filename=\"" + filename + "\"";
     response.setProperty("Content-Disposition", contentDispositionValue);
     
     if(entries != null) {
      long tarFileLength = tarWriter.computeTarLength(entries);
      // response.setContentLength((int) tarFileLength);
      // For some reason setContentLength does not work
      response.setProperty("Content-Length", String.valueOf(tarFileLength));
      tarWriter.setOutputStream(response.getPortletOutputStream());
      tarWriter.writeEntry(entries);
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
