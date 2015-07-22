package qbic.model.maxquant;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;

/**
 * According to this https://vaadin.com/forum#!/thread/8216422 The Converter (in this case
 * MaxquantDigestionModeToNameConverter) will not call for Native select by defaut So one has to
 * create a Factory and include it into VaadinSession via
 * VaadinSession.getCurrent().setConverterFactory( new MaxquantConverterFactory()); this is true for
 * vaadin 7.5
 * 
 * @author wojnar
 * 
 */
public class MaxquantConverterFactory extends DefaultConverterFactory {
  private static final long serialVersionUID = 6988206501368446880L;

  @SuppressWarnings("unchecked")
  @Override
  public <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> createConverter(
      Class<PRESENTATION> presentationType, Class<MODEL> modelType) {
    // Handle one particular type conversion
    if ((String.class == presentationType || Object.class == presentationType)
        && DigestionMode.class == modelType)
      return (Converter<PRESENTATION, MODEL>) new MaxquantDigestionModeToNameConverter();

    // Default to the supertype
    return super.createConverter(presentationType, modelType);
  }
}
