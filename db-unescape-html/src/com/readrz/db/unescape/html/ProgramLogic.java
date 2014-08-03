package com.readrz.db.unescape.html;

import java.net.UnknownHostException;
import java.util.Date;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.readrz.data.Snap;
import com.readrz.data.SnapsListener;
import com.readrz.data.SnapsLoader;
import com.readrz.data.index.Idx;
import com.readrz.data.mongo.MongoColls;
import com.readrz.lang.parse.PreprocessUtils;

public final class ProgramLogic implements SnapsListener {

	private int _counterUnescaped;
	private int _counterUnindexed;
	private DBCollection _snaps;
	private DBCollection _snapsIdx;
	private DBCollection _snapsHtml;
	private DBCollection _snapsImag;
	private DBCollection _snapsThumb;
	private boolean _forceUnindex;
	
	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb,
			Date fromDate,
			boolean forceUnindex) throws UnknownHostException {
		
		System.out.println("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		
		try {
			System.out.println("Getting db: " + mongoDb);
			DB db = mongoClient.getDB(mongoDb);
			
			_snaps = db.getCollection(MongoColls.snaps);
			_snapsIdx = db.getCollection(MongoColls.snapsidx);
			_snapsHtml = db.getCollection(MongoColls.snapshtml);
			_snapsImag = db.getCollection(MongoColls.snapsimag);
			_snapsThumb = db.getCollection(MongoColls.snapsthumb);
			_forceUnindex = forceUnindex;
	
			// process all snaps
			_counterUnescaped = 0;
			_counterUnindexed = 0;
			SnapsLoader loader = new SnapsLoader(new SnapsListener[]{this});
			loader.load(db.getCollection(MongoColls.snaps), fromDate, null, null);

			System.out.println("Unescaped " + _counterUnescaped + " snaps.");
			System.out.println("Unindexed " + _counterUnindexed + " snaps.");
			System.out.println("Processed " + (_counterUnescaped + _counterUnindexed) + " snaps.");
			
		} finally {
			
			System.out.println("Closing db connection...");
			mongoClient.close();
		}
		
		System.out.println("DONE.");
	}

	@Override
	public void onSnap(Date currDate, Snap oldSnap) {
		
		String oldTitle = oldSnap.getTitle();
		String newTitle = PreprocessUtils.titleUnescape(oldTitle);
		
		String oldText = oldSnap.getText();
		String newText = PreprocessUtils.textUnescape(oldText);
		
		if (!newTitle.equals(oldTitle) ||
			!newText.equals(oldText)) {
			
//			System.out.println();
//			System.out.println("--------");
//			System.out.println("Unescape #" + _counter + ", id: " + oldSnap.getId() + ", date: " + oldSnap.getSrcDate());
//			if (!newTitle.equals(oldTitle)) {
//				System.out.println();
//				System.out.println("OLD TITLE:" + oldTitle);
//				System.out.println("NEW TITLE:" + newTitle);
//			}
//			if (!newText.equals(oldText)) {
//				System.out.println();
//				System.out.println("OLD TEXT:" + oldText);
//				System.out.println("NEW TEXT:" + newText);
//			}

			Snap newSnap = new Snap(oldSnap.getFeedId(), oldSnap.getSrcDate(), newTitle, oldSnap.getUrl(), newText);
			try {
				
				// delete unescaped version
				// (this is needed because escaped version may collide
				//  with another correctly escaped version already existing)
				oldSnap.delete(_snaps, _snapsIdx, _snapsHtml, _snapsImag, _snapsThumb, WriteConcern.ACKNOWLEDGED);
				
				// update or re-insert
				newSnap.upsertByUniqGetId(_snaps);

				// delete all external data for NEW SNAP
				newSnap.deleteExternalData(_snapsIdx, _snapsHtml, _snapsImag, _snapsThumb, WriteConcern.ACKNOWLEDGED);

			} catch (Exception ex) {
				System.out.println();
				System.out.println("ERROR: " + ex.getMessage());
				System.out.println("ERROR: Could not update snap: " + oldSnap.getId());
			}

			_counterUnescaped++;
			if (_counterUnescaped % 1000 == 0) {
				System.out.println("Unescaped " + _counterUnescaped + " snaps.");
			}
			
		} else if (_forceUnindex) {
			
			// remove forward index data
			oldSnap.setFwdHitsData(new byte[0]);
			oldSnap.isIndexed(false);
			oldSnap.updateFwdHitsUnacknowledged(_snaps);
			
			// remove all inverse index data for OLD SNAP
			Idx.removeForSnap(_snapsIdx, oldSnap.getId(), WriteConcern.ACKNOWLEDGED);

			_counterUnindexed++;
			if (_counterUnindexed % 1000 == 0) {
				System.out.println("Unindexed " + _counterUnindexed + " snaps.");
			}
		}
	}
}
