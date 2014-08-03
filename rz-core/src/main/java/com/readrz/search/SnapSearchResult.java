package com.readrz.search;

import java.util.Set;

import org.bson.types.ObjectId;

public final class SnapSearchResult {
	
	private final ObjectId _snapId;
	private final Set<Integer> _sentenceHitStarts;
	
	public SnapSearchResult(ObjectId snapId, Set<Integer> sentenceStarts) {
		_snapId = snapId;
		_sentenceHitStarts = sentenceStarts;
	}
	
	public ObjectId getSnapId() {
		return _snapId;
	}
	
	/**
	 * (Null means all sentences.)
	 */
	public Set<Integer> getSentenceHitStarts() {
		return _sentenceHitStarts;
	}
}
