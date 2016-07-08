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
package controllers;

import logging.Log4j2Logger;
import logging.Logger;
import logging.SysOutLogger;
import model.notes.Note;
import model.notes.Notes;
import model.notes.ObjectFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import main.OpenBisClient;
import helpers.HistoryReader;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.data.util.BeanItemContainer;

import de.uni_tuebingen.qbic.qbicmainportlet.ProjectView;

public class MultiscaleController implements Serializable {
  
  private static Logger LOGGER = new Log4j2Logger(MultiscaleController.class);


  private OpenBisClient openbis;
  private JAXBElement<Notes> jaxbelem;
  private String user;
  private String currentSampleId;
  private String currentSampleCode;

  public MultiscaleController(OpenBisClient openbis, String user) {
    this.openbis = openbis;
    this.user = user;
  }

  // sample.getproperties("Q_NOTES");

  /**
   * 
   */
  private static final long serialVersionUID = -8194363636454560096L;

  public boolean isReady() {
    return jaxbelem != null && jaxbelem.getValue() != null;
  }

  public List<Note> getNotes() {
    return jaxbelem.getValue().getNote();
  }

  public boolean update(String sampleCode) {
    try {
      java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
      SearchCriteria sc = new SearchCriteria();
      sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sampleCode));
      List<Sample> samples = openbis.getOpenbisInfoService().searchForSamplesOnBehalfOfUser(openbis.getSessionToken(), sc, fetchOptions, user);
      if (samples != null && samples.size() == 1) {
        Sample sample = samples.get(0);
        String xml = sample.getProperties().get("Q_NOTES");
        if(xml != null){
          jaxbelem = helpers.HistoryReader.parseNotes(xml);
        }else{
          jaxbelem = new JAXBElement<Notes>(new QName(""),Notes.class, new Notes());
        }
        currentSampleCode = sample.getCode();
        currentSampleId = sample.getIdentifier();
        return true;
      }
    } catch (java.lang.IndexOutOfBoundsException | JAXBException | NullPointerException e ) {
      currentSampleId = null;
      currentSampleCode = null;
      // TODO change to logger
      System.out.println("error trhown");
      e.printStackTrace();
    }
    return false;

  }

  public BeanItemContainer<Note> getContainer() {
    BeanItemContainer<Note> container = new BeanItemContainer<Note>(Note.class);
    container.addAll(getNotes());
    return container;
  }

  public String getUser() {
    // TODO Auto-generated method stub
    return user;
  }

  public boolean addNote(Note note) {
    if(currentSampleId != null){
      jaxbelem.getValue().getNote().add(note);
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("id", currentSampleId);
      params.put("user", note.getUsername());
      params.put("comment", note.getComment());
      params.put("time", note.getTime());
      openbis.ingest("DSS1", "add-to-xml-note", params);
      return true;
    }
    return false;
  }

  public String getcurrentCode() {
    return currentSampleCode;
  }

  public List<String> getSearchResults(String samplecode) {
    java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, samplecode + "*"));
    List<Sample> samples = openbis.getOpenbisInfoService().searchForSamplesOnBehalfOfUser(openbis.getSessionToken(), sc, fetchOptions, user);
    List<String> ret = new ArrayList<String>(samples.size());
    for(Sample sample : samples){
      ret.add(sample.getCode());
    }
    return ret;
  }
  
  public String getLiferayUser(String userID) {
  Company company = null;
  long companyId = 1;
  String userString = "";
  try {
    String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
    company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
    companyId = company.getCompanyId();
    LOGGER.debug(String.format("Using webId %s and companyId %d to get Portal User", webId,
        companyId));
  } catch (PortalException | SystemException e) {
    LOGGER.error(
        "liferay error, could not retrieve companyId. Trying default companyId, which is "
            + companyId, e.getStackTrace());
  }

     User user = null;
      try {
        user = UserLocalServiceUtil.getUserByScreenName(companyId, userID);        
      } catch (PortalException | SystemException e) {
      }
      
      if (user == null) {
        LOGGER.warn(String.format("Openbis user %s appears to not exist in Portal", userID));
        userString = userID;
        // membersLayout.addComponent(new Label(member));
      } else {
        String fullname = user.getFullName();
        String email = user.getEmailAddress();
        // VaadinSession.getCurrent().getService();
        // ThemeDisplay themedisplay =
        // (ThemeDisplay)
        // VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
        // String url = user.getPortraitURL(themedisplay);
        // ExternalResource er = new ExternalResource(url);
        // com.vaadin.ui.Image image = new com.vaadin.ui.Image(user.getFullName(), er);
        // image.setHeight(80, Unit.PIXELS);
        // image.setWidth(65, Unit.PIXELS);
        // membersLayout.addComponent(image);
        // String labelString =
        // new String("<a href=\"mailto:" + email
        // + "\" style=\"color: #0068AA; text-decoration: none\">" + fullname + "</a>");
        // Label userLabel = new Label(labelString, ContentMode.HTML);
        // membersLayout.addComponent(userLabel);
        userString += ("<a href=\"mailto:");
        userString += (email);
        userString += ("\" style=\"color: #0068AA; text-decoration: none\">");
        userString += (fullname);
        userString += ("</a>");
      }
  return userString;
  }
}
