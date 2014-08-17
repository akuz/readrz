package com.readrz.data.ontology;

public final class GroupInfo {
	
	private final Group _group;
	private final boolean _isTopic;
	
	public GroupInfo(Group group, boolean isTopic) {
		_group = group;
		_isTopic = isTopic;
	}
	
	public Group getGroup() {
		return _group;
	}
	
	public boolean isTopic() {
		return _isTopic;
	}
}
