package com.iparadigms.ipgrammar;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import morfologik.tools.FSADumpTool;

import org.languagetool.dev.POSDictionaryBuilder;

public class POSDictionary {
    
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
    private String _resourcesDir = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/";
    private String _outputDir = "output/";
    
    private String _pathToInfoFile = _resourcesDir + "english.info";
    private String _pathToBinaryDict = _resourcesDir + "english.dict";
    
    private String _pathToDump = _resourcesDir + "dictionary.dump";
    
    private String _pathToTempDump = _resourcesDir + _outputDir + "temp.dump";
    private String _pathToNewDict = _resourcesDir + _outputDir + "new_english.dict";
    private String _pathToNewInfo = _resourcesDir + _outputDir + "new_english.info";
    
    private ArrayList<String[]> _posDictionary = new ArrayList<String[]>();
    
    public POSDictionary () throws Exception {
        dumpRequest();
        populateLocalDictionary();
    }
    
    public boolean searchWord (String line) {
        boolean itemFound = false;
        String[] items = line.split("\\.");
        
        for (String s[] : _posDictionary)
            if (s[0].equals(items[0]) && s[1].equals(items[1]) && s[2].equals(items[2]))
               itemFound = true;
        
        return itemFound;
    }
    
    public void addWord (String line) {
        _posDictionary.add(line.split("\\."));
    }
    
    public void addWord (String[] items) {
        _posDictionary.add(items);
    }
    
    public int getWordCount () {
        return _posDictionary.size();
    }
    
    public void buildPosDictionary () throws Exception {
        //Creates dump file from dictionary object in memory
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(_pathToTempDump)));
        for (int x = 0; x < _posDictionary.size(); x++)
            writer.print(_posDictionary.get(x)[0] + "\t"
                    + _posDictionary.get(x)[1] + "\t"
                    + _posDictionary.get(x)[2] + "\n");
        writer.close();
        
        //Creates new dictionary from memory dump file
        File tempDump = new File(_pathToTempDump);
        File newDict = new File (_pathToNewDict);
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File(_pathToInfoFile));
        Files.copy(pos.build(tempDump).toPath(), newDict.toPath(), StandardCopyOption.REPLACE_EXISTING);
        tempDump.delete();
        
        //Creating new info file to go with dictionary
        File oldInfo = new File (_pathToInfoFile);
        File newInfo = new File (_pathToNewInfo);
        Files.copy(oldInfo.toPath(), newInfo.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void populateLocalDictionary () throws Exception {
        //Reads through dump file and populates list in memory
        String line = "";
        BufferedReader dictReader = new BufferedReader(new FileReader(_pathToDump));
        while ((line = dictReader.readLine()) != null){
            if (!line.equals(""))
                addWord (line.split("\t"));
        }
        dictReader.close();
    }
    
    private void dumpRequest () throws Exception {
        //Necessary file objects for checking
        File dumpedDict = new File(_pathToDump);
        File binaryDict = new File (_pathToBinaryDict);
        File infoFile = new File (_pathToInfoFile);
        
        //Determines if dump is present to proceed dumping
        //Then checks if .dict file and .info files are present for dumping
        if (!dumpedDict.exists())
writeLog("DUMP FILE NOT FOUND AT : " + _resourcesDir + " : DUMPING");
            if (!binaryDict.exists())
writeLog("WARNING : NO DICTIONARY FILE FOUND AT : " + _resourcesDir);
            else
                if (!infoFile.exists())
writeLog("WARNING : NO INFO FILE FOUND AT : " + _resourcesDir);
                else
                    dumpDictionary();
    }
    
    private void dumpDictionary () throws Exception {
        //Change system.out output to ByteArrayOutputStream for capture, then dumps
        PrintStream old = System.out;
        ByteArrayOutputStream corpusDumpText = new ByteArrayOutputStream();
        System.setOut(new PrintStream(corpusDumpText));
        FSADumpTool.main("--raw-data", "-x", "-d", _pathToBinaryDict);
        System.out.flush();
        System.setOut(old);
        
        //Saves dump results to dump file
        PrintWriter printLine = new PrintWriter(new FileWriter(_pathToDump, false));
        printLine.print(corpusDumpText.toString());
        printLine.close();
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}
