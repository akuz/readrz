package com.readrz.math.timecluster;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.math.NormalMeanDist;
import me.akuz.core.math.SampleVariance;

public final class TimeClusterAlgo {
	
	public TimeClusterAlgo(double[] values) {
		
		final int ITERATION_COUNT = 10;
//		final int DESIRED_FACTOR_COUNT = 5;

		final double DESIRED_CLUSTER_VALUES_STDDEV_SHRINKAGE = 0.25;
		
		final int    DESIRED_CLUSTER_LENGTH_MEAN = 10;
		final double DESIRED_CLUSTER_LENGTH_MEAN_VARIANCE = Math.pow(3, 2);
		final double DESIRED_CLUSTER_LENGTH_VARIANCE = Math.pow(2, 2);

//		final double EXPECTED_NUMBER_OF_CLUSTERS = (double) values.length / DESIRED_CLUSTER_LENGTH_MEAN;
//		final double EXPECTED_FACTOR_OCCURRENCE_COUNT = EXPECTED_NUMBER_OF_CLUSTERS / DESIRED_FACTOR_COUNT;
//		final double FACTOR_ALPHA = 0.01 * EXPECTED_NUMBER_OF_CLUSTERS;
		
		final SampleVariance sv = new SampleVariance();
		for (int i=0; i<values.length; i++) {
			sv.add(values[i]);
		}
		
		NormalMeanDist valuesDistPrior = new NormalMeanDist(
				sv.getMean(), 
				sv.getVariance(),
				Math.pow(Math.sqrt(sv.getVariance()) * DESIRED_CLUSTER_VALUES_STDDEV_SHRINKAGE, 2));
		
		NormalMeanDist lengthDistPrior = new NormalMeanDist(
				DESIRED_CLUSTER_LENGTH_MEAN, 
				DESIRED_CLUSTER_LENGTH_MEAN_VARIANCE,
				DESIRED_CLUSTER_LENGTH_VARIANCE);
		
		// init first factor
		List<TimeClusterFactor> factors = new ArrayList<>();
		TimeClusterFactor firstFactor = new TimeClusterFactor(valuesDistPrior, lengthDistPrior);
		factors.add(firstFactor);

		// init last cluster with first value
		TimeCluster lastCluster = new TimeCluster(firstFactor);
		lastCluster.addLastValue(values[0]);
		
		// assign other values
		for (int i=1; i<values.length; i++) {
			
//			double value = values[i];
			
			
		}

		for (int iter=1; iter<=ITERATION_COUNT; iter++) {
			
		}
		
		
	}

}
