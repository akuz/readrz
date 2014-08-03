package com.readrz.fb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.Interval;
import me.akuz.core.sort.HeapSort;

public final class TestOverlap {
	
	public static final boolean test(List<Date> starts, List<Date> ends) {
		
		if (starts.size() != ends.size()) {
			throw new IllegalArgumentException("Starts and ends lengths don't match");
		}
		
		List<Interval> intervals = new ArrayList<>();
		for (int i=0; i<starts.size(); i++) {
			intervals.add(new Interval(starts.get(i), ends.get(i)));
		}
		HeapSort.sort(intervals);
		
		int prevIndex=0;
		for (int currIndex=1; currIndex<intervals.size(); currIndex++) {
			
			Interval prevInterval = intervals.get(prevIndex);
			Interval currInterval = intervals.get(currIndex);
			
			if (prevInterval.end().compareTo(currInterval.start()) > 0) {
				return true;
			}
			if (currInterval.end().compareTo(prevInterval.end()) > 0) {
				prevIndex = currIndex;
			}
		}
		
		return false;
	}
}
