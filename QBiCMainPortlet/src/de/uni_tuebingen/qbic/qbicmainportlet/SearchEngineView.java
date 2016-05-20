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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import logging.Log4j2Logger;



public class SearchEngineView extends CustomComponent {


  /**
   * 
   */
  private static final long serialVersionUID = 5371970241077786446L;
  private logging.Logger LOGGER = new Log4j2Logger(SearchEngineView.class);
  private Panel mainlayout;
  private DataHandler datahandler;
  private final String infotext = "no info text";

  Map<String, Map<String, String>> samplePropertiesMapping;

  Set<String> visibleSpaces;

  public SearchEngineView(DataHandler datahandler) {
    this.datahandler = datahandler;
    this.visibleSpaces = listSpacesOnBehalfOfUser();
    initSearchEngine();
    initUI();
  }

  private void initSearchEngine() {
    // TODO Auto-generated method stub


    // retrieve all relevant sample type search fields (properties/attributes)
    Map<String, SampleType> sampleTypeMap = datahandler.getOpenBisClient().getSampleTypes();

    for (String key : sampleTypeMap.keySet()) {
      // LOGGER.info(key);
      Map<String, String> props =
          datahandler.getOpenBisClient().getLabelsofProperties(sampleTypeMap.get(key));
      // LOGGER.info(props.toString());
      // samplePropertiesMapping.put(key, props);
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
    // searchfield.setCaption("QSearch");
    searchfield.setWidth(30.0f, Unit.EM);

    // TODO would be nice to have a autofill or something similar
    searchFieldLayout.addComponent(searchfield);

    final NativeSelect navsel = new NativeSelect();
    navsel.addItem("Whole DB");
    navsel.addItem("Projects Only");
    navsel.addItem("Experiments Only");
    navsel.addItem("Samples Only");
    navsel.setValue("Whole DB");
    navsel.setWidth(10.0f, Unit.EM);
    navsel.setNullSelectionAllowed(false);

    navsel.addValueChangeListener(new Property.ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6896454887050432147L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // TODO Auto-generated method stub
        Notification.show((String) navsel.getValue());

        switch ((String) navsel.getValue()) {
          case "Whole DB":
            datahandler.setShowOptions(Arrays.asList("Projects", "Experiments", "Samples"));
            break;
          case "Projects Only":
            datahandler.setShowOptions(Arrays.asList("Projects"));
            break;
          case "Experiments Only":
            datahandler.setShowOptions(Arrays.asList("Experiments"));
            break;
          case "Samples Only":
            datahandler.setShowOptions(Arrays.asList("Samples"));
            break;
          default:
            datahandler.setShowOptions(Arrays.asList("Projects", "Experiments", "Samples"));
            break;
        }


      }
    });

    searchFieldLayout.addComponent(navsel);

    searchbar.addComponent(searchFieldLayout);



    Button searchOk = new Button("Search");
    // searchOk.addStyleName(ValoTheme.BUTTON_BORDERLESS);
    searchOk.setIcon(FontAwesome.SEARCH);
    searchOk.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -2409450448301908214L;


      @Override
      public void buttonClick(ClickEvent event) {
        String queryString = (String) searchfield.getValue().toString();

        // LOGGER.debug("the query was " + queryString);

        if (searchfield.getValue() == null || searchfield.getValue().toString().equals("")
            || searchfield.getValue().toString().trim().length() == 0) {
          Notification.show("Query field was empty!", Type.WARNING_MESSAGE);
        } else {

          try {
            /**
             * Sample foundSample = datahandler.getOpenBisClient() .getSampleByIdentifier(
             * matcher.group(0).toString());
             */

            datahandler.setSampleResults(querySamples(queryString));
            datahandler.setExpResults(queryExperiments(queryString));
            datahandler.setProjResults(queryProjects(queryString));
            datahandler.setLastQueryString(queryString);


            State state = (State) UI.getCurrent().getSession().getAttribute("state");
            ArrayList<String> message = new ArrayList<String>();
            message.add("clicked");
            message.add("view");
            message.add("searchresults");
            state.notifyObservers(message);

          } catch (Exception e) {
            LOGGER.error("after query: ", e);
            Notification.show("No Sample found for given barcode.", Type.WARNING_MESSAGE);
          }
        }


      }

    });

    // setClickShortcut() would add global shortcut, instead we
    // 'scope' the shortcut to the panel:
    mainlayout.addAction(new com.vaadin.ui.Button.ClickShortcut(searchOk, KeyCode.ENTER));
    // searchfield.addItems(this.getSearchResults("Q"));
    searchfield.setDescription(infotext);
    searchfield.addValidator(new NullValidator("Field must not be empty", false));
    searchfield.setValidationVisible(false);

    searchbar.addComponent(searchOk);
    // searchbar.setComponentAlignment(searchOk, Alignment.MIDDLE_CENTER);
    // searchbar.setMargin(new MarginInfo(true, false, true, false));
    mainlayout.setContent(searchbar);
    // mainlayout.setComponentAlignment(searchbar, Alignment.MIDDLE_RIGHT);
    // mainlayout.setWidth(100, Unit.PERCENTAGE);
    setCompositionRoot(mainlayout);
  }

  public List<Sample> querySamples(String queryString) {
    EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
    // SearchCriteria sc1 = new SearchCriteria();
    // sc1.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, queryString));

    SearchCriteria sc2 = new SearchCriteria();
    sc2.addMatchClause(MatchClause.createAnyFieldMatch(queryString));

    // List<Sample> samples1 =
    // datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(),
    // sc1, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());

    List<Sample> samples2 = datahandler.getOpenBisClient().getOpenbisInfoService()
        .searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc2,
            fetchOptions, LiferayAndVaadinUtils.getUser().getScreenName());

    // LOGGER.info("hits: " + samples1.size() + " " + samples2.size());

    return samples2;
  }



  public List<Experiment> queryExperiments(String queryString) {
    EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
    // SearchCriteria sc1 = new SearchCriteria();
    // sc1.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, queryString));

    SearchCriteria sc2 = new SearchCriteria();
    sc2.addMatchClause(MatchClause.createAnyFieldMatch(queryString));

    // List<Sample> samples1 =
    // datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(),
    // sc1, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());

    // List<Sample> samples2 =
    // datahandler.getOpenBisClient().getOpenbisInfoService().searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(),
    // sc2, fetchOptions,LiferayAndVaadinUtils.getUser().getScreenName());
    List<Experiment> exps = datahandler.getOpenBisClient().getOpenbisInfoService()
        .searchForExperiments(datahandler.getOpenBisClient().getSessionToken(), sc2);


    List<Experiment> expFilteredByUser = new ArrayList<Experiment>();


    for (Experiment e : exps) {
      String[] splitstr = e.getIdentifier().split("/");
      String spaceCode = splitstr[1];

      if (visibleSpaces.contains(spaceCode)) {
        expFilteredByUser.add(e);
      }
    }

    // LOGGER.info("hits: " + samples1.size() + " " + samples2.size());

    return expFilteredByUser;
  }

  public List<Project> queryProjects(String queryString) {
    List<Project> result = new ArrayList<Project>();

    List<Project> projects =
        new ArrayList<Project>(datahandler.getOpenBisClient().getOpenbisInfoService()
            .listProjectsOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(),
                LiferayAndVaadinUtils.getUser().getScreenName()));

    for (Project p : projects) {
      String projectDesc = p.getDescription();

      // sometimes there is no description available (check for null entry)
      if (projectDesc == null) {
        continue;

      } else {
        projectDesc = projectDesc.toLowerCase();

        if (projectDesc.contains(queryString.toLowerCase())) {
          result.add(p);
        }
      }
    }

    return result;
  }


  public Set<String> listSpacesOnBehalfOfUser() {
    // first retrieve all projects belonging to the user
    List<Project> projects =
        new ArrayList<Project>(datahandler.getOpenBisClient().getOpenbisInfoService()
            .listProjectsOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(),
                LiferayAndVaadinUtils.getUser().getScreenName()));

    Set<String> resultSpaceNames = new HashSet<String>();

    for (Project p : projects) {
      resultSpaceNames.add(p.getSpaceCode());
      // LOGGER.info(p.getSpaceCode());
    }

    // LOGGER.info(resultSpaceNames.toString());

    return resultSpaceNames;
  }


  public List<String> getSearchResults(String samplecode) {
    java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, samplecode + "*"));

    List<Sample> samples = datahandler.getOpenBisClient().getOpenbisInfoService()
        .searchForSamplesOnBehalfOfUser(datahandler.getOpenBisClient().getSessionToken(), sc,
            fetchOptions, LiferayAndVaadinUtils.getUser().getScreenName());
    List<String> ret = new ArrayList<String>(samples.size());
    for (Sample sample : samples) {
      ret.add(sample.getCode());
    }
    return ret;
  }
}
