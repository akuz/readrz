package com.readrz.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class WwwSummPeriod {

	private final DBObject _dbo;
	
	public static final String _nameField = "name";
	public static final String _abbrField = "abbr";
	
	public WwwSummPeriod(Period period) {
		_dbo = new BasicDBObject();
		_dbo.put(_nameField, period.getName());
		_dbo.put(_abbrField, period.getAbbr());
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getName() {
		return (String)_dbo.get(_nameField);
	}
	
	public Double getAbbr() {
		return (Double)_dbo.get(_abbrField);
	}
	
}
