/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miniproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author priya
 */
public class FormVocabulary {

    /**
     * @param args the command line          
     */
    
    public static void formvocab() throws ClassNotFoundException, SQLException
    {
          try{
       Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");                          
       
       Statement stat1 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       int pageid;
       String redirname;
       String pagequery = "select id,name from Page  where id not in (select rd_from from redirect) and id > 38477"; //87927 rows
       ResultSet pages = stat1.executeQuery(pagequery);
       String insertquery = "insert into hindivocab (pageid,name,original) values (?,?,?)";
       PreparedStatement pst = conn.prepareStatement(insertquery);
       while(pages.next())
       {
           pageid = pages.getInt("id");
           String origname = pages.getString("name");
           System.out.println("Processing "+origname);
            try{
           pst.setInt(1, pageid);
           pst.setString(2, origname);
           pst.setString(3, "O");
           pst.executeUpdate();
           }
           catch(Exception ex)
            {
                System.out.println("Insert exception "+ex.getMessage());
            }
           
           
           String pageredirquery = "select id, redirects from page_redirects where id ="+pageid;
           ResultSet redirects = conn.createStatement().executeQuery(pageredirquery);
          
           
           while(redirects.next())
           {
             redirname = redirects.getString("redirects");
            try{ 
             pst.setInt(1, pageid);
             pst.setString(2, redirname);
             pst.setString(3, "S");
             pst.executeUpdate();
            }
            catch(Exception ex)
            {
                System.out.println("Insert exception "+ex.getMessage());
            }
           }
           
       origname = origname.replace('"', '\"');
      String pageotherredirq = "select rd_from, name from redirect, Page where rd_title = \""+origname+"\" and rd_from = pageId";
      ResultSet otherredirects = conn.createStatement().executeQuery(pageotherredirq);
      
         while(otherredirects.next())             
         {
            String otherredirname =  otherredirects.getString("name");
            try{
             pst.setInt(1, pageid);
             pst.setString(2, otherredirname);
             pst.setString(3, "S");
             pst.executeUpdate();
            }
            catch(Exception ex)
            {
                System.out.println("Insert exception "+ex.getMessage());
            }
             
         }
      
      
           
       }
       
       pages.close();
       
    }
    catch(Exception ex)
    {
        System.out.println(ex.getMessage());
    }
          
          
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // TODO code application logic here
        
        formvocab();
    }
}
