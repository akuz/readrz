package com.readrz.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.Binary;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Summary request.
 * 
 */
public final class SummRequest {
	
	public final static String _idField = MongoUtils._id;
	public final static String _isRequestedField = "r";
	
	private final DBObject _dbo;
	
	public SummRequest(byte[] id, boolean isRequested) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, new Binary(id));
		_dbo.put(_isRequestedField, isRequested);
	}
	
	public SummRequest(DBObject dbo) {
		_dbo = dbo;
	}
	
	public byte[] getId() {
		Object dbo = _dbo.get(_idField);
		if (dbo instanceof Binary) {
			return ((Binary)dbo).getData();
		} else {
			return (byte[])dbo;
		}
	}
	
	public boolean isRequested() {
		Boolean is = (Boolean)_dbo.get(_isRequestedField);
		return is == null ? false : is.booleanValue();
	}
	
	public void upsertUnacknowledged(DBCollection coll) {
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());
		MongoUtils.upsertUnacknowledged(coll, q, _dbo);
	}

	public static DBCursor findRequested(DBCollection coll) {
		BasicDBObject q = new BasicDBObject();
		q.put(_isRequestedField, true);
		return coll.find(q);
	}
	
	public static List<SummRequest> findRequestedAsList(DBCollection coll) {
		List<SummRequest> list = null;
		DBCursor cursor = SummRequest.findRequested(coll);
		try {
			while (cursor.hasNext()) {
				SummRequest pathsReq = new SummRequest(cursor.next());
				if (list == null) {
					list = new ArrayList<SummRequest>();
				}
				list.add(pathsReq);
			}
		} finally {
			cursor.close();
		}
		return list;
	}
	
	public static void ensureIndices(DBCollection coll) {

		coll.createIndex(
				new BasicDBObject()
					.append(_isRequestedField, 1),
				new BasicDBObject()
					.append("name", "idxIsRequested"));
	}

}
