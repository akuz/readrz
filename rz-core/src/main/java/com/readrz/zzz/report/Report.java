package com.readrz.zzz.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Report implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String _title;
	private String _dateStr;
	private List<EntityNode> _entityNodes;
	private GroupNode _rootGroupNode;
	
	/**
	 * Needed for deserialization.
	 */
	public Report() {
	}
	
	public Report(String title, String dateStr) {
		_title = title;
		_dateStr = dateStr;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public String getDateStr() {
		return _dateStr;
	}
	
	public GroupNode getRootGroupNode() {
		return _rootGroupNode;
	}
	
	public void setRootGroupNode(GroupNode rootGroupNode) {
		_rootGroupNode = rootGroupNode;
	}
	
	public List<EntityNode> getEntityNodes() {
		return _entityNodes;
	}
	
	public void addEntityNode(EntityNode entityNode) {
		if (_entityNodes == null) {
			_entityNodes = new ArrayList<EntityNode>();
		}
		_entityNodes.add(entityNode);
	}
}
