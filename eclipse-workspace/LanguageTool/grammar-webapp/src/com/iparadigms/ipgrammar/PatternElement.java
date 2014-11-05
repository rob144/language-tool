package com.iparadigms.ipgrammar;

import org.languagetool.rules.patterns.Element;

public class PatternElement extends Element {

    public static class Builder {
        private String string;
        private int caseSensitive = 0;
        private int regexp = 0;
        private int inflected = 0;

        public Builder(String string) {
            this.string = string;
        }

        public Builder string(String val) {
            this.string = val;
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
            return new PatternElement(this);
        }
    }

    private PatternElement(Builder b) {
    	super(b.string, b.caseSensitive != 0, b.regexp != 0, b.inflected != 0);
    }
}
