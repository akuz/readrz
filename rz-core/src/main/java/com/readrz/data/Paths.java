package com.readrz.data;

import java.util.Date;

import org.bson.types.Binary;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Paths for a PathsId, which does *not* include a groupKeyId
 * (this is main paths for a PathsId, they include Top Paths,
 * as well as group stats for the PathsId without groupKeyId).
 * 
 */
public final class Paths {
	
	public final static String _idField                    = MongoUtils._id;
	public final static String _dateField                  = "d";
	public final static String _rootItemField              = "ri";
	public final static String _autoItemsListField         = "ai";
	public final static String _patternEntityItemsMapField = "pei";
	public final static String _patternGroupItemsMapField  = "pgi";
	public final static String _topicGroupItemsMapField    = "tgi";

	protected final DBObject _dbo;
	
	public Paths(
			byte[] id, 
			Date date, 
			DBObject rootItem,
			BasicDBList autoItemsList,
			DBObject patternEntityItemsMap,
			DBObject patternGroupItemsMap,
			DBObject topicGroupItemsMap) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_idField, new Binary(id));
		_dbo.put(_dateField, date);
		_dbo.put(_rootItemField, rootItem);
		_dbo.put(_autoItemsListField, autoItemsList);
		_dbo.put(_patternEntityItemsMapField, patternEntityItemsMap);
		_dbo.put(_patternGroupItemsMapField, patternGroupItemsMap);
		_dbo.put(_topicGroupItemsMapField, topicGroupItemsMap);
	}
	
	public Paths(DBObject dbo) {
		_dbo = dbo;
	}
	
	public final byte[] getId() {
		Object dbo = _dbo.get(_idField);
		if (dbo instanceof Binary) {
			return ((Binary)dbo).getData();
		} else {
			return (byte[])dbo;
		}
	}
	
	public final Date getDate() {
		return (Date)_dbo.get(_dateField);
	}
	
	public PathsItem getRootItem() {
		return new PathsItem((DBObject)_dbo.get(_rootItemField));
	}
	public Integer getRootCount() {
		PathsItem rootItem = getRootItem();
		return rootItem == null ? null : rootItem.getCount();
	}
	
	public final BasicDBList getAutoItemsList() {
		return (BasicDBList)_dbo.get(_autoItemsListField);
	}
	
	public final DBObject getPatternEntityItemsMap() {
		return (DBObject)_dbo.get(_patternEntityItemsMapField);
	}
	
	public final DBObject getPatternGroupItemsMap() {
		return (DBObject)_dbo.get(_patternGroupItemsMapField);
	}
	
	public final DBObject getTopicGroupItemsMap() {
		return (DBObject)_dbo.get(_topicGroupItemsMapField);
	}

	public final void upsertUnacknowledged(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());
		MongoUtils.upsertUnacknowledged(coll, q, _dbo);
	}
	
	public final static AliveStatus findAliveStatus(
			DBCollection coll, 
			byte[] idData, 
			Date now,
			Period period) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, new Binary(idData));
		
		BasicDBObject f = new BasicDBObject();
		f.put(_dateField, 1);
		
		DBObject dbo = coll.findOne(q, f);
		if (dbo != null) {
			
			return getAliveStatus(dbo, period, now);
			
		} else { // not found at all
			
			return AliveStatus.Dead;
		}
	}
	
	public AliveStatus getAliveStatus(Period period, Date now) {
		return getAliveStatus(_dbo, period, now);
	}
	
	public static AliveStatus getAliveStatus(DBObject dbo, Period period, Date now) {
		Date date = (Date)dbo.get(_dateField);
		return period.getAliveStatus(now, date);
	}
	
	public final static Paths findOne(DBCollection coll, byte[] idData) {
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, new Binary(idData));
		DBObject dbo = coll.findOne(q);
		return dbo == null ? null : new Paths(dbo);
	}
	
}
