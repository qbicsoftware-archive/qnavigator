/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.Locale;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Image;

public class ImageToStringConverter implements Converter<String, Image> {
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
