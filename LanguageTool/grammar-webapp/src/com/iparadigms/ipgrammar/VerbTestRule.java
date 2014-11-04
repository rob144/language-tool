package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;

import com.iparadigms.ipgrammar.VerbTool;

import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.en.EnglishRule;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.util.logging.Logger;

/**
 * Check if a conjugated verb agrees with the subject of the sentence
 * E.g. He goes (OK), He went (OK), He gone (incorrect).
 * This rule loads a dictionary of verb conjugations from an external text file.
 * 
 * @author Robin Dunn
 */
public class VerbTestRule extends EnglishRule {

    private VerbTool verbTool;
    private ArrayList<Element[]> patterns;
    
    private final Logger LOG = Logger.getLogger(VerbTestRule.class.getName());

    public VerbTestRule() throws java.io.IOException {
        /*if (messages != null) {
            super.setCategory(new Category(messages.getString("category_misc")));
        }
        */
        setLocQualityIssueType(ITSIssueType.Misspelling);
        addExamplePair(Example.wrong("He <marker>go</marker> home."),
                   Example.fixed("He <marker>went</marker> home."));
        verbTool = new VerbTool();
        
    	/* Patterns to catch */
    	//They came home. 				PRP VB*
    	//You and I ran. 				PRP and PRP VB*
    	//The cat and the dog jumped. 	DT NN and DT NN VB*
    	//Footballers earn a lot.  		NNS VB*
    	boolean f = false;
    	boolean t = true;
    	
        patterns = new ArrayList<Element[]>();
    	
    	patterns.add(new Element[]{ 
    		new Element("PRP", f, f, f),
    		new Element("VB*", f, t, f)
    	});
    	
    	patterns.add(new Element[]{  
			new Element("PRP", f, f, f),
			new Element("and", f, f, f),
			new Element("PRP", f, f, f),
			new Element("VB*", f, t, f)
    	});
    	
    	patterns.add(new Element[]{ 
			new Element("DT", f, f, f),
			new Element("NN", f, f, f),
			new Element("and", f, f, f),
			new Element("DT", f, f, f),
			new Element("NN", f, f, f),
			new Element("VB*", f, t, f)
    	});
    	
    	patterns.add(new Element[]{  
			new Element("NNS", f, f, f),
			new Element("VB*", f, t, f)
    	});
    	
    }

    @Override
    public String getId() {
        return "EN_VERB_TEST_CONJUGATION";
    }

    @Override
    public String getDescription() {
        return "Verb conjugation should agree with subject.";
    }

    @Override
    public RuleMatch[] match(final AnalyzedSentence sentence) {

        final List<RuleMatch> ruleMatches = new ArrayList<>();
        final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();	
    	
    	//TODO: FOR EACH PATTERN: how many tokens in the pattern = numTokens
    	for(Element[] pattern : patterns){
    		//Loop through sentence tokens looking for the first matching token in the pattern
    		for (int i = 2; i < tokens.length; i++) {
    			//If find first pattern token, check if next token matches until end of pattern tokens.
    			//If all tokens match, return index of first matching token in the setence.
    	    	//Pass the matching tokens into the verb agreement tool stating the 'person' i.e. singular or plural?
    	    	//Let the verb agreement tool check if the subject verb pattern is valid, return true/false.
    			/*if(verbTool.checkAgreement(prevPrevWord, prevToken.getToken(), currentWord) == false){    
                    RuleMatch match = new RuleMatch(this, currentToken.getStartPos(),
                        currentToken.getStartPos() + currentToken.getToken().length(), 
                        "Verb conjugation does not agree with subject", "Verb conjugation error");
                    ruleMatches.add(match);
                }*/
    		}
    	}
    	
        return toRuleMatchArray(ruleMatches);
    }
    
    @Override
    public void reset() {
        // nothing
    }

}
