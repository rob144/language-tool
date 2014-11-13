package com.iparadigms.ipgrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SocketLogicBridge {
	
	private static LinkedList<String> _request = new LinkedList<String>();
	private static LinkedList<UpdateSocket> _connections = new LinkedList<UpdateSocket>();
	
	public SocketLogicBridge () {
		
	}
	
	public static void storeRequest(UpdateSocket socket, String request){
		_connections.add(socket);
		_request.add(request);
	}
	
	//getStatus
	
	//removeStatus
	
	//public static UpdateSocket getSocket
	
	//public static String getRequest
	
	public static boolean requestWaiting(){
		return !_request.isEmpty();
	}
}
