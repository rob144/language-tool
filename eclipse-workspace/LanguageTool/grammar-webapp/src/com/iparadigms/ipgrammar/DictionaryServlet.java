package com.iparadigms.ipgrammar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import morfologik.tools.FSADumpTool;

public class DictionaryServlet extends HttpServlet {
    
    private final String _dictionaryLocation = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/english.dict";
    private POSDictionary dict = new POSDictionary();
    
    public DictionaryServlet () throws FileNotFoundException, IOException {
    }
    
    public String exportDictionary () {
        try {
            //FSADumpTool.main("--raw-data", "-d", dictionaryLocation);
        } catch (Exception ex) { writeLog("" + ex); }
        
        return "";
    }
    
    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        
        String output = "No output generated";
        
        if (req.getParameter("add") != null)
            if (!dict.searchWord(req.getParameter("add"))) {
                dict.addWord(req.getParameter("add"));
                output = "Word added";
            } else
                output = "Item exists, word not added";
        
        if (req.getParameter("search") != null) {
            if (dict.searchWord(req.getParameter("search")))
                output = "Item exists";
            else
                output = "Item does not exist";
        }
        
        resp.getWriter().print(output);
    }
    
    private void writeLog(String text){
        System.out.println(text);
    }
}