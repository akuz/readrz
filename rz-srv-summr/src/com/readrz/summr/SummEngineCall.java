package com.readrz.summr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.HashIndex;
import me.akuz.core.Hit;
import me.akuz.core.Index;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.logs.LogUtils;

import com.readrz.data.Period;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.Summ;
import com.readrz.data.SummId;
import com.readrz.data.SummKind;
import com.readrz.data.SummListItem;
import com.readrz.data.SummListItemKeyword;
import com.readrz.data.SummListItemSource;
import com.readrz.data.SummListItemQuote;
import com.readrz.data.SummListItemSourceItem;
import com.readrz.data.SummMenu;
import com.readrz.data.SummRequest;
import com.readrz.data.SummResult;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.lang.Ranking;
import com.readrz.lang.Unstemmer;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.lang.corpus.TopicsCorpus;
import com.readrz.math.merging.snaps.Cluster1;
import com.readrz.math.merging.snaps.Cluster1Algo;
import com.readrz.math.merging.snaps.ClusterDocument;
import com.readrz.math.merging.snaps.ClusterSentence;
import com.readrz.math.wordpaths.PathsConst;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.SnapSearch;
import com.readrz.search.SnapSearchResult;

/**
 * Summary calculation call, executed on a thread pool.
 *
 */
public final class SummEngineCall implements Callable<Boolean> {
	
	private static final int ERROR_SUMM_DYING_MINS = 10;
	private static final int ERROR_SUMM_DEAD_MINS  = 20;
	
	private static final int MIN_DOCUMENT_LENGTH = 3;
	private static final int MAX_SUMM_ITEM_COUNT = 100;
	
	private static final int SOURCE_ITEM_MAX_TITLE_LENGTH = 50;
	
	private final Logger _log;
	private final SummEngine _engine;
	private final boolean _isUserRequested;
	private final SummId _summId;
	private final String _summIdStr;
	private final Date _maxDateExc;
	private final ProgramOptions _options;
	private volatile boolean _isFinished;
	private SummKind _kind;
	private Period _period;
	private Date _minDateInc;
	private Exception _exception;
	
	public SummEngineCall(
			SummEngine engine,
			boolean isUserRequested,
			SummId summId,
			Date maxDateExc,
			ProgramOptions options) {
		
		_log = LogUtils.getLogger(this.getClass().getName());
		
		_engine = engine;
		_isUserRequested = isUserRequested;
		_summId = summId;
		_summIdStr = _summId.toString();
		_maxDateExc = maxDateExc;
		_options = options;
	}
	
	public boolean isUserRequested() {
		return _isUserRequested;
	}
	
	public SummId getSummId() {
		return _summId;
	}
	
	public ProgramOptions getOptions() {
		return _options;
	}
	
	@Override
	public Boolean call() {

		try {
			_log.fine("Calculating summary... " + _summIdStr);
			
			_kind = SummKind.getOrThrow(_summId.getKindId());
			_period = Period.getOrThrow(_summId.getPeriodId());
			_minDateInc = DateUtils.addMinutes(_maxDateExc, - _period.getLengthMins());

			if (_kind.equals(SummKind.List)) {
				createSumm_ListKind();
			} else if (_kind.equals(SummKind.Menu)) {
				createSumm_MenuKind();
			} else {
				throw new IllegalArgumentException("Unknown summary kind: " + _kind);
			}

		} catch (Exception ex) {
			
			// save error
			_exception = ex;
			
			// summary with error
			Summ summ = new Summ(_summId);
			summ.setError(ex.getClass().getSimpleName() + ": " + ex.getMessage());
			
			// save the result error summary
			Date createDate = _maxDateExc;
			Date dyingDate = DateUtils.addMinutes(createDate, ERROR_SUMM_DYING_MINS);
			Date deadDate = DateUtils.addMinutes(createDate, ERROR_SUMM_DEAD_MINS);
			SummResult result = new SummResult(_summId, createDate, dyingDate, deadDate, summ);
			result.upsertUnacknowledged(_engine.getSummColl());
		}
		
		_log.fine("Saving isRequested as false... " + _summIdStr);
		SummRequest upd = new SummRequest(_summId.getData(), false);
		upd.upsertUnacknowledged(_engine.getSummreqColl());
		
		_log.fine("Done summary. " + _summIdStr);

		_isFinished = true;
		return _exception != null;
	}

	private void createSumm_MenuKind() {
		
		_log.fine("Loading data... " + _summIdStr);
		QueryKeyIds queryKeyIds = new QueryKeyIds();
		queryKeyIds.addDocumentKeyIds(_summId.getSearchKeyIds());
		queryKeyIds.addDocumentKeyIds(_summId.getGroupKeyIds());
		SnapSearch snapSearch
			= _engine.getSnapSearcher()
				.startSearch(
					false, 
					queryKeyIds, 
					_minDateInc,
					_maxDateExc,
					PathsConst.ALL_CURSORS_LIMIT);
		
		// load data
		List<Snap> snaps = new ArrayList<>();
		while (true) {
			
			SnapSearchResult result = snapSearch.findNext();
			if (result == null) {
				break;
			}

			SnapInfo snapInfo = _engine.getSnapCache().get(result.getSnapId());
			if (snapInfo == null) {
				continue;
			}
			
			snaps.add(snapInfo.getSnap());
		}
		_log.fine("Loaded " + snaps.size() + " snaps. " + _summIdStr);

		_log.fine("Calculating group stats... " + _summIdStr);
		GroupStats groupStats = new GroupStats(snaps, queryKeyIds.getDocumentKeyIds());

		_log.fine("Preparing summary... " + _summIdStr);
		Summ summ = new Summ(_summId);
		{
			SummMenu menu 
				= SummMenuBuild.createSummMenu(
					"All", 
					snaps.size(), 
					groupStats, 
					_engine.getOntology().getPatternGroupCatalog(),
					_engine.getOntology().getTopicGroupCatalog());
			
			summ.setMenu(menu);
		}

		_log.fine("Saving summary... " + _summIdStr);
		Date createDate = _maxDateExc;
		Date dyingDate = DateUtils.addMinutes(createDate, _period.getDyingMins());
		Date deadDate = DateUtils.addMinutes(createDate, _period.getDeadMins());
		SummResult result = new SummResult(_summId, createDate, dyingDate, deadDate, summ);
		result.upsertUnacknowledged(_engine.getSummColl());
	}

	private void createSumm_ListKind() {
		
		final double TOTAL_MINUTES = DateUtils.minutesBetween(_minDateInc, _maxDateExc);
		final double MIN_MINUTES_DIFF_TO_MERGE = TOTAL_MINUTES / 12.0;
		final int ITERATION_COUNT = 6;
		
		_log.fine("Loading data... " + _summIdStr);
		QueryKeyIds queryKeyIds = new QueryKeyIds();
		queryKeyIds.addDocumentKeyIds(_summId.getSearchKeyIds());
		queryKeyIds.addDocumentKeyIds(_summId.getGroupKeyIds());
		SnapSearch snapSearch
			= _engine.getSnapSearcher()
				.startSearch(
					false, 
					queryKeyIds, 
					_minDateInc,
					_maxDateExc,
					PathsConst.ALL_CURSORS_LIMIT);
		
		// load data
		int snapCount = 0;
		List<ClusterSentence> sentences = new ArrayList<>();
		Map<Integer, Integer> keyIdByStemIndex = new HashMap<>();
		Index<String> stemsIndex = new HashIndex<>();
		while (true) {
			
			// get next search result
			SnapSearchResult result = snapSearch.findNext();
			if (result == null) {
				break;
			}

			SnapInfo snapInfo = _engine.getSnapCache().get(result.getSnapId());
			if (snapInfo == null) {
				continue;
			}
			
			Snap snap = snapInfo.getSnap();
			FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
			
			CorpusDoc corpusDoc 
				= TopicsCorpus.createDocForTopics(
						snap, 
						fwdHits, 
						true,
						_engine.getKeysIndex(), 
						_engine.getStopKeyIds(),
						stemsIndex,
						null,
						true);
			
			if (corpusDoc.getLength() < MIN_DOCUMENT_LENGTH) {
				continue;
			}
			
			ClusterDocument document = new ClusterDocument(snapInfo);

			List<CorpusSentence> corpusSentences = corpusDoc.getSentences();
			for (int sentenceIndex=0; sentenceIndex<corpusSentences.size(); sentenceIndex++) {
				
				CorpusSentence corpusSentence = corpusSentences.get(sentenceIndex);
				ClusterSentence sentence = new ClusterSentence(document, sentenceIndex, corpusSentence, keyIdByStemIndex);
				
				if (sentence.getStemCount() > 0) {
					
					sentences.add(sentence);
					document.addSentence(sentence);
				}
			}
			
			if (document.getSentences().size() > 0) {
				snapCount += 1;
			}
		}
		_log.fine("Loaded " + sentences.size() + " sentences from " + snapCount + " snaps. " + _summIdStr);

		_log.fine("Clustering... " + _summIdStr);
		Cluster1Algo clusterAlgo = new Cluster1Algo(sentences, MIN_MINUTES_DIFF_TO_MERGE, ITERATION_COUNT);
		List<Cluster1> clusters1 = clusterAlgo.getClusters1();
		_log.fine("Found " + clusters1.size() + " clusters. " + _summIdStr);
		
		_log.fine("Preparing summary... " + _summIdStr);
		Summ summ = new Summ(_summId);
		for (int i=0; i<clusters1.size() && i < MAX_SUMM_ITEM_COUNT; i++) {
			
			Cluster1 cluster1 = clusters1.get(i);
			SummListItem summItem = createSummListItem(keyIdByStemIndex, cluster1);
			summ.addListItem(summItem);
		}
		
		_log.fine("Saving summary... " + _summIdStr);
		Date createDate = _maxDateExc;
		Date dyingDate = DateUtils.addMinutes(createDate, _period.getDyingMins());
		Date deadDate = DateUtils.addMinutes(createDate, _period.getDeadMins());
		SummResult result = new SummResult(_summId, createDate, dyingDate, deadDate, summ);
		result.upsertUnacknowledged(_engine.getSummColl());
	}
	
	private SummListItem createSummListItem(Map<Integer, Integer> keyIdByStemIndex, Cluster1 cluster1) {
		
		Integer documentCount = cluster1.getDocumentCount();
		List<Pair<Integer, Double>> clusterStemIndexList = cluster1.getBaseCluster().getStemIndexList();
		
		// analyze documents
		double bestSnapRank = 0;
		SnapInfo bestSnapInfo = null;
		Map<String, List<Pair<Snap, Date>>> sourcesMap = null;
		Unstemmer<Integer> unstemmerByKeyId = null;
		if (documentCount > 1) {
			sourcesMap = new HashMap<>();
			unstemmerByKeyId = new Unstemmer<>();
		}
		for (Entry<ClusterDocument, Double> entry : cluster1.getDocumentRanks().entrySet()) {
			
			// get document info
			ClusterDocument doc = entry.getKey();
			Double rank = entry.getValue();
			SnapInfo snapInfo = doc.getSnapInfo();
			Snap snap = snapInfo.getSnap();

			// init snap rank
			double snapRank = rank;
			
			// increase if has image
			if (snap.isScanned() && 
				snap.isScannedImage()) {
				snapRank *= 2.0;
			}
			
			// multiply by recency
			snapRank *= Ranking.calcDateRecencyRank(snap.getSrcDate(), _minDateInc, _maxDateExc);

			// update best snap
			if (bestSnapRank < snapRank) {
				bestSnapRank = snapRank;
				bestSnapInfo = snapInfo;
			}
			
			if (documentCount > 1) {
				
				// add source item
				String sourceName = snapInfo.getSource().getName();
				List<Pair<Snap, Date>> sourceItems = sourcesMap.get(sourceName);
				if (sourceItems == null) {
					sourceItems = new ArrayList<>();
					sourcesMap.put(sourceName, sourceItems);
				}
				sourceItems.add(new Pair<Snap, Date>(snap, snap.getSrcDate()));
				
				// update unstemmer
				FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
				int sentenceIndex = 0;
				while (fwdHits.nextSentence()) {
					
					Hit sentenceHit = fwdHits.getSentenceHit();
					double sentenceIndexRank = ClusterSentence.calcSentenceIndexRank(sentenceIndex);
					
					FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(EnumSet.of(FwdHitKind.WORD, FwdHitKind.PATTERN));
					
					for (int i=0; i<clusterStemIndexList.size(); i++) {
						
						Integer stemIndex = clusterStemIndexList.get(i).v1();
						Integer keyId = keyIdByStemIndex.get(stemIndex);
						
						if (keyId != null && !_summId.getSearchKeyIds().contains(keyId)) {
							
							List<FwdHit> fwdHitsList = fwdHitsMap.getByKeyId().get(keyId);
							if (fwdHitsList != null && fwdHitsList.size() > 0) {
								
								for (int j=0; j<fwdHitsList.size(); j++) {
									
									FwdHit fwdHit = fwdHitsList.get(j);
									
									String word = snap.extractHitStr(sentenceHit, fwdHit.getHit());
									String capitalizedWord = StringUtils.capitalizeIfNoCaps(word);
									
									unstemmerByKeyId.add(keyId, capitalizedWord, sentenceIndexRank);
								}
							}
						}
					}
					
					sentenceIndex += 1;
				}
			}
		}
		
		// create quote
		SummListItemQuote quote = null;
		if (bestSnapInfo != null) {

			// use search key ids as *sentence* key ids
			QueryKeyIds quoteQueryKeyIds = new QueryKeyIds();
			quoteQueryKeyIds.addSentenceKeyIds(_summId.getSearchKeyIds());
			quoteQueryKeyIds.addDocumentKeyIds(_summId.getGroupKeyIds());

			quote = new SummListItemQuote(quoteQueryKeyIds, bestSnapInfo.getSnap(), bestSnapInfo.getSource());
		}

		// select keywords
		List<SummListItemKeyword> keywords = null;
		if (documentCount > 1) {
			
			// optimize unstemmer
			unstemmerByKeyId.optimize();
			
			// select unstemmed keywords
			for (int i=0; i<clusterStemIndexList.size(); i++) {
				
				Pair<Integer, Double> pair = clusterStemIndexList.get(i);
				Integer stemIndex = pair.v1();
				Double stemProb = pair.v2();
				Integer keyId = keyIdByStemIndex.get(stemIndex);
				
				if (keyId != null && !_summId.getSearchKeyIds().contains(keyId)) {
					
					String keyword = unstemmerByKeyId.getWordsByKey().get(keyId);
					if (keyword != null) {
						
						if (keywords == null) {
							keywords = new ArrayList<>();
						}
						keywords.add(new SummListItemKeyword(keyword, stemProb));
					}
				}
			}
		}
		
		// sort key words
		if (keywords != null && keywords.size() > 1) {
			Collections.sort(keywords, new SummItemKeywordSorter());
		}
		
		// sort members
		List<SummListItemSource> sources = null;
		if (sourcesMap != null) {
			
			sources = new ArrayList<>();
			
			for (Entry<String, List<Pair<Snap, Date>>> entry : sourcesMap.entrySet()) {
				
				// get source name
				String sourceName = entry.getKey();
				
				// sort source items
				List<Pair<Snap, Date>> sourceItems = entry.getValue();
				if (sourceItems.size() > 1) {
					Collections.sort(sourceItems, new PairComparator<Snap, Date>(SortOrder.Desc));
				}
				
				// create source
				SummListItemSource source = new SummListItemSource(
						sourceItems.size() > 1 
						? sourceName + " (" + sourceItems.size() + ")"
						: sourceName);
				sources.add(source);
				
				// create source item
				for (int i=0; i<sourceItems.size(); i++) {
					
					Snap snap = sourceItems.get(i).v1();
					Date snapDate = snap.getSrcDate();
					String dateAgo = DateUtils.formatAgo(snapDate, new Date());
					String dateShort = new UtcDate(snapDate, UtcDate.ShortDateFormatString).toString();
					String titleQuote = StringUtils.trimBySpace(snap.getTitle(), SOURCE_ITEM_MAX_TITLE_LENGTH);
					
					SummListItemSourceItem sourceItem = new SummListItemSourceItem(
							snap.getId().toString(), 
							dateAgo, 
							dateShort, 
							titleQuote);
					source.addItem(sourceItem);
				}
			}
			
			if (sources.size() > 1) {
				Collections.sort(sources, new SummItemMemberSorter());
			}
		}

		// create and return summary item
		return new SummListItem(documentCount, quote.getDbo(), keywords, sources);
	}

	public boolean isFinished() {
		return _isFinished;
	}
	
	public boolean hasException() {
		return _exception != null;
	}
	
	public Exception getException() {
		return _exception;
	}
	
	
	
	
	@Override
	public String toString() {
		return _summIdStr;
	}
}
