package com.readrz.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;
import me.akuz.nlp.detect.WordsDetector;
import me.akuz.nlp.porter.PorterStemmer;

import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitsSorter;
import com.readrz.data.index.FwdHitsUtils;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Entity;
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.Ontology;
import com.readrz.lang.parse.GroupIdsDetector;
import com.readrz.lang.parse.PatternsDetector;

public final class QueryParser {
	
	private static final Pattern _matchSpacePattern = Pattern.compile("^\\*?\\s*$");
	private final KeysIndex _keysIndex;
	private final GroupIdsDetector _groupIdsDetector;
	private final PatternsDetector _patternsDetector;
	private final WordsDetector _wordsDetector;
	
	public QueryParser(PorterStemmer porterStemmer, KeysIndex keysIndex, Ontology ontology) {
		
		_keysIndex = keysIndex;
		_groupIdsDetector = new GroupIdsDetector(ontology.getTopicGroupCatalog(), ontology.getPatternGroupCatalog());
		_patternsDetector = new PatternsDetector(ontology.getEntityListCatalog().getLists(), true);
		_wordsDetector = new WordsDetector(porterStemmer);
	}
	
	public Query parse(String queryString) {
		
		Query query = new Query(queryString);
		
		// check if query is empty
		if (queryString != null) {

			// prepare query hit
			Hit queryStringHit = new Hit(queryString);

			// parse group ids
			Map<Integer, List<Hit>> groupIdHitsByGroupIndex = _groupIdsDetector.extractHitsByGroupIndex(queryString, queryStringHit);
			List<FwdHit> groupFwdHits = null;
			if (groupIdHitsByGroupIndex != null) {
				
				groupFwdHits = new ArrayList<FwdHit>();
				for (Integer groupIndex : groupIdHitsByGroupIndex.keySet()) {
					
					List<Hit> hits = groupIdHitsByGroupIndex.get(groupIndex);
					Group group = _groupIdsDetector.getGroupByIndex(groupIndex);
					Integer keyId = _keysIndex.getId(group.getId());
					for (int i=0; i<hits.size(); i++) {
						Hit hit = hits.get(i);
						FwdHit fwdHit = new FwdHit(keyId, hit);
						groupFwdHits.add(fwdHit);
					}
				}
				if (groupFwdHits.size() > 1) {
					Collections.sort(groupFwdHits, new FwdHitsSorter());
				}
			}

			// parse patterns
			Map<Integer, List<Hit>> patternHitsByEntityIndex = _patternsDetector.extractHitsByEntityIndex(queryString, queryStringHit);
			List<FwdHit> entityFwdHits = null;
			if (patternHitsByEntityIndex != null) {
				
				entityFwdHits = new ArrayList<FwdHit>();
				for (Integer patternEntityIndex : patternHitsByEntityIndex.keySet()) {
					
					List<Hit> hits = patternHitsByEntityIndex.get(patternEntityIndex);
					Entity patternEntity = _patternsDetector.getPatternEntityByIndex(patternEntityIndex);
					Integer keyId = _keysIndex.getId(patternEntity.getId());
					for (int i=0; i<hits.size(); i++) {
						Hit hit = hits.get(i);
						FwdHit fwdHit = new FwdHit(keyId, hit);
						entityFwdHits.add(fwdHit);
					}
				}
				if (entityFwdHits.size() > 1) {
					Collections.sort(entityFwdHits, new FwdHitsSorter());
				}
			}
			
			// parse words
			Map<String, List<Hit>> hitsByStem = _wordsDetector.extractHitsByStem(queryString, queryStringHit);
			List<FwdHit> wordFwdHits = null;
			if (hitsByStem != null) {
				
				wordFwdHits = new ArrayList<FwdHit>();
				for (String stem : hitsByStem.keySet()) {
					
					List<Hit> hits = hitsByStem.get(stem);
					Integer stemKeyId = _keysIndex.getIdCached(stem);
					if (stemKeyId == null) {
						query.setIsSomeTermsNotFound(true);
					} else {
						for (int i=0; i<hits.size(); i++) {
							Hit hit = hits.get(i);
							FwdHit fwdHit = new FwdHit(stemKeyId, hit);
							wordFwdHits.add(fwdHit);
						}
					}
				}
				if (wordFwdHits.size() > 1) {
					Collections.sort(wordFwdHits, new FwdHitsSorter());
				}
			}
			
			// add query terms for all identified hits
			List<FwdHit> uniqueFwdHits = FwdHitsUtils.getUniqueFwdHitsByPriority(groupFwdHits, entityFwdHits, wordFwdHits);
			addQueryTermsForFwdHits(queryString, query, uniqueFwdHits);
			
			// check spaces between matches
			if (!query.getIsSomeTermsNotFound()) {
				Hit prevHit = null;
				for (int i=0; i<uniqueFwdHits.size(); i++) {
					Hit hit = uniqueFwdHits.get(i).getHit();
					Matcher m = _matchSpacePattern.matcher(queryString);
					int start = prevHit == null ? queryStringHit.start() : prevHit.end();
					int end = hit.start();
					m.region(start, end);
					if (!m.matches()) {
						query.setIsSomeTermsNotFound(true);
						break;
					}
					prevHit = hit;
				}
				if (!query.getIsSomeTermsNotFound()) {
					Matcher m = _matchSpacePattern.matcher(queryString);
					if (prevHit == null) {
						int start = queryStringHit.start();
						int end = queryStringHit.end();
						m.region(start, end);
					} else {
						int start = prevHit.end();
						int end = queryStringHit.end();
						m.region(start, end);
					}
					if (!m.matches()) {
						query.setIsSomeTermsNotFound(true);
					}
				}
			}
		}
		
		return query;
	}
	
	private static final void addQueryTermsForFwdHits(String queryString, Query query, List<FwdHit> uniqueFwdHits) {
		
		for (int i=0; i<uniqueFwdHits.size(); i++) {
			
			FwdHit addFwdHit = uniqueFwdHits.get(i);
			
			// check if added or overlaps
			int keyId = addFwdHit.getKeyId();
			Hit hit = addFwdHit.getHit();
			String origWord = queryString.substring(hit.start(), hit.end());
			QueryTerm term = new QueryTerm(hit, keyId, origWord);
			if (queryString.length() > hit.end()) {
				char charAfter = queryString.charAt(hit.end());
				if (charAfter == '*') {
					term.setIsDocumentLevel(true);
				}
			}
			query.addTerm(term);
		}
	}
	
}
