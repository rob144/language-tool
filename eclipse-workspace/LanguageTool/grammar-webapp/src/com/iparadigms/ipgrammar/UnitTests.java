package com.iparadigms.ipgrammar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import morfologik.tools.FSADumpTool;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.dev.POSDictionaryBuilder;

public class UnitTests {

    private Language _lang;
    private JLanguageTool _langTool;
    private List<PatternRule> _myRules;

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
        
        FSADumpTool.main("--raw-data", "-d", "grammar-webapp/src/com/iparadigms/ipgrammar/resources/english.dict");
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
        _langTool = new JLanguageTool(_lang);
        EnglishTagger tagger = (EnglishTagger) _langTool.getLanguage().getTagger();
        System.out.println("Tagger dictionary path: " + tagger.getFileName());
        tagger.setFileName("/whatever");
        
        if (tagger.getFileName().equals("/whatever"))
            return true;
        else
            return false;
    }
    
    public boolean testPosDictionaryBuilder() throws Exception {
        POSDictionaryBuilder pos = new POSDictionaryBuilder(new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/english.info"));
        pos.build(
                new File(
                        "grammar-webapp/src/com/iparadigms/ipgrammar/resources/dictionary.dump")).renameTo(
                                new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/dictionary.dict"));
        
        if (new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/dictionary.dict").exists())
            return true;
        else
            return false;
    }
}
