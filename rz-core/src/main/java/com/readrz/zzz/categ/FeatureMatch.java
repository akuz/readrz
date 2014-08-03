package com.readrz.zzz.categ;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class FeatureMatch {
	
	private final static DecimalFormat _fmt = new DecimalFormat("0.00");
	
	private final Feature _feature;
	private final int _matchIndex;
	private final double _signal;
	private final List<FeatureMatch> _crossedMatches;
	
	public FeatureMatch(Feature feature, int matchIndex, double signal, List<FeatureMatch> crossedMatches) {
		
		_feature = feature;
		_matchIndex = matchIndex;
		_signal = signal;
		
		// create base matches
		if (crossedMatches == null) {
			_crossedMatches = new ArrayList<FeatureMatch>(1);
			_crossedMatches.add(this);
		} else {
			_crossedMatches = crossedMatches;
		}
	}
	
	public Feature getFeature() {
		return _feature;
	}
	
	public int getMatchIndex() {
		return _matchIndex;
	}
	
	public double getSignal() {
		return _signal;
	}
	
	public List<FeatureMatch> getCrossedMatches() {
		return _crossedMatches;
	}
		
	@Override
	public String toString() {
		return _matchIndex + "/" + _fmt.format(_signal);
	}

	@Override
	public int hashCode() {
		return _matchIndex;
	}

	@Override
	public boolean equals(Object o) {
		
		if (o == this) {
			return true;
		}
		if (o instanceof FeatureMatch == false) {
			return false;
		}
		FeatureMatch other = (FeatureMatch)o;
		
		if (_matchIndex != other._matchIndex) {
			return false;
		}
		
		if (_feature.equals(other._feature) == false) {
			return false;
		}
		
		return true;
	}
}
