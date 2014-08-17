package com.readrz.math.wordpaths;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.logs.LogUtils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.readrz.data.Paths;
import com.readrz.data.PathsId;
import com.readrz.data.PathsItem;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.SnapPhrase;
import com.readrz.data.SnapPhraseSet;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.KeysIndex;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.QueryParser;
import com.readrz.search.SnapSearch;
import com.readrz.search.SnapSearchResult;
import com.readrz.search.SnapSearcher;

public final class PathsCall implements Callable<Boolean> {
	
	private static final Logger _log = LogUtils.getLogger(PathsCall.class.getName());
	
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final PathsId _pathsId;
	private final Date _maxDateExc;
	private final Set<Integer> _stopKeyIds;
	private final KeysIndex _keysIndex;
	private final String _pathsIdString;
	private final SnapSearcher _snapSearcher;
	private final DBCollection _pathsColl;
	
	public PathsCall(
		PathsId pathsId,
		Date maxDateExc,
		KeysIndex keysIndex,
		Set<Integer> stopKeyIds,
		QueryParser queryParser,
		SnapSearcher snapSearcher,
		DBCollection pathsColl) {

		if (maxDateExc == null) {
			throw new IllegalArgumentException("Max date must not be null");
		}
		
		_stopped = new CountDownLatch(1);
		
		_pathsId = pathsId;
		_maxDateExc = maxDateExc;
		_keysIndex = keysIndex;
		_stopKeyIds = stopKeyIds;
		_pathsIdString = pathsId.toString(keysIndex);
		_snapSearcher = snapSearcher;
		_pathsColl = pathsColl;
	}

	@Override
	public Boolean call() {
		
		try {

			long ms2 = System.currentTimeMillis();
			
			// init min date
			Date minDateInc = DateUtils.addMinutes(_maxDateExc, - _pathsId.getPeriod().getLengthMins());
			
			// prepare data structures
			List<SnapPhrase> phrases = new ArrayList<>();
			Set<ObjectId> snapIds = new HashSet<>();

			_log.fine("Selecting phrases for " + _pathsIdString + " (after " + minDateInc + ")...");

			SnapSearch snapSearch 
				= _snapSearcher.startSearch(
					false, 
					_pathsId.getQueryKeyIds(), 
					minDateInc, 
					_maxDateExc,
					PathsConst.ALL_CURSORS_LIMIT);
			
			try {
				while (true) {
					
					if (_stopRequested) {
						break;
					}
					
					SnapSearchResult result = snapSearch.findNext();
					if (result == null) {
						break;
					}
					
					Set<Integer> sentenceStarts = result.getSentenceHitStarts();
					ObjectId snapId = result.getSnapId();
					SnapInfo snapInfo = _snapSearcher.findSnapInfo(snapId);
					
					if (snapInfo == null) {
						continue;
					}
						
					Snap snap = snapInfo.getSnap();
					byte[] fwdHitsData = snap.getFwdHitsData();

					if (fwdHitsData != null) {
						SnapPhraseSet snapPhraseSet = new SnapPhraseSet(snap);
						FwdHits fwdHits = new FwdHits(fwdHitsData);
						while (fwdHits.nextSentence()) {
	
							Hit sentenceHit = fwdHits.getSentenceHit();
							
							if (sentenceStarts == null || sentenceStarts.contains(sentenceHit.start())) {
								
								FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(FwdHitKind.ALL);
								SnapPhrase prase = new SnapPhrase(snap, snapPhraseSet, sentenceHit, fwdHitsMap);
								snapPhraseSet.add(prase);
								phrases.add(prase);
								snapIds.add(snap.getId());
							}
						}
					}
				}
			} finally {
				snapSearch.close();
			}
			
			if (_stopRequested) {
				return null;
			}
			
			_log.fine("Found " + phrases.size() + " matching phrases in " + snapIds.size() + " snaps, " + _pathsIdString + "");
			
			_log.fine("Creating root item, " + _pathsIdString + "...");
			PathsItem rootItem = new PathsItem(
					null,
					null,
					null,
					null, 
					null,
					null,
					snapIds.size(), 
					null);
			
			BasicDBList autoItemsList;
			{
				_log.fine("Calculating auto items, " + _pathsIdString + "...");
				
				Set<ObjectId> ignoreSnapIds = new HashSet<>();
				Stats2CalcAuto auto = new Stats2CalcAuto(
						_pathsIdString,
						phrases,
						ignoreSnapIds,
						_pathsId.getQueryKeyIds(),
						null, 
						minDateInc, 
						_maxDateExc, 
						_stopKeyIds, 
						PathsConst.LEVEL_DEPTHS,
						PathsConst.MAX_LEVEL,
						1 // starting level
						);
				
				_log.fine("Converting auto items, " + _pathsIdString + "...");
				autoItemsList = convertPathsNodes(auto.getNodes());
			}

			BasicDBObject patternEntityItemsMap = new BasicDBObject();
			{
				_log.fine("Calculating pattern items stats, " + _pathsIdString + "...");
				Set<ObjectId> ignoreSnapIds = new HashSet<>();
				Stats2CalcByKind stats = new Stats2CalcByKind(
						_pathsIdString,
						ignoreSnapIds,
						phrases, 
						_pathsId.getQueryKeyIds(), 
						minDateInc, 
						_maxDateExc,
						_stopKeyIds, 
						PathsConst.LEVEL_DEPTHS,
						PathsConst.MAX_LEVEL, // max level
						1, // starting level
						FwdHitKind.PATTERN);

				_log.fine("Creating pattern entity items map, " + _pathsIdString + "...");
				Map<Integer, PathsNode> patternEntityPathsNodesMap = stats.getNodes(FwdHitKind.PATTERN);
				if (patternEntityPathsNodesMap != null) {
					for (Entry<Integer, PathsNode> entry : patternEntityPathsNodesMap.entrySet()) {
						
						Integer keyId = entry.getKey();
						PathsNode pathsNode = entry.getValue();
						
						DBObject pathsItemDbo = convertPathsNode(pathsNode);
						patternEntityItemsMap.put(keyId.toString(), pathsItemDbo);
					}
				}
			}

			BasicDBObject patternGroupItemsMap = new BasicDBObject();
			{
				_log.fine("Calculating pattern groups stats, " + _pathsIdString + "...");
				Stats1CalcByKind stats = new Stats1CalcByKind(
						phrases, 
						true,
						_pathsId.getQueryKeyIds(),
						true,
						FwdHitKind.PATTERN_GROUP);

				_log.fine("Creating pattern group items map, " + _pathsIdString + "...");
				Map<Integer, Stats1Item> statsItemsMap = stats.getItems(FwdHitKind.PATTERN_GROUP);
				if (statsItemsMap != null) {
					for (Entry<Integer, Stats1Item> entry : statsItemsMap.entrySet()) {
						
						Integer keyId = entry.getKey();
						Stats1Item statsItem = entry.getValue();
						
						PathsItem pathsItem = new PathsItem(
								null,
								null,
								null,
								null,
								_keysIndex.getStrCached(statsItem.getKeyId()), 
								statsItem.getKeyId(),
								statsItem.getSnapCount(), 
								null);
						
						patternGroupItemsMap.put(keyId.toString(), pathsItem.getDbo());
					}
				}
			}

			BasicDBObject topicGroupItemsMap = new BasicDBObject();
			{
				_log.fine("Calculating topic group stats, " + _pathsIdString + "...");
				
				// replace sentence key ids with senCheck key ids
				// in order to find extra stats relative to senCheck key ids
				QueryKeyIds topicQueryKeyIds = _pathsId.getQueryKeyIds().clone();
				topicQueryKeyIds.clearSentenceKeyIds();
				topicQueryKeyIds.addSentenceKeyIds(topicQueryKeyIds.getSenCheckKeyIds());
				topicQueryKeyIds.clearSenCheckKeyIds();
				
				Stats1CalcByKind stats = new Stats1CalcByKind(
						phrases, 
						true,
						topicQueryKeyIds,
						false,
						FwdHitKind.TOPIC_GROUP);

				_log.fine("Creating topic group items map, " + _pathsIdString + "...");
				Map<Integer, Stats1Item> statsItemsMap = stats.getItems(FwdHitKind.TOPIC_GROUP);
				if (statsItemsMap != null) {
					for (Entry<Integer, Stats1Item> entry : statsItemsMap.entrySet()) {
						
						Integer keyId = entry.getKey();
						Stats1Item statsItem = entry.getValue();
						
						PathsItem pathsItem = new PathsItem(
								null,
								null,
								null,
								null,
								_keysIndex.getStrCached(statsItem.getKeyId()), 
								statsItem.getKeyId(),
								statsItem.getSnapCount(), 
								null);
						
						topicGroupItemsMap.put(keyId.toString(), pathsItem.getDbo());
					}
				}
			}
			
			_log.fine("Calculating paths object, " + _pathsIdString + "...");
			Paths paths = new Paths(
					_pathsId.getData(), 
					_maxDateExc, 
					rootItem.getDbo(), 
					autoItemsList, 
					patternEntityItemsMap, 
					patternGroupItemsMap, 
					topicGroupItemsMap);
			
			_log.fine((System.currentTimeMillis() - ms2) 
					+ " ms used for calculating paths for " + _pathsIdString);
			
			_log.fine("Saving paths for " + _pathsIdString);
			paths.upsertUnacknowledged(_pathsColl);

			_log.fine("Done paths for " + _pathsIdString);
			
		} finally {
			System.gc();
			_stopped.countDown();
		}
		
		return true;
	}

	private DBObject convertPathsNode(PathsNode pathsNode) {
		List<PathsNode> list = new ArrayList<>();
		list.add(pathsNode);
		return (DBObject)convertPathsNodes(list).get(0);
	}

	private BasicDBList convertPathsNodes(List<PathsNode> pathsNodes) {
		BasicDBList rootList = new BasicDBList();
		Queue<Pair<BasicDBList, List<PathsNode>>> queue = new LinkedList<>();
		queue.add(new Pair<BasicDBList, List<PathsNode>>(rootList, pathsNodes));
		convertPathsNodes(queue);
		return rootList;
	}

	private void convertPathsNodes(Queue<Pair<BasicDBList, List<PathsNode>>> queue) {
		
		while (queue.size() > 0) {
			
			Pair<BasicDBList, List<PathsNode>> pair = queue.poll();
			BasicDBList addToList = pair.v1();
			List<PathsNode> pathsNodes = pair.v2();
			
			for (int i=0; i<pathsNodes.size(); i++) {

				// convert paths node
				PathsNode pathsNode = pathsNodes.get(i);
				PathsItem pathsItem;
				if (pathsNode.getLeafSentenceKeyId() != null) {
					
					pathsItem = new PathsItem(
							pathsNode.getFullSearch(), 
							pathsNode.getFullSentenceKeyIds(), 
							pathsNode.getHierSearch(), 
							pathsNode.getHierSentenceKeyIds(), 
							pathsNode.getLeafSearch(),
							pathsNode.getLeafSentenceKeyId(),
							pathsNode.getSnapCount(), 
							pathsNode.getSnapId());

				} else { // one snap node
					
					pathsItem = new PathsItem(
							pathsNode.getFullSearch(),
							pathsNode.getFullSentenceKeyIds(), 
							pathsNode.getHierSearch(),
							pathsNode.getHierSentenceKeyIds(), 
							null,
							null, 
							null, 
							pathsNode.getSnapId());
				}

				addToList.add(pathsItem.getDbo());

				// convert item children
				if (pathsNode.getChildren() != null && pathsNode.getChildren().size() > 0) {
					queue.add(new Pair<BasicDBList, List<PathsNode>>(pathsItem.ensureChildren(), pathsNode.getChildren()));
				}
			}
		}
	}
	
	public boolean isStopped() {
		return _stopped.getCount() == 0;
	}
	
	public final void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			_log.warning("Interrupted while waiting for paths call to stop");
		}
	}	

}
