package com.readrz.math.topicdetect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.mongodb.BasicDBList;
import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsBuilder;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Entity;
import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;

/**
 * Provides functionality to first, calculate topic probabilities
 * for a document (and word-places within document), and second,
 * detect topic occurrences as either present or absent, using
 * the output probabilities from the previous step.
 */
public final class TopicsDetector {

	private final TopicsAnalyser _analyser;
	private final TopicModel _topicModel;
	private final KeysIndex _keysIndex;
	private final Integer _detectIgnoreTopicIndex;
	private final double _detectExpectedDocWordsThreshold;
	private final double _detectExpectedSenteceWordsThreshold;
	private final Map<Integer, Integer> _topicModelStemIndexByKeyId;

	public TopicsDetector(
			TopicModel topicModel, 
			KeysIndex keysIndex, 
			Set<Integer> stopKeyIds, 
			int collectTopTopicsCount,
			Integer detectIgnoreTopicIndex, 
			double detectExpectedDocWordsThreshold, 
			double detectExpectedSentenceWordsThreshold) {
		
		_analyser = new TopicsAnalyser(topicModel, keysIndex, stopKeyIds, collectTopTopicsCount);
		
		_topicModel = topicModel;
		_keysIndex = keysIndex;

		_detectIgnoreTopicIndex = detectIgnoreTopicIndex;
		_detectExpectedDocWordsThreshold = detectExpectedDocWordsThreshold;
		_detectExpectedSenteceWordsThreshold = detectExpectedSentenceWordsThreshold;
		
		_topicModelStemIndexByKeyId = new HashMap<>();
		for (int stemIndex=0; stemIndex<topicModel.getStemCount(); stemIndex++) {
			Integer keyId = topicModel.getKeyIdByStemIndex(stemIndex);
			_topicModelStemIndexByKeyId.put(keyId, stemIndex);
		}		
	}
	
	/**
	 * Creates corpus doc used for further topic detection.
	 */
	public CorpusDoc step0_createCorpusDoc(Snap snap, FwdHits fwdHits) {

		return _analyser.step0_createCorpusDoc(snap, fwdHits);
	}
	
	/**
	 * Calculate topic probs for each word-place, as well as for the whole document; 
	 * returns a corpus doc object which contains the output, with probs in tags:
	 * tags contain List<Pair<Integer, Double>> of top topic indices with probs.
	 */
	public void step1_calcTopicProbs(CorpusDoc doc) {
		
		final double alpha = 0.5 * (double)doc.getLength() / _topicModel.getTopicCount();
		
		_analyser.step1_calcTopicProbs(doc, alpha);
	}

	/**
	 * Uses the output from the previous step of calculating topic probs to
	 * do further processing and detect certain topics as present or absent
	 * within the document and at word-places; word-places tags in the output
	 * contain Integer (topic index) or null; the tag for the doc contains 
	 * List<Pair<Integer, Double>> listing detected topics and their probs;
	 * it also updates the topics and topic groups forward hits in the 
	 * parsed snap, if one provided with the call.
	 */
	public void step2_confirmTopics(CorpusDoc doc) {
		
		// get doc length
		final int docLength = doc.getLength();
		
		// choose detected (most likely) topics
		Map<Integer, Double> detectedTopics = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Pair<Integer, Double>> docDecectedTopicProbs = (List<Pair<Integer, Double>>)doc.getTag();
		for (int i=0; i<docDecectedTopicProbs.size(); i++) {
			Pair<Integer, Double> pair = docDecectedTopicProbs.get(i);
			Integer topicIndex = pair.v1();
			if (_detectIgnoreTopicIndex != null && _detectIgnoreTopicIndex.equals(pair.v1())) {
				continue;
			}
			Double topicProb = pair.v2();
			double topicExpectedDocWordsCount = topicProb * docLength;
			if (topicExpectedDocWordsCount > _detectExpectedDocWordsThreshold) {
				detectedTopics.put(topicIndex, topicProb);
			}
		}
		
		// not check that each detected topic
		// has been mentioned properly 
		// in at least one sentence
		Set<Integer> confirmedTopics = new HashSet<>();
		List<CorpusSentence> corpusSentences = doc.getSentences();
		for (int sentenceIndex=0; sentenceIndex<corpusSentences.size(); sentenceIndex++) {
			
			CorpusSentence corpusSentence = corpusSentences.get(sentenceIndex);
			List<CorpusPlace> places = corpusSentence.getPlaces();
			
			// calculate expected number of words by topic
			Map<Integer, Double> sentenceExpectedWordCountByTopicIndex = new HashMap<>();
			for (int i=0; i<places.size(); i++) {
				CorpusPlace place = places.get(i);
				if (place.getStemIndex() >= 0) {
					@SuppressWarnings("unchecked")
					List<Pair<Integer, Double>> placeTopicProbs = (List<Pair<Integer, Double>>)place.getTag();
					Pair<Integer, Double> pair = placeTopicProbs.get(0);
					Integer topicIndex = pair.v1();
					if (detectedTopics.containsKey(topicIndex)) {
						Double topicProb = pair.v2();
						Double expectedWordCount = sentenceExpectedWordCountByTopicIndex.get(topicIndex);
						if (expectedWordCount == null) {
							expectedWordCount = topicProb;
						} else {
							expectedWordCount += topicProb;
						}
						sentenceExpectedWordCountByTopicIndex.put(topicIndex, expectedWordCount);
					}
				}
			}
			
			for (int i=0; i<places.size(); i++) {
				CorpusPlace place = places.get(i);
				if (place.getStemIndex() >= 0) {
					@SuppressWarnings("unchecked")
					List<Pair<Integer, Double>> placeTopicProbs = (List<Pair<Integer, Double>>)place.getTag();
					Pair<Integer, Double> pair = placeTopicProbs.get(0);
					Integer topicIndex = pair.v1();
					if (detectedTopics.containsKey(topicIndex)) {
						Double expectedWordCount = sentenceExpectedWordCountByTopicIndex.get(topicIndex);
						if (expectedWordCount == null || expectedWordCount < _detectExpectedSenteceWordsThreshold) {
							place.setTag(null);
						} else {
							confirmedTopics.add(topicIndex);
							place.setTag(topicIndex);
						}
						sentenceExpectedWordCountByTopicIndex.put(topicIndex, expectedWordCount);
					} else {
						place.setTag(null);
					}
				}
			}
		}
		
		// keep only confirmed doc topic probs
		List<Pair<Integer, Double>> confirmedTopicProbs = new ArrayList<>();
		for (Integer topicIndex : confirmedTopics) {
			Double topicProb = detectedTopics.get(topicIndex);
			confirmedTopicProbs.add(new Pair<Integer, Double>(topicIndex, topicProb));
		}
		Collections.sort(confirmedTopicProbs, new PairComparator<Integer, Double>(SortOrder.Desc));
		doc.setTag(confirmedTopicProbs);
	}
	
	/**
	 * Updates FwdHits with the detected topics and topic groups occurrences.
	 */
	public FwdHits step3_updateFwdHits(CorpusDoc doc, FwdHits oldFwdHits) {
		
		FwdHitsBuilder newFwdHitsBuilder = new FwdHitsBuilder();
		
		oldFwdHits.reset();
		int oldSentenceIndex = 0;
		while (oldFwdHits.nextSentence()) {
			
			Hit oldSentenceHit = oldFwdHits.getSentenceHit();
			
			if (oldSentenceIndex >= doc.getSentenceCount()) {
				throw new IllegalStateException("CorpusDoc and old fwdHits sentence counts don't match");
			}
			CorpusSentence sentence = doc.getSentence(oldSentenceIndex);
			if (!oldSentenceHit.equals(sentence.getSentenceHit())) {
				throw new IllegalStateException("CorpusDoc and old FwdHits sentence hits don't match");
			}
			
			// populate topics and topic groups
			List<FwdHit> newTopicFwdHits = null;
			List<FwdHit> newTopicGroupFwdHits = null;
			for (int i=0; i<sentence.getPlaceCount(); i++) {
				
				CorpusPlace place = sentence.getPlace(i);
				Integer topicIndex = (Integer)place.getTag();
				
				if (topicIndex != null) {
					
					Entity topicEntity = _topicModel.getTopicEntity(topicIndex);
					Integer topicEntityKeyId = _keysIndex.getId(topicEntity.getId());

					{ // populate topic fwd hit
						
						if (newTopicFwdHits == null) {
							newTopicFwdHits = new ArrayList<>();
						}
						FwdHit topicFwdHit = new FwdHit(topicEntityKeyId, place.getFwdHit().getHit());
						newTopicFwdHits.add(topicFwdHit);
					}
					
					{ // populate topic groups fwd hits
						
						BasicDBList groupKeyIdsDbList = topicEntity.getGroupKeyIds();
						if (groupKeyIdsDbList != null && groupKeyIdsDbList.size() > 0) {
							
							for (int j=0; j<groupKeyIdsDbList.size(); j++) {
								Integer groupKeyId = (Integer)groupKeyIdsDbList.get(j);

								if (newTopicGroupFwdHits == null) {
									newTopicGroupFwdHits = new ArrayList<>();
								}
								FwdHit topicGroupFwdHit = new FwdHit(groupKeyId, place.getFwdHit().getHit());
								newTopicGroupFwdHits.add(topicGroupFwdHit);
							}
						}
					}					
				}
			}

			FwdHitsMap oldFwdHitsMap = oldFwdHits.getSentenceHits(FwdHitKind.ALL);
			FwdHitsMap newFwdHitsMap = new FwdHitsMap();
			
			// copy all fwd hits, except topics and topic groups
			for (Entry<FwdHitKind, List<FwdHit>> entry : oldFwdHitsMap.getByKind().entrySet()) {
				
				if (entry.getKey().equals(FwdHitKind.TOPIC)) {
					continue;
				}
				if (entry.getKey().equals(FwdHitKind.TOPIC_GROUP)) {
					continue;
				}
				newFwdHitsMap.put(entry.getKey(), entry.getValue());
			}
			// add topics fwd hits
			if (newTopicFwdHits != null) {
				newFwdHitsMap.put(FwdHitKind.TOPIC, newTopicFwdHits);
			}
			// add topic groups fwd hits
			if (newTopicGroupFwdHits != null) {
				newFwdHitsMap.put(FwdHitKind.TOPIC_GROUP, newTopicGroupFwdHits);
			}
			
			newFwdHitsBuilder.addSentenceHits(oldSentenceHit, newFwdHitsMap);

			oldSentenceIndex++;
		}
		
		return new FwdHits(newFwdHitsBuilder.getData());
	}
	
}
