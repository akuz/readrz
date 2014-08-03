package com.readrz.www;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.stringtemplate.v4.ST;

import com.readrz.www.facades.FacadeTemplates;
import com.readrz.www.pages.BrowsePage;

public final class BrowseServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(BrowseServlet.class);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		try {
			BrowsePage page = new BrowsePage(req, resp);
			ST st = FacadeTemplates.get(getServletContext()).getBrowsePageST();
			st.add("page", page);
			String htmlResponse = st.render();
	    	HttpResponseUtils.writeHtmlUtf8Response(resp, htmlResponse);
			
		} catch (Exception ex) {
			
			_log.warn("Could not process request", ex);
			throw new ServletException("Could not process request", ex);
		}
	}

}
