package com.readrz.zzz.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Index;
import me.akuz.core.Out;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.UtcDate;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Location;
import com.readrz.zzz.categ.EntityFeature;
import com.readrz.zzz.categ.OntologyOld;
import com.readrz.zzz.parse.matches.EntityMatch;

public final class EntitiesParserOld {

	private final Index<String> _stemsIndex;
	private final Index<String> _wordsIndex;
	private final List<EntityFeature> _entityFeaturesList;
	private final List<Pair<Integer, Integer>> _entityFeaturesIndexCount;
	private int _detectedEntitiesCount;
	private int _nextOptimizationDetectedEntitiesCount;
	private final Pattern _allEntitiesRegex;
	
	public EntitiesParserOld(OntologyOld ontology, Index<String> stemsIndex, Index<String> wordsIndex) {
		
		_stemsIndex = stemsIndex;
		_wordsIndex = wordsIndex;
		_entityFeaturesList = new ArrayList<EntityFeature>(ontology.getEntityFeatures().values());
		_entityFeaturesIndexCount = new ArrayList<Pair<Integer,Integer>>();
		for (int i=0; i<_entityFeaturesList.size(); i++) {
			_entityFeaturesIndexCount.add(new Pair<Integer, Integer>(i, 0));
		}
		_detectedEntitiesCount = 0;
		_nextOptimizationDetectedEntitiesCount = 100;
		
		StringBuilder sb = new StringBuilder();
		if (_entityFeaturesList != null && _entityFeaturesList.size() > 0) {

			// after non-letter or at start
			sb.append("(?<=(?:\\W|^))(?:");

			for (int i=0; i<_entityFeaturesList.size(); i++) {
				EntityFeature entityFeature = _entityFeaturesList.get(i);
				if (i>0) {
					sb.append("|");
				}
				sb.append("(");
				String[] patterns = entityFeature.getPatterns();
				for (int j=0; j<patterns.length; j++) {
					if (j>0) {
						sb.append("|");
					}
					sb.append("(?:");
					
					String pattern = patterns[j];
					// IMPORTANT: convert all capturing groups to non-capturing
					pattern = pattern.replaceAll("\\((?!\\?)", "(?:");
					sb.append(pattern);
					sb.append(")");
				}
				sb.append(")");
			}
			
			// followed by non-letter or end
			sb.append(")(?=(?:\\W|$))");
		}
		if (sb.length() > 0) {
			System.out.println(sb.toString());
			_allEntitiesRegex = Pattern.compile(sb.toString());
		} else {
			_allEntitiesRegex = null;
		}
	}
	
	public List<EntityFeature> getEntityFeaturesList() {
		return _entityFeaturesList;
	}

	public void extractMatches(int postId, UtcDate postDate, String str, FieldName field, Out<Map<EntityFeature, List<EntityMatch>>> outEntityMatchMap) {
		
		if (_allEntitiesRegex != null) {
			
			Matcher matcher = _allEntitiesRegex.matcher(str);
	
			while (matcher.find()) {
				
				int entityIndex = getMatchedEntityIndex(matcher);
				EntityFeature entityFeature = _entityFeaturesList.get(entityIndex);
				Location location = new Location(field, matcher.start(entityIndex+1), matcher.end(entityIndex+1));
				
				String word = _wordsIndex.ensureGetCachedValue(matcher.group(entityIndex+1));
				EntityMatch entityMatch = new EntityMatch(location, entityFeature, word);
				_stemsIndex.ensureGetCachedValue(entityMatch.getStem());
				
				Map<EntityFeature, List<EntityMatch>> entityMatchMap = outEntityMatchMap.getValue();
				if (entityMatchMap == null) {
					entityMatchMap = new HashMap<EntityFeature, List<EntityMatch>>();
					outEntityMatchMap.setValue(entityMatchMap);
				}
				List<EntityMatch> entityMatches = entityMatchMap.get(entityFeature);
				
				if (entityMatches == null) {
					entityMatches = new ArrayList<EntityMatch>();
					entityMatchMap.put(entityFeature, entityMatches);
				}
				entityMatches.add(entityMatch);
	
				System.out.println(
						"Post: " + postId + ", " +
						"Date: " + postDate + ", " +
						"Entity: " + entityFeature.getKey() + 
						" at " + location  + 
						": " + str.substring(location.getStart(), location.getEnd()));
			}
		}
	}

	private int getMatchedEntityIndex(Matcher matcher) {
		
		for (int i=0; i<_entityFeaturesIndexCount.size(); i++) {
			Pair<Integer,Integer> pair = _entityFeaturesIndexCount.get(i);
			
			Integer index = pair.v1();
			Integer count = pair.v2();
			
			String m = matcher.group(index + 1);
			if (m != null && m.length() > 0) {
			
				_detectedEntitiesCount += 1;
				if (_detectedEntitiesCount >= _nextOptimizationDetectedEntitiesCount) {

					// optimize the order of checking the entities
					Collections.sort(_entityFeaturesIndexCount, new PairComparator<Integer, Integer>(SortOrder.Desc));
					_nextOptimizationDetectedEntitiesCount = (int)(1.5 * _nextOptimizationDetectedEntitiesCount);

					System.out.println("Optimized entity detector!");
					System.out.println("------ top entities ------");
					for (int k=0; k<10 && k<_entityFeaturesIndexCount.size(); k++) {
						Pair<Integer,Integer> pair2 = _entityFeaturesIndexCount.get(k);
						Integer index2 = pair2.v1();
						Integer count2 = pair2.v2();
						EntityFeature entityFeature = _entityFeaturesList.get(index2);
						System.out.println("" + count2 + "\t" + entityFeature.getKey());
					}
					System.out.println("--------------------------");
				}
				
				pair.setV2(count + 1); 
				return index;
			}
		}

		throw new IllegalStateException("Cannot find which entity was found by matcher");
	}
	
}
