package com.readrz.data;

import java.util.List;
import java.util.Map;

import me.akuz.core.Hit;

import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.search.QueryKeyIds;

public final class SnapPhrase {
	
	private final Snap _snap;
	private final SnapPhraseSet _snapPhraseSet;
	private final Hit _sentenceHit;
	private final Hit _sentenceBounds;
	private final FwdHitsMap _fwdHitsMap;
	
	public SnapPhrase(final Snap snap, final SnapPhraseSet snapPhraseSet, final Hit sentenceHit, final FwdHitsMap fwdHitsMap) {
		_snap = snap;
		_snapPhraseSet = snapPhraseSet;
		_sentenceHit = sentenceHit;
		_sentenceBounds = sentenceHit.start() < 0 ? sentenceHit.shift(snap.getTitle().length()) : sentenceHit;
		_fwdHitsMap = fwdHitsMap;
	}
	
	public Snap getSnap() {
		return _snap;
	}
	
	public SnapPhraseSet getSnapPhraseSet() {
		return _snapPhraseSet;
	}
	
	public Hit getSentenceHit() {
		return _sentenceHit;
	}
	
	public Hit getSentenceBounds() {
		return _sentenceBounds;
	}
	
	public FwdHitsMap getFwdHitsMap() {
		return _fwdHitsMap;
	}
	
	public boolean checkMatchesQuery(QueryKeyIds queryKeyIds) {
		return checkMatchesQuery(queryKeyIds, null, null, null);
	}
	
	public boolean checkMatchesQuery(QueryKeyIds queryKeyIds, Map<Integer, List<FwdHit>> outSentenceFwdHitsMap) {
		return checkMatchesQuery(queryKeyIds, null, null, outSentenceFwdHitsMap);
	}
		
	public boolean checkMatchesQuery(
			QueryKeyIds queryKeyIds, 
			Map<Integer, List<FwdHit>> outDocumentFwdHitsMap, 
			Map<Integer, List<FwdHit>> outSenCheckFwdHitsMap, 
			Map<Integer, List<FwdHit>> outSentenceFwdHitsMap) {

		{ // check phrase satisfies *document* search

			List<Integer> documentKeyIds = queryKeyIds.getDocumentKeyIds();
			if (documentKeyIds != null && documentKeyIds.size() > 0) {
				if (!_snapPhraseSet.getAllKeyIds().containsAll(documentKeyIds)) {
					return false;
				}
			}
		}
		
		{ // check phrase satisfies *sentence* search

			return _fwdHitsMap.matchesSearch(
					queryKeyIds.getDocumentKeyIds(),
					outDocumentFwdHitsMap,
					queryKeyIds.getSenCheckKeyIds(), 
					outSenCheckFwdHitsMap,
					queryKeyIds.getSentenceKeyIds(), 
					outSentenceFwdHitsMap);
		}
	}
	
}
