package com.iparadigms.ipgrammar;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DictionaryServlet extends HttpServlet {
    
    private POSDictionary _dict;
    
    public DictionaryServlet () throws Exception {
        _dict = new POSDictionary();
    }
    
    public String getResponse (HttpServletRequest req) {
        String output = "Invalid request";
        String request = req.getParameter("request");
        
        if (request.equals("add")) {
        	output = _dict.searchWord(req.getParameter("line"));
        	
            if (!(_dict.searchWord(req.getParameter("line")).equals("Item does not exist"))) {
                _dict.addWord(req.getParameter("line"));
                output = "Word added";
            } else
                output = "Item exists, word not added";
        }
        
        if (request.equals("search"))
        	output = _dict.searchWord(req.getParameter("line"));
        
        if (request.equals("inflections"))
        	output = _dict.getInflections(req.getParameter("line"));
        
        if (request.equals("build")) {
            try {
                _dict.buildPosDictionary();
                output = "Dictionary built\tWord count: " + _dict.getWordCount();
            } catch (Exception e) { e.printStackTrace(); }
        }
        return output;
    }
    
    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().print(getResponse(req));
    }
}