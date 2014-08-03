package com.readrz.http;

import me.akuz.core.SystemUtils;
import me.akuz.core.gson.GsonSerializers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public final class JsonResponse {
	
	private final static String _isOkField    = "isOk";
	private final static String _isReadyField = "isReady";
	private final static String _warningField = "warning";
	private final static String _dataField    = "data";
	
	private final DBObject _dbo;
	
	public JsonResponse(String json) {
		_dbo = (DBObject)JSON.parse(json);
	}
	
	private JsonResponse(boolean isOk, boolean isReady, String warning, DBObject data) {
		_dbo = new BasicDBObject()
			.append(_isOkField, isOk)
			.append(_isReadyField, isReady)
			.append(_warningField, warning)
			.append(_dataField, data);
	}
	
	public static JsonResponse createNotOk(String warning) {
		return new JsonResponse(false, false, warning, null);
	}
	
	public static JsonResponse createOkNotReady() {
		return new JsonResponse(true, false, null, null);
	}
	
	public static JsonResponse createOkReady(String warning, DBObject data) {
		return new JsonResponse(true, true, warning, data);
	}
	
	public boolean isOk() {
		Boolean isOk = (Boolean)_dbo.get(_isOkField);
		return isOk != null && isOk.booleanValue();
	}
	
	public boolean isReady() {
		Boolean isReady = (Boolean)_dbo.get(_isReadyField);
		return isReady != null && isReady.booleanValue();
	}
	
	public String getWarning() {
		return (String)_dbo.get(_warningField);
	}
	
	public DBObject getData() {
		return (DBObject)_dbo.get(_dataField);
	}
	
	@Override
	public String toString() {
		return SystemUtils.isLocalhost()
				? GsonSerializers.NoHtmlEscapingPretty.toJson(_dbo)
				: GsonSerializers.NoHtmlEscapingPlain.toJson(_dbo);
	}

}
