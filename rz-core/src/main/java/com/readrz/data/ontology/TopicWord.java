package com.readrz.data.ontology;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class TopicWord {
	
	private static final String _wordField = "word";
	private static final String _stemField = "stem";
	private static final String _probField = "prob";

	private final DBObject _dbo;
	
	public TopicWord(DBObject dbo) {
		_dbo = dbo;
	}
	
	public TopicWord(String word, String stem, double prob) {
		_dbo = new BasicDBObject();
		_dbo.put(_wordField, word);
		_dbo.put(_stemField, stem);
		_dbo.put(_probField, prob);
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getWord() {
		return (String)_dbo.get(_wordField);
	}
	
	public String getStem() {
		return (String)_dbo.get(_stemField);
	}
	
	public Double getProb() {
		return (Double)_dbo.get(_probField);
	}

}
