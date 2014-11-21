package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.vaadin.server.StreamResource;

/**
 *  This class should get an openbis datastore_server url for an xml file. 
 * @author wojnar
 *
 */
public class QcMlOpenbisSource implements StreamResource.StreamSource {
URL u;
/**
 * 
 * @param sourceURL url to openbis datastore_server for an xml file.
 */
public QcMlOpenbisSource(URL sourceURL){
  u = sourceURL;
}

/*
 * We need to implement this method that returns the resource as a stream.
 */
public InputStream getStream() {
  try {
    URLConnection urlConnection = u.openConnection();

    urlConnection.setUseCaches(false);
    urlConnection.setDoOutput(false);
    urlConnection.connect();
    InputStream is = urlConnection.getInputStream();
    return is;
  } catch (MalformedURLException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  return null;
}
}

