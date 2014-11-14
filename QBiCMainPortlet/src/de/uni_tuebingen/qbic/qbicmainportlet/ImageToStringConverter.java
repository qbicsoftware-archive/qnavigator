package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.Locale;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Image;

public class ImageToStringConverter
implements Converter<String, Image> {
private static final long serialVersionUID = -220989690852205509L;

@Override
public Class<Image> getModelType() {
 return Image.class;
}

@Override
public Class<String> getPresentationType() {
 return String.class;
}


@Override
public Image convertToModel(String value, Class<? extends Image> targetType, Locale locale)
    throws com.vaadin.data.util.converter.Converter.ConversionException {
  Image image = new Image();
  image.setCaption(value);
  return image;
}

@Override
public String convertToPresentation(Image value, Class<? extends String> targetType, Locale locale)
    throws com.vaadin.data.util.converter.Converter.ConversionException {
  return value.getCaption();
}
}
