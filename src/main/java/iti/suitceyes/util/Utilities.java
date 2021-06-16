package iti.suitceyes.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utilities {

	public Utilities() {
		// TODO Auto-generated constructor stub
	}
	
	public static HashMap<String, Float> sortByValue(HashMap<String, Float> hashmap) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Float> > list = 
               new LinkedList<Map.Entry<String, Float>>(hashmap.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Float> >() { 
            public int compare(Map.Entry<String, Float> o1,  
                               Map.Entry<String, Float> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Float> temp = new LinkedHashMap<String, Float>(); 
        for (Map.Entry<String, Float> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
