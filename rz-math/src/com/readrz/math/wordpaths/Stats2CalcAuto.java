package com.readrz.math.wordpaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.math.WeightedAverage;
import me.akuz.core.sort.SelectK;

import org.bson.types.ObjectId;

import com.readrz.data.Snap;
import com.readrz.data.SnapPhrase;
import com.readrz.search.QueryKeyIds;

/**
 * Calculates extra sentence items.
 *
 */
public final class Stats2CalcAuto {

	private final String _logPathsIdString;
	private final List<PathsNode> _nodes;
	
	public Stats2CalcAuto(
			String logPathsIdString,
			List<SnapPhrase> phrases,
			Set<ObjectId> parentSnapIds,
			QueryKeyIds queryKeyIds,
			Stats2Item parent,
			Date minDateInc,
			Date maxDateExc, 
			Set<Integer> stopKeyIds,
			int[] levelDepths,
			int maxLevel,
			int level) {
		
		_logPathsIdString = logPathsIdString;
		List<Stats2Item> autoItems = new ArrayList<>();
		List<SnapPhrase> remainingKeyStatsPhrases = phrases;
		final int maxNodeCount = levelDepths[level];

		String noParentFullSearch = null;
		
		int counter = -1;
		while (true) {
			
			counter++;
			
//			StopWatch sw = new StopWatch();

//			sw.reset();
//			sw.start();

			// calculate remaining stats
			Set<ObjectId> emptySnapIds = new HashSet<>();
			Stats2Item remainingItem = new Stats2Item(
					logPathsIdString,
					emptySnapIds,
					remainingKeyStatsPhrases,
					phrases,
					queryKeyIds, 
					null, 
					minDateInc, 
					maxDateExc, 
					stopKeyIds,
					levelDepths,
					maxLevel,
					level);
			
//			sw.stop();
//			System.out.println("---- " + _logPathsIdString + ": " + sw.getTime() + " ms used for remaining stats");
			
			// get no parent full search
			if (counter == 0 && parent == null) {
				noParentFullSearch = remainingItem.getFullSearch();
			}

			// stop spawning children when too little probability left
			if (remainingItem.getSumPhraseRank() < PathsConst.AUTO_SUM_PHRASE_PROB_TO_BREAK) {
				break;
			}

			// find new child
			boolean childAdded = false;
			List<Pair<Integer, Double>> keysSortedByExpectedWeight = remainingItem.getKeysSortedByExpectedWeight();
			for (int i=0; i < keysSortedByExpectedWeight.size(); i++) {
				
				Pair<Integer, Double> pair = keysSortedByExpectedWeight.get(i);
				Integer considerExtraSentenceKeyId = pair.v1();
				
				if (stopKeyIds != null && stopKeyIds.contains(considerExtraSentenceKeyId)) {
					continue;
				}
				
				QueryKeyIds considerQueryKeyIds = queryKeyIds.clone();
				considerQueryKeyIds.addSentenceKeyId(considerExtraSentenceKeyId);
				
				List<Integer> considerHierSentenceKeyIds;
				if (parent != null && parent.getHierSentenceKeyIds() != null) {
					considerHierSentenceKeyIds = new ArrayList<>(parent.getHierSentenceKeyIds());
				} else {
					considerHierSentenceKeyIds = new ArrayList<>();
				}
				considerHierSentenceKeyIds.add(considerExtraSentenceKeyId);
				
//				sw.reset();
//				sw.start();

				Stats2Item considerItem = new Stats2Item(
						logPathsIdString,
						parentSnapIds,
						remainingKeyStatsPhrases,
						phrases,
						considerQueryKeyIds,
						considerHierSentenceKeyIds,
						minDateInc, 
						maxDateExc, 
						stopKeyIds,
						levelDepths,
						maxLevel,
						level);

//				sw.stop();
//				System.out.println("---- " + _logPathsIdString + ": " + sw.getTime() + " ms used for candidate stats");

				if (considerItem.getSnapId() == null) {
					continue;
				}
				if (considerItem.getSnapCount() < PathsConst.AUTO_MIN_CHILD_SNAP_COUNT) {
					continue;
				}
				if (considerItem.getSumPhraseRank() < PathsConst.AUTO_SUM_PHRASE_PROB_TO_ADD) {
					continue;
				}
				
				// add new child
				autoItems.add(considerItem);
				remainingKeyStatsPhrases = considerItem.getRemainingKeyStatsPhrasesAndForgetThem();
				childAdded = true;

//				System.out.println("---- " + _logPathsIdString + ": auto item #" + _items.size() +": '" + considerItem.getExtraSearch() + "'");
				
				break;
			}
			
			// not more children added
			if (childAdded == false) {
				break;
			}
			
			// max number of items
			if (autoItems.size() >= maxNodeCount) {
				break;
			}
		}
		
		List<Pair<Stats2Item, Double>> sortedItems = new ArrayList<>();
		for (int i=0; i<autoItems.size(); i++) {
			
			Stats2Item item = autoItems.get(i);
			
			List<Pair<Integer, Double>> keysSorted = item.getKeysSortedByExpectedWeight();
			
			double lastWeight = 1.0;
			WeightedAverage average = new WeightedAverage();
			for (int j=0; j<PathsConst.AUTO_SORTING_SPREAD_KEY_COUNT; j++) {
				double weight;
				if (j < keysSorted.size()) {
					weight = keysSorted.get(j).v2();
				} else {
					weight = lastWeight;
				}
				average.add(j+1, weight);
				lastWeight = weight;
			}
			double averageTime = average.get();
			double logAverageTime = StatsUtils.log2(averageTime);
			double itemRank = 1.0 / logAverageTime * item.getSumPhraseRank();
			sortedItems.add(new Pair<Stats2Item, Double>(item, itemRank));
		}
		if (sortedItems.size() > 1) {
			Collections.sort(sortedItems, new PairComparator<Stats2Item, Double>(SortOrder.Desc));
		}
		_nodes = new ArrayList<>();
		for (int i=0; i<sortedItems.size(); i++) {
			
			Stats2Item statsItem = sortedItems.get(i).v1();
			
			PathsNode pathsNode = new PathsNode(
					statsItem.getFullSentenceKeyIds(),
					statsItem.getHierSentenceKeyIds(),
					statsItem.getLeafSentenceKeyId(),
					statsItem.getFullSearch(),
					statsItem.getHierSearch(),
					statsItem.getLeafSearch(),
					statsItem.getSnapCount(),
					statsItem.getSnapId(),
					statsItem.getChildren());
			
			_nodes.add(pathsNode);
		}
		
		// add remaining snaps as extra nodes
		if (_nodes.size() < maxNodeCount) {
			
			Set<ObjectId> alreadyAddedSnapIds = new HashSet<>();
			SelectK<ObjectId, Date> selectRemainingSnapIds = new SelectK<>(SortOrder.Desc, maxNodeCount - _nodes.size());
			for (int i=0; i<remainingKeyStatsPhrases.size(); i++) {
				
				SnapPhrase phrase = remainingKeyStatsPhrases.get(i);
				Snap snap = phrase.getSnap();
				
				if (parentSnapIds.contains(snap.getId())) {
					continue;
				}
				if (alreadyAddedSnapIds.contains(snap.getId())) {
					continue;
				}
				
				selectRemainingSnapIds.add(new Pair<ObjectId, Date>(snap.getId(), snap.getSrcDate()));
				alreadyAddedSnapIds.add(snap.getId());
			}
			
			// select latest snap ids (up to the remaining depth)
			List<Pair<ObjectId, Date>> latestRemainingSnapIds = selectRemainingSnapIds.get();
			for (int i=0; i<latestRemainingSnapIds.size(); i++) {
				
				// add a node for the latest snap id
				ObjectId snapId = latestRemainingSnapIds.get(i).v1();
				PathsNode pathsNode;
				if (parent != null) {
					
					// populate from parent
					pathsNode = new PathsNode(
							queryKeyIds.getSentenceKeyIds(), 
							parent.getHierSentenceKeyIds(),
							null,
							parent.getFullSearch(),
							parent.getHierSearch(),
							null,
							null,
							snapId,
							null);
				} else {
					
					// try to fill full search
					pathsNode = new PathsNode(
							queryKeyIds.getSentenceKeyIds(), 
							null,
							null,
							noParentFullSearch,
							null,
							null,
							null,
							snapId,
							null);
				}
				_nodes.add(pathsNode);
			}
		}

	}
	
	public List<PathsNode> getNodes() {
		return _nodes;
	}

	public String getLogPathsIdString() {
		return _logPathsIdString;
	}
	
}
