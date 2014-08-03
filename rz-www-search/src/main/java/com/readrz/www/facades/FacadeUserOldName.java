package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.user.UserOldNameDA;

public class FacadeUserOldName {

	private static final Object _lock = new Object();
	private static UserOldNameDA _instance;
	
	public static UserOldNameDA get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					_instance = new UserOldNameDA(
							db.getCollection(MongoColls.userOldName));
				}
			}
		}
		return _instance;
	}
}
