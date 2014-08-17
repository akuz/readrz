package com.readrz.data;

public final class SnapInfo {
	
	private final Snap _snap;
	private final Feed _feed;
	private final Source _source;
	
	public SnapInfo(Snap snap, Feed feed, Source source) {
		_snap = snap;
		_feed = feed;
		_source = source;
	}
	
	public Snap getSnap() {
		return _snap;
	}
	
	public Feed getFeed() {
		return _feed;
	}
	
	public Source getSource() {
		return _source;
	}

}
