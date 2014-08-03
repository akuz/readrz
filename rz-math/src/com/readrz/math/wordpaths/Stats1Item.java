package com.readrz.math.wordpaths;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;

public final class Stats1Item {
	
	private final Integer _keyId;
	private final Set<ObjectId> _snapIds;
	
	public Stats1Item(Integer keyId) {
		_keyId = keyId;
		_snapIds = new HashSet<>();
	}
	
	public Integer getKeyId() {
		return _keyId;
	}
	
	public int getSnapCount() {
		return _snapIds.size();
	}
	
	public Set<ObjectId> getSnapIds() {
		return _snapIds;
	}

	public void addSnap(ObjectId snapId) {
		_snapIds.add(snapId);
	}

}
