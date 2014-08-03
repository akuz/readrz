package com.readrz.utils.db;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public final class MongoUtils {
	
	public final static void updateOne(DBCollection coll, DBObject q, DBObject u) {
		coll.update(q, u, false, false, WriteConcern.ACKNOWLEDGED);
	}
	
	public final static void updateOne(DBCollection coll, DBObject q, DBObject u, WriteConcern wc) {
		coll.update(q, u, false, false, wc);
	}

	public final static boolean upsert(DBCollection coll, DBObject q, DBObject dbo, boolean ensureId) {

		WriteResult wr = coll.update(q, dbo, true, false, WriteConcern.ACKNOWLEDGED);
		CommandResult cr = wr.getLastError();
		boolean updatedExisting = cr.getBoolean(MongoUtils.updatedExisting);
		
		if (ensureId) {
			if (updatedExisting) {
				
				BasicDBObject f = new BasicDBObject();
				f.put(MongoUtils._id, 1);
				
				DBObject o = coll.findOne(q, f);
				dbo.put(MongoUtils._id, o.get(MongoUtils._id));
				
			} else {
				ObjectId upserted = cr.getObjectId(MongoUtils.upserted);
				dbo.put(MongoUtils._id, upserted);
			}
		}
		
		return updatedExisting;		
	}

	public final static void upsertUnacknowledged(DBCollection coll, DBObject q, DBObject dbo) {

		coll.update(q, dbo, true, false, WriteConcern.UNACKNOWLEDGED);
	}

	public final static boolean getIdOrUpsert(DBCollection coll, DBObject q, DBObject dbo, boolean ensureId) {

		// only get id (to check if exists)
		BasicDBObject f = new BasicDBObject();
		f.put(MongoUtils._id, 1);
		
		// search for existing
		DBObject existing = coll.findOne(q, f);
		
		if (existing != null) {
			
			// copy id from the found existing value
			dbo.put(MongoUtils._id, existing.get(MongoUtils._id));
			return true;
			
		} else {
			
			// insert or update (if collision)
			return upsert(coll, q, dbo, ensureId);
		}
	}

	public final static String _id = "_id";
	public final static String upserted = "upserted";
	public final static String updatedExisting = "updatedExisting";
}
