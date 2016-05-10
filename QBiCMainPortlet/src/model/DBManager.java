package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import logging.Log4j2Logger;
import model.userdb.Person;



public class DBManager {
  private DBConfig config;

  private logging.Logger LOGGER = new Log4j2Logger(DBManager.class);

  public DBManager(DBConfig config) {
    this.config = config;
  }

  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Connection login() {

    String DB_URL =
        "jdbc:mariadb://" + config.getHostname() + ":" + config.getPort() + "/"
            + config.getSql_database();

    Connection conn = null;

    try {
      Class.forName("org.mariadb.jdbc.Driver");
      conn = DriverManager.getConnection(DB_URL, config.getUsername(), config.getPassword());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return conn;
  }

  //
  // public void addProjectForPrincipalInvestigator(int pi_id, String projectCode) {
  // String sql = "INSERT INTO projects (pi_id, project_code) VALUES(?, ?)";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
  // {
  // statement.setInt(1, pi_id);
  // statement.setString(2, projectCode);
  // statement.execute();
  // // ResultSet rs = statement.getGeneratedKeys();
  // // if (rs.next()) {
  // // rs.getInt(1);
  // // }
  // // nothing will be in the database, until you commit it!
  // conn.commit();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  // logout(conn);
  // }

  public String getInvestigatorForProject(String projectIdentifier) {
    String details = getPersonDetailsForProject(projectIdentifier, "PI");
    return details.split("\n")[0].trim();
  }

  // TODO
  // should contain name, role for this project and some information for every person
  // public List<Person> getPersonsForProject(String projectIdentifier) {
  //
  // }

  public List<Person> getPersonWithAffiliations(Integer personID) {
    List<Person> res = new ArrayList<Person>();
    String lnk = "persons_organizations";
    String sql =
        "SELECT persons.*, organizations.*, " + lnk + ".occupation FROM persons, organizations, "
            + lnk + " WHERE persons.id = " + Integer.toString(personID) + " AND persons.id = "
            + lnk + ".person_id and organizations.id = " + lnk + ".organization_id";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String title = rs.getString("title");
        String first = rs.getString("first_name");
        String last = rs.getString("family_name");
        String eMail = rs.getString("email");
        String phone = rs.getString("phone");


        int affiliationID = rs.getInt("organizations.id");

        String group_acronym = rs.getString("group_acronym");
        String group_name = rs.getString("group_name");
        String institute = rs.getString("institute");
        String organization = rs.getString("umbrella_organization");
        String affiliation = "";

        if (group_name == null | group_name.toUpperCase().equals("NULL") | group_name.equals("")) {

          if (institute == null | institute.toUpperCase().equals("NULL") | institute.equals("")) {
            affiliation = organization;
          } else {
            affiliation = institute;
          }

        } else {
          affiliation = group_name + " (" + group_acronym + ")";
        }

        String role = rs.getString(lnk + ".occupation");
        res.add(new Person(id, username, title, first, last, eMail, phone, affiliationID,
            affiliation, role));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public String getPersonDetailsForProject(String projectIdentifier, String role) {
    String sql =
        "SELECT projects_persons.*, projects.* FROM projects_persons, projects WHERE projects.openbis_project_identifier = ?"
            + " AND projects.id = projects_persons.project_id AND projects_persons.project_role = ?";

    int id = -1;

    List<Person> personWithAffiliations = new ArrayList<Person>();

    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, projectIdentifier);
      statement.setString(2, role);

      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        id = rs.getInt("person_id");
      }
      personWithAffiliations = getPersonWithAffiliations(id);
    } catch (SQLException e) {
      e.printStackTrace();
      logout(conn);
      // LOGGER.debug("Project not associated with Investigator. PI will be set to 'Unknown'");
    }

    String details = "";
    if (personWithAffiliations.size() > 0) {
      Person p = personWithAffiliations.get(0);
      String institute = p.getOneAffiliationWithRole().getAffiliation();

      details =
          String.format("%s %s \n%s \n \n%s \n%s \n", p.getFirst(), p.getLast(), institute,
              p.getPhone(), p.geteMail());
      // TODO is address important?
    }

    logout(conn);
    return details;
  }

  public String getInvestigatorDetailsForProject(String projectCode) {
    String id_query = "SELECT pi_id FROM projects WHERE project_code=?";
    String id = "";
    Boolean success = false;

    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(id_query)) {
      statement.setString(1, projectCode);

      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        id = Integer.toString(rs.getInt("pi_id"));
      }
      // statement.close();
      success = true;

    } catch (SQLException e) {
      e.printStackTrace();
      // LOGGER.debug("Project not associated with Investigator. PI will be set to 'Unknown'");
    }

    String sql = "SELECT * FROM project_investigators WHERE pi_id=?";
    String details = "";

    if (success) {
      try (PreparedStatement statement = conn.prepareStatement(sql)) {
        statement.setString(1, id);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
          String first = rs.getString("first_name");
          String last = rs.getString("last_name");
          String email = rs.getString("email");
          String phone = rs.getString("phone");
          String zipcode = rs.getString("zip_code");
          String city = rs.getString("city");
          String street = rs.getString("street");
          String institute = rs.getString("institute");

          details =
              String.format("%s %s \n%s \n%s \n%s %s \n \n%s \n%s \n", first, last, institute,
                  street, zipcode, city, phone, email);
        }
        // statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    logout(conn);
    return details;
  }

  public String getProjectName(String projectIdentifier) {
    String sql = "SELECT short_title from projects WHERE openbis_project_identifier = ?";
    String res = "";
    Connection conn = login();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, projectIdentifier);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        res = rs.getString(1);
      }
    } catch (SQLException e) {
      LOGGER.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  // public Map<Integer, String> getPrincipalInvestigatorsWithIDs() {
  // String sql = "SELECT pi_id, first_name, last_name FROM project_investigators WHERE active = 1";
  // Map<Integer, String> res = new HashMap<Integer, String>();
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql)) {
  // ResultSet rs = statement.executeQuery();
  // while (rs.next()) {
  // int pi_id = rs.getInt("pi_id");
  // String first = rs.getString("first_name");
  // String last = rs.getString("last_name");
  // res.put(pi_id, first + " " + last);
  // }
  // statement.close();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  // logout(conn);
  // return res;
  // }

}
