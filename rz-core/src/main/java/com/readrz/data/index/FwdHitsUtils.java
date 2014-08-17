package com.readrz.data.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Pair;


public final class FwdHitsUtils {

	public static final boolean overlapsAny(List<FwdHit> list, FwdHit fwdHit) {
		if (list == null) {
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).getHit().overlaps(fwdHit.getHit())) {
				return true;
			}
		}
		return false;
	}

	public static final boolean overlapsAny(Map<Integer, List<FwdHit>> map, FwdHit fwdHit) {
		if (map == null) {
			return false;
		}
		for (List<FwdHit> list : map.values()) {
			if (overlapsAny(list, fwdHit)) {
				return true;
			}
		}
		return false;
	}

	public static final boolean overlapsAnyPair(List<Pair<FwdHit, Double>> list, FwdHit fwdHit) {
		if (list == null) {
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).v1().getHit().overlaps(fwdHit.getHit())) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean overlapsAny(List<FwdHit> list, FwdHit fwdHit, int minIndex, int maxIndex) {
		if (list == null) {
			return false;
		}
		final int checkMinIndex = Math.max(0, minIndex);
		final int checkMaxIndex = Math.min(list.size()-1, maxIndex);
		for (int i=checkMinIndex; i<=checkMaxIndex; i++) {
			if (list.get(i).getHit().overlaps(fwdHit.getHit())) {
				return true;
			}
		}
		return false;
	}

	public static final List<FwdHit> getUniqueFwdHitsByPriority(
			List<FwdHit> groupFwdHits, 
			List<FwdHit> entityFwdHits, 
			List<FwdHit> wordsFwdHits) {
		
		List<FwdHit> uniqueFwdHits = new ArrayList<>();
		
		// convert group id hits
		if (groupFwdHits != null) {
			for (int i=0; i<groupFwdHits.size(); i++) {

				FwdHit fwdHit = groupFwdHits.get(i);
				uniqueFwdHits.add(fwdHit);
			}
		}
		
		// convert pattern hits
		if (entityFwdHits != null) {
			for (int i=0; i<entityFwdHits.size(); i++) {

				FwdHit fwdHit = entityFwdHits.get(i);

				// don't include patterns that overlap with group ids
				if (groupFwdHits != null && overlapsAny(groupFwdHits, fwdHit)) {
					continue;
				}
				
				uniqueFwdHits.add(fwdHit);
			}
		}

		// convert word hits
		if (wordsFwdHits != null) {
			for (int i=0; i<wordsFwdHits.size(); i++) {
				
				FwdHit fwdHit = wordsFwdHits.get(i);
				
				// don't include co-occurrences
				if (overlapsAny(wordsFwdHits, fwdHit, i+1, i+2)) {
					continue;
				}
				
				// don't include words that overlap with group ids
				if (groupFwdHits != null && overlapsAny(groupFwdHits, fwdHit)) {
					continue;
				}
				
				// don't include words that overlap with entities
				if (entityFwdHits != null && overlapsAny(entityFwdHits, fwdHit)) {
					continue;
				}
				
				uniqueFwdHits.add(fwdHit);
			}
		}
		
		// sort before returning
		Collections.sort(uniqueFwdHits);	

		return uniqueFwdHits;
	}
	
	@SafeVarargs
	public static final int countFwdHits(Map<Integer, List<FwdHit>>... mapArr) {
		
		int result = 0;
		
		if (mapArr != null) {
			for (int i=0; i<mapArr.length; i++) {
				if (mapArr[i] != null) {
					for (List<FwdHit> list : mapArr[i].values()) {
						result += list.size();
					}
				}
			}
		}
		
		return result;
	}
	
	@SafeVarargs
	public static final List<FwdHit> flattenFwdHitsMaps(Map<Integer, List<FwdHit>>... mapArr) {
		
		List<FwdHit> result = new ArrayList<>();
		
		if (mapArr != null) {
			for (int i=0; i<mapArr.length; i++) {
				if (mapArr[i] != null) {
					for (List<FwdHit> list : mapArr[i].values()) {
						result.addAll(list);
					}
				}
			}
		}
		
		return result;
	}

	@SafeVarargs
	public static final List<FwdHit> getUniqueFwdHits(List<FwdHit>... fwdHitsArr) {
		
		List<FwdHit> sortedFwdHits = new ArrayList<>();
		if (fwdHitsArr != null) {
			for (int i=0; i<fwdHitsArr.length; i++) {
				if (fwdHitsArr[i] != null) {
					for (int j=0; j<fwdHitsArr[i].size(); j++) {
						sortedFwdHits.add(fwdHitsArr[i].get(j));
					}
				}
			}
		}
		if (sortedFwdHits.size() > 1) {
			Collections.sort(sortedFwdHits, new FwdHitsSorter());
		}
		
		List<FwdHit> uniqueFwdHits = new ArrayList<>();
		for (int i=0; i<sortedFwdHits.size(); i++) {
			
			FwdHit currFwdHit = sortedFwdHits.get(i);
			
			boolean overlaps = false;
			for (int j=0; j<uniqueFwdHits.size(); j++) {
				
				FwdHit prevFwdHit = uniqueFwdHits.get(j);
				if (prevFwdHit.getHit().overlaps(currFwdHit.getHit())) {
					overlaps = true;
					break;
				}
			}
			
			if (!overlaps) {
				uniqueFwdHits.add(currFwdHit);
			}
		}
		
		return uniqueFwdHits;
	}

	public static final List<FwdHit> getUniqueHitsForTopics(
			boolean populateListPatterns,
			List<FwdHit> patternHits, 
			List<FwdHit> wordHits,
			KeysIndex keysIndex,
			Set<Integer> stopKeyIds) {
		
		// select pattern hits
		List<FwdHit> selectedPatternHits = null;
		if (patternHits != null && patternHits.size() > 0) {
			
			selectedPatternHits = new ArrayList<>();
			for (int i=0; i<patternHits.size(); i++) {
				
				FwdHit patternHit = patternHits.get(i);
				
				int keyId = patternHit.getKeyId();
				if (stopKeyIds != null && stopKeyIds.contains(keyId)) {
					continue; // stop key id
				}
	
				String stem = keysIndex.getStrCached(keyId);
				if (stem == null) {
					continue; // unknown stem
				}
	
				if (populateListPatterns == false) {
					if (stem.startsWith("e/lists")) {
						continue; // no list patterns
					}
				}
	
				selectedPatternHits.add(patternHit);
			}
		}
	
		// select word hits
		List<FwdHit> selectedWordHits = null;
		if (wordHits != null && wordHits.size() > 0) {
			
			selectedWordHits = new ArrayList<>();
			for (int i=0; i<wordHits.size(); i++) {
				
				FwdHit wordHit = wordHits.get(i);
				
				int keyId = wordHit.getKeyId();
				if (stopKeyIds != null && stopKeyIds.contains(keyId)) {
					continue; // stop key id
				}
				
				String stem = keysIndex.getStrCached(keyId);
				if (stem == null) {
					continue; // unknown stem
				}
				
				selectedWordHits.add(wordHit);
			}
		}
	
		List<FwdHit> uniqueFwdHits = getUniqueFwdHitsByPriority(null, selectedPatternHits, selectedWordHits);
		return uniqueFwdHits;
	}


}
