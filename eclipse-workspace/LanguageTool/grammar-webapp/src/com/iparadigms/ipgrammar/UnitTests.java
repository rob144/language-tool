package com.iparadigms.ipgrammar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import morfologik.tools.FSADumpTool;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.dev.POSDictionaryBuilder;

public class UnitTests {

    private Language _lang;
    private JLanguageTool _langTool;
    private String _resourcesDir = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/";
    private String _pathToBinaryDict = _resourcesDir + "english.dict";
    private String _pathToTextDict = _resourcesDir + "dictionary.dump";
    private String _pathToInfoFile = _resourcesDir + "english.info";
    
    public UnitTests() throws Exception{
        if (dumpDictionaryToMemory()
                && testSetDictionaryFileName()
                && testPosDictionaryBuilder())
            System.out.println("Unit tests succeeded");
        else
            System.out.println("Unit tests failed");
    }
    
    public boolean dumpDictionaryToMemory() throws Exception {
        PrintStream old = System.out;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
        
        FSADumpTool.main("--raw-data", "-d", _pathToBinaryDict);
        System.out.flush();
        System.setOut(old);
        
        String[] lineArray = baos.toString().split("\n");
        String[][] multiArray = new String[lineArray.length][];
        for (int x = 0; x < lineArray.length; x++)
            multiArray[x] = lineArray[x].split("\\+");
        
        if (multiArray.length > 2)
            return true;
        else
            return false;
    }
    
    public boolean testSetDictionaryFileName() throws Exception {
        //_lang = Language.getLanguageForShortName("en-GB").getClass().newInstance();
        _lang = new IPEnglish();
        _lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
        
        EnglishTagger tagger = (EnglishTagger) _langTool.getLanguage().getTagger();
        System.out.println("Tagger dictionary path: " + tagger.getFileName());
        tagger.setFileName("/whatever");
        
        _langTool = new JLanguageTool(_lang);
        
        if (tagger.getFileName().equals("/whatever"))
            return true;
        else
            return false;
    }
    
    public boolean testPosDictionaryBuilder() throws Exception {
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File(_pathToInfoFile));
        pos.build(new File(_pathToTextDict)).renameTo(new File(_pathToBinaryDict));
        
        if (new File(_pathToBinaryDict).exists())
            return true;
        else
            return false;
    }
}