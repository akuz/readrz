package com.readrz.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import me.akuz.core.CompareUtils;
import me.akuz.core.Hit;
import me.akuz.core.SortOrder;
import me.akuz.core.logs.LogUtils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.readrz.data.Snap;
import com.readrz.data.index.Hits;
import com.readrz.data.index.Idx;

/**
 * Implements the search algorithm by iterating multiple inverse index
 * cursors and comparing the index entries to see if the document matches
 * all search requirements, and then returning matching documents.
 * @author andrey
 *
 */
public final class SnapSearch {
	
	private static Object _staticLock = new Object();
	private static Logger _log;
	private static final void initLog() {
		synchronized (_staticLock) {
			_log = LogUtils.getLogger(SnapSearch.class.getName());
		}
	}
	
	private final boolean _writeLog;
	private final List<Integer> _keyIds;
	private final int _documentKeyIdsStart;
	private final int _documentKeyIdsEnd;
	private final int _senCheckKeyIdsStart;
	private final int _sentenceKeyIdsStart;
	private final int _sentenceKeyIdsEnd;

	private final DBCollection _snapsColl;
	private final DBCollection _snapsidxColl;
	private final DBObject _snapGetFields;

	private final List<DBCursor> _cursors;
	private final List<Idx> _idxs;
	private boolean _isOpen;
	
	/**
	 * Creates search with parameters and opens all iteration cursors.
	 * @param log - doLogging (optional)
	 * @param queryKeyIds - query key ids
	 * @param minDateInc - minimum date for search
	 * @param maxDateExc - maximum date for search
	 * @param cursorsLimit - set upper limit on all cursors to prevent DB returning large chunks for cursors
	 * @param snapsColl - db collection of snaps (documents)
	 * @param snapsidxColl - db collection containing inverse index (from key ids to snaps)
	 * @param feedsColl - db collection of feeds (to be used when returning results)
	 * @param sourcesColl - db collection of sources (to be used when returning results)
	 * @param extensionKeyId - key id that must occur within the same sentence with sentenceKeyIds (if provided), and not overlap with them
	 */
	public SnapSearch(
			boolean doLog,
			QueryKeyIds queryKeyIds,
			Date minDateInc,
			Date maxDateExc,
			Integer cursorsLimit,
			DBCollection snapsColl,
			DBCollection snapsidxColl) {

		_writeLog = doLog;
		if (doLog) {
			initLog();
		}
		_keyIds = new ArrayList<Integer>();
		
		_documentKeyIdsStart = _keyIds.size();
		_keyIds.addAll(queryKeyIds.getDocumentKeyIds());
		_documentKeyIdsEnd = _keyIds.size();
		
		_senCheckKeyIdsStart = _keyIds.size();
		_keyIds.addAll(queryKeyIds.getSenCheckKeyIds());
		
		_sentenceKeyIdsStart = _keyIds.size();
		_keyIds.addAll(queryKeyIds.getSentenceKeyIds());
		_sentenceKeyIdsEnd = _keyIds.size();

		_snapsColl = snapsColl;
		_snapsidxColl = snapsidxColl;
		
		_snapGetFields = new BasicDBObject();
		_snapGetFields.put(Snap._isIndexed, true);
		_snapGetFields.put(Snap._isDupChecked, true);
		_snapGetFields.put(Snap._isDuplicate, true);
		_snapGetFields.put(Snap._feedIdField, true);
		
		_cursors = new ArrayList<DBCursor>();
		_idxs = new ArrayList<Idx>();
		boolean allCursorsOpen = false;

		try {
			
			BasicDBObject minDateCondition = null;
			if (minDateInc != null) {
				minDateCondition = new BasicDBObject()
					.append(Idx._srcDateField, new BasicDBObject()
						.append("$gte", minDateInc));
			}
			BasicDBObject maxDateCondition = null;
			if (maxDateExc != null) {
				maxDateCondition = new BasicDBObject()
					.append(Idx._srcDateField, new BasicDBObject()
						.append("$lt", maxDateExc));
			}
			
//			System.out.println("================================");
			if (_keyIds.size() > 0) {
				
				for (int i=0; i<_keyIds.size(); i++) {
					Integer keyId = _keyIds.get(i);
					if (keyId == null) {
						throw new IllegalArgumentException("KeyId cannot be null");
					}
					BasicDBObject keyCondition = new BasicDBObject();
					keyCondition.put(Idx._keyIdField, keyId);
					
					BasicDBObject query = null;
					BasicDBList andConditions = null;
					if (minDateCondition != null || maxDateCondition != null) {
						andConditions = new BasicDBList();
						andConditions.add(keyCondition);
						if (minDateCondition != null) {
							andConditions.add(minDateCondition);
						}
						if (maxDateCondition != null) {
							andConditions.add(maxDateCondition);
						}
						query = new BasicDBObject()
							.append("$and", andConditions);
					} else {
						query = keyCondition;
					}
					
					DBCursor cur = _snapsidxColl.find(query).hint(Idx.IdxSort).sort(Idx.IdxSort);
					if (cursorsLimit != null) {
						cur = cur.limit(cursorsLimit);
					}

	//				DBObject explain = cur.explain();
	//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	//				System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(query));
	//				System.out.println("------------------------------------");
	//				System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(explain));
	//				System.out.println("------------------------------------");
					_cursors.add(cur);
				}
				
			} else {
				
				BasicDBObject query;
				if (minDateCondition != null && maxDateCondition != null) {
					BasicDBList andConditions = new BasicDBList();
					andConditions.add(minDateCondition);
					andConditions.add(maxDateCondition);
					query = new BasicDBObject()
						.append("$and", andConditions);
				} else if (minDateCondition != null) {
					query = minDateCondition;
				} else if (maxDateCondition != null) {
					query = maxDateCondition;
				} else {
					query = new BasicDBObject();
				}
				
				DBCursor cur = _snapsColl.find(query, _snapGetFields);
				cur = cur.hint(Snap.idxSrcDateDesc).sort(Snap.idxSrcDateDesc);
				if (cursorsLimit != null) {
					cur = cur.limit(cursorsLimit);
				}
				
//				DBObject explain = cur.explain();
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//				System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(query));
//				System.out.println("------------------------------------");
//				System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(explain));
//				System.out.println("------------------------------------");
				_cursors.add(cur);
				
			}
//			System.out.println("================================");
			
			if (_writeLog) {
				_log.finest("All " + _cursors.size() + "cursors open");
			}
			allCursorsOpen = true;

		} finally {
			if (!allCursorsOpen) {
				close();
			}
		}
		
		_isOpen = allCursorsOpen;
		for (int i=0; i<_cursors.size(); i++) {
			_idxs.add(i, null);
		}
		if (_writeLog) {
			_log.finest("Initialized " + _cursors.size() + " values");
		}
	}
	
	public final void close() {
		if (_isOpen) {
			for (int i=0; i<_cursors.size(); i++) {
				_cursors.get(i).close();
			}
			_cursors.clear();
			_isOpen = false;
		}
	}

	public SnapSearchResult findNext() {

		if (_writeLog) {
			_log.finest("findNext");
		}

		if (!_isOpen) {
			if (_writeLog) {
				_log.finest("closed, returning null");
			}
			return null;
		}
		
		try {

			// if conditions present
			if (_keyIds.size() > 0) {
				
				int moveNextIndex = -1;
				List<Map<Integer, List<Hit>>> idxsHitsBySentenceStart = new ArrayList<Map<Integer, List<Hit>>>();
				
				while (_isOpen) {
				
					boolean moved = true;
		
					if (moveNextIndex < 0) {
						if (_writeLog) {
							_log.finest("moving *all* " + _cursors.size() + " cursors");
						}
						for (int i=0; i<_cursors.size(); i++) {
							DBCursor cur = _cursors.get(i);
							if (cur.hasNext()) {
								Idx idx = new Idx(cur.next());
								_idxs.set(i, idx);
							} else {
								moved = false;
								break;
							}
						}
					} else {
						if (_writeLog) {
							_log.finest("moving one cursor: " + moveNextIndex);
						}
						DBCursor cur = _cursors.get(moveNextIndex);
						if (cur.hasNext()) {
							Idx idx = new Idx(cur.next());
							_idxs.set(moveNextIndex, idx);
						} else {
							moved = false;
						}
					}
					
					if (moved == false) {
						if (_writeLog) {
							_log.finest("did not move, closing and returning null");
						}
						close();
						break;
					}
					
					moveNextIndex = -1;
					Date srcDate = null;
					ObjectId snapId = null;
					boolean isMatchDocument = true;
					if (_writeLog) {
						_log.finest("comparing " + _idxs.size() + " values idx");
					}
					for (int i=0; i<_idxs.size(); i++) {
						
						Idx idx = _idxs.get(i);
						
						if (_writeLog) {
							_log.finest("value idx " + i);
						}
	
						if (srcDate == null) {
							
							srcDate = idx.getSrcDate();
							snapId = idx.getItemId();
							moveNextIndex = i;
							
						} else {
							
							Date otherSrcDate = idx.getSrcDate();
							ObjectId otherItemId = idx.getItemId();
							
							int srcDateCmp = CompareUtils.compareNullsLowest(srcDate, otherSrcDate, SortOrder.Asc);
							if (srcDateCmp < 0) {
								
								isMatchDocument = false;
								srcDate = otherSrcDate;
								snapId = otherItemId;
								moveNextIndex = i;
								
							} else if (srcDateCmp > 0) {
								
								isMatchDocument = false;
								
							} else {
								
								int itemIdCmp = CompareUtils.compareNullsLowest(snapId, otherItemId, SortOrder.Asc);
								if (itemIdCmp < 0) {
									
									isMatchDocument = false;
									srcDate = otherSrcDate;
									snapId = otherItemId;
									moveNextIndex = i;
									
								} else if (itemIdCmp > 0) {
									
									isMatchDocument = false;
								}
							}
						}
					}
					
					if (isMatchDocument) {
	
						if (_writeLog) {
							_log.finest("matchDocument");
						}

						// reset sentence starts
						idxsHitsBySentenceStart.clear();
						
						// don't collect sentence hits for document keys
						for (int i=_documentKeyIdsStart; i<_documentKeyIdsEnd; i++) {
							idxsHitsBySentenceStart.add(null);
						}
						
						// collect sentence starts for sentence and senCheck keys
						for (int i=_senCheckKeyIdsStart; i<_sentenceKeyIdsEnd; i++) {
							Idx idx = _idxs.get(i);
							Hits hits = new Hits(idx.getHitsData());
							Map<Integer, List<Hit>> hitsBySentenceStart = new HashMap<Integer, List<Hit>>();
							while (hits.nextSentence()) {
								Hit sentenceHit = hits.getSentenceHit();
								List<Hit> sentenceHits = hits.getSentenceHitsList();
								hitsBySentenceStart.put(sentenceHit.start(), sentenceHits);
							}
							idxsHitsBySentenceStart.add(hitsBySentenceStart);
						}

						// init result sentence starts
						Set<Integer> resultSentenceStarts;
						if (_senCheckKeyIdsStart < _sentenceKeyIdsEnd) {
							
							resultSentenceStarts = new HashSet<>();
							resultSentenceStarts.addAll(idxsHitsBySentenceStart.get(_senCheckKeyIdsStart).keySet());

							// intersect with other sentence starts
							for (int i=_senCheckKeyIdsStart+1; i<_sentenceKeyIdsEnd; i++) {
								Set<Integer> sentenceStarts = idxsHitsBySentenceStart.get(i).keySet();
								resultSentenceStarts.retainAll(sentenceStarts);
								if (resultSentenceStarts.size() == 0) {
									break;
								}
							}
							
							// check there is a non-overlapping combination, if more than one sentence key
							if (resultSentenceStarts.size() > 0 && _sentenceKeyIdsStart < _sentenceKeyIdsEnd - 1) {
								
								// check all sentences
								Set<Integer> nonMatchSentenceStarts = null;
								for (Integer sentenceStart : resultSentenceStarts) {
									
									// non-satisfied by default, unless we find
									// a non-overlapping hits combination below
									boolean isSentenceMatchSatisfied = false;

									// create stack of sentence hits lists
									List<List<Hit>> stackHitsList = new ArrayList<>();
									for (int i=_sentenceKeyIdsStart; i<_sentenceKeyIdsEnd; i++) {
										List<Hit> hitsList = idxsHitsBySentenceStart.get(i).get(sentenceStart);
										stackHitsList.add(hitsList);
									}
									int[] stackIndex = new int[stackHitsList.size()];
									
									while (true) {
											
										// check if combination is non-overlapping
										boolean isCombinationNonOverlapping = true;
										for (int i=1; i<stackIndex.length; i++) {
											
											// get stack hit
											int index = stackIndex[i];
											Hit stackHit = stackHitsList.get(i).get(index);
											
											// check against other hits on stack
											for (int j=0; j<i; j++) {
												
												int otherIndex = stackIndex[j];
												Hit otherStackHit = stackHitsList.get(j).get(otherIndex);
												
												// check if overlaps with other hit on stack
												if (stackHit.overlaps(otherStackHit)) {
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
											isSentenceMatchSatisfied = true;
											break;
										}
										
										// continue checking next combinations
										boolean isMovedToOtherCombination = false;
										for (int i=0; i<stackIndex.length; i++) {
											
											// get stack hits
											int index = stackIndex[i];
											List<Hit> stackHits = stackHitsList.get(i);
											
											// check if can move
											if (index < stackHits.size() - 1) {
												
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
											break;
										}
										
									} // non-overlap check - while(true)
									
									// collect non-satisfied sentences
									if (!isSentenceMatchSatisfied) {
										if (nonMatchSentenceStarts == null) {
											nonMatchSentenceStarts = new HashSet<>();
										}
										nonMatchSentenceStarts.add(sentenceStart);
									}
								}
								
								// remove non-match sentences
								if (nonMatchSentenceStarts != null) {
									resultSentenceStarts.removeAll(nonMatchSentenceStarts);
								}
							}
							
						} else {
							// null means *all sentences*
							resultSentenceStarts = null;
						}

						// check there are result sentences that satisfy search keys, or
						// null means that there were only document keys, so no checks needed
						if (resultSentenceStarts == null || resultSentenceStarts.size() > 0) {
							
							Snap snap = Snap.findById(_snapsColl, snapId, _snapGetFields);
							if (snap.isIndexed() && snap.isDupChecked() && !snap.isDuplicate()) {
								
								SnapSearchResult result = new SnapSearchResult(snapId, resultSentenceStarts);
								return result;
							}
						}
						
						// move all cursors after match
						moveNextIndex = -1;
						
					} else {
						
						if (_writeLog) {
							_log.finest("not match");
						}
						
						// index of cursor to move already set
					}
				}

			} else {
				
				DBCursor cur = _cursors.get(0);
				
				while (_isOpen) {
					if (cur.hasNext()) {
	
						Snap snap = new Snap(cur.next());
						
						if (snap.isIndexed() && snap.isDupChecked() && !snap.isDuplicate()) {
	
							SnapSearchResult result = new SnapSearchResult(snap.getId(), null);
							return result;
						}
						
					} else {
						if (_writeLog) {
							_log.finest("did not move, closing and returning null");
						}
						close();
					}
				}
			}
			
			return null;

		} catch (Exception ex) {
			close();
			throw new IllegalStateException("Error finding next search entry, closing cursors",  ex) ;
		}
	}

}
