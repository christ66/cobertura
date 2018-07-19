package com.acme.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.IOException;

public class SimpleServlet extends HttpServlet {
    
    private static final long serialVersionUID = 2498612240223019445L;

    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out.println("Hi");
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
