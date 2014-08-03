package com.readrz.math.topicmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LDABuildTopicsTreeNode {
	
	private final LDABuildTopicsTree _tree;
	private final String _leafId;
	private final String _id;
	private final String _name;
	private final Set<Integer> _priorityKeyIds;
	private final Set<Integer> _excludedKeyIds;
	private final double _estimatedCorpusPlacesFraction;
	private double _priorityStemsMassFraction;
	private final List<LDABuildTopicsTreeNode> _children;
	private final LDABuildTopicsTreeNode _parent;
	private boolean _isTransient;
	private boolean _isGroup;
	private boolean _isGroupTaxonomy;
	
	public LDABuildTopicsTreeNode(LDABuildTopicsTree tree) {
		this(tree, null, 0, null, null);
	}
	
	private LDABuildTopicsTreeNode(
			LDABuildTopicsTree tree, 
			LDABuildTopicsTreeNode parent, 
			double estimatedCorpusPlacesFraction, 
			String leafId,
			String name) {
		
		_tree = tree;
		_priorityKeyIds = new HashSet<>();
		_excludedKeyIds = new HashSet<>();
		_leafId = leafId;
		if (parent == null) {
			_id = leafId;
		} else {
			String parentId = parent.getId();
			if (parentId != null) {
				_id = parent.getId() + "/" + leafId;
			} else {
				_id = leafId;
			}
			_priorityKeyIds.addAll(parent.getPriorityKeyIds());
			_excludedKeyIds.addAll(parent.getExcludedKeyIds());
		}
		_name = name;
		_priorityStemsMassFraction = tree.getDefaultPriorityStemsMassFraction();
		_estimatedCorpusPlacesFraction = estimatedCorpusPlacesFraction;
		_children = new ArrayList<>();
		_parent = parent;
	}
	
	public String getLeafId() {
		return _leafId;
	}
	
	public String getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public boolean isTransient() {
		return _isTransient;
	}
	public LDABuildTopicsTreeNode isTransient(boolean is) {
		_isTransient = is;
		return this;
	}
	
	public boolean isGroup() {
		return _isGroup;
	}
	public LDABuildTopicsTreeNode isGroup(boolean is) {
		_isGroup = is;
		return this;
	}
	
	public boolean isGroupTaxonomy() {
		return _isGroupTaxonomy;
	}
	public LDABuildTopicsTreeNode isGroupTaxonomy(boolean is) {
		_isGroupTaxonomy = is;
		return this;
	}
	
	public Set<Integer> getPriorityKeyIds() {
		return _priorityKeyIds;
	}
	
	public Set<Integer> getExcludedKeyIds() {
		return _excludedKeyIds;
	}
	
	public double getPriorityStemsMassFraction() {
		return _priorityStemsMassFraction;
	}
	public LDABuildTopicsTreeNode setPriorityStemsMassFraction(double massFraction) {
		_priorityStemsMassFraction = massFraction;
		return this;
	}
	
	public double getEstimatedCorpusPlacesFraction() {
		return _estimatedCorpusPlacesFraction;
	}
	
	public LDABuildTopicsTreeNode addChildNode(double estimatedCorpusPlacesFraction, String leafTopicId, String topicName) {
		LDABuildTopicsTreeNode childNode = new LDABuildTopicsTreeNode(_tree, this, estimatedCorpusPlacesFraction, leafTopicId, topicName);
		_children.add(childNode);
		return childNode;
	}
	
	public LDABuildTopicsTreeNode addPriorityWords(String queryString) {
		Set<Integer> keyIds = _tree.parse(queryString);
		_priorityKeyIds.addAll(keyIds);
		return this;
	}
	
	public LDABuildTopicsTreeNode addExcludedWords(String queryString) {
		Set<Integer> keyIds = _tree.parse(queryString);
		_excludedKeyIds.addAll(keyIds);
		return this;
	}
	
	public LDABuildTopicsTreeNode getParent() {
		return _parent;
	}
	
	public List<LDABuildTopicsTreeNode> getChildren() {
		return _children;
	}

}
