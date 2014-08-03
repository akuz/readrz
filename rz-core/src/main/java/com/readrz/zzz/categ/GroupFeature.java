package com.readrz.zzz.categ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.readrz.zzz.ParsedPost;

/**
 * Feature that indicates the presence of other Features.
 * 
 */
public final class GroupFeature extends AbstractFeature implements Feature {
	
	private final String _key;
	private List<String> _subKeys;
	private final boolean _isUncrossable;
	private final boolean _isTaxonomy;
	private final boolean _isSecondaryTaxonomy;
	private final boolean _isIgnoredForFullName;
	private final Map<String, Feature> _subFeatures;
	private final List<String> _subFeaturesAddedOrder;
	private final List<GroupFeature> _childGroups;
	private final List<Feature> _leafFeatures;
	
	private double _evaluatedSignal;
	private Set<FeatureMatch> _evaluatedMatches;
	
	public GroupFeature(
			String key, 
			String name, 
			boolean isUncrossable, 
			boolean isTaxonomy, 
			boolean isSecondaryTaxonomy,
			boolean isIgnoredForFullName) {
		
		_key = key;
		setName(name);
		_isUncrossable = isUncrossable;
		_isTaxonomy = isTaxonomy;
		_isSecondaryTaxonomy = isSecondaryTaxonomy;
		_isIgnoredForFullName = isIgnoredForFullName;
		_subFeatures = new HashMap<String, Feature>();
		_subFeaturesAddedOrder = new ArrayList<String>();
		_childGroups = new ArrayList<GroupFeature>();
		_leafFeatures = new ArrayList<Feature>();
	}
	
	public String getKey() {
		return _key;
	}
	
	public List<String> getSubKeys() {
		if (_subKeys == null) {
			_subKeys = new ArrayList<String>(_key == null ? 0 : 1);
			if (_key != null) {
				_subKeys.add(_key);
			}
		}
		return _subKeys;
	}
	
	public boolean isTaxonomy() {
		return _isTaxonomy;
	}
	
	public boolean isSecondaryTaxonomy() {
		return _isSecondaryTaxonomy;
	}
	
	public boolean isIgnoredForFullName() {
		return _isIgnoredForFullName;
	}
	
	public List<Feature> getLeafFeatures() {
		return _leafFeatures;
	}
	
	public void addLeafFeature(Feature leafFeature) {
		addSubFeatureCheckNotExists(leafFeature);
		_leafFeatures.add(leafFeature);
	}
	
	public List<GroupFeature> getChildGroups() {
		return _childGroups;
	}
	
	public void addChildGroup(GroupFeature group) {
		addSubFeatureCheckNotExists(group);
		_childGroups.add(group);
	}
	
	private void addSubFeatureCheckNotExists(Feature feature) {
		if (_subFeatures.containsKey(feature.getKey())) {
			throw new IllegalStateException("Child feature " + feature.getKey() 
					+ " is already added to group feature " + getKey());
		}
		feature.setParentGroup(this);
		_subFeatures.put(feature.getKey(), feature);
		_subFeaturesAddedOrder.add(feature.getKey());
	}
	
	public List<Feature> extractLeafFeatures(boolean includingSecondaryTaxonomies) {
		
		Set<Feature> featureSet = new HashSet<Feature>();
		Queue<GroupFeature> queue = new LinkedList<GroupFeature>();
		
		if (includingSecondaryTaxonomies == true ||
			this.isSecondaryTaxonomy() == false) {
			
			queue.add(this);
		}

		while (queue.size() > 0) {
			
			GroupFeature group = queue.poll();
			
			// populate leaf features
			for (int i=0; i<group._leafFeatures.size(); i++) {
				featureSet.add(group._leafFeatures.get(i));
			}
			
			// process child groups
			for (int i=0; i<group._childGroups.size(); i++) {
				
				if (includingSecondaryTaxonomies ||
					group.isSecondaryTaxonomy() == false) {
				
					queue.add(group._childGroups.get(i));
				}
			}
		}
		
		List<Feature> featureList = new ArrayList<Feature>(featureSet.size());
		featureList.addAll(featureSet);
		return featureList;
	}

	public List<Pair<GroupFeature, Double>> extractSortedChildGroups(
			Map<GroupFeature, Double> groupSortNumbers,
			SortOrder sortOrder) {
		
		// extract child groups
		List<Pair<GroupFeature, Double>> sortedChildGroups = new ArrayList<Pair<GroupFeature,Double>>();
		for (String childKey : _subFeatures.keySet()) {
			
			Feature childFeature = _subFeatures.get(childKey);
			
			if (childFeature instanceof GroupFeature) {
				
				GroupFeature childGroup = (GroupFeature)childFeature;
				Double groupSortNumber = groupSortNumbers.get(childGroup);
				if (groupSortNumber != null) {
					sortedChildGroups.add(new Pair<GroupFeature, Double>(childGroup, groupSortNumber));
				}
			}
		}

		// sort child group features by numbers desc
		Collections.sort(sortedChildGroups, new PairComparator<GroupFeature, Double>(sortOrder));

		return sortedChildGroups;
	}
	
	public boolean evaluate(ParsedPost parsedPost) {
		
		_evaluatedSignal = 0.0;
		_evaluatedMatches = null;
		
		// check all child features
		for (int f=0; f<_subFeaturesAddedOrder.size(); f++) {
			
			String childKey = _subFeaturesAddedOrder.get(f);
			Feature childFeature = _subFeatures.get(childKey);
			
			// merge absolute matches into a single group feature matches list
			Set<FeatureMatch> childMatches = childFeature.getEvaluatedMatches();
			if (childMatches != null) {
				
				if (_evaluatedMatches == null) {
					_evaluatedMatches = new HashSet<FeatureMatch>();
				}
				for (FeatureMatch childMatch : childMatches) {
					
					_evaluatedMatches.add(childMatch);
					
					if (_evaluatedSignal < childMatch.getSignal()) {
						_evaluatedSignal = childMatch.getSignal();
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
	
	public Set<FeatureMatch> getEvaluatedMatches() {
		return _evaluatedMatches;
	}
	
	public double getEvaluatedSignal() {
		return _evaluatedSignal;
	}
	
	@Override
	public String toString() {
		return getKey();
	}

	public boolean isUnpublished() {
		// always publish groups
		return false;
	}

	public boolean isUncrossable() {
		return _isUncrossable;
	}

	public String[] getGroupKeys() {
		// group feature cannot be added 
		// to another group feature
		return null;
	}
}
