package com.readrz.math.wordpaths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.readrz.data.SnapPhrase;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;
import com.readrz.search.QueryKeyIds;

/**
 * Calculates simple level 1 stats for given FwdHitKinds, 
 * arranged by FwdKeyKind, and then by keyId.
 *
 */
public final class Stats1CalcByKind {
	
	private final Map<FwdHitKind, Map<Integer, Stats1Item>> _items;
	
	public Stats1CalcByKind(
			List<SnapPhrase> phrases,
			boolean isPhraseMatchChecked,
			QueryKeyIds queryKeyIds,
			boolean isSentenceExtra,
			FwdHitKind... fwdHitKinds) {
		
		_items = new HashMap<>();
		
		for (int i=0; i<phrases.size(); i++) {
			
			SnapPhrase phrase = phrases.get(i);
			ObjectId snapId = phrase.getSnap().getId();
			
			// only initialize output map, if we need sentence hits
			Map<Integer, List<FwdHit>> foundSentenceFwdHitsMap = null;
			if (isSentenceExtra) {
				foundSentenceFwdHitsMap = new HashMap<>();
			}
			
			// if phrase match already checked AND we don't need
			// to find "sentence extra" hits, then don't call 
			// the method that checks if phrase matches query
			// and finds sentence hits (because we need none)
			if ((isPhraseMatchChecked && isSentenceExtra == false) || 
				phrase.checkMatchesQuery(queryKeyIds, foundSentenceFwdHitsMap)) {
				
				FwdHitsMap fwdHitsMap = phrase.getFwdHitsMap();
				
				for (int k=0; k<fwdHitKinds.length; k++) {
					
					FwdHitKind fwdHitKind = fwdHitKinds[k];
					
					List<FwdHit> fwdHits = fwdHitsMap.get(fwdHitKind);
					if (fwdHits != null && fwdHits.size() > 0) {
						
						for (int l=0; l<fwdHits.size(); l++) {
							
							FwdHit fwdHit = fwdHits.get(l);
							
							// do not count any hits that 
							// overlap with sentence hits when
							// in "is sentence extra" mode
							if (isSentenceExtra && 
								FwdHitsUtils.overlapsAny(foundSentenceFwdHitsMap, fwdHit)) {
								continue;
							}
							
							Map<Integer, Stats1Item> itemsByKeyId = _items.get(fwdHitKind);
							if (itemsByKeyId == null) {
								itemsByKeyId = new HashMap<>();
								_items.put(fwdHitKind, itemsByKeyId);
							}
							Stats1Item item = itemsByKeyId.get(fwdHit.getKeyId());
							if (item == null) {
								item = new Stats1Item(fwdHit.getKeyId());
								itemsByKeyId.put(fwdHit.getKeyId(), item);
							}
							item.addSnap(snapId);
						}
					}
				}
			}
		}
	}
	
	public Map<Integer, Stats1Item> getItems(FwdHitKind fwdHitKind) {
		return _items.get(fwdHitKind);
	}

}
