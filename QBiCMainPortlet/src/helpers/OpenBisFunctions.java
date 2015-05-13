package helpers;


public class OpenBisFunctions {

  /**
   * Returns the 4 or 5 character project prefix used for samples in openBIS.
   * 
   * @param sample sample ID starting with a standard project prefix.
   * @return Project prefix of the sample
   */
  public static String getProjectPrefix(String sample) {
    if (Utils.isInteger("" + sample.charAt(4)))
      return sample.substring(0, 4);
    else
      return sample.substring(0, 5);
  }

  public static double statusToDoubleValue(String status) {

    double value = 0.0;

    switch (status) {
      case "STARTED":
        value = 0.25;
        break;
      case "RUNNING":
        value = 0.5;
        break;
      case "FINISHED":
        value = 1.0;
        break;

    }
    return value;
  }
}
