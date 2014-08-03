package com.readrz.zzz.paths;

import java.util.List;
import java.util.Map;

import me.akuz.core.Pair;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Phrase;
import com.readrz.zzz.PhrasePlace;

public final class PathQuoteSelectorBest {

	private final Path _path;
	private PathQuote _bestQuote;
	
	public PathQuoteSelectorBest(Path path) {
		_path = path;
		selectBestQuote();
	}
	
	public Path getPath() {
		return _path;
	}
	
	public PathQuote getBestQuote() {
		return _bestQuote;
	}
	
	public boolean isEmpty() {
		return _bestQuote == null;
	}
	
	private void selectBestQuote() {
		
		final int RANK_WORDS_COUNT = 10;

		PathStats pathStats = _path.getStats();
		List<Pair<Phrase, Double>> phraseProbs = pathStats.getPhraseProbs();
		List<Pair<String, Double>> stemsByExpectedWeight = pathStats.getStemsSortedByExpectedWeight();
		
		Pair<Phrase, Double> bestPhrase = null;
		
		for (int i=0; i<phraseProbs.size(); i++) {
			
			Pair<Phrase, Double> phrasePair = phraseProbs.get(i);
			Phrase phrase = phrasePair.v1();
			Double phraseProb = phrasePair.v2();
			List<PhrasePlace> phrasePlaces = phrase.getPlaces();
			
			// ignore empty phrases
			if (phrasePlaces.size() == 0) {
				continue;
			}

			Map<String, Double> phraseStemWeights = phrase.getStemWeights();
			
			double phraseRank = 0;
			for (int j=0; j<stemsByExpectedWeight.size() && j<RANK_WORDS_COUNT; j++) {
				
				Pair<String, Double> stemPair = stemsByExpectedWeight.get(j);
				String stem = stemPair.v1();
				Double expectedWeight = stemPair.v2();
				
				Double phraseStemWeight = phraseStemWeights.get(stem);
				
				if (phraseStemWeight != null) {
					
					double stemRank = phraseStemWeight * expectedWeight;
					phraseRank += stemRank;
				}
			}
			phraseRank *= phraseProb;
			
			// dramatically increase title quotes rank
			if (phrase.getFieldName() == FieldName.TITLE) {
				phraseRank *= 1000;
			}

			// update best quote
			if (bestPhrase == null) {
				
				bestPhrase = new Pair<Phrase, Double>(phrase, phraseRank);
				
			} else if (bestPhrase.v2() < phraseRank) {
				
				bestPhrase.setV1(phrase);
				bestPhrase.setV2(phraseRank);
			}
		}
		
		if (bestPhrase != null) {
			_bestQuote = new PathQuote(bestPhrase.v1(), bestPhrase.v2());
		} else {
			_bestQuote = null;
		}
	}
}
