/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2009 Amit Nithianandan
 * Copyright (C) 2009 John Lewis
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License.
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package net.sourceforge.cobertura.webapp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;

public class FlushCoberturaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(
	        final HttpServletRequest request,
	        final HttpServletResponse response) throws ServletException, IOException {
	    
		try {
			String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
			String methodName = "saveGlobalProjectData";
			Class<?> saveClass = Class.forName(className);
			
            java.lang.reflect.Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
			saveMethod.invoke(null, new Object[0]);
		}
		catch (Throwable throwable) {
		    final PrintStream ps = new PrintStream(response.getOutputStream());
			
			ps.println("<HTML><BODY>");
			ps.println("<P>Could not save Cobertura data.  Make sure cobertura.jar "
			    + "is in the web server's lib directory: " + throwable.getLocalizedMessage());
			ps.print("<P>");
			throwable.printStackTrace(ps);
			ps.println("</BODY></HTML>");
			
			response.flushBuffer();
		}
	}

	@Override
	protected void doPost(
	        final HttpServletRequest request,
	        final HttpServletResponse response) throws ServletException, IOException {
	    
		doGet(request, response);
	}
}
