package com.readrz.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.readrz.utils.db.MongoUtils;

public final class SnapImag {
	
	private final DBObject _dbo;
	
	// keeping names short, as the number of these
	// objects will grow proportionally with time...
	public static final String _idField     = MongoUtils._id;
	public static final String _urlField    = "u";
	public static final String _widthField  = "w";
	public static final String _heightField = "h";
	public static final String _bytesField  = "b";
	
	public SnapImag(DBObject dbo) {
		_dbo = dbo;
	}
	
	public SnapImag(Object snapId, String url, BufferedImage imag) {
		_dbo = new BasicDBObject();
		_dbo.put(_idField, snapId);
		_dbo.put(_urlField, url);
		_dbo.put(_widthField, imag.getWidth());
		_dbo.put(_heightField, imag.getHeight());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(imag, "jpg", baos);
			baos.flush();
		} catch (IOException e) {
			throw new IllegalStateException("Could not serialize image", e);
		}
		byte[] bytes = baos.toByteArray();
		_dbo.put(_bytesField, new Binary(bytes));
	}
	
	public ObjectId getId() {
		return (ObjectId)_dbo.get(MongoUtils._id);
	}
	
	public String getUrl() {
		return (String)_dbo.get(_urlField);
	}
	
	public Integer getWidth() {
		return (Integer)_dbo.get(_widthField);
	}
	
	public Integer getHeight() {
		return (Integer)_dbo.get(_heightField);
	}
	
	public byte[] getImageBytes() {
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
		return bytes;
	}

	public BufferedImage getImage() {

		byte[] bytes = getImageBytes();
		if (bytes == null) {
			return null;
		}

		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage imag;
		try {
			imag = ImageIO.read(in);
		} catch (IOException e) {
			throw new IllegalStateException("Could not deserialize image", e);
		}
		return imag;
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

	public static SnapImag find(DBCollection coll, ObjectId id) {
		
		BasicDBObject q = new BasicDBObject()
			.append(_idField, id);
		
		DBObject dbo = coll.findOne(q);
		return dbo != null ? new SnapImag(dbo) : null;
	}
}
