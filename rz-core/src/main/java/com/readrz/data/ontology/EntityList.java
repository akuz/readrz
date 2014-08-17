package com.readrz.data.ontology;

import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.index.KeysIndex;

/**
 * Contains a list of entities; also provides a prefix 
 * for individual entity ids, when loaded from file
 * (call completeAfterLoadingFromFile()).
 * 
 */
public final class EntityList {
	
	private final DBObject _dbo;
	private final static String _idField    = "_id";
	private final static String _listField  = "_list";
	
	public EntityList(DBObject dbo) {
		_dbo = dbo;
	}
	
	public EntityList(String id) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, id);
		_dbo.put(_listField, new BasicDBList());
	}

	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getId() {
		return (String)_dbo.get(_idField);
	}
	
	public BasicDBList getList() {
		return (BasicDBList)_dbo.get(_listField);
	}
	
	public void add(Entity entity) {
		getList().add(entity.getDbo());
	}
	
	public void addAll(EntityList other) {
		getList().addAll(other.getList());
	}

	public void validateAfterLoading() {
		String id = getId();
		if (id == null || id.length() == 0) {
			throw new IllegalStateException("Required " + _idField + " field not present");
		}
		BasicDBList list = getList();
		if (list == null) {
			throw new IllegalStateException("Required " + _listField + " field not present");
		}
		for (int i=0; i<list.size(); i++) {
			Entity entity = new Entity((DBObject)list.get(i));
			entity.validateAfterLoading();
		}
	}
	
	public void completeAfterLoading(KeysIndex keysIndex) {
		BasicDBList list = getList();
		Set<String> idSet = new HashSet<>();
		for (int i=0; i<list.size(); i++) {
			Entity entity = new Entity((DBObject)list.get(i));
			if (idSet.contains(entity.getId())) {
				throw new IllegalStateException("Entity with id " + entity.getId() + " already exists ");
			}
			idSet.add(entity.getId());
			entity.validateAfterLoading();
			entity.completeAfterLoading(getId(), keysIndex);
		}
	}
}
