package com.readrz.summr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.types.ObjectId;

import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;

public final class GroupStats {
	
	private final Map<Integer, Integer> _snapCountByKeyId;
	
	public GroupStats(List<Snap> snaps, List<Integer> documentKeyIds) {
		
		final Map<Integer, Set<ObjectId>> snapIdsByKeyId = new HashMap<>();
		
		for (int i=0; i<snaps.size(); i++) {
			
			Snap snap = snaps.get(i);
			ObjectId snapId = snap.getId();
			
			FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
			while (fwdHits.nextSentence()) {

				// get sentence data
				FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(FwdHitKind.ALL);

				// get found fwd hits
				Map<Integer, List<FwdHit>> foundDocumentFwdHitsMap = new HashMap<>();
				fwdHitsMap.matchesSearch(documentKeyIds, foundDocumentFwdHitsMap, null, null, null, null);
				
				{ // find non overlapping *pattern* group hits
					
					List<FwdHit> patternGroupFwdHits = fwdHitsMap.get(FwdHitKind.PATTERN_GROUP);
					if (patternGroupFwdHits != null) {
						for (int j=0; j<patternGroupFwdHits.size(); j++) {
							FwdHit fwdHit = patternGroupFwdHits.get(j);
							if (!FwdHitsUtils.overlapsAny(foundDocumentFwdHitsMap, fwdHit)) {
								Set<ObjectId> snapIds = snapIdsByKeyId.get(fwdHit.getKeyId());
								if (snapIds == null) {
									snapIds = new HashSet<>();
									snapIdsByKeyId.put(fwdHit.getKeyId(), snapIds);
								}
								snapIds.add(snapId);
							}
						}
					}
				}
				
				{ // find non overlapping *topic* group hits
					
					List<FwdHit> topicGroupFwdHits = fwdHitsMap.get(FwdHitKind.TOPIC_GROUP);
					if (topicGroupFwdHits != null) {
						for (int j=0; j<topicGroupFwdHits.size(); j++) {
							FwdHit fwdHit = topicGroupFwdHits.get(j);
							if (!FwdHitsUtils.overlapsAny(foundDocumentFwdHitsMap, fwdHit)) {
								Set<ObjectId> snapIds = snapIdsByKeyId.get(fwdHit.getKeyId());
								if (snapIds == null) {
									snapIds = new HashSet<>();
									snapIdsByKeyId.put(fwdHit.getKeyId(), snapIds);
								}
								snapIds.add(snapId);
							}
						}
					}
				}
			}
		}
		
		// collect snap counts
		_snapCountByKeyId = new HashMap<>();
		for (Entry<Integer, Set<ObjectId>> entry : snapIdsByKeyId.entrySet()) {
			_snapCountByKeyId.put(entry.getKey(), entry.getValue().size());
		}
	}
	
	public Integer getSnapCount(Integer keyId) {
		return _snapCountByKeyId.get(keyId);
	}
	
	public Map<Integer, Integer> getSnapCountByKeyId() {
		return _snapCountByKeyId;
	}

}
