package de.uni_tuebingen.qbic.qbicmainportlet;

import static com.google.common.truth.Truth.ASSERT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMultiScaleComponent {

	 @BeforeClass
	  public static void setUpBeforeClass() throws Exception {}

	  @AfterClass
	  public static void tearDownAfterClass() throws Exception {}

	  @Before
	  public void setUp() throws Exception {}
	  
	  @Before
	  public void tearDown() throws Exception {}	  
	
	  
	  @Test
	  public void test(){
	      Date d = new Date(115,7,3,10,43,29);
	      System.out.println(d);
	      SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	      String date = ft.format(d);
	      System.out.println(date);
	      String [] datetime = date.split("T");
	      String day = datetime[0];
	      String time = datetime[1].split("\\.")[0];
	      
	      ASSERT.that(day).comparesEqualTo("2015-08-03");
	      ASSERT.that(time).comparesEqualTo("10:43:29"); 
	      
	      
	       d = new Date(115,7,21,9,43,29);
	      System.out.println(d);
	       ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	       date = ft.format(d);
	      System.out.println(date);
	       datetime = date.split("T");
	       day = datetime[0];
	       time = datetime[1].split("\\.")[0];
	      
	      ASSERT.that(day).comparesEqualTo("2015-08-21");
	      ASSERT.that(time).comparesEqualTo("09:43:29"); 	      

	       d = new Date(115,11,21,23,00,00);
	      System.out.println(d);
	       ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	       date = ft.format(d);
	      System.out.println(date);
	       datetime = date.split("T");
	       day = datetime[0];
	       time = datetime[1].split("\\.")[0];
	      
	      ASSERT.that(day).comparesEqualTo("2015-12-21");
	      ASSERT.that(time).comparesEqualTo("23:00:00"); 	 	      
	      
	      
		  
	  }
}
