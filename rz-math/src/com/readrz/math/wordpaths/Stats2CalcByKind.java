package com.readrz.math.wordpaths;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.readrz.data.SnapPhrase;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;
import com.readrz.search.QueryKeyIds;

/**
 * Calculates *sentence extra* items for given FwdHitKinds, 
 * arranged by FwdKeyKind, and then by keyId.
 *
 */
public final class Stats2CalcByKind {
	
	private final String _logPathsIdString;
	private final Map<FwdHitKind, Map<Integer, PathsNode>> _nodes;
	
	public Stats2CalcByKind(
			String logPathsIdString,
			Set<ObjectId> parentSnapIds,
			List<SnapPhrase> phrases,
			QueryKeyIds queryKeyIds, 
			Date minDateInc,
			Date maxDateExc,
			Set<Integer> stopKeyIds,
			int[] levelDepths,
			int maxLevel,
			int level,
			FwdHitKind... fwdHitKinds) {
		
		_logPathsIdString = logPathsIdString;
		_nodes = new HashMap<>();
		
		for (int i=0; i<phrases.size(); i++) {
			
			SnapPhrase phrase = phrases.get(i);
			
			Map<Integer, List<FwdHit>> foundSentenceFwdHitsMap = new HashMap<>();
			if (phrase.checkMatchesQuery(queryKeyIds, foundSentenceFwdHitsMap)) {
				
				FwdHitsMap fwdHitsMap = phrase.getFwdHitsMap();
				
				for (int k=0; k<fwdHitKinds.length; k++) {
					
					FwdHitKind fwdHitKind = fwdHitKinds[k];
					
					List<FwdHit> fwdHits = fwdHitsMap.get(fwdHitKind);
					if (fwdHits != null && fwdHits.size() > 0) {
						
						for (int j=0; j<fwdHits.size(); j++) {
							
							FwdHit fwdHit = fwdHits.get(j);
							
							if (FwdHitsUtils.overlapsAny(foundSentenceFwdHitsMap, fwdHit)) {
								continue;
							}
							
							Map<Integer, PathsNode> itemsByKeyId = _nodes.get(fwdHitKind);
							if (itemsByKeyId == null) {
								itemsByKeyId = new HashMap<>();
								_nodes.put(fwdHitKind, itemsByKeyId);
							}
							PathsNode pathsNode = itemsByKeyId.get(fwdHit.getKeyId());
							if (pathsNode == null) {

//								StopWatch sw = new StopWatch();
//
//								sw.reset();
//								sw.start();

								QueryKeyIds itemQueryKeyIds = queryKeyIds.clone();
								itemQueryKeyIds.addSentenceKeyId(fwdHit.getKeyId());
								
								List<Integer> hierSentenceKeyIds = new ArrayList<>();
								hierSentenceKeyIds.add(fwdHit.getKeyId());
								
								Stats2Item statsItem
									= new Stats2Item(
										logPathsIdString,
										parentSnapIds,
										phrases,
										phrases,
										itemQueryKeyIds, 
										hierSentenceKeyIds,
										minDateInc,
										maxDateExc,
										stopKeyIds,
										levelDepths,
										maxLevel,
										level);
								
								pathsNode = new PathsNode(
										statsItem.getFullSentenceKeyIds(), 
										statsItem.getHierSentenceKeyIds(),
										statsItem.getLeafSentenceKeyId(),
										statsItem.getFullSearch(), 
										statsItem.getHierSearch(), 
										statsItem.getLeafSearch(),
										statsItem.getSnapCount(),
										statsItem.getSnapId(),
										statsItem.getChildren());
								
								itemsByKeyId.put(fwdHit.getKeyId(), pathsNode);
								
//								sw.stop();
//								System.out.println("---- " + _logPathsIdString + ": " + sw.getTime() + " ms used for '" + fwdHitKind + "' item '" + item.getExtraSearch() + "'");
							}
						}
					}
				}
			}
		}
	}

	public Map<Integer, PathsNode> getNodes(FwdHitKind fwdHitKind) {
		return _nodes.get(fwdHitKind);
	}

	public String getLogPathsIdString() {
		return _logPathsIdString;
	}
}
