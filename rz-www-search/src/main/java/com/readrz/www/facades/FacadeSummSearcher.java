package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.search.SummSearcher;

public final class FacadeSummSearcher {
	
	private static final Object _lock = new Object();
	private static SummSearcher _instance;
	
	public static SummSearcher get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new SummSearcher(
							db.getCollection(MongoColls.summ),  
							db.getCollection(MongoColls.summreq));
				}
			}
		}
		return _instance;
	}

}
