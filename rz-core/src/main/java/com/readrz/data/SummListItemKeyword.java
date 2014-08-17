package com.readrz.data;

import me.akuz.core.Rounding;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class SummListItemKeyword {

	private final DBObject _dbo;
	
	public static final String _wordField = "w";
	public static final String _probField = "p";
	
	public SummListItemKeyword(String word, double prob) {
		_dbo = new BasicDBObject();
		_dbo.put(_wordField, word);
		_dbo.put(_probField, Rounding.round(prob, 2));
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getWord() {
		return (String)_dbo.get(_wordField);
	}
	
	public Double getProb() {
		return (Double)_dbo.get(_probField);
	}
	
}
