package com.readrz.data.user;

import java.util.Date;

import me.akuz.core.DateUtils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Data access to "userOldName" collection.
 *
 */
public final class UserOldNameDA {
	
	private final int EXPIRY_DAYS = 7;
	
	private static final String _idField = MongoUtils._id;
	private static final String _userIdField = "uid";
	private static final String _dateField = "d";

	private final DBCollection _userOldNameColl;
	
	public UserOldNameDA(DBCollection userOldNameColl) {
		_userOldNameColl = userOldNameColl;
	}
	
	public void saveOldUsername(String usernameSystem, ObjectId userId) {
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, usernameSystem);
		q.put(_userIdField, userId);
		q.put(_dateField, new Date());
		_userOldNameColl.save(q);
	}
	
	public boolean canTakeUsername(String usernameSystem, ObjectId userId) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, usernameSystem);
		
		DBObject dbo = _userOldNameColl.findOne(q);
		if (dbo == null) {
			return true;
		}
		
		ObjectId oldUserId = (ObjectId)dbo.get(_userIdField);
		if (oldUserId == null || oldUserId.equals(userId)) {
			return true;
		}
				
		Date date = (Date)dbo.get(_dateField);
		if (date == null) {
			return true;
		}

		Date now = new Date();
		double daysSince = DateUtils.daysBetween(date, now);
		if (daysSince > EXPIRY_DAYS) {
			return true;
		}
		
		return false;
	}

	public static void ensureIndices(DBCollection coll) {
		
		// no additional indices for now
	}

}
