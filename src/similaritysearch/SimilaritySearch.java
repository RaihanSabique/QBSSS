/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package similaritysearch;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Soundbank;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jdk.nashorn.internal.runtime.regexp.joni.constants.CCSTATE;

/**
 *
 * @author Raihan Sabique
 */
public class SimilaritySearch {

    private static String url = "jdbc:mysql://localhost:3306/q_bank?user=root";
    private static String password = "12345";
    private static String username = "root";
    public static ArrayList<ArrayList<String>> mcqList = new ArrayList<ArrayList<String>>();
    public static ArrayList<ArrayList<Integer>> mcqListBinary = new ArrayList<ArrayList<Integer>>();
    public static ArrayList<String> stopWord = new ArrayList<String>();
    public static String ch_id = "";
    public static RuleFileParser parser = new RuleFileParser();
    public static ArrayList<Result> result = new ArrayList<>();
    public static ArrayList<ArrayList<Double>> tf = new ArrayList<ArrayList<Double>>();
    public static ArrayList<Double> idf = new ArrayList<Double>();
    public static ArrayList<Double> qVect = new ArrayList<Double>();
    public static Map<Integer,wordBox> dictionary = new Map <Integer,wordBox>();
    public static PriorityQueue<Result> pq = new PriorityQueue<Result>(new Comparator<Result>() {
    
            public int compare(Result lhs, Result rhs) {
                if(lhs.p<rhs.p) return 1;
                if(lhs.p>rhs.p) return -1;
                else{
                return 0;
                }
            }
        });
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        // TODO code application logic here
        ArrayList<String> current_question= new ArrayList<String>();
        System.out.println(System.getProperty("file.encoding"));
        Font banglaFont=new Font("Arial Unicode MS", Font.BOLD,15);
        
        initialization();
        wordNet();
        
        //System.out.println(stringCompare("পাইথন","পাইথন"));
        
        String question,title, op1, op2, op3, op4;
        FileInputStream I = new FileInputStream("src\\similaritysearch\\input.txt");
        InputStreamReader r = new InputStreamReader(I,"UTF-8");
        int f;
        String c="";
        List<String> qu=new ArrayList<String>();
        List<List<String>> inputQuestion=new ArrayList<List<String>>();
        int mcqidx=0;
        System.out.println("Input:");
        while((f = r.read())!=-1) {
            //System.out.println(Character.toString((char)f));
            if((char)f=='>')
            {
                StringBuilder builder = new StringBuilder(c);
        String[] words = builder.toString().split("\\s");
        int flag;
        //System.out.println("Input:");
        for (String word : words) {
            flag = 0;
            for (int i = 0; i < stopWord.size(); i++) {
                if (stopWord.get(i).equals(word) || word.matches("\\s*")) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                String q= parser.stemOfWord(word);
                
                int k=(int)q.charAt(0);
                k=k%259;
                wordBox wb=new wordBox();
                wb=dictionary.get(k);
                //System.out.println(q);
                //System.out.println("WB:"+wb.getWord());
                qu.add(parser.stemOfWord(word));
                
                //System.out.println(parser.stemOfWord(word));  
            }
          }
                for(int k=0;k<qu.size();k++)
                {
                    System.out.println(qu.get(k));
                }
                inputQuestion.add(qu);
                mcqidx++;
                //System.out.println(inputQuestion.get(0).get(0));
                qu.clear();
                c="";
            }
            else{
                c =c+ Character.toString((char)f);
            }
         }
        for(List<String> l : inputQuestion)
        {
            System.out.println("seq:");
            for(String st: l)
            {
                System.out.println(st);
            }
        }
        
        int tfCount;
        double x;
        
        for(int i=0;i<mcqList.size();i++)
        {
            //System.out.println("D"+(i+1));
            
            
            ArrayList<Double> tfcntTemp=new ArrayList<>();
            
            for(int j=0;j<qu.size();j++)
            {
                tfCount=0;
               
                for(int k=0;k<mcqList.get(i).size();k++)
                {
                    if(stringCompare(qu.get(j),mcqList.get(i).get(k))==1)
                    {
                        tfCount++;
                       
                    }
                }
               
                x=(tfCount*1.0/mcqList.get(i).size());
                DecimalFormat df = new DecimalFormat("#.##");      
                x = Double.valueOf(df.format(x));
                tfcntTemp.add(x);
                //System.out.print(tfCount +" ");
                
            }
            tf.add(tfcntTemp);
         
            //System.out.println("");
            //System.out.print(tf.get(i).get(j)+ " ");
        }
        for(int i=0;i<qu.size();i++)
        {
            idf.add(0.00);
            qVect.add(0.00);
        }
        System.out.println("                               Term Frequency       ");
        for(int i=0;i<tf.size();i++)
        {
            //System.out.print("\nMCQ No " + (i+1)+ ":  ");
            for(int j=0;j<tf.get(i).size();j++)
            {
                if(tf.get(i).get(j)>=0.1)
                {
                    idf.set(j, idf.get(j)+1);
                }
                //System.out.print(tf.get(i).get(j)+"   ");
            }
            
        }
        System.out.print("\n\n\nIDF:");
        for(int i=0;i<qu.size();i++)
        {
            double d=Math.log((mcqList.size()*1.0)/(1+idf.get(i)));
            DecimalFormat df = new DecimalFormat("#.##"); 
            d = Double.valueOf(df.format(d));
            idf.set(i,d);
            //System.out.print(idf.get(i)+"  ");
        }
        //System.out.print("\n");
        for(int i=0;i<idf.size();i++)
        {
            //qVect.set(i, (idf.get(i)/idf.size()));
            qVect.set(i, 1.0);
            //System.out.println("Query Vector: "+qVect.get(i)+"  ");
        }
/*TF-IDF*/
    //System.out.println("\n                              TF-IDF               ");
    for(int i=0;i<tf.size();i++)
    {
        //System.out.print("\n MCQ No "+(i+1)+ ":    ");
        for(int j=0;j<tf.get(i).size();j++)
        {
            DecimalFormat df = new DecimalFormat("#.##");
            tf.get(i).set(j, Double.valueOf(df.format(tf.get(i).get(j)*idf.get(j))));
            //System.out.print(tf.get(i).get(j)+"        ");
        }
    }
    double s,rt1,rt2;
    double cs;
    System.out.println("\nCosine Similarity");
    double maxCS=-99999.00;
    int highRankIdx=0;
    for(int i=0;i<tf.size();i++)
    {   
        s=0.0;
        rt1=0.0;
        rt2=0.0;
        for(int j=0;j<tf.get(i).size();j++)
        {
            
            s+= tf.get(i).get(j)*qVect.get(j);
            rt1+= tf.get(i).get(j)*tf.get(i).get(j);
            rt2+= qVect.get(j)*qVect.get(j);
        }
        //System.out.println("here:" + s +"  & " +rt1 +"  -> "+ Math.sqrt(rt1)+"   "+Math.sqrt(rt2));
        cs=s /(Math.sqrt(rt1)*Math.sqrt(rt2));
        if(cs>maxCS)
        {   
            maxCS=cs;
            highRankIdx=i;
            //System.out.println("MCQN0 "+i+":"+cs);
        }
    }
    System.out.println("MCQN0 "+highRankIdx+":"+maxCS);
          
        /*double percentage;
        int cnt;
        
        for(int i=0;i<mcqList.size();i++)
        {
            cnt=0;
            for(int j=0;j<mcqListBinary.get(i).size();j++)
            {
                if(mcqListBinary.get(i).get(j)== 1)
                {
                    cnt++;
                }
            }
            percentage=(cnt*1.0/mcqListBinary.get(i).size())*100.0;
            //System.out.println(percentage);
            //String id=
            
            if(percentage> 0.0)
            {
//                String id=ch_id.substring(1);
//                id= id + "000";
//                Integer  x=Integer.parseInt(id);
                String mcqid=ch_id + Integer.toString(1000+i).substring(1);
                Result res=new Result();
                res.mcqid =mcqid;
                res.p=percentage;
                pq.add(res);
                System.out.println("mcqid:"+mcqid + ",Similarity percentage is =>" + percentage);
            }
            
        }
        int n=1;
        Result temp=new Result();
        while(pq.size()!=0){ 
            temp=pq.poll();
             System.out.println(n+"." + temp.mcqid + "=> similarity " + temp.p);
             n++;
             
        } */ 
        
        
    }
    
    public static int stringCompare(String s1,String s2)
    {
        int l1=s1.length();
        int l2=s2.length();
        double count=0.0;
        for(int i=0;i<l1;i++)
        {
            char c=(char)s1.charAt(i);
            for(int j=0;j<l2;j++)
            {
                if(c==(char)s2.charAt(j))
                {
                    count++;
                }
                        
            }
        }
        if(count/l2>0.8)
            return 1;
        else
            return 0;
        
    }
    
    public static String ParserSWremover(String s)
    {  
        String str=""; 
        StringBuilder builder = new StringBuilder(s);
        String[] words = builder.toString().split("\\s");
        int flag;
        for (String word : words) {
            flag = 0;
            for (int i = 0; i < stopWord.size(); i++) {
                //System.out.println(stopWord.get(i));
                if (stopWord.get(i).equals(word) || word.matches("\\s*")) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                str+=parser.stemOfWord(word)+" ";
                
                //System.out.println(parser.stemOfWord(word));
               
            }
        }
        return str;
        
    }
    
    
   
    public static void wordNet() throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        FileInputStream I = new FileInputStream("src\\similaritysearch\\input1.txt");
        InputStreamReader r = new InputStreamReader(I,"UTF-8");
        int f;
        String c="";
        String str="";
        ArrayList<String> wordList=new ArrayList<String>();
        while((f = r.read())!=-1) {
            
         
            // int to character
            c=Character.toString((char)f);
            if((char)f!='\n'){
                //c +=Character.toString((char)f);
                //System.out.println(c);
                if((char)f==';')
                {
                    wordList.add(ParserSWremover(str));
                    //System.out.println(ParserSWremover(str));
                    str="";
                }
                else if((char)f==',')
                {
                    wordList.add(ParserSWremover(str));
                    //System.out.println(ParserSWremover(str));
                    str="";
                }
                else{
                    str+=Character.toString((char)f);
                    
                }
            }
            else{
                wordList.add(ParserSWremover(str));
                //System.out.println(ParserSWremover(str));
                
                
                for(int i=0;i<wordList.size();i++)
                {
                    if(wordList.get(i)!=""){
                    wordBox wb=new wordBox();
                    
                    wb.setWord(wordList.get(i));
                    for(int j=0;j<wordList.size();j++)
                    {
                        if(j!=i)
                        {
                            wb.setsynWord(wordList.get(j));
                        }
                    }
                    //System.out.println(wb.word+"->"+wb.getKey());
                    dictionary.add(wb.getKey(), wb);
                    //System.out.println("ADDED");
                    //wb=null;
                    }
                }
                wordList.clear();
                str="";
            }
        }
    }
    
    public static void initialization() {
        try {

            Class.forName("com.mysql.jdbc.Driver");
            //String url = "jdbc:mysql://localhost:3306/street_children_database?user=root";
            Connection con = DriverManager.getConnection(url, username, password);

            if (con != null) {
                System.out.println("connection successfully established");
            } else {
                System.out.println("Connection failed!!!!!");
            }

            String chapter;
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter the chapter name:");
            chapter = sc.nextLine();

            //Statement stmt = con.createStatement();
            String sqlString = "select chapterid from chapter where etitle= ?";
            PreparedStatement ps = con.prepareStatement(sqlString);
            ps.setString(1, chapter);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ch_id = rs.getString(1);
                System.out.println(ch_id);
            }

//            String sqlSW = "select * from stopWord";
//            ps = con.prepareStatement(sqlSW);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                stopWord.add(rs.getString(1));
//            }
            
        FileInputStream I = new FileInputStream("src\\similaritysearch\\stopwords-bn.txt");
        InputStreamReader r = new InputStreamReader(I,"UTF-8");
        int f;
        String c="";
   
        while((f = r.read())!=-1) {
         
            // int to character
            
            if((char)f!='\n'){
                c = c + Character.toString((char)f);
            }
            else{
            System.out.println(parser.stemOfWord(c));
            stopWord.add(c);
            c="";
            }
            
            // print char
            //System.out.println("Character Read: "+c);
         }
       
            
            //System.out.println(stopWord.get(0));

            String sqlString1 = "select * from simmcq where mcqid like ?";
            //ResultSet rs1 = stmt.executeQuery("select * from simmcq where mcqid like ?%");
            ps = con.prepareStatement(sqlString1);
            ps.setString(1, ch_id + "%");
            rs = ps.executeQuery();
            System.out.println(ch_id);

            String mcq = "";
            int count = 0;
            while (rs.next()) {
                //System.out.println("bal");
                count++;
                mcq = rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6);
                //System.out.println(mcq);
                StringBuilder builder = new StringBuilder(mcq);
                String[] words = builder.toString().split("\\s");
                ArrayList<String> tempMcq = new ArrayList<String>();
                ArrayList<Integer> tempMcqbinary = new ArrayList<Integer>();
                int flag;
                for (String word : words) {
                    flag = 0;
                    for (int i = 0; i < stopWord.size(); i++) {
                        if (stopWord.get(i).equals(word)) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        //System.out.println(parser.stemOfWord(word));
                        tempMcq.add(parser.stemOfWord(word));
                        tempMcqbinary.add(0);
                    }

                    //System.out.println(word);
                }

                mcqList.add(tempMcq);
                mcqListBinary.add(tempMcqbinary);

                //System.out.println("here:" + mcqList.get(0).get(0));
                //tempMcq.removeAll(tempMcq);
                mcq = "";

                //System.out.println(rs.getString(2));
            }
            //System.out.println("here:" + mcqList.get(0).size());
            for (int i = 0; i < mcqList.size(); i++) {
                //System.out.println("Mcq no:" + i);
                for (int j = 0; j < mcqList.get(i).size(); j++) {
                    //System.out.println(mcqList.get(i).get(j) + "=>" + mcqListBinary.get(i).get(j));

                }

                //System.out.println();
            }

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
