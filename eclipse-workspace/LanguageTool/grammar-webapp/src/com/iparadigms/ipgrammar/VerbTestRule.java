package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;

import com.iparadigms.ipgrammar.VerbTool;

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
    
        for (int i = 2; i < tokens.length; i++) {
      
            AnalyzedTokenReadings currentToken = tokens[i];
            AnalyzedTokenReadings prevToken = tokens[i-1];
            
            /* Check if the word before the previous word is a contraction 
             * e.g. shouldn't, can't, wouldn't etc. */
            String s = (i>=3) ? tokens[i-2].getToken() : "";
            if ( s.matches("t") ) s = (i>=4) ? tokens[i-3].getToken() + s : s;
            if ( s.matches("'t") ) s = (i>=5) ? tokens[i-4].getToken() + s : s;
            String prevPrevWord = s;

            /* Check if the current word is a contraction */
            s = (i < tokens.length - 3) ? tokens[i+1].getToken() : "";
            if ( s.matches("'") ) s += tokens[i+2].getToken();
            String currentWord = s.matches("'t") ? currentToken.getToken() + s : currentToken.getToken();
            
            if(prevToken.hasPosTag("PRP") && currentToken.hasPartialPosTag("VB")){
                if(prevToken.getToken().toLowerCase().matches("i|you|he|she|it|one|we|they")){
                    //Check that the verb conjugation agrees with the personal pronoun
                    if(verbTool.checkAgreement(prevPrevWord, prevToken.getToken(), currentWord) == false){    
                        RuleMatch match = new RuleMatch(this, currentToken.getStartPos(),
                            currentToken.getStartPos() + currentToken.getToken().length(), 
                            "Verb conjugation does not agree with subject", "Verb conjugation error");
                        ruleMatches.add(match);
                    }
                }
            } 
        }
        return toRuleMatchArray(ruleMatches);
    }

    private int findPattern(String pattern){
    	int result = -1;
    	
    	/* Patterns to catch */
    	//They came home. 				PRP VB*
    	//You and I ran. 				PRP and PRP VB*
    	//The cat and the dog jumped. 	DT NN and DT NN VB*
    	//Footballers earn a lot.  		NNS VB*

    	
    	pattern = "";
    	
    	//TODO: how many tokens in the pattern = numTokens
    	//Loop through sentence tokens looking for the first matching token in the pattern
    	//If find first pattern token, check if next token matches until end of pattern tokens.
    	//If all tokens match, return index of first matching token in the setence.
    	//Pass the matching tokens into the verb agreement tool stating the 'person' i.e. singular or plural?
    	//Let the verb agreement tool check if the subject verb pattern is valid, return true/false.
    	
    	return result;
    }
    
    @Override
    public void reset() {
        // nothing
    }

}
