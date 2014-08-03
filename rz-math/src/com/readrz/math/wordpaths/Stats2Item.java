package com.readrz.math.wordpaths;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.sort.SelectK;

import org.bson.types.ObjectId;

import com.readrz.data.Snap;
import com.readrz.data.SnapPhrase;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;
import com.readrz.lang.Unstemmer;
import com.readrz.search.QueryKeyIds;

public final class Stats2Item {

	private final static DecimalFormat FMT_PROB = new DecimalFormat("0.0000");

	private final static double STEM_WEIGHT_DECAY_CHAR_HALF_LIFE   = 30;
	private final static int    BEST_ID_RANK_CHECK_KEYS_COUNT      = 3;
	private final static int    STATS_SELECT_TOP_STEMS_COUNT       = 100;
	
	// key ids
	private final QueryKeyIds _queryKeyIds;
	private final List<Integer> _fullSentenceKeyIds;
	private final List<Integer> _hierSentenceKeyIds;
	private final Integer _leafSentenceKeyId;
	private final Set<Integer> _stopKeyIds;
	
	// period dates
	private final Date _minDateInc;
	private final Date _maxDateExc;
	
	// main stats
	private double _sumPhraseRank;
	private final Map<Integer, Double> _keyProbs;
	private final Map<Integer, Double> _keyConditionalExpectedWeights;
	private Unstemmer<Integer> _unstemFullSearchLowercase;
	private Unstemmer<Integer> _unstemHierSearchLowercase;
	private Unstemmer<Integer> _unstemLeafSearchLowercase;
	private Unstemmer<String>  _unstemFullSearchOriginal;
	private Unstemmer<String>  _unstemHierSearchOriginal;
	private Unstemmer<String>  _unstemLeafSearchOriginal;
	private boolean _isNormalized;
	
	// stats calculated after normalization
	private List<Pair<Integer, Double>> _keysSortedByProb;
	private List<Pair<Integer, Double>> _keysSortedByExpectedWeight;
	private String _fullSearch;
	private String _hierSearch;
	private String _leafSearch;
	private ObjectId _snapId;
	private String _snapTitle;
	private double _snapRank;
	private final List<PathsNode> _children;
	private List<SnapPhrase> _matchingKeyStatsPhrases;
	private List<SnapPhrase> _remainingKeyStatsPhrases;
	private Set<ObjectId> _matchingSnapIds;
	
	public Stats2Item(
			String logPathsIdString,
			Set<ObjectId> parentSnapIds,
			List<SnapPhrase> keyStatsPhrases, 
			List<SnapPhrase> snapStatsPhrases,
			QueryKeyIds queryKeyIds, 
			List<Integer> hierSentenceKeyIds,
			Date minDateInc,
			Date maxDateExc,
			Set<Integer> stopKeyIds,
			int[] levelDepths,
			int maxLevel,
			int level) {

		_queryKeyIds = queryKeyIds;
		_fullSentenceKeyIds = queryKeyIds.getSentenceKeyIds();
		_hierSentenceKeyIds = hierSentenceKeyIds;
		if (hierSentenceKeyIds != null && hierSentenceKeyIds.size() > 0) {
			_leafSentenceKeyId = hierSentenceKeyIds.get(hierSentenceKeyIds.size()-1);
		} else {
			_leafSentenceKeyId = null;
		}
		_stopKeyIds = stopKeyIds;
		
		// keep dates for ranking
		_minDateInc = minDateInc;
		_maxDateExc = maxDateExc;
		
		// prepare stats objects
		_keyProbs = new HashMap<Integer, Double>();
		_keyConditionalExpectedWeights = new HashMap<Integer, Double>();
		_unstemFullSearchLowercase = new Unstemmer<>();
		_unstemFullSearchOriginal = new Unstemmer<>();
		_unstemHierSearchLowercase = new Unstemmer<>();
		_unstemHierSearchOriginal = new Unstemmer<>();
		_unstemLeafSearchLowercase = new Unstemmer<>();
		_unstemLeafSearchOriginal = new Unstemmer<>();

		// fine matching phrases
		_matchingKeyStatsPhrases = new ArrayList<>();
		_matchingSnapIds = new HashSet<ObjectId>();
		for (int i=0; i<keyStatsPhrases.size(); i++) {
			SnapPhrase phrase = keyStatsPhrases.get(i);
			
			// check if matches query, and find sentence hits
			Map<Integer, List<FwdHit>> foundSentenceFwdHitsMap = new HashMap<>();
			if (phrase.checkMatchesQuery(_queryKeyIds, foundSentenceFwdHitsMap)) {
				
				// remember matching key stats phrase
				_matchingKeyStatsPhrases.add(phrase);
				
				// add phrase stats
				addPhraseStats(phrase, foundSentenceFwdHitsMap);
				
				// remember matching snap id
				_matchingSnapIds.add(phrase.getSnap().getId());
			}
		}

		// enrich matching snap ids
		for (int i=0; i<snapStatsPhrases.size(); i++) {
			SnapPhrase phrase = snapStatsPhrases.get(i);
			
			// check if this snap is already counted as matching
			if (_matchingSnapIds.contains(phrase.getSnap().getId())) {
				continue;
			}
			
			// check if phrase matches query
			if (phrase.checkMatchesQuery(_queryKeyIds)) {
				
				// remember another matching snap id
				_matchingSnapIds.add(phrase.getSnap().getId());
			}
		}

		// find remaining phrases
		_remainingKeyStatsPhrases = new ArrayList<>();
		for (int i=0; i<keyStatsPhrases.size(); i++) {
			SnapPhrase phrase = keyStatsPhrases.get(i);
			
			// check the phrase is not from a matching snap
			if (!_matchingSnapIds.contains(phrase.getSnap().getId())) {
			
				// phrase does not match query
				_remainingKeyStatsPhrases.add(phrase);
			}
		}
		
		normalize();

		// select best id
		for (int i=0; i<_matchingKeyStatsPhrases.size(); i++) {
			SnapPhrase phrase = _matchingKeyStatsPhrases.get(i);
			Snap snap = phrase.getSnap();
			if (!parentSnapIds.contains(snap.getId())) {
				updateSnapId_MatchChecked(phrase);
			}
		}
		
		// calculate children
		if (level < maxLevel) {
			
			Set<ObjectId> parentSnapIdsForChildren = new HashSet<>(parentSnapIds);
			if (_snapId != null) {
				parentSnapIdsForChildren.add(_snapId);
			}
			
			Stats2CalcAuto autoChildren 
				= new Stats2CalcAuto(
					logPathsIdString, 
					_matchingKeyStatsPhrases,
					parentSnapIdsForChildren,
					queryKeyIds,
					this, 
					minDateInc, 
					maxDateExc, 
					stopKeyIds, 
					levelDepths,
					maxLevel, 
					level+1);
			
			_children = autoChildren.getNodes();
			
		} else {
			_children = null;
		}
	}
	
	public QueryKeyIds getQueryKeyIds() {
		return _queryKeyIds;
	}
	
	public List<Integer> getFullSentenceKeyIds() {
		return _fullSentenceKeyIds;
	}
	
	public List<Integer> getHierSentenceKeyIds() {
		return _hierSentenceKeyIds;
	}
	
	public Integer getLeafSentenceKeyId() {
		return _leafSentenceKeyId;
	}
	
	public int getSnapCount() {
		return _matchingSnapIds.size();
	}
	
	public Set<ObjectId> getSnapIds() {
		return _matchingSnapIds;
	}
	
	public ObjectId getSnapId() {
		return _snapId;
	}
	
	public String getSnapTitle() {
		return _snapTitle;
	}
	
	public double getSumPhraseRank() {
		return _sumPhraseRank;
	}
	
	public List<PathsNode> getChildren() {
		return _children;
	}
	
	public List<SnapPhrase> getRemainingKeyStatsPhrasesAndForgetThem() {
		List<SnapPhrase> remainingKeyStatsPhrases = _remainingKeyStatsPhrases;
		_remainingKeyStatsPhrases = null;
		return remainingKeyStatsPhrases;
	}
	
	public Map<Integer, Double> getKeyProbs() {
		checkIsNormalized(true);
		return _keyProbs;
	}
	
	public Map<Integer, Double> getKeyConditionalExpectedWeights() {
		checkIsNormalized(true);
		return _keyConditionalExpectedWeights;
	}
	
	private void updateSnapId_MatchChecked(SnapPhrase phrase) {

		// calculate phrase rank
		double phraseRank = StatsRanks.calcPhraseRank(phrase, _minDateInc, _maxDateExc);
		
		// calculate extra keys rank
		double extraKeysRank = 1.0;
		List<Pair<Integer, Double>> keysSortedByExpectedWeight = getKeysSortedByExpectedWeight();
		final int length = Math.min(keysSortedByExpectedWeight.size(), BEST_ID_RANK_CHECK_KEYS_COUNT);
		for (int i=0; i<length; i++) {
			
			Pair<Integer, Double> pair = keysSortedByExpectedWeight.get(i);
			Integer keyId = pair.v1();
			Double weight = pair.v2();
			
			if (phrase.getFwdHitsMap().getAllKeyIds().contains(keyId)) {
				extraKeysRank *= weight;
			} else {
				extraKeysRank *= keysSortedByExpectedWeight.get(length-1).v2();
			}
		}
		
		// calculate snap rank
		double snapRank = phraseRank * extraKeysRank;
		if (phrase.getSnap().isScanned() && phrase.getSnap().isScannedImage()) {
			snapRank *= 2.0; // boost rank for snaps with images
		}
		
		// update snap id
		if (_snapRank < snapRank) {
			
			Snap newBestSnap = phrase.getSnap();
			_snapId = newBestSnap.getId();
			_snapTitle = newBestSnap.getTitle();
			_snapRank = snapRank;
		}
	}

	private boolean addPhraseStats(SnapPhrase phrase, Map<Integer, List<FwdHit>> foundSentenceFwdHitsMap) {

		List<FwdHit> uniqueFoundSentenceFwdHits 
			= FwdHitsUtils.getUniqueFwdHits(
				FwdHitsUtils.flattenFwdHitsMaps(
					foundSentenceFwdHitsMap));
		
		// calculate phrase rank
		double phraseRank = StatsRanks.calcPhraseRank(phrase, _minDateInc, _maxDateExc);
		
		// mark key hits as already counted 
		List<Pair<FwdHit, Double>> collectedFwdHitWeights = new ArrayList<>();
		FwdHitsMap fwdHitsMap = phrase.getFwdHitsMap();

		collectKeysStats(
				phrase.getSentenceBounds(),
				uniqueFoundSentenceFwdHits, 
				fwdHitsMap.get(FwdHitKind.PATTERN), 
				collectedFwdHitWeights);

		collectKeysStats(
				phrase.getSentenceBounds(),
				uniqueFoundSentenceFwdHits, 
				fwdHitsMap.get(FwdHitKind.WORD), 
				collectedFwdHitWeights);
		
		for (int i=0; i<collectedFwdHitWeights.size(); i++) {

			// get collected fwd hit weight
			Pair<FwdHit, Double> pair = collectedFwdHitWeights.get(i);
			FwdHit fwdHit = pair.v1();
			Double weight = pair.v2();

			// add observation of the hit's key id
			addKeyIdObservation(phraseRank, fwdHit.getKeyId(), weight);
		}

		_sumPhraseRank += phraseRank;
		
		// collect unstemming stats
		List<String>  leafSearches = null;
		StringBuilder hierSearchSB = null;
		Set<Integer>  hierSearchSBAddedKeyIds = null;
		StringBuilder fullSearchSB = null;
		Set<Integer>  fullSearchSBAddedKeyIds = null;
		for (int i=0; i<uniqueFoundSentenceFwdHits.size(); i++) {
			
			FwdHit fwdHit = uniqueFoundSentenceFwdHits.get(i);
			String str = phrase.getSnap().extractHitStr(phrase.getSentenceHit(), fwdHit.getHit());
			
			// add leaf search sample
			if (_leafSentenceKeyId != null && _leafSentenceKeyId.equals(fwdHit.getKeyId())) {
				if (leafSearches == null) {
					leafSearches = new ArrayList<>();
				}
				leafSearches.add(str);
			}
			
			// add hier search sample
			if (_hierSentenceKeyIds != null &&
				_hierSentenceKeyIds.size() > 0 &&
				_hierSentenceKeyIds.contains(fwdHit.getKeyId()) && 
				(hierSearchSBAddedKeyIds == null || 
				!hierSearchSBAddedKeyIds.contains(fwdHit.getKeyId()))) {
				
				if (hierSearchSB == null) {
					hierSearchSB = new StringBuilder();
				}
				if (hierSearchSB.length() > 0) {
					hierSearchSB.append(" ");
				}
				hierSearchSB.append(str);
				
				if (hierSearchSBAddedKeyIds == null) {
					hierSearchSBAddedKeyIds = new HashSet<>();
				}
				hierSearchSBAddedKeyIds.add(fwdHit.getKeyId());
			}
			
			// add full search sample
			if (_fullSentenceKeyIds != null &&
				_fullSentenceKeyIds.size() > 0 &&
				_fullSentenceKeyIds.contains(fwdHit.getKeyId()) && 
				(fullSearchSBAddedKeyIds == null || 
				!fullSearchSBAddedKeyIds.contains(fwdHit.getKeyId()))) {
				
				if (fullSearchSB == null) {
					fullSearchSB = new StringBuilder();
				}
				if (fullSearchSB.length() > 0) {
					fullSearchSB.append(" ");
				}
				fullSearchSB.append(str);
				
				if (fullSearchSBAddedKeyIds == null) {
					fullSearchSBAddedKeyIds = new HashSet<>();
				}
				fullSearchSBAddedKeyIds.add(fwdHit.getKeyId());
			}
		}
		if (leafSearches != null) {
			for (int i=0; i<leafSearches.size(); i++) {
				
				String leafSearch = leafSearches.get(i);
				String lowercaseSearch = leafSearch.toLowerCase();
				
				_unstemLeafSearchLowercase.add(0, lowercaseSearch, phraseRank);
				_unstemLeafSearchOriginal.add(lowercaseSearch, leafSearch, phraseRank);
			}
		}
		if (hierSearchSB != null) {

			String hierSearch = hierSearchSB.toString();
			String lowercaseSearch = hierSearch.toLowerCase();
			
			_unstemHierSearchLowercase.add(0, lowercaseSearch, phraseRank);
			_unstemHierSearchOriginal.add(lowercaseSearch, hierSearch, phraseRank);
		}
		if (fullSearchSB != null) {

			String fullSearch = fullSearchSB.toString();
			String lowercaseSearch = fullSearch.toLowerCase();
			
			_unstemFullSearchLowercase.add(0, lowercaseSearch, phraseRank);
			_unstemFullSearchOriginal.add(lowercaseSearch, fullSearch, phraseRank);
		}

		return true;
	}
	
	private final void collectKeysStats(
			Hit sentenceBounds,
			List<FwdHit> foundFwdHits,
			List<FwdHit> collectFromFwdHits,
			List<Pair<FwdHit, Double>> collectedFwdHitWeights) {
		
		if (collectFromFwdHits == null) {
			return;
		}
		
		// loop through provided hits
		for (int i=0; i<collectFromFwdHits.size(); i++) {
			
			FwdHit fwdHit = collectFromFwdHits.get(i);
			Integer keyId = fwdHit.getKeyId();
			
			// check not a stop key id
			if (_stopKeyIds != null && _stopKeyIds.contains(keyId)) {
				continue;
			}
			
			// check does not overlap with found key hits
			if (FwdHitsUtils.overlapsAny(foundFwdHits, fwdHit)) {
				continue;
			}

			// check does not overlap with already counted
			if (FwdHitsUtils.overlapsAnyPair(collectedFwdHitWeights, fwdHit)) {
				continue;
			}

			Hit hit = fwdHit.getHit();
			
			int minDistToPathKey = Integer.MAX_VALUE;
			if (foundFwdHits.size() > 0) {
				for (int j=0; j<foundFwdHits.size(); j++) {
					Hit foundHit = foundFwdHits.get(j).getHit();
					int dist = hit.distanceTo(foundHit);
					if (minDistToPathKey > dist) {
						minDistToPathKey = dist;
					}
					if (minDistToPathKey < 0) {
						throw new InternalError("This hit should not be added because it overlaps with key hit");
					}
					if (minDistToPathKey <= 1) {
						break; // already the closest (before becoming same word)
					}
				}
			} else {
				// measure from sentence start
				minDistToPathKey = Math.abs(hit.start() - sentenceBounds.start());
			}
			
			// calculate weight for the location of extra hit
			Double keyWeight = StatsUtils.calcDistanceWeightExponential(minDistToPathKey, STEM_WEIGHT_DECAY_CHAR_HALF_LIFE);
			
			// mark as already counted
			collectedFwdHitWeights.add(new Pair<FwdHit, Double>(fwdHit, keyWeight));
		}
	}
	
	private void addKeyIdObservation(double phraseProb, Integer keyId, double keyWeight) {
		
		{ // update sum key weight
			
			Double sumKeyProb = _keyProbs.get(keyId);
			if (sumKeyProb == null) {
				sumKeyProb = 0.0;
			}
			// add *phrase* prob
			sumKeyProb += phraseProb;
			_keyProbs.put(keyId, sumKeyProb);
		}
		
		{ // update sum key x phrase weight
			
			Double sumKeyPhraseWeight = _keyConditionalExpectedWeights.get(keyId);
			if (sumKeyPhraseWeight == null) {
				sumKeyPhraseWeight = 0.0;
			}
			sumKeyPhraseWeight += phraseProb * keyWeight;
			_keyConditionalExpectedWeights.put(keyId, sumKeyPhraseWeight);
		}
	}

	private final void checkIsNormalized(boolean yes) {
		if (yes) {
			if (_isNormalized == false) {
				throw new IllegalStateException("Cannot call this method, first call normalize()");
			}
		} else {
			if (_isNormalized) {
				throw new IllegalStateException("Cannot call this method, already normalized");
			}
		}
	}

	private void normalize() {
		
		checkIsNormalized(false);

		if (_sumPhraseRank > 0) {

			for (Integer keyId : _keyProbs.keySet()) {

				Double sumKeyWeight = _keyProbs.get(keyId);
				_keyProbs.put(keyId, 
						sumKeyWeight / 
						_sumPhraseRank);
				
				Double sumKeyPhraseWeight = _keyConditionalExpectedWeights.get(keyId);
				_keyConditionalExpectedWeights.put(keyId, 
						sumKeyPhraseWeight / 
						sumKeyWeight);
			}
		}
		
		// calculate aggregated stats
		SelectK<Integer, Double> stemsByProbSelectK = new SelectK<>(SortOrder.Desc, STATS_SELECT_TOP_STEMS_COUNT);
		SelectK<Integer, Double> stemsByExpectedWeightSelectK = new SelectK<>(SortOrder.Desc, STATS_SELECT_TOP_STEMS_COUNT);
		for (Integer keyId : _keyProbs.keySet()) {
		
			// get key probability
			Double keyProb = _keyProbs.get(keyId);
			
			// get key conditional expected weight
			Double keyConditionalExpectedWeight = _keyConditionalExpectedWeights.get(keyId);
			
			// calculate key expected weight
			Double keyExpectedWeight = keyProb * keyConditionalExpectedWeight;
			
			// add for selecting top items
			stemsByProbSelectK.add(new Pair<Integer, Double>(keyId, keyProb));
			stemsByExpectedWeightSelectK.add(new Pair<Integer, Double>(keyId, keyExpectedWeight));
		}
		_keysSortedByProb = stemsByProbSelectK.get();
		_keysSortedByExpectedWeight = stemsByExpectedWeightSelectK.get();
		
		// unstem leaf search
		if (_leafSentenceKeyId != null) {
			
			_unstemLeafSearchLowercase.optimize();
			_unstemLeafSearchOriginal.optimize();
			String lowercaseSearch = _unstemLeafSearchLowercase.getWordsByKey().get(0);
			_leafSearch = _unstemLeafSearchOriginal.getWordsByKey().get(lowercaseSearch);
			_unstemLeafSearchLowercase = null;
			_unstemLeafSearchOriginal = null;
		}
		
		// unstem hier search
		if (_hierSentenceKeyIds != null && _hierSentenceKeyIds.size() > 0) {
			
			_unstemHierSearchLowercase.optimize();
			_unstemHierSearchOriginal.optimize();
			String lowercaseSearch = _unstemHierSearchLowercase.getWordsByKey().get(0);
			_hierSearch = _unstemHierSearchOriginal.getWordsByKey().get(lowercaseSearch);
			_unstemHierSearchLowercase = null;
			_unstemHierSearchOriginal = null;
		}
		
		// unstem full search
		if (_fullSentenceKeyIds != null && _fullSentenceKeyIds.size() > 0) {
			
			_unstemFullSearchLowercase.optimize();
			_unstemFullSearchOriginal.optimize();
			String lowercaseSearch = _unstemFullSearchLowercase.getWordsByKey().get(0);
			_fullSearch = _unstemFullSearchOriginal.getWordsByKey().get(lowercaseSearch);
			_unstemFullSearchLowercase = null;
			_unstemFullSearchOriginal = null;
		}
		
		_isNormalized = true;
	}
	
	public List<Pair<Integer, Double>> getKeysSortedByProb() {
		checkIsNormalized(true);
		return _keysSortedByProb;
	}
	
	public List<Pair<Integer, Double>> getKeysSortedByExpectedWeight() {
		checkIsNormalized(true);
		return _keysSortedByExpectedWeight;
	}
	
	public String getFullSearch() {
		return _fullSearch;
	}
	
	public String getHierSearch() {
		return _hierSearch;
	}
	
	public String getLeafSearch() {
		return _leafSearch;
	}
	
	public String toString() {
		
		checkIsNormalized(true);

		StringBuilder sb = new StringBuilder();
		
		sb.append("Search: ");
		sb.append(_fullSearch);
		sb.append("\n");
		sb.append("SumRank: ");
		sb.append(FMT_PROB.format(_sumPhraseRank));
		sb.append("\n");
		sb.append("Snap#: ");
		sb.append(_matchingSnapIds.size());
		sb.append("\n");
		
		return sb.toString();
	}

}
