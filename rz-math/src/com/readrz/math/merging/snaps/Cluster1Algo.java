package com.readrz.math.merging.snaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;

import com.readrz.math.merging.Merge;
import com.readrz.math.merging.ProbItemCluster;
import com.readrz.math.merging.ProbItemClusterManager;

public final class Cluster1Algo {
	
	private static final int    TOP_STEM_COUNT               = 10;
	private static final double TOP_STEM_MIN_PROB            = 0.5;
	private static final double TOP_STEM_ALWAYS_PROB         = 0.9;
	private static final double OVERLAP_STEM_WEIGHT_TO_MERGE = 2.5;
	
	private final Logger _log;
	private final List<Cluster1> _clusters1;
	
	public Cluster1Algo(List<ClusterSentence> sentences, double minMinutesDiffToMerge, int iterationCount) {

		_log = LogUtils.getLogger(this.getClass().getName());

		ProbItemClusterManager<ClusterSentence> clusterManager1 
			= new ProbItemClusterManager<>(
					TOP_STEM_COUNT, 
					TOP_STEM_MIN_PROB, 
					TOP_STEM_ALWAYS_PROB, 
					OVERLAP_STEM_WEIGHT_TO_MERGE,
					minMinutesDiffToMerge);
			
		_log.fine("Clustering sentences (" + iterationCount +" iterations)...");
		Merge<ClusterSentence, ProbItemCluster<ClusterSentence>> merge1 
			= new Merge<>(
					sentences,
					clusterManager1,
					iterationCount);

		List<ProbItemCluster<ClusterSentence>> baseClusters1 = merge1.getClusters();
		_log.fine("Found " + baseClusters1.size() + " sentence clusters.");

		_log.fine("Assigning documents to sentence clusters...");
		Map<ProbItemCluster<ClusterSentence>, Cluster1> clusters1Map = new HashMap<>();
		Set<ClusterDocument> categorizedDocs = new HashSet<>();
		for (int i=0; i<sentences.size(); i++) {
			
			// get next sentence and its document
			ClusterSentence sentence = sentences.get(i);
			ClusterDocument doc = sentence.getDocument();
			
			// check if this document already categorized
			if (categorizedDocs.contains(doc)) {
				continue;
			}
			
			// find top cluster for the doc
			ProbItemCluster<ClusterSentence> topCluster = null;
			double topClusterRank = 0;
			
			List<ClusterSentence> docSentences = doc.getSentences();
			for (int j=0; j<docSentences.size(); j++) {
				
				// get next document sentence
				ClusterSentence docSentence = docSentences.get(j);

				// get sentence cluster
				@SuppressWarnings("unchecked")
				ProbItemCluster<ClusterSentence> cluster = (ProbItemCluster<ClusterSentence>)docSentence.getTag();
				
				// rank sentence against the cluster
				Double clusterRank = cluster.rankItem(docSentence) * docSentence.getOrderWeight();
				
				// select top cluster
				if (topClusterRank < clusterRank) {
					topClusterRank = clusterRank;
					topCluster = cluster;
				}
			}
			
			// assign to top cluster
			if (topCluster != null) {
				Cluster1 cluster1 = clusters1Map.get(topCluster);
				if (cluster1 == null) {
					cluster1 = new Cluster1(topCluster);
					clusters1Map.put(topCluster, cluster1);
				}
				cluster1.addDocument(doc, topClusterRank);
			}
			
			// remember this doc was categorized
			categorizedDocs.add(doc);
		}
		
		_log.fine("Sorting " + baseClusters1.size() + " clusters...");
		_clusters1 = new ArrayList<>(clusters1Map.values());
		Collections.sort(_clusters1, new Cluster1Sorter());
		
		_log.fine("Finished " + baseClusters1.size() + " clustering.");
	}
	
	public List<Cluster1> getClusters1() {
		return _clusters1;
	}

}
