package com.readrz.zzz.categ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.math.SigmoidFunc;
import me.akuz.core.math.StatsUtils;
import me.akuz.nlp.porter.PorterStemmerOrig;
import me.akuz.nlp.porter.PorterStemmerOrigUtils;

import com.readrz.zzz.ParsedPost;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.WordMatch;

/**
 * Feature that indicates a presence of a topic.
 *
 */
public final class TopicFeature extends AbstractFeature implements Feature, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String _key;
	private List<String> _subKeys;
	private boolean _isUnpublished;
	private String[] _groupKeys;
	private double _adjustmentFactor;
	private double _matchesAreaStdDev;
	private boolean _isStemmed;
	private Map<String, Double> _words;

	private transient Map<String, Double> _stems;
	private transient double _evaluatedSignal;
	private transient Set<FeatureMatch> _evaluatedMatches;
	
	/**
	 * Needed for deserialization.
	 */
	public TopicFeature() {
		// default non-null values
		_adjustmentFactor = 1.0;
		_matchesAreaStdDev = 10;
	}
	
	public TopicFeature(
			String key, String name, String[] groupKeys, 
			double adjustmentFactor, double matchesAreaStdDev) {
		
		_key = key;
		setName(name);
		_groupKeys = groupKeys;
		_adjustmentFactor = adjustmentFactor;
		_matchesAreaStdDev = matchesAreaStdDev;
		_words = new HashMap<String, Double>();
	}
	
	public String getKey() {
		return _key;
	}
	
	public List<String> getSubKeys() {
		if (_subKeys == null) {
			_subKeys = new ArrayList<String>(1);
			_subKeys.add(_key);
		}
		return _subKeys;
	}
	
	public String[] getGroupKeys() {
		return _groupKeys;
	}
	
	public double getMatchesAreaStdDev() {
		return _matchesAreaStdDev;
	}
	
	public double getAdjustmentFactor() {
		return _adjustmentFactor;
	}
	
	public Map<String, Double> getWords() {
		return _words;
	}
	
	private void ensureStemsInitialized() {
		
		if (_stems == null) {
			_stems = new HashMap<String, Double>();
		
			if (_words != null) {
				
				if (_isStemmed) {
					_stems.putAll(_words);
				} else {
					
					PorterStemmerOrig ps = new PorterStemmerOrig();
	
					for (String word : _words.keySet()) {
					
						String stem = PorterStemmerOrigUtils.stem(ps, word);
						Double weight = _words.get(word);
						_stems.put(stem, weight);
					}
				}
			}
		}
	}

	public boolean evaluate(ParsedPost parsedPost) {
		
		ensureStemsInitialized();
		
		_evaluatedSignal = 0.0;
		_evaluatedMatches = null;
		
		List<AnyMatch> allMatches = parsedPost.getAllMatches();
		List<WordMatch> wordMatches = parsedPost.getWordMatches();
		if (wordMatches != null && wordMatches.size() > 0) {
			
			final int matchesAreaStdDevTimesThree = (int)(_matchesAreaStdDev * 3.0);
			
			// evaluate at each word match
			for (int i=0; i<wordMatches.size(); i++) {

				// get the word match to evaluate at
				WordMatch wordMatch = wordMatches.get(i);
				int wordMatchIndex = wordMatch.getMatchIndex();

				// don't evaluate at words, which are not from topic
				if (_stems.containsKey(wordMatch.getStem()) == false) {
					continue;
				}
				
				// scan through the match area
				int weightedSum = 0;
				for (int otherMatchIndex = Math.max(0, wordMatchIndex - matchesAreaStdDevTimesThree); 
					otherMatchIndex <= Math.min(wordMatchIndex + matchesAreaStdDevTimesThree, allMatches.size()-1); 
					otherMatchIndex++) {
					
					AnyMatch otherMatch = allMatches.get(otherMatchIndex);
					if (otherMatch instanceof WordMatch == false) {
						continue;
					}
					
					WordMatch otherWordMatch = (WordMatch)otherMatch;
					
					String stem = otherWordMatch.getStem();
					
					Double weight = _stems.get(stem);
					
					if (weight != null) {
						// weight weights by distance to the area middle match
						double distanceWeight = StatsUtils.calcDistanceWeightGaussian(Math.abs(wordMatchIndex - otherMatchIndex), _matchesAreaStdDev);
						weightedSum += weight.doubleValue() * distanceWeight * _adjustmentFactor;
					}
				}
				
				if (weightedSum > 0) {
					
					double middleSignal = SigmoidFunc.sigmoid(weightedSum);
					
					if (middleSignal >= CategConstants.MIN_FEATURE_DETECTION_SIGNAL) {
						
						FeatureMatch featureMatch = new FeatureMatch(
								this, 
								wordMatchIndex, 
								middleSignal,
								null);
						
						if (_evaluatedMatches == null) {
							_evaluatedMatches = new HashSet<FeatureMatch>();
						}
						_evaluatedMatches.add(featureMatch);
						
						if (_evaluatedSignal < middleSignal) {
							_evaluatedSignal = middleSignal;
						}
					}
				}
			}
		}

		if (_evaluatedSignal >= CategConstants.MIN_FEATURE_DETECTION_SIGNAL) {
			if (isUnpublished() == false) {
				parsedPost.addFeature(this, _evaluatedSignal, _evaluatedMatches);
			}
			return true;
		} else {
			return false;
		}
	}

	public double getEvaluatedSignal() {
		return _evaluatedSignal;
	}

	public Set<FeatureMatch> getEvaluatedMatches() {
		return _evaluatedMatches;
	}

	@Override
	public String toString() {
		return getKey();
	}

	public boolean isUnpublished() {
		return _isUnpublished;
	}

	public boolean isUncrossable() {
		// always cross topic features
		return false;
	}
}
