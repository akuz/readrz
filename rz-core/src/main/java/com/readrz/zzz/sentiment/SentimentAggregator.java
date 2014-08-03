package com.readrz.zzz.sentiment;

public final class SentimentAggregator {
	
	private double _totalWeight;
	private double _totalWeightedSentiment;
	private double _average;
	
	public SentimentAggregator() {
		// init with zeroes
	}
	
	public void add(double weight, double sentiment) {
		_totalWeight += weight;
		_totalWeightedSentiment += weight * sentiment;
		_average = _totalWeightedSentiment / _totalWeight;
	}
	
	public void add(SentimentAggregator other) {
		_totalWeight += other._totalWeight;
		_totalWeightedSentiment += other._totalWeightedSentiment;
		_average = _totalWeightedSentiment / _totalWeight;
	}
	
	public double getAverage() {
		return _average;
	}
}
