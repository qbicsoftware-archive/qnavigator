package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import de.uni_tuebingen.qbic.qbicmainportlet.HomeView;
import logging.Log4j2Logger;



public class DBManager {
  private DBConfig config;
  
  private logging.Logger LOGGER = new Log4j2Logger(HomeView.class);

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

  public void addProjectForPrincipalInvestigator(int pi_id, String projectCode) {
    String sql = "INSERT INTO projects (pi_id, project_code) VALUES(?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, pi_id);
      statement.setString(2, projectCode);
      statement.execute();
      // ResultSet rs = statement.getGeneratedKeys();
      // if (rs.next()) {
      // rs.getInt(1);
      // }
      // nothing will be in the database, until you commit it!
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
  }

  public String getInvestigatorForProject(String projectCode) {
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
      //statement.close();
      success = true;
      
    } catch (SQLException e) {
      e.printStackTrace();
    	//LOGGER.debug("Project not associated with Investigator. PI will be set to 'Unknown'");
    }
    
    String sql = "SELECT first_name, last_name FROM project_investigators WHERE pi_id=?";
    String fullName = "";
    
    if(success) {
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
    	statement.setString(1, id);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        fullName = first + " " + last;
      }
      //statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    }
    
    logout(conn);
    return fullName;
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
	      //statement.close();
	      success = true;
	      
	    } catch (SQLException e) {
	      e.printStackTrace();
	    	//LOGGER.debug("Project not associated with Investigator. PI will be set to 'Unknown'");
	    }
	    
	    String sql = "SELECT * FROM project_investigators WHERE pi_id=?";
	    String details = "";
	    
	    if(success) {
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

	        details = String.format("%s %s \n%s \n%s \n%s %s \n \n%s \n%s \n", first, last, institute, street, zipcode, city, phone, email);
	      }
	      //statement.close();
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    }
	    
	    logout(conn);
	    return details;
	  }

  public Map<Integer, String> getPrincipalInvestigatorsWithIDs() {
    String sql = "SELECT pi_id, first_name, last_name FROM project_investigators WHERE active = 1";
    Map<Integer, String> res = new HashMap<Integer, String>();
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        int pi_id = rs.getInt("pi_id");
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        res.put(pi_id, first + " " + last);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

}
