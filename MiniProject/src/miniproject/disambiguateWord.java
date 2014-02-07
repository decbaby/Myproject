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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author priya
 */
public class disambiguateWord {
    
    
    
    public void disambiguateWordsfunc(List<String> topntitles) throws ClassNotFoundException, SQLException
    {
        try{
       Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");  
                        
       Statement stat1 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       //String linkstocompare = "";
       //First extract links of all top titles in news story 
       for(String toptitle: topntitles)
       {
           
            String ctrquery,text,linkeditem,insertquery;
            query = "select pageId, name, text from Page where name = \""+toptitle+"\"";
            insertquery = "insert into linkages (name, link) values (?,?)";
            ResultSet rs = stat1.executeQuery(query);
            
            while(rs.next())
            {
                int linkedIndexst = 0;
                int linkedIndexend = 0;
                
                text= rs.getString("text");
                String fulltitle = rs.getString("name");
                System.out.println(fulltitle);
                 //System.out.println(text);
                PreparedStatement insertpst = conn.prepareStatement(insertquery);
                
           while(linkedIndexst != -1)
             {       
              linkedIndexst = text.indexOf("[[", linkedIndexst);
              linkedIndexend = text.indexOf("]]", linkedIndexst);
              
              if(linkedIndexst != -1 && linkedIndexend != -1 )
              {   
                 linkeditem = text.substring(linkedIndexst+2, linkedIndexend);
                  try
              {    
              insertpst.setString(1, fulltitle);
              insertpst.setString(2, linkeditem);
              insertpst.executeUpdate();
         //     linkstocompare = linkstocompare+" "+linkeditem;
              }
              
              catch(Exception sqlex)
              {
                  System.out.println("Primary key constraint violated "+fulltitle+" "+linkeditem);
              }
                  
              }
            if(linkedIndexst != -1)
           {             
             linkedIndexst += "[[".length();
           }     
            
              if(linkedIndexend != -1)
           {             
             linkedIndexend += "]]".length();
           } 
          
             
         } //end of while  
     }  //end of while            
} //end of for
       
       
       for(String toptitle: topntitles)
       {
           
            String linkstocompare = "";
           for(String othertitle: topntitles)
           {
             if(othertitle.equals(toptitle))  
                 continue;
              String comparequery = "select link from linkages where name = \""+othertitle+"\""; 
              ResultSet comparers = stat1.executeQuery(comparequery);
             
              while(comparers.next())
              {
                  linkstocompare = linkstocompare + comparers.getString(1);
              }
              
           }
           
           
           
           String ctrquery,text,linkeditem,insertquery;
           int ctr ;
           ctrquery = "select count(*) from Page where name like ?";
           PreparedStatement pstmt = conn.prepareStatement(ctrquery);
           pstmt.setString(1, "%"+toptitle+"%");
           ResultSet prs = pstmt.executeQuery();
           prs.next();
           ctr = prs.getInt(1);
           if (ctr > 1)
           {                                   
               
            System.out.println("disambiguating "+toptitle+"occurences= "+ctr);
           // System.out.println("Links to compare = "+linkstocompare);
            query = "select name, text from Page where name like ?";
            insertquery = "insert into linkages (name, link) values (?,?)";
            PreparedStatement allpstmt = conn.prepareStatement(query);
            allpstmt.setString(1, "%"+toptitle+"%");
            ResultSet rs = allpstmt.executeQuery();
            int highestmatch = 0;
            String bestmatch = toptitle;
            while(rs.next())
            {
                int linkedIndexst = 0;
                int linkedIndexend = 0;
                
                text= rs.getString("text");
                String fulltitle = rs.getString("name");
               // if(fulltitle.equals(toptitle)) //we have already extracted links for exact match
                 //   continue;
                
                 System.out.println("Processing Full title  "+fulltitle);
               //  System.out.println(text);
                 PreparedStatement insertpst = conn.prepareStatement(insertquery);
                 int match = 0;
           while(linkedIndexst != -1)
             {       
              linkedIndexst = text.indexOf("[[", linkedIndexst);
              //linkedIndexend = text.indexOf("]]", linkedIndexend);
              linkedIndexend = text.indexOf("]]", linkedIndexst);
              if(linkedIndexst != -1 && linkedIndexend != -1)
              {   
                 linkeditem = text.substring(linkedIndexst+2, linkedIndexend);
                 if(linkstocompare.contains(linkeditem))
              {
                  match = match + 1;
              }
                 
                  try
              {    
              insertpst.setString(1, fulltitle);
              insertpst.setString(2, linkeditem);
              insertpst.executeUpdate();              
              }
              
              catch(Exception sqlex)
              {
                  System.out.println("Primary key constraint violated "+fulltitle+" "+linkeditem);
              }
                  
              }
            if(linkedIndexst != -1)
           {             
             linkedIndexst += "[[".length();
           }     
            
              if(linkedIndexend != -1)
           {             
             linkedIndexend += "]]".length();
           } 
           
             
              
             
         } //end of while  
           
            if(highestmatch < match)
              {
                  highestmatch = match;
                  bestmatch = fulltitle;
              }
           
           
       }  //end of while
            
      System.out.println("Best match of "+toptitle +"is "+bestmatch+" no of matches = "+highestmatch); 
      rs.close();        
    }  //end of if
           else if(ctr == 1)
           {
                System.out.println("Best match of "+toptitle +"is "+toptitle);
           }  
           
           
           
 } //end of for
       
       
       
       
} //end of try
    catch(Exception ex)
              {
                System.out.println(ex.getMessage());
              }    
        
        
        
    } //end of func
    
    
}//end of class
