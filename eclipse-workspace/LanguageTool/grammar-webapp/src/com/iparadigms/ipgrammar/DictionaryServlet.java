package com.iparadigms.ipgrammar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import morfologik.tools.FSADumpTool;

@SuppressWarnings("serial")
public class DictionaryServlet extends HttpServlet {
    
    private final String _dictionaryLocation = "grammar-webapp/src/com/iparadigms/ipgrammar/resources/english.dict";
    private POSDictionary _dict = new POSDictionary();
    
    public DictionaryServlet () throws FileNotFoundException, IOException {
        //exportDictionary();
    }
    
    public void exportDictionary () {
        try {
            PrintStream old = System.out;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            System.setOut(ps);
            
            FSADumpTool.main("--raw-data", "-d", _dictionaryLocation);
            System.out.flush();
            System.setOut(old);
            
            String[] lineArray = baos.toString().split("\n");
            String[][] multiArray = new String[lineArray.length][];
            for (int x = 0; x < lineArray.length; x++)
                multiArray[x] = lineArray[x].split("\\+");
            
        } catch (Exception ex) { writeLog("" + ex); }
    }
    
    /*public void buildDictionary () {
        POSDictionaryBuilder builder = new POSDictionaryBuilder(infoFilePath);
        
        builder.build(dumpFilePath);
    }*/
    
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
                _dict.buildDictionary();
                output = "Dictionary built";
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        resp.getWriter().print(output);
    }
    
    private void writeLog(String text){
        System.out.println(text);
    }
}