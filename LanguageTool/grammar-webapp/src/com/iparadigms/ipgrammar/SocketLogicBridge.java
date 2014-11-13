package com.iparadigms.ipgrammar;

import java.io.IOException;
import java.util.LinkedList;

public class SocketLogicBridge {
	
	private static RuleTestServlet _logic;
	
	private static LinkedList<String> _status = new LinkedList<String>();
	private static LinkedList<UpdateSocket> _socketStatus = new LinkedList<UpdateSocket>();
	
	private static LinkedList<String> _request = new LinkedList<String>();
	private static LinkedList<UpdateSocket> _waitingSocket = new LinkedList<UpdateSocket>();
	
	public SocketLogicBridge () {}
	
	public static void request(UpdateSocket socket, String request){
		try {
			_logic.poll(socket, request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setStatus(UpdateSocket socket, String status){
		_socketStatus.add(socket);
		_status.add(status);
	}
	
	public static String getStatus(UpdateSocket socket){
		return _status.get(_socketStatus.indexOf(socket));
	}
	
	public static void removeStatus(UpdateSocket socket, String status){
		int socketIndex = _socketStatus.indexOf(socket);
		int statusIndex = _status.indexOf(status);
		
		if (socketIndex == statusIndex) {
			_socketStatus.remove(socket);
			_status.remove(status);
		}
	}
	
	public static void inform (RuleTestServlet logic) throws IOException {
		_logic = logic;
	}
}
