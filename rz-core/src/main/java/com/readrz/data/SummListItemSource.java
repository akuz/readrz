package com.readrz.data;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class SummListItemSource {
	
	private final DBObject _dbo;
	
	public static final String _sourceField = "source";
	public static final String _itemsField  = "items";
	
	public SummListItemSource(String source) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_sourceField, source);
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getSourceName() {
		return (String)_dbo.get(_sourceField);
	}
	
	public void addItem(SummListItemSourceItem item) {
		BasicDBList items = (BasicDBList)_dbo.get(_itemsField);
		if (items == null) {
			items = new BasicDBList();
			_dbo.put(_itemsField, items);
		}
		items.add(item.getDbo());
	}
	
	public BasicDBList getItems() {
		return (BasicDBList)_dbo.get(_itemsField);
	}

}
