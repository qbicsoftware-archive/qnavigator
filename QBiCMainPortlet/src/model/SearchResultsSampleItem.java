package model;
import java.util.Map;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

public class SearchResultsSampleItem extends CustomComponent {
	Sample sampleToView;

	public SearchResultsSampleItem(Sample sampleToView, int rowNumber) {
		this.sampleToView = sampleToView;

		Map<String, String> props = sampleToView.getProperties();
		
		HorizontalLayout rowContent = new HorizontalLayout();
		rowContent.setSpacing(true);
		Label itemNumber = new Label(Integer.toString(rowNumber) + ".");
		Label spacer1 = new Label();
		Label qbicCode = new Label(sampleToView.getCode());
		Label spacer2 = new Label();
		
		String sampleLabel;
		
		if (props.get("Q_SECONDARY_NAME") == null || props.get("Q_SECONDARY_NAME").equals("")) {
			sampleLabel = "no description";			
		}
		else {
			sampleLabel = props.get("Q_SECONDARY_NAME");
		}
		
		Label qbicLabel = new Label(sampleLabel);
		
		rowContent.addComponent(itemNumber);

		rowContent.addComponent(qbicCode);

		rowContent.addComponent(qbicLabel);



		setCompositionRoot(rowContent);

	}


}
