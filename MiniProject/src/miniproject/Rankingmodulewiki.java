/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miniproject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author priya
 */
public class Rankingmodulewiki {
    
    
    public static void Rankwordsinvocab(String dbname) throws SQLException, ClassNotFoundException
    {
        
        DecimalFormat df = new DecimalFormat("#.##");
        Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/"+dbname+"?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");  
                        
       Statement stat1 = conn.createStatement();
       Statement stat2 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       
       String query1,query2,key,keyword,text,linkedkeyword,pipelinkedkeyword; 
       int linked, mentions ;
       double ratio;
       query1 = "select  pageId, name from Page";
       query2 = "select text from Page";
       
       HashMap ranks = new HashMap<String,Double>();
       
       ResultSet rs1 = stat1.executeQuery(query1);
       ResultSet rs2;
       while(rs1.next())
       {
         key= rs1.getString(2);
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
           
       serialize(ranks);
       
       }
       
    
        public static void serialize(HashMap ranks)
    {	  
	   try{
	        FileOutputStream fout = new FileOutputStream("ranks.ser"); //("/home/priya/dev/galagosearch-read-only/dict/dict.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);   
		oos.writeObject(ranks);
		oos.close();
		System.out.println("Done");
 
	   }catch(Exception ex){
		   ex.printStackTrace();
	   }
   }
        
        public static HashMap deserialzeDict(){
 
	   HashMap ranks;
	   try{
 	       FileInputStream fin = new FileInputStream("ranks.ser"); //("/home/priya/dev/galagosearch-read-only/dict/dict.ser");
	       ObjectInputStream ois = new ObjectInputStream(fin);
	       ranks = (HashMap) ois.readObject();
	       ois.close();
 
	       return ranks;
 
	   }catch(Exception ex){
		   ex.printStackTrace();
		   return null;
	   } 
        }
       
       public static void main(String args[]) throws SQLException, ClassNotFoundException               
       {
           Rankwordsinvocab("hindiwiki");
           HashMap ranks = deserialzeDict();
           for (Object key : ranks.keySet()) {
		System.out.println("Key : " + key.toString() + " Value : "
			+ ranks.get(key));
	}
       }
       
       
    }
    
    
    
    

