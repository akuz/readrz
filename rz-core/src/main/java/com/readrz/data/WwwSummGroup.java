package com.readrz.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.ontology.GroupInfo;

public final class WwwSummGroup {

	private final DBObject _dbo;
	
	public static final String _idField      = "id";
	public static final String _nameField    = "name";
	public static final String _isTopicField = "isTopic";
	
	public WwwSummGroup(GroupInfo groupInfo) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, groupInfo.getGroup().getId());
		_dbo.put(_nameField, groupInfo.getGroup().getName());
		_dbo.put(_isTopicField, groupInfo.isTopic());
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getId() {
		return (String)_dbo.get(_idField);
	}
	
	public String getName() {
		return (String)_dbo.get(_nameField);
	}
	
	public boolean isTopic() {
		Boolean is = (Boolean)_dbo.get(_isTopicField);
		return is != null && is.booleanValue();
	}
	
}
