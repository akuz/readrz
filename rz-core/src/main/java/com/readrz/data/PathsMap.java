package com.readrz.data;

import com.mongodb.DBObject;
import com.readrz.data.ontology.Group;

/**
 * Helps to access paths items packaged in a map.
 *
 */
public final class PathsMap {

	private final Paths _paths;
	private final DBObject _map;
	
	public PathsMap(Paths paths, DBObject map) {
		_paths = paths;
		_map = map;
	}
	
	public Integer getCountForGroup(Group group) {
		if (group == null) {
			return _paths.getRootItem().getCount();
		} else {
			Integer groupKeyId = group.getKeyId();
			DBObject pathsItemDbo = (DBObject)_map.get(groupKeyId.toString());
			if (pathsItemDbo != null) {
				PathsItem pathsItem = new PathsItem(pathsItemDbo);
				return pathsItem.getCount();
			} else {
				return null;
			}
		}
	}
	
	public PathsItem getItemForKey(Integer keyId) {
		DBObject pathsItemDbo = (DBObject)_map.get(keyId.toString());
		if (pathsItemDbo != null) {
			return new PathsItem(pathsItemDbo);
		} else {
			return null;
		}
	}
}
