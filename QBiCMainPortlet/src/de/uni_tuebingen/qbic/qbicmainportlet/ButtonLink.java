package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/*
 * Link that looks like a Button, that is actually a Label Based on
 * https://vaadin.com/forum/#!/thread/69989
 */
public class ButtonLink extends Label {
  /**
   * 
   */
  private static final long serialVersionUID = 4654807493467264366L;
  String caption;

  /**
   * Link that looks like a Button, that is actually a Label. Set the caption of the "Button" and
   * the externalResource which should contain a url
   * 
   * @param caption
   * @param externalResource
   */
  public ButtonLink(String caption, ExternalResource externalResource) {
    super();
    super.setWidth(null);
    this.caption = caption;
    setResource(externalResource);
  }

  public void setResource(ExternalResource externalResource) {
    buildHTMLCode(externalResource.getURL());
    super.setContentMode(ContentMode.HTML);
  }
  
//The following lines are copy pasted from rendered Vaadin v6.1 buttons.
  private void buildHTMLCode(String url) {
    StringBuilder sb = new StringBuilder("<a href='");
    sb.append(url);
    sb.append("' style='text-decoration: display: block;'>");
    sb.append("<div class='v-button' tabindex='0' style='width: 100%;'>");
    sb.append("<span class='v-button-wrap'>");
    sb.append(caption);
    sb.append("</span>");
    sb.append("</div>");
    sb.append("</a>");
    super.setValue(sb.toString());
  }
}
