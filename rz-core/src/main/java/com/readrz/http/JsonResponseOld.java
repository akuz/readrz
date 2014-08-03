package com.readrz.http;

public final class JsonResponseOld {
	
	private boolean _hasSucceeded;
	private boolean _hasWarnings;
	private String _message;
	private String _log;
	private Object _data;
	private String _dataJson;
	
	public final static JsonResponseOld createCompleted(String message, String log, Object data, String dataJson) {
		return new JsonResponseOld(true, false, message, log, data, dataJson);
	}
	
	public final static JsonResponseOld createCompletedWithWarnings(String message, String log, Object data, String dataJson) {
		return new JsonResponseOld(true, true, message, log, data, dataJson);
	}

	public final static JsonResponseOld createFailed(String message, String log) {
		return new JsonResponseOld(false, true, message, log, null, null);
	}
	
	public final static JsonResponseOld create(boolean hasSucceeded, boolean hasWarnings, String message, String log, Object data, String dataJson) {
		return new JsonResponseOld(hasSucceeded, hasWarnings, message, log, data, dataJson);
	}
	
	public JsonResponseOld() {
		// needed for deserialization
	}

	public JsonResponseOld(boolean hasSucceeded, boolean hasWarnings, String message, String log, Object data, String dataJson) {
		_hasSucceeded = hasSucceeded;
		_message = message;
		_hasWarnings = hasWarnings;
		_log = log;
		_data = data;
		_dataJson = dataJson;
	}
	
	public boolean hasSucceeded() {
		return _hasSucceeded;
	}
	
	public boolean hasWarnings() {
		return _hasWarnings;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public String getLog() {
		return _log;
	}
	
	public Object getData() {
		return _data;
	}
	
	public String getDataJson() {
		return _dataJson;
	}
}
