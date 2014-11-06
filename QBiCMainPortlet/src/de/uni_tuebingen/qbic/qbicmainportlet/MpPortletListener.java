package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.VaadinPortletSession.PortletListener;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;

public class MpPortletListener implements PortletListener,
    com.vaadin.data.Property.ValueChangeListener {


  public static final String RESOURCE_ID = "mainPortletResourceId";
  public static final String RESOURCE_ATTRIBUTE = "resURL";

  private final long DEFAULT_CACHETIME = 1000 * 60 * 60 * 24;
  private final int DEFAULT_BUFFER_SIZE = 32768;
  // private int MAX_BUFFER_SIZE = 65536;

  private final String contentType = "application/x-tar";
  private String filename = "all.tar";
  private final int tar_record_size = 512;
  private final int tar_block_size = tar_record_size * 20;


  // Link that is used to download data sets
  private ButtonLink open;
  private FilterTable table;
  private Set<Object> currentSelectedTableIndices;
  private ResourceURL resURL;
  private DataHandler dataHandler;



  public MpPortletListener(ButtonLink open, FilterTable table) {
    this.open = open;
    this.table = table;
    dataHandler = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

  }

  @Override
  public void handleRenderRequest(RenderRequest request, RenderResponse response, UI uI) {
    this.resURL = response.createResourceURL();
    resURL.setResourceID(RESOURCE_ID);
    // System.out.println("resourceURL initialized");
    request.setAttribute(RESOURCE_ATTRIBUTE, resURL);
    open.setResource(new ExternalResource(resURL.toString()));
    // uI.getSession();
    // VaadinSession.getCurrent().setAttribute(RESOURCE_ATTRIBUTE, resURL.toString());
  }

  @Override
  public void handleActionRequest(ActionRequest request, ActionResponse response, UI uI) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleEventRequest(EventRequest request, EventResponse response, UI uI) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleResourceRequest(ResourceRequest request, ResourceResponse response, UI uI) {
    // resource url was clicked
    if (RESOURCE_ID.equals(request.getResourceID()) && this.currentSelectedTableIndices != null) {
      handleRequestHelper2(response, request, uI);
    }
  }


  private boolean handleRequestHelper2(ResourceResponse response, ResourceRequest request, UI uI) {
    boolean ret = true;
    try {
      // int bufferSize = 0;
      // if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
      int bufferSize = DEFAULT_BUFFER_SIZE;
      // }
      OutputStream out = null;
      InputStream fis = null;
      TarOutputStream zipWriter = null;
      try {
        // Sets content type
        response.setContentType(contentType);

        // Sets cache headers
        String contentDispositionValue = "attachement; filename=\"" + filename + "\"";
        response.setProperty("Content-Disposition", contentDispositionValue);

        long[] file_sizes = new long[this.currentSelectedTableIndices.size()];
        int i = 0;
        Iterator<Object> iterator = this.currentSelectedTableIndices.iterator();

        //
        while (iterator.hasNext()) {
          file_sizes[i] =
              (Long) this.table.getItem(iterator.next()).getItemProperty("file_size_bytes")
                  .getValue();
          i++;
        }

        long tarFileLength = computeTarLength(file_sizes, tar_record_size, tar_block_size);

        // response.setContentLength((int) tarFileLength);
        // For some reason setContentLength does not work
        response.setProperty("Content-Length", String.valueOf(tarFileLength));

        final byte[] buffer = new byte[bufferSize];

        out = response.getPortletOutputStream();

        zipWriter =
            new TarOutputStream(new BufferedOutputStream(out), tar_block_size, tar_record_size);// new
                                                                                                // ZipOutputStream(new
                                                                                                // BufferedOutputStream(out));

        i = 0;
        iterator = this.currentSelectedTableIndices.iterator();
        while (iterator.hasNext()) {
          Object next = iterator.next();
          String fileName =
              (String) this.table.getItem(next).getItemProperty("File Name").getValue();
          int bytesRead = 0;

          fis =
              dataHandler.getDatasetStream((String) this.table.getItem(next)
                  .getItemProperty("CODE").getValue());

          BufferedInputStream fif = new BufferedInputStream(fis);

          TarEntry tar_entry = new TarEntry(fileName);
          tar_entry.setSize(file_sizes[i]);
          i++;
          zipWriter.putNextEntry(tar_entry);// putNextEntry( new
                                            // org.apache.tools.zip.ZipEntry(fileName +
                                            // String.valueOf(i)));
          long totalWritten = 0;

          while ((bytesRead = fif.read(buffer)) > 0) {
            // zipWriter.write(buffer, 0, bytesRead);
            zipWriter.write(buffer, 0, bytesRead);
            totalWritten += bytesRead;
            if (totalWritten >= buffer.length) {
              // Avoid chunked encoding for small resources
              // zipWriter.flush();
              zipWriter.flush();
            }
          }
          // StreamUtil.transfer(fif, zipWriter,false);
          zipWriter.closeEntry();
          // out.flush();
          try {
            // try to close stream
            if (fis != null) {
              fis.close();
            }
          } catch (IOException e1) {
            // NOP
          }

        }

      } finally {
        try {
          // try to close stream
          if (zipWriter != null) {
            zipWriter.close();
          }
          if (out != null) {
            out.close();
          }
        } catch (IOException e1) {
          // NOP
        }
      }
      // }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      ret = false;
    }
    System.out.println("Download finished.");
    return ret;
  }

  private boolean handleRequestHelper1(VaadinSession session, VaadinRequest request,
      VaadinResponse response) throws IOException {
    // resource url was clicked
    if (RESOURCE_ID.equals(request.getPathInfo()) && this.currentSelectedTableIndices != null) {

      boolean ret = true;
      try {
        // int bufferSize = 0;
        // if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
        int bufferSize = DEFAULT_BUFFER_SIZE;
        // }
        OutputStream out = null;
        InputStream fis = null;
        TarOutputStream zipWriter = null;
        try {
          // Sets content type
          response.setContentType(contentType);

          // Sets cache headers
          response.setCacheTime(DEFAULT_CACHETIME);

          String contentDispositionValue = "attachement; filename=\"" + filename + "\"";
          response.setHeader("Content-Disposition", contentDispositionValue);

          long[] file_sizes = new long[this.currentSelectedTableIndices.size()];
          int i = 0;
          Iterator<Object> iterator = this.currentSelectedTableIndices.iterator();

          //
          while (iterator.hasNext()) {
            file_sizes[i] =
                (Long) this.table.getItem(iterator.next()).getItemProperty("file_size_bytes")
                    .getValue();
            i++;
          }

          long tarFileLength = computeTarLength(file_sizes, tar_record_size, tar_block_size);
          response.setHeader("Content-Length", String.valueOf(tarFileLength));


          final byte[] buffer = new byte[bufferSize];

          out = response.getOutputStream();

          zipWriter =
              new TarOutputStream(new BufferedOutputStream(out), tar_block_size, tar_record_size);// new
                                                                                                  // ZipOutputStream(new
                                                                                                  // BufferedOutputStream(out));
          DataHandler dataHandler =
              (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
          i = 0;
          iterator = this.currentSelectedTableIndices.iterator();
          while (iterator.hasNext()) {
            Object next = iterator.next();
            String fileName =
                (String) this.table.getItem(next).getItemProperty("File Name").getValue();
            int bytesRead = 0;

            fis =
                dataHandler.getDatasetStream((String) this.table.getItem(next)
                    .getItemProperty("CODE").getValue());

            BufferedInputStream fif = new BufferedInputStream(fis);

            TarEntry tar_entry = new TarEntry(fileName + String.valueOf(i));
            tar_entry.setSize(file_sizes[i]);
            i++;
            zipWriter.putNextEntry(tar_entry);// putNextEntry( new
                                              // org.apache.tools.zip.ZipEntry(fileName +
                                              // String.valueOf(i)));
            long totalWritten = 0;

            while ((bytesRead = fif.read(buffer)) > 0) {
              // zipWriter.write(buffer, 0, bytesRead);
              zipWriter.write(buffer, 0, bytesRead);
              totalWritten += bytesRead;
              if (totalWritten >= buffer.length) {
                // Avoid chunked encoding for small resources
                // zipWriter.flush();
                zipWriter.flush();
              }
            }
            // StreamUtil.transfer(fif, zipWriter,false);
            zipWriter.closeEntry();
            // out.flush();
            try {
              // try to close stream
              if (fis != null) {
                fis.close();
              }
            } catch (IOException e1) {
              // NOP
            }

          }

        } finally {
          try {
            // try to close stream
            if (zipWriter != null) {
              zipWriter.close();
            }
          } catch (IOException e1) {
            // NOP
          }
        }
        // }

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        ret = false;
      }

      return ret;
    }
    return false;
  }


  /**
   * Computes the size of an uncompressed tarball with the given parameters. See this for more
   * information: http://en.wikipedia.org/wiki/Tar_%28computing%29#Format_details
   * 
   * @param file_sizes in bytes
   * @param tar_record_size in bytes
   * @param tar_block_size in bytes
   * @return size of the whole tar ball
   */
  private long computeTarLength(long[] file_sizes, int tar_record_size, int tar_block_size) {
    // Every file has a header
    long length_tar_headers = file_sizes.length * tar_record_size;
    long total_length_file_sizes = 0;
    long append_zeros = 0;
    for (int i = 0; i < file_sizes.length; i++) {
      // every file is saved uncomppressed, add just its file size
      total_length_file_sizes += file_sizes[i];
      // Each entry must be a multiple of the record size. it is not. entry will be filled with
      // zeros until it is.
      long mod = file_sizes[i] % tar_record_size;
      if (mod > 0) {

        append_zeros += tar_record_size - mod;
      }
    }

    // tar ball must be a multiple of block size. If it is not will be filled with zeros until it
    // is.
    long mod = (length_tar_headers + total_length_file_sizes + append_zeros) % tar_block_size;
    if (mod > 0) {
      mod = tar_block_size - mod;
    }
    return length_tar_headers + total_length_file_sizes + append_zeros + mod;
  }

  /*
   * @Override public void update(Observable o, Object arg) { ArrayList<String> a =
   * (ArrayList<String>)arg; if(a.get(0).equals(MpValueChangeListener.class.getName())) {
   * 
   * //use direct url to data set, created by openbis if(a.size() == 2){ DataHandler dataHandler =
   * (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler"); URL url=
   * dataHandler.getUrlForDataset(a.get(1)); open.setResource(new ExternalResource(url)); } //Else
   * it will be more complicated else{ open.setResource(new ExternalResource((String)
   * VaadinSession.getCurrent().getAttribute(RESOURCE_ATTRIBUTE))); // this.datasetsCodes.clear();
   * for(int i = 1; i < a.size(); i++){ // this.datasetsCodes.add(a.get(i)); } } }
   * 
   * }
   */
  @Override
  public void valueChange(ValueChangeEvent event) {
    this.currentSelectedTableIndices = (Set<Object>) event.getProperty().getValue();
    if (this.currentSelectedTableIndices != null) {

      if (this.currentSelectedTableIndices.size() == 1) {
        DataHandler dataHanlder =
            (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
        Iterator<Object> iterator = this.currentSelectedTableIndices.iterator();
        Object next = iterator.next();
        String datasetCode = (String) table.getItem(next).getItemProperty("CODE").getValue();
        String datasetType = (String) table.getItem(next).getItemProperty("File Name").getValue();
        try {
          this.open.setResource(new ExternalResource(dataHanlder.getUrlForDataset(datasetCode,
              datasetType)));
          this.open.setEnabled(true);
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        this.currentSelectedTableIndices = null;
      } else if (this.currentSelectedTableIndices.size() > 1) {
        this.open.setResource(new ExternalResource(this.resURL.toString()));
        this.open.setEnabled(UI.getCurrent().getPage().getWebBrowser().isChrome());
      } else {
        // nothing selected. Probably will never occur, because then the set is probably null
        this.currentSelectedTableIndices = null;
        this.open.setEnabled(false);
      }
    } else {
      this.open.setEnabled(false);
    }
  }


}
