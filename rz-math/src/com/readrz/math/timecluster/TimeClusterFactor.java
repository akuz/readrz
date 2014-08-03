package com.readrz.math.timecluster;

import me.akuz.core.math.NormalMeanDist;

public final class TimeClusterFactor {
	
	private int _clusterCount;
	private final NormalMeanDist _valueDist;
	private final NormalMeanDist _lengthDist;
	
	public TimeClusterFactor(NormalMeanDist valueDistPrior, NormalMeanDist lengthDistPrior) {
		_valueDist = valueDistPrior.clone();
		_lengthDist = lengthDistPrior.clone();
	}
	
	public int getClusterCount() {
		return _clusterCount;
	}
	
	public NormalMeanDist getValueDist() {
		return _valueDist;
	}
	
	public NormalMeanDist getLengthDist() {
		return _lengthDist;
	}
	
	public double getLogLikeClusterValueAdd(final int currentClusterLength, final double newValue) {
		
		double logLike = 0;
		
		logLike += _lengthDist.probabilityMoreThan(currentClusterLength);
		
		logLike += _valueDist.density(newValue);
		
		return logLike;
	}

}
