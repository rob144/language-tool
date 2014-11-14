package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.lang.AssertionError;

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

import com.iparadigms.ipgrammar.VerbConjugationRule;

@SuppressWarnings("serial")
public class RuleTestServlet extends WebSocketServlet{
    
    private final int CONTEXT_SIZE = 80; // characters
    
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
        
        SocketLogicBridge.logicInstance(this);
    }
    
    private void testIPRules(UpdateSocket socket, String ruleId, int lineStart, int lineLimit) {
    	
    	String prefix = "test_rules#,#";
    	String update = prefix + "Testing rule " +
    		ruleId + " on " + (lineStart+lineLimit) + " lines";
    	sendSocketMessage(socket, update, "update");
        disableAllActiveRules();
        
        if (ruleId.equals("###"))
            for (String ruleID : ruleIdsIP)
                _langTool.enableRule(ruleID);
        else
            _langTool.enableRule(ruleId);
        
		try {
			List<RuleMatch> matches;
			matches = _langTool.check(_engCorpus.getLinesToString(lineStart, lineLimit));
			RuleAsXmlSerializer serializer = new RuleAsXmlSerializer();
	        String xmlResponse = prefix + 
	        		serializer.ruleMatchesToXml(matches,
	        				_engCorpus.getLinesToString(lineStart, lineLimit),
	        				CONTEXT_SIZE, _lang);
	        sendSocketMessage(socket, xmlResponse, "result");
		} catch (IOException e) { e.printStackTrace(); }
    }
    
    private void findWordContext(UpdateSocket socket, String word,
    		int lineStart, int lineLimit) throws IOException {
    	
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

                for (int b = 0; b < phrase.length; b++) {
                	//Check if the token [and subsequent tokens if specified] matches
                	
                	boolean isPosTag = false;
                	String item = "";
                	
                	if (phrase[b].contains("%")) {
                		isPosTag = true;
                		item = phrase[b].replace("%", "");
                	}
                	
                    if (!tokens[(x+b)].getToken().equals(phrase[b]) && !isPosTag) {
                    	//If word (no %), check if matches
                    	match = false;
                        break;
                    }
                    if (!tokens[(x+b)].hasPosTag(item) && isPosTag) {
                    	//If tag (% present), check if matches
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
    	
        String prefix = "context#,#";
    	String output = prefix + "<tr><th>Tag</th><th>Pre</th><th>Post</th><th>Example</th></tr>";
    	for (int y = 0; preceedingTagsOccurence.length > y; y++)
    		output += "<tr><td>"
    			+ posTagNames.get(y) + "</td><td>"
    			+ preceedingTagsOccurence[y] + "</td><td>"
    			+ proceedingTagsOccurence[y] + "</td><td>"
    			+ exampleLinesPrevious[y]
    			+ "</td></tr>"
    			+"<tr><td></td><td></td><td></td><td>" + exampleLinesProceeding[y] + "</td></tr>";
    	
    	sendSocketMessage(socket, output, "result");
    }
    
    private void testRuleCompetence(UpdateSocket socket) throws IOException {
    	
    	String prefix = "rule_competence#,#";
    	
    	String output = prefix + "Testing rule competence";
    	sendSocketMessage(socket, output, "update");
    	
    	output = prefix + "Rules OK";
        EnglishPatternRuleTest enRuleTest  = new EnglishPatternRuleTest ();
        PatternRuleTest ruleTest = new PatternRuleTest ();
        
        try {
            enRuleTest.testRulesForLanguage(_lang);
            ruleTest.testGrammarRulesFromXML(_myRules, _langTool, _langTool, _lang);
        } catch (AssertionError a) {
            String error = a.toString();
            String ruleId = error.substring(error.indexOf("IP_"), error.indexOf(":", error.indexOf("IP_")));
            output = prefix + "First error found with rule : " + ruleId;
        }
        sendSocketMessage(socket, output, "result");
    }
    
    private void ipRulesFalsePositive(UpdateSocket socket) throws IOException {
    	
    	String prefix = "false_positives#,#";
    	disableAllActiveRules();
        
        String returnText = prefix
        		+ "<tr><th>Rule ID</th><th>Matches</th></tr>";
        
        String corpusText = _engCorpus.getLinesToString(5000);
        
        for (int x = 0; ruleIdsIP.size() > x; x++) {
        	String update = prefix + "TESTING IP RULE "
        		+ (x+1) + " of " + ruleIdsIP.size();
        	sendSocketMessage(socket, update, "update");
        	
            _langTool.enableRule(ruleIdsIP.get(x));
            if (x != 0)
                _langTool.disableRule(ruleIdsIP.get(x-1));
            
            List<RuleMatch> matches = _langTool.check(corpusText);
            returnText += "<tr><td>" + ruleIdsIP.get(x)
            	+ "</td><td>" + matches.size() + "</td></tr>";
        }
        sendSocketMessage(socket, returnText, "result");
    }
    
    private void testRulesProcessTime(UpdateSocket socket) throws IOException {
    	
    	String prefix = "process_time#,#";
    	
        String corpusText = _engCorpus.getLinesToString(1000);
        List<RuleMatch> matches = _langTool.check(corpusText);
        
        String result = prefix + 
        		"<tr><th>Lines Used</th><th>Rules Used</th><th>Matches</th><th>Time Taken</th></tr>";
        
        for (int x = 1; x <= 10; x++) {
        	String update = prefix + "LOOP NUMBER : " + x;
        	sendSocketMessage(socket, update, "update");
        	
            corpusText = _engCorpus.getLinesToString(x*1000);
            long startTime = System.nanoTime();
            matches = _langTool.check(corpusText);
            double timeElapsed = (double)((System.nanoTime() - startTime)/1000000000.0);
            
            result += "<tr><td>" + (x*1000) + "</td><td>"
            		+ _langTool.getAllActiveRules().size()
            		+ "</td><td>" + matches.size() + "</td><td>" + timeElapsed + "</td></tr>";
        }
        sendSocketMessage(socket, result, "result");
    }
    
    public void handleRequest(UpdateSocket socket, String request) throws IOException {
    	
    	String[] test = request.split(",");
    	
    	switch (test[0]){
    	case "test_rule":
    		String[] parametersRules = test[1].split("\\.");
            testIPRules(socket,
            	parametersRules[0],
            	Integer.parseInt(parametersRules[1]),
            	Integer.parseInt(parametersRules[2]));
            break;
    	case "rule_competence":
    		testRuleCompetence(socket);
    		break;
    	case "false_positives":
    		ipRulesFalsePositive(socket);
    		break;
    	case "processing_time":
    		testRulesProcessTime(socket);
    		break;
    	case "context":
    		String[] parametersContext = test[1].split("\\.");
        	findWordContext(socket,
        		parametersContext[0],
        		Integer.parseInt(parametersContext[1]),
        		Integer.parseInt(parametersContext[2]));
        	break;
    	}
    }
    
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(100000);
        factory.register(UpdateSocket.class);
    }
    
    private void sendSocketMessage(UpdateSocket socket, String output, String option){
    	
    	if (option.equals("update"))
    		output = "update#;#" + output;
    	if (option.equals("result"))
    		output = "result#;#" + output;
    	
    	try {
			socket.sendMessage(output);
		} catch (IOException e) { e.printStackTrace(); }
    }
    
    private void disableAllActiveRules() {
        List<Rule> myRules = _langTool.getAllActiveRules();
        List<String> myRuleIds = new ArrayList<String>();
        
        for (Rule r : myRules)
            myRuleIds.add(r.getId());        
            
        _langTool.disableRules(myRuleIds);
    }
}