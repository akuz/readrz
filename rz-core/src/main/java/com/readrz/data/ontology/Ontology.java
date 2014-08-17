package com.readrz.data.ontology;

import java.io.IOException;
import java.util.Map;

import me.akuz.core.StringUtils;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;

public final class Ontology {
	
	private final KeysIndex _keysIndex;
	private final EntityListCatalog _entityListCatalog;
	private final GroupCatalog _patternGroupCatalog;
	private final GroupCatalog _topicGroupCatalog;
	
	public Ontology(KeysIndex keysIndex) {
		_keysIndex = keysIndex;
		_entityListCatalog = new EntityListCatalog();
		_patternGroupCatalog = new GroupCatalog(keysIndex);
		_topicGroupCatalog = new GroupCatalog(keysIndex);
	}
	
	public EntityListCatalog getEntityListCatalog() {
		return _entityListCatalog;
	}
	
	public GroupCatalog getPatternGroupCatalog() {
		return _patternGroupCatalog;
	}
	
	public GroupCatalog getTopicGroupCatalog() {
		return _topicGroupCatalog;
	}
	
	public void loadFromDir(String dir) throws IOException {

		System.out.println("Loading ontology from dir...");

		System.out.println("Loading entity lists...");
		_entityListCatalog.loadFromDir(StringUtils.concatPath(dir, "entities"), _keysIndex);
		
		System.out.println("Loading pattern groups...");
		_patternGroupCatalog.loadFromDir(StringUtils.concatPath(dir, "patternGroups"), _keysIndex);
		
		System.out.println("Loading topic groups...");
		_topicGroupCatalog.loadFromDir(StringUtils.concatPath(dir, "topicGroups"), _keysIndex);
		
		System.out.println("Loaded ontology from dir.");

		assignEntitiesToGroups();
	}

	public void loadFromDB(DB db) {
		
		System.out.println("Loading ontology from db...");

		System.out.println("Loading entity lists...");
		_entityListCatalog.loadFromDB(db.getCollection(MongoColls.entities));
		
		System.out.println("Loading pattern groups...");
		_patternGroupCatalog.loadFromDB(db.getCollection(MongoColls.groupsPattern));
		
		System.out.println("Loading topic groups...");
		_topicGroupCatalog.loadFromDB(db.getCollection(MongoColls.groupsTopic));
		
		System.out.println("Loaded ontology from db.");
	}

	private final void assignEntitiesToGroups() {

		System.out.println("Assigning entities to groups...");
		
		Map<String, EntityList> entityListMapByRootId = _entityListCatalog.getListsByRootId();
		for (String entityListId : entityListMapByRootId.keySet()) {
			
			EntityList entityList = entityListMapByRootId.get(entityListId);
			BasicDBList list = entityList.getList();
			for (int i=0; i<list.size(); i++) {
				
				Entity entity = new Entity((DBObject)list.get(i));
				Integer entityKeyId = _keysIndex.getId(entity.getId());
				
				BasicDBList groupIds = entity.getGroupIds();
				if (groupIds != null) {
					for (int j=0; j<groupIds.size(); j++) {
						
						String entityGroupId = (String)groupIds.get(j);
						Group patternGroup = _patternGroupCatalog.getGroupByIdAndAssignEntityToGroups(entityGroupId, entityKeyId, entity, _keysIndex);
						Group topicGroup = _topicGroupCatalog.getGroupByIdAndAssignEntityToGroups(entityGroupId, entityKeyId, entity, _keysIndex);

						// check if any group was found
						if (patternGroup == null && topicGroup == null) {
							throw new IllegalStateException("Entity " + entity.getId() + " specifies unknown group: " + entityGroupId);
						} else {
							if (patternGroup != null) {
								System.out.println("Assigned " + entity.getId() + " to pattern group: " + patternGroup.getId());
							}
							if (topicGroup != null) {
								System.out.println("Assigned " + entity.getId() + " to topic group: " + topicGroup.getId());
							}
						}
					}
				}
			}
		}
		
		System.out.println("Assigned entities to groups.");
	}


	public void saveToDB(DB db) throws IOException {
		
		System.out.println("Saving ontology to db...");

		System.out.println("Saving entity lists...");
		_entityListCatalog.saveToDb(db.getCollection(MongoColls.entities));
		
		System.out.println("Saving pattern groups...");
		_patternGroupCatalog.saveToDB(db.getCollection(MongoColls.groupsPattern));
		
		System.out.println("Saving topic groups...");
		_topicGroupCatalog.saveToDB(db.getCollection(MongoColls.groupsTopic));
		
		System.out.println("Saved ontology to db.");
	}
	
}
