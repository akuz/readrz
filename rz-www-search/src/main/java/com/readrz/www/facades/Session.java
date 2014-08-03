package com.readrz.www.facades;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.crypt.bcrypt.BCrypt;


public final class Session {
	
	private final static String _sessionCookieName = "rzSessionId";
	
	public static String getSessionId(HttpServletRequest req, HttpServletResponse resp) {
		
		String sessionId = null;
		
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i=0; i<cookies.length; i++) {
				if (_sessionCookieName.equals(cookies[i].getName())) {
					sessionId = cookies[i].getValue();
					break;
				}
			}
		}
		
		// check session id
		if (sessionId != null) {
			String userAgent = String.format("%s", req.getHeader("User-Agent"));
			try {
				if (BCrypt.checkpw(userAgent, sessionId) == false) {
					sessionId = null;
				} 
			} catch (Exception e) {
				sessionId = null;
			}
		}
		
		// create session, if not exists
		if (sessionId == null) {
			sessionId = resetSessionId(req, resp);
		}
		
//		System.out.println("SessionId: " + sessionId);
		
		return sessionId;
	}

	public static String resetSessionId(HttpServletRequest req, HttpServletResponse resp) {
		
		// create new session id
		String userAgent = String.format("%s", req.getHeader("User-Agent"));
		String sessionId = BCrypt.hashpw(userAgent, BCrypt.gensalt());
		
		// write session cookie
		Cookie cookie = new Cookie(_sessionCookieName, sessionId);
		resp.addCookie(cookie);
		
		return sessionId;
	}
}
