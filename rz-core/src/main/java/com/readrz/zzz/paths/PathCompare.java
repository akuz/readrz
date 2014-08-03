package com.readrz.zzz.paths;

import java.util.Comparator;

public class PathCompare implements Comparator<Path> {

	public int compare(
			Path arg0,
			Path arg1) {

		int cmp;
		
		double w0 = arg0.getStats().getSumAdjustedPhraseProb();
		double w1 = arg1.getStats().getSumAdjustedPhraseProb();
		
		if (w0 < w1) {
			cmp = -1;
		} else if (w0 > w1) {
			cmp = 1;
		} else {
			cmp = 0;
		}
		
		return - cmp; // desc
	}
}
