package com.readrz.math.merging;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Pair;


public interface ProbItem {
	
	Set<Integer>                getStemIndexSet();
	List<Pair<Integer, Double>> getStemIndexList();
	Map<Integer, Double>        getStemIndexMap();
	
	Date getDate();
	double getWeight();
	
	Object getTag();
	void   setTag(Object tag);
}
