package com.readrz.zzz.paths;

import com.readrz.zzz.Phrase;

public final class PathQuote {
	
	private final double _rank;
	private final Phrase _phrase;
	
	public PathQuote(Phrase phrase, double rank) {
		_phrase = phrase;
		_rank = rank;
	}
	
	public Phrase getPhrase() {
		return _phrase;
	}
	
	public double getRank() {
		return _rank;
	}

}
