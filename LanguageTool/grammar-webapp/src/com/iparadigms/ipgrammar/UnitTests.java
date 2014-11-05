package com.iparadigms.ipgrammar;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import morfologik.tools.FSADumpTool;

import org.languagetool.dev.POSDictionaryBuilder;

public class UnitTests {
	private String resources = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/";
	private String test = "test/";
	private String temp = "temp/";
    private String originalDict = resources + test + "cmd.dict";
    private String originalInfo = resources + test + "cmd.info";
    private String originalDump = resources + test + "cmd.dump";
    private String selfEditedDump = resources + test + "self_edited.dump";
    private String selfEditedBuiltDump = resources + test + "self_edited_built.dump";
    
    public UnitTests(){
        
    }
    
    public String runTests () throws Exception {
    	String output = "";
    	
    	File directory = new File(resources + temp);
    	directory.mkdir();
    	
        if (testDictionaryDump() &&
                testPosDictionaryBuilder() &&
                addWordToMemoryThenDump() &&
        		testAddedWordsBuildDictionaryDump()
        		) {
            output = "Unit tests succeeded";
            directory.delete();
        }
        else
            output = "Unit tests failed";
        
        return output;
    }
    
    public boolean testDictionaryDump() throws Exception {
        //took original dict file, dumped it at command, then comparing java dump result
        String builtDump = resources + temp + "built.dump";
        
        //Reassigning output locale
        PrintStream old = System.out;
        ByteArrayOutputStream corpusDumpText = new ByteArrayOutputStream();
        PrintStream writeCorpusDump = new PrintStream(corpusDumpText);
        System.setOut(writeCorpusDump);
        
        //Dumping to output
        FSADumpTool.main("--raw-data", "-x", "-d", originalDict);
        System.out.flush();
        System.setOut(old);
        writeCorpusDump.close();
        
        //Moving dump to file
        PrintWriter printLine = new PrintWriter(new FileWriter(builtDump, false));
        printLine.print(corpusDumpText.toString());
        printLine.close();
        
        return compareFiles(originalDump, builtDump);
    }
    
    public boolean testPosDictionaryBuilder() throws Exception {
        //Took original dump and info, built dict with them, moved dict to local area, compared
        String builtDict = resources + temp + "built.dict";
        File dumpFile = new File (originalDump);
        File dictFile = new File (builtDict);
        
        //Building dict
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File(originalInfo));
        Files.copy(pos.build(dumpFile).toPath(), dictFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        return compareFiles(originalDict, builtDict);
    }
    
    public boolean addWordToMemoryThenDump() throws Exception {
        //This takes original dump, puts in memory, adds word, creates new dump, then compares to hand-made edited dump
        String editedDump = resources + temp + "built_edited.dump";
        
        //DUMP TO MEMORY
        ArrayList<String[]> posDictionary = new ArrayList<String[]>();
        String line = "";
        BufferedReader dictReader = new BufferedReader(new FileReader(originalDump));
        while ((line = dictReader.readLine()) != null){
            if (!line.equals("")) {
                String[] items = line.split("\t");
                posDictionary.add(new String[]{items[0], items[1], items[2]});
            }
        }
        dictReader.close();
        
        //ADD WORD
        String test = "component component NNS";
        posDictionary.add(test.split(" "));
        
        //MEMORY TO DUMP
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(editedDump)));
        
        for (int x = 0; x < posDictionary.size(); x++) {
            line = "";
            line = posDictionary.get(x)[0] + "\t"
                + posDictionary.get(x)[1] + "\t"
                + posDictionary.get(x)[2] + "\n";
            writer.print(line);
        }
        writer.close();
        
        return compareFiles(selfEditedDump, editedDump);
    }
    
    public boolean testAddedWordsBuildDictionaryDump() throws Exception {
        //Take self edited dump, build dictionary, dump again, compare
    	String editedbuiltDict = resources + temp + "built_edited.dict";
        String newBuiltInfo = resources + temp + "built_edited.info";
        
        //Build dictionary from self edited dump
        File dumpFile = new File (selfEditedBuiltDump);
        File dictFile = new File (editedbuiltDict);
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File(originalInfo));
        Files.copy(pos.build(dumpFile).toPath(), dictFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        //Dump edited dictionary
        String builtEditedDictDump = resources + temp + "built_edited_dict.dump";
        //IMPORTANT: CREATING INFO FILE TO MIRROR .DICT FILE in SAME DIRECTORY, non-explicit requirement for dumping
        File editedbuiltDictInfoFile = new File (newBuiltInfo);
        File originalInfoFile = new File (originalInfo);
        Files.copy(originalInfoFile.toPath(), editedbuiltDictInfoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        PrintStream old = System.out;
        ByteArrayOutputStream corpusDumpText = new ByteArrayOutputStream();
        PrintStream writeCorpusDump = new PrintStream(corpusDumpText);
        System.setOut(writeCorpusDump);
        FSADumpTool.main("--raw-data", "-x", "-d", editedbuiltDict);
        System.out.flush();
        System.setOut(old);
        writeCorpusDump.close();
        
        PrintWriter printLine = new PrintWriter(new FileWriter(builtEditedDictDump, false));
        printLine.print(corpusDumpText.toString());
        printLine.close();
        
        editedbuiltDictInfoFile.delete();
        dictFile.delete();
        
        return compareFiles(selfEditedBuiltDump, builtEditedDictDump);
    }
    
    private boolean compareFiles(String filePathOne, String filePathTwo) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(Paths.get(filePathOne))) {
          DigestInputStream dis = new DigestInputStream(is, md);
          /* Read stream to EOF as normal... */
          while(dis.read()!= -1);
          dis.close();
        }
        byte[] digest = md.digest();
        
        MessageDigest md2 = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(Paths.get(filePathTwo))) {
          DigestInputStream dis = new DigestInputStream(is, md2);
          /* Read stream to EOF as normal... */
          while(dis.read()!= -1);
          dis.close();
        }
        byte[] digest2 = md2.digest();
        
        File f = new File (filePathTwo);
        f.delete();
        
        if (Arrays.equals(digest, digest2))
            return true;
        else
            return false;
    }
}
