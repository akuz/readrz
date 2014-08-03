package com.readrz.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;


public final class Unstemmer<TKey> {
	
	private final Map<TKey, List<Pair<String, Double>>> _wordWeightsByKey;
	private Map<TKey, String> _wordsByKey;
	
	public Unstemmer() {
		_wordWeightsByKey = new HashMap<TKey, List<Pair<String, Double>>>();
	}
	
	public void add(TKey key, String word, Double weight) {
		
		List<Pair<String, Double>> list = _wordWeightsByKey.get(key);
		if (list == null) {
			list = new ArrayList<Pair<String,Double>>();
			_wordWeightsByKey.put(key, list);
		}
		
		boolean found = false;
		for (int i=0; i<list.size(); i++) {
			
			Pair<String, Double> wordWeight = list.get(i);
			
			if (word.equalsIgnoreCase(wordWeight.v1())) {
				wordWeight.setV2(wordWeight.v2() + weight);
				found = true;
				break;
			}
		}
		
		if (!found) {
			Pair<String, Double> wordWeight = new Pair<String, Double>(word, weight);
			list.add(wordWeight);
		}
	}
	
	public void optimize() {
		
		if (_wordsByKey != null) {
			throw new IllegalStateException("Cannot optimize again, already optimized");
		}
		
		Map<TKey, String> wordByKey = new HashMap<TKey, String>();
		
		PairComparator<String, Double> cmp = new PairComparator<String, Double>(SortOrder.Desc);
		
		for (TKey key : _wordWeightsByKey.keySet()) {
			
			List<Pair<String, Double>> list = _wordWeightsByKey.get(key);
			
			Collections.sort(list, cmp);
			
			wordByKey.put(key, list.get(0).v1());
		}
		
		_wordsByKey = wordByKey;
	}
	
	public Map<TKey, String> getWordsByKey() {

		if (_wordsByKey == null) {
			throw new IllegalStateException("Not optimized, call optimize first");
		}
		
		return _wordsByKey;
	}

}
