package com.iparadigms.ipgrammar;
 
public class ServerMain {
	
    private static boolean test = false;
	
    public static void main(String[] args) throws Exception {
        if (test)
            runUnitTests();
        
    	ServerManager serverManager = new ServerManager ();
    	serverManager.start();
    }
    
    private static void runUnitTests() throws Exception {
        UnitTests tests = new UnitTests();
        System.out.println(tests.runTests());
    }
}