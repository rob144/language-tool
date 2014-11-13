package com.iparadigms.ipgrammar;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class UpdateSocket extends WebSocketAdapter
{
    @Override
    public void onWebSocketConnect(Session sess){
        super.onWebSocketConnect(sess);
        System.out.println("Socket Connected: " + sess);
    }
    
    @Override
    public void onWebSocketText(String message){
        super.onWebSocketText(message);
        System.out.println("Received TEXT message: " + message);
        
        if (message.split(":")[0].equals("get_status"))
        	SocketLogicBridge.getStatus(this);
        
        if (message.split(":")[0].equals("request"))
        	SocketLogicBridge.request(this, message.split(":")[1]);
        
        if (message.split(":")[0].equals("remove_self"))
			SocketLogicBridge.removeStatus(this, message.split(":")[1]);
    }
    
    @Override
    public void onWebSocketClose(int statusCode, String reason){
        super.onWebSocketClose(statusCode,reason);
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }
    
    @Override
    public void onWebSocketError(Throwable cause){
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
    
    public void sendMessage(String message) throws IOException {
    	super.getRemote().sendString(message);
    }
}