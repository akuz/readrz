package com.readrz.data.user;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * User core identity object.
 * 
 */
public final class User {

	public static final String _idField = MongoUtils._id;
	public static final String _usernameSystemField = "u";
	public static final String _usernameDisplayField = "d";
	public static final String _passwordHashField = "ph";
	public static final String _emailField = "e";

	private final DBObject _dbo;

	public User(ObjectId id, String usernameDisplay, String email, String passwordHash) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, id);
		setUsernameDisplay(usernameDisplay);
		setEmail(email);
		setPasswordHash(passwordHash);
	}
	
	public User(String usernameDisplay, String email, String passwordHash) {
		_dbo = new BasicDBObject();
		setUsernameDisplay(usernameDisplay);
		setEmail(email);
		setPasswordHash(passwordHash);
	}
	
	public User(DBObject dbo) {
		_dbo = dbo;
	}
	
	public DBObject getDbo() {
		return _dbo;
	}

	public ObjectId getId() {
		return (ObjectId)_dbo.get(_idField);
	}
	
	public String getUsernameSystem() {
		return (String)_dbo.get(_usernameSystemField);
	}
	
	public String getUsernameDisplay() {
		return (String)_dbo.get(_usernameDisplayField);
	}
	
	public void setUsernameDisplay(String usernameDisplay) {
		if (usernameDisplay == null || usernameDisplay.length() == 0) {
			throw new IllegalArgumentException("Username cannot be null or empty");
		}
		_dbo.put(_usernameSystemField, toUsernameSystem(usernameDisplay));
		_dbo.put(_usernameDisplayField, usernameDisplay);
	}
	
	public static final String toUsernameSystem(String usernameDisplay) {
		return usernameDisplay.toLowerCase();
	}
	
	public String getEmail() {
		return (String)_dbo.get(_emailField);
	}
	
	public void setEmail(String email) {
		if (email == null) {
			_dbo.removeField(_emailField);
		} else {
			_dbo.put(_emailField, email);
		}
	}
	
	public String getPasswordHash() {
		return (String)_dbo.get(_passwordHashField);
	}
	public void setPasswordHash(String hash) {
		if (hash == null) {
			throw new NullPointerException("Password hash be null");
		}
		_dbo.put(_passwordHashField, hash);
	}
	
	public static final void ensureIndices(DBCollection coll) {
		
		coll.createIndex(
				new BasicDBObject()
					.append(_usernameSystemField, 1),
				new BasicDBObject()
					.append("name", "idxUsernameSystem")
					.append("unique", true)
					.append("sparse", true));
		
		coll.createIndex(
				new BasicDBObject()
					.append(_emailField, 1),
				new BasicDBObject()
					.append("name", "idxEmail")
					.append("unique", true)
					.append("sparse", true));
	}

}
