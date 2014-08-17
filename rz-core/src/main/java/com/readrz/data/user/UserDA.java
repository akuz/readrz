package com.readrz.data.user;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Data access to "user" collection.
 *
 */
public final class UserDA {
	
	private final DBCollection _userColl;
	
	public UserDA(DBCollection userColl) {
		_userColl = userColl;
	}
	
	public User findByUsernameSystem(String usernameSystem) {
		
		BasicDBObject q 
			= new BasicDBObject()
			.append(User._usernameSystemField, usernameSystem);
		
		DBObject dbo = _userColl.findOne(q);
		
		return dbo != null ? new User(dbo) : null;
	}
	
	public User findByEmail(String email) {
		
		BasicDBObject q 
			= new BasicDBObject()
			.append(User._emailField, email);
		
		DBObject dbo = _userColl.findOne(q);
		
		return dbo != null ? new User(dbo) : null;
	}
	
	public User findUserById(ObjectId userId) {
		
		BasicDBObject q 
			= new BasicDBObject()
			.append(User._idField, userId);
		
		DBObject dbo = _userColl.findOne(q);
		
		return dbo != null ? new User(dbo) : null;
	}

	public void saveUser(DBObject dbo) {
		_userColl.save(dbo);
	}

}
