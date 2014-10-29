package com.iparadigms.ipgrammar;
/* 
* Adapted from original code written in Python by Tom De Smedt <tomdesmedt@organisms.be>
* The verb.txt morphology was adopted from the XTAG morph_englis.flat:
* http://www.cis.upenn.edu/~xtag/
*/

import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.logging.Logger;
import java.util.logging.Level;

public class VerbTool {

    private HashMap<String, Integer> verb_tenses_keys = new HashMap<String, Integer>();
    private HashMap<String, String> verb_tenses_aliases = new HashMap<String, String>();
    private HashMap<String, String[]> verb_tenses = new HashMap<String, String[]>();
    private Map<String, String> verb_lemmas = new HashMap<String, String>();
    private String VERBS_FILENAME = "verb.txt";
    
    private final Logger LOG = Logger.getLogger(VerbTool.class.getName());

    public VerbTool() throws IOException {
        
        verb_tenses_keys.put( "infinitive"           , 0 );
        verb_tenses_keys.put( "1st singular present" , 1 );
        verb_tenses_keys.put( "2nd singular present" , 2 );
        verb_tenses_keys.put( "3rd singular present" , 3 );
        verb_tenses_keys.put( "present plural"       , 4 );
        verb_tenses_keys.put( "present participle"   , 5 );
        verb_tenses_keys.put( "1st singular past"    , 6 );
        verb_tenses_keys.put( "2nd singular past"    , 7 );
        verb_tenses_keys.put( "3rd singular past"    , 8 );
        verb_tenses_keys.put( "past plural"          , 9 );
        verb_tenses_keys.put( "past"                 , 10 );
        verb_tenses_keys.put( "past participle"      , 11 );

        verb_tenses_aliases.put( "inf"      ,   "infinitive"            );
        verb_tenses_aliases.put( "1sgpres"  ,   "1st singular present"  );
        verb_tenses_aliases.put( "2sgpres"  ,   "2nd singular present"  );
        verb_tenses_aliases.put( "3sgpres"  ,   "3rd singular present"  );
        verb_tenses_aliases.put( "pl"       ,   "present plural"        );
        verb_tenses_aliases.put( "prog"     ,   "present participle"    );
        verb_tenses_aliases.put( "1sgpast"  ,   "1st singular past"     );
        verb_tenses_aliases.put( "2sgpast"  ,   "2nd singular past"     );
        verb_tenses_aliases.put( "3sgpast"  ,   "3rd singular past"     );
        verb_tenses_aliases.put( "pastpl"   ,   "past plural"           );
        verb_tenses_aliases.put( "ppart"    ,   "past participle"       );

        /* Each verb has morphs for infinitve,
        * 3rd singular present, present participle,
        * past and past participle.
        * Verbs like "be" have other morphs as well
        * (i.e. I am, you are, she is, they aren't);
        * Additionally, the following verbs can be negated:
        * be, can, do, will, must, have, may, need, dare, ought.
        */
        
        String[] data = loadVerbData();
        
        for(int i = 0; i < data.length; i++){
            String[] a = data[i].trim().split(",");
//if(data[i].toLowerCase().contains("better")) System.out.println("data: " + data[i]);
//if(a.length >= 2) System.out.println("load array for verb" + a[0] + " length: " + a.length );
            if( a.length >= 2) verb_tenses.put(a[0], a);
        }

        /* Each verb can be lemmatised:
        * inflected morphs of the verb point
        * to its infinitive in this dictionary.
        */

        for (Map.Entry<String, String[]> infinitive : verb_tenses.entrySet()) {
            for (String tense : infinitive.getValue()) {
                if( tense != "" )  verb_lemmas.put(tense, infinitive.getKey());
            }
        }
    }

    private String[] loadVerbData(){
       
        String line = null;
        ArrayList<String> stringList = new ArrayList<String>();
        
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                VerbTool.class.getResourceAsStream(VERBS_FILENAME)));
            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }
            reader.close();
        }catch(IOException ex){
             System.out.println(ex.getMessage());
        }

        return stringList.toArray(new String[]{});
    }

    public boolean checkAgreement(String prevWord, String subject, String verb){

        boolean result = false;
        /* subject of the verb e.g. in He loves her "he" is the subject
        * verb e.g. in He loves her "loves" is the verbi
        * */
        
        if(verb.toLowerCase().contains("n't")) verb = verb.replace("n't","");

        String infinitive = verb_infinitive(verb);

        /* If it's a real verb, i.e. can we find the infinitive?, do the conjugation check */
        if ( infinitive != null && infinitive.matches("[a-zA-Z]+") ){
          
            HashMap<String, String> options = new HashMap<String, String>();
            options.put( "i"    ,   "first_person_sing"     );
            options.put( "you"  ,   "second_person"         );
            options.put( "he"   ,   "third_person_sing"     );
            options.put( "she"  ,   "third_person_sing"     );
            options.put( "it"   ,   "third_person_sing"     );
            options.put( "one"  ,   "third_person_sing"     );
            options.put( "we"   ,   "first_person_plural"   );
            options.put( "they" ,   "third_person_plural"   );

            String person = "";
            String option = options.get(subject.toLowerCase());
            if( option.contains("first")    )  person  = "1";
            if( option.contains("second")   )  person  = "2";
            if( option.contains("third")    )  person  = "3";
            if( option.contains("plural")   )  person  = "*";
            
            //Build a list of all possible valid conjugations for the given person/subject
            List<String> valid_conjugations = new ArrayList<String>();
            valid_conjugations.add( verb_present(infinitive, person, false) );
            if(prevWord.toLowerCase().matches(
                "did|didn\'t|can|can\'t|could|couldn't|would|wouldn't|should|shouldn't|will|won't")){
                valid_conjugations.add( infinitive );
            }else{
                valid_conjugations.add( verb_past(infinitive, person, false) );
            }
            
            String listString = "";
            for (String s : valid_conjugations){ listString += s + "\t"; }

            for (String string : valid_conjugations) {
                if(string.matches(verb)){
                    result = true;
                    break;
                }
            }
        }
         
        return result;
    }

    public String verb_infinitive(String v){
        /* Returns the uninflected form of the verb. */

        try{
            return verb_lemmas.get(v);
        } catch(Exception e) {
            return "";
        }
    }

    public String verb_conjugate(String verb, String tense, boolean negate){

        /*Inflects the verb to the given tense.
        For example: be
        present: I am, you are, she is,
        present participle: being,
        past: I was, you were, he was,
        past participle: been,
        negated present: I am not, you aren't, it isn't.
        */
        verb = verb_infinitive(verb);
        int index = verb_tenses_keys.get(tense);
        if(negate) index += verb_tenses_keys.size();
        /*if(verb.equals("better")){
            System.out.println("Verb : " + verb + "\nIndex : " + index);
            String[] tenses = verb_tenses.get(verb);
            System.out.println("array length: " + tenses.length);
            for(String t : tenses){
                System.out.println("tense: " + t);
            }
        }*/
        String result = verb_tenses.get(verb)[index];
        
        return result;
    }

    public String verb_present(String v, String person, boolean negate){

        /*Inflects the verb in the present tense.
        The person can be specified with 1, 2, 3, "1st", "2nd", "3rd", "plural", "*".
        Some verbs like be, have, must, can be negated.
        */
        
        person = person.replace("pl","*");
        person = person.trim();
        
        Map<String, String> hashPersons = new HashMap<String, String>();
        hashPersons.put( "1" , "1st singular present" ); 
        hashPersons.put( "2" , "2nd singular present" ); 
        hashPersons.put( "3" , "3rd singular present" ); 
        hashPersons.put( "*" , "present plural" ); 

        if( hashPersons.containsKey(person) && verb_conjugate(v, hashPersons.get(person), negate).trim().length() > 0){
            return verb_conjugate(v, hashPersons.get(person), negate);
        }
        
        return verb_conjugate(v, "infinitive", negate);
    }

    public String verb_present_participle(String v){

        /* Inflects the verb in the present participle.
        For example:
        give -> giving, be -> being, swim -> swimming
        */

        return verb_conjugate(v, "present participle", false);
    }
    
    public String verb_past(String v, String person, boolean negate){

        /* Inflects the verb in the past tense.
        The person can be specified with 1, 2, 3, "1st", "2nd", "3rd", "plural", "*".
        Some verbs like be, have, must, can be negated.
        For example:
        give -> gave, be -> was, swim -> swam
        */

        person = person.replace("pl","*");
        person = person.trim();

        Map<String, String> hashPersonTense = new HashMap<String, String>();
        hashPersonTense.put( "1" , "1st singular past" ); 
        hashPersonTense.put( "2" , "2nd singular past" ); 
        hashPersonTense.put( "3" , "3rd singular past" ); 
        hashPersonTense.put( "*" , "past plural" ); 
       
        if( hashPersonTense.containsKey(person) 
            && verb_conjugate(v, hashPersonTense.get(person), negate).trim().length() > 0){
            return verb_conjugate(v, hashPersonTense.get(person), negate);
        }

        return verb_conjugate(v, "past", negate);
    }

    public String verb_past_participle(String v){

        /* Inflects the verb in the present partpiciple.
        * For example: give -> given, be -> been, swim -> swum
        */

        return verb_conjugate(v, "past participle", false);
    }
    
    public String verb_all_tenses(){

        /* Returns all possible verb tenses. */
        return verb_tenses_keys.keySet().toString();
    }

    public String verb_tense(String v){

        /* Returns a string from verb_tenses_keys representing the verb's tense.
        * For example: given -> "past participle"
        */

        String infinitive = verb_infinitive(v);
        String[] verbArray = verb_tenses.get(infinitive);

        for (Map.Entry<String, Integer> tense : verb_tenses_keys.entrySet()) {

            if( verbArray[tense.getValue()] == v){
                return tense.getKey();
            }
            if( verbArray[tense.getValue() + verb_tenses_keys.size()] == v ){
                return tense.getKey();
            }
        }

        return "";
    }

    public boolean verb_is_tense(String v, String tense, boolean negated){
    
        /* Checks whether the verb is in the given tense. */
        
        if( verb_tenses_aliases.containsKey(tense) ){
            tense = verb_tenses_aliases.get(tense);
        }if( verb_tense(v) == tense ){
            return true;
        }else{
            return false;
        }
    }
     
    public boolean verb_is_present(String v, String person, boolean negated){

        /* Checks whether the verb is in the present tense. */

        person = person.replace("*","plural");
        String tense = verb_tense(v);
        if( tense != null && !tense.isEmpty()){
            if( tense.contains("present") && tense.contains(person)){
                if( negated == false){
                    return true;
                }else if( v.contains("n't") || v.contains(" not") ){
                    return true;
                }
            }
        }
        
        return false;
    }
 
    public boolean verb_is_present_participle(String v){

        /* Checks whether the verb is in present participle. */
        
        String tense = verb_tense(v);
        if( tense == "present participle"){
            return true;
        }else{
            return false;
        }

    }

    public boolean verb_is_past(String v, String person, boolean negated){

        /* Checks whether the verb is in the past tense. */

        person = person.replace("*","plural");
        String tense = verb_tense(v);
        if( tense != null && !tense.isEmpty() ){
            if( tense.contains("past") && tense.contains(person) ){
                if( negated == false){
                    return true;
                }else if( v.contains("n't") || v.contains(" not") ){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean verb_is_past_participle(String v){

        /* Checks whether the verb is in past participle. */

        String tense = verb_tense(v);
        if( tense == "past participle"){
            return true;
        }else{
            return false;
        }
    }

}


