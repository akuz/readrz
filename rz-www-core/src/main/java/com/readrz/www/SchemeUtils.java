package com.readrz.www;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SchemeUtils {
	
	public static final String http  = "http";
	public static final String https = "https";

	public final static void ensureScheme_orThrow(String scheme, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		ensureScheme_or(scheme, req, resp, false, null);
	}

	public final static boolean ensureScheme_sendRedirect(String scheme, HttpServletRequest req, HttpServletResponse resp, String hideServletPath) throws IOException, ServletException {
		return ensureScheme_or(scheme, req, resp, true, hideServletPath);
	}
	
	private final static boolean ensureScheme_or(String scheme, HttpServletRequest req, HttpServletResponse resp, boolean sendRedirect, String hideServletPath) throws IOException, ServletException {
		
		String protoScheme = req.getHeader("X-Forwarded-Proto");
		
		if (protoScheme != null &&
			protoScheme.equalsIgnoreCase(scheme) == false) {
			
			if (sendRedirect) {
				
				// get request url
				final String requestUrl = req.getRequestURL().toString();
				
				// update scheme
				String sendUrl = updateScheme(scheme, requestUrl);
				
				// hide servlet path
				if (hideServletPath != null) {
					
					// check servlet path
					if (hideServletPath.length() < 2 ||
						hideServletPath.startsWith("/") == false) {
						throw new IllegalArgumentException("Servlet path should start with / and be longer than that");
					}

					// remove servlet path
					if (sendUrl.endsWith(hideServletPath)) {
						// keep the slash from the path (see + 1 at the end)
						sendUrl = sendUrl.substring(0, sendUrl.length() - hideServletPath.length() + 1);
					}
				}
				
				// append query string
				String queryString = req.getQueryString();
				if (queryString != null) {
					sendUrl = sendUrl + "?" + queryString;
				}
				
				resp.sendRedirect(sendUrl);
				return true;
				
			} else {
				
				throw new ServletException("Disallowed request protocol: " + protoScheme);
			}
			
		} else {
			return false;
		}
	}
	
	public static final String updateScheme(String scheme, String absoluteUrl) {
		
		int idx = absoluteUrl.indexOf("://");
		
		if (idx < 0) {
			throw new IllegalArgumentException("URL does not contain scheme to update: " + absoluteUrl);
		}
		
		String url = scheme + absoluteUrl.substring(idx);

		return url;
	}
}
