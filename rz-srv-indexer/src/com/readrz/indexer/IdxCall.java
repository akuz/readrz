package com.readrz.indexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import me.akuz.core.Hit;
import me.akuz.core.logs.LogUtils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteConcern;
import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsBuilder;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.HitsBuilder;
import com.readrz.data.index.Idx;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.parse.SnapsParser;
import com.readrz.math.topicdetect.TopicsDetector;

public final class IdxCall implements Callable<Boolean> {
	
	private final Logger _log;
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final SnapsParser _snapsParser;
	private final TopicsDetector _topicsDetector;
	
	private final DBCollection _snapsColl;
	private final DBCollection _snapsidxColl;
	private final boolean _redoAll;
	private final Date _startDate;
	private final Date _endDate;
	
	public IdxCall(
			SnapsParser snapsParser,
			TopicsDetector topicsDetector,
			DBCollection snapsColl, 
			DBCollection snapsidxColl,
			boolean redoAll, 
			Date startDate, 
			Date endDate) {

		_log = LogUtils.getLogger(this.getClass().getName());
		_stopped = new CountDownLatch(1);

		_snapsParser = snapsParser;
		_topicsDetector = topicsDetector;
		
		_snapsColl = snapsColl;
		_snapsidxColl = snapsidxColl;
		_redoAll = redoAll;
		_startDate = startDate;
		_endDate = endDate;
	}

	@Override
	public Boolean call() {
		
		BasicDBList extraConditions = null;
		
		if (_redoAll == false) {
			extraConditions = new BasicDBList();
			extraConditions.add(
				new BasicDBObject()
					.append(Snap._isIndexed, false));
		}
		
		DBCursor cursor = Snap.selectBetweenDatesAsc(_snapsColl, _startDate, _endDate, extraConditions);
		
		try {

			int counter = 0;
			long ms = System.currentTimeMillis();
			while (cursor.hasNext()) {
				
				if (_stopRequested) {
					break;
				}
				
				// next snap
				Snap snap = new Snap(cursor.next());
				
				_log.fine("Indexing snap: " + snap.getId() + " at: " + snap.getSrcDate());
				
				// parse snap
				FwdHits fwdHits = _snapsParser.parse(snap);
				
				// detect topics
				if (_topicsDetector != null) {
					
					CorpusDoc doc = _topicsDetector.step0_createCorpusDoc(snap, fwdHits);
					_topicsDetector.step1_calcTopicProbs(doc);
					_topicsDetector.step2_confirmTopics(doc);
					fwdHits = _topicsDetector.step3_updateFwdHits(doc, fwdHits);
				}

				// build binary hits datas
				FwdHitsBuilder fwdHitsBuilder = new FwdHitsBuilder();
				Map<Integer, HitsBuilder> hitBuildersByKeyId = new HashMap<>();
				fwdHits.reset();
				while (fwdHits.nextSentence()) {
					
					Hit sentenceHit = fwdHits.getSentenceHit();
					FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(FwdHitKind.ALL);
					
					// populate inverse hits (all, including groups)
					populateHitBuilders(hitBuildersByKeyId, sentenceHit, fwdHitsMap);
					
					// save forward hits, if any
					if (fwdHitsMap.size() > 0) {
						fwdHitsBuilder.addSentenceHits(sentenceHit, fwdHitsMap);
					}
				}

				// get properties for saving
				ObjectId snapId = snap.getId();
				Date srcDate = snap.getSrcDate();
				
				// save inverse hits
				Idx.removeForSnap(_snapsidxColl, snapId, WriteConcern.ACKNOWLEDGED);
				for (Integer keyId : hitBuildersByKeyId.keySet()) {

					// serialize hits data
					HitsBuilder hb = hitBuildersByKeyId.get(keyId);
					byte[] hitsData = hb.getData();
					
					// create index entry and save it to the database
					Idx idx = new Idx(keyId, srcDate, snapId, hitsData);
					idx.upsertUnacknowledged(_snapsidxColl);
				}
				
				// convert forward hits
				byte[] fwdHitsData = fwdHitsBuilder.getData();
				
				snap.isIndexed(true);
				snap.setFwdHitsData(fwdHitsData);
				snap.updateFwdHitsUnacknowledged(_snapsColl);
				counter++;
			}
			
			long dur = System.currentTimeMillis() - ms;
			_log.fine("Indexed " + counter + " snaps in " + dur + " ms.");
			
		} finally {
			
			_stopped.countDown();
			cursor.close();
		}
		
		return true;
	}
	
	private static final void populateHitBuilders(
			Map<Integer, HitsBuilder> hitBuildersByKeyId,
			Hit sentenceHit,
			FwdHitsMap fwdHitsMap) {
		
		for (Entry<FwdHitKind, List<FwdHit>> entry : fwdHitsMap.entrySet()) {
		
			List<FwdHit> fwdHits = entry.getValue();
			Map<Integer, List<Hit>> hitsByKeyId = new HashMap<>();
			
			for (int i=0; i<fwdHits.size(); i++) {
				FwdHit fwdHit = fwdHits.get(i);
				List<Hit> hits = hitsByKeyId.get(fwdHit.getKeyId());
				if (hits == null) {
					hits = new ArrayList<>();
					hitsByKeyId.put(fwdHit.getKeyId(), hits);
				}
				hits.add(fwdHit.getHit());
			}
			
			for (Entry<Integer, List<Hit>> entry2 : hitsByKeyId.entrySet()) {
				
				HitsBuilder hitsBuilder = hitBuildersByKeyId.get(entry2.getKey());
				if (hitsBuilder == null) {
					hitsBuilder = new HitsBuilder();
					hitBuildersByKeyId.put(entry2.getKey(), hitsBuilder);
				}
				try {
					hitsBuilder.addSentenceHits(sentenceHit, entry2.getValue());
				} catch (Exception ex) {
					throw new IllegalStateException("Could not add hits for key id: " + entry2.getKey(), ex);
				}
			}
		}
	}

	public boolean isStopped() {
		return _stopped.getCount() == 0;
	}
	
	public final void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			_log.warning("Interrupted while waiting for idx scan call to stop");
		}
	}	

}
