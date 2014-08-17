package com.readrz.data.index;

import java.util.Date;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.readrz.utils.db.MongoUtils;

/**
 * Index entry (for inverse index), which includes word id, 
 * matched item id, and also srcDate for sorting, as well as the 
 * binary hit data about word location within the item.
 * 
 */
public final class Idx {
	
	public final static String _keyIdField    = "k";
	public final static String _srcDateField  = "d";
	public final static String _itemIdField   = "i";
	public final static String _hitsDataField = "h";

	public static final BasicDBObject IdxSort;
	public static final BasicDBObject IdxItemId;
	
	static {
		
		BasicDBObject idxSort = new BasicDBObject();
		idxSort.put(_keyIdField, 1);
		idxSort.put(_srcDateField, -1);
		idxSort.put(_itemIdField, -1);
		IdxSort = idxSort;
		
		BasicDBObject idxItemId = new BasicDBObject();
		idxItemId.put(_itemIdField, 1);
		IdxItemId = idxItemId;
	}
	
	private final DBObject _dbo;
	
	public Idx(Integer keyId, Date srcDate, ObjectId itemId, byte[] hitsData) {
		_dbo = new BasicDBObject();
		_dbo.put(_keyIdField, keyId);
		_dbo.put(_srcDateField, srcDate);
		_dbo.put(_itemIdField, itemId);
		_dbo.put(_hitsDataField, new Binary(hitsData));
	}
	
	public Idx(DBObject dbo) {
		_dbo = dbo;
	}
	
	public ObjectId getId() {
		return (ObjectId)_dbo.get(MongoUtils._id);
	}
	
	public Integer getKeyId() {
		return (Integer)_dbo.get(_keyIdField);
	}
	
	public Date getSrcDate() {
		return (Date)_dbo.get(_srcDateField);
	}
	
	public ObjectId getItemId() {
		return (ObjectId)_dbo.get(_itemIdField);
	}
	
	public byte[] getHitsData() {
		Object obj = _dbo.get(_hitsDataField);
		if (obj instanceof Binary) {
			return ((Binary)obj).getData();
		} else {
			return (byte[])obj;
		}
	}
	
	public static void ensureIndices(DBCollection coll) {
		
		coll.createIndex(
				IdxSort, 
				new BasicDBObject()
					.append("name", "idxSort")
					.append("unique", true));
		
		coll.createIndex(
				IdxItemId, 
					new BasicDBObject()
					.append("name", "idxItemId"));
	}

	public void upsertUnacknowledged(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_keyIdField, getKeyId());
		q.put(_srcDateField, getSrcDate());
		q.put(_itemIdField, getItemId());
		MongoUtils.upsertUnacknowledged(coll, q, _dbo);
	}

	public static void removeForSnap(DBCollection coll, ObjectId itemId, WriteConcern writeConcern) {
		BasicDBObject q = new BasicDBObject();
		q.put(_itemIdField, itemId);
		coll.remove(q, writeConcern);
	}
	
}
