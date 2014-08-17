package com.readrz.data.user;

import java.util.Date;

import me.akuz.core.DateUtils;
import me.akuz.core.crypt.bcrypt.BCrypt;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Data access to "userCode" collection.
 *
 */
public final class UserCodeDA {
	
	public static final int EXPIRY_MINS = 15;
	
	private static final String _idField = MongoUtils._id;
	private static final String _codeHashField = "ch";
	private static final String _dateField = "d";

	private final DBCollection _userCodeColl;
	
	public UserCodeDA(DBCollection userCodeColl) {
		_userCodeColl = userCodeColl;
	}
	
	public boolean checkCode(Object userId, String candidateCode) {
		
		BasicDBObject q 
			= new BasicDBObject()
			.append(_idField, userId);

		DBObject dbo = _userCodeColl.findOne(q);
		if (dbo != null) {
		
			// check not expired
			Date date = (Date)dbo.get(_dateField);
			if (date == null) {
				return false;
			}
			Date now = new Date();
			double minsSince = DateUtils.minutesBetween(date, now);
			if (minsSince > EXPIRY_MINS) {
				return false;
			}
			
			// check candidate code
			String codeHash = (String)dbo.get(_codeHashField);
			return BCrypt.checkpw(candidateCode, codeHash);

		} else {
			return false;
		}
	}

	public void saveCode(ObjectId userId, String code) {
		
		// hash code
		String codeHash = BCrypt.hashpw(code, BCrypt.gensalt());
		
		// save record
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, userId);
		q.put(_codeHashField, codeHash);
		q.put(_dateField, new Date());
		_userCodeColl.save(q);
	}
	
	public void removeCode(ObjectId userId) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, userId);
		_userCodeColl.remove(q);
	}

	public static void ensureIndices(DBCollection coll) {
		// no indices for now
	}

}
