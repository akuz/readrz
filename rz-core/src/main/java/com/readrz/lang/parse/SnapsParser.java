package com.readrz.lang.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.akuz.core.Hit;
import me.akuz.nlp.detect.SentencesDetector;
import me.akuz.nlp.detect.WordsDetector;

import com.mongodb.BasicDBList;
import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsBuilder;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Entity;

/**
 * Parses snaps (title and text) into data structures that
 * identify where words and pattern entities are located.
 *
 */
public final class SnapsParser {
	
	private final KeysIndex _keysIndex;
	private final SentencesDetector _sentencesDetector;
	private final PatternsDetector _patternEntitiesParser;
	private final WordsDetector _wordsParser;
	
	public SnapsParser(
			KeysIndex keysIndex,
			SentencesDetector sentencesParser,
			PatternsDetector patternEntitiesParser,
			WordsDetector wordsParser) {
		
		_keysIndex = keysIndex;
		_sentencesDetector = sentencesParser;
		_patternEntitiesParser = patternEntitiesParser;
		_wordsParser = wordsParser;
	}
	
	public FwdHits parse(Snap snap) {

		// sentences bounds list (for each sentence)
		List<Hit> detectedSentenceHits = new ArrayList<Hit>();
		
		// add title sentence bounds, with index 0
		Hit titleBounds = snap.getPreprocessedTitleBounds();
		Hit titleHit = titleBounds.shift(-snap.getTitle().length());
		detectedSentenceHits.add(titleHit);
		
		// add text sentences bounds, with index > 0
		_sentencesDetector.parseSentences(snap.getText(), snap.getPreprocessedTextBounds(), detectedSentenceHits);
		
		// prepare result object
		FwdHitsBuilder fwdHitsBuilder = new FwdHitsBuilder();
		
		// parse individual sentences
		for (int sentenceIndex=0; sentenceIndex<detectedSentenceHits.size(); sentenceIndex++) {

			// index limited number of sentences
			if (fwdHitsBuilder.getSentenceCount() >= Const.MAX_INDEX_SENTENCES) {
				break;
			}
			
			// get sentence bounds & "extract from str" (title or text)
			Hit sentenceHit = detectedSentenceHits.get(sentenceIndex);
			String extractFromStr;
			Hit extractBounds;
			if (sentenceHit.start() < 0) {
				extractFromStr = snap.getTitle();
				extractBounds = sentenceHit.shift(extractFromStr.length());				
			} else {
				extractFromStr = snap.getText();
				extractBounds = sentenceHit;
			}
			if (extractBounds.length() <= 0) {
				continue;
			}
			
			// create map for fwd hits in sentence
			FwdHitsMap fwdHitsMap = new FwdHitsMap();
			
			// extract word hits
			{
				List<FwdHit> sentenceWordFwdHits = new ArrayList<FwdHit>();

				// use words detector
				Map<String, List<Hit>> wordHitsByStem
					= _wordsParser.extractHitsByStem(
						extractFromStr, extractBounds);
				
				// add word hits to hit-builders
				if (wordHitsByStem != null) {
					for (String stem : wordHitsByStem.keySet()) {
						
						List<Hit> stemHits = wordHitsByStem.get(stem);
	
						// add underscore to stems to indicate
						// that this key is coming from a stem
						Integer stemKeyId = _keysIndex.getId(stem);
	
						// transform to forward hits
						for (int i=0; i<stemHits.size(); i++) {
							Hit stemHit = stemHits.get(i);
							FwdHit fwdHit = new FwdHit(stemKeyId, stemHit);
							sentenceWordFwdHits.add(fwdHit);
						}
					}
				}
				
				fwdHitsMap.put(FwdHitKind.WORD, sentenceWordFwdHits);
			}

			// extract pattern hits
			{
				List<FwdHit> sentencePatternFwdHits = new ArrayList<FwdHit>();
				List<FwdHit> sentencePatternGroupFwdHits = new ArrayList<FwdHit>();

				// use pattern entities detector
				Map<Integer, List<Hit>> patternHitsByEntityIndex 
					= _patternEntitiesParser.extractHitsByEntityIndex(
						extractFromStr, extractBounds);
				
				// add entity hits to hit-builders
				if (patternHitsByEntityIndex != null) {
					for (Integer patternEntityIndex : patternHitsByEntityIndex.keySet()) {
						
						Entity entity = _patternEntitiesParser.getPatternEntityByIndex(patternEntityIndex);
						String entityId = entity.getId();
						List<Hit> patternHits = patternHitsByEntityIndex.get(patternEntityIndex);
						Integer keyId = _keysIndex.getId(entityId);
						
						// transform to forward hits
						for (int i=0; i<patternHits.size(); i++) {
							
							Hit patternHit = patternHits.get(i);
							
							// collect pattern forward hits
							{
								FwdHit fwdHit = new FwdHit(keyId, patternHit);
								sentencePatternFwdHits.add(fwdHit);
							}
							
							// collect pattern group forward hits
							{
								BasicDBList groupKeyIds = entity.getGroupKeyIds();
								if (groupKeyIds != null && groupKeyIds.size() > 0) {
									for (int j=0; j<groupKeyIds.size(); j++) {
										Integer groupKeyId = (Integer)groupKeyIds.get(j);
										FwdHit groupFwdHit = new FwdHit(groupKeyId, patternHit);
										sentencePatternGroupFwdHits.add(groupFwdHit);
									}
								}
							}
						}
					}
				}
				
				fwdHitsMap.put(FwdHitKind.PATTERN, sentencePatternFwdHits);
				fwdHitsMap.put(FwdHitKind.PATTERN_GROUP, sentencePatternGroupFwdHits);
			}
			
			// add result sentence
			fwdHitsBuilder.addSentenceHits(sentenceHit, fwdHitsMap);
		}

		return new FwdHits(fwdHitsBuilder.getData());
	}
}
