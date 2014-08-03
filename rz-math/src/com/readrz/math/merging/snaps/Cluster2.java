//package com.readrz.math.merging.snaps;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import com.readrz.math.merging.ProbItemCluster;
//import com.readrz.utils.Pair;
//import com.readrz.utils.PairComparator;
//import com.readrz.utils.SortOrder;
//
//public final class Cluster2 {
//	
//	private final ProbItemCluster<Cluster1> _baseCluster;
//	private final List<Cluster1> _clusters1;
//	private final int _documentCount;
//	
//	public Cluster2(ProbItemCluster<Cluster1> baseCluster) {
//		
//		_baseCluster = baseCluster;
//
//		List<Pair<Cluster1, Integer>> sortedClusters1 = new ArrayList<>();
//		for (Cluster1 cluster1 : _baseCluster.getItems()) {
//			sortedClusters1.add(new Pair<Cluster1, Integer>(cluster1, cluster1.getDocumentCount()));
//		}
//		Collections.sort(sortedClusters1, new PairComparator<Cluster1, Integer>(SortOrder.Desc));
//		
//		int documentCount = 0;
//		_clusters1 = new ArrayList<>();
//		for (int i=0; i<sortedClusters1.size(); i++) {
//			Pair<Cluster1, Integer> pair = sortedClusters1.get(i);
//			documentCount += pair.v2();
//			_clusters1.add(pair.v1());
//		}
//		_documentCount = documentCount;
//	}
//	
//	public ProbItemCluster<Cluster1> getBaseCluster() {
//		return _baseCluster;
//	}
//	
//	public List<Cluster1> getClusters1() {
//		return _clusters1;
//	}
//	
//	public int getDocumentCount() {
//		return _documentCount;
//	}
//}
