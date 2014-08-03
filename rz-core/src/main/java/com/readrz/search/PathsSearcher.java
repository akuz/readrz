package com.readrz.search;

import com.mongodb.DBCollection;
import com.readrz.data.Paths;
import com.readrz.data.PathsRequest;

public final class PathsSearcher {
	
	private static DBCollection _pathsColl;
	private static DBCollection _pathsreqColl;

	public PathsSearcher(DBCollection pathsColl, DBCollection pathsreqColl) {
		_pathsColl = pathsColl;
		_pathsreqColl = pathsreqColl;
	}
	
	public Paths findOne(byte[] idData) {
		return Paths.findOne(_pathsColl, idData);
	}

	public void request(PathsRequest paths) {
		paths.upsertUnacknowledged(_pathsreqColl);
	}
}
