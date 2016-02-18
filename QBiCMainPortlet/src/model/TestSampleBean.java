package model;

import helpers.UglyToPrettyNameMapper;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import parser.XMLParser;
import properties.Qproperties;


public class TestSampleBean implements Comparable<Object>, Serializable {

  /**
     * 
     */
  private static final long serialVersionUID = -61486023394904818L;
  private String id;
  private String code;
  private String type;

  private String sampleType;

  private String secondaryName;
  private String additionalInfo;
  private String externalDB;

  private String properties;

  private UglyToPrettyNameMapper prettyNameMapper = new UglyToPrettyNameMapper();



  public TestSampleBean(String id, String code, String type, String sampleType,
      String secondaryName, String additionalInfo, String externalDB, String properties) {
    super();
    this.id = id;
    this.code = code;
    this.type = type;
    this.sampleType = sampleType;
    this.secondaryName = secondaryName;
    this.additionalInfo = additionalInfo;
    this.externalDB = externalDB;
    this.properties = properties;

  }

  public TestSampleBean() {

  }



  public String getId() {
    return id;
  }



  public void setId(String id) {
    this.id = id;
  }



  public String getCode() {
    return code;
  }



  public void setCode(String code) {
    this.code = code;
  }



  public String getType() {
    return type;
  }



  public void setType(String type) {
    this.type = type;
  }



  @Override
  public int compareTo(Object o) {
    return id.compareTo(((SampleBean) o).getId());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SampleBean) {
      SampleBean b = (SampleBean) o;
      return id.equals(b.getId());
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public String getSecondaryName() {
    return secondaryName;
  }

  public void setSecondaryName(String secondaryName) {
    this.secondaryName = secondaryName;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public String getExternalDB() {
    return externalDB;
  }

  public void setExternalDB(String externalDB) {
    this.externalDB = externalDB;
  }

  public String getSampleType() {
    return sampleType;
  }

  public void setSampleType(String sampleType) {
    this.sampleType = sampleType;
  }

  public String getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    try {
      this.properties = generateXMLPropertiesFormattedString(properties);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  public String generateXMLPropertiesFormattedString(Map<String, String> properties)
      throws JAXBException {

    String xmlPropertiesString = "";
    Iterator<Entry<String, String>> it = properties.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      if (pairs.getKey().equals("Q_PROPERTIES")) {
        XMLParser xmlParser = new XMLParser();
        JAXBElement<Qproperties> xmlProperties =
            xmlParser.parseXMLString(pairs.getValue().toString());
        Map<String, String> xmlPropertiesMap = xmlParser.getMap(xmlProperties);

        Iterator itProperties = xmlPropertiesMap.entrySet().iterator();
        while (itProperties.hasNext()) {
          Map.Entry pairsProperties = (Map.Entry) itProperties.next();

          xmlPropertiesString += pairsProperties.getKey() + ": " + pairsProperties.getValue() + " ";
        }
        break;
      }
    }
    return xmlPropertiesString;
  }

}
