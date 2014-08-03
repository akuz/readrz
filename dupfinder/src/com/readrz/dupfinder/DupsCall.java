package com.readrz.dupfinder;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import me.akuz.core.DateUtils;

import org.bson.types.ObjectId;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.readrz.data.Feed;
import com.readrz.data.Snap;

public final class DupsCall implements Callable<Boolean> {
	
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final Map<ObjectId, ObjectId> _sourceIdByFeedId;
	private final DBCollection _feedsColl;
	private final DBCollection _snapsColl;
	private final double _dupsPeriodMins;
	private final boolean _redoAll;
	private final Date _startDate;
	private final Date _endDate;
	private final boolean _isVerbose;
	
	public DupsCall(Map<ObjectId, ObjectId> sourceIdByFeedId, DBCollection feedsColl, DBCollection snapsColl, double dupsPeriodMins, boolean redoAll, Date startDate, Date endDate, boolean isVerbose) {

		_stopped = new CountDownLatch(1);
		_sourceIdByFeedId = sourceIdByFeedId;
		_feedsColl = feedsColl;
		_snapsColl = snapsColl;
		_dupsPeriodMins = dupsPeriodMins;
		_redoAll = redoAll;
		_startDate = startDate;
		_endDate = endDate;
		_isVerbose = isVerbose;
	}

	public Boolean call() {
		
		// load from start date - minus the dups check period
		Date loadStartDate = DateUtils.addMins(_startDate, - _dupsPeriodMins);
		
		// load *all* anyway, even if dupsChecked, because we need to compare to them!
		DBCursor cursor = Snap.selectBetweenDatesAsc(_snapsColl, loadStartDate, _endDate, null);
		
		try {

			LinkedList<Snap> prevSnaps = new LinkedList<Snap>();
			LinkedList<Snap> justCheckedSnaps = new LinkedList<Snap>();
			
			while (cursor.hasNext()) {
				
				if (_stopRequested) {
					break;
				}
				
				Snap currSnap = new Snap(cursor.next());
				
				Date dupsMinDate = DateUtils.addMins(currSnap.getSrcDate(), - _dupsPeriodMins);
				
				ObjectId currFeedId = currSnap.getFeedId();
				ObjectId currSourceId = null;
				if (currFeedId != null) {

					currSourceId = _sourceIdByFeedId.get(currFeedId);
					if (currSourceId == null) {
						
						DBObject dbo = Feed.getById(_feedsColl, currFeedId);
						if (dbo != null) {
							Feed feed = new Feed(dbo);
							currSourceId = feed.getSourceId();
						}
						
						if (currSourceId != null) {
							_sourceIdByFeedId.put(currFeedId, currSourceId);
						} else {
							System.out.println("WARNING: Skipping snap " + currSnap.getId() + ", because could not find its feed's sourceId");
							continue;
						}
					}

				} else {
					System.out.println("WARNING: Skipping snap " + currSnap.getId() + ", because it does not have feedId");
					continue;
				}
				
				boolean justChecked = false;
				if (_redoAll) {
					// reset the duplicate flags
					currSnap.isDupChecked(false);
					currSnap.isDuplicate(false);
				} else {
					if (currSnap.isDupChecked() == false) {
						justChecked = true;
					}
				}

				Iterator<Snap> prevSnapsIterator;
				
				if (currSnap.isDupChecked()) {
					// check only against new checked snaps
					prevSnapsIterator = justCheckedSnaps.iterator();
				} else {
					// check against all snaps in the past
					prevSnapsIterator = prevSnaps.iterator();
				}
				
				while (prevSnapsIterator.hasNext()) {
					
					if (_stopRequested) {
						break;
					}
					
					Snap prevSnap = prevSnapsIterator.next();
					
					// check if past snap is beyond min check date now
					if (prevSnap.getSrcDate().compareTo(dupsMinDate) < 0) {
						
						// do not compare to that snap anymore
						prevSnapsIterator.remove();
						continue;
						
					} else {
						
						// do not compare to known duplicates
						if (prevSnap.isDuplicate()) {
							continue;
						}
						
						ObjectId prevFeedId = prevSnap.getFeedId();
						ObjectId prevSourceId = _sourceIdByFeedId.get(prevFeedId);
						
						if (prevSourceId == null) {
							throw new IllegalStateException("Previous sourceId is null, which should not happen");
						}
						
						// if from the same source
						if (currSourceId.equals(prevSourceId)) {
							
							final boolean isTitleMatch = currSnap.getTitleHash().equals(prevSnap.getTitleHash());
							final boolean isUrlMatch   = currSnap.getUrlHash()  .equals(prevSnap.getUrlHash());
							
							// if match title or url
							if (isTitleMatch || isUrlMatch) {

								int currPrevDateCmp = currSnap.getSrcDate().compareTo(prevSnap.getSrcDate());
								
								if (currPrevDateCmp == 0) {
									
									// use the one, which was *downloaded* last
									if (currSnap.getId().compareTo(prevSnap.getId()) < 0) {
										
										if (_isVerbose) {
											System.out.println("dup: " + currSnap.getSrcDate() + " " + currSnap.getId() + " " + currSnap.getTitle());
											System.out.println("new: " + prevSnap.getSrcDate() + " " + prevSnap.getId() + " " + prevSnap.getTitle());
										}
										
										// this is duplicate, don't check more
										currSnap.isDuplicate(true);
										break;
										
									} else {
										
										if (_isVerbose) {
											System.out.println("dup: " + prevSnap.getSrcDate() + " " + prevSnap.getId() + " " + prevSnap.getTitle());
											System.out.println("new: " + currSnap.getSrcDate() + " " + currSnap.getId() + " " + currSnap.getTitle());
										}

										// mark prev as duplicate, keep checking
										prevSnap.isDuplicate(true);
										prevSnap.updateDupInfoUnacknowledged(_snapsColl);
										continue;
									}
									
								} else if (currPrevDateCmp < 0) {
									
									if (_isVerbose) {
										System.out.println("dup: " + currSnap.getSrcDate() + " " + currSnap.getId() + " " + currSnap.getTitle());
										System.out.println("new: " + prevSnap.getSrcDate() + " " + prevSnap.getId() + " " + prevSnap.getTitle());
									}

									// this is duplicate, don't check more
									currSnap.isDuplicate(true);
									break;
									
								} else {

									if (_isVerbose) {
										System.out.println("dup: " + prevSnap.getSrcDate() + " " + prevSnap.getId() + " " + prevSnap.getTitle());
										System.out.println("new: " + currSnap.getSrcDate() + " " + currSnap.getId() + " " + currSnap.getTitle());
									}
									
									// mark prev as duplicate, keep checking
									prevSnap.isDuplicate(true);
									prevSnap.updateDupInfoUnacknowledged(_snapsColl);
									continue;
								}
							}
						}
					}
				}
				
				// isDuplicate was set above
				currSnap.isDupChecked(true);
				currSnap.updateDupInfoUnacknowledged(_snapsColl);
				
				if (justChecked) {
					justCheckedSnaps.addLast(currSnap);
				}
				
				prevSnaps.addLast(currSnap);
			}
			
		} finally {
			
			_stopped.countDown();
			cursor.close();
		}
		
		return true;
	}
	
	public boolean isStopped() {
		return _stopped.getCount() == 0;
	}
	
	public final void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			System.out.println("WARNING: Interrupted while waiting for dups scan call to stop");
		}
	}	

}
