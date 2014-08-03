package com.readrz.http;

public final class JsonRequestOld {
	
	private String _method;
	private String _dataJson;
	
	public JsonRequestOld() {
		// needed for deserialization
	}
		
	public JsonRequestOld(String method, String dataJson) {
		_method = method;
		_dataJson = dataJson;
	}
	
	public String getMethod() {
		return _method;
	}
	
	public String getDataJson() {
		return _dataJson;
	}
}
