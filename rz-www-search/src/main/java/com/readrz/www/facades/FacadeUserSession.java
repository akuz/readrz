package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.user.UserDA;
import com.readrz.data.user.UserSessionDA;

public class FacadeUserSession {

	private static final Object _lock = new Object();
	private static UserSessionDA _instance;
	
	public static UserSessionDA get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					
					UserDA userDA = FacadeUser.get();
					
					_instance = new UserSessionDA(
							userDA,
							db.getCollection(MongoColls.userSession));
				}
			}
		}
		return _instance;
	}
}
