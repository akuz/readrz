package com.readrz.lang.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.readrz.data.ontology.Entity;
import com.readrz.data.ontology.EntityList;

/**
 * Detects pattern entities occurrences.
 *
 */
public final class PatternsDetector {

	private final List<EntityList> _entityLists;
	private final List<Entity> _patternEntitiesList;
	private final List<Pair<Integer, Integer>> _patternEntitiesCounts;
	private int _totalDetectedEntitiesCount;
	private int _nextOptimizationDetectedEntitiesCount;
	private final Pattern _patternEntitiesRegex;
	
	public PatternsDetector(List<EntityList> entityLists) {
		this(entityLists, false);
	}

	public PatternsDetector(List<EntityList> entityLists, boolean caseInsensitive) {
		
		_entityLists = entityLists;
		_patternEntitiesList = new ArrayList<Entity>();
		_patternEntitiesCounts = new ArrayList<Pair<Integer,Integer>>();
		_nextOptimizationDetectedEntitiesCount = 100;
		_totalDetectedEntitiesCount = 0;
		
		int patternEntityIndex = 0;
		StringBuilder sb = new StringBuilder();
		if (_entityLists != null && _entityLists.size() > 0) {

			// after non-letter or at start
			sb.append("(?<=(?:\\W|^))(?:");

			for (int i=0; i<_entityLists.size(); i++) {
				
				EntityList entityList = _entityLists.get(i);
				BasicDBList list = entityList.getList();
				
				for (int j=0; j<list.size(); j++) {
					
					Entity entity = new Entity((DBObject)list.get(j));
					
					_patternEntitiesCounts.add(new Pair<Integer, Integer>(patternEntityIndex, 0));
					_patternEntitiesList.add(entity);
					patternEntityIndex++;
	
					if (i>0 || j>0) {
						sb.append("|");
					}
					sb.append("(");

					String entityId = entity.getId();
					String entityIdRegex = entityId.replaceAll("/", "\\/");
					sb.append("(?:");
					sb.append(entityIdRegex);
					sb.append(")");
					
					// check if entity has patterns
					BasicDBList patterns = entity.getPatterns();
					if (patterns != null) {
						for (int k=0; k<patterns.size(); k++) {
							sb.append("|");
							sb.append("(?:");
							
							String pattern = (String)patterns.get(k);
							// IMPORTANT: convert all capturing groups to non-capturing
							pattern = pattern.replaceAll("\\((?!\\?)", "(?:");
							sb.append(pattern);
							sb.append(")");
						}
					}
					
					sb.append(")");
				}
			}
			
			// followed by non-letter or end
			sb.append(")(?=(?:\\W|$))");
		}
		if (sb.length() > 0) {
			int flags = 0;
			if (caseInsensitive) {
				flags |= Pattern.CASE_INSENSITIVE;
			}
			_patternEntitiesRegex = Pattern.compile(sb.toString(), flags);
		} else {
			_patternEntitiesRegex = null;
		}
	}
	
	public Entity getPatternEntityByIndex(int index) {
		return _patternEntitiesList.get(index);
	}

	public Map<Integer, List<Hit>> extractHitsByEntityIndex(String str, Hit bounds) {

		Map<Integer, List<Hit>> hitsByEntityIndex = null;
		
		if (_patternEntitiesRegex != null && str != null) {

			Matcher matcher = _patternEntitiesRegex.matcher(str);
			matcher.region(bounds.start(), bounds.end());
			while (matcher.find()) {
				
				int entityIndex = getMatchedPatternEntityIndex(matcher);
				int matchStart = matcher.start(entityIndex+1);
				int matchEnd = matcher.end(entityIndex+1);
				
				if (matchStart >= matchEnd) {
					// can't match empty strings
					// possibly bad pattern
					// for entity
					continue;
				}

				Hit hit = new Hit(matchStart, matchEnd);

				// register the hit
				if (hitsByEntityIndex == null) {
					hitsByEntityIndex = new HashMap<Integer, List<Hit>>();
				}
				List<Hit> hits = hitsByEntityIndex.get(entityIndex);
				if (hits == null) {
					hits = new ArrayList<Hit>();
					hitsByEntityIndex.put(entityIndex, hits);
				}
				hits.add(hit);
			}
		}
		
		return hitsByEntityIndex;
	}

	private int getMatchedPatternEntityIndex(Matcher matcher) {
		
		for (int i=0; i<_patternEntitiesCounts.size(); i++) {
			Pair<Integer,Integer> pair = _patternEntitiesCounts.get(i);
			
			Integer index = pair.v1();
			Integer count = pair.v2();
			
			String m = matcher.group(index + 1);
			if (m != null && m.length() > 0) {
			
				_totalDetectedEntitiesCount += 1;
				if (_totalDetectedEntitiesCount >= _nextOptimizationDetectedEntitiesCount) {

					// optimize the order of checking the entities
					Collections.sort(_patternEntitiesCounts, new PairComparator<Integer, Integer>(SortOrder.Desc));
					_nextOptimizationDetectedEntitiesCount = (int)(1.5 * _nextOptimizationDetectedEntitiesCount);

					System.out.println("Optimized entity detector!");
					System.out.println("------ top entities ------");
					for (int k=0; k<10 && k<_patternEntitiesCounts.size(); k++) {
						Pair<Integer,Integer> pair2 = _patternEntitiesCounts.get(k);
						Integer index2 = pair2.v1();
						Integer count2 = pair2.v2();
						Entity entity = _patternEntitiesList.get(index2);
						System.out.println("" + count2 + "\t" + entity.getId());
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
