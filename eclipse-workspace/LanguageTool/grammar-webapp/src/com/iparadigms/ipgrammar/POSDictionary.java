package com.iparadigms.ipgrammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.languagetool.dev.POSDictionaryBuilder;

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
    
    public void addWord (String wordData) {
        _posDictionary.add(wordData.split(" "));
    }
    
    public void addWord (String word, String lemma, String posTag) {
        _posDictionary.add(new String[]{word, lemma, posTag});
    }
    
    public void buildDictionary () throws Exception {
        String temp = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/temp.dump";
        File tempDump = new File (temp);
        FileWriter write = new FileWriter(temp, false);
        PrintWriter printLine = new PrintWriter(write);
        //getting lines and formatting them for .dump useable by FSADump
        String line = "";
        for (int x = 0; x < _posDictionary.size(); x++) {
            line = "";
            line = _posDictionary.get(x)[0].toString() + "\t" + _posDictionary.get(x)[1].toString() + "\t" + _posDictionary.get(x)[2].toString();
            //adding to temp.dump
            printLine.println(line);
        }
        //run dictionary build functionality found in UnitTests.java
        printLine.close();
        
        //.dump being converted into binary dictionary
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/english.info"));
        pos.build(new File(temp)).renameTo( new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/dictionary.dict"));
        tempDump.delete();
    }
    
    private void populatePosDictionary () throws FileNotFoundException, IOException {
        String line = "";
        BufferedReader dictReader = new BufferedReader(new FileReader(dictionaryLocation));
        while ((line = dictReader.readLine()) != null){
            String[] items = line.split("\t");
            addWord (items[0], items[1], items[2]);
        }
            
        dictReader.close();
    }
}
