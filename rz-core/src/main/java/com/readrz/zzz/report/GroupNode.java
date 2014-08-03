package com.readrz.zzz.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class GroupNode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient GroupNode _parent;
	private String _id;
	private String _name;
	private double _volume;
	private boolean _isTaxonomy;
	private Set<String> _leafEntityKeys;
	private List<GroupNode> _children;
	
	/**
	 * Needed for deserialization.
	 */
	public GroupNode() {
	}
	
	public GroupNode(
			GroupNode parent,
			String id, 
			String name, 
			double volume,
			boolean isTaxonomy) {
		
		_parent = parent;
		_id = id;
		_name = name;
		_volume = volume;
		_isTaxonomy = isTaxonomy;
		_leafEntityKeys = new HashSet<String>();
		_children = new ArrayList<GroupNode>();
	}
	
	public GroupNode getParent() {
		return _parent;
	}
	
	public void setParent(GroupNode parent) {
		_parent = parent;
	}
	
	public String getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public double getVolume() {
		return _volume;
	}
	
	public void addChild(GroupNode child) {
		_children.add(child);
	}
	
	public List<GroupNode> getChildren() {
		return _children;
	}
	
	public void addLeafEntityKey(String key) {
		_leafEntityKeys.add(key);
	}
	
	public Set<String> getLeafEntityKeys() {
		return _leafEntityKeys;
	}
	
	public boolean getIsTaxonomy() {
		return _isTaxonomy;
	}
	
	public Set<String> collectEntityKeysIncludingChildren() {
		
		Set<String> leafKeys = new HashSet<String>();
		
		Queue<GroupNode> queue = new LinkedList<GroupNode>();
		queue.add(this);
		
		while (queue.size() > 0) {

			// take next data
			GroupNode data = queue.poll();
			
			// add data leaf keys
			leafKeys.addAll(data.getLeafEntityKeys());
			
			// process children
			List<GroupNode> children = data.getChildren();
			for (int i=0; i<children.size(); i++) {
				queue.add(children.get(i));
			}
		}

		return leafKeys;
	}
}
