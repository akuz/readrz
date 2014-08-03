package com.readrz.zzz.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.readrz.zzz.Phrase;

public final class PathStats {
	
	private double _sumAdjustedPhraseProb;
	private final Map<String, Double>  _stemProbs;
	private final Map<String, Double>  _stemConditionalExpectedWeights;
	private final List<Pair<Phrase, Double>> _phraseProbs;

	private boolean _normalized;
	private List<Pair<String, Double>> _stemsSortedByExpectedWeight;
	private Map<String, Double> _stemsExpectedWeights;
	private Map<String, String> _wordsByStem;
	
	public PathStats() {
		_stemProbs = new HashMap<String, Double>();
		_stemConditionalExpectedWeights = new HashMap<String, Double>();
		_phraseProbs = new ArrayList<Pair<Phrase, Double>>();
	}
	
	public List<Pair<Phrase, Double>> getPhraseProbs() {
		return _phraseProbs;
	}
	
	private final void checkNormalized(boolean yes) {
		if (yes) {
			if (_normalized == false) {
				throw new IllegalStateException("Cannot call this method, first call normalize()");
			}
		} else {
			if (_normalized) {
				throw new IllegalStateException("Cannot call this method, already normalized");
			}
		}
	}
	
	public Map<String, Double> getStemProbs() {
		checkNormalized(true);
		return _stemProbs;
	}
	
	public Map<String, Double> getStemConditionalExpectedWeights() {
		checkNormalized(true);
		return _stemConditionalExpectedWeights;
	}
	
	public void addPhraseObservation(Phrase phrase, double phraseProb) {

		checkNormalized(false);

		double adjustedPhraseProb = phraseProb * phrase.getWeight();
		
		Map<String, Double> phraseStemWeights = phrase.getStemWeights();
		
		for (String stem : phraseStemWeights.keySet()) {
			addPhraseStemObservation(adjustedPhraseProb, stem, phraseStemWeights.get(stem));
		}

		_sumAdjustedPhraseProb += adjustedPhraseProb;
		
		_phraseProbs.add(new Pair<Phrase, Double>(phrase, phraseProb));
	}
	
	private void addPhraseStemObservation(double adjustedPhraseProb, String stem, double stemWeight) {
		
		// add stem phrase prob
		Double sumStemAdjustedPhraseProb = _stemProbs.get(stem);
		if (sumStemAdjustedPhraseProb == null) {
			sumStemAdjustedPhraseProb = adjustedPhraseProb;
		} else {
			sumStemAdjustedPhraseProb += adjustedPhraseProb;
		}
		_stemProbs.put(stem, sumStemAdjustedPhraseProb);
		
		// add stem conditional expected weight
		Double sumStemConditionalWeight = _stemConditionalExpectedWeights.get(stem);
		if (sumStemConditionalWeight == null) {
			sumStemConditionalWeight = adjustedPhraseProb * stemWeight;
		} else {
			sumStemConditionalWeight += adjustedPhraseProb * stemWeight;
		}
		_stemConditionalExpectedWeights.put(stem, sumStemConditionalWeight);
	}

	public void normalizeObservations() {
		
		checkNormalized(false);

		if (_sumAdjustedPhraseProb > 0) {

			for (String stem : _stemProbs.keySet()) {

				Double sumStemAdjustedPhraseProb = _stemProbs.get(stem);
				_stemProbs.put(stem,
						sumStemAdjustedPhraseProb /
						_sumAdjustedPhraseProb);
				
				Double sumStemConditionalWeight = _stemConditionalExpectedWeights.get(stem);
				_stemConditionalExpectedWeights.put(stem, 
						sumStemConditionalWeight / 
						sumStemAdjustedPhraseProb);
			}
		}
		
		_normalized = true;
	}
	
	public double getSumAdjustedPhraseProb() {
		checkNormalized(true);
		return _sumAdjustedPhraseProb;
	}
	
	private void ensureExtectedWeights() {
		
		checkNormalized(true);

		if (_stemsSortedByExpectedWeight == null) {

			_stemsSortedByExpectedWeight = new ArrayList<Pair<String,Double>>();
			_stemsExpectedWeights = new HashMap<String, Double>();
			
			for (String stem : _stemProbs.keySet()) {
			
				// get stem probability
				Double stemProb = _stemProbs.get(stem);
				
				// get stem conditional expected weight
				Double stemConditionalExpectedWeight = _stemConditionalExpectedWeights.get(stem);
				
				// calculate stem addition weight
				Double stemExpectedWeight = stemProb * stemConditionalExpectedWeight;
				
				// add for sorting
				_stemsSortedByExpectedWeight.add(new Pair<String, Double>(stem, stemExpectedWeight));
				_stemsExpectedWeights.put(stem, stemExpectedWeight);
			}
			Collections.sort(_stemsSortedByExpectedWeight, new PairComparator<String, Double>(SortOrder.Desc));
		}
	}
	
	public List<Pair<String, Double>> getStemsSortedByExpectedWeight() {
		
		ensureExtectedWeights();
		
		return _stemsSortedByExpectedWeight;
	}
	
	public Map<String, Double> getStemsExpectedWeights() {
		
		ensureExtectedWeights();
		
		return _stemsExpectedWeights;
	}
	
	public void setWordsByStem(Map<String, String> wordsByStem) {
		_wordsByStem = wordsByStem;
	}
	
	public Map<String, String> getWordsByStem() {
		return _wordsByStem;
	}
	
}
