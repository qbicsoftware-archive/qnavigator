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
    
    namesMapping.put("MSH_UNDEFINED_STATE", "Sample not initialized for MultiscaleHCC workflow");
    
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
