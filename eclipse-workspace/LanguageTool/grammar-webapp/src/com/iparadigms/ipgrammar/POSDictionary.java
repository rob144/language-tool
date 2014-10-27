package com.iparadigms.ipgrammar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class POSDictionary {
    
    private final String dictionaryLocation = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/dictionary.dump";
    private ArrayList<String[]> _posDictionary = new ArrayList<String[]>();
    
    public POSDictionary () throws FileNotFoundException, IOException {
        populatePosDictionary();
    }
    
    public boolean searchWord (String posWord) {
        boolean itemFound = false;
        
        for (String s[] : _posDictionary)
            if (s[0].equals(posWord))
               itemFound = true;
        
        return itemFound;
    }
    
    public void addWord (String posWord) {
        String[] wordItems = new String[3];
        wordItems = posWord.split("\t");
        _posDictionary.add(wordItems);
    }
    
    private void populatePosDictionary () throws FileNotFoundException, IOException {
        String line = "";
        BufferedReader dictReader = new BufferedReader(new FileReader(dictionaryLocation));
        while ((line = dictReader.readLine()) != null)
            addWord (line);
        dictReader.close();
    }
}