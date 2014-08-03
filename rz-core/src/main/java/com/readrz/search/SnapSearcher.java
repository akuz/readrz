package com.readrz.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.Triple;

import org.bson.types.ObjectId;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.data.Feed;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.Source;

public final class SnapSearcher {
	
	private final DBCollection _snapsColl;
	private final DBCollection _snapsidxColl;
	private final DBCollection _feedsColl;
	private final DBCollection _sourcesColl;
	
	public SnapSearcher(
		DBCollection snapscoll,
		DBCollection snapsidxColl,
		DBCollection feedsColl,
		DBCollection sourcesColl) {
		
		_snapsColl = snapscoll;
		_snapsidxColl = snapsidxColl;
		_feedsColl = feedsColl;
		_sourcesColl = sourcesColl;
	}
	
	public SnapSearch startSearch(
			boolean writeLog,
			QueryKeyIds queryKeyIds,
			Date minDateInc, 
			Date maxDateExc,
			Integer cursorsLimit) {

		SnapSearch search = new SnapSearch(
				writeLog, 
				queryKeyIds,
				minDateInc,
				maxDateExc,
				cursorsLimit,
				_snapsColl, 
				_snapsidxColl);
		
		return search;
	}
	
	public List<SnapSearchResult> findWithLimit(
			int limit,
			boolean writeLog,
			QueryKeyIds queryKeyIds,
			Date minDateInc, 
			Date maxDateExc,
			Integer cursorsLimit) {

		List<SnapSearchResult> results = new ArrayList<SnapSearchResult>();

		SnapSearch search = startSearch(
				writeLog,
				queryKeyIds,
				minDateInc, 
				maxDateExc, 
				cursorsLimit);

		try {
			
			while (results.size() < limit) {
				
				SnapSearchResult searchResult = search.findNext();
				if (searchResult == null) {
					break;
				}

				results.add(searchResult);
			}
			
		} finally {
			search.close();
		}
		
		return results;
	}
	
	public SnapInfo findSnapInfo(ObjectId snapId) {
		Snap snap = findSnap(snapId);
		if (snap != null) {
			Feed feed = findFeed(snap.getFeedId());
			if (feed != null) {
				Source source = findSource(feed.getSourceId());
				if (source != null) {
					return new SnapInfo(snap, feed, source);
				}
			}
		}
		return null;
	}
	
	public Snap findSnap(ObjectId id) {
		DBObject dbo = _snapsColl.findOne(id);
		return (dbo != null) ? new Snap(dbo) : null;
	}
	
	public Feed findFeed(ObjectId id) {
		DBObject dbo = _feedsColl.findOne(id);
		return (dbo != null) ? new Feed(dbo) : null;
	}
	
	public Source findSource(ObjectId id) {
		DBObject dbo = _sourcesColl.findOne(id);
		return (dbo != null) ? new Source(dbo) : null;
	}
	
	public Triple<Snap, Feed, Source> findSnapFeedSource(ObjectId snapId) {
		Snap snap = findSnap(snapId);
		if (snap != null) {
			Feed feed = findFeed(snap.getFeedId());
			if (feed != null) {
				Source source = findSource(feed.getSourceId());
				if (source != null) {
					return new Triple<Snap, Feed, Source>(snap, feed, source);
				}
			}
		}
		return null;
	}

}
