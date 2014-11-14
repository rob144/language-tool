package com.iparadigms.ipgrammar;

import java.io.IOException;

public class SocketLogicBridge {
	
	private static RuleTestServlet _logic;
	
	public SocketLogicBridge(){}
	
	public static void request(UpdateSocket socket, String request){
		try {
			_logic.handleRequest(socket, request);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void logicInstance (RuleTestServlet logic){ //should only be one instance
		_logic = logic;
	}
}
