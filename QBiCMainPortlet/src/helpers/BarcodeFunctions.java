package helpers;


import java.util.List;

/**
 * Helper functions used for sample creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class BarcodeFunctions {

  public static float getPercentageStep(int max) {
    return new Float(1.0 / max);
  }

  /**
   * Computes a checksum digit for a given String. This checksum is weighted position-specific,
   * meaning it will also most likely fail a check if there is a typo of the String resulting in a
   * swapping of two numbers.
   * 
   * @param s String for which a checksum should be computed.
   * @return Character representing the checksum of the input String.
   */
  public static char checksum(String s) {
    int i = 1;
    int sum = 0;
    for (int idx = 0; idx <= s.length() - 1; idx++) {
      sum += (((int) s.charAt(idx))) * i;
      i += 1;
    }
    return Utils.mapToChar(sum % 34);
  }

  /**
   * Returns a String denoting the range of a list of barcodes as used in QBiC
   * 
   * @param ids List of code strings
   * @return String denoting a range of the barcodes
   */
  public static String getBarcodeRange(List<String> ids) {
    String head = OpenBisFunctions.getProjectPrefix(ids.get(0));
    String min = ids.get(0).substring(5, 8);
    String max = min;
    for (String id : ids) {
      String num = id.substring(5, 8);
      if (num.compareTo(min) < 0)
        min = num;
      if (num.compareTo(max) > 0)
        max = num;
    }
    return head + min + "-" + max;
  }

  /**
   * Checks if a String fits the QBiC barcode pattern
   * 
   * @param code A String that may be a barcode
   * @return true if String is a QBiC barcode, false if not
   */
  public static boolean isQbicBarcode(String code) {
    String pattern = "Q[A-Z0-9]{4}[0-9]{3}[A-Z0-9]{2}";
    return code.matches(pattern);
  }
}
