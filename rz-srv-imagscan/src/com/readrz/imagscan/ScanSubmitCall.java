package com.readrz.imagscan;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.readrz.data.Snap;

public final class ScanSubmitCall implements Callable<Boolean> {

	private static final Logger _log = LogUtils.getLogger(ScanSubmitCall.class.getName());

	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final ScanEngine _engine;
	private final DBCollection _snapsColl;
	private final boolean _redoAll;
	private final Date _startDate;
	private final Date _endDate;
	
	public ScanSubmitCall(
			ScanEngine engine, 
			DBCollection snapsColl, 
			boolean redoAll, 
			Date startDate, 
			Date endDate) {

		_stopped = new CountDownLatch(1);
		_engine = engine;
		_snapsColl = snapsColl;
		_redoAll = redoAll;
		_startDate = startDate;
		_endDate = endDate;
	}

	public Boolean call() {
		
		// don't load scanned snaps,
		// if not redoing scan for all
		BasicDBList extraConditions = null;
		if (_redoAll) {
			BasicDBObject condition = new BasicDBObject()
				.append(Snap._isDupChecked, true)
				.append(Snap._isDuplicate, false);
			extraConditions = new BasicDBList();
			extraConditions.add(condition);
		} else {
			BasicDBObject condition = new BasicDBObject()
				.append(Snap._isScanned, new BasicDBObject().append("$ne", true))
				.append(Snap._isDupChecked, true)
				.append(Snap._isDuplicate, false);
			extraConditions = new BasicDBList();
			extraConditions.add(condition);
		}
		
		// open cursors through snaps to scan
		DBCursor cursor = Snap.selectBetweenDatesAsc(_snapsColl, _startDate, _endDate, extraConditions);
		
		try {

			while (cursor.hasNext()) {
				
				if (_stopRequested) {
					break;
				}
			
				// get cursor snap
				Snap snap = new Snap(cursor.next());
				
				// create call to submit
				ScanSnapCall scanSnapCall = new ScanSnapCall(_engine, snap, 1, null);
				
				// submit the call
				_engine.submit(scanSnapCall);
			}
			
		} finally {
			
			cursor.close();

			_stopped.countDown();
		}
		
		return true;
	}
	
	public boolean isStopped() {
		return _stopped.getCount() == 0;
	}
	
	public void stopAsync() {
		_stopRequested = true;
	}
	
	public final void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			_log.warning("Interrupted while waiting for submit call to stop");
		}
	}	

}
