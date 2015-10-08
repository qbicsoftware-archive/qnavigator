  package model;

  import helpers.UglyToPrettyNameMapper;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

  import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

  import parser.Parser;
import properties.Qproperties;  

  public class BiologicalSampleBean implements Comparable<Object>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -61486023394904818L;
    private String id;
    private String code;
    private String type;
    
    private String biologicalEntity;

    private String primaryTissue;
    private String tissueDetailed;

    private String secondaryName;
    private String additionalInfo;
    private String externalDB;
    
    private String properties;
    
    private UglyToPrettyNameMapper prettyNameMapper = new UglyToPrettyNameMapper();
    
    

    public BiologicalSampleBean(String id, String code, String type, String biologicalEntity, String primaryTissue, String tissueDetailed, String secondaryName, String additionalInfo, String externalDB, String properties) {
      super();
      this.id = id;
      this.code = code;
      this.type = type;
      this.setBiologicalEntity(biologicalEntity);
      this.setPrimaryTissue(primaryTissue);
      this.setTissueDetailed(tissueDetailed);
      this.secondaryName = secondaryName;
      this.additionalInfo = additionalInfo;
      this.externalDB = externalDB;
      this.properties = properties;
      
    }

    public BiologicalSampleBean() {

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
      this.type = prettyNameMapper.getPrettyName(type);
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
    

    public String getProperties() {
      return properties;
    }

    public void setProperties(Map<String,String> properties) {
      try {
        this.properties = generateXMLPropertiesFormattedString(properties);
      } catch (JAXBException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    

    public String generateXMLPropertiesFormattedString(Map<String,String> properties) throws JAXBException {

      String xmlPropertiesString = "";
      Iterator<Entry<String, String>> it = properties.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
        if (pairs.getKey().equals("Q_PROPERTIES")) {
          Parser xmlParser = new Parser();
          JAXBElement<Qproperties> xmlProperties =
              xmlParser.parseXMLString(pairs.getValue().toString());
          Map<String, String> xmlPropertiesMap = xmlParser.getMap(xmlProperties);

          Iterator itProperties = xmlPropertiesMap.entrySet().iterator();
          while (itProperties.hasNext()) {
            Map.Entry pairsProperties = (Map.Entry) itProperties.next();

            xmlPropertiesString +=
                    pairsProperties.getKey() + ": "
                        + pairsProperties.getValue() + " ";
          }
          break;
        }
      }
      return xmlPropertiesString;
    }

    public String getPrimaryTissue() {
      return primaryTissue;
    }

    public void setPrimaryTissue(String primaryTissue) {
      this.primaryTissue = primaryTissue;
    }

    public String getTissueDetailed() {
      return tissueDetailed;
    }

    public void setTissueDetailed(String tissueDetailed) {
      this.tissueDetailed = tissueDetailed;
    }

    public String getBiologicalEntity() {
      return biologicalEntity;
    }

    public void setBiologicalEntity(String biologicalEntity) {
      this.biologicalEntity = biologicalEntity;
    }

  }
