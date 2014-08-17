package com.readrz.data.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Alias class for a map of forward hits by their kind.
 *
 */
public final class FwdHitsMap {

	private final Map<Integer, List<FwdHit>> _byKeyId;
	private final Map<FwdHitKind, List<FwdHit>> _byKind;
	
	public FwdHitsMap() {
		_byKeyId = new HashMap<>();
		_byKind = new HashMap<>();
	}
	
	public Set<Integer> getAllKeyIds() {
		return _byKeyId.keySet();
	}
	
	public Map<Integer, List<FwdHit>> getByKeyId() {
		return _byKeyId;
	}
	
	public Map<FwdHitKind, List<FwdHit>> getByKind() {
		return _byKind;
	}
	
	public int size() {
		return _byKind.size();
	}
	
	public Set<FwdHitKind> keySet() {
		return _byKind.keySet();
	}
	
	public Set<Entry<FwdHitKind, List<FwdHit>>> entrySet() {
		return _byKind.entrySet();
	}
	
	public List<FwdHit> get(FwdHitKind kind) {
		return _byKind.get(kind);
	}
	
	public void put(FwdHitKind kind, List<FwdHit> list) {
		for (int i=0; i<list.size(); i++) {
			FwdHit fwdHit = list.get(i);
			List<FwdHit> byKeyList = _byKeyId.get(fwdHit.getKeyId());
			if (byKeyList == null) {
				byKeyList = new ArrayList<>();
				_byKeyId.put(fwdHit.getKeyId(), byKeyList);
			}
			byKeyList.add(fwdHit);
		}
		_byKind.put(kind, list);
	}
	
	public boolean matchesSearch( 
			List<Integer> documentKeyIds, 
			Map<Integer, List<FwdHit>> outDocumentFwdHitsMap,
			List<Integer> senCheckKeyIds, 
			Map<Integer, List<FwdHit>> outSenCheckFwdHitsMap, 
			List<Integer> sentenceKeyIds, 
			Map<Integer, List<FwdHit>> outSentenceFwdHitsMap) {

		// if there are no key ids to check...
		if ((documentKeyIds == null || documentKeyIds.size() == 0) &&
			(senCheckKeyIds == null || senCheckKeyIds.size() == 0) &&
			(sentenceKeyIds == null || sentenceKeyIds.size() == 0)) {
			
			// there are no requirements
			return true;
		}
		
		// check and collect senCheck fwd hits
		if (senCheckKeyIds != null && senCheckKeyIds.size() > 0) {
			for (int i=0; i<senCheckKeyIds.size(); i++) {
				Integer keyId = senCheckKeyIds.get(i);
				List<FwdHit> list = _byKeyId.get(keyId);
				if (list == null || list.size() == 0) {
					// does not match sentence search
					return false;
				}
				if (outSenCheckFwdHitsMap != null) {
					outSenCheckFwdHitsMap.put(keyId, list);
				}
			}
		}

		// check and collect sentence fwd hits
		int foundSentenceKeysFwdHitCount = 0;
		if (sentenceKeyIds != null && sentenceKeyIds.size() > 0) {
			for (int i=0; i<sentenceKeyIds.size(); i++) {
				Integer keyId = sentenceKeyIds.get(i);
				List<FwdHit> list = _byKeyId.get(keyId);
				if (list == null || list.size() == 0) {
					// does not match sentence search
					return false;
				}
				if (outSentenceFwdHitsMap == null) {
					outSentenceFwdHitsMap = new HashMap<>();
				}
				if (!outSentenceFwdHitsMap.containsKey(keyId)) {
					outSentenceFwdHitsMap.put(keyId, list);
					foundSentenceKeysFwdHitCount += list.size();
				}
			}
		}
		
		// collect document fwd hits
		if (documentKeyIds != null && documentKeyIds.size() > 0) {
			if (outDocumentFwdHitsMap != null) {
				for (int i=0; i<documentKeyIds.size(); i++) {
					Integer keyId = documentKeyIds.get(i);
					List<FwdHit> list = _byKeyId.get(keyId);
					if (list != null) {
						outDocumentFwdHitsMap.put(keyId, list);
					}
				}
			}
		}
		
		// if searching for sentence keys
		if (sentenceKeyIds != null && sentenceKeyIds.size() > 0) {

			// check if found enough sentence keys found
			if (foundSentenceKeysFwdHitCount < sentenceKeyIds.size()) {
				return false;
			}
			
			// check if there is a non-overlapping combination
			List<List<FwdHit>> stackFwdHitsList = new ArrayList<>();
			for (int i=0; i<sentenceKeyIds.size(); i++) {
				Integer keyId = sentenceKeyIds.get(i);
				stackFwdHitsList.add(outSentenceFwdHitsMap.get(keyId));
			}
			int[] stackIndex = new int[stackFwdHitsList.size()];
			
			while (true) {
					
				// check if combination is non-overlapping
				boolean isCombinationNonOverlapping = true;
				for (int i=1; i<stackIndex.length; i++) {
					
					// get stack hit
					int index = stackIndex[i];
					FwdHit stackHit = stackFwdHitsList.get(i).get(index);
					
					// check against other hits on stack
					for (int j=0; j<i; j++) {
						
						int otherIndex = stackIndex[j];
						FwdHit otherStackHit = stackFwdHitsList.get(j).get(otherIndex);
						
						// check if overlaps with other hit on stack
						if (stackHit.getHit().overlaps(otherStackHit.getHit())) {
							isCombinationNonOverlapping = false;
							break;
						}
					}
					
					// check if no need to check more
					if (!isCombinationNonOverlapping) {
						break;
					}
				}
				
				// check if found non-overlapping combination
				if (isCombinationNonOverlapping) {
					break;
				}
				
				// continue checking next combinations
				boolean isMovedToOtherCombination = false;
				for (int i=0; i<stackIndex.length; i++) {
					
					// get stack hits
					int index = stackIndex[i];
					List<FwdHit> stackFwdHits = stackFwdHitsList.get(i);
					
					// check if can move
					if (index < stackFwdHits.size() - 1) {
						
						stackIndex[i] = index + 1;
						
						for (int j=0; j<i; j++) {
							stackIndex[j] = 0;
						}
						
						isMovedToOtherCombination = true;
						break;
					}
				}
				
				// check if moved to next combination
				if (!isMovedToOtherCombination) {
					return false;
				}
				
			} // non-overlap check - while(true)

		} // if searching for sentence keys
		
		// all checks passed
		return true;
	}
	
}
