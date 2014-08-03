package com.readrz.math.merging.snaps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.readrz.math.merging.ProbItemCluster;

public final class Cluster1 {
	
	private final ProbItemCluster<ClusterSentence> _baseCluster;
	private final Set<ObjectId> _sourceIds;
	private final Map<ClusterDocument, Double> _documentRanks;
//	private double _sumDocumentRanks;
//	private Object _tag;
	
	public Cluster1(ProbItemCluster<ClusterSentence> baseCluster) {
		_baseCluster = baseCluster;
		_documentRanks = new HashMap<>();
		_sourceIds = new HashSet<>();
	}
	
	public ProbItemCluster<ClusterSentence> getBaseCluster() {
		return _baseCluster;
	}
	
	public void addDocument(ClusterDocument document, double rank) {
		_sourceIds.add(document.getSnapInfo().getSource().getId());
		_documentRanks.put(document, rank);
//		_sumDocumentRanks += rank;
	}
	
	public int getSourceCount() {
		return _sourceIds.size();
	}
	
	public Set<ObjectId> getSourceIds() {
		return _sourceIds;
	}
	
	public int getDocumentCount() {
		return _documentRanks.size();
	}
	
	public Map<ClusterDocument, Double> getDocumentRanks() {
		return _documentRanks;
	}

//	@Override
//	public double getWeight() {
//		return _sumDocumentRanks;
//	}
//
//	@Override
//	public Set<Integer> getStemIndexSet() {
//		return _baseCluster.getStemIndexSet();
//	}
//
//	@Override
//	public List<Pair<Integer, Double>> getStemIndexList() {
//		return _baseCluster.getStemIndexList();
//	}
//
//	@Override
//	public Map<Integer, Double> getStemIndexMap() {
//		return _baseCluster.getStemIndexMap();
//	}
//
//	@Override
//	public Object getTag() {
//		return _tag;
//	}
//
//	@Override
//	public void setTag(Object tag) {
//		_tag = tag;
//	}

}
