/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miniproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import static miniproject.MiniProject.Html_clean;
import static miniproject.MiniProject.MatchNgramwithWikiHindi;
import static miniproject.MiniProject.findUniBigrams;
import static miniproject.MiniProject.getStopwords;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author priya
 */
public class MiniProjectwithRank {

    /**
     * @param args the command line arguments
     */
    
    
     private static List<String>  stopwords = new ArrayList<String>();
    
    public static void getStopwords(String filename) throws FileNotFoundException, IOException
    {
        
  //  try
    //{     
    BufferedReader reader = new BufferedReader(new FileReader(filename));    
    String line="";
     while (((line = reader.readLine()) != null)) { 
         
        if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			} 
     
        stopwords.add(line);        
        }
    
    reader.close();
        
    //}
    
    //catch(Exception e)
    //{
      //  System.out.println(e.getMessage());
    //}
    
    }
    public static void Html_clean(String urlstr, String input) throws FileNotFoundException, IOException
    {
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("clean/"+input+"clean"+".txt"));
        URL url = new URL(urlstr);     
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode rootNode = cleaner.clean(url);  
        
        TagNode divElements[] = rootNode.getElementsByName("body", true); //"div"
        for (int i = 0; divElements != null && i < divElements.length; i++)
        {
            writer.write(divElements[i].getText().toString());
        }
        
        writer.close();
          
       /* String line;
        BufferedReader reader = new BufferedReader(new FileReader(input+".txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("clean/"+input+"clean"+".txt"));
        while (((line = reader.readLine()) != null)) { 
             line = line.replaceAll("[^\\w\\s]", "");
           //  line = line.replaceAll("([0-9])+", "");
             writer.write(line);
             writer.write("\n");
             
        }
        reader.close();
      */
        
    }
    
       
    
    public static void findUniBigrams(String input) throws IOException
    {
         Runtime rt = Runtime.getRuntime();
         Process p = null;
         
         
          BufferedWriter writer = new BufferedWriter(new FileWriter("script.sh"));
          writer.write("rm grams/"+input+"grams.txt");
          writer.write("\n");
          writer.write("./ngramtool-20040527-linux-static/text2ngram -n1 -m5 clean/"+input+"clean.txt >> grams/"+input+"grams.txt");
          writer.close();
            try                                
       {
          
        p = rt.exec("./script.sh");
       // p = rt.exec("/home/priya/NetBeansProjects/MiniProject/ngramtool-20040527-linux-static/text2ngram -n1 -m2 clean/hindu6clean.txt >> grams/hindu6grams.txt");
           
          // p = rt.exec("/home/priya/ZGR/zgrviewer/run.sh --Pdot /home/priya/ZGR/Graphviz/graphs/Hospital.dot");
          // p = rt.exec( "java -jar /home/priya/ZGR/zgrviewer/target/zgrviewer-0.9.0.jar" );
                 

       }
       catch ( Exception e )
       {
           System.out.println( "Error executing file" );
           System.out.println(e.getMessage());
       }
            
    }
    
    
     public static void MatchNgramwithWikiHindi(String input) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
    {
        
       Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");  
                        
       Statement stat1 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       
        String fulltext = "", linkedtext = "";//= "<html><body>";
        String line1;
        BufferedReader reader1 = new BufferedReader(new FileReader(input+".html"));
        while (((line1 = reader1.readLine()) != null)) { 
        fulltext = fulltext+line1+"\n";
        
        }
        
        linkedtext = fulltext;
      //  fulltext= fulltext+"</html></body>";
        
        
        reader1.close();
       
        String line,url,repstr,finalngram;
        List<String> keywords = new ArrayList<String>();
        int ctr = 0;
         String title = "";
        BufferedReader reader = new BufferedReader(new FileReader("grams/"+input+"grams.txt"));
        while (((line = reader.readLine()) != null)) { 
           
          line = line.substring(0,line.lastIndexOf(" "));
         // 
          ctr++;
            try {            
                
             finalngram = line.replaceAll("([0-9])+", "");
            if (stopwords.contains(finalngram))
             {  
                System.out.println("Ignored ngram "+finalngram);
                continue; //ignore ngrams that are stopwords.
             }   
        //    title = line.replaceAll("[^\\w\\s]", "").replaceAll("([0-9])+", "").replace(" ","_" );                         
             title = line.replaceAll("([0-9])+", "").replace(" ","_" );             
             System.out.println(title);            
             String query1 = "select name  from hindiwiki.Page where name = \""+title+"\"";
             ResultSet rs1 = stat1.executeQuery(query1);
             if(rs1.next())
             {
               
                /* url = "https://hi.wikipedia.org/wiki/"+rs1.getString("name");
                 System.out.println(line+" "+url+" "+ctr);
                 repstr = "<a href=\""+url+"\">"+line+"</a>";
                 if(fulltext.contains(line))
                 { 
                     linkedtext = linkedtext.replaceAll(line, repstr);
                     fulltext = fulltext.replaceAll(line, "");
                 //fulltext = fulltext.replaceAll(line, repstr);
                 }
                 //*/
                 
                 keywords.add(title);
                 
                 
             }
                          
            }
          
              catch(Exception e)
             {
             System.out.println("Error is "+e.getMessage()+" "+e.toString());
             }  
        }
        reader.close();
        
       
           RankKeywords obj = new RankKeywords();
           obj.Rankwords("hindiwiki", keywords);
        
        
       
     /*  BufferedWriter writer1 = new BufferedWriter(new FileWriter("linked/"+input+".html"));
      // writer1.write(fulltext);
       writer1.write(linkedtext);
       writer1.close(); */
        
        
        
    }
    
    
    
    
    
       public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        // TODO code application logic here
           
            getStopwords("hindiST.txt");
        String urlstr = "file:///home/priya/NetBeansProjects/MiniProject/";
         Html_clean(urlstr+"NBT38.html", "NBT38");
         findUniBigrams("NBT38");
          MatchNgramwithWikiHindi("NBT38"); 
          
          
    }
}
