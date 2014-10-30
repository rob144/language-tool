package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishPatternRuleTest;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleTest;
import org.languagetool.tools.RuleAsXmlSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iparadigms.ipgrammar.VerbConjugationRule;

@SuppressWarnings("serial")
public class RuleTestServlet extends HttpServlet{
    
    private final int CONTEXT_SIZE = 40; // characters
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
    //Tools
    private Language _lang;
    private JLanguageTool _langTool;
    private List<String> ruleIdsIP = new ArrayList<String>();
    private CorpusTextHolder _engCorpus;
    private List<PatternRule> _myRules;

    public RuleTestServlet() throws InstantiationException, IllegalAccessException, IOException{
        _lang = Language.getLanguageForShortName("en-GB").getClass().newInstance();
        _lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
        _langTool = new JLanguageTool(_lang);
        
        //ngram
        /*File languageModelIndexDir = new File("grammar-webapp/src/com/iparadigms/ipgrammar/resources/google-ngram-index");
        _langTool.activateLanguageModelRules(languageModelIndexDir);*/
        
        //Add IP rules, record their IDs, then disable them
        PatternRuleLoader ruleLoader = new PatternRuleLoader();
        _myRules = ruleLoader.getRules(
            TextEngineServlet.class.getResourceAsStream("MyRules.xml"), "MyRules.xml");
        for (PatternRule r : _myRules) {
            _langTool.addRule(r);
            ruleIdsIP.add(r.getId());
        }
        
        //Java IP rules
        _langTool.addRule(new VerbConjugationRule());
        
        //Default rules
        _langTool.activateDefaultPatternRules();
        
        //Load/extract corpus
        _engCorpus = new CorpusTextHolder ("eng");
        _engCorpus.getLinesToString(1000);
    }
    
    private String testIPRules(String ruleId, int lineStart, int lineLimit) throws IOException {
        
        lineLimit = lineLimit == 0 ? 20000 : lineLimit;
        disableAllActiveRules();
        
        if (ruleId.equals("###"))
            for (String ruleID : ruleIdsIP)
                _langTool.enableRule(ruleID);
        else
            _langTool.enableRule(ruleId);
        
        List<RuleMatch> matches = _langTool.check(_engCorpus.getLinesToString(lineStart, lineLimit));
writeLog("Number of matches: " + matches.size());
        
        //TODO : Either this side or client side figure a way to matches data in a more accessible fashion
        RuleAsXmlSerializer serializer = new RuleAsXmlSerializer();
        String xmlResponse = serializer.ruleMatchesToXml(matches, _engCorpus.getLinesToString(lineStart, lineLimit), CONTEXT_SIZE, _lang);
        return xmlResponse;
    }
    
    private String testRuleCompetence () {
        EnglishPatternRuleTest enRuleTest  = new EnglishPatternRuleTest ();
        PatternRuleTest ruleTest = new PatternRuleTest ();
        
        try {
            enRuleTest.testRulesForLanguage(_lang);
            ruleTest.testGrammarRulesFromXML(_myRules, _langTool, _langTool, _lang);
        } catch (IOException e) { e.printStackTrace();}
        
        return "";
    }
    
    private String ipRulesFalsePositive () throws IOException {
writeLog("TESTING EACH IP RULE");
        disableAllActiveRules();
        
        String corpusText = _engCorpus.getLinesToString(1000);
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
        returnText += "##########################\nRules checked : " 
        		+ ruleIdsIP.size() + "\tTotal number of matches : " + matchesCount;
        return returnText;
    }
    
    private String testRulesProcessTime () throws IOException {
        String formattedString = "<p>";
        String corpusText = _engCorpus.getLinesToString(1000);
        List<RuleMatch> matches = _langTool.check(corpusText);
        
        for (int x = 1; x <= 10; x++) {
            corpusText = _engCorpus.getLinesToString(x*1000);
            
            writeLog("LOOP NUMBER : " + x);
            if (x != 1)
                formattedString += "<br/>";
            
            long startTime = System.nanoTime();
            matches = _langTool.check(corpusText);
            double timeElapsed = (double)((System.nanoTime() - startTime)/1000000000.0);
            
            formattedString += x*1000 + ",\t" + _langTool.getAllActiveRules().size()
                    + ",\t" + matches.size() + ",\t" + timeElapsed;
        }
        formattedString += "</p>";
        return formattedString;
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
        
        writeLog("RUNNING TEST : " + req.getParameter("test"));
        
        //Need to start returning things to populate results div then writing them
        String output = "";
        
        if (req.getParameter("test").equals("test_rule"))
            output = testIPRules(req.getParameter("rule_id"),
                Integer.parseInt(req.getParameter("line_start")),
                Integer.parseInt(req.getParameter("line_limit")));
        
        if (req.getParameter("test").equals("rule_competence")) {
            output = testRuleCompetence();
        }
        
        if (req.getParameter("test").equals("false_positives"))
            output = ipRulesFalsePositive();
        
        if (req.getParameter("test").equals("processing_time"))
            output = testRulesProcessTime();
        
        resp.getWriter().print(output);
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}
