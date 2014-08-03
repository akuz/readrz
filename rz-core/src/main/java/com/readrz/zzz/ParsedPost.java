package com.readrz.zzz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.math.StatsUtils;

import com.readrz.zzz.categ.EntityFeature;
import com.readrz.zzz.categ.Feature;
import com.readrz.zzz.categ.FeatureMatch;
import com.readrz.zzz.data.Post;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.BreakMatch;
import com.readrz.zzz.parse.matches.EntityMatch;
import com.readrz.zzz.parse.matches.WordMatch;

public final class ParsedPost {
	
	private final Post _post;
	private final List<AnyMatch> _allMatches;
	private final List<AnyMatch> _titleMatches;
	private final List<WordMatch> _wordMatches;
	private final Map<EntityFeature, List<EntityMatch>> _entityMatchMap;
	private Map<Feature, Pair<Double, Set<FeatureMatch>>> _featureMatches;
	
	public ParsedPost(
			Post post, 
			List<AnyMatch> allMatches, 
			List<AnyMatch> titleMatches,
			List<WordMatch> wordMatches,
			Map<EntityFeature, List<EntityMatch>> entityMatchMap) {
		
		_post = post;
		_allMatches = allMatches;
		_titleMatches = titleMatches;
		_wordMatches = wordMatches;
		_entityMatchMap = entityMatchMap;
	}
	
	public Post getPost() {
		return _post;
	}

	public List<AnyMatch> getAllMatches() {
		return _allMatches;
	}

	public List<AnyMatch> getTitleMatches() {
		return _titleMatches;
	}
	
	public List<WordMatch> getWordMatches() {
		return _wordMatches;
	}
	
	public Map<EntityFeature, List<EntityMatch>> getEntityMatchMap() {
		return _entityMatchMap;
	}
	
	public Map<Feature, Pair<Double, Set<FeatureMatch>>> getFeatureMatches() {
		return _featureMatches;
	}
	
	public void addFeature(Feature feature, Double signal, Set<FeatureMatch> matches) {
		if (_featureMatches == null) {
			_featureMatches = new HashMap<Feature, Pair<Double, Set<FeatureMatch>>>();
		}
		_featureMatches.put(feature, new Pair<Double, Set<FeatureMatch>>(signal, matches));
	}
	
	public List<Phrase> extractPhrases() {
		
		List<Phrase> phrases = new ArrayList<Phrase>();
		
		if (_entityMatchMap != null) {
			for (EntityFeature feature : _entityMatchMap.keySet()) {
				
				List<Phrase> entityPhrases = new ArrayList<Phrase>();
				List<EntityMatch> entityMatches = _entityMatchMap.get(feature);
				
				double sumEntityPhrasesWeight = 0;
				for (int i=0; i<entityMatches.size(); i++) {
					
					EntityMatch entityMatch = entityMatches.get(i);
					
					// add phrase words
					int entityMatchIndex = entityMatch.getMatchIndex();
					int maxIndexDeviation = (int)(3*Phrase.STEM_WEIGHT_MATCHES_FROM_CENTER_STD_DEV);
					boolean stoppedLeft = false;
					boolean stoppedRight = false;
					
					List<Pair<AnyMatch, Double>> weightedMatches = new ArrayList<Pair<AnyMatch,Double>>();

					// add entity match
					weightedMatches.add(new Pair<AnyMatch, Double>(entityMatch, 1.0));
					
					// add other matches
					for (int indexDeviation=1; indexDeviation<maxIndexDeviation; indexDeviation++) {
						
						double distanceWeight = StatsUtils.calcDistanceWeightGaussian(indexDeviation, Phrase.STEM_WEIGHT_MATCHES_FROM_CENTER_STD_DEV);

						if (!stoppedLeft) {
							int matchIndex = entityMatchIndex - indexDeviation;
							if (matchIndex < 0) {
								stoppedLeft = true;
							} else {
								AnyMatch match = _allMatches.get(matchIndex);
								if (match instanceof BreakMatch) {
									stoppedLeft = true;
								} else { 
									weightedMatches.add(new Pair<AnyMatch, Double>(match, distanceWeight));
								}
							}
						}

						if (!stoppedRight) {
							int matchIndex = entityMatchIndex + indexDeviation;
							if (matchIndex >= _allMatches.size()) {
								stoppedRight = true;
							} else {
								AnyMatch match = _allMatches.get(matchIndex);
								if (match instanceof BreakMatch) {
									stoppedRight = true;
								} else { 
									weightedMatches.add(new Pair<AnyMatch, Double>(match, distanceWeight));
								}
							}
						}
						
						if (stoppedLeft && stoppedRight) {
							break;
						}
					}
					
					// sort weighted matches by match index
					List<Pair<Pair<AnyMatch, Double>, Integer>> matchesSortedByMatchIndex = new ArrayList<Pair<Pair<AnyMatch, Double>, Integer>>();
					for (int m=0; m<weightedMatches.size(); m++) {
						Pair<AnyMatch, Double> pair = weightedMatches.get(m);
						matchesSortedByMatchIndex.add(new Pair<Pair<AnyMatch,Double>, Integer>(pair, pair.v1().getMatchIndex()));
					}
					Collections.sort(matchesSortedByMatchIndex, new PairComparator<Pair<AnyMatch, Double>, Integer>(SortOrder.Asc));
					
					// weight adjustment (title location)
					double titleLocationAdjustment = 1.0;
					if (entityMatch.getLocation().getFieldName().equals(FieldName.TITLE) == false) {
						titleLocationAdjustment = Phrase.NON_TITLE_PHRASE_WEIGHT_ADJUSTMENT;
					}
					
					// weight adjustment (mention number)
					double mentionNumberAdjustment = StatsUtils.calcDistanceWeightGaussian(
							i, Phrase.PHRASE_WEIGHT_MATCHES_FROM_START_STD_DEV);;
					
					// calculate phrase weight
					double phraseWeight = titleLocationAdjustment * mentionNumberAdjustment;
					
					sumEntityPhrasesWeight += phraseWeight;

					// create entity phrase
					Phrase phrase = new Phrase(
							_post.getSrcId(), 
							_post.getPostId(), 
							_post.getPublished(), 
							_titleMatches,
							entityMatch.getLocation().getFieldName(), 
							feature, 
							phraseWeight);
					
					entityPhrases.add(phrase);

					// add phrase places
					for (int m=0; m<matchesSortedByMatchIndex.size(); m++) {
						
						Pair<Pair<AnyMatch,Double>, Integer> pair = matchesSortedByMatchIndex.get(m);
						
						AnyMatch match = pair.v1().v1();
						double weight = pair.v1().v2();
						
						phrase.addPlace(match, weight);
					}
				}
				
				if (sumEntityPhrasesWeight > 0) {
					
					// normalize entity phrases weights
					for (int i=0; i<entityPhrases.size(); i++) {
						
						Phrase phrase = entityPhrases.get(i);
						
						// normalize phrase weight
						phrase.setWeight(phrase.getWeight() / sumEntityPhrasesWeight);
						
						// add phrase
						phrases.add(phrase);
					}
				}
			}
		}
		
		return phrases;
	}
}
