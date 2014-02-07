/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * This program does  maximal n-gram matching. N-grams are sorted as per length
 * hence longer n-grams are processed first and thus if a smaller n-gram is encountered later that
 * overlaps the already processed longer n-gram then smaller n-gram is not linked
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import static miniproject.MiniProject.MatchNgramwithWikiHindi;
import static miniproject.MiniProject.findUniBigrams;
import static miniproject.MiniProject.getStopwords;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author priya
 */
public class MiniProjectv1 {

    /**
     * @param args the command line arguments
     */

     private static final Pattern UNDESIRABLES = Pattern.compile("[,.;!?(){}\\[\\]<>%:-]");
     private static List<String>  stopwords = new ArrayList<String>();
     static String steps;
    
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
         
         
          BufferedWriter writer = new BufferedWriter(new FileWriter("script.bat"));
          writer.write("rm grams/"+input+"grams.txt");
          writer.write("\n");
          writer.write("G:/NetbeansProjects/MiniProject/ngramtool-20040527-mingw32-static/text2ngram -n1 -m5 clean/"+input+"clean.txt >> grams/"+input+"grams.txt");
          writer.close();
            try                                
       {
          
        p = rt.exec("script.bat");       

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
                    "jdbc:mysql://localhost:8888/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek14");  
                        
       Statement stat1 = conn.createStatement();
       String query = "set names utf8";
       stat1.execute(query);
       
        String fulltext = "", linkedtext = "";
        String line1;
        BufferedReader reader1 = new BufferedReader(new FileReader(input+".html"));
        while (((line1 = reader1.readLine()) != null)) { 
        fulltext = fulltext+line1+"\n";        
        }
        
        linkedtext = fulltext;                    
        reader1.close();
        //System.out.println(fulltext);
       
        String line,url,repstr,finalngram;
        int ctr = 0;
        int atIndex;
         String title = "";
         List<String> ngrams = new ArrayList<String>();
       
         File file = new File("grams/"+input+"grams.txt");
         if (file.exists())
         {    
        BufferedReader reader = new BufferedReader(new FileReader("grams/"+input+"grams.txt"));
        
        while (((line = reader.readLine()) != null)) {   
             System.out.println(line);
          line = line.substring(0,line.lastIndexOf(" "));         
          ngrams.add(line.trim());
          ctr++;                    
          }
          reader.close();          
             System.out.println("no of lines = "+ctr);
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

    Collections.sort(ngrams,  x); //sort n-gram lines in descending order of length
       
    List<String> linkedterms = new ArrayList<String>();
    List<String> ambig = new ArrayList<String>();
    for(String ngram:ngrams)
    {
       // System.out.println(ngram);
                                                 
             finalngram = ngram.replaceAll("([0-9])+", ""); //cleaning of n-grams to get title
             finalngram = processWord(finalngram);
             
            if (stopwords.contains(finalngram))
             {  
                 
                System.out.println("Ignored ngram "+finalngram);
                continue; //ignore ngrams that are stopwords.
             }          
             title = finalngram.replace(" ","_" );             
            // System.out.println(title);            
             String query1 = "select name  from hindiwiki.Page where name = \""+title+"\"";
             
             ResultSet rs1 = stat1.executeQuery(query1);
             if(rs1.next())
             {
               String disambig = "select 1 from hindiwiki.Page where name = \""+title+"\" and isdisambiguation = 1";
               ResultSet disambigrs = stat1.executeQuery(disambig);
              if(disambigrs.next())
              {                  
                 ambig.add(title);
                // url = "https://hi.wikipedia.org/wiki/"+rs1.getString("name");
              }//disambiguatePage(title); }
                 else
              {
                  disambigrs.close();
                  System.out.println("clear word "+title);
                  
              }
                 //System.out.println(ngram+" "+url+" "+ctr);
                  url = "https://hi.wikipedia.org/wiki/"+title;//rs1.getString("name");
                 repstr = "<a href=\""+url+"\">"+ngram+"</a>";
                 if(fulltext.contains(" "+ngram+" ") || fulltext.contains(ngram+" ")
                         || fulltext.contains(" "+ngram))
                 {                                                                                                                                                                     
                     System.out.println("linked ngram"+ngram+" url is "+repstr);
                     if(linkedtext.contains(" "+ngram+" "))
                     {
                        linkedtext = linkedtext.replaceAll(" "+ngram+" ", " "+repstr+" ");
                        linkedterms.add(title);
                     }  
                      else
                     {
                       if(linkedtext.contains(ngram+" "))  
                       {   linkedterms.add(title);
                        linkedtext = linkedtext.replaceAll(ngram+" ", repstr+" ");  
                       }
                       else if(linkedtext.contains(" "+ngram))  
                       {   
                        linkedtext = linkedtext.replaceAll(" "+ngram, " "+repstr);   
                        linkedterms.add(title);
                       }         
                     }  
                        
                     
                     fulltext = fulltext.replaceAll(ngram, "");
                     
                 //fulltext = fulltext.replaceAll(line, repstr);
                 }                                  
                 
             }
             
             else
             {
                 rs1.close();
                 String redirquery1 = "select redirects from hindiwiki.page_redirects where redirects = \""+title+"\"";
                 ResultSet redirrs1 = stat1.executeQuery(redirquery1);
                 
            if(redirrs1.next())
              {
                 System.out.println("Redir title is"+title);                
                 url = "https://hi.wikipedia.org/wiki/"+redirrs1.getString("redirects");
                 //System.out.println(ngram+" "+url+" "+ctr);
                 repstr = "<a href=\""+url+"\">"+ngram+"</a>";
                 if(fulltext.contains(" "+ngram+" ") || fulltext.contains(ngram+" ")
                         || fulltext.contains(" "+ngram))
                 {                                                                                                                                                                     
                     System.out.println("linked ngram"+ngram+" url is "+repstr);
                     if(linkedtext.contains(" "+ngram+" "))
                     {   
                        linkedtext = linkedtext.replaceAll(" "+ngram+" ", " "+repstr+" ");
                        linkedterms.add(title);
                      }
                      else
                     {
                       if(linkedtext.contains(ngram+" "))  
                       {    
                        linkedtext = linkedtext.replaceAll(ngram+" ", repstr+" ");   
                        linkedterms.add(title);
                       } 
                       else if(linkedtext.contains(" "+ngram))
                       {   
                        linkedtext = linkedtext.replaceAll(" "+ngram, " "+repstr);   
                        linkedterms.add(title);
                       } 
                     }  
                        
                     
                     fulltext = fulltext.replaceAll(ngram, "");
                     
                 //fulltext = fulltext.replaceAll(line, repstr);
                 }                                                   
             }
            
            else
            {
             redirrs1.close();   
             String pquery =  "select count(*) from hindiwiki.Page where name like ?";
             PreparedStatement pstmt  = conn.prepareStatement(pquery);
             pstmt.setString(1, title+"_(%)");
             ResultSet prs = pstmt.executeQuery();
             prs.next();
             int forms = prs.getInt(1);
             if (forms > 0)
             {
                  System.out.println("The title or ngram"+title+" "+forms+" different forms");
             }    
                  
             prs.close();
            } //end of else
            
            
            
          }//end of else
             
                                                    
            }//end of for
    
    for(String word: ambig)
             {
                  System.out.println("Ambiguous word "+word);
                 String finaldoc = disambiguatePage(word,linkedterms);
                 linkedtext = linkedtext.replace("https://hi.wikipedia.org/wiki/"+word, "https://hi.wikipedia.org/wiki/"+finaldoc);
             }
    
      BufferedWriter writer1 = new BufferedWriter(new FileWriter("linked/"+input+".html"));
      // writer1.write(fulltext);
       writer1.write(linkedtext);
       writer1.close(); 
        } 
              catch(Exception e)
             {
             System.out.println("Error is "+e.getMessage()+" "+e.toString());
             }                          
        
        }
      
        
       
       
    
     
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        // TODO code application logic here
        getStopwords("hindiST.txt");
        String urlstr = "file:///G:/NetbeansProjects/MiniProject/";
        // Html_clean(urlstr+args[0]+".html", args[0]);
         Html_clean(urlstr+"NBT8.html", "NBT8");
        //findUniBigrams(args[0]);
         findUniBigrams("NBT8");
        //if(args[1].equals("english"))            
         //MatchNgramwithWiki(args[0]);
        //else
        // MatchNgramwithWikiHindi(args[0]); 
        MatchNgramwithWikiHindi("NBT8"); 
        System.out.println("file:///G:/NetbeansProjects/MiniProject/linked/NBT39.html");
        // MatchNgramwithWiki("hindu10");
         //MatchNgramwithWiki("hindu62");
         //findUniBigrams("hindu6");
        
        
    }
    
    private static String processWord(String x) {
    return UNDESIRABLES.matcher(x).replaceAll("");
}

    private static String disambiguatePage(String title, List<String> linkedterms) throws ClassNotFoundException, SQLException 
        {  
            
          String finaldoc= "";   
        try{
       Class.forName("com.mysql.jdbc.Driver");  
       Connection conn2 = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:8888/hindiwiki?useUnicode=yes&characterEncoding=UTF-8", "root", "geek14");  
                        
       Statement stat2 = conn2.createStatement();
       String query = "set names utf8";
       stat2.execute(query);
       String gettext = "select text from Page where name =\""+title+"\""; //find the text of ambiguous page
       String longtext,linkeditem;
       List<String> doclist = new ArrayList<String>();
       ResultSet textrs = stat2.executeQuery(gettext);
       if(textrs.next())
       {
           longtext = textrs.getString(1);
           int linkedIndexst = 0;
                int linkedIndexend = 0;
                int pos;
           while(linkedIndexst != -1) //linkedIndexend
            {       
              linkedIndexst =  longtext.indexOf("[[", linkedIndexst);
              linkedIndexend = longtext.indexOf("]]", linkedIndexst);
              
              if(linkedIndexst != -1 && linkedIndexend != -1 )
              {   
                 linkeditem = longtext.substring(linkedIndexst+2, linkedIndexend);
                 
                 if(linkeditem.indexOf('|') > -1)
                 {  
                     System.out.println("linkeditem having pipe symbol = "+linkeditem);
                     pos = linkeditem.indexOf('|');
                     System.out.println("Position of pipe is "+pos);
                     linkeditem = linkeditem.substring(0,pos);    
                     System.out.println("Linked item after pipe removal = "+linkeditem);
                 }            
                 
                 
               //  if(linkeditem.contains(title.replace("_", " ")))
                 //{
                   //  System.out.println("Matching linked item "+linkeditem);
                     doclist.add(linkeditem.replace(" ", "_")); // add all links that contain title these links are perhaps unambiguous ones
                /// }
                 
               }
              
              

               if(linkedIndexst != -1)
           {             
             linkedIndexst += "[[".length();
           }     
            
           
    
        
    }
       }
       
       textrs.close();
       List<String> alldocterms = new ArrayList<String>();
       String[] docterms;
       double pt_d,tp,maxtp = 0.0;
       String doctext;
       for(String doc: doclist)
       {
           System.out.println("Finding text for "+doc);
          steps = "step 10";
           String existquery = "select count(*) from Page where name =\""+doc+"\"";
           ResultSet countrs = stat2.executeQuery(existquery);
           steps = "step 11";
           countrs.next();
           int exist = countrs.getInt(1);
           steps = "step 12";
           if (exist == 1)
           gettext = "select text from Page where name =\""+doc+"\"";
           else
           gettext = "select text from Page p,page_redirects pr where redirects = \""+doc+"\" and pr.id = p.pageId";
          
           countrs.close();
           
           ResultSet textrs1 = stat2.executeQuery(gettext);
           steps = "step 13";
        if(textrs1.next())   
        {   
           tp = 1.0000000; 
           doctext = textrs1.getString(1);
           docterms = doctext.split(" ");
           int docsize = docterms.length;
           System.out.println("Length of doc is "+docsize);
           for(String word: linkedterms) //find term frequency of each word in linkedterms 
           {
             pt_d = 0.0;  
              int count = 0;
              
               word= word.replace("_", " "); //convert title to ngram
             /*for(String term:docterms)
             {
                 if(term.equals(word))
                     count++;                 
             } */            
             
             int atIndex = 0;
            
             
           while (atIndex != -1)
             {       
             atIndex = doctext.indexOf(" "+word+" ", atIndex);
            if(atIndex != -1)
             {
             count++;
             atIndex += (" "+word+" ").length();
             }
             }
           System.out.println("The word "+word+" occurs "+count+" times "+"in doc "+doc);
          if(count > 0)     //for each linkedword count frequency in article page
           pt_d = (count*1.0/docsize*1.0);
          else 
            pt_d = 1.0/100000.0;

         // System.out.println("Probability of word is "+pt_d);
           tp = tp*pt_d;
           
           } //end of for
        //   System.out.print("Total probability of doc "+doc+" is "+tp);
           if(maxtp < tp)
           {
               maxtp = tp;
               finaldoc = doc;
          //     System.out.print("Max probability is "+maxtp);
           }
 
       } //end of if testrs1.next   

     } //end of for
            System.out.println("Final document is "+finaldoc);
   }   
        
        catch(Exception ex)
        {
            System.out.println("Exception is at step"+steps+" error is "+ex.getMessage());
        }
         return finaldoc;
    
}//end of static function
    
} //end of class def

