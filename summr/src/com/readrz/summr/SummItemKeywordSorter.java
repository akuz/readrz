package com.readrz.summr;

import java.util.Comparator;

import com.readrz.data.SummListItemKeyword;

public final class SummItemKeywordSorter implements Comparator<SummListItemKeyword> {

	private static final double EPSILON = 0.00000001;
	
	@Override
	public int compare(SummListItemKeyword keyword1, SummListItemKeyword keyword2) {
		
		double probDiff = keyword1.getProb() - keyword2.getProb();

		// if different probability
		if (Math.abs(probDiff) > EPSILON) {
			
			// descending by probability
			return -(int)Math.signum(probDiff);

		} else { // same probability
			
			// ascending alphabetically
			return keyword1.getWord().compareTo(keyword2.getWord());
		}
	}

}
