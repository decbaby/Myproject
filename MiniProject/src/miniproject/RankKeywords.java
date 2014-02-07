/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miniproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static miniproject.Rankingmodulewiki.serialize;

/**
 *
 * @author priya
 */
public class RankKeywords {
    
    
    public Map<String, Double> Rankwords(String dbname, List<String> keywords) throws SQLException, ClassNotFoundException
    {
        
        DecimalFormat df = new DecimalFormat("#.##");
        Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/"+dbname+"?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");  
                        
       Statement stat1 = conn.createStatement();
       Statement stat2 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       
       String query1,query2,keyword,text,linkedkeyword,pipelinkedkeyword; 
       int linked, mentions ;
       double ratio;
      // query1 = "select  pageId, name from Page";
       query2 = "select text from Page";
       
       HashMap ranks = new HashMap<String,Double>();
       
       //ResultSet rs1 = stat1.executeQuery(query1);
       ResultSet rs2;
      // while(rs1.next())
       for(String key:keywords)
       {
        
         keyword = key.replace("_", " ");
         linkedkeyword= "[["+keyword+"]]";
         pipelinkedkeyword = "[["+keyword+"|";
         rs2 = stat2.executeQuery(query2);
         linked = 0;
         mentions = 0;
         while(rs2.next())
         {    
             int atIndex = 0;
             int count = 0; 
             int atlinkedIndex = 0;
             int linkedcount   = 0; 
             int atpipelinkedIndex = 0;
             int pipelinkedcount   = 0; 
             text = rs2.getString("text");
           //  System.out.println(text);
         while (atIndex != -1)
         {       
             atIndex = text.indexOf(keyword, atIndex);
            if(atIndex != -1)
           {
             count++;
             atIndex += keyword.length();
           }
         }
    
         if(count> 0)
         {                           
           //  System.out.println(text);
         while (atlinkedIndex != -1)
         {       
            atlinkedIndex = text.indexOf(linkedkeyword, atlinkedIndex);
            if(atlinkedIndex != -1)
           {
             linkedcount++;
             atlinkedIndex += linkedkeyword.length();
           }            
         }  //end of link while
         
         
          while (atpipelinkedIndex != -1)
         {       
            atpipelinkedIndex = text.indexOf(pipelinkedkeyword, atpipelinkedIndex);
            if(atpipelinkedIndex != -1)
           {
             pipelinkedcount++;
             atpipelinkedIndex += pipelinkedkeyword.length();
           }            
         }  //end of pipe link while
         
         
         } //end of if
         mentions = mentions + count;  
         linked = linked + linkedcount + pipelinkedcount;
         }
       System.out.println("Number of times "+keyword+" appeared is "+mentions);  
       System.out.println("Number of times "+keyword+" linked is "+linked);  
       ratio = 0.0;
       if (linked > 0)
           ratio = (linked*100000.00)/mentions;
       else 
           ratio = 0.0;
       
        System.out.print(ratio+"\n");
       
         ranks.put(key, ratio);
       }     
   
       printMap(ranks);
       
       Map<String, Double> sortedMap = sortByComparator(ranks);
       System.out.println("Ranks in Descending order");
		printMap(sortedMap);
                
       return sortedMap; //return sorted ranked list to calling program         
 
       }
    
    
    private static Map sortByComparator(Map unsortMap) {
 
		List list = new LinkedList(unsortMap.entrySet());
 
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
                                       .compareTo(((Map.Entry) (o1)).getValue());
			}
		});
 
		// put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
    
    
    public static void printMap(Map<String, Double> map){
		for (Map.Entry entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() 
                                   + " Value : " + entry.getValue());
		}
	}
 
    
    public static void main(String args[]) throws SQLException, ClassNotFoundException
    {
        RankKeywords obj = new RankKeywords();
        List<String> keywords = new ArrayList<String>();
        keywords.add("करीब");
        obj.Rankwords("hindiwiki", keywords);
                
    }
    
}
