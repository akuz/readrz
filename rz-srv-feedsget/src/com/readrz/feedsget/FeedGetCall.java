package com.readrz.feedsget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import me.akuz.core.DateUtils;
import me.akuz.core.Pair;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.readrz.data.Snap;
import com.readrz.data.mongo.MongoColls;

/**
 * A call for scanning the feed, which can be executed on a thread pool.
 *
 */
public final class FeedGetCall implements Callable<Boolean> {

	private final static int freqCountPeriodMins;
	private final static List<Pair<Integer, Integer>> freqCountCheckMins;
	private final static int freqMaxCheckMins;
	
	static {
		freqCountPeriodMins = 60;
		freqCountCheckMins = new ArrayList<>();
		freqCountCheckMins.add(new Pair<Integer, Integer>(10,  1));
		freqCountCheckMins.add(new Pair<Integer, Integer>( 5,  2));
		freqCountCheckMins.add(new Pair<Integer, Integer>( 2,  3));
		freqCountCheckMins.add(new Pair<Integer, Integer>( 1,  5));
		freqCountCheckMins.add(new Pair<Integer, Integer>( 0, 10));
		freqMaxCheckMins = 10;
	}
	
	private final DB _db;
	private final DBCollection _snapsColl; 
	private final FeedGet _feedGet;
	private final ProgramOptions _options;
	private boolean _isComplete;
	private Exception _exception;
	private Boolean _isAllPostsKnown;
	private volatile boolean _stopRequested;
	
	public FeedGetCall(
			DB db,
			FeedGet feedGet,
			ProgramOptions options) {
		
		_db = db;
		_snapsColl = _db.getCollection(MongoColls.snaps);
		
		_feedGet = feedGet;
		_options = options;
	}
	
	public FeedGet getFeedGet() {
		return _feedGet;
	}
	
	public ProgramOptions getOptions() {
		return _options;
	}
	
	public void stopAsync() {
		_stopRequested = true;
	}
	
	@Override
	public Boolean call() {

		try {
			while (!_stopRequested) {
				
				// make remote call
				GetterOutput getterOutput = Getter.getOutput(_feedGet);
	
				if (_stopRequested) {
					break;
				}
	
				Date nowDate = new Date();
				Date minDate = DateUtils.addMinutes(nowDate, -freqCountPeriodMins);
				int freqCheckSnapCount = 0;
				
				// save snaps we got
				List<Snap> snaps = getterOutput.getSnaps();
				_isAllPostsKnown = true;
				for (int i=0; i<snaps.size(); i++) {
					
					if (_stopRequested) {
						break;
					}
					
					// get all downloaded posts
					Snap snap = snaps.get(i);
					
					if (snap.getSrcDate().after(minDate) &&
						snap.getSrcDate().before(nowDate)) {
						freqCheckSnapCount += 1;
					}
					
					// save and check if post existed
					if (snap.ensureByUniqDontGetId(_snapsColl) == false) {
						
						// got new post here
						_isAllPostsKnown = false;
					}
				}
				
				if (_stopRequested) {
					break;
				}
	
				// set last scan date
				_feedGet.setLastScanDate(nowDate);
	
				// set next scan date
				double nextCheckMins = freqMaxCheckMins;
				for (int i=0; i<freqCountCheckMins.size(); i++) {
					Pair<Integer, Integer> pair = freqCountCheckMins.get(i);
					if (freqCheckSnapCount >= pair.v1()) {
						if (nextCheckMins > pair.v2()) {
							nextCheckMins = pair.v2();
						}
					}
				}
				Date nextDate = DateUtils.addMinutes(nowDate, nextCheckMins);
				_feedGet.setNextScanDate(nextDate);
				
				break;
			}
			
		} catch (Exception ex) {
			_exception = ex;
		}
		
		_isComplete = true;
		return _exception != null;
	}

	public boolean isComplete() {
		return _isComplete;
	}
	
	public boolean hasException() {
		return _exception != null;
	}
	
	public Exception getException() {
		return _exception;
	}
	
	public Boolean isAllPostsKnown() {
		return _isAllPostsKnown;
	}
	
	@Override
	public String toString() {
		return _feedGet.toString();
	}
}
