/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package similaritysearch;
import java.util.ArrayList;
/**
 *
 * @author Raihan Sabique
 */
public class wordBox {
    String word;
    ArrayList<String> synword=new ArrayList<String>();
    public void setWord(String w){
        word=w;
    }
    public void setsynWord(String sw){
        synword.add(sw);
    }

    public String getWord() {
        return word;
    }

    public ArrayList<String> getSynword() {
        return synword;
    }
    
    public int getKey()
    {
        int c=(int)word.charAt(0);
        return c%259;
    }
    
}
