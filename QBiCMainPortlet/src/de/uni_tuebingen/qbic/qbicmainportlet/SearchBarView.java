package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import logging.Log4j2Logger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;


public class SearchBarView extends CustomComponent {


  /**
   * 
   */
  private static final long serialVersionUID = 5371970241077786446L;
  private logging.Logger LOGGER = new Log4j2Logger(SearchBarView.class);
  private Panel mainlayout;
  private DataHandler datahandler;
  private final String infotext =
      "This search box lets you search for qbic barcodes. If a barcode exits, comments/notes for that barcode will be displayed. You can as well add notes/comments to a barcode.";

  public SearchBarView(DataHandler datahandler) {
    this.datahandler = datahandler;
    initUI();
  }
  
  public void initUI() {
    mainlayout = new Panel();
    mainlayout.addStyleName(ValoTheme.PANEL_BORDERLESS);

    // static information for the user
    // Label info = new Label();
    // info.setValue(infotext);
    // info.setStyleName(ValoTheme.LABEL_LIGHT);
    // info.setStyleName(ValoTheme.LABEL_H4);
    //mainlayout.addComponent(info);

    // Search bar
    // *----------- search text field .... search button-----------*
    HorizontalLayout searchbar = new HorizontalLayout();
    searchbar.setSpacing(true);
    final ComboBox searchfield = new ComboBox();
    searchfield.setInputPrompt("search for sample");
    // TODO would be nice to have a autofill or something similar
    searchbar.addComponent(searchfield);
    Button searchOk = new Button("GoTo");
    searchOk.addStyleName(ValoTheme.BUTTON_BORDERLESS);
    searchOk.setIcon(FontAwesome.SEARCH);
    searchOk.addClickListener(new ClickListener() {
		private static final long serialVersionUID = -2409450448301908214L;

	@Override
      public void buttonClick(ClickEvent event) {    
        
        LOGGER.info("searching for sample: " + (String)searchfield.getValue());
        if (searchfield.getValue() == null || searchfield.getValue().toString().equals("")) {
          Notification.show("Please provide a valid Sample ID.", Type.WARNING_MESSAGE);
        }
        
        else {
        String entity = (String) searchfield.getValue().toString();

        Sample foundSample = datahandler.getOpenBisClient().getSampleByIdentifier(entity);
        String identifier = foundSample.getIdentifier();
        
        State state = (State) UI.getCurrent().getSession().getAttribute("state");
        ArrayList<String> message = new ArrayList<String>();
        message.add("clicked");
        message.add(identifier);
        message.add("sample");
        state.notifyObservers(message);
        }
      }
    });
    
    // setClickShortcut() would add global shortcut, instead we
    // 'scope' the shortcut to the panel:
    mainlayout.addAction(new com.vaadin.ui.Button.ClickShortcut(searchOk, KeyCode.ENTER));
    searchfield.addItems(this.getSearchResults("Q"));
    searchfield.setDescription(infotext);
    searchfield.addValidator(new NullValidator("Field must not be empty", false));
    searchfield.setValidationVisible(false);
    
    searchbar.addComponent(searchOk);
    //searchbar.setMargin(new MarginInfo(true, false, true, false));
    mainlayout.setContent(searchbar);
    //mainlayout.setComponentAlignment(searchbar, Alignment.MIDDLE_RIGHT);
    //mainlayout.setWidth(100, Unit.PERCENTAGE);
    setCompositionRoot(mainlayout);
  }
  
  public List<String> getSearchResults(String samplecode) {
    java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, samplecode + "*"));
    List<Sample> samples = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc, fetchOptions, LiferayAndVaadinUtils.getUser().getScreenName());
    List<String> ret = new ArrayList<String>(samples.size());
    for(Sample sample : samples){
      ret.add(sample.getCode());
    }
    return ret;
  }
}
