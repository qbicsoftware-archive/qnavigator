package de.uni_tuebingen.qbic.qbicmainportlet;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Label;
/*
 * Link that looks like a Button, that is actually a Label
 * Based on https://vaadin.com/forum/#!/thread/69989
 */
public class ButtonLink extends Label {
    String caption;
	public ButtonLink(String caption, ExternalResource externalResource) {
		super(
            "<a href='"+externalResource.getURL()+"' style='text-decoration: display: block;'>" +
                // The following lines are copy pasted from rendered Vaadin v6.1 buttons.
                "<div class='v-button' tabindex='0' style='width: 100%;'>" +
                  "<span class='v-button-wrap'>" +
                    "<span class='v-button-caption'>"+
                      caption +
                    "</span>"+
                  "</span>"+
                "</div>"+
              "</a>",
              Label.CONTENT_XHTML);
        super.setWidth(null);
        this.caption = caption;
    }
    
    public void setResource(ExternalResource externalResource){
    	super.setValue(            "<a href='"+externalResource.getURL()+"' style='text-decoration: display: block;'>" +
                // The following lines are copy pasted from rendered Vaadin v6.1 buttons.
                "<div class='v-button' tabindex='0' style='width: 100%;'>" +
                  "<span class='v-button-wrap'>" +
                    "<span class='v-button-caption'>"+
                      caption +
                    "</span>"+
                  "</span>"+
                "</div>"+
              "</a>");
    	super.setContentMode(Label.CONTENT_XHTML);
    }
}