package com.readrz.feedsget;

import java.util.Date;

import me.akuz.core.CompareUtils;
import me.akuz.core.SortOrder;

import com.readrz.data.Feed;

public final class FeedGet implements Comparable<FeedGet> {

	private final Feed _feed;
	private Date _lastScanDate;
	private Date _nextScanDate;
	
	public FeedGet(Feed feed) {
		_feed = feed;
	}
	
	public Feed getFeed() {
		return _feed;
	}
	
	public Date getLastScanDate() {
		return _lastScanDate;
	}
	public void setLastScanDate(Date date) {
		_lastScanDate = date;
	}
	
	public Date getNextScanDate() {
		return _nextScanDate;
	}
	public void setNextScanDate(Date date) {
		_nextScanDate = date;
	}
	
	public String toString() {
		return _feed.getUrl() 
			+ ", last scan date: " 
			+ _lastScanDate
			+ ", next scan date: " 
			+ _nextScanDate;
	}

	@Override
	public int compareTo(FeedGet o) {
		return CompareUtils.compareNullsLowest(this._nextScanDate, o._nextScanDate, SortOrder.Asc);
	}

}
