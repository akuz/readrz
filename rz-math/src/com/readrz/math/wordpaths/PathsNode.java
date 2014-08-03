package com.readrz.math.wordpaths;

import java.util.List;

import org.bson.types.ObjectId;

public final class PathsNode {
	
	private final List<Integer> _fullSentenceKeyIds;
	private final List<Integer> _hierSentenceKeyIds;
	private final Integer _leafSentenceKeyId;
	private final String _fullSearch;
	private final String _hierSearch;
	private final String _leafSearch;
	private final Integer _snapCount;
	private final ObjectId _snapId;
	private final List<PathsNode> _children;
	
	/**
	 * Create a summary paths node (more than one snap).
	 * 
	 */
	public PathsNode(
			List<Integer> fullSentenceKeyIds,
			List<Integer> hierSentenceKeyIds,
			Integer leafSentenceKeyId,
			String fullSearch,
			String hierSearch,
			String leafSearch,
			Integer snapCount,
			ObjectId snapId,
			List<PathsNode> children) {
		
		_fullSentenceKeyIds = fullSentenceKeyIds;
		_hierSentenceKeyIds = hierSentenceKeyIds;
		_leafSentenceKeyId = leafSentenceKeyId;
		_fullSearch = fullSearch;
		_hierSearch = hierSearch;
		_leafSearch = leafSearch;
		_snapCount = snapCount;
		_snapId = snapId;
		_children = children;
	}
	
	public List<Integer> getFullSentenceKeyIds() {
		return _fullSentenceKeyIds;
	}
	
	public List<Integer> getHierSentenceKeyIds() {
		return _hierSentenceKeyIds;
	}
	
	public Integer getLeafSentenceKeyId() {
		return _leafSentenceKeyId;
	}
	
	public String getFullSearch() {
		return _fullSearch;
	}
	
	public String getHierSearch() {
		return _hierSearch;
	}
	
	public String getLeafSearch() {
		return _leafSearch;
	}
	
	public Integer getSnapCount() {
		return _snapCount;
	}
	
	public ObjectId getSnapId() {
		return _snapId;
	}
	
	public List<PathsNode> getChildren() {
		return _children;
	}

}
