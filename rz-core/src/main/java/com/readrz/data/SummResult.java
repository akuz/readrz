package com.readrz.data;

import java.util.Date;

import org.bson.types.Binary;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Result of summary calculation.
 * 
 */
public final class SummResult {
	
	public final static String _idField         = MongoUtils._id;
	public final static String _createDateField = "createDate";
	public final static String _dyingDateField  = "dyingDate";
	public final static String _deadDateField   = "deadDate";
	public final static String _summField       = "summ";

	protected final DBObject _dbo;
	
	public SummResult(
			SummId summId, 
			Date createDate, 
			Date dyingDate, 
			Date deadDate, 
			Summ summ) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_idField, new Binary(summId.getData()));
		_dbo.put(_createDateField, createDate);
		_dbo.put(_dyingDateField, dyingDate);
		_dbo.put(_deadDateField, deadDate);
		_dbo.put(_summField, summ.getDbo());
	}
	
	public SummResult(DBObject dbo) {
		_dbo = dbo;
	}
	
	public SummId getId() {
		return new SummId(getIdData());
	}
	public final byte[] getIdData() {
		Object dbo = _dbo.get(_idField);
		if (dbo instanceof Binary) {
			return ((Binary)dbo).getData();
		} else {
			return (byte[])dbo;
		}
	}
	
	public Date getCreateDate() {
		return (Date)_dbo.get(_createDateField);
	}
	
	public Date getDyingDate() {
		return (Date)_dbo.get(_dyingDateField);
	}
	
	public Date getDeadDate() {
		return (Date)_dbo.get(_deadDateField);
	}
	
	public final Summ getSumm() {
		return new Summ((DBObject)_dbo.get(_summField));
	}
	
	public AliveStatus getAliveStatus(Date now) {
		if (getDeadDate().compareTo(now) < 0) {
			return AliveStatus.Dead;
		}
		if (getDyingDate().compareTo(now) < 0) {
			return AliveStatus.Dying;
		}
		return AliveStatus.Alive;
	}
	
	public final void upsertUnacknowledged(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getIdData());
		MongoUtils.upsertUnacknowledged(coll, q, _dbo);
	}
	
	public final static AliveStatus findAliveStatus(
			DBCollection coll, 
			byte[] id, 
			Date now) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, new Binary(id));
		
		BasicDBObject f = new BasicDBObject();
		f.put(_dyingDateField, 1);
		f.put(_deadDateField, 1);
		
		DBObject dbo = coll.findOne(q, f);
		if (dbo != null) {
			
			SummResult result = new SummResult(dbo);
			return result.getAliveStatus(now);
			
		} else { // not found at all
			
			return AliveStatus.Dead;
		}
	}
	
	public final static SummResult findOne(DBCollection coll, byte[] id) {
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, new Binary(id));
		DBObject dbo = coll.findOne(q);
		return dbo == null ? null : new SummResult(dbo);
	}
	
}
