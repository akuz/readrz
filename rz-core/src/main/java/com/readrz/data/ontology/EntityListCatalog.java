package com.readrz.data.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.FileUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.readrz.data.index.KeysIndex;
import com.readrz.utils.db.MongoUtils;

public final class EntityListCatalog {
	
	private final Map<Integer, Entity> _entitiesByKeyId;
	private final Map<String, EntityList> _listsByRootId;
	private final List<EntityList> _lists;
	
	public EntityListCatalog() {
		_entitiesByKeyId = new HashMap<>();
		_listsByRootId = new HashMap<>();
		_lists = new ArrayList<>();
	}
	
	public Entity getEntityByKeyId(Integer keyId) {
		return _entitiesByKeyId.get(keyId);
	}
	
	public Map<Integer, Entity> getEntitiesByKeyId() {
		return _entitiesByKeyId;
	}
	
	public Map<String, EntityList> getListsByRootId() {
		return _listsByRootId;
	}
	
	public List<EntityList> getLists() {
		return _lists;
	}
	
	public void addEntityList(EntityList entityList) {
		
		EntityList existingEntityList = _listsByRootId.get(entityList.getId());
		if (existingEntityList != null) {
			existingEntityList.addAll(entityList);
		} else {
			_listsByRootId.put(entityList.getId(), entityList);
			_lists.add(entityList);
		}
		BasicDBList entitiesDbList = entityList.getList();
		if (entitiesDbList != null) {
			for (int i=0; i<entitiesDbList.size(); i++) {
				Entity entity = new Entity((DBObject)entitiesDbList.get(i));
				_entitiesByKeyId.put(entity.getKeyId(), entity);
			}
		}
	}
	
	public void loadFromDB(DBCollection dbCollection) {

		DBCursor dbCursor = dbCollection.find();
		try {
			while (dbCursor.hasNext()) {
				EntityList entityList = new EntityList(dbCursor.next());
				try {
					entityList.validateAfterLoading();
				} catch (Exception ex) {
					throw new IllegalStateException("Could not validate entity list loaded from database", ex);
				}
				addEntityList(entityList);
			}
		} finally {
			dbCursor.close();
		}
	}

	public void loadFromDir(String dirPath, KeysIndex keysIndex) throws IOException {
		
		List<File> dirFiles = FileUtils.getFiles(dirPath);
		for (File file : dirFiles) {
			String jsonStr = FileUtils.readEntireFile(file);
			DBObject dbo;
			try {
				dbo = (DBObject)JSON.parse(jsonStr);
			} catch (Exception ex) {
				throw new IOException("Could not load entity list from " + file.getAbsolutePath(), ex);
			}
			EntityList entityList = new EntityList(dbo);
			try {
				entityList.validateAfterLoading();
			} catch (Exception ex) {
				throw new IOException("Could not validate entity list from " + file.getAbsolutePath(), ex);
			}
			try {
				entityList.completeAfterLoading(keysIndex);
			} catch (Exception ex) {
				throw new IOException("Could not complete entity list from " + file.getAbsolutePath(), ex);
			}
			
			addEntityList(entityList);
		}
	}
	
	public void saveToDb(DBCollection dbCollection) {
		
		System.out.println("Removing the old entities...");
		BasicDBList rootEntityIds = new BasicDBList();
		rootEntityIds.addAll(_listsByRootId.keySet());
		BasicDBObject removeQuery = new BasicDBObject();
		removeQuery.put(
			MongoUtils._id, 
				new BasicDBObject("$not", 
					new BasicDBObject("$in", 
						rootEntityIds)));
		dbCollection.remove(removeQuery);
		
		System.out.println("Updating existing and inserting new entities...");
		for (EntityList entityList : _listsByRootId.values()) {
			BasicDBObject upsertQuery = new BasicDBObject();
			upsertQuery.put(MongoUtils._id, entityList.getId());
			MongoUtils.upsert(dbCollection, upsertQuery, entityList.getDbo(), false);
		}
	}
	
}
