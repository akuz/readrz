package com.readrz.data.ontology;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.index.KeysIndex;

/**
 * Entity defines something that can be detected in text.
 * If entity specifies patterns, it will be detected as exact match.
 * Alternatively, if it specifies topicProb and topicWords, it will be detected as a topic.
 *
 */
public final class Entity {
	
	private final DBObject _dbo;
	private final static String _idField          = "_id";
	private final static String _keyIdField       = "_keyId";
	private final static String _nameField        = "_name";
	private final static String _groupIdsField    = "_groupIds";
	private final static String _groupKeyIdsField = "_groupKeyIds";
	private final static String _patternsField    = "_patterns";
	private final static String _attributesField  = "_attributes";
	private final static String _tickersField     = "_tickers";
	private final static String _topicProbField   = "_topicProb";
	private final static String _topicWordsField  = "_topicWords";
	
	public Entity(DBObject dbo) {
		_dbo = dbo;
	}
	
	/**
	 * Creates a topic entity (with topic probability).
	 * Add individual topic words using addTopicWord().
	 * 
	 */
	public Entity(String id, String name, double topicProb) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, id);
		_dbo.put(_nameField, name);
		_dbo.put(_groupIdsField, new BasicDBList());
		_dbo.put(_topicProbField, topicProb);
		_dbo.put(_topicWordsField, new BasicDBList());
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public boolean isTopic() {
		return getTopicProb() != null;
	}
	
	public Double getTopicProb() {
		return (Double)_dbo.get(_topicProbField);
	}
	
	public BasicDBList getTopicWords() {
		return (BasicDBList)_dbo.get(_topicWordsField);
	}
	
	public void addTopicWord(TopicWord topicWord) {
		BasicDBList list = getTopicWords();
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_topicWordsField, list);
		}
		list.add(topicWord.getDbo());
	}
	
	public void addGroupId(String groupId) {
		BasicDBList list = getGroupIds();
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_groupIdsField, list);
		}
		// it's ok to be inefficient here
		if (!list.contains(groupId)) {
			list.add(groupId);
		}
	}
	
	public String getId() {
		return (String)_dbo.get(_idField);
	}
	
	public Integer getKeyId() {
		return (Integer)_dbo.get(_keyIdField);
	}
	
	public String getName() {
		return (String)_dbo.get(_nameField);
	}

	public BasicDBList getGroupIds() {
		return (BasicDBList)_dbo.get(_groupIdsField);
	}
	public BasicDBList getGroupKeyIds() {
		return (BasicDBList)_dbo.get(_groupKeyIdsField);
	}
	public void addGroupKeyId(Integer groupKeyId) {
		BasicDBList list = getGroupKeyIds();
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_groupKeyIdsField, list);
		}
		boolean exists = false;
		for (int i=0; i<list.size(); i++) {
			Integer existing = (Integer)list.get(i);
			if (existing.equals(groupKeyId)) {
				exists = true;
				break;
			}
		}
		if (exists == false) {
			list.add(groupKeyId);
		}
	}

	public BasicDBList getPatterns() {
		return (BasicDBList)_dbo.get(_patternsField);
	}

	public BasicDBList getTickers() {
		return (BasicDBList)_dbo.get(_tickersField);
	}

	public DBObject getAttributes() {
		return (DBObject)_dbo.get(_attributesField);
	}
	public void addAttribute(String key, Object value) {
		DBObject attrs = getAttributes();
		if (attrs == null) {
			attrs = new BasicDBObject();
			_dbo.put(_attributesField, attrs);
		}
		attrs.put(key, value);
	}
	
	public void completeAfterLoading(String entityListId, KeysIndex keysIndex) {
		if (entityListId == null || entityListId.length() == 0) {
			throw new IllegalStateException("Argument entityListId cannot be null or empty");
		}
		
		// complete to the full id
		String entityId = entityListId + "/" + getId();
		Integer entityKeyId = keysIndex.getId(entityId);
		
		// update properties
		_dbo.put(_idField, entityId);
		_dbo.put(_keyIdField, entityKeyId);
	}

	public void validateAfterLoading() {
		if (getId() == null) {
			throw new IllegalStateException("Required " + _idField + " field not present");
		}
		if (getName() == null) {
			throw new IllegalStateException("Required " + _nameField + " field not present");
		}
		// check types
		getGroupIds();
		getTickers();
		getAttributes();
		getPatterns();
		getTopicProb();
		getTopicWords();
	}
}
