package com.iparadigms.ipgrammar;

public class TextMatch {
	 
	 public int ref;
     public int fromy;
     public int toy;
     public int fromx;
     public int tox;
     public String message;
     
     public TextMatch(){}
     
     public TextMatch(int ref, int fromy, int toy, int fromx, int tox, String message){
    	 this.ref = ref;
    	 this.fromy = fromy;
    	 this.toy = toy;
    	 this.fromx = fromx;
    	 this.tox = tox;
    	 this.message = message;
     }
}
