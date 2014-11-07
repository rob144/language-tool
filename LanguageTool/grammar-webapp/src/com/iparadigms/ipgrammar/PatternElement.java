package com.iparadigms.ipgrammar;

import org.languagetool.rules.patterns.Element;

public class PatternElement extends Element {
	
    public static class Builder {
        private String string = "";
        private String posString = "";
        private int caseSensitive = 0;
        private int regexp = 0;
        private int posRegExp = 0;
        private int inflected = 0;
        private int posNegate = 0;

        public Builder() {}
        
        public Builder tokenString(String string) {
            this.string = string;
            return this;
        }

        public Builder string(String val) {
            this.string = val;
            return this;
        }
        
        public Builder posString(String val) {
            this.posString = val;
            return this;
        }
        
        public Builder posRegExp(int val) {
            this.posRegExp = val;
            return this;
        }

        public Builder posNegate(int val) {
            this.posNegate = val;
            return this;
        }
        
        public Builder caseSens(int val) {
            this.caseSensitive = val;
            return this;
        }
        
        public Builder regexp(int val) {
            this.regexp = val;
            return this;
        }
        
        public Builder inflected(int val) {
            this.inflected = val;
            return this;
        }

        public PatternElement build() {
        	PatternElement elem = new PatternElement(this);
        	if(this.posString.length() >= 1) {
        		elem.setPosElement(posString, posRegExp != 0, posNegate != 0);
        	}
            return elem;
        }
    }

    private PatternElement(Builder b) {
    	super(b.string, b.caseSensitive != 0, b.regexp != 0, b.inflected != 0);
    	
    }
}
