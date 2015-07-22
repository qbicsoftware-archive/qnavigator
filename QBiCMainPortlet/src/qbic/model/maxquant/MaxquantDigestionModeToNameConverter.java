package qbic.model.maxquant;

import java.util.HashMap;
import java.util.Locale;

import com.vaadin.data.util.converter.Converter;

/**
 * converts maxquant digestion modes from digestionMode(integer) to a user readable string and vice versa.
 * @author wojnar
 *
 */
public class MaxquantDigestionModeToNameConverter implements Converter<String, DigestionMode> {
  private static final long serialVersionUID = 8425874280007470256L;

  HashMap<Integer, String> modeToName = new HashMap<Integer, String>();
  HashMap<String, Integer> nameToMode = new HashMap<String, Integer>();

  public MaxquantDigestionModeToNameConverter() {
    add(0, "Specific");
  }

  public void add(int mode, String name) {
    modeToName.put(mode, name);
    nameToMode.put(name, mode);
  }

  @Override
  public DigestionMode convertToModel(String value, Class<? extends DigestionMode> targetType,
      Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
    if (nameToMode.containsKey(value)) {
      return new DigestionMode(nameToMode.get(value));
    } else {
      throw new com.vaadin.data.util.converter.Converter.ConversionException(
          "Unknown digestion mode: " + value);
    }
  }

  @Override
  public String convertToPresentation(DigestionMode value, Class<? extends String> targetType,
      Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
    if (modeToName.containsKey(value.getValue())) {
      return modeToName.get(value.getValue());
    } else {
      throw new com.vaadin.data.util.converter.Converter.ConversionException(
          "Unknown digestion mode: " + value);
    }
  }

  @Override
  public Class<DigestionMode> getModelType() {
    return DigestionMode.class;
  }

  @Override
  public Class<String> getPresentationType() {
    // TODO Auto-generated method stub
    return String.class;
  }

}
