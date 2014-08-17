package com.readrz.data;

import java.util.List;

import me.akuz.core.SystemUtils;
import me.akuz.core.gson.GsonSerializers;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.data.mongo.MongoObject;
import com.readrz.utils.db.MongoUtilsAdaptT;

/**
 * An item within the Paths calculation result object.
 *
 */
public final class PathsItem implements MongoObject {
	
	private final DBObject _dbo;
	
	private static final String _fullSearchField  = "fullSearch";
	private static final String _fullKeyIdsField  = "fullKeyIds";
	private static final String _hierSearchField  = "hierSearch";
	private static final String _hierKeyIdsField  = "hierKeyIds";
	private static final String _leafSearchField  = "leafSearch";
	private static final String _leafKeyIdField   = "leafKeyId";
	private static final String _countField       = "count";
	private static final String _snapIdField      = "snapId";
	private static final String _childrenField    = "children";
	
	private transient List<Integer> _transient_fullSentenceKeyIds;
	private transient List<Integer> _transient_hierSentenceKeyIds;
	
	public PathsItem(DBObject dbo) {
		_dbo = dbo;
	}
	
	public PathsItem(
			String fullSearch,
			List<Integer> fullKeyIds,
			String hierSearch,
			List<Integer> hierKeyIds,
			String leafSearch,
			Integer leafKeyId,
			Integer count,
			ObjectId snapId) {
		
		_dbo = new BasicDBObject();
		_transient_fullSentenceKeyIds = fullKeyIds;
		_dbo.put(_fullSearchField, fullSearch);
		_dbo.put(_fullKeyIdsField, fullKeyIds);
		_dbo.put(_hierSearchField, hierSearch);
		_dbo.put(_hierKeyIdsField, hierKeyIds);
		_dbo.put(_leafSearchField, leafSearch);
		_dbo.put(_leafKeyIdField, leafKeyId);
		_dbo.put(_countField, count);
		if (snapId != null) {
			_dbo.put(_snapIdField, snapId.toString());
		}
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public List<Integer> getFullKeyIds() {
		if (_transient_fullSentenceKeyIds == null) {
			_transient_fullSentenceKeyIds = MongoUtilsAdaptT.Integer.toList((BasicDBList)_dbo.get(_fullKeyIdsField));
		}
		return _transient_fullSentenceKeyIds;
	}
	
	public List<Integer> getHierKeyIds() {
		if (_transient_hierSentenceKeyIds == null) {
			_transient_hierSentenceKeyIds = MongoUtilsAdaptT.Integer.toList((BasicDBList)_dbo.get(_hierKeyIdsField));
		}
		return _transient_hierSentenceKeyIds;
	}
	
	public Integer getLeafKeyId() {
		return (Integer)_dbo.get(_leafKeyIdField);
	}
	
	public Integer getCount() {
		return (Integer)_dbo.get(_countField);
	}
	
	public String getSnapId() {
		return (String)_dbo.get(_snapIdField);
	}
	
	public ObjectId getSnapObjectId() {
		String snapId = getSnapId();
		return snapId != null ? new ObjectId(snapId) : null;
	}
	
	public String getFullSearch() {
		return (String)_dbo.get(_fullSearchField);
	}
	
	public String getHierSearch() {
		return (String)_dbo.get(_hierSearchField);
	}
	
	public String getLeafSearch() {
		return (String)_dbo.get(_leafSearchField);
	}
	
	public void addChild(DBObject dbo) {
		BasicDBList list = (BasicDBList)_dbo.get(_childrenField);
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_childrenField, list);
		}
		list.add(dbo);
	}
	public BasicDBList getChildren() {
		return (BasicDBList)_dbo.get(_childrenField);
	}
	public BasicDBList ensureChildren() {
		BasicDBList list = (BasicDBList)_dbo.get(_childrenField);
		if (list == null) {
			list = new BasicDBList();
			_dbo.put(_childrenField, list);
		}
		return list;
	}
	
	@Override
	public String toString() {
		return SystemUtils.isLocalhost()
				? GsonSerializers.NoHtmlEscapingPretty.toJson(_dbo)
				: GsonSerializers.NoHtmlEscapingPlain.toJson(_dbo);
	}
}
