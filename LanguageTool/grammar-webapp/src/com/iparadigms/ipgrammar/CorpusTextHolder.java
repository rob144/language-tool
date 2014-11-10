package com.iparadigms.ipgrammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CorpusTextHolder {
    
    private int lineLimit = 50000;
    
    private final String RESOURCES_DIR = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/";
    private final String TATOEBA_CORPUS_CSV = RESOURCES_DIR + "sentences.csv";
    private final String CORPUS_FILENAME_SUFFIX = "_tatoeba_corpus.txt";
    
    private String _extractedCorpusFileName = "";
    private String _langCode = "";
    
    private List<String> _arrayLines = new ArrayList<String>();
    
    public CorpusTextHolder (String langCode) throws FileNotFoundException, IOException {
        _langCode = langCode;
        _extractedCorpusFileName = RESOURCES_DIR + _langCode + CORPUS_FILENAME_SUFFIX;
        File f = new File(RESOURCES_DIR + _langCode + CORPUS_FILENAME_SUFFIX);
        
        //Extract corpus to new file
        if (!f.exists()) {
System.out.println("EXTRACTING CORPUS TEXT");
            BufferedReader corpusReader = new BufferedReader(new FileReader(TATOEBA_CORPUS_CSV));
            FileWriter write = new FileWriter(_extractedCorpusFileName, false);
            PrintWriter printLine = new PrintWriter(write);
            String line = "";
            
            //Keep looping through until corpus is out of lines
            while ((line = corpusReader.readLine()) != null){
                if(line.split("\t")[1].equals(_langCode)){
                    printLine.println(line.split("\t")[2]);
                }
            }
            corpusReader.close();
            printLine.close();
            write.close();
        }
        
        loadCorpusText();
    }
    
    public void loadCorpusText () throws FileNotFoundException, IOException {
        // TODO : add lines from extracted corpus to class variable
        System.out.println("LOADING CORPUS TEXT");
        BufferedReader extractedCorpusReader = new BufferedReader(new FileReader(_extractedCorpusFileName));
        String line = "";
        
        //Keep looping through until corpus is either out of lines or upper limit reached
        for (int x = 0; x < lineLimit && ((line = extractedCorpusReader.readLine()) != null); x++)
            _arrayLines.add(line);
        extractedCorpusReader.close();
    }
    
    public String getLinesToString (int startIndex, int endIndex) {
        String lines = "";
        
        for (int x = startIndex; x < endIndex; x++)
            lines += getLine(x) + "\n";
        
        return lines;
    }
    
    public String getLinesToString (int endIndex) {
        String lines = "";
        
        for (int x = 0; x < endIndex; x++)
            lines += getLine(x) + "\n";
        
        return lines;
    }
    
    public List<String> getLines (int startIndex, int endIndex) {
        return _arrayLines.subList(startIndex, endIndex);
    }
    
    public List<String> getLines (int endIndex) {
        return _arrayLines.subList(0, endIndex);
    }
    
    public String getLine (int lineNumber) {
        return _arrayLines.get(lineNumber);
    }
}