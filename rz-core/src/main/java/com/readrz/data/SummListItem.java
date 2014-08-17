package com.readrz.data;

import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class SummListItem {

	private final DBObject _dbo;
	
	public static final String _countField    = "count";
	public static final String _quoteField    = "quote";
	public static final String _keywordsField = "keywords";
	public static final String _sourcesField  = "sources";
	
	public SummListItem(
			Integer count,
			DBObject quoteDbo,
			List<SummListItemKeyword> keywords,
			List<SummListItemSource> sources) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_countField, count);
		_dbo.put(_quoteField, quoteDbo);
		if (keywords != null) {
			BasicDBList keywordsDbList = new BasicDBList();
			for (int i=0; i<keywords.size(); i++) {
				keywordsDbList.add(keywords.get(i).getDbo());
			}
			_dbo.put(_keywordsField, keywordsDbList);
		}
		if (sources != null) {
			BasicDBList sourcesDbList = new BasicDBList();
			for (int i=0; i<sources.size(); i++) {
				sourcesDbList.add(sources.get(i).getDbo());
			}
			_dbo.put(_sourcesField, sourcesDbList);
		}
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
}
