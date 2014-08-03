package com.readrz.http;

import java.util.Calendar;
import java.util.Date;

public final class GoogleIdCache {
	
	private final String _service;
	private final String _appName;
	private final String _appUrl;
	private final String _email;
	private final String _password;
	private final int _expiryMinutes;
	
	private final Object _lock = new Object();
	private GoogleId _googleId;
	private Date _lastDate;
	
	public GoogleIdCache(String service, String appName, String appUrl, String email, String password, int expiryMinutes) {
		
		_service = service;
		_appName = appName;
		_appUrl = appUrl;
		_email = email;
		_password = password;
		_expiryMinutes = expiryMinutes;
	}
	
	public String getAppUrl() {
		return _appUrl;
	}
	
	public GoogleId getGoodleId() throws Exception {

		// double checking pattern
		if (checkNeedsRefresh()) {
			synchronized (_lock) {
				if (checkNeedsRefresh()) {
					
					System.out.println("Authorizing with Google...");
					_googleId = new GoogleId(_service, _appName, _appUrl, _email, _password);
					Calendar now = me.akuz.core.DateUtils.getUTCCalendar();
					_lastDate = now.getTime();
				}
			}
		}
		
		return _googleId;
	}
	
	private boolean checkNeedsRefresh() {

		if (_googleId == null) {
			return true;
		} else if (_lastDate == null) {
			return true;
		} else {
			Calendar expiryCal = me.akuz.core.DateUtils.getUTCCalendar();
			expiryCal.add(Calendar.MINUTE, -_expiryMinutes);
			Date expiryDate = expiryCal.getTime();
			return _lastDate.compareTo(expiryDate) < 0;
		}
	}
	
}
