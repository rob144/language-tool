package com.iparadigms.ipgrammar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import morfologik.tools.FSADumpTool;

@SuppressWarnings("serial")
public class DictionaryServlet extends HttpServlet {
    
    private final Logger LOG = Logger.getLogger(RuleTestServlet.class.getName());
    
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
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}