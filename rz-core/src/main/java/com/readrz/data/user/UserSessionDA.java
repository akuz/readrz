package com.readrz.data.user;

import java.util.Date;

import me.akuz.core.DateUtils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Data access to "userSession" collection.
 *
 */
public final class UserSessionDA {
	
	private final static double EXTEND_DAYS = 2;
	private final static double EXPIRY_DAYS = 3;
	
	private static final String _idField = MongoUtils._id;
	private static final String _userIdField = "uid";
	private static final String _dateField = "d";

	private final UserDA _userDA;
	private final DBCollection _userSessionColl;
	
	public UserSessionDA(UserDA userDA, DBCollection userSessionColl) {
		_userDA = userDA;
		_userSessionColl = userSessionColl;
	}
	
	public User findUserBySession(String sessionId) {
		
		// find query
		BasicDBObject q 
			= new BasicDBObject()
			.append(_idField, sessionId);

		// find user id by session
		ObjectId userId = null;
		DBObject dbo = _userSessionColl.findOne(q);
		if (dbo != null) {

			// start checking session
			Date date = (Date)dbo.get(_dateField);
			if (date != null) {

				// check expiry
				Date now = new Date();
				double daysSince = DateUtils.daysBetween(date, now);
				if (daysSince < EXPIRY_DAYS) {
					
					// get session user id
					userId = (ObjectId)dbo.get(_userIdField);

					// refresh session validity
					if (daysSince > EXTEND_DAYS) {
						linkSessionToUser(sessionId, userId);
					}
				}
			}
		}
		
		// find user by id
		User user = null;
		if (userId != null) {
			user = _userDA.findUserById(userId);
		}
		
		return user;
	}
	
	public void linkSessionToUser(String sessionId, ObjectId userId) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, sessionId);
		q.put(_userIdField, userId);
		q.put(_dateField, new Date());
		_userSessionColl.save(q);
	}
	
	public void expireSession(String sessionId) {
		
		Date expired = DateUtils.addDays(new Date(), - EXPIRY_DAYS);
		
		BasicDBObject q = new BasicDBObject()
			.append(_idField, sessionId);
		
		BasicDBObject u = new BasicDBObject()
			.append("$set", new BasicDBObject()
					.append(_dateField, expired));

		_userSessionColl.update(q, u);
	}

	public static void ensureIndices(DBCollection coll) {
		
		// no additional indices for now
	}

}
