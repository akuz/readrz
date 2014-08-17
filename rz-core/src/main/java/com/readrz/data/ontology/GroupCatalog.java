package com.readrz.data.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import me.akuz.core.FileUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.readrz.data.index.KeysIndex;
import com.readrz.utils.db.MongoUtils;

public final class GroupCatalog {
	
	private final KeysIndex _keysIndex;
	
	private Map<String, Group> _mapByRootId;
	private List<String> _rootGroupIdsSorted;
	private List<Group> _rootGroupsSorted;
	private Map<String, Group> _allGroupsById;
	private Map<Integer, Group> _allGroupsByKeyId;
	private Map<Integer, List<Integer>> _groupKeyIdsByEntityKeyId;
	
	public GroupCatalog(KeysIndex keysIndex) {
		_keysIndex = keysIndex;
	}

	public List<Group> getRootGroups() {
		return _rootGroupsSorted;
	}
	
	public List<Integer> getEntityGroupKeyIds(Integer entityKeyId) {
		return _groupKeyIdsByEntityKeyId.get(entityKeyId);
	}
	
	public Map<String, Group> getAllGroupsById() {
		return _allGroupsById;
	}
	
	public Group getGroupByKeyId(Integer groupKeyId) {
		return _allGroupsByKeyId.get(groupKeyId);
	}
	
	public Group getGroupById(String groupId) {
		return getGroupByIdAndAssignEntityToGroups(groupId, null, null, null);
	}

	public Group getGroupByIdAndAssignEntityToGroups(String groupId, Integer entityKeyId, Entity entity, KeysIndex keys) {

		int idx = Collections.binarySearch(_rootGroupIdsSorted, groupId);
		if (idx >= 0) {
			
			Group group = _rootGroupsSorted.get(idx);
			
			if (entityKeyId != null) {
				
				// assign entity to group
				Integer groupKeyId = _keysIndex.getId(group.getId());
				group.addEntityKeyId(entityKeyId);
				entity.addGroupKeyId(groupKeyId);
			}
			
			return _rootGroupsSorted.get(idx);
		} else {
			idx = - (idx + 1);
			if (idx == 0) {
				return null;
			} else {
				Group candidateParent = _rootGroupsSorted.get(idx-1);
				if (groupId.startsWith(candidateParent.getId())) {
					return candidateParent.findExactMatchAndAssignEntityToGroup(groupId, entityKeyId, entity, keys);
				} else {
					return null;
				}
			}
		}
	}	
	
	private final void init(List<Group> rootGroups, boolean initTransientFields) {

		Map<String, Group> mapByRootId = new HashMap<>();
		List<String> groupIdsSorted = new ArrayList<>();
		Map<String, Group> groupsById = new HashMap<>();
		
		for (int i=0; i<rootGroups.size(); i++) {
			Group group = rootGroups.get(i);
			mapByRootId.put(group.getId(), group);
			groupIdsSorted.add(group.getId());
			groupsById.put(group.getId(), group);
		}
		Collections.sort(groupIdsSorted);
		
		List<Group> groupsSorted = new ArrayList<Group>(groupIdsSorted.size());
		for (int i=0; i<groupIdsSorted.size(); i++) {
			String groupId = groupIdsSorted.get(i);
			groupsSorted.add(groupsById.get(groupId));
		}
		_mapByRootId = mapByRootId;
		_rootGroupIdsSorted = groupIdsSorted;
		_rootGroupsSorted = groupsSorted;
		
		_allGroupsById = new HashMap<>();
		_allGroupsByKeyId = new HashMap<>();
		_groupKeyIdsByEntityKeyId = new HashMap<>();
		
		Queue<Group> queue = new LinkedList<>();
		queue.addAll(_rootGroupsSorted);
		
		while (queue.size() > 0) {
			
			Group group = queue.poll();
			_allGroupsById.put(group.getId(), group);
			Integer groupKeyId = _keysIndex.getId(group.getId());
			_allGroupsByKeyId.put(groupKeyId, group);
			BasicDBList groupEntityKeyIds = group.getEntityKeyIdsDbList();
			if (groupEntityKeyIds != null) {
				for (Object entityKeyIdObj : groupEntityKeyIds) {
					Integer entityKeyId = (Integer)entityKeyIdObj;
					List<Integer> entityGroupKeyIds = _groupKeyIdsByEntityKeyId.get(entityKeyId);
					if (entityGroupKeyIds == null) {
						entityGroupKeyIds = new ArrayList<Integer>();
						_groupKeyIdsByEntityKeyId.put(entityKeyId, entityGroupKeyIds);
					}
					entityGroupKeyIds.add(groupKeyId);
				}
			}

			BasicDBList childGroups = group.getChildGroups();
			if (childGroups != null) {
				for (int i=0; i<childGroups.size(); i++) {
					Group childGroup = new Group((DBObject)childGroups.get(i));
					if (initTransientFields) {
						childGroup.setParent(group);
					}
					queue.add(childGroup);
				}
			}
		}		
	}
		
	public void loadFromDB(DBCollection dbCollection) {
		
		List<Group> rootGroups = new ArrayList<>();
		DBCursor dbCursor = dbCollection.find();
		try {
			while (dbCursor.hasNext()) {
				Group rootGroup = new Group(dbCursor.next());
				try {
					rootGroup.validateAfterLoading();
				} catch (Exception ex) {
					throw new IllegalStateException("Could not validate group from db", ex);
				}
				
				rootGroups.add(rootGroup);
			}
		} finally {
			dbCursor.close();
		}
		System.out.println(String.format("Loaded %d root groups from db.", rootGroups.size()));
		
		init(rootGroups, true);
	}
	
	public void loadFromDir(String dirPath, KeysIndex keysIndex) throws IOException {
		
		List<File> dirFiles = FileUtils.getFiles(dirPath);
		List<Group> rootGroups = new ArrayList<>();
		for (File file : dirFiles) {

			String jsonStr = FileUtils.readEntireFile(file);
			DBObject dbo;
			try {
				dbo = (DBObject)JSON.parse(jsonStr);
			} catch (Exception ex) {
				throw new IOException("Could not load groups from " + file.getAbsolutePath(), ex);
			}
			Group rootGroup = new Group(dbo);
			try {
				rootGroup.validateAfterLoading();
			} catch (Exception ex) {
				throw new IOException("Could not validate group from " + file.getAbsolutePath(), ex);
			}
			try {
				rootGroup.completeAfterLoading(keysIndex);
			} catch (Exception ex) {
				throw new IOException("Could not complete group from " + file.getAbsolutePath(), ex);
			}
			
			rootGroups.add(rootGroup);
		}
		System.out.println(String.format("Loaded %d root groups from dir.", rootGroups.size()));
		
		init(rootGroups, false);
	}
	
	public void saveToDB(DBCollection dbCollection) {
		
		System.out.println("Removing the old groups...");
		BasicDBList rootGroupIds = new BasicDBList();
		rootGroupIds.addAll(_mapByRootId.keySet());
		BasicDBObject removeQuery = new BasicDBObject();
		removeQuery.put(
			MongoUtils._id, 
				new BasicDBObject("$not", 
					new BasicDBObject("$in", 
						rootGroupIds)));
		dbCollection.remove(removeQuery);
		
		System.out.println("Updating existing and inserting new groups...");
		for (Group rootGroup : _mapByRootId.values()) {
			BasicDBObject upsertQuery = new BasicDBObject();
			upsertQuery.put(MongoUtils._id, rootGroup.getId());
			MongoUtils.upsert(dbCollection, upsertQuery, rootGroup.getDbo(), false);
		}
	}
}
