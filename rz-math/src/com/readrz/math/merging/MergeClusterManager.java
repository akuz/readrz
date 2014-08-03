package com.readrz.math.merging;

public interface MergeClusterManager<TCluster> {

	void setTime(double time);
	
	TCluster createCluster();
}
