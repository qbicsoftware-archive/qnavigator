package helpers;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.PopupView.Content;

public class ToolTip implements Content {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 11560196377958530L;
	VerticalLayout layout;
	
    public ToolTip(String content) {
    	layout = new VerticalLayout();
    	layout.addComponent(new Label(content, ContentMode.HTML));
	}
    		
    @Override
    public final Component getPopupComponent() {
        return layout;
    }

    @Override
    public final String getMinimizedValueAsHTML() {
        return FontAwesome.LINK.getHtml();
    }
    
    
};
