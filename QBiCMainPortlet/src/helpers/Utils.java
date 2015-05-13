package helpers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Image;

public class Utils {

  /**
   * Checks if a String can be parsed to an Integer
   * 
   * @param s a String
   * @return true, if the String can be parsed to an Integer successfully, false otherwise
   */
  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * Parses a whole String list to integers and returns them in another list.
   * 
   * @param strings List of Strings
   * @return list of integer representations of the input list
   */
  public static List<Integer> strArrToInt(List<String> strings) {
    List<Integer> res = new ArrayList<Integer>();
    for (String s : strings) {
      res.add(Integer.parseInt(s));
    }
    return res;
  }

  /**
   * Maps an integer to a char representation. This can be used for computing the checksum.
   * 
   * @param i number to be mapped
   * @return char representing the input number
   */
  public static char mapToChar(int i) {
    i += 48;
    if (i > 57) {
      i += 7;
    }
    return (char) i;
  }

  /**
   * Checks which of two Strings can be parsed to a larger Integer and returns it.
   * 
   * @param a a String
   * @param b another String
   * @return the String that represents the larger number.
   */
  public static String max(String a, String b) {
    int a1 = Integer.parseInt(a);
    int b1 = Integer.parseInt(b);
    if (Math.max(a1, b1) == a1)
      return a;
    else
      return b;
  }

  /**
   * Creates a string with leading zeroes from a number
   * 
   * @param id number
   * @param length of the final string
   * @return the completed String with leading zeroes
   */
  public static String createCountString(int id, int length) {
    String res = Integer.toString(id);
    while (res.length() < length) {
      res = "0" + res;
    }
    return res;
  }

  /**
   * Increments the value of an upper case char. When at "X" restarts with "A".
   * 
   * @param c the char to be incremented
   * @return the next letter in the alphabet relative to the input char
   */
  public static char incrementUppercase(char c) {
    if (c == 'X')
      return 'A';
    else {
      int charValue = c;
      return (char) (charValue + 1);
    }
  }
  
  public static StreamResource getTSVStream(final String content, String id) {
    StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
      private static final long serialVersionUID = 946357391804404061L;

      @Override
      public InputStream getStream() {
        try {
          InputStream is = new ByteArrayInputStream(content.getBytes());
          return is;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }, String.format("%s_table_contents.tsv", id));
    return resource;
  }
  
  public static String containerToString(Container container) {
    String header = "";
    Collection<?> i = container.getItemIds();
    String rowString = "";

    Collection<?> propertyIDs = container.getContainerPropertyIds();

    for (Object o : propertyIDs) {
      header += o.toString() + "\t";
    }

    // for (int x = 1; x <= i.size(); x++) {
    for (Object id : i) {
      Item it = container.getItem(id);

      for (Object o : propertyIDs) {
        // Could be extended to an exclusion list if we don't want to show further columns
        if (o.toString() == "dl_link") {
          continue;
        } else if (o.toString() == "Status") {
          Image image = (Image) it.getItemProperty(o).getValue();
          rowString += image.getCaption() + "\t";
        } else {
          Property prop = it.getItemProperty(o);
          rowString += prop.toString() + "\t";
        }
      }
      rowString += "\n";
    }
    return header + "\n" + rowString;
  }
  
  //TODO fix and test
  public static String containerToString(BeanItemContainer container) {
    String header = "";
    Collection<?> i = container.getItemIds();
    String rowString = "";

    Collection<?> propertyIDs = container.getContainerPropertyIds();

    for (Object o : propertyIDs) {
      header += o.toString() + "\t";
    }

    // for (int x = 1; x <= i.size(); x++) {
    for (Object id : i) {
      Item it = container.getItem(id);

      for (Object o : propertyIDs) {
        // Could be extended to an exclusion list if we don't want to show further columns
        if (o.toString() == "dl_link") {
          continue;
        } else if (o.toString() == "Status") {
          Image image = (Image) it.getItemProperty(o).getValue();
          rowString += image.getCaption() + "\t";
        } else {
          Property prop = it.getItemProperty(o);
          rowString += prop.toString() + "\t";
        }
      }
      rowString += "\n";
    }
    return header + "\n" + rowString;
  }
  
  public static void printMapContent(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet())
    {
        System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }

}
