package edu.internet2.ion.ionui.servlets;

import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmailCollector extends HttpServlet{
    
    final private String filename = "/var/www/html/ion_emails.txt";
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
	String email = request.getParameter("email");
	if(email == null || "".equals(email.replaceAll("\\s*", ""))){
	    return;
	}
	
	FileWriter fout = new FileWriter(filename, true);
	fout.append(email+"\n");
	fout.close();
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    	throws IOException, ServletException {
		this.doGet(request, response);
    }
}
