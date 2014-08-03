package com.readrz.zzz.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.Out;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.UtcDate;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Location;
import com.readrz.zzz.ParsedPost;
import com.readrz.zzz.categ.EntityFeature;
import com.readrz.zzz.categ.Feature;
import com.readrz.zzz.categ.GroupFeature;
import com.readrz.zzz.categ.OntologyOld;
import com.readrz.zzz.categ.TopicFeature;
import com.readrz.zzz.data.Post;
import com.readrz.zzz.data.PostsListener;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.EntityMatch;
import com.readrz.zzz.parse.matches.WordMatch;

public final class PostsParser implements PostsListener {

	private final PostsParserListener[] _listeners;
	private final Index<String> _stemsIndex;
	private final Index<String> _wordsIndex;
	private final EntitiesParserOld _entitiesParser;
	private final WordsParserOld _wordsParser;
	private final BreaksParser _breaksParser;
	private final List<TopicFeature> _topicFeaturesList;
	private final List<GroupFeature> _groupFeaturesList;
	
	public PostsParser(
			PostsParserListener[] listeners,
			Set<String> stopStemSet,
			OntologyOld ontology) {
		
		_listeners = listeners;
		
		_stemsIndex = new HashIndex<String>();
		_wordsIndex = new HashIndex<String>();
		
		_entitiesParser = new EntitiesParserOld(ontology, _stemsIndex, _wordsIndex);
		_wordsParser = new WordsParserOld(stopStemSet, _stemsIndex, _wordsIndex);
		_breaksParser = new BreaksParser();

		// list topic features
		_topicFeaturesList = new ArrayList<TopicFeature>(ontology.getTopicFeatures().values());

		// list group features in reverse evaluation order
		_groupFeaturesList = new ArrayList<GroupFeature>();
		Queue<GroupFeature> queue = new LinkedList<GroupFeature>();
		queue.add(ontology.getRootGroup());
		while (queue.size() > 0) {
			
			GroupFeature group = queue.poll();
			_groupFeaturesList.add(group);
			
			List<GroupFeature> childGroups = group.getChildGroups();
			for (int i=0; i<childGroups.size(); i++) {
				queue.add(childGroups.get(i));
			}
		}
	}
	
	public ParsedPost parsePost(Date currDate, Post post) {

		// remove tickers in parentheses at the end
		String title = post.getTitle();
		String text = post.getText();
		
		// entity matches output map
		Out<Map<EntityFeature, List<EntityMatch>>> outEntityMatchMap = new Out<Map<EntityFeature,List<EntityMatch>>>(null);

		// create post UTC date
		UtcDate postUtcDate = new UtcDate(post.getDate());
		
		// look for entities in title
		_entitiesParser.extractMatches(post.getPostId(), postUtcDate, title, FieldName.TITLE, outEntityMatchMap);

		// look for entities in text
		_entitiesParser.extractMatches(post.getPostId(), postUtcDate, text, FieldName.TEXT, outEntityMatchMap);
		
		// prepare to sort entity and word matches
		List<Pair<AnyMatch, Location>> allMatchesSorted = new ArrayList<Pair<AnyMatch, Location>>();
		
		// collect entity matches
		List<Location> blockedLocations = null;
		Map<EntityFeature, List<EntityMatch>> entityMatchMap = outEntityMatchMap.getValue();
		if (entityMatchMap != null) {
			for (EntityFeature entityDef : entityMatchMap.keySet()) {
				
				List<EntityMatch> entityMatchList = entityMatchMap.get(entityDef);
				for (int i=0; i<entityMatchList.size(); i++) {
					
					EntityMatch entityMatch = entityMatchList.get(i);
					if (blockedLocations == null) {
						blockedLocations = new ArrayList<Location>();
					}
					blockedLocations.add(entityMatch.getLocation());
					allMatchesSorted.add(new Pair<AnyMatch, Location>(entityMatch, entityMatch.getLocation()));
				}
			}
		}
		Collections.sort(allMatchesSorted, new PairComparator<AnyMatch, Location>(SortOrder.Asc));
		
		// find word matches
		List<WordMatch> wordMatches = new ArrayList<WordMatch>();
		int allMatchesOverlapCheckSize = allMatchesSorted.size();
		_wordsParser.addWordMatches(FieldName.TITLE, title, wordMatches, allMatchesSorted, allMatchesOverlapCheckSize);
		_wordsParser.addWordMatches(FieldName.TEXT, text, wordMatches, allMatchesSorted, allMatchesOverlapCheckSize);
		Collections.sort(allMatchesSorted, new PairComparator<AnyMatch, Location>(SortOrder.Asc));

		// find sentence breaks
		_breaksParser.addBreakMatches(post, allMatchesSorted);
		Collections.sort(allMatchesSorted, new PairComparator<AnyMatch, Location>(SortOrder.Asc));
		
		// collect all matches and set indexes
		List<AnyMatch> allMatches = new ArrayList<AnyMatch>();
		List<AnyMatch> titleMatches = new ArrayList<AnyMatch>();
		for (int i=0; i<allMatchesSorted.size(); i++) {
			
			AnyMatch anyMatch = allMatchesSorted.get(i).v1();

			// update match index
			anyMatch.setMatchIndex(i);
			
			// add to all matches
			allMatches.add(anyMatch);
			
			// add to title matches
			if (anyMatch.getLocation().getFieldName() == FieldName.TITLE) {
				titleMatches.add(anyMatch);
			}
		}

		// create parsed text object common for all mentions
		ParsedPost parsedPost = new ParsedPost(post, allMatches, titleMatches, wordMatches, entityMatchMap);
		
		// evaluate topic features
		for (int i=_topicFeaturesList.size()-1; i>=0; i--) {
			Feature feature = _topicFeaturesList.get(i);
			feature.evaluate(parsedPost);
		}
		// evaluate entity features
		List<EntityFeature> entityFeaturesList = _entitiesParser.getEntityFeaturesList();
		for (int i=entityFeaturesList.size()-1; i>=0; i--) {
			Feature feature = entityFeaturesList.get(i);
			feature.evaluate(parsedPost);
		}
		// evaluate group features
		for (int i=_groupFeaturesList.size()-1; i>=0; i--) {
			Feature feature = _groupFeaturesList.get(i);
			feature.evaluate(parsedPost);
		}
		
		// FIXME: Commented out features crossing
		
//		Set<Feature> detectedFeaturesSet = parsedPost.getFeatureMatches() != null ? parsedPost.getFeatureMatches().keySet() : null;
//		
//		if (detectedFeaturesSet != null && detectedFeaturesSet.size() > 0) {
//			
//			// start crossing detected features
//			Set<Feature> detectedFeatures = new HashSet<Feature>();
//			detectedFeatures.addAll(detectedFeaturesSet);
//			Set<Feature> featuresToCross = new HashSet<Feature>();
//			featuresToCross.addAll(detectedFeaturesSet);
//			Set<Feature> newFeatures = new HashSet<Feature>();
//			newFeatures.addAll(detectedFeaturesSet);
//			
//			// repeat until stop condition
//			Set<Feature> analyzedCrossFeatures = new HashSet<Feature>();
//			while (featuresToCross.size() > 0) {
//				
//				newFeatures.clear();
//				
//				for (Feature feature1 : featuresToCross) {
//					for (Feature feature2 : detectedFeatures) {
//						
//						// check feature1 is crossable
//						if (feature1.isUncrossable()) {
//							continue;
//						}
//						// check feature2 is crossable
//						if (feature2.isUncrossable()) {
//							continue;
//						}
//						// don't cross if keys overlap
//						boolean keysOverlap = false;
//						List<String> keys1 = feature1.getSubKeys();
//						List<String> keys2 = feature2.getSubKeys();
//						for (int i=0; i<keys1.size(); i++) {
//							for (int j=0; j<keys2.size(); j++) {
//
//								String key1 = keys1.get(i);
//								String key2 = keys2.get(j);
//
//								if (key1.equals(key2)) {
//									keysOverlap = true;
//									break;
//								}
////								if (key1.startsWith(key2)) {
////									
////									keysOverlap = true;
////									break;
////									
////								} else if (key2.startsWith(key1)) {
////									
////									keysOverlap = true;
////									break;
////								}
//							}
//							if (keysOverlap) {
//								break;
//							}
//						}
//						if (keysOverlap) {
//							continue;
//						}
//						
//						// create cross feature, but don't evaluate yet
//						CrossFeature cross = new CrossFeature(feature1, feature2);
//	
//						// check this cross hasn't been calculated yet
//						if (analyzedCrossFeatures.contains(cross) == false) {
//							
//							// remember this cross as analyzed
//							analyzedCrossFeatures.add(cross);
//							
//							// it's new, evaluate cross
//							boolean detected = cross.evaluate(parsedPost);
//	
//							if (detected) {
//								
//								// if not deep enough, continue crossing
//								if (cross.getSubKeys().size() < 3) {
//									newFeatures.add(cross);
//								}
//							}
//						}
//					}
//				}
//				
//				featuresToCross.clear();
//				featuresToCross.addAll(newFeatures);
//			}
//		}
		 
		return parsedPost;
	}

	public void onPost(Date currDate, Post post) {
		
		if (_listeners != null && _listeners.length > 0) {

			ParsedPost parsedPost = parsePost(currDate, post);
			
			for (int i=0; i<_listeners.length; i++) {
				_listeners[i].onPostParsed(currDate, parsedPost);
			}
		}
	}

}
