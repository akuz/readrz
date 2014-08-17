package com.readrz.data;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Summary of a specific kind, for a specific period, search keys, and group keys.
 *
 */
public final class Summ {
	
	public final static String _summIdField = "summId";
	public final static String _menuField   = "menu";
	public final static String _listField   = "list";
	public final static String _errorField  = "error";

	private final DBObject _dbo;
	
	public Summ(SummId summId) {
		_dbo = new BasicDBObject();
		_dbo.put(_summIdField, summId.toDbo());
	}
	
	public Summ(DBObject dbo) {
		_dbo = dbo;
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public DBObject getSummId() {
		return (DBObject)_dbo.get(_summIdField);
	}

	public void setMenu(SummMenu menu) {
		_dbo.put(_menuField, menu.getRootItem().getDbo());
	}
	
	public DBObject getMenu() {
		return (DBObject)_dbo.get(_menuField);
	}
	
	public void addListItem(SummListItem item) {
		
		BasicDBList items = (BasicDBList)_dbo.get(_listField);
		if (items == null) {
			items = new BasicDBList();
			_dbo.put(_listField, items);
		}
		items.add(item.getDbo());
	}
	
	public BasicDBList getList() {
		return (BasicDBList)_dbo.get(_listField);
	}

	public boolean hasError() {
		return getError() != null;
	}
	public String getError() {
		return (String)_dbo.get(_errorField);
	}
	public void setError(String error) {
		_dbo.put(_errorField, error);
	}

}
