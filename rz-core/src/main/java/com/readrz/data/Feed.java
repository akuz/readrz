package com.readrz.data;

import java.sql.SQLException;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Feed that we take snaps of (single RSS feed, 
 * single web-page, Twitter user feed, etc).
 *
 */
public final class Feed {

	public  static final String lastGrConTag    = ".";
	
	private static final String _sourceIdField  = "sourceId";
	private static final String _titleField     = "title";
	private static final String _descField      = "desc";
	private static final String _urlField       = "url";
	private static final String _tagsField      = "tags";
	private static final String _grConTagField  = "grConTag";
	
	private final DBObject _dbo;
	
	public Feed(ObjectId sourceId, String title, String desc, String url, BasicDBList tags) {
		_dbo = new BasicDBObject();
		setSourceId(sourceId);
		setTitle(title);
		setDesc(desc);
		setUrl(url);
		setTags(tags);
	}
	
	public Feed(DBObject dbo) {
		_dbo = dbo;
	}

	public ObjectId getId() {
		return (ObjectId)_dbo.get(MongoUtils._id);
	}
	
	public ObjectId getSourceId() {
		return (ObjectId)_dbo.get(_sourceIdField);
	}
	
	public void setSourceId(ObjectId sourceId) {
		if (sourceId == null) {
			_dbo.removeField(_sourceIdField);
		} else {
			_dbo.put(_sourceIdField, sourceId);
		}
	}
	
	public String getTitle() {
		return (String)_dbo.get(_titleField);
	}
	
	public void setTitle(String title) {
		if (title == null) {
			_dbo.removeField(_titleField);
		} else {
			_dbo.put(_titleField, title);
		}
	}
	
	public String getDesc() {
		return (String)_dbo.get(_descField);
	}
	
	public void setDesc(String desc) {
		if (desc == null) {
			_dbo.removeField(_descField);
		} else {
			_dbo.put(_descField, desc);
		}
	}
	
	public String getUrl() {
		return (String)_dbo.get(_urlField);
	}
	
	public void setUrl(String url) {
		if (url == null) {
			_dbo.removeField(_urlField);
		} else {
			_dbo.put(_urlField, url);
		}
	}
	
	public String getGrConTag() {
		return (String)_dbo.get(_grConTagField);
	}
	
	public void setGrConTag(String grConTag) {
		if (grConTag == null) {
			_dbo.removeField(_grConTagField);
		} else {
			_dbo.put(_grConTagField, grConTag);
		}
	}
	
	public BasicDBList getTags() {
		return (BasicDBList)_dbo.get(_tagsField);
	}
	
	public void setTags(BasicDBList tags) {
		if (tags != null) {
			_dbo.put(_tagsField, tags);
		} else {
			_dbo.removeField(_tagsField);
		}
	}
	
	public static void ensureIndices(DBCollection coll) {
		
		coll.createIndex(
				new BasicDBObject()
					.append(_urlField, 1),
				new BasicDBObject()
					.append("name", "idxUrl")
					.append("unique", true));
		
		coll.createIndex(
				new BasicDBObject()
					.append(_sourceIdField, 1),
				new BasicDBObject()
					.append("name", "idxSource"));
	}

	public static DBObject getById(DBCollection coll, ObjectId feedId) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(MongoUtils._id, feedId);
		return coll.findOne(q);
	}
	
	public boolean saveByUrlEnsureId(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_urlField, getUrl());
		return MongoUtils.getIdOrUpsert(coll, q, _dbo, true);
	}
	
	public void updateGrConTag(DBCollection coll) throws SQLException {

		BasicDBObject q = new BasicDBObject();
		q.put(MongoUtils._id, getId());

		BasicDBObject u = 
			new BasicDBObject().append(
			"$set", new BasicDBObject().append(
					_grConTagField, getGrConTag()));
		
		MongoUtils.updateOne(coll, q, u);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
