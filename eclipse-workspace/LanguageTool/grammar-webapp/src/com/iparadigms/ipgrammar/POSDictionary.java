package com.iparadigms.ipgrammar;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

import morfologik.tools.FSADumpTool;
import org.languagetool.dev.POSDictionaryBuilder;

public class POSDictionary {
    
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
    private String _resourcesDir = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/";
    
    private String _pathToInfoFile = _resourcesDir + "english.info";
    private String _pathToBinaryDict = _resourcesDir + "english.dict";
    
    private String _pathToDump = _resourcesDir + "dictionary.dump";
    
    private String _pathToTempDump = _resourcesDir + "temp.dump";
    private String _pathToNewDict = _resourcesDir + "new_english.dict";
    
    private ArrayList<String[]> _posDictionary = new ArrayList<String[]>();
    
    public POSDictionary () throws Exception {
        dumpDictionary();
        populateLocalDictionary();
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
    
    public int getWordCount () {
        return _posDictionary.size();
    }
    
    public void buildPosDictionary () throws Exception {
        File tempDump = new File (_pathToTempDump);
        FileWriter writeTempDump = new FileWriter(_pathToTempDump, false);
        PrintWriter printLineTempDump = new PrintWriter(writeTempDump);
        //getting lines and formatting them for .dump useable by FSADump
        String line = "";
        for (int x = 0; x < getWordCount(); x++) {
            line = "";
            line = _posDictionary.get(x)[0].toString() + "\t"
            	+ _posDictionary.get(x)[1].toString() + "\t"
            	+ _posDictionary.get(x)[2].toString() + "\n";
            //adding to temp.dump
            printLineTempDump.print(line);
        }
        printLineTempDump.close();
        writeTempDump.close();
        
        //.dump being converted into binary dictionary
        File binaryDict = new File (_pathToNewDict);
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File(_pathToInfoFile));
        pos.build(tempDump).renameTo(binaryDict);
        //tempDump.delete();
    }
    
    private void populateLocalDictionary () throws Exception {
        String line = "";
        BufferedReader dictReader = new BufferedReader(new FileReader(_pathToDump));
        while ((line = dictReader.readLine()) != null){
            if (!line.equals("")) {
                String[] items = line.split("\t");
                addWord (items[0], items[1], items[2]);
            }
        }
        dictReader.close();
    }
    
    private void dumpDictionary () throws Exception {
        
        File dumpedDict = new File(_pathToDump);
        if (!dumpedDict.exists()) {
            
            File binaryDict = new File (_pathToBinaryDict);
            if (!binaryDict.exists()) {
writeLog("WARNING : NO DICTIONARY OR DUMP PRESENT");
            } else {
writeLog("DICTIONARY DUMP MISSING : DUMPING DICTIONARY");
                PrintStream old = System.out;
                
                ByteArrayOutputStream corpusDumpText = new ByteArrayOutputStream();
                PrintStream writeCorpusDump = new PrintStream(corpusDumpText);
                System.setOut(writeCorpusDump);
                
                FSADumpTool.main("--raw-data", "-x", "-d", _pathToBinaryDict);
                System.out.flush();
                System.setOut(old);
                
                FileWriter write = new FileWriter(_pathToDump, false);
                PrintWriter printLine = new PrintWriter(write);
                
                printLine.println(corpusDumpText.toString());
                
                printLine.close();
                write.close();
            }
        }
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}
