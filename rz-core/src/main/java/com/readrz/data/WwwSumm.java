package com.readrz.data;

import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.ontology.GroupInfo;

/**
 * Www display summary (combined from several underlying summaries).
 *
 */
public final class WwwSumm {
	
	public static final String _periodField = "period";
	public static final String _searchField = "search";
	public static final String _groupsField = "groups";
	public static final String _menusField  = "menus";
	public static final String _listField   = "list";
	public static final String _summIdField = "summId";
	
	private final DBObject _dbo;
	
	public WwwSumm() {
		_dbo = new BasicDBObject();
	}
	
	public WwwSumm(Period period, String queryString, List<GroupInfo> groupInfos) {
		_dbo = new BasicDBObject();
		_dbo.put(_periodField, new WwwSummPeriod(period).getDbo());
		if (queryString != null && queryString.length() > 0) {
			_dbo.put(_searchField, queryString);
		}
		if (groupInfos != null && groupInfos.size() > 0) {
			BasicDBList groups = new BasicDBList();
			for (int i=0; i<groupInfos.size(); i++) {
				groups.add(new WwwSummGroup(groupInfos.get(i)).getDbo());
			}
			_dbo.put(_groupsField, groups);
		}
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public void addMenu(DBObject menuDbo) {
		BasicDBList menus = (BasicDBList)_dbo.get(_menusField);
		if (menus == null) {
			menus = new BasicDBList();
			_dbo.put(_menusField, menus);
		}
		menus.add(menuDbo);
	}
	
	public void setList(BasicDBList listDbList) {
		_dbo.put(_listField, listDbList);
	}
	
	public void setSummId(DBObject summId) {
		_dbo.put(_summIdField, summId);
	}

}
