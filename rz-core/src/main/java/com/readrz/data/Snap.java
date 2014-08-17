package com.readrz.data;

import java.util.Date;

import me.akuz.core.Hit;
import me.akuz.core.crypt.MurmurHash3;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.readrz.data.index.Idx;
import com.readrz.lang.parse.PreprocessUtils;
import com.readrz.utils.db.MongoUtils;

/**
 * Snap (at a particular moment) of a text document from a Feed.
 *
 */
public final class Snap {
	
	// keeping names short, as the number of these
	// objects will grow proportionally with time...
	public static final String _idField         = MongoUtils._id;
	public static final String _feedIdField     = "f";
	public static final String _srcDateField    = "d";
	public static final String _titleField      = "t";
	public static final String _titleHashField  = "th";
	public static final String _urlField        = "u";
	public static final String _urlHashField    = "uh";
	public static final String _actualUrlField  = "ua";
	public static final String _textField       = "x";
	public static final String _isDupChecked    = "dpc";
	public static final String _isDuplicate     = "dp";
	public static final String _isScanned       = "is";
	public static final String _isScanFailed    = "isf";
	public static final String _isScannedImage  = "isi";
	public static final String _scannedImageUrl = "isu";
	public static final String _isIndexed       = "ii";
	public static final String _fwdHitsData     = "fh";
	public static final BasicDBObject idxSrcDateDesc;
	
	static {
		idxSrcDateDesc = new BasicDBObject();
		idxSrcDateDesc.put(_srcDateField, -1);
	}

	private final DBObject _dbo;
	private boolean _isPreprocessed;
	private Hit _preprocessedTitleBounds;
	private Hit _preprocessedTextBounds;

	public Snap(ObjectId feedId, Date srcDate, String title, String url, String text) {
		_dbo = new BasicDBObject();
		_dbo.put(_feedIdField, feedId);
		_dbo.put(_srcDateField, srcDate);
		_dbo.put(_titleField, title);
		_dbo.put(_titleHashField, MurmurHash3.murmurhash3_x86_32(title));
		_dbo.put(_urlField, url);
		_dbo.put(_urlHashField, MurmurHash3.murmurhash3_x86_32(url));
		_dbo.put(_textField, text);
		_dbo.put(_isDupChecked, false);
		_dbo.put(_isDuplicate, false);
		_dbo.put(_isIndexed, false);
		_dbo.put(_fwdHitsData, null);
	}
	
	public Snap(DBObject dbo) {
		_dbo = dbo;
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public ObjectId getId() {
		return (ObjectId)_dbo.get(_idField);
	}
	
	public boolean isScanned() {
		Boolean is = (Boolean)_dbo.get(_isScanned);
		return is == null ? false : is.booleanValue();
	}
	public void isScanned(boolean is) {
		_dbo.put(_isScanned, is);
	}
	
	public boolean isScanFailed() {
		Boolean is = (Boolean)_dbo.get(_isScanFailed);
		return is == null ? false : is.booleanValue();
	}
	public void isScanFailed(boolean is) {
		_dbo.put(_isScanFailed, is);
	}
	
	public boolean isScannedImage() {
		Boolean is = (Boolean)_dbo.get(_isScannedImage);
		return is == null ? false : is.booleanValue();
	}
	public void isScannedImage(boolean is) {
		_dbo.put(_isScannedImage, is);
	}
	
	public String getScannedImageUrl() {
		return (String)_dbo.get(_scannedImageUrl);
	}
	public void setScannedImageUrl(String url) {
		_dbo.put(_scannedImageUrl, url);
	}
	
	public boolean isDupChecked() {
		Boolean is = (Boolean)_dbo.get(_isDupChecked);
		return is == null ? false : is.booleanValue();
	}
	public void isDupChecked(boolean is) {
		_dbo.put(_isDupChecked, is);
	}
	
	public boolean isDuplicate() {
		Boolean is = (Boolean)_dbo.get(_isDuplicate);
		return is == null ? false : is.booleanValue();
	}
	public void isDuplicate(boolean is) {
		_dbo.put(_isDuplicate, is);
	}
	
	public boolean isIndexed() {
		Boolean is = (Boolean)_dbo.get(_isIndexed);
		return is == null ? false : is.booleanValue();
	}
	public void isIndexed(boolean is) {
		_dbo.put(_isIndexed, is);
	}
	
	public byte[] getFwdHitsData() {
		Object o = _dbo.get(_fwdHitsData);
		if (o instanceof Binary) {
			return ((Binary)o).getData();
		} else {
			return (byte[])o;
		}
	}
	public void setFwdHitsData(byte[] data) {
		_dbo.put(_fwdHitsData, new Binary(data));
	}
	
	public ObjectId getFeedId() {
		return (ObjectId)_dbo.get(_feedIdField);
	}
	
	public Date getSrcDate() {
		return (Date)_dbo.get(_srcDateField);
	}
	
	public String getTitle() {
		return (String)_dbo.get(_titleField);
	}
	
	public Integer getTitleHash() {
		return (Integer)_dbo.get(_titleHashField);
	}
	
	public String getUrl() {
		return (String)_dbo.get(_urlField);
	}
	
	public Integer getUrlHash() {
		return (Integer)_dbo.get(_urlHashField);
	}

	public String getActualUrl() {
		return (String)_dbo.get(_actualUrlField);
	}
	public void setActualUrl(String actualUrl) {
		_dbo.put(_actualUrlField, actualUrl);
	}

	public String getText() {
		return (String)_dbo.get(_textField);
	}
	
	public static void ensureIndices(DBCollection coll) {
		
		coll.getDB().command(
				new BasicDBObject()
					.append("collMod", coll.getName())
					.append("usePowerOf2Sizes", true));

		// put date as first for optimization
		// of inserting operations when
		// downloading new items
		coll.createIndex(
				new BasicDBObject()
					.append(_srcDateField, 1)
					.append(_feedIdField, 1)
					.append(_titleHashField, 1)
					.append(_urlHashField, 1),
				new BasicDBObject()
					.append("name", "idxUniq")
					.append("unique", true));
		
		coll.createIndex(
				idxSrcDateDesc,
				new BasicDBObject()
					.append("name", "idxSrcDateDesc"));
	}
	
	public boolean ensureByUniqDontGetId(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_srcDateField, getSrcDate());
		q.put(_feedIdField, getFeedId());
		q.put(_titleHashField, getTitleHash());
		q.put(_urlHashField, getUrlHash());
		
		return MongoUtils.getIdOrUpsert(coll, q, _dbo, false);
	}
	
	public boolean upsertByUniqGetId(DBCollection coll) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_srcDateField, getSrcDate());
		q.put(_feedIdField, getFeedId());
		q.put(_titleHashField, getTitleHash());
		q.put(_urlHashField, getUrlHash());
		
		return MongoUtils.upsert(coll, q, _dbo, true);
	}
	
	public void delete(
			DBCollection snaps,
			DBCollection snapsIdx, 
			DBCollection snapsHtml, 
			DBCollection snapsImag, 
			DBCollection snapsThumb, 
			WriteConcern writeConcern) {

		// delete external data
		deleteExternalData(snapsIdx, snapsHtml, snapsImag, snapsThumb, writeConcern);
		
		// delete the snap itself
		BasicDBObject q = new BasicDBObject()
			.append(_idField, getId());
		snaps.remove(q, writeConcern);
	}

	public void deleteExternalData(
			DBCollection snapsIdx, 
			DBCollection snapsHtml, 
			DBCollection snapsImag, 
			DBCollection snapsThumb, 
			WriteConcern writeConcern) {
		
		// delete all inverse index data
		Idx.removeForSnap(snapsIdx, getId(), writeConcern);

		// remove html
		SnapHtml.deleteForSnap(snapsHtml, getId(), writeConcern);

		// remove images
		SnapImag.deleteForSnap(snapsImag, getId(), writeConcern);
		SnapThumb.deleteForSnap(snapsThumb, getId(), writeConcern);
	}

	public Hit getPreprocessedTitleBounds() {
		ensurePreprocessed();
		return _preprocessedTitleBounds;
	}
	
	public Hit getPreprocessedTextBounds() {
		ensurePreprocessed();
		return _preprocessedTextBounds;
	}
	
	public void ensurePreprocessed() {
		if (_isPreprocessed) {
			return;
		}
		_preprocessedTitleBounds = PreprocessUtils.titleFindBounds(getTitle());
		_preprocessedTextBounds  = PreprocessUtils.textFindBounds(getText());
		_isPreprocessed = true;
	}

	public static Snap findById(
			DBCollection coll, 
			ObjectId id) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, id);
		
		DBObject dbo = coll.findOne(q);
		return dbo == null ? null : new Snap(dbo);
	}

	public static Snap findById(
			DBCollection coll, 
			ObjectId id,
			DBObject fields) {
		
		BasicDBObject q = new BasicDBObject();
		q.put(_idField, id);
		
		DBObject dbo = coll.findOne(q, fields);
		return dbo == null ? null : new Snap(dbo);
	}

	public static DBCursor selectBetweenDatesAsc(
			DBCollection coll, 
			Date minDateInc, 
			Date maxDateExc,
			BasicDBList extraConditions) {
		
		BasicDBObject q = null;
		
		BasicDBList conditions = new BasicDBList();
		if (minDateInc != null) {
			conditions.add(
				new BasicDBObject().append(_srcDateField,
					new BasicDBObject().append("$gte", minDateInc)));
		}
		if (maxDateExc != null) {
			conditions.add(
				new BasicDBObject().append(_srcDateField,
					new BasicDBObject().append("$lt", maxDateExc)));
		}
		if (extraConditions != null && extraConditions.size() > 0) {
			for (int i=0; i<extraConditions.size(); i++) {
				conditions.add(extraConditions.get(i));
			}
		}
		if (conditions.size() > 0) {
			q = new BasicDBObject();
			q.put("$and", conditions);
		}

		BasicDBObject s = new BasicDBObject();
		s.put(_srcDateField, 1);
		
		return coll.find(q).sort(s);
	}

	public void updateScanInfoUnacknowledged(DBCollection coll) {

		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());

		BasicDBObject u = 
			new BasicDBObject().append(
			"$set", new BasicDBObject()
					.append(_isScanned, isScanned())
					.append(_isScanFailed, isScanFailed())
					.append(_isScannedImage, isScannedImage())
					.append(_scannedImageUrl, getScannedImageUrl()));
		
		MongoUtils.updateOne(coll, q, u, WriteConcern.UNACKNOWLEDGED);
	}

	public void updateDupInfoUnacknowledged(DBCollection coll) {

		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());

		BasicDBObject u = 
			new BasicDBObject().append(
			"$set", new BasicDBObject()
					.append(_isDupChecked, isDupChecked())
					.append(_isDuplicate, isDuplicate()));
		
		MongoUtils.updateOne(coll, q, u, WriteConcern.UNACKNOWLEDGED);
	}

	public void updateFwdHitsUnacknowledged(DBCollection coll) {

		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());

		BasicDBObject u = 
			new BasicDBObject().append(
			"$set", new BasicDBObject()
					.append(_isIndexed, _dbo.get(_isIndexed))
					.append(_fwdHitsData, _dbo.get(_fwdHitsData)));
		
		MongoUtils.updateOne(coll, q, u, WriteConcern.UNACKNOWLEDGED);
	}

	public void updateActualUrlUnacknowledged(DBCollection coll) {

		BasicDBObject q = new BasicDBObject();
		q.put(_idField, getId());

		BasicDBObject u = 
			new BasicDBObject().append(
			"$set", new BasicDBObject()
					.append(_actualUrlField, _dbo.get(_actualUrlField)));
		
		MongoUtils.updateOne(coll, q, u, WriteConcern.UNACKNOWLEDGED);
	}

	@Override
	public String toString() {
		return _dbo.toString();
	}

	public String extractHitStr(Hit sentenceHit, Hit hit) {
		
		// select field string based on sentence hit (negative start for title)
		String str = sentenceHit.start() < 0 ? getTitle() : getText();
		
		// the hit bounds are correct within this field string, so no need to adjust
		return str.substring(hit.start(), hit.end());
	}
}
