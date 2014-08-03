package com.readrz.www.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.readrz.data.user.User;

public final class AuthPage extends AnyAbstractPage {
	
	private User _user;
	private String _code;
	
	public AuthPage(HttpServletRequest req, HttpServletResponse resp) {
		super(req, resp);
	}
	
	public String getCode() {
		return _code;
	}
	public void setCode(String code) {
		_code = code;
	}
	
	public User getUser() {
		return _user;
	}
	public void setUser(User user) {
		_user = user;
	}
}
