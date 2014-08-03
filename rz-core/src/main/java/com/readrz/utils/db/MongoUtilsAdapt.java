package com.readrz.utils.db;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.readrz.data.mongo.MongoObject;

public final class MongoUtilsAdapt {

	public static final BasicDBList toDBList(Collection<? extends MongoObject> coll) {
		BasicDBList dbList = null;
		if (coll != null) {
			dbList = new BasicDBList();
			for (MongoObject obj : coll) {
				dbList.add(obj.getDbo());
			}
		}
		return dbList;
	}

	public static final BasicDBObject toDBMap(Map<String, ? extends MongoObject> map) {
		BasicDBObject dbo = null;
		if (map != null) {
			dbo = new BasicDBObject();
			for (Entry<String,? extends MongoObject> entry : map.entrySet()) {
				dbo.put(entry.getKey(), entry.getValue().getDbo());
			}
		}
		return dbo;
	}
	
}
