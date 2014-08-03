package com.readrz.math.merging.snaps;

import java.util.Comparator;

public class Cluster1Sorter implements Comparator<Cluster1> {

	@Override
	public int compare(Cluster1 cluster1, Cluster1 cluster2) {

		int cmp;
		
		// get source counts
		int srcCount1 = cluster1.getSourceCount();
		int srcCount2 = cluster2.getSourceCount();
		
		// descending by source count
		cmp = -(srcCount1 - srcCount2);
		if (cmp != 0) {
			return cmp;
		}
		
		// get doc counts
		int docCount1 = cluster1.getDocumentCount();
		int docCount2 = cluster2.getDocumentCount();
		
		// descending by doc count
		cmp = -(docCount1 - docCount2);
		if (cmp != 0) {
			return cmp;
		}
		
		// compare dates for single docs
		if (docCount1 == 1 && docCount2 == 1) {
			
			ClusterDocument doc1 = cluster1.getDocumentRanks().keySet().iterator().next();
			ClusterDocument doc2 = cluster2.getDocumentRanks().keySet().iterator().next();
			
			// descending by date
			return -doc1.getSnapInfo().getSnap().getSrcDate().compareTo(
					doc2.getSnapInfo().getSnap().getSrcDate());
	
		} else {
			
			// equivalent
			return 0;
		}
	}

}
