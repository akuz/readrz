package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.search.SnapSearcher;

public final class FacadeSearcher {
	
	private static final Object _lock = new Object();
	private static SnapSearcher _instance;
	
	public static SnapSearcher get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new SnapSearcher(
							db.getCollection(MongoColls.snaps),
							db.getCollection(MongoColls.snapsidx),
							db.getCollection(MongoColls.feeds),
							db.getCollection(MongoColls.sources));
				}
			}
		}
		return _instance;
	}
	
}
