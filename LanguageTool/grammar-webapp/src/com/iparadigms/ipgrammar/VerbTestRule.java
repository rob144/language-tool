package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.List;

import com.iparadigms.ipgrammar.VerbTool;
import com.iparadigms.ipgrammar.PatternElement;

import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.en.EnglishRule;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
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
        setLocQualityIssueType(ITSIssueType.Grammar);
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

    public List<RuleMatch> matchPattern(Element[] pattern, AnalyzedTokenReadings[] sentenceTokens){
    	
    	final List<RuleMatch> ruleMatches = new ArrayList<>();
   
    		for (int i = 0, elemIndex = 0; elemIndex < pattern.length && i < sentenceTokens.length; i++) {
    			//If find first pattern token, check if next token matches until end of pattern tokens.
    		
System.out.println("analysed token: " + sentenceTokens[i].getAnalyzedToken(0) 
		+ " token.getPOSTag() ["+ sentenceTokens[i].getAnalyzedToken(0).getPOSTag() +"]");

    			if(pattern[elemIndex].isMatched(sentenceTokens[i].getAnalyzedToken(0))){
    				
System.out.println("matched token: " + sentenceTokens[i].getToken());

					if(elemIndex < pattern.length - 1){
						elemIndex++;
						continue;
					}
						
					//If all tokens match, return index of first matching token in the sentence.
    				if(elemIndex == pattern.length - 1){
    					AnalyzedTokenReadings tok = sentenceTokens[i];
						RuleMatch match = new RuleMatch(
							this, tok.getStartPos(),
							tok.getStartPos() + tok.getToken().length(), 
		                    "verb test rule", 
		                    "verb test rule"
	    	            );
						ruleMatches.add(match);
						return ruleMatches;
    				}
    	            
    			}
    			
    	    	//TODO: Pass the matching tokens into the verb agreement tool stating the 'person' i.e. singular or plural?
    	    	//Let the verb agreement tool check if the subject verb pattern is valid, return true/false.
    			/*if(verbTool.checkAgreement(prevPrevWord, prevToken.getToken(), currentWord) == false){    
                    RuleMatch match = new RuleMatch(this, currentToken.getStartPos(),
                        currentToken.getStartPos() + currentToken.getToken().length(), 
                        "Verb conjugation does not agree with subject", "Verb conjugation error");
                    ruleMatches.add(match);
                }*/
    		}
    	
    	return ruleMatches;
    }
    
    @Override
    public RuleMatch[] match(final AnalyzedSentence sentence) {

        List<RuleMatch> ruleMatches = new ArrayList<>();
        final AnalyzedTokenReadings[] sentenceTokens = sentence.getTokensWithoutWhitespace();	
    	
    	/* Example Patterns to catch */
    	//They came home. 				PRP VB*
    	//You and I ran. 				PRP and PRP VB*
    	//The cat and the dog jumped. 	DT NN and DT NN VB*
    	//Footballers earn a lot.  		NNS VB*
        
        ArrayList<PatternElement[]> patterns = new ArrayList<PatternElement[]>();

        ruleMatches = matchPattern(
        	new PatternElement[]{ 
        		new PatternElement.Builder().posString("PRP").build(),
        		new PatternElement.Builder().posString("VB.*").posRegExp(1).build()
        	}, sentenceTokens
        );
        
    	//TODO: if there is a match, feed into verbtool
    	//TODO: if verbtool returns false, there is an error.
    	//For each match in ruleMatches do verbTool.checkAgreement(prevWord, subject, verb);
    	
    	patterns.add(new PatternElement[]{
			new PatternElement.Builder().posString("PRP").build(),
			new PatternElement.Builder().tokenString("and").build(),
			new PatternElement.Builder().posString("PRP").build(),
			new PatternElement.Builder().posString("VB.*").posRegExp(1).build()
    	});
    	
    	patterns.add(new PatternElement[]{ 
			new PatternElement.Builder().posString("DT").build(),
			new PatternElement.Builder().posString("NN").build(),
			new PatternElement.Builder().tokenString("and").build(),
			new PatternElement.Builder().posString("DT").build(),
			new PatternElement.Builder().posString("NN").build(),
			new PatternElement.Builder().posString("VB.*").posRegExp(1).build()
    	});
    	
    	patterns.add(new PatternElement[]{  
    		new PatternElement.Builder().posString("NNS").build(),
    		new PatternElement.Builder().posString("VB.*").posRegExp(1).build()
    	});

System.out.println("num matches: " + ruleMatches.size());

        return toRuleMatchArray(ruleMatches);
    }
    
    @Override
    public void reset() {
        // nothing
    }

}