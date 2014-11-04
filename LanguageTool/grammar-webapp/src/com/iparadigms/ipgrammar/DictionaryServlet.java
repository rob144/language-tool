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
    
    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        String output = "Invalid get request";
        
        if (req.getParameter("add") != null)
            if (!_dict.searchWord(req.getParameter("add").split(" ")[0])) {
                _dict.addWord(req.getParameter("add"));
                output = "Word added";
            } else
                output = "Item exists, word not added";
        
        if (req.getParameter("search") != null) {
            if (_dict.searchWord(req.getParameter("search").split(" ")[0]))
                output = "Item exists";
            else
                output = "Item does not exist";
        }
        
        if (req.getParameter("build") != null) {
            // TODO check if the added items actually end up in dump prior to it being deleted
            try {
                _dict.buildPosDictionary();
                output = "Dictionary built\nWord count: " + _dict.getWordCount();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        resp.getWriter().print(output);
    }
    
    private void writeLog(String text){
        LOG.log(Level.INFO, text);
    }
}