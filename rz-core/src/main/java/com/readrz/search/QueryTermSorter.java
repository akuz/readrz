package com.readrz.search;

import java.util.Comparator;

import me.akuz.core.Hit;


public final class QueryTermSorter implements Comparator<QueryTerm> {

	@Override
	public int compare(QueryTerm o1, QueryTerm o2) {
		Hit h1 = o1.getHit();
		Hit h2 = o2.getHit();
		return h1.compareTo(h2);
	}

}
