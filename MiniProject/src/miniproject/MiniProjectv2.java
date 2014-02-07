/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * This program does  maximal n-gram matching. N-grams are sorted as per length
 * hence longer n-grams are processed first and thus if a smaller n-gram is encountered later that
 * overlaps the already processed longer n-gram then smaller n-gram is not linked
 * Also it provides the maximal n-grams that are linked to the Ranking module for ranking
 */
package miniproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import static miniproject.MiniProjectv1.Html_clean;
import static miniproject.MiniProjectv1.MatchNgramwithWikiHindi;
import static miniproject.MiniProjectv1.findUniBigrams;
import static miniproject.MiniProjectv1.getStopwords;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author priya
 */
public class MiniProjectv2 {

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
     
        stopwords.add(line.trim());        
        }
    
    reader.close();
    
    }
    public static void Html_clean(String urlstr, String input) throws FileNotFoundException, IOException
    {
        System.out.print("Came in Html clean");
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
        
        System.out.println("Came in find grams function");
         Runtime rt = Runtime.getRuntime();
         Process p = null;
         
         
          BufferedWriter writer = new BufferedWriter(new FileWriter("script.sh"));
          writer.write("rm grams/"+input+"grams.txt");
          writer.write("\n");
          writer.write("G:/NetbeansProjects/MiniProject/ngramtool-20040527-mingw32-static/text2ngram -n1 -m5 clean/"+input+"clean.txt >> grams/"+input+"grams.txt");
          writer.close();
            try                                
       {
          
        p = rt.exec("./script.sh");       

       }
            
       catch ( Exception e )
       {
           System.out.println( "Error executing file" );
           System.out.println("Exception in finding grams"+e.getMessage());
       }
            System.out.println("script written");
            
    }
    
    
     
       public static void MatchNgramwithWikiHindi(String input) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
    {
        try{
       Class.forName("com.mysql.jdbc.Driver");  
       Connection conn = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek");  
                        
       Statement stat1 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       
        String fulltext = "", linkedtext = "", fulltextforrank = "";
        String line1;
        BufferedReader reader1 = new BufferedReader(new FileReader(input+".html"));
        while (((line1 = reader1.readLine()) != null)) { 
        fulltext = fulltext+line1+"\n";        
        }
        
        linkedtext = fulltext;    
        fulltextforrank = fulltext;        
        reader1.close();
        //System.out.println(fulltext);
       
        String line,url,repstr,finalngram;
        int ctr = 0,matchedctr, linkedctr;
        int atIndex;
         String title = "";
         List<String> ngrams = new ArrayList<String>();
         List<String> matchedngrams = new ArrayList<String>();
         
         List<String> linkedngrams = new ArrayList<String>();
         
         File file = new File("grams/"+input+"grams.txt");
         if (file.exists())
         {    
        BufferedReader reader = new BufferedReader(new FileReader("grams/"+input+"grams.txt"));
        
        while (((line = reader.readLine()) != null)) {   
             System.out.println(line);
          line = line.substring(0,line.lastIndexOf(" "));         
          ngrams.add(line);
          ctr++;                    
          }
          reader.close();          
             System.out.println("Total ngrams = "+ctr);
         }
         
         else
             {
       System.out.println("File is not present");
            }   
         Comparator<String> x = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            if(o1.length() > o2.length())
                return -1;

            if(o2.length() > o1.length())
                return 1;

            return 0;
        }
    };

    Collections.sort(ngrams,  x);
       
    
    for(String ngram:ngrams)
    {
       // System.out.println(ngram);
                                                 
             finalngram = ngram.replaceAll("([0-9])+", "");
            if (stopwords.contains(finalngram))
             {  
                System.out.println("Ignored ngram "+finalngram);
                continue; //ignore ngrams that are stopwords.
             }          
             title = ngram.replaceAll("([0-9])+", "").replace(" ","_" );             
            // System.out.println(title);            
             String query1 = "select name  from hindiwiki.Page where name = \""+title+"\"";
             ResultSet rs1 = stat1.executeQuery(query1);
             if(rs1.next())
             {
                 
                 url = "https://hi.wikipedia.org/wiki/"+rs1.getString("name");
                 //System.out.println(ngram+" "+url+" "+ctr);
                 repstr = "<a href=\""+url+"\">"+ngram+"</a>";
                 if(fulltext.contains(" "+ngram+" ") || fulltext.contains(ngram+" ")
                         || fulltext.contains(" "+ngram))
                 {                                                                                                                                                                     
                   
                     if(linkedtext.contains(" "+ngram+" "))
                     {  
                        linkedtext = linkedtext.replaceAll(" "+ngram+" ", " "+repstr+" ");
                        linkedngrams.add(title);
                        matchedngrams.add(ngram);
                        System.out.println("linked ngram"+ngram+" url is "+repstr);
                        
                     } 
                      else
                     {
                       if(linkedtext.contains(ngram+" "))  
                       {   
                        linkedtext = linkedtext.replaceAll(ngram+" ", repstr+" ");   
                        linkedngrams.add(title);
                        matchedngrams.add(ngram);
                        System.out.println("linked ngram"+ngram+" url is "+repstr);
                       }
                       else
                       if(linkedtext.contains(" "+ngram))     
                       { 
                        linkedtext = linkedtext.replaceAll(" "+ngram, " "+repstr);   
                        linkedngrams.add(title);
                        matchedngrams.add(ngram);
                        System.out.println("linked ngram"+ngram+" url is "+repstr);
                       }
                     }  
                        
                     
                     fulltext = fulltext.replaceAll(ngram, "");
                     
                 //fulltext = fulltext.replaceAll(line, repstr);
                 }                                  
                 
             }
                          
            } //end of for
    
         matchedctr = matchedngrams.size();
         linkedctr =  linkedngrams.size();
         int suggestednooflinks = 5; //(int) Math.round(.2*linkedctr);
         System.out.println("Suggested no. of links = "+suggestednooflinks);
         int counter = 0;
         RankKeywords obj = new RankKeywords();         
         Map<String, Double> sortedMap =  obj.Rankwords("hindiwiki", linkedngrams);
         List<String> topntitles = new ArrayList<String>();
         for (Map.Entry entry : sortedMap.entrySet()) {			
               topntitles.add((String) entry.getKey().toString().trim());
               counter++;
               if (counter == suggestednooflinks )
               {
                   break;
               }
		}
         
          System.out.println("Suggested no. of links = "+topntitles.size());
          for (String toptitle: topntitles)
          {
               System.out.println("Top title = "+toptitle);
          }
          
         linkedtext = fulltextforrank;
         for(String matchedngram: matchedngrams)
         {
             title = matchedngram.replaceAll("([0-9])+", "").replace(" ","_" );
           if(topntitles.contains(title.trim()))
           {
               url = "https://hi.wikipedia.org/wiki/"+title.trim();              
               repstr = "<a href=\""+url+"\">"+matchedngram+"</a>";
               
               if(fulltextforrank.contains(" "+matchedngram+" ")
                       || fulltextforrank.contains(matchedngram+" ")
                         || fulltextforrank.contains(" "+matchedngram)
                       )
                 {                                                                                                                                                                                       
                     if(linkedtext.contains(" "+matchedngram+" "))
                     {  
                        linkedtext = linkedtext.replaceAll(" "+matchedngram+" ", " "+repstr+" ");
                        
                     } 
                      else
                     {
                       if(linkedtext.contains(matchedngram+" "))  
                       {                              
                        linkedtext = linkedtext.replaceAll(matchedngram+" ", repstr+" ");                           
                       }
                       else
                           
                       if(linkedtext.contains(" "+matchedngram))     
                       { 
                        linkedtext = linkedtext.replaceAll(" "+matchedngram, " "+repstr);                           
                       }
                     }  
                                             
                     fulltextforrank = fulltextforrank.replaceAll(matchedngram, "");                     
                 //fulltext = fulltext.replaceAll(line, repstr);
                 }                                 
               
           }
             
         }
                                            
      BufferedWriter writer1 = new BufferedWriter(new FileWriter("linked/"+input+".html"));     
       writer1.write(linkedtext);
       writer1.close(); 
       
       disambiguateWord dob = new disambiguateWord();
       dob.disambiguateWordsfunc(topntitles);
        } 
              catch(Exception e)
             {
             System.out.println("Error is "+e.getMessage()+" "+e.toString());
             }  
        
        
        
        
        }
      
        
       
       
    
     
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        // TODO code application logic here
        getStopwords("hindiST.txt");
        String urlstr = "file:///home/priya/NetBeansProjects/MiniProject/";
        // Html_clean(urlstr+args[0]+".html", args[0]);
         Html_clean(urlstr+"NBT16.html", "NBT16");
        //findUniBigrams(args[0]);
         findUniBigrams("NBT16");
        //if(args[1].equals("english"))            
         //MatchNgramwithWiki(args[0]);
        //else
        // MatchNgramwithWikiHindi(args[0]); 
        MatchNgramwithWikiHindi("NBT16"); 
        System.out.println("file:///home/priya/NetBeansProjects/MiniProject/linked/NBT16.html");
        // MatchNgramwithWiki("hindu10");
         //MatchNgramwithWiki("hindu62");
         //findUniBigrams("hindu6");
        
        
    }
}
