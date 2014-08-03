package com.readrz.zzz.categ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.akuz.core.Out;
import me.akuz.core.math.StatsUtils;

import com.readrz.zzz.ParsedPost;

/**
 * Feature that detects the presence of two other features *near each other*,
 * taking into account the distance between their respective detected locations.
 *
 */
public final class CrossFeature extends AbstractFeature implements Feature {
	
	private final static String _commonCrossFeaturesName = "Cross Feature";
	
	private String _key;
	private transient List<String> _subKeys;
	private final Feature _feature1;
	private final Feature _feature2;
	private final boolean _isUncrossable;
	private double _evaluatedSignal;
	private Set<FeatureMatch> _evaluatedMatches;
	
	public CrossFeature(Feature feature1, Feature feature2) {

		if (feature1.isUncrossable()) {
			throw new IllegalArgumentException("Cannot cross left feature because it is uncrossable");
		}
		if (feature2.isUncrossable()) {
			throw new IllegalArgumentException("Cannot cross right feature because it is uncrossable");
		}
		_isUncrossable = false;

		Set<String> keySet = new HashSet<String>();
		keySet.addAll(feature1.getSubKeys());
		keySet.addAll(feature2.getSubKeys());
		
		_subKeys = new ArrayList<String>(keySet.size());
		_key = prepareKeys(keySet, _subKeys);
		
		_feature1 = feature1;
		_feature2 = feature2;

		setName(_commonCrossFeaturesName);
	}
	
	public String getKey() {
		return _key;
	}
	
	public List<String> getSubKeys() {
		return _subKeys;
	}
	
	public boolean isUnpublished() {
		// always publish cross features
		return false;
	}
	
	public boolean isUncrossable() {
		return _isUncrossable;
	}

	public String[] getGroupKeys() {
		// never grouped
		return null;
	}
	
	private final static boolean containsCrossedFeature(List<FeatureMatch> crossedMatches, Feature feature) {
		
		for (int i=0; i<crossedMatches.size(); i++) {
			if (crossedMatches.get(i).getFeature().equals(feature)) {
				return true;
			}
		}
		return false;
	}

	private final static void updateBaseMatchesMeanStats(
			FeatureMatch match1,
			FeatureMatch match2, 
			Out<Double> sumAbsSignalTimesMatchIndex, 
			Out<Double> sumAbsSignal) {
		
		List<FeatureMatch> crossedMatches1 = match1.getCrossedMatches();
		List<FeatureMatch> crossedMatches2 = match2.getCrossedMatches();
	
		for (int i=0; i<crossedMatches2.size(); i++) {
			
			FeatureMatch crossedMatch2 = crossedMatches2.get(i);
			Feature feature2 = crossedMatch2.getFeature();
			
			if (containsCrossedFeature(crossedMatches1, feature2)) {
				sumAbsSignal.setValue(0.0);
				return;
			}
			
			double absSignal = Math.abs(crossedMatch2.getSignal());
			double absSignalTimesMatchIndex = absSignal * crossedMatch2.getMatchIndex();
			
			sumAbsSignalTimesMatchIndex.setValue(
					sumAbsSignalTimesMatchIndex.getValue() 
					+ absSignalTimesMatchIndex);
			
			sumAbsSignal.setValue(
					sumAbsSignal.getValue() 
					+ absSignal);
		}
	}

	private final static void minimizeBaseSignal(
			int combinedMatchIndex,
			FeatureMatch match1,
			FeatureMatch match2, 
			Out<Double> minBaseSignal) {
		
		List<FeatureMatch> crossedMatches1 = match1.getCrossedMatches();
		List<FeatureMatch> crossedMatches2 = match2.getCrossedMatches();
	
		for (int i=0; i<crossedMatches2.size(); i++) {
			
			FeatureMatch crossedMatch2 = crossedMatches2.get(i);
			Feature feature2 = crossedMatch2.getFeature();
			
			double adjAbsSignal;
			
			if (containsCrossedFeature(crossedMatches1, feature2)) {
				
				adjAbsSignal = 0.0;
			
			} else {
				
				double absSignal = Math.abs(crossedMatch2.getSignal());

				adjAbsSignal = absSignal
					* StatsUtils.calcDistanceWeightGaussian(
							combinedMatchIndex - crossedMatch2.getMatchIndex(), 
							CategConstants.CROSS_FEATURE_MATCHES_STDDEV);
			}
			
			if (minBaseSignal.getValue() > adjAbsSignal) {
				minBaseSignal.setValue(adjAbsSignal);
			}
		}
	}
	
	public boolean evaluate(ParsedPost parsedPost) {

		_evaluatedSignal = 0.0;
		_evaluatedMatches = null;
		boolean detected = false;

		Set<FeatureMatch> matches1 = _feature1.getEvaluatedMatches();
		Set<FeatureMatch> matches2 = _feature2.getEvaluatedMatches();

		if (matches1 != null && matches2 != null) {
			
			for (FeatureMatch match1 : matches1) {
				for (FeatureMatch match2 : matches2) {
					
					Out<Double> sumAbsSignalTimesMatchIndex = new Out<Double>(0.0); 
					Out<Double> sumAbsSignal = new Out<Double>(0.0);
					
					updateBaseMatchesMeanStats(match1, match2, sumAbsSignalTimesMatchIndex, sumAbsSignal);
					updateBaseMatchesMeanStats(match2, match1, sumAbsSignalTimesMatchIndex, sumAbsSignal);
					
					if (sumAbsSignal.getValue() > 0) {
						
						int combinedMatchIndex = (int)(sumAbsSignalTimesMatchIndex.getValue()/sumAbsSignal.getValue());

						Out<Double> minBaseSignal = new Out<Double>(1.0);
						minimizeBaseSignal(combinedMatchIndex, match1, match2, minBaseSignal);
						minimizeBaseSignal(combinedMatchIndex, match2, match1, minBaseSignal);
						
						double signal = minBaseSignal.getValue();

						if (signal >= CategConstants.MIN_FEATURE_DETECTION_SIGNAL) {
							
							List<FeatureMatch> crossedMatches = new ArrayList<FeatureMatch>(
									match1.getCrossedMatches().size() + 
									match2.getCrossedMatches().size());
							
							crossedMatches.addAll(match1.getCrossedMatches());
							crossedMatches.addAll(match2.getCrossedMatches());
							
							FeatureMatch featureMatch = new FeatureMatch(
									this, 
									combinedMatchIndex, 
									signal, 
									crossedMatches);
							
							if (_evaluatedSignal < signal) {
								_evaluatedSignal = signal;
							}
							
							if (_evaluatedMatches == null) {
								_evaluatedMatches = new HashSet<FeatureMatch>();
							}
							
							_evaluatedMatches.add(featureMatch);
							
							if (isUnpublished() == false) {
								parsedPost.addFeature(this, _evaluatedSignal, _evaluatedMatches);
							}
							
							detected = true;
						}
					}
				}
			}
		}
		
		return detected;
	}

	public Set<FeatureMatch> getEvaluatedMatches() {
		return _evaluatedMatches;
	}

	public double getEvaluatedSignal() {
		return _evaluatedSignal;
	}
}
