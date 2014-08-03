//package com.readrz.math.merging.snaps;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Logger;
//
//import com.readrz.math.merging.Merge;
//import com.readrz.math.merging.ProbItemCluster;
//import com.readrz.math.merging.ProbItemClusterManager;
//import com.readrz.utils.Pair;
//import com.readrz.utils.PairComparator;
//import com.readrz.utils.SortOrder;
//import com.readrz.utils.logs.LogUtils;
//
//public final class Cluster2Algo {
//	
//	private final Logger _log;
//	private final List<Cluster2> _clusters2;
//	
//	public Cluster2Algo(List<ClusterDocument> documents, List<ClusterSentence> sentences) {
//
//		_log = LogUtils.getLogger(this.getClass().getName());
//
//		// calculate *base* level 1 clusters
//		ProbItemClusterManager<ClusterSentence> clusterManager1 = new ProbItemClusterManager<>(10, 0.5, 0.9, 2.5);
//		Merge<ClusterSentence, ProbItemCluster<ClusterSentence>> merge1 = new Merge<>(sentences, clusterManager1, 5);
//		List<ProbItemCluster<ClusterSentence>> baseClusters1 = merge1.getClusters();
//		_log.info("Found " + baseClusters1.size() + " level 1 clusters");
//
//		// create level 1 clusters from base clusters
//		Map<ProbItemCluster<ClusterSentence>, Cluster1> clusters1Map = new HashMap<>();
//		for (int i=0; i<documents.size(); i++) {
//			ClusterDocument document = documents.get(i);
//			ProbItemCluster<ClusterSentence> topCluster = null;
//			double topClusterRank = 0;
//			double topClusterSentenceRank = 0;
//			List<ClusterSentence> documentSentences = document.getSentences();
//			for (int j=0; j<documentSentences.size(); j++) {
//				ClusterSentence sentence = documentSentences.get(j);
//				@SuppressWarnings("unchecked")
//				ProbItemCluster<ClusterSentence> baseCluster = (ProbItemCluster<ClusterSentence>)sentence.getTag();
//				Double clusterSentenceRank = baseCluster.rankItem(sentence);
//				double clusterRank = baseCluster.getWeight() * clusterSentenceRank * sentence.getOrderWeight();
//				if (topClusterRank < clusterRank) {
//					topClusterRank = clusterRank;
//					topClusterSentenceRank = clusterSentenceRank * sentence.getOrderWeight();
//					topCluster = baseCluster;
//				}
//			}
//			if (topCluster != null) {
//				Cluster1 cluster1 = clusters1Map.get(topCluster);
//				if (cluster1 == null) {
//					cluster1 = new Cluster1(topCluster);
//					clusters1Map.put(topCluster, cluster1);
//				}
//				cluster1.addDocument(document, topClusterSentenceRank);
//			}
//		}
//		List<Cluster1> clusters1 = new ArrayList<>(clusters1Map.values());
//
//		// ------------- level 2 ---------------
//
//		ProbItemClusterManager<Cluster1> clusterManager2 = new ProbItemClusterManager<>(10, 0.5, 0.9, 2.0);
//		Merge<Cluster1, ProbItemCluster<Cluster1>> algo2 = new Merge<>(clusters1, clusterManager2, 5);
//		List<ProbItemCluster<Cluster1>> baseClusters2 = algo2.getClusters();
//		_log.info("Found " + baseClusters2.size() + " level 2 clusters");
//
//		// ------------- results ---------------
//
//		List<Pair<Cluster2, Integer>> sortedClusters2 = new ArrayList<>();
//		for (int i=0; i<baseClusters2.size(); i++) {
//			
//			Cluster2 cluster2 = new Cluster2(baseClusters2.get(i));
//			sortedClusters2.add(new Pair<Cluster2, Integer>(cluster2, cluster2.getDocumentCount()));
//		}
//		Collections.sort(sortedClusters2, new PairComparator<Cluster2, Integer>(SortOrder.Desc));
//
//		_clusters2 = new ArrayList<>();
//		for (int i=0; i<sortedClusters2.size(); i++) {
//			_clusters2.add(sortedClusters2.get(i).v1());
//		}
//	}
//	
//	public List<Cluster2> getClusters2() {
//		return _clusters2;
//	}
//
//}
