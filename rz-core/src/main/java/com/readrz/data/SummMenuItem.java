package com.readrz.data;

import me.akuz.core.CompareUtils;
import me.akuz.core.SortOrder;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class SummMenuItem implements Comparable<SummMenuItem> {

	private static final String _nameField       = "name";
	private static final String _groupIdField    = "groupId";
	private static final String _countField      = "count";
	private static final String _isTaxonomyField = "isTaxonomy";
	private static final String _isActiveField   = "isActive";
	private static final String _childrenField   = "children";
	
	private final DBObject _dbo;
	
	public SummMenuItem(String name, String groupId, Integer count) {
		_dbo = new BasicDBObject();
		_dbo.put(_nameField, name);
		_dbo.put(_groupIdField, groupId);
		_dbo.put(_countField, count);
	}
	
	public SummMenuItem(DBObject dbo) {
		_dbo = dbo;
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getName() {
		return (String)_dbo.get(_nameField);
	}
	public void setName(String name) {
		_dbo.put(_nameField, name);
	}
	
	public String getGroupId() {
		return (String)_dbo.get(_groupIdField);
	}
	
	public Integer getCount() {
		return (Integer)_dbo.get(_countField);
	}
	
	public boolean isTaxonomy() {
		Boolean is = (Boolean)_dbo.get(_isTaxonomyField);
		return is != null && is.booleanValue();
	}
	public void isTaxonomy(boolean is) {
		if (is) {
			_dbo.put(_isTaxonomyField, is);
		} else {
			_dbo.removeField(_isTaxonomyField);
		}
	}
	
	public boolean isActive() {
		Boolean is = (Boolean)_dbo.get(_isActiveField);
		return is != null && is.booleanValue();
	}
	public void isActive(boolean is) {
		if (is) {
			_dbo.put(_isActiveField, is);
		} else {
			_dbo.removeField(_isActiveField);
		}
	}
	
	public BasicDBList getChildren() {
		return (BasicDBList)_dbo.get(_childrenField);
	}
	public void addChild(SummMenuItem child) {
		BasicDBList children = (BasicDBList)_dbo.get(_childrenField);
		if (children == null) {
			children = new BasicDBList();
			_dbo.put(_childrenField, children);
		}
		children.add(child.getDbo());
	}

	@Override
	public int compareTo(SummMenuItem o) {
		
		// first compare counts
		int cmp = CompareUtils.compareNullsLowest(getCount(), o.getCount(), SortOrder.Desc);
		if (cmp != 0) {
			return cmp;
		}
		
		// next compare names
		return getName().compareToIgnoreCase(o.getName());
	}
	
}
