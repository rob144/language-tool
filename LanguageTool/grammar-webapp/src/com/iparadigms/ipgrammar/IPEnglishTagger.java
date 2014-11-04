package com.iparadigms.ipgrammar;

import java.util.Locale;

import org.languagetool.tagging.BaseTagger;

public class IPEnglishTagger extends BaseTagger {
    
    private String fileName = "/en/english.dict";
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override
    public final String getFileName() {
      return this.fileName;    
    }
    
    public IPEnglishTagger() {
      super();
      setLocale(Locale.ENGLISH);
    }
}
