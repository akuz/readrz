package com.readrz.math.merging;


public final class ProbItemClusterManager<TDoc extends ProbItem> implements MergeClusterManager<ProbItemCluster<TDoc>> {

	private final int    _topStemCount;
	private final double _topStemMinProb;
	private       double _topStemMinProb_current;
	private final double _topStemAlwaysProb;
	private       double _topStemAlwaysProb_current;
	private final double _overlapStemWeightToMerge;
	private final double _maxMinutesDiffToMerge;
	private double _time;
	
	public ProbItemClusterManager(
			final int topStemCount, 
			final double topStemMinProb,
			final double topStemAlwaysProb,
			final double overlapStemWeightToMerge,
			final double maxMinutesDiffToMerge) {
		
		_topStemCount = topStemCount;
		_topStemMinProb = topStemMinProb;
		_topStemAlwaysProb = topStemAlwaysProb;
		_overlapStemWeightToMerge = overlapStemWeightToMerge;
		_maxMinutesDiffToMerge = maxMinutesDiffToMerge;
		setTime(0);
	}
	
	public int getTopStemCount() {
		return _topStemCount;
	}
	
	public double getTopStemMinProb() {
		return _topStemMinProb_current;
	}
	
	public double getTopStemAlwaysProb() {
		return _topStemAlwaysProb_current;
	}
	
	public double getOverlapStemWeightToMerge() {
		return _overlapStemWeightToMerge;
	}
	
	public double getMaxMinutesDiffToMerge() {
		return _maxMinutesDiffToMerge;
	}
	
	@Override
	public ProbItemCluster<TDoc> createCluster() {
		return new ProbItemCluster<TDoc>(this);
	}

	@Override
	public void setTime(double time) {
		if (time < _time) {
			throw new IllegalArgumentException("Time must be nondecreasing");
		}
		if (time > 1) {
			throw new IllegalArgumentException("Max allowed time is 1");
		}
		_time = time;
		_topStemMinProb_current    = _topStemMinProb * _time;
		_topStemAlwaysProb_current = _topStemAlwaysProb + (1.0 - _topStemAlwaysProb) * _time;
	}

}
