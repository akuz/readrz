package com.readrz.www.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.UtcDate;

import com.readrz.data.Period;
import com.readrz.data.user.User;
import com.readrz.www.ProfileConst;
import com.readrz.www.RzPar;
import com.readrz.www.facades.FacadeUserSession;
import com.readrz.www.facades.Session;

public abstract class AnyAbstractPage {

	private final String _servletPath;
	private final String _dateTimeStr;
	private final Period _topPeriod;
	private final User _user;
	private double _durationSec;
	private Map<String, Object> _pars;
	private List<String> _errors;
	private List<String> _messages;
	
	public AnyAbstractPage(HttpServletRequest req, HttpServletResponse resp) {

		_servletPath = req.getServletPath();

		// handle top period
		Period topPeriod = RzPar.getTopPeriodOfUser(req);
		String setTopPeriodStr = req.getParameter(RzPar.parSetPeriod);
		if (setTopPeriodStr != null && setTopPeriodStr.length() > 0) {
			topPeriod = RzPar.parseAndCheckPeriod(setTopPeriodStr, RzPar.defaultGroupingPeriod);
			RzPar.setTopPeriodOfUser(resp, topPeriod);
		}
		_topPeriod = topPeriod;

		// get session and user
		String sessionId = Session.getSessionId(req, resp);
		User user = FacadeUserSession.get().findUserBySession(sessionId);
		_user = user;
		
		UtcDate utcDate = new UtcDate(UtcDate.ShortDateFormatString);
		_dateTimeStr = utcDate.toString();
	}
	
	public String getServletPath() {
		return _servletPath;
	}
	
	public boolean getIsLogin() {
		return ProfileConst.servletPathLogin.equals(_servletPath);
	}
	public boolean getIsRecover() {
		return ProfileConst.servletPathRecover.equals(_servletPath);
	}
	public boolean getIsRegister() {
		return ProfileConst.servletPathRegister.equals(_servletPath);
	}
	public boolean getIsProfile() {
		return ProfileConst.servletPathProfile.equals(_servletPath);
	}

	public void preservePars(HttpServletRequest req) {
		
		Map<String, String[]> pars = req.getParameterMap();
		if (pars != null) {
			for (String key : pars.keySet()) {
				
				String[] values = pars.get(key);
				if (values != null && values.length > 0) {

					// only take first value
					setPar(key, values[0]);
				}
			}
		}
	}
	
	public void setPar(String key, String value) {
		if (_pars == null) {
			_pars = new HashMap<>();
		}
		_pars.put(key, value);
	}

	public Map<String, Object> getPars() {
		return _pars;
	}
	
	public boolean hasErrors() {
		return _errors != null && _errors.size() > 0;
	}
	public List<String> getErrors() {
		return _errors;
	}
	public void addError(String message) {
		if (_errors == null) {
			_errors = new ArrayList<>();
		}
		_errors.add(message);
	}
	
	public boolean hasMessages() {
		return _messages != null && _messages.size() > 0;
	}
	public List<String> getMessages() {
		return _messages;
	}
	public void addMessage(String message) {
		if (_messages == null) {
			_messages = new ArrayList<>();
		}
		_messages.add(message);
	}
	
	public String getDateTimeStr() {
		return _dateTimeStr;
	}
	
	public double getDurationSec() {
		return _durationSec;
	}
	public void setDurationSec(double dur) {
		_durationSec = dur;
	}
	
	public User getUser() {
		return _user;
	}
	
	public Period getTopPeriod() {
		return _topPeriod;
	}
}
