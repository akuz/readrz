package com.readrz.math.topicmodels;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.Index;
import me.akuz.core.Pair;

import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Group;
import com.readrz.search.Query;
import com.readrz.search.QueryParser;

public final class LDABuildTopicsTree {
	
	private final QueryParser _queryParser;
	private final KeysIndex _keysIndex;
	private final Index<String> _stemsIndex;
	private final double _priorityStemsMassFraction;
	private LDABuildTopicsTreeNode _rootNode;
	
	public LDABuildTopicsTree(
			QueryParser queryParser, 
			KeysIndex keysIndex, 
			Index<String> stemsIndex, 
			double priorityStemsMassFraction) {
		
		_queryParser = queryParser;
		_keysIndex = keysIndex;
		_stemsIndex = stemsIndex;
		_priorityStemsMassFraction = priorityStemsMassFraction;
		_rootNode = new LDABuildTopicsTreeNode(this);
	}
	
	public Set<Integer> parse(String queryString) {
		Query query = _queryParser.parse(queryString);
		// return unique sentence keys ids for this purpose
		return new HashSet<Integer>(query.getQueryKeyIds().getSentenceKeyIds());
	}
	
	public double getDefaultPriorityStemsMassFraction() {
		return _priorityStemsMassFraction;
	}
	
	public LDABuildTopicsTreeNode getRootNode() {
		return _rootNode;
	}
	
	public LDABuildTopicsTreeNode addChildNode(double estimatedCorpusPlacesFraction, String leafTopicId, String topicName) {
		return _rootNode.addChildNode(estimatedCorpusPlacesFraction, leafTopicId, topicName);
	}
	
	public List<LDABuildTopic> buildTopics() {
		
		List<LDABuildTopic> topics = new ArrayList<>();
		Deque<LDABuildTopicsTreeNode> queue = new LinkedList<>();
		queue.addAll(_rootNode.getChildren());
		
		while (queue.size() > 0) {
			
			LDABuildTopicsTreeNode node = queue.poll();

			// create topic for this node
			if (node.getEstimatedCorpusPlacesFraction() > 0){
				
				LDABuildTopic topic = new LDABuildTopic(
						node.getId(), 
						node.getEstimatedCorpusPlacesFraction(),
						node.isTransient(),
						node.isGroup());
				
				// set priority weights
				if (node.getPriorityKeyIds().size() > 0) {
					double priorityStemMassFraction = node.getPriorityStemsMassFraction() / node.getPriorityKeyIds().size();
					for (Integer keyId : node.getPriorityKeyIds()) {
						String stem = _keysIndex.getStrCached(keyId);
						if (stem != null) {
							Integer stemIndex = _stemsIndex.getIndex(stem);
							if (stemIndex != null) {
								topic.addPriorityStemMass(stemIndex, priorityStemMassFraction);
							}
						}
					}
				}
				
				// set excluded weights
				if (node.getExcludedKeyIds().size() > 0) {
					for (Integer keyId : node.getExcludedKeyIds()) {
						String stem = _keysIndex.getStrCached(keyId);
						if (stem != null) {
							Integer stemIndex = _stemsIndex.getIndex(stem);
							if (stemIndex != null) {
								topic.addExcludedStem(stemIndex);
							}
						}
					}
				}
				topics.add(topic);
			}
			
			// create topics for child nodes
			List<LDABuildTopicsTreeNode> childNodes = node.getChildren();
			if (childNodes.size() > 0) {
				for (int i=childNodes.size()-1; i>=0; i--) {
					queue.addFirst(childNodes.get(i));
				}
			} 
		}
		
		return topics;
	}
	
	public List<Group> buildGroups(String rootGroupId) {
		
		List<Group> rootGroups = new ArrayList<>();
		
		Queue<Pair<LDABuildTopicsTreeNode, Group>> queue = new LinkedList<>();

		// create root groups
		for (LDABuildTopicsTreeNode rootNode : _rootNode.getChildren()) {
			if (rootNode.isGroup()) {
				Group rootGroup = new Group(rootGroupId + "/" + rootNode.getLeafId(), rootNode.getName(), rootNode.isGroupTaxonomy());
				rootGroups.add(rootGroup);
				queue.add(new Pair<LDABuildTopicsTreeNode, Group>(rootNode, rootGroup));
			}
		}
		
		// create branch groups
		while (queue.size() > 0) {
	
			// next node to expand
			Pair<LDABuildTopicsTreeNode, Group> pair = queue.poll();
			LDABuildTopicsTreeNode parentNode = pair.v1();
			Group parentGroup = pair.v2();
			
			// create child groups
			for (LDABuildTopicsTreeNode childNode : parentNode.getChildren()) {
				
				// if node is marked as group
				if (childNode.isGroup()) {
					
					// create child group
					Group childGroup = new Group(childNode.getLeafId(), childNode.getName(), childNode.isGroupTaxonomy());
					
					// add to parent group
					parentGroup.addChildGroup(childGroup.getDbo());
					
					// queue node for expansion
					if (childNode.getChildren().size() > 0) {
						queue.add(new Pair<LDABuildTopicsTreeNode, Group>(childNode, childGroup));
					}
				}
			}
		}
		
		return rootGroups;
	}

}
