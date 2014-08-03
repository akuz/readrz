package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.user.UserCodeDA;

public class FacadeUserCode {

	private static final Object _lock = new Object();
	private static UserCodeDA _instance;
	
	public static UserCodeDA get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new UserCodeDA(
							db.getCollection(MongoColls.userCode));
				}
			}
		}
		return _instance;
	}
}
