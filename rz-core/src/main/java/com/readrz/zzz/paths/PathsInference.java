package com.readrz.zzz.paths;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.Pair;

import com.readrz.lang.Unstemmer;
import com.readrz.zzz.Phrase;
import com.readrz.zzz.PhrasePlace;
import com.readrz.zzz.parse.matches.AnyMatch;

public final class PathsInference {
	
	private final List<Phrase> _phrases;
	
	public PathsInference() {
		_phrases = new ArrayList<Phrase>();
	}
	
	public void addPhrase(Phrase phrase) {
		_phrases.add(phrase);
	}
	
	public PathsTree calculateTree(
			LevelConfigs levelConfigs,
			Set<String> ignoreStems) {
		
		if (levelConfigs == null || levelConfigs.getMaxLevel() == 0) {
			throw new IllegalArgumentException("Argument levelConfigs cannot be null or empty");
		}

		Path root = new Path(null, null, null, 1.0);
		calculatePathStatsFromData(root);
		
		Queue<Path> queue = new LinkedList<Path>();
		queue.add(root);
		
		while (queue.size() > 0) {
			
			Path parent = queue.poll();
			PathStats parentStats = parent.getStats();
			Set<String> parentPathAllConditionsStems = parent.getAllConditionsStems();
			List<Pair<String, Double>> parentStemsByExpectedWeight = parentStats.getStemsSortedByExpectedWeight();
			
			Path child1 = null;
			final int PARENT_MAX_CHECK_DEPTH = 10;
			
			for (int i=0; i<parentStemsByExpectedWeight.size() && i<PARENT_MAX_CHECK_DEPTH; i++) {
				
				String candidateStem = parentStemsByExpectedWeight.get(i).v1();
				
				if ((ignoreStems == null || ignoreStems.contains(candidateStem) == false)
					&& parentPathAllConditionsStems.contains(candidateStem) == false) {
					
					Double candidateStemProb = parentStats.getStemProbs().get(candidateStem);
					PathCondition candidatePathCond = new PathCondition(true, candidateStem);
					
					Path candidatePath = new Path(root, parent, candidatePathCond, candidateStemProb);
					calculatePathStatsFromData(candidatePath);
					
					child1 = candidatePath;
					parent.addChild(child1);
					
					if (child1.getLevel() < levelConfigs.getMaxLevel()) {
						queue.add(child1);
					}
					
					break;
				}
			}
			
			if (child1 != null) {
				
				String splitStem = child1.getLeafCondition().getStem();
				double child2LeafProbability = 1.0 - child1.getLeafProbability();

				PathCondition child2Cond = new PathCondition(false, splitStem);
				
				final int MAX_ADD_EXPECTED_WEIGHTS = 10;
				PathStats child1Stats = child1.getStats();
				List<Pair<String, Double>> child1StemsByExpectedWeight = child1Stats.getStemsSortedByExpectedWeight();
				int addedExpectedWeights = 0;
				for (int j=0; j<child1StemsByExpectedWeight.size() && addedExpectedWeights < MAX_ADD_EXPECTED_WEIGHTS; j++) {
					
					Pair<String, Double> pair = child1StemsByExpectedWeight.get(j);
					String stem = pair.v1();
					Double expectedWeight = pair.v2();
					
					if (splitStem.equals(stem) == false &&
						(ignoreStems == null || ignoreStems.contains(stem) == false)
						&& parentPathAllConditionsStems.contains(stem) == false) {

						child2Cond.addExpectedStemWeight(stem, expectedWeight);
						addedExpectedWeights += 1;
					}
				}
				
				Path child2 = new Path(root, parent, child2Cond, child2LeafProbability);
				calculatePathStatsFromData(child2);
				
				// check if any phrases left within path2
				if (child2.getStats().getSumAdjustedPhraseProb() > 0.0000001) {
				
					parent.addChild(child2);
	
					LevelConfig levelConfig = levelConfigs.get(child2.getLevel());
					
					if (child2.getStats().getSumAdjustedPhraseProb() > levelConfig.getMinimumPathWeight() &&
						child2.getLevelPositiveSiblingsCount() < levelConfig.getMaximumPositivePathsCount()) {
						
						queue.add(child2);
					}
				}
			}
		}
		
		return new PathsTree(ignoreStems, root);
	}
	
	private void calculatePathStatsFromData(Path path) {
		
		// create output objects
		PathStats stats = new PathStats();
		Unstemmer<String> unstemmer = new Unstemmer<String>();
		
		// get all conditions from the path
		List<PathCondition> conditions = path.getAllConditions();

		// observe phrases
		for (int i=0; i<_phrases.size(); i++) {
			
			// get phrase and its weights
			Phrase phrase = _phrases.get(i);
			Map<String, Double> phraseStemWeights = phrase.getStemWeights();
			
			// initialize observation prob
			double phraseProb = 1.0;
			
			// check all path conditions satisfied
			for (int j=0; j<conditions.size(); j++) {
				
				PathCondition cond = conditions.get(j);
				String condStem = cond.getStem();
				Double phraseCondStemWeight = phraseStemWeights.get(condStem);
				Map<String, Double> condExpectedStemWeights = cond.getExpectedStemWeights();
				
				// positive condition
				if (cond.isPositive()) { 
					
					if (phraseCondStemWeight == null) {
						phraseProb = 0;
						break;
					}
				} 
				else { // negative condition

					if (phraseCondStemWeight != null) {
						phraseProb = 0;
						break;
					}
					
					if (condExpectedStemWeights != null) {
						
						for (String stem : condExpectedStemWeights.keySet()) {
							
							Double expectedWeight = condExpectedStemWeights.get(stem);
							Double weight = phraseStemWeights.get(stem);
							
							if (weight != null) {
								phraseProb *= 1.0 - Math.min(weight, expectedWeight);
							}
						}
					}
				}
			}

			// add phrase observation
			if (phraseProb > 0) {

				// update stats
				stats.addPhraseObservation(phrase, phraseProb);
				
				// update unstemmer
				List<PhrasePlace> places = phrase.getPlaces();
				for (int j=0; j<places.size(); j++) {
					PhrasePlace place = places.get(j);
					AnyMatch match = place.getMatch();
					unstemmer.add(match.getStem(), match.getText(), 
							phraseProb * place.getWeight());
				}

			}
		}

		// normalize statistics
		stats.normalizeObservations();
		
		// optimize unstemmer
		unstemmer.optimize();
		
		// assign result
		stats.setWordsByStem(unstemmer.getWordsByKey());
		
		// assign stats to path
		path.setStats(stats);
	}

}
