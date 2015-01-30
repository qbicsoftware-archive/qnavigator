package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;


public class TarWriter {
  //some default value
  private String rootFolderName = "qbic";
  // TODO Take Vaadin defaults!
  private final int DEFAULT_BUFFER_SIZE = 32768;
  private int MAX_BUFFER_SIZE = 65536;
  private int BUFFER_SIZE = DEFAULT_BUFFER_SIZE;

  // Standard in most modern tar applications
  private final int tar_record_size = 512;
  final int tar_block_size = tar_record_size * 20;

  private TarOutputStream tar = null;

  /**
   * Sets the root Folder Name. If the archive is extracted everything is extracted to this folder
   * @param rootFolderName
   */
  public void setRootFolderName(String rootFolderName){
    this.rootFolderName = rootFolderName;
  }
  /**
   * Sets the buffer size to new value only if 0 < bufferSize <= MAX_BUFFER_SIZE.
   * 
   * @param bufferSize
   */
  public void setBufferSize(int bufferSize) {
    if (0 < bufferSize && bufferSize <= MAX_BUFFER_SIZE) {
      BUFFER_SIZE = bufferSize;
    }
  }


  /**
   * sets the buffer size to default
   */
  public void resetBufferSize() {
    BUFFER_SIZE = DEFAULT_BUFFER_SIZE;
  }

  /**
   * if another outputstream was already used it is first closed.
   * 
   * @param out
   */
  public void setOutputStream(OutputStream out) {
    if (tar != null) {
      try {
        this.tar.close();
      } catch (IOException e) {
        System.out.println("TarWriter::setOutputStream, close throws Exception.");
      }
    }
    this.tar = new TarOutputStream(out);
    tar.setLongFileMode(TarOutputStream.LONGFILE_POSIX);
    tar.setBigNumberMode(TarOutputStream.BIGNUMBER_POSIX);
    
  }

  /**
   * tries to close the tar stream
   */
  public void closeStream() {
    if (tar != null) {
      try {
        this.tar.close();
      } catch (IOException e) {
        System.out.println("TarWriter::closeStream, close throws Exception.");
      }
      tar = null;
    }
  }

  /**
   * writes entries into the Tar ball. Each key value pair of entries will be one entry.
   * entries::key is the TarEntry name. entry::value the inputStream that will be written to that
   * entry.
   * 
   * @param entry
   */
  public void writeEntry(Map<String, AbstractMap.SimpleEntry<InputStream, Long>> entries) {
    Set<Entry<String, SimpleEntry<InputStream, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<InputStream, Long>>> it = entrySet.iterator();
    while (it.hasNext()) {
      Entry<String, SimpleEntry<InputStream, Long>> entry = it.next();
      this.writeEntry(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue());
    }
  }



  /**
   * Writes the entry into the tar ball. filSize has to match the size of the entry. It will be
   * written to the tar ball under the name entryName.
   * 
   * @param entryName
   * @param entry
   * @param fileSize
   */
  public void writeEntry(String entryName, InputStream entry, long fileSize) {
    //System.out.println(entryName + entry + fileSize);
    TarEntry tar_entry = new TarEntry(entryName);
    tar_entry.setSize(fileSize);
    tar_entry.getRealSize();
    try {
      tar.putNextEntry(tar_entry);
      long totalWritten = 0;
      int bytesRead = 0;
      final byte[] buffer = new byte[BUFFER_SIZE];
      //System.out.println("File: " + entryName + ", Size: " + Long.toString(fileSize));
      

      while ((bytesRead = entry.read(buffer)) > 0) {
        //System.out.println("bytes read: " + Integer.toString(bytesRead) + " buffer.length: " + Integer.toString(buffer.length));
        tar.write(buffer, 0, bytesRead);
        totalWritten += bytesRead;
        //System.out.println("totalWritten: " + totalWritten);
        if (totalWritten >= buffer.length) {
          // Avoid chunked encoding for small resources
          tar.flush();
        }
      }
      //System.out.println("bytesRead");
      tar.closeEntry();

      // try to close input stream
      if (entry != null) {
        entry.close();
      }
    } catch (IOException e1) {
      //e1.printStackTrace();
      if(e1 instanceof ClientAbortException){
        System.out.println("TarWriter::writeEntry client aborted download.");
        return;
      }
      else{
        System.out.println("TarWriter::writeEntry failed: IOException.");
      }
      
    }
  }
  /**
   * creates a String path out of the given parameters
   * @param paths
   * @return
   */
  public String getPath(String... paths){
    StringBuilder sb = new StringBuilder(rootFolderName);
    for(String s : paths){
      if(!s.startsWith(java.io.File.separator))sb.append(java.io.File.separator);
      sb.append(s);
    }
    return sb.toString();
  }
  
  public long computeTarLength(Map<String, AbstractMap.SimpleEntry<InputStream, Long>> entries){
    Set<Entry<String, SimpleEntry<InputStream, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<InputStream, Long>>> it = entrySet.iterator();
    long [] tarLen = new long[entrySet.size()];
    int i = 0;
    while (it.hasNext()) {
      Entry<String, SimpleEntry<InputStream, Long>> entry = it.next();
      tarLen[i] = entry.getValue().getValue();
      i++;
    }
    return computeTarLength(tarLen, tar_record_size, tar_block_size);
  }
  
  public long computeTarLength2(Map<String, AbstractMap.SimpleEntry<String, Long>> entries){
    Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
    Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
    long [] tarLen = new long[entrySet.size()];
    String [] fileNames = new String[entrySet.size()];
    int i = 0;
    while (it.hasNext()) {
      Entry<String, SimpleEntry<String, Long>> entry = it.next();
      tarLen[i] = entry.getValue().getValue();
      fileNames[i] = entry.getKey();
      i++;
    }
    return computeTarLength( tarLen, tar_record_size, tar_block_size);
  }
  
  /**
   * Computes the size of an uncompressed tarball with the given parameters. See this for more
   * information: http://en.wikipedia.org/wiki/Tar_%28computing%29#Format_details
   * IMPORTANT: computed value might be slightly bigger than the real one. But it suffices for our purppose, where we want to show download progress. 
   * 
   * @param file_sizes in bytes
   * @param tar_record_size in bytes
   * @param tar_block_size in bytes
   * @return size of the whole tar ball
   */
  private long computeTarLength(long[] file_sizes, int tar_record_size, int tar_block_size) {
    // Every file has a header
    long length_tar_headers = file_sizes.length * tar_record_size*3;
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

  /**
   * Computes the size of an uncompressed tarball with the given parameters. See this for more
   * information: http://en.wikipedia.org/wiki/Tar_%28computing%29#Format_details
   * Important: handles different header sizes
   * DOES NOT WORK DO NOT USE
   * 
   * @param file_sizes in bytes
   * @param tar_record_size in bytes
   * @param tar_block_size in bytes
   * @return size of the whole tar ball
   */
  @Deprecated
  private long computeTarLength(String [] fileName, long[] file_sizes, int tar_record_size, int tar_block_size) {
    // Every file has a header
    long total_length_file_sizes = 0;
    long append_zeros = 0;
    for (int i = 0; i < file_sizes.length; i++) {
      // every file is saved uncomppressed, add just its file size
      TarEntry entry = new TarEntry(fileName[i]);
      entry.setSize(file_sizes[i]);
      System.out.println(fileName[i]);
      System.out.println(file_sizes[i]);
      System.out.println(entry.getSize());
      System.out.println(entry.getSize());
      total_length_file_sizes += entry.getSize();
      // Each entry must be a multiple of the record size. it is not. entry will be filled with
      // zeros until it is.
      long mod = file_sizes[i] % tar_record_size;
      if (mod > 0) {

        append_zeros += tar_record_size - mod;
      }
    }

    // tar ball must be a multiple of block size. If it is not will be filled with zeros until it
    // is.
    long mod = (total_length_file_sizes + append_zeros) % tar_block_size;
    if (mod > 0) {
      mod = tar_block_size - mod;
    }
    return total_length_file_sizes + append_zeros + mod;
  }
  
  
  
public static void test1(){
  System.out.println("main started!");
  int numberOfDataSets = 2;
  long[] file_sizes = new long[numberOfDataSets];
  file_sizes[0] = 1355186601;
  file_sizes[1] = 1188020835;
  String[] fileNames = new String[numberOfDataSets];
  String[] codes = new String[numberOfDataSets];
  fileNames[0] =
      "/home/wojnar/QBiC/tar_test/20140618171546_FKL1516-S02-A-ERC_08_2_1_1_QSDPR004AI.raw";
  codes[0] = "20140620173152849-3060";
  fileNames[1] =
      "/home/wojnar/QBiC/tar_test/20140618172046_FKL1516-S03-A-ERC_08_2_1_2_QSDPR005AQ.raw";
  codes[1] = "20140620174145184-3068";

  TarWriter tarTest = new TarWriter();
  OutputStream out = null;
  try {
    out = new FileOutputStream("/home/wojnar/QBiC/tar_test/testfile2.tar");
  } catch (FileNotFoundException e) {
    System.out.println("file outputstream should create that file. Yet it did not?");
    return;
  }
  tarTest.setOutputStream(out);
  for (int i = 0; i < numberOfDataSets; i++) {
    System.out.println("writing: " + fileNames[i]);
    InputStream fis = null;
    try {
      fis = new FileInputStream(fileNames[i]);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }

    BufferedInputStream fif = new BufferedInputStream(fis);
    tarTest.writeEntry(fileNames[i], fif, file_sizes[i]);
  }
  System.out.println("closeing stream");
  tarTest.closeStream();
  System.out.println("tar finished");
}

public static void test_write_openbisStreams(){
  
  OpenBisClient openbisClient = new OpenBisClient("admin", "ohsaup7ahChie1Aen2eeBeagh", "https://qbis.qbic.uni-tuebingen.de:443", false);
  IOpenbisServiceFacade facade = openbisClient.getFacade();
  String datasetCodeMaxQuantResults = "20141121122213779-4640";
  String datasetCodeMaxQuantOriginOut = "20141121122213743-4639";
  DataSet dataSetResults = facade.getDataSet(datasetCodeMaxQuantResults);
  String MaxQuantResultsFolder = "results";
  FileInfoDssDTO[] fileInfoResults = dataSetResults.listFiles("original/" + MaxQuantResultsFolder, true);
  
  System.out.println("folder size is: " + fileInfoResults.length + " should be: " + 6);
  System.out.println("Size is: " + fileInfoResults[0].getFileSize() + " should be: " + 0);
  System.out.println("Size is: " + fileInfoResults[1].getFileSize() + " should be: " + 0);
  System.out.println("Size is: " + fileInfoResults[2].getFileSize() + " should be: " + 0);
  System.out.println("Size is: " + fileInfoResults[3].getFileSize() + " should be: " + 0);
  System.out.println("Size is: " + fileInfoResults[4].getFileSize() + " should be: " + 2);
  
  DataSet dataSetOriginOut = facade.getDataSet(datasetCodeMaxQuantOriginOut);
  FileInfoDssDTO[] fileInfoOriginOut = dataSetOriginOut.listFiles("original", false);
  System.out.println("Size is: " + fileInfoOriginOut.length + " should be: " + 1);
  System.out.println("Size is: " + fileInfoOriginOut[0].getFileSize() + " should be: " + 958);
  
  
  Map<String, AbstractMap.SimpleEntry<String, Long>> entries = new HashMap<String, AbstractMap.SimpleEntry<String, Long>>();
  
  entries.put(String.format("%s/%s", MaxQuantResultsFolder, getFileName(fileInfoResults[0])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[0].getFileSize()));
  entries.put(String.format("%s/%s", MaxQuantResultsFolder,  getFileName(fileInfoResults[1])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[1].getFileSize()));
  entries.put(String.format("%s/%s", MaxQuantResultsFolder,  getFileName(fileInfoResults[2])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[2].getFileSize()));
  entries.put(String.format("%s/%s", MaxQuantResultsFolder,  getFileName(fileInfoResults[3])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[3].getFileSize()));
  entries.put(String.format("%s/%s", MaxQuantResultsFolder,  getFileName(fileInfoResults[4])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[4].getFileSize()));
  entries.put(String.format("%s/%s", MaxQuantResultsFolder,  getFileName(fileInfoResults[5])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213779-4640", fileInfoResults[5].getFileSize()));
  
  entries.put(String.format("%s",  getFileName(fileInfoOriginOut[0])) , new AbstractMap.SimpleEntry<String, Long>("20141121122213743-4639", fileInfoOriginOut[0].getFileSize()));
  
  TarWriter tarWriter = new TarWriter();
  long tarFileLength = tarWriter.computeTarLength2(entries);
  System.out.println("Theoretical Length of tarFile: " + tarFileLength + " should be: " + 10240);
  
  
  OutputStream out = null;
  try {
    out = new FileOutputStream("/home/wojnar/QBiC/tar_test/testfile_wsimulation_withOpenbis.tar");
  } catch (FileNotFoundException e) {
    System.out.println("file outputstream should create that file. Yet it did not?");
    return;
  }
  tarWriter.setOutputStream(out);
  
  Set<Entry<String, SimpleEntry<String, Long>>> entrySet = entries.entrySet();
  Iterator<Entry<String, SimpleEntry<String, Long>>> it = entrySet.iterator();
  while (it.hasNext()) {
    Entry<String, SimpleEntry<String, Long>> entry = it.next();
    
    String[] splittedFilePath = entry.getKey().split("/");

    if((splittedFilePath.length == 0) || (splittedFilePath == null)) {
      IOpenbisServiceFacade f = openbisClient.getFacade();
      DataSet dataSet = f.getDataSet(entry.getValue().getKey());
      FileInfoDssDTO[] filelist = dataSet.listFiles("original", false);
      tarWriter.writeEntry(entry.getKey(), dataSet.getFile(filelist[0].getPathInDataSet()), entry.getValue().getValue());
    }
    else {
      IOpenbisServiceFacade f = openbisClient.getFacade();
      DataSet dataSet = f.getDataSet(entry.getValue().getKey());
      FileInfoDssDTO[] filelist = dataSet.listFiles("original/" + entry.getKey(), false);
      
      System.out.println("File size: " + filelist[0].getFileSize() +  " should be " + entry.getValue().getValue());
      tarWriter.writeEntry(entry.getKey(), dataSet.getFile(filelist[0].getPathInDataSet()), entry.getValue().getValue());
    }
  }
  tarWriter.closeStream();
  System.out.println("Stream written.");
}
public static String getFileName(FileInfoDssDTO f){
  String download_link = f.getPathInDataSet();
  String[] splitted_link = download_link.split("/");
  String file_name = download_link.split("/")[splitted_link.length -1];
  return file_name;
}
public static void test2(){
  TarWriter tarTest = new TarWriter();
  System.out.println(tarTest.getPath("simple", "test","/blabla"));
}
  public static void main(String args[]) {
    test_write_openbisStreams();
    // tarTest.handleTar();
  }



  public void handleTar() {

    int bufferSize = DEFAULT_BUFFER_SIZE;

    OutputStream out = null;
    InputStream fis = null;
    TarOutputStream zipWriter = null;
    try {

      long[] file_sizes = new long[2];
      int i = 0;
      file_sizes[0] = 1355186601;
      file_sizes[1] = 1188020835;

      long tarFileLength = computeTarLength(file_sizes, tar_record_size, tar_block_size);
      System.out.println("Estimated tar file len: " + String.valueOf(tarFileLength));

      final byte[] buffer = new byte[bufferSize];

      out = new FileOutputStream("/home/wojnar/QBiC/tar_test/testfile.tar");

      zipWriter =
          new TarOutputStream(new BufferedOutputStream(out), tar_block_size, tar_record_size);// new
                                                                                              // ZipOutputStream(new
                                                                                              // BufferedOutputStream(out));

      i = 0;
      String[] fileNames = new String[2];
      String[] codes = new String[2];
      fileNames[0] =
          "/home/wojnar/QBiC/tar_test/20140618171546_FKL1516-S02-A-ERC_08_2_1_1_QSDPR004AI.raw";
      codes[0] = "20140620173152849-3060";
      fileNames[1] =
          "/home/wojnar/QBiC/tar_test/20140618172046_FKL1516-S03-A-ERC_08_2_1_2_QSDPR005AQ.raw";
      codes[1] = "20140620174145184-3068";
      while (i < 2) {
        String fileName = fileNames[i];
        int bytesRead = 0;

        fis = new FileInputStream(fileName);

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
      zipWriter.close();

    } // TODO: ClientAbortException
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Download finished.");
  }



}
