package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logging.Log4j2Logger;
import model.SampleBean;
import model.SearchResultsExperimentBean;
import model.SearchResultsSampleBean;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FetchOption;

import com.liferay.portal.kernel.search.messaging.SearchReaderMessageListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;



public class SearchEngineView extends CustomComponent {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5371970241077786446L;
	private logging.Logger LOGGER = new Log4j2Logger(SearchEngineView.class);
	private Panel mainlayout;
	private DataHandler datahandler;
	private final String infotext =
			"This search box lets you search for qbic barcodes. If a barcode exits, comments/notes for that barcode will be displayed. You can as well add notes/comments to a barcode.";

	Map<String, Map<String, String> > samplePropertiesMapping;


	public SearchEngineView(DataHandler datahandler) {
		this.datahandler = datahandler;
		initSearchEngine();
		initUI();
	}

	private void initSearchEngine() {
		// TODO Auto-generated method stub


		// retrieve all relevant sample type search fields (properties/attributes)
		Map<String, SampleType> sampleTypeMap = datahandler.getOpenBisClient().getSampleTypes();

		for (String key : sampleTypeMap.keySet()) {
			//LOGGER.info(key);
			Map<String, String> props = datahandler.getOpenBisClient().getLabelsofProperties(sampleTypeMap.get(key));
			//LOGGER.info(props.toString());
			//samplePropertiesMapping.put(key, props);
		}

	}

	public void initUI() {

		mainlayout = new Panel();
		mainlayout.addStyleName(ValoTheme.PANEL_BORDERLESS);

		// Search bar
		// *----------- search text field .... search button-----------*
		HorizontalLayout searchbar = new HorizontalLayout();

		VerticalLayout searchFieldLayout = new VerticalLayout();

		searchbar.setSpacing(true);
		final TextField searchfield = new TextField();
		searchfield.setHeight("44px");
		searchfield.setImmediate(true);

		searchfield.setInputPrompt("search DB");
		//searchfield.setCaption("QSearch");
		searchfield.setWidth(20.0f, Unit.EM);

		// TODO would be nice to have a autofill or something similar
		searchFieldLayout.addComponent(searchfield);

		NativeSelect navsel = new NativeSelect();
		navsel.addItem("Whole DB");
		navsel.addItem("Experiments Only");
		navsel.addItem("Samples Only");
		navsel.setValue("Whole DB");
		navsel.setWidth(10.0f, Unit.EM);
		navsel.setNullSelectionAllowed(false);
		searchFieldLayout.addComponent(navsel);

		searchbar.addComponent(searchFieldLayout);




		Button searchOk = new Button("Search");
		//searchOk.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		searchOk.setIcon(FontAwesome.SEARCH);
		searchOk.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -2409450448301908214L;
			

			@Override
			public void buttonClick(ClickEvent event) {
				String queryString = (String) searchfield.getValue().toString();

				LOGGER.debug("the query was " + queryString);

				if (searchfield.getValue() == null || searchfield.getValue().toString().equals("") || searchfield.getValue().toString().trim().length() == 0) {
					Notification.show(
							"Query field was empty!",
							Type.WARNING_MESSAGE);
				}
				else {

					try {
						/**		Sample foundSample = datahandler.getOpenBisClient()
									.getSampleByIdentifier(
											matcher.group(0).toString()); */

						datahandler.setSampleResults(querySamples(queryString));
						datahandler.setExpResults(queryExperiments(queryString));
						datahandler.setLastQueryString(queryString);


						State state = (State) UI.getCurrent().getSession()
								.getAttribute("state");
						ArrayList<String> message = new ArrayList<String>();
						message.add("clicked");
						message.add("view");
						message.add("searchresults");
						state.notifyObservers(message);

//						Window window = new Window("Search results for '" + queryString + "':");
//						window.center();
//						window.setWidth("80%");
//						VerticalLayout winContent = new VerticalLayout();
//						winContent.setMargin(true);
//						winContent.setSpacing(true);



						

						

						//Grid testGrid = new Grid();

						//int rowNumber = 1;



						
						//System.out.println(beanContainer.getContainerPropertyIds());
						



//						window.setContent(winContent);
//						UI.getCurrent().addWindow(window);

						//datahandler.getOpenBisClient().getLabelsofProperties(test);

						//String identifier = foundSample.getIdentifier();

						String identifier = new String();



					} catch (Exception e) {
						LOGGER.error("after query: ", e);
						Notification.show(
								"No Sample found for given barcode.",
								Type.WARNING_MESSAGE);
					}
				}


			}

		});

		// setClickShortcut() would add global shortcut, instead we
		// 'scope' the shortcut to the panel:
		mainlayout.addAction(new com.vaadin.ui.Button.ClickShortcut(searchOk, KeyCode.ENTER));
		//searchfield.addItems(this.getSearchResults("Q"));
		searchfield.setDescription(infotext);
		searchfield.addValidator(new NullValidator("Field must not be empty", false));
		searchfield.setValidationVisible(false);

		searchbar.addComponent(searchOk);
		//searchbar.setComponentAlignment(searchOk, Alignment.MIDDLE_CENTER);
		//searchbar.setMargin(new MarginInfo(true, false, true, false));
		mainlayout.setContent(searchbar);
		//mainlayout.setComponentAlignment(searchbar, Alignment.MIDDLE_RIGHT);
		//mainlayout.setWidth(100, Unit.PERCENTAGE);
		setCompositionRoot(mainlayout);
	}

	public List<Sample> querySamples(String queryString) {
		EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
		// SearchCriteria sc1 = new SearchCriteria();
		// sc1.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, queryString));

		SearchCriteria sc2 = new SearchCriteria();
		sc2.addMatchClause(MatchClause.createAnyFieldMatch(queryString));

		//List<Sample> samples1 = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc1, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());

		List<Sample> samples2 = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc2, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());

		//LOGGER.info("hits: " + samples1.size() + " " + samples2.size());

		return samples2;
	}


	public List<Experiment> queryExperiments(String queryString) {
		EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
		// SearchCriteria sc1 = new SearchCriteria();
		// sc1.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, queryString));

		SearchCriteria sc2 = new SearchCriteria();
		sc2.addMatchClause(MatchClause.createAnyFieldMatch(queryString));

		//List<Sample> samples1 = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc1, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());

		//List<Sample> samples2 = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc2, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());
		List<Experiment> exps = datahandler.getOpenBisClient().getOpenbisInfoService().searchForExperiments(datahandler.getOpenBisClient().getSessionToken(), sc2);

		//LOGGER.info("hits: " + samples1.size() + " " + samples2.size());

		return exps;
	}



	public List<String> getSearchResults(String samplecode) {
		java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
		SearchCriteria sc = new SearchCriteria();
		sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, samplecode + "*"));
		
		List<Sample> samples = datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());
		List<String> ret = new ArrayList<String>(samples.size());
		for(Sample sample : samples){
			ret.add(sample.getCode());
		}
		return ret;
	}
}
