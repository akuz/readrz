package com.readrz.math.merging.snaps;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.math.StatsUtils;

import com.readrz.data.index.FwdHit;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.math.merging.ProbItem;

public final class ClusterSentence implements ProbItem {
	
	public static final double SENTENCE_INDEX_HALF_LIFE = 1.0;

	private final ClusterDocument _document;
	private final Date _date;
	private final List<Pair<Integer, Double>> _stemIndexList;
	private final Map<Integer, Double>  _stemIndexMap;
	private final double _orderWeight;
	private Object _tag;
	
	public ClusterSentence(ClusterDocument document, int sentenceIndex, CorpusSentence sentence, Map<Integer, Integer> fillKeyIdByStemIndex) {

		_document = document;
		_date = document.getSnapInfo().getSnap().getSrcDate();

		_stemIndexMap = new HashMap<>();
		
		List<CorpusPlace> places = sentence.getPlaces();
		for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
			
			CorpusPlace place = places.get(placeIndex);
			int stemIndex = place.getStemIndex();
			FwdHit fwdHit = place.getFwdHit();
			
			if (stemIndex >= 0 && fwdHit != null) {
				_stemIndexMap.put(stemIndex, 1.0);
				if (fillKeyIdByStemIndex != null) {
					fillKeyIdByStemIndex.put(stemIndex, fwdHit.getKeyId());
				}
			}
		}
		
		_stemIndexList = new ArrayList<>();
		for (Entry<Integer, Double> entry : _stemIndexMap.entrySet()) {
			_stemIndexList.add(new Pair<Integer, Double>(entry.getKey(), entry.getValue()));
		}
		
		_orderWeight = calcSentenceIndexRank(sentenceIndex);
	}
	
	public static final double calcSentenceIndexRank(int sentenceIndex) {
		return StatsUtils.calcDistanceWeightExponential(sentenceIndex, SENTENCE_INDEX_HALF_LIFE);
	}
	
	public ClusterDocument getDocument() {
		return _document;
	}

	public int getStemCount() {
		return _stemIndexMap.size();
	}

	public Set<Integer> getStemIndexSet() {
		return _stemIndexMap.keySet();
	}

	public List<Pair<Integer, Double>> getStemIndexList() {
		return _stemIndexList;
	}

	public Map<Integer, Double> getStemIndexMap() {
		return _stemIndexMap;
	}
	
	public Date getDate() {
		return _date;
	}
	
	public double getWeight() {
		return 1.0;
	}
	
	public double getOrderWeight() {
		return _orderWeight;
	}
	
	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}
}
