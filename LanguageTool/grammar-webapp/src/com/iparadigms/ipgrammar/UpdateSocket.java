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
    public void onWebSocketText(String packet){
        super.onWebSocketText(packet);
        System.out.println("Received TEXT message: " + packet);
        
        String[] message = packet.split(";");
        
        if (message[0].equals("request"))
        	SocketLogicBridge.request(this, message[1]);
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