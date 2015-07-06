package helpers;
import java.util.HashMap;
import java.util.Map;


public class UglyToPrettyNameMapper {
  private Map<String,String> namesMapping = new HashMap<String, String>();

  public UglyToPrettyNameMapper() {
    // openBIS experiment types translated
    
    namesMapping.put("Q_EXPERIMENTAL_DESIGN", "Sampling units");
    namesMapping.put("Q_SAMPLE_EXTRACTION", "Sample extraction");
    namesMapping.put("Q_SAMPLE_PREPARATION", "Sample preparation");
    namesMapping.put("Q_PROJECT_DETAILS", "Project details");
    
    
    
    namesMapping.put("Q_BIOLOGICAL_ENTITY", "Experimental unit");
    namesMapping.put("Q_BIOLOGICAL_SAMPLE", "Extracted sample");
    namesMapping.put("Q_TEST_SAMPLE", "Prepared sample");
    
    namesMapping.put("MSH_UNDEFINED_STATE", "Sample not yet part of the MultiscaleHCC workflow");
    namesMapping.put("MSH_SURGERY_SAMPLE_TAKEN", "Liver tumor biopsy finished");
    namesMapping.put("MSH_SENT_TO_PATHOLOGY","Tumor sample sent to pathology");
    namesMapping.put("MSH_PATHOLOGY_REVIEW_STARTED", "Tumor sample is under review");
    namesMapping.put("MSH_PATHOLOGY_REVIEW_FINISHED", "Tumor sample review completed.");
    namesMapping.put("MSH_SENT_TO_HUMAN_GENETICS","Tumor sample sent to Human Genetics department");
    

  }
  
  public String getPrettyName(String uglyName) {
    String prettyName = uglyName;
    
    if (namesMapping.containsKey(uglyName))
    {
      prettyName = namesMapping.get(uglyName);
    }
    
    
    return prettyName;
  }
  
}
