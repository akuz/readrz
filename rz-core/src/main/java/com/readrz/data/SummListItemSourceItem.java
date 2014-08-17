package com.readrz.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class SummListItemSourceItem {
	
	private final DBObject _dbo;
	
	public static final String _snapIdStrField  = "snapId";
	public static final String _dateAgoField    = "dateAgo";
	public static final String _dateShortField  = "dateShort";
	public static final String _titleQuoteField = "titleQuote";
	
	public SummListItemSourceItem(
			String snapId, 
			String dateAgo, 
			String dateShort, 
			String titleQuote) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_snapIdStrField, snapId);
		_dbo.put(_dateAgoField, dateAgo);
		_dbo.put(_dateShortField, dateShort);
		_dbo.put(_titleQuoteField, titleQuote);
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getSnapId() {
		return (String)_dbo.get(_snapIdStrField);
	}
	
	public String getDateAgo() {
		return (String)_dbo.get(_dateAgoField);
	}
	
	public String getDateShort() {
		return (String)_dbo.get(_dateShortField);
	}
	
	public String getTitleQuote() {
		return (String)_dbo.get(_titleQuoteField);
	}

}
