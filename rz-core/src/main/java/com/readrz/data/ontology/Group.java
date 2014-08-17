package com.readrz.data.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.StringUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.index.KeysIndex;
import com.readrz.utils.db.MongoUtilsAdaptT;

public final class Group {
	
	private final DBObject _dbo;
	private final static String _idField = "_id";
	private final static String _nameField = "_name";
	private final static String _keyIdField = "_keyId";
	private final static String _isTaxonomyField = "_isTaxonomy";
	private final static String _childGroupsField = "_childGroups";
	private final static String _entityKeyIdsField = "_entityKeyIds";

	// should not be populated before serialization
	private final static String _transient_parentField = "__parent__";
	private final static String _transient_fullNameField = "__fullName__";
	private final static String _transient_entityKeyIds = "__entityKeyIds__";
	private final static String _transient_entityKeyIdsList = "__entityKeyIdsList__";
	
	public Group(DBObject dbo) {
		_dbo = dbo;
	}
	
	public Group(String id, String name, boolean isTaxonomy) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, id);
		_dbo.put(_nameField, name);
		_dbo.put(_isTaxonomyField, isTaxonomy);
	}
	
	public Group getParent() {
		return (Group)_dbo.get(_transient_parentField);
	}
	public void setParent(Group parent) {
		_dbo.put(_transient_parentField, parent);
	}
	public Group findNonTaxonomyParent() {

		Group parent = getParent();
		while (parent != null) {
			if (!parent.isTaxonomy()) {
				break;
			}
			parent = parent.getParent();
		}
		return parent;
	}
	public List<Group> findNonTaxonomyParents() {
		
		List<Group> list = new ArrayList<>();
		Group loopGroup = this;
		while (true) {
			Group parentGroup = loopGroup.findNonTaxonomyParent();
			if (parentGroup == null) {
				break;
			}
			list.add(parentGroup);
			loopGroup = parentGroup;
		}
		Collections.reverse(list);
		return list;
	}
	public List<Group> findParents() {
		
		List<Group> list = new ArrayList<>();
		Group loopGroup = this;
		while (true) {
			Group parentGroup = loopGroup.getParent();
			if (parentGroup == null) {
				break;
			}
			list.add(parentGroup);
			loopGroup = parentGroup;
		}
		Collections.reverse(list);
		return list;
	}

	public DBObject getDbo() {
		return _dbo;
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
	
	public boolean isTaxonomy() {
		Boolean is = (Boolean)_dbo.get(_isTaxonomyField);
		return is == null ? false : is.booleanValue();
	}
	
	public BasicDBList getChildGroups() {
		return (BasicDBList)_dbo.get(_childGroupsField);
	}
	
	public void addChildGroup(DBObject groupDbo) {
		BasicDBList list = (BasicDBList)_dbo.get(_childGroupsField);
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_childGroupsField, list);
		}
		// it's ok to be inefficient here
		if (!list.contains(groupDbo)) {
			list.add(groupDbo);
		}
	}
	
	public Set<Integer> getEntityKeyIds() {
		@SuppressWarnings("unchecked")
		Set<Integer> entityKeyIds = (Set<Integer>)_dbo.get(_transient_entityKeyIds);
		if (entityKeyIds == null) {
			entityKeyIds = MongoUtilsAdaptT.Integer.toSet((BasicDBList)_dbo.get(_entityKeyIdsField));
			_dbo.put(_transient_entityKeyIds, entityKeyIds);
		}
		return entityKeyIds;
	}
	public List<Integer> getEntityKeyIdsList() {
		@SuppressWarnings("unchecked")
		List<Integer> entityKeyIdsList = (List<Integer>)_dbo.get(_transient_entityKeyIdsList);
		if (entityKeyIdsList == null) {
			entityKeyIdsList = MongoUtilsAdaptT.Integer.toList((BasicDBList)_dbo.get(_entityKeyIdsField));
			_dbo.put(_transient_entityKeyIdsList, entityKeyIdsList);
		}
		return entityKeyIdsList;
	}
	public BasicDBList getEntityKeyIdsDbList() {
		return (BasicDBList)_dbo.get(_entityKeyIdsField);
	}
	
	public void addEntityKeyId(Integer keyId) {
		BasicDBList list = (BasicDBList)_dbo.get(_entityKeyIdsField);
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_entityKeyIdsField, list);
		}
		boolean exists = false;
		for (int i=0; i<list.size(); i++) {
			Integer existing = (Integer)list.get(i);
			if (existing.equals(keyId)) {
				exists = true;
				break;
			}
		}
		if (exists == false) {
			list.add(keyId);
		}
	}

	public void validateAfterLoading() {
		
		Queue<Group> queue = new LinkedList<Group>();
		queue.add(this);
		
		while (queue.size() > 0) {
			Group group = queue.poll();

			if (group.getId() == null) {
				throw new IllegalStateException("Required " + _idField + " field not present");
			}
			if (group.getName() == null) {
				throw new IllegalStateException("Required " + _nameField + " field not present");
			}
			
			// check types
			group.getEntityKeyIdsDbList();
			group.getChildGroups();
			
			BasicDBList childGroups = group.getChildGroups();
			if (childGroups != null) {
				for (int i=0; i<childGroups.size(); i++) {
					Group childGroup = new Group((DBObject)childGroups.get(i));
					queue.add(childGroup);
				}
			}
		}
	}
	
	public void completeAfterLoading(KeysIndex keysIndex) {
		
		Queue<Pair<Group, Group>> queue = new LinkedList<Pair<Group, Group>>();
		queue.add(new Pair<Group, Group>(this, null));
		
		while (queue.size() > 0) {
			
			Pair<Group, Group> pair = queue.poll();
			Group group = pair.v1();
			Group parent = pair.v2();
			
			// complete to full id
			if (parent != null) {
				group._dbo.put(_idField, parent.getId() + "/" + group.getId());
			}
			
			// set group key id
			group._dbo.put(_keyIdField, keysIndex.getId(group.getId()));
			
			BasicDBList childGroups = group.getChildGroups();
			if (childGroups != null) {
				for (int i=0; i<childGroups.size(); i++) {
					Group childGroup = new Group((DBObject)childGroups.get(i));
					queue.add(new Pair<Group, Group>(childGroup, group));
				}
			}
		}
	}

	public Group findExactMatch(String groupId) {
		return findExactMatchAndAssignEntityToGroup(groupId, null, null, null);
	}

	public Group findExactMatchAndAssignEntityToGroup(String groupId, Integer entityKeyId, Entity entity, KeysIndex keysIndex) {
		
		// check this group
		if (groupId.startsWith(getId()) == false) {
			return null;
		}
		
		// go deeper until found exact group id
		Group loopGroup = this;
		while (loopGroup != null) {
			
			if (entityKeyId != null) {
				
				// add entity to loop group, and vice-versa
				Integer loopGroupKeyId = keysIndex.getId(loopGroup.getId());
				loopGroup.addEntityKeyId(entityKeyId);
				entity.addGroupKeyId(loopGroupKeyId);
			}
			
			if (groupId.equals(loopGroup.getId())) {
			
				// found exact match
				return loopGroup;
			
			} else {
			
				// check children
				BasicDBList childGroups = loopGroup.getChildGroups();
				
				// reset loop group
				loopGroup = null;
				if (childGroups != null) {
					for (int l=0; l<childGroups.size(); l++) {
						Group childGroup = new Group((DBObject)childGroups.get(l));
						if (groupId.startsWith(childGroup.getId())) {
							loopGroup = childGroup;
							break;
						}
					}
				}
			}
		}
		
		return null;
	}

	public boolean hasChildGroups() {
		BasicDBList childGroupsDBList = getChildGroups();
		return childGroupsDBList != null && childGroupsDBList.size() > 0;
	}

	public String getFullName() {
		
		String fullName = (String)_dbo.get(_transient_fullNameField);
		if (fullName == null) {
		
			List<String> stack = new ArrayList<>();
			stack.add(getName());
			
			Group loopGroup = this.getParent();
			while (loopGroup != null) {
				if (!loopGroup.isTaxonomy()) {
					stack.add(loopGroup.getName());
				}
				loopGroup = loopGroup.getParent();
			}
			
			StringBuilder sb = new StringBuilder();
			for (int i=stack.size()-1; i>=0; i--) {
				StringUtils.appendIfNotEmpty(sb, " / ");
				sb.append(stack.get(i));
			}
	
			fullName = sb.toString();
			_dbo.put(_transient_fullNameField, fullName);
		}
		
		return fullName;
	}
}
