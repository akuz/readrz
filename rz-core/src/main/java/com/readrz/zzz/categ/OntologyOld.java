package com.readrz.zzz.categ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class OntologyOld {
	
	private final Map<String, Feature> _allFeatures;
	private final Map<String, TopicFeature> _topicFeatures;
	private final Map<String, EntityFeature> _entityFeatures;
	private final GroupFeature _rootGroup;
	
	public OntologyOld() {
		_allFeatures = new HashMap<String, Feature>();
		_topicFeatures = new HashMap<String, TopicFeature>();
		_entityFeatures = new HashMap<String, EntityFeature>();
		_rootGroup = new GroupFeature(
				CategConstants.ROOT_FEATURE_KEY, 
				CategConstants.ROOT_FEATURE_NAME, 
				true, 
				false,
				false,
				true);
		_allFeatures.put(_rootGroup.getKey(), _rootGroup);
	}

	private void checkFeatureDoesNotExist(Feature newFeature) {
		Feature existingFeature = _allFeatures.get(newFeature.getKey());
		if (existingFeature != null) {
			throw new IllegalStateException(
					"Feature with key " + newFeature.getKey() + " already exists");
		}
	}
	
	public void addTopicFeature(TopicFeature feature) {
		checkFeatureDoesNotExist(feature);
		_allFeatures.put(feature.getKey(), feature);
		_topicFeatures.put(feature.getKey(), feature);
	}
	
	public void addEntityFeature(EntityFeature feature) {
		checkFeatureDoesNotExist(feature);
		_allFeatures.put(feature.getKey(), feature);
		_entityFeatures.put(feature.getKey(), feature);
	}
	
	public void addGroupFeature(GroupFeature feature) {

		// check all group features don't exist yet
		List<GroupFeature> groupSubGroupsChecked = new ArrayList<GroupFeature>();
		Queue<GroupFeature> queue = new LinkedList<GroupFeature>();
		queue.add(feature);
		while (queue.size() > 0) {
			
			GroupFeature group = queue.poll();
			
			// check feature doesn't exist yet
			checkFeatureDoesNotExist(group);

			// remember this checked group
			groupSubGroupsChecked.add(group);
			
			// queue children for processing
			List<GroupFeature> childGroups = group.getChildGroups();
			for (int i=0; i<childGroups.size(); i++) {
				queue.add(childGroups.get(i));
			}
		}
		
		// register all group features
		for (int i=0; i<groupSubGroupsChecked.size(); i++) {
			GroupFeature subGroup = groupSubGroupsChecked.get(i);
			_allFeatures.put(subGroup.getKey(), subGroup);
		}

		_rootGroup.addChildGroup(feature);
	}

	public Feature getLeafFeature(String key) {

		Feature feature;
		
		feature = _topicFeatures.get(key);
		if (feature != null) {
			return feature;
		}
		
		feature = _entityFeatures.get(key);
		if (feature != null) {
			return feature;
		}
		
		return null;
	}
	
	public GroupFeature getGroupFeature(String key) {
		
		Feature feature = _allFeatures.get(key);
		if (feature == null) {
			return null;
		}
		if (feature instanceof GroupFeature) {
			return (GroupFeature)feature;
		} else {
			throw new IllegalStateException("Feature " + key + " is not a group feature");
		}
	}
	
	public Feature getFeature(String key) {
		return _allFeatures.get(key);
	}
	
	public Map<String, Feature> getAllFeatures() {
		return _allFeatures;
	}
	
	public Map<String, TopicFeature> getTopicFeatures() {
		return _topicFeatures;
	}
	
	public Map<String, EntityFeature> getEntityFeatures() {
		return _entityFeatures;
	}
	
	public GroupFeature getRootGroup() {
		return _rootGroup;
	}
}
