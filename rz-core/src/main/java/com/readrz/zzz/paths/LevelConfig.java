package com.readrz.zzz.paths;

public final class LevelConfig {
	
	private final double _minimumPathWeight;
	private final int _maximumPositivePathsCount;
	
	public LevelConfig(double minimumPathWeight, int maximumPositivePathsCount) {
		_minimumPathWeight = minimumPathWeight;
		_maximumPositivePathsCount = maximumPositivePathsCount;
	}
	
	public double getMinimumPathWeight() {
		return _minimumPathWeight;
	}
	
	public int getMaximumPositivePathsCount() {
		return _maximumPositivePathsCount;
	}
}
