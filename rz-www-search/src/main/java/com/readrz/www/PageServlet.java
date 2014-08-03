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

public final class PageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(PageServlet.class);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		try {

			// get requested page name
			String pathInfo = req.getPathInfo();
			if (pathInfo == null || pathInfo.equals("/")) {
				throw new IllegalArgumentException("Page to return not specified");
			}
			if (!pathInfo.startsWith("/")) {
				throw new IllegalStateException("Path should start with a forward slash");
			}
			String pageName = pathInfo.substring(1);

			// render requested page
			String templateName = "page_" + pageName;
			ST template = FacadeTemplates.get(getServletContext()).getST(templateName);
			if (template == null) {
				throw new IllegalArgumentException("Page not found: " + pageName);
			}
			String html = template.render();

			// write response
	    	HttpResponseUtils.writeHtmlUtf8Response(resp, html);
			
		} catch (Exception ex) {
			
			_log.warn("Could not process request", ex);
			throw new ServletException("Could not process request", ex);
		}
	}

}
