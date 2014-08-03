package com.readrz.zzz;

import com.readrz.zzz.parse.matches.AnyMatch;

public final class PhrasePlace {
	
	private final AnyMatch _match;
	private final double _weight;
	
	public PhrasePlace(AnyMatch match, double weight) {
		_match = match;
		_weight = weight;
	}
	
	public AnyMatch getMatch() {
		return _match;
	}
	
	public double getWeight() {
		return _weight;
	}
}
