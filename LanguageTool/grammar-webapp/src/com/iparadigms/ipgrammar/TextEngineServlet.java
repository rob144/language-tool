package com.iparadigms.ipgrammar;

import java.util.List;
import java.io.IOException;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.RuleAsXmlSerializer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.iparadigms.ipgrammar.VerbConjugationRule;

@SuppressWarnings("serial")
public class TextEngineServlet extends HttpServlet{

    private final int CONTEXT_SIZE = 40; // characters
    private final String RULES_FILENAME = "MyRules.xml";
    private final Logger LOG = Logger.getLogger(TextEngineServlet.class.getName());
    private Language _lang;
    private JLanguageTool _langTool;
    private List<PatternRule> _myRules;

    public TextEngineServlet(String langCode) throws InstantiationException, IllegalAccessException, IOException{
        //langCode e.g. en-GB, en-US.
        _lang = Language.getLanguageForShortName(langCode).getClass().newInstance();
        _lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
        _langTool = new JLanguageTool(_lang);

LOG.log(Level.INFO, "ADDING RULES.");
        try {
            PatternRuleLoader ruleLoader = new PatternRuleLoader();
            _myRules = ruleLoader.getRules(
                TextEngineServlet.class.getResourceAsStream(RULES_FILENAME), RULES_FILENAME);
            for (PatternRule rule : _myRules) {
                _langTool.addRule(rule);
            }
        }catch (NullPointerException ex) { LOG.log(Level.INFO, ex.toString()); }
        
        //_langTool.addRule(new VerbTestRule());
        
        _langTool.activateDefaultPatternRules();
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
LOG.log(Level.INFO, "***Post Servlet****");
        
        String results = "";
        String text = req.getParameter("text");
        
        resp.setContentType("text/html");

        try{
            results = doCheck(text);   
        }catch(Exception ex){
            throw new ServletException("Exception thrown in TextEngineServlet doPost()", ex);
        }

        resp.getWriter().print( results );
    }

    public String doCheck(String text) throws IOException, InstantiationException, IllegalAccessException
    {   
        List<RuleMatch> matches = _langTool.check(text);
        //TODO: add markup to plain text i.e. <br> and <p> tags for lines
        String[] strArrLines = text.split("(?:\r\n|\r|\n)");
System.out.println("text lines: " + strArrLines.length);
LOG.log(Level.INFO, "Number of matches: {0}", matches.size() );
        final RuleAsXmlSerializer serializer = new RuleAsXmlSerializer();
        //Do we need to send XML?
        //TODO: build the line and char objects to contain error ids and messages
        final String xmlResponse = serializer.ruleMatchesToXml(matches, text, CONTEXT_SIZE, _lang);
        return xmlResponse;
    }
}