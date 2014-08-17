package com.readrz.data;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.utils.db.MongoUtils;

/**
 * Source that has provides multiple Feeds (news agency with 
 * multiple RSS feeds, web-site with multiple pages, etc).
 *
 */
public final class Source {
	
	private static final String _nameField = "name";
	private static final String _tagsField = "tags";
	
	private final DBObject _dbo;
	
	public Source(String name, BasicDBList tags) {
		_dbo = new BasicDBObject();
		_dbo.put(_nameField, name);
		_dbo.put(_tagsField, tags);
	}
	
	public Source(DBObject dbo) {
		_dbo = dbo;
	}
	
	public ObjectId getId() {
		return (ObjectId)_dbo.get(MongoUtils._id);
	}
	
	public String getName() {
		return (String)_dbo.get(_nameField);
	}
	
	public void setName(String name) {
		if (name == null) {
			_dbo.removeField(_nameField);
		} else {
			_dbo.put(_nameField, name);
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

	public static DBObject getById(DBCollection coll, ObjectId feedId) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(MongoUtils._id, feedId);
		return coll.findOne(q);
	}
	
	public static void ensureIndices(DBCollection coll) {
		
		coll.createIndex(
				new BasicDBObject()
					.append(_nameField, 1),
				new BasicDBObject()
					.append("name", "idxNameUniq")
					.append("unique", true));
	}
	
	public boolean saveByNameEnsureId(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_nameField, getName());
		return MongoUtils.getIdOrUpsert(coll, q, _dbo, true);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
