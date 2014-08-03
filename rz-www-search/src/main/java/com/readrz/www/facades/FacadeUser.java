package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.user.UserDA;

public class FacadeUser {

	private static final Object _lock = new Object();
	private static UserDA _instance;
	
	public static UserDA get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new UserDA(
							db.getCollection(MongoColls.user));
				}
			}
		}
		return _instance;
	}
}
