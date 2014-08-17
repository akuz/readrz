package com.readrz.data;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;

import me.akuz.core.ZlibUtils;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.readrz.utils.db.MongoUtils;

public final class SnapHtml {
	
	private final DBObject _dbo;
	
	// keeping names short, as the number of these
	// objects will grow proportionally with time...
	public static final String _idField     = MongoUtils._id;
	public static final String _bytesField  = "b";
	private static final Charset _encoding  = Charset.forName("UTF-8");

	public SnapHtml(DBObject dbo) {
		_dbo = dbo;
	}
	
	public SnapHtml(Object snapId, String html) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, snapId);
		
		byte[] bytes;
		try {
			bytes = ZlibUtils.deflate(9, html, _encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported encoding: " + _encoding, e);
		}
		_dbo.put(_bytesField, new Binary(bytes));
	}
	
	public ObjectId getId() {
		return (ObjectId)_dbo.get(MongoUtils._id);
	}
	
	public String getHtml() {
		
		Object o = _dbo.get(_bytesField);
		if (o == null) {
			return null;
		}

		byte[] bytes;
		if (o instanceof Binary) {
			bytes = ((Binary)o).getData();
		} else {
			bytes = (byte[])o;
		}
		
		String str;
		try {
			str = ZlibUtils.inflate(bytes, _encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not unzip html", e);
		} catch (DataFormatException e) {
			throw new IllegalStateException("Could not unzip html", e);
		}
		return str;
	}
	
	public static final void deleteForSnap(DBCollection coll, ObjectId snapId, WriteConcern writeConcern) {
		
		BasicDBObject q = new BasicDBObject()
			.append(_idField, snapId);
		
		coll.remove(q, writeConcern);
	}
	
	public static void ensureIndices(DBCollection coll) {
		
		coll.getDB().command(
				new BasicDBObject()
					.append("collMod", coll.getName())
					.append("usePowerOf2Sizes", true));
		
	}

	public void upsertUnacknowledged(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject()
			.append(_idField, getId());
		
		MongoUtils.upsertUnacknowledged(coll, q, _dbo);
	}

	public static SnapHtml find(DBCollection coll, ObjectId id) {
		
		BasicDBObject q = new BasicDBObject()
			.append(_idField, id);
		
		DBObject dbo = coll.findOne(q);
		return dbo != null ? new SnapHtml(dbo) : null;
	}
}
