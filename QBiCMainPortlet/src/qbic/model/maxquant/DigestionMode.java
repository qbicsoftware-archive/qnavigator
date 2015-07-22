package qbic.model.maxquant;

import java.io.Serializable;

/**
 * Had problems with vaadin converters and nativeselect. This is used so that only the digestion
 * mode is converted but nothing else.
 * 
 * @author wojnar
 * 
 */
public class DigestionMode implements Serializable {
  private static final long serialVersionUID = -6145111851511617158L;

  int value;

  public DigestionMode(int v) {
    value = v;
  }

  void setValue(int v) {
    value = v;
  }

  int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public boolean equals(DigestionMode mode) {
    return this.value == mode.getValue();
  }

  public boolean equals(int v) {
    return this.value == v;
  }

}
