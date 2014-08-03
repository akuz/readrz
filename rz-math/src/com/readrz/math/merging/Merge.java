package com.readrz.math.merging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;


public final class Merge<TItem, TCluster extends MergeCluster<TItem>> {

	private final Logger _log;
	private final List<TCluster> _clusters;
	
	public Merge(
			List<TItem> items, 
			MergeClusterManager<TCluster> clusterManager,
			final int iterationCount) {

		_log = LogUtils.getLogger(this.getClass().getName());
		
		_log.finest("Initializing...");
		List<TCluster> clusters = new ArrayList<>();
		Map<TItem, TCluster> itemClusters = new HashMap<>();

		for (int i=0; i<items.size(); i++) {
			
			TItem item = items.get(i);
			
			double bestClusterRank = 0;
			TCluster bestCluster = null;
			
			for (int j=0; j<clusters.size(); j++) {
				TCluster cluster = clusters.get(j);
				double rank = cluster.rankItem(item);
				if (bestClusterRank < rank) {
					bestClusterRank = rank;
					bestCluster = cluster;
				}
			}
			
			if (bestCluster != null) {
				bestCluster.addItem(item);
				itemClusters.put(item, bestCluster);
			} else {
				bestCluster = clusterManager.createCluster();
				clusters.add(bestCluster);
				bestCluster.addItem(item);
				itemClusters.put(item, bestCluster);
			}
			
			int count = i+1;
			if (count % 100 == 0) {
				_log.finest("Processed " + count + " items");
			}
		}
		_log.finest("Cluster count: " + clusters.size());
		
		int iter = 1;
		while (true) {
			
			_log.finest("Iteration " + iter + "...");
			double time = (double)iter / (double)iterationCount;
			clusterManager.setTime(time);
			
			int changeCount = 0;
			for (int i=0; i<items.size(); i++) {
				
				TItem item = items.get(i);
				TCluster oldCluster = itemClusters.get(item);
				TCluster oldSingleCluster = null;
				if (oldCluster.getItemCount() == 1) {
					oldSingleCluster = oldCluster;
				} else {
					oldCluster.removeItem(item);
					itemClusters.remove(item);
				}
				
				double newBestClusterRank = 0;
				TCluster newBestCluster = null;
				
				for (int j=0; j<clusters.size(); j++) {
					TCluster cluster = clusters.get(j);
					if (cluster != oldSingleCluster) {
						double rank = cluster.rankItem(item);
						if (newBestClusterRank < rank) {
							newBestClusterRank = rank;
							newBestCluster = cluster;
						}
					}
				}
				
				if (newBestCluster != null) {
					if (oldSingleCluster != null) {
						oldSingleCluster.removeItem(item);
						itemClusters.remove(item);
					}
					newBestCluster.addItem(item);
					itemClusters.put(item, newBestCluster);
				} else {
					if (oldSingleCluster != null) {
						newBestCluster = oldSingleCluster;
					} else {
						newBestCluster = clusterManager.createCluster();
						clusters.add(newBestCluster);
						newBestCluster.addItem(item);
						itemClusters.put(item, newBestCluster);
					}
				}
				
				if (newBestCluster != oldCluster) {
					changeCount++;
					if (oldCluster.getItemCount() == 0) {
						_log.finest("Cluster collapsed");
						clusters.remove(oldCluster);
					}
				}
			}

			_log.finest("Cluster change count: " + changeCount);
			_log.finest("Cluster count: " + clusters.size());
			
			if (iter >= iterationCount) {
				_log.finest("All iterations done, stopping...");
				break;
			}
			
			iter++;
		}
		
		_clusters = clusters;
	}
	
	public List<TCluster> getClusters() {
		return _clusters;
	}
}
