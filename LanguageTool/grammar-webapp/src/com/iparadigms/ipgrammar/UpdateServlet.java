package com.iparadigms.ipgrammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishPatternRuleTest;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternRuleTest;
import org.languagetool.tools.RuleAsXmlSerializer;
 
@SuppressWarnings("serial")
public class UpdateServlet extends WebSocketServlet {
 
	private static ArrayList<UpdateSocket> _activeSockets = new ArrayList<UpdateSocket>();
	
    private final int CONTEXT_SIZE = 40; // characters
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
    //Tools
    private Language _lang;
    private JLanguageTool _langTool;
    private List<String> ruleIdsIP = new ArrayList<String>();
    private CorpusTextHolder _engCorpus;
    private List<PatternRule> _myRules;
    
    public UpdateServlet() throws InstantiationException, IllegalAccessException, IOException{
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
        
        //TODO : Either this side or client side figure a way to matches data in a more accessible fashion
        RuleAsXmlSerializer serializer = new RuleAsXmlSerializer();
        String xmlResponse = serializer.ruleMatchesToXml(matches, _engCorpus.getLinesToString(lineStart, lineLimit), CONTEXT_SIZE, _lang);
        return xmlResponse;
    }
    
    private String testRuleCompetence () throws IOException {
        EnglishPatternRuleTest enRuleTest  = new EnglishPatternRuleTest ();
        PatternRuleTest ruleTest = new PatternRuleTest ();
        
        try {
            enRuleTest.testRulesForLanguage(_lang);
            ruleTest.testGrammarRulesFromXML(_myRules, _langTool, _langTool, _lang);
        } catch (AssertionError a) {
            String s = a.toString();
            String p = s.substring(s.indexOf("IP_"), s.indexOf(":", s.indexOf("IP_")));
            return "First error found with rule : " + p;
        }
        return "Rules OK";
    }
    
    private String ipRulesFalsePositive () throws IOException {
writeLog("TESTING EACH IP RULE");
        disableAllActiveRules();
        
        String corpusText = _engCorpus.getLinesToString(5000);
        String returnText = "";
        
        for (int x = 0; ruleIdsIP.size() > x; x++) {
writeLog("TESTING IP RULE " + (x+1) + " of " + ruleIdsIP.size());
            _langTool.enableRule(ruleIdsIP.get(x));
            if (x != 0) {
                _langTool.disableRule(ruleIdsIP.get(x-1));
                returnText += ":";
            }
            
            List<RuleMatch> matches = _langTool.check(corpusText);
            returnText += ruleIdsIP.get(x) + "," + matches.size();
        }
        
        return returnText;
    }
    
    private String testRulesProcessTime () throws IOException {
        String result = "";
        String corpusText = _engCorpus.getLinesToString(1000);
        List<RuleMatch> matches = _langTool.check(corpusText);
        
        for (int x = 1; x <= 10; x++) {
writeLog("LOOP NUMBER : " + x);
            corpusText = _engCorpus.getLinesToString(x*1000);
            
            if (x != 1)
                result += ":";
            
            long startTime = System.nanoTime();
            matches = _langTool.check(corpusText);
            double timeElapsed = (double)((System.nanoTime() - startTime)/1000000000.0);
            
            result += (x*1000) + "," + _langTool.getAllActiveRules().size()
                    + "," + matches.size() + "," + timeElapsed;
        }
        return result;
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

        String output = null;
        
        if (req.getParameter("test").equals("test_rule"))
            output = testIPRules(req.getParameter("rule_id"),
                Integer.parseInt(req.getParameter("line_start")),
                Integer.parseInt(req.getParameter("line_limit")));
        
        if (req.getParameter("test").equals("rule_competence"))
            output = testRuleCompetence();
        
        if (req.getParameter("test").equals("false_positives"))
            output = ipRulesFalsePositive();
        
        if (req.getParameter("test").equals("processing_time"))
            output = testRulesProcessTime();
        
writeLog("RETURNING TEST OUTPUT");
        resp.getWriter().print(output);
    }
    
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(10000);
        factory.register(UpdateSocket.class);
    }
    
    public static void addActiveSocket (UpdateSocket socket) throws IOException {
    	_activeSockets.add(socket);
    }
    
    public static void removeActiveSocket (UpdateSocket socket) throws IOException {
    	_activeSockets.remove(socket);
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}