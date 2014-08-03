package com.readrz.zzz.paths;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class PathCondition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final boolean _isPositive;
	private final String _stem;
	private Map<String, Double> _expectedStemWeights;
	
	public PathCondition(boolean isPositive, String stem) {
		_isPositive = isPositive;
		_stem = stem;
	}
	
	public boolean isPositive() {
		return _isPositive;
	}
	
	public String getStem() {
		return _stem;
	}
	
	public void addExpectedStemWeight(String stem, Double weight) {
		if (_expectedStemWeights == null) {
			_expectedStemWeights = new HashMap<String, Double>();
		}
		_expectedStemWeights.put(stem, weight);
	}
	
	public Map<String, Double> getExpectedStemWeights() {
		return _expectedStemWeights;
	}
}
