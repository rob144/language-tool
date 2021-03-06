package com.iparadigms.ipgrammar;
import org.languagetool.language.English;
import org.languagetool.tagging.Tagger;

public class IPEnglish extends English {
    
    private Tagger tagger;
    
    //Pass in instance of IPEnglishTagger
    @Override
    public Tagger getTagger() {
      if (tagger == null) {
        tagger = new IPEnglishTagger();
      }
      return tagger;
    }
    
}
