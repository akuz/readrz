package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;

public final class FacadeKeys {
	
	private static final Object _lock = new Object();
	private static KeysIndex _instance;
	
	public static KeysIndex get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new KeysIndex(
						db.getCollection(MongoColls.keys));
					
					_instance.loadFromDB();
				}
			}
		}
		return _instance;
	}

}
