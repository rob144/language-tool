package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.EnglishPatternRuleTest;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleTest;
import org.languagetool.tools.RuleAsXmlSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iparadigms.ipgrammar.VerbConjugationRule;

public class RuleTestServlet extends HttpServlet{
    
    //Logic control variables
    private final boolean testEachIPRule = false;
    private final boolean testProcessingTime = false;
    
    private final int CONTEXT_SIZE = 40; // characters
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
    //Tools
    private Language _lang;
    private JLanguageTool _langTool;
    private List<String> ruleIdsIP = new ArrayList<String>();
    private CorpusTextHolder _engCorpus;

    public RuleTestServlet() throws InstantiationException, IllegalAccessException, IOException{
        _lang = Language.getLanguageForShortName("en-GB").getClass().newInstance();
        _lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
        _langTool = new JLanguageTool(_lang);  
        
        //Add IP rules, record their IDs, then disable them
        PatternRuleLoader ruleLoader = new PatternRuleLoader();
        List<PatternRule> myRules = ruleLoader.getRules(
            TextEngineServlet.class.getResourceAsStream("MyRules.xml"), "MyRules.xml");
        for (PatternRule r : myRules) {
            _langTool.addRule(r);
            ruleIdsIP.add(r.getId());
        }
        
        //Java IP rules
        //_langTool.addRule(new VerbConjugationRule());
        
        //Default rules
        _langTool.activateDefaultPatternRules();
    }
    
    private String testingRules (String langCode, String ruleId, int lineTestStart, int lineTestLimit) throws IOException {
        
        _engCorpus = new CorpusTextHolder (langCode);
        lineTestLimit = lineTestLimit == 0 ? 20000 : lineTestLimit;
        
        if (testProcessingTime) {
            return testRulesProcessTime();
        } else {
            if (testEachIPRule)
                return testEachIPRule();
            else
                return testIPRule(ruleId, lineTestStart, lineTestLimit);
        }
    }
    
    private String testIPRule(String ruleId, int lineStart, int lineLimit) throws IOException {
        disableAllActiveRules();
        
        if (ruleId.equals("###"))
            for (String ruleID : ruleIdsIP)
                _langTool.enableRule(ruleID);
        else
            _langTool.enableRule(ruleId);
        
        List<RuleMatch> matches = _langTool.check(_engCorpus.getLinesToString(lineStart, lineLimit));
writeLog("Number of matches: " + matches.size());
        RuleAsXmlSerializer serializer = new RuleAsXmlSerializer();
        String xmlResponse = serializer.ruleMatchesToXml(matches, _engCorpus.getLinesToString(lineStart, lineLimit), CONTEXT_SIZE, _lang);
        return xmlResponse;
    }
    
    private String testRulesProcessTime () throws IOException {
        String formattedString = "";
        String corpusText = _engCorpus.getLinesToString(1000);
        List<RuleMatch> matches = _langTool.check(corpusText);
        
        for (int x = 1; x <= 60; x++) {
            corpusText = _engCorpus.getLinesToString(x*1000);
            
            writeLog("LOOP NUMBER : " + x);
            if (x != 1)
                formattedString += "\n";
            
            long startTime = System.nanoTime();
            matches = _langTool.check(corpusText);
            double timeElapsed = (double)((System.nanoTime() - startTime)/1000000000.0);
            
            formattedString += x*1000 + ",\t" + _langTool.getAllActiveRules().size()
                    + ",\t" + matches.size() + ",\t" + timeElapsed;
        }
        
        return formattedString;
    }
    
    private String testEachIPRule () throws IOException {
writeLog("TESTING EACH IP RULE");
        String corpusText = _engCorpus.getLinesToString(1000);
        disableAllActiveRules();
        
        String returnText = "";
        int matchesCount = 0;
        
        for (int x = 0; ruleIdsIP.size() > x; x++) {
writeLog("TESTING IP RULE " + x + " of " + ruleIdsIP.size());
            _langTool.enableRule(ruleIdsIP.get(x));
            if (x != 0)
                _langTool.disableRule(ruleIdsIP.get(x-1));
            
            List<RuleMatch> matches = _langTool.check(corpusText);
            
            matchesCount += matches.size();
            returnText += ruleIdsIP.get(x) + ", \t" + matches.size() + "\n";
        }
        returnText += "##########################\nRules checked : " + ruleIdsIP.size() + "\tTotal number of matches : " + matchesCount;
        return returnText;
    }
    
    public void disableAllActiveRules () {
        List<Rule> myRules = _langTool.getAllActiveRules();
        List<String> myRuleIds = new ArrayList<String>();
        
        for (Rule r : myRules)
            myRuleIds.add(r.getId());        
            
        _langTool.disableRules(myRuleIds);
    }
    
    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.getWriter().print(
            testingRules(
                req.getParameter("langcode"),
                req.getParameter("ruleid"),
                Integer.parseInt(req.getParameter("lineteststart")),
                Integer.parseInt(req.getParameter("linetestlimit"))
            )
        );
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}
