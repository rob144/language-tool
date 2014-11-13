package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.lang.AssertionError;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
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
public class RuleTestServlet extends WebSocketServlet{
    
    private final int CONTEXT_SIZE = 80; // characters
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
        
        SocketLogicBridge.inform(this);
    }
    
    private String testIPRules(String ruleId, int lineStart, int lineLimit) throws IOException {
    	
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
    
    private String findWordContext (String word, int lineStart, int lineLimit) throws IOException {
    	
    	List<String> posTagNames = new ArrayList<String>();
    	posTagNames.add("CC");
    	posTagNames.add("CD");
    	posTagNames.add("DT");
    	posTagNames.add("EX");
    	posTagNames.add("FW");
    	posTagNames.add("IN");
    	posTagNames.add("JJ");
    	posTagNames.add("JJR");
    	posTagNames.add("JJS");
    	posTagNames.add("LS");
    	posTagNames.add("MD");
    	posTagNames.add("NN");
    	posTagNames.add("NNS");
    	posTagNames.add("NN:U");
    	posTagNames.add("NN:UN");
    	posTagNames.add("NNP");
    	posTagNames.add("NNPS");
    	posTagNames.add("PDT");
    	posTagNames.add("POS");
    	posTagNames.add("PRP");
    	posTagNames.add("PRP$");
    	posTagNames.add("RB");
    	posTagNames.add("RBR");
    	posTagNames.add("RBS");
    	posTagNames.add("RP");
    	posTagNames.add("SYM");
    	posTagNames.add("TO");
    	posTagNames.add("UH");
    	posTagNames.add("VB");
    	posTagNames.add("VBD");
    	posTagNames.add("VBG");
    	posTagNames.add("VBN");
    	posTagNames.add("VBP");
    	posTagNames.add("VBZ");
    	posTagNames.add("WDT");
    	posTagNames.add("WP");
    	posTagNames.add("WP$");
    	posTagNames.add("WRB");
    	posTagNames.add("``");
    	posTagNames.add(",");
    	posTagNames.add("''");
    	posTagNames.add(".");
    	posTagNames.add(":");
    	posTagNames.add("$");
    	posTagNames.add("#");
    	posTagNames.add("SENT_START");
    	posTagNames.add("SENT_END");
    	
    	String[] exampleLinesPrevious = new String[47];
    	String[] exampleLinesProceeding = new String[47];
    	int[] preceedingTagsOccurence = new int[47];
    	int[] proceedingTagsOccurence = new int[47];
    	String[] corpus = _engCorpus.getLinesToString(lineStart, lineLimit).split("\n");
    	int z = 0;
    	
        String[] phrase = word.split(" ");

        for (String line : corpus) { //For each line in the corpus extract tokens
    	  	AnalyzedSentence s = _langTool.getAnalyzedSentence(line);
    	  	AnalyzedTokenReadings[] tokens = s.getTokensWithoutWhitespace();
    	  	
    	  	for (int x = 0; tokens.length > x; x++) { //For each token in the sentence

                boolean match = true;
                int count = 0;

                for (int b = 0; b < phrase.length; b++) { //Check if the token [and subsequent tokens if specified] matches
                	
                	boolean isPosTag = false;
                	String item = "";
                	
                	if (phrase[b].contains("%")) {
                		isPosTag = true;
                		item = phrase[b].replace("%", "");
                	}
                	
                    if (!tokens[(x+b)].getToken().equals(phrase[b]) && !isPosTag) { //If word (no %), check if matches
                    	match = false;
                        break;
                    }
                    if (!tokens[(x+b)].hasPosTag(item) && isPosTag) { //If tag (% present), check if matches
                    	match = false;
                    	break;
                    }
                    count = b + 1;
                }

    	  		if (match) { //Record POS tags of preceding and proceeding tokens if match found
    	  			for (int y = 0; tokens[x+count].getReadingsLength() > y; y++) {
    	  				if (tokens[x+count].getAnalyzedToken(y).getPOSTag() != null) {
    	  					z = posTagNames.indexOf(tokens[x+count].getAnalyzedToken(y).getPOSTag());
	    	  				proceedingTagsOccurence[z] += 1;
	    	  				
	    	  				if (exampleLinesProceeding[z] == null && tokens[x+count].getReadingsLength() == 1)
	    	  					exampleLinesProceeding[z] = line;
    	  				}
    	  			}
    	  			for (int y = 0; tokens[x-1].getReadingsLength() > y; y++) {
    	  				if (tokens[x-1].getAnalyzedToken(y).getPOSTag() != null) {
    	  					z = posTagNames.indexOf(tokens[x-1].getAnalyzedToken(y).getPOSTag());
    	  					preceedingTagsOccurence[z] += 1;
    	  					
    	  					if (exampleLinesPrevious[z] == null && tokens[x-1].getReadingsLength() == 1)
    	  						exampleLinesPrevious[z] = line;
    	  				}
    	  			}
    	  		}
    	  	}
    	}
    	
    	String output = "<tr><th>Tag</th><th>Pre</th><th>Post</th><th>Example</th></tr>";
    	for (int y = 0; preceedingTagsOccurence.length > y; y++)
    		output += "<tr><td>"
    			+ posTagNames.get(y) + "</td><td>"
    			+ preceedingTagsOccurence[y] + "</td><td>"
    			+ proceedingTagsOccurence[y] + "</td><td>"
    			+ exampleLinesPrevious[y]
    			+ "</td></tr>"
    			+"<tr><td></td><td></td><td></td><td>" + exampleLinesProceeding[y] + "</td></tr>";
    	
    	return output;
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
    
    public void poll(UpdateSocket socket, String request) throws IOException {
    	
    	System.out.println(request);
    	
    	String[] test = request.split(";");
    	
    	String output = "";
    	
    	if (test[0].equals("test_rule")) {
    		String[] parameters = test[1].split("\\.");
            output = testIPRules(parameters[0],
                Integer.parseInt(parameters[1]),
                Integer.parseInt(parameters[2]));
    	}
        if (test[0].equals("rule_competence"))
            output = testRuleCompetence();
        if (test[0].equals("false_positives"))
            output = ipRulesFalsePositive();
        if (test[0].equals("processing_time"))
            output = testRulesProcessTime();
        if (test[0].equals("context")) {
        	String[] parameters = test[1].split("\\.");
        	output = findWordContext(parameters[0],
        			Integer.parseInt(parameters[1]),
        			Integer.parseInt(parameters[2]));
        }
        
        SocketLogicBridge.setStatus(socket, output);
    }
    
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(100000);
        factory.register(UpdateSocket.class);
    }
    
    private void disableAllActiveRules () {
        List<Rule> myRules = _langTool.getAllActiveRules();
        List<String> myRuleIds = new ArrayList<String>();
        
        for (Rule r : myRules)
            myRuleIds.add(r.getId());        
            
        _langTool.disableRules(myRuleIds);
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}