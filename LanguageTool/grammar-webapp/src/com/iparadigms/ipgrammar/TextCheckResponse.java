package com.iparadigms.ipgrammar;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.RuleAsXmlSerializer;

public class TextCheckResponse {
	
	private String _text;
	private List<RuleMatch> _matches;
	private List<TextMatch> _textMatches;
	private int CONTEXT_SIZE = 40; 
	private Language _lang;

	public TextCheckResponse(String text, List<RuleMatch>matches, Language lang){
		_text = text;
		_matches = matches;
		_textMatches = buildMatches(matches);
		_lang = lang;
	}
	
	private List<TextMatch> buildMatches(List<RuleMatch> matches){
		
		List<TextMatch> newMatches = new ArrayList<TextMatch>();
		int idCounter = 1;
		
		for(RuleMatch m : matches){	
			TextMatch match = new TextMatch();
			match.ref = idCounter++;
			match.fromy = m.getLine();
			match.toy = m.getEndLine();
			match.fromx = m.getColumn() - 1; 
			match.tox =	m.getEndColumn() - 2;
			match.message = m.getMessage();
//System.out.println("BUILD MATCH: " + new Gson().toJson(match));
			newMatches.add(match);
		}
		return newMatches;
	}
	
	private List<TextMatch> getMatchesAtPosition(int lineNum, int charNum){
		
		List<TextMatch> matches = new ArrayList<TextMatch>();
		for(TextMatch m : _textMatches){	
			if(m.fromy == lineNum &&  m.fromx <= charNum &&  m.tox >= charNum){
				matches.add(m);
			}
		}
		return matches;
	}
	
	private ArrayList<Integer> getErrorIds(List<TextMatch> matches){
		
		ArrayList<Integer> matchIds = new ArrayList<Integer>();
		for(int i=0; i< matches.size(); i++){
			matchIds.add(matches.get(i).ref);
		}
		return matchIds;
	}
	
	private String getErrorMessages(List<TextMatch> matches){
		
		String messages = "";
		for(int i=0; i< matches.size(); i++){
			if(i == 0) messages = "<ul>";
			messages += "<li>" + matches.get(i).message + "</li>";
			if(i == matches.size() - 1) messages += "</ul>";
		}
		return messages;
	}
	
	private String getMarkupForLine( char[] arrChars, int lineNumber ) {
            
		String lineMarkup = "";
		
	    if(new String(arrChars).trim().length() == 0){
	    	lineMarkup = "<br/>";
        } else {
        	lineMarkup = "<p class='line'>";
        	for(int charNum = 0; charNum < arrChars.length; charNum++ ){
                lineMarkup += "<a id='" + lineNumber + "_" + charNum + "'>" + arrChars[charNum] + "</a>";
        	}
        	lineMarkup += "</p>";
        }
        return lineMarkup;
    }

	public String getJsonResponse(){
    	
        String[] arrLines = _text.split("(?:\r\n|\r|\n)");
        String[] arrHtmlLines = new String[arrLines.length];
        List<TextChar[]> listLineObjs = new ArrayList<TextChar[]>();
        TextCheckJsonResponse jsonResponse = new TextCheckJsonResponse();
        
		for (int lineNum = 0; lineNum < arrLines.length; lineNum++ ){
		    
		    char[] arrChars = arrLines[lineNum].toCharArray();
		    TextChar[] arrCharObjs = new TextChar[arrChars.length];
		    arrHtmlLines[lineNum] = getMarkupForLine( arrChars, lineNum );
		    
		    //Build the character objects
		    for(int charNum = 0; charNum < arrChars.length; charNum++){
		    	
		    	TextChar objChar 	= 	new TextChar();
		    	objChar.errors 		= 	getMatchesAtPosition(lineNum, charNum);
			    objChar.messages 	= 	getErrorMessages(objChar.errors);
			    objChar.errorIds 	= 	getErrorIds(objChar.errors);
			    objChar.character 	= 	String.valueOf(arrChars[charNum]);
			    
			    arrCharObjs[charNum] = objChar;
		    }
		    listLineObjs.add( arrCharObjs );    
		}

        jsonResponse.htmlLines = arrHtmlLines;
        jsonResponse.textData = listLineObjs;
        
    	return new GsonBuilder().setPrettyPrinting().create().toJson(jsonResponse);
    }
	
	public String getXmlResponse(){
        return new RuleAsXmlSerializer().ruleMatchesToXml(_matches, _text, CONTEXT_SIZE, _lang);
	}
}
