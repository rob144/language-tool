package com.iparadigms.ipgrammar;
 
public class ServerMain {
	
    public static void main(String[] args) throws Exception {
        //runUnitTests();
        
    	ServerManager serverManager = new ServerManager ();
    	serverManager.start();
    }
    
    private static void runUnitTests() throws Exception{
        UnitTests tests = new UnitTests();
    }
}